package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create a chat room (like zoom chat private msg), supporting:
 * - open *general* messages to all users
 * - private messages to a specific *user*
 *
 * <p>
 * Each _05_observer will receive messages from its queue with 2 Binding keys:
 * - room1.general (public msg for general/all users) and room1.pedro (private msg for given user)
 *
 * <p>
 * Send message with specific Routing keys:
 * - routingKey="room1.general" (public to general/all users)
 * - routingKey="room1.pedro"   (private to specific user)
 *
 * <p>
 * Run _05_observer with 3 parameters <room> <user> <general>:
 * $ runobserver room1 pedro general
 *
 *
 * @author rui
 */
public class ObserverServer {

    //Reference for gui
    private  ServerGuiClient gui;


    private  HashMap<String,ArrayList<String>> lobbys_and_players =new HashMap<>();// Nr de Lobby e o nome do jogador no lobby
    private  HashMap<String,Integer> lobbys_and_level =new HashMap<>();// Nr de Lobby e qts jogadores lá estão
    private  HashMap<String,Integer> lobbys_and_size =new HashMap<>();// Nr de Lobby e qts jogadores lá estão
    private Boolean state=false;




    //Preferences for exchange...
    private  Channel channelToRabbitMq;
    private  Channel channelToRabbitMq_Servers;
    private Channel channelToRabbitMqFrontServer;
    private  String exchangeName;
    private  BuiltinExchangeType exchangeType;
    private  String[] exchangeBindingKeys;
    private  String messageFormat;
    private String receivedMessage;
    //Settings for specifying topics
//    private  String room;
//    private  String user;
//    private  String general;
//    private Main Game;
    //Store received message to be get by gui
    public boolean gameState = false;

    public ObserverServer() {
    }

    /**
     * @param gui
     */
    public void setServer(ServerGuiClient gui, String host, int port, String user, String pass, String exchangeName,String queueName,String queueNameFrontServer,BuiltinExchangeType exchangeType, String messageFormat) throws IOException, TimeoutException {
        this.gui=gui;
        this.exchangeName=exchangeName;
        this.messageFormat=messageFormat;
        this.exchangeType=exchangeType;

        Connection connection=RabbitUtils.newConnection2Server(host, port, user, pass);
        this.channelToRabbitMq=RabbitUtils.createChannel2Server(connection);
        this.channelToRabbitMq_Servers=RabbitUtils.createChannel2Server(connection);
        this.channelToRabbitMqFrontServer=RabbitUtils.createChannel2Server(connection);
        this.channelToRabbitMqFrontServer.queueDeclare(queueNameFrontServer, false, false, false, null);


        bindExchangeToChannelRabbitMQ();
        attachConsumerToChannelExchangeWithKey();

        JSONObject json = new JSONObject();
        json.put("operation","ServerOn");
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;

        channelToRabbitMqFrontServer.basicPublish("",queueNameFrontServer,prop,json.toString().getBytes("UTF-8"));
//        sendMessageFrontServer(json.toString());

        /* Declare a queue as Durable (queue won't be lost even if RabbitMQ restarts);
            NB: RabbitMQ doesn't allow to redefine an existing queue with different
            parameters, need to create a new one */
        boolean durable = true;
        //channel.queueDeclare(Send.QUEUE_NAME, false, false, false, null);
        channelToRabbitMq.queueDeclare(queueNameFrontServer, false , false, false, null);
        System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            /* The server pushes messages asynchronously, hence we provide a DefaultConsumer callback
            that will buffer the messages until ready to use them. */
        //Set QoS: accept only one unacked message at a time; and force dispatch to next worker that is not busy.
        int prefetchCount = 1;
        channelToRabbitMq.basicQos(prefetchCount);

        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), "UTF-8");
            System.out.println(" [x] Received '" + message + "'");
            try {
                setReceivedMessage(message);
            } finally {
                System.out.println(" [x] Done processing task");
                //Worker must Manually ack each finalised task, hence, even if worker is killed
                //(CTRL+C) while processing a message, nothing will be lost.
                //Soon after the worker dies all unacknowledged messages will be redelivered.
                //Ack must be sent on the same channel message it was received,
                // otherwise raises exception (channel-level protocol exception).
                channelToRabbitMq.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        };
        //boolean autoAck = true; //When true disables "Manual message acknowledgments"
        //Set flag=false for worker to send proper ack (once it is done with a task).
        boolean autoAck = false;
        //Register handler deliverCallback()
        channelToRabbitMq.basicConsume(queueName, autoAck, deliverCallback, consumerTag -> { });
    }

    /**
     * Declare exchange of specified type.
     */
    private void bindExchangeToChannelRabbitMQ() throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Declaring Exchange '" + this.exchangeName + "' with type " + this.exchangeType);

        /* TODO: Declare exchange type  */
        channelToRabbitMq_Servers.exchangeDeclare(exchangeName+"server", BuiltinExchangeType.FANOUT);
    }

    /**
     * Creates a Consumer associated with an unnamed queue.
     */
    private void attachConsumerToChannelExchangeWithKey() {
        try {
            /* TODO: Create a non-durable, exclusive, autodelete queue with a generated name.
                The string queueName will contain a random queue name (e.g. amq.gen-JzTY20BRgKO-HjmUJj0wLg) */
            String queueName =  channelToRabbitMq_Servers.queueDeclare().getQueue();


            /* TODO: Create binding: tell exchange to send messages to a queue; fanout exchange ignores the last parameter (binding key) */
            String routingKey = "";
            channelToRabbitMq_Servers.queueBind(queueName,exchangeName+"server",routingKey);
            channelToRabbitMq_Servers.queuePurge(queueName);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Created SERVER_Channel bound to Exchange " + this.exchangeName + "...");

            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), messageFormat);

                //Store the received message
//                System.out.println(" [x] SERVER Tag [" + consumerTag + "] - Received '" + message + "'");
                syncServers(message);

                // TODO: Notify the GUI about the new message arrive
                gui.updateUser();
            };
            CancelCallback cancelCallback=consumerTag -> {
                System.out.println(" [x] SERVER Tag [" + consumerTag + "] - Cancel Callback invoked!");
            };

            // TODO: Consume with deliver and cancel callbacks
            channelToRabbitMq_Servers.basicConsume(queueName,true,deliverCallback,cancelCallback);

        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString());
        }
    }
    public void syncServers(String receivedMessage){
        System.out.println("Mensagens recebidas entre servers:" + receivedMessage);

        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        System.out.println(operation+operation);
        if(operation.equals("Enter lobby")){
            System.out.println("Player Entering.......");
        }else{
            System.out.println(".........");
        }
        System.out.println("JSON:"+json);
        switch (operation){
            case "Criar lobby":
                System.out.println("entrei criar lobby");
                String donoLobby = json.getString("dono lobby");
                int nrJogadores = json.getInt("nr jogadores");
                int nivelJogo = json.getInt("nivel jogo");
                ArrayList<String> playersNoLobby = new ArrayList<>();
                playersNoLobby.add(donoLobby);
                this.lobbys_and_players.put(donoLobby,playersNoLobby);
                this.lobbys_and_level.put(donoLobby,nivelJogo);
                this.lobbys_and_size.put(donoLobby,nrJogadores);
                System.out.println("Criei um novo lobby:" + this.lobbys_and_players);
                break;
            case "Enter lobby":
                System.out.println("entrei nos lobbys");
                String lobby = json.getString("lobby");
                String user = json.getString("user");
                if(!this.lobbys_and_players.get(lobby).contains(user)){
                    this.lobbys_and_players.get(lobby).add(user);
                }
                System.out.println("Jogador "+ user+" entrou no lobby "+lobby+":" + this.lobbys_and_players);
                break;
            case "ServerOn":
                if(!state){

                        String info = json.getString("info");
                        JSONArray info1 = json.getJSONArray("lobbys");
                        JSONArray info2 = json.getJSONArray("lvl");
                        JSONArray info3 = json.getJSONArray("size");
                        System.out.println("INFO:"+info);
                        System.out.println("INFO1:"+info1);
                        System.out.println("INFO2:"+info2);
                        System.out.println("INFO3:"+info3);
                        JSONArray players;
                        for (int i=0;i<info1.length();i++){
                            ArrayList<String> players_normal=new ArrayList<>();
                            players=json.getJSONArray("players"+i);
                            if (players != null) {
                                for (int y=0;y<players.length();y++){
                                    players_normal.add(players.getString(y));
                                }
                            }
                            lobbys_and_players.put(info1.getString(i),players_normal);
                            lobbys_and_level.put(info1.getString(i),info2.getInt(i));
                            lobbys_and_size.put(info1.getString(i),info3.getInt(i));

                        }
                    System.out.println("SYNC:");
                    System.out.println("LOBBYS_AND_PLAYERS:"+lobbys_and_players);
                    System.out.println("LOBBYS_AND_SIZE:"+lobbys_and_size);
                    System.out.println("LOBBYS_AND_LVL:"+lobbys_and_level);


                    this.state=true;
                }else {
                    System.out.println("Server already syncronized");
                }

                break;
        }

    }

    /**
     * Publish messages to existing exchange instead of the nameless one.
     * - Messages will be lost if no queue is bound to the exchange yet.
     * - User may be some 'username' or 'general' (for all)
     */
    public void sendMessage(String msgToSend,String exchangeName) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey="";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;

        // TODO: Publish message
        channelToRabbitMq.basicPublish(this.exchangeName+exchangeName,routingKey,prop,msgToSend.getBytes("UTF-8"));

    }
    public void sendMessageToServers(String msgToSend) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey="";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;

        // TODO: Publish message
        channelToRabbitMq.basicPublish(exchangeName+"server",routingKey,prop,msgToSend.getBytes("UTF-8"));

    }

    /**
     * @return the receivedMessage
     */
    public String getReceivedMessage() {
        return receivedMessage;
    }


    public int getlobbyPLayers(int lobby){
        int count =0;
//        for (Map.Entry<String, Integer> set : lobbys_and_players.entrySet()) {
//            if (set.getValue().equals(lobby))count++;
//        }


        return count;
    }


    /**
     * @param receivedMessage the receivedMessage to set
     */
    public void setReceivedMessage(String receivedMessage) throws IOException {
        System.out.println("server "+receivedMessage);
        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        switch (operation){
            case "GETLOBBYS":
                json.put("lobbys_and_players",printLobbys());
                sendMessage(json.toString(),"client");
                break;
            case "Criar lobby":
                sendMessageToServers(receivedMessage);
                break;
            case "Enter lobby":
                String lobby = json.getString("lobby");
                String user = json.getString("user");
                System.out.println(user + " entrou no lobby do " + lobby);

                this.lobbys_and_players.get(lobby).add(user);
                json.put("jogadoresNoLobby",this.lobbys_and_players.get(lobby));
                json.put("nivel",this.lobbys_and_level.get(lobby));
                json.put("comecar jogo", this.lobbys_and_players.get(lobby).size() == this.lobbys_and_size.get(lobby));
                sendMessage(json.toString(),lobby);
                sendMessageToServers(receivedMessage);
                break;
            case "LETS START THE GAME":
                sendMessage(json.toString(), json.getString("lobby"));
            case "MoveFrog":
                sendMessage(json.toString(), json.getString("lobby"));
                break;

        }
    }

    private String printLobbys() {
        int pos=0;
        StringBuilder lobbys = new StringBuilder();
        System.out.println(this.lobbys_and_players);

        if(this.lobbys_and_players.size()>0){
            lobbys.append("Insira o nome do lobby que deseja entrar:\n");
            for (Map.Entry<String, ArrayList<String>> lobby : this.lobbys_and_players.entrySet()){//percorre lobbys
                String dono = lobby.getKey();
                int sizePlayer = lobby.getValue().size();
                lobbys.append("\t[").append(++pos).append("]").append("Lobby do  ").append(dono).
                        append(" : ").append("\t nivel de jogo:").
                        append(this.lobbys_and_level.get(dono)).append("\t").append(sizePlayer).append("/").append(this.lobbys_and_size.get(dono)).append("\n");
            }
            lobbys.append("Ou insira '0' para criar um lobby\n");
        }else{
            lobbys.append("Nao existem Lobbys criados neste momento! Insira 0 para criar um lobby\n");
        }
        return lobbys.toString();
    }
}
