package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;
import org.json.*;

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
    private  HashMap<String, String> lobbys_and_map =new HashMap<String, String>();// Nr de Lobby e qts jogadores lá estão
    private  HashMap<String,Integer> lobbys_and_size =new HashMap<>();// Nr de Lobby e qts jogadores lá estão
    private HashMap<String, String> lobbyCurrentPlayers = new HashMap<>();
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
            channelToRabbitMq_Servers.exchangeDeclare(this.exchangeName, BuiltinExchangeType.FANOUT);
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
                channelToRabbitMq_Servers.queueBind(queueName,this.exchangeName,routingKey);
                channelToRabbitMq_Servers.queuePurge(queueName);

                Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Created SERVER_Channel bound to Exchange " + this.exchangeName + "...");

                DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                    String message=new String(delivery.getBody(), messageFormat);

                    //Store the received message
    //                System.out.println(" [x] SERVER Tag [" + consumerTag + "] - Received '" + message + "'");
                    //syncServers(message);

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

    public void syncServers(String receivedMessage) throws IOException {
        System.out.println("Mensagens recebidas entre servers:" + receivedMessage);

        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        System.out.println(operation);
        if(operation.equals("Enter lobby")){
            System.out.println("Player Entering.......");
        }else{
            System.out.println(".........");
        }
        //System.out.println("JSON:"+json);
        switch (operation){
            case "Criar lobby":
                //System.out.println("entrei criar lobby");
                String donoLobby = json.getString("dono lobby");
                int nrJogadores = json.getInt("nr jogadores");
                String mapa = json.getString("mapa");
                ArrayList<String> playersNoLobby = new ArrayList<>();
                playersNoLobby.add(donoLobby);
                this.lobbys_and_players.put(donoLobby,playersNoLobby);
                this.lobbys_and_map.put(donoLobby,mapa);
                this.lobbys_and_size.put(donoLobby,nrJogadores);
                this.lobbyCurrentPlayers.put(donoLobby, donoLobby);
                System.out.println("Criei um novo lobby:" + this.lobbys_and_players);
                break;
            case "Enter lobby":
                String lobbyID = json.getString("lobbyID"); // Use lobbyID from the JSON
                String user = json.getString("user");

                // Convert lobby ID to lobby name
                ArrayList<String> lobbyNames = new ArrayList<>(this.lobbys_and_players.keySet());
                int lobbyIndex;
                try {
                    lobbyIndex = Integer.parseInt(lobbyID) - 1;
                } catch (NumberFormatException e) {
                    System.out.println("Invalid lobby ID: " + lobbyID);
                    throw new RuntimeException("Invalid lobby ID: " + lobbyID);
                }
                if (lobbyIndex < 0 || lobbyIndex >= lobbyNames.size()) {
                    System.out.println("Lobby ID out of range: " + lobbyID);
                    throw new RuntimeException("Lobby ID out of range: " + lobbyID);
                }
                String lobbyName = lobbyNames.get(lobbyIndex);

                // Use lobbyName instead of lobby
                if (!this.lobbys_and_players.get(lobbyName).contains(user)) {
                    this.lobbys_and_players.get(lobbyName).add(user);
                }
                System.out.println("Jogador " + user + " entrou no lobby: " + this.lobbys_and_players);
                break;
            case "ServerOn":
                if(!state){
                        String info = json.getString("info");
                        JSONArray info1 = json.getJSONArray("lobbys");
                        JSONArray info2 = json.getJSONArray("mapa");
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
                            lobbys_and_map.put(info1.getString(i),info2.getString(i));
                            lobbys_and_size.put(info1.getString(i),info3.getInt(i));

                        }
                    System.out.println("SYNC:");
                    System.out.println("LOBBYS_AND_PLAYERS:"+lobbys_and_players);
                    System.out.println("LOBBYS_AND_SIZE:"+lobbys_and_size);
                    System.out.println("LOBBYS_AND_MAP:"+lobbys_and_map);


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
    public void sendMessage(String msgToSend) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey="";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;

        // TODO: Publish message
        channelToRabbitMq.basicPublish("client",routingKey,prop,msgToSend.getBytes("UTF-8"));
    }

    public void sendMessageToServers(String msgToSend) throws IOException {
        //RoutingKey will be ignored by FANOUT exchange
        String routingKey="";
        BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;

        // TODO: Publish message
        channelToRabbitMq.basicPublish(this.exchangeName,routingKey,prop,msgToSend.getBytes("UTF-8"));

    }

    /**
     * @return the receivedMessage
     */
    public String getReceivedMessage() {
        return receivedMessage;
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
                    sendMessage(json.toString());
                    break;
                case "Criar lobby":
                    sendMessageToServers(receivedMessage);
                    break;
                case "Enter lobby":
                    String lobbyID = json.getString("lobbyID"); // Use lobbyID from the JSON
                    String user = json.getString("user");
                    ArrayList<String> lobbyNames = new ArrayList<>(this.lobbys_and_players.keySet());

                    // Validate lobbyID is a valid integer and within the range of available lobbies
                    int lobbyIndex;
                    try {
                        lobbyIndex = Integer.parseInt(lobbyID) - 1; // Subtract 1 because list indices start at 0
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid lobby ID: " + lobbyID);
                        throw new RuntimeException("Invalid lobby ID: " + lobbyID);
                    }
                    if (lobbyIndex < 0 || lobbyIndex >= lobbyNames.size()) {
                        System.out.println("Lobby ID out of range: " + lobbyID);
                        throw new RuntimeException("Lobby ID out of range: " + lobbyID);
                    }

                    // Get the lobby name corresponding to the given ID
                    String lobbyName = lobbyNames.get(lobbyIndex);

                    // The rest of your code can remain the same, but use lobbyName instead of lobby
                    ArrayList<String> players = this.lobbys_and_players.get(lobbyName);
                    if (players == null) {
                        System.out.println("Lobby " + lobbyName + " does not exist.");
                        throw new RuntimeException("Lobby " + lobbyName + " does not exist.");
                    } else {
                        if (!players.contains(user)) {
                            players.add(user);
                            System.out.println(user + " entrou no lobby do " + lobbyID);
                        } else {
                            System.out.println("User " + user + " is already in the lobby " + lobbyName);
                        }
                        json.put("lobby", "teste1");
                        json.put("jogadoresNoLobby", players);
                        json.put("mapa", this.lobbys_and_map.get(lobbyName));
                        json.put("comecar jogo", players.size() == this.lobbys_and_size.get(lobbyName));

                        // Update the current player for the lobby
                        String currentPlayer = lobbyCurrentPlayers.get(lobbyName);
                        json.put("currentPlayer", currentPlayer);
                        sendMessage(json.toString());
                        //sendMessageToServers(receivedMessage);
                        //sendTestMessage();
                    }
                    break;

                case "LETS START THE GAME":
                    sendMessage(json.toString());
                    break;
                case "MovePlayer":
                System.out.println("ENTREI MOVEPLAYER SERVER||||||||||||");
                    json.put("operation","MovePlayer");
                    json.put("user", json.getString("user"));
                    json.put("move", json.getString("move"));
                    json.put("lobby",json.getString("lobby"));
                    sendMessage(json.toString());
                    System.out.println("Message MOVEPLAYER-SERVER sent\n");
                    break;
                case "EndTurn":
                    // Update turn
                    String lobbyName1 = "teste1"; // Get the lobby name from the JSON
                    String currentPlayer = lobbyCurrentPlayers.get(lobbyName1);
                    String nextPlayer = getNextPlayerUsername(lobbyName1, currentPlayer); // Implement this method to get the username of the next player

                    lobbyCurrentPlayers.put(lobbyName1, nextPlayer);

                    // Send message to all players with the updated turn information
                    JSONObject turnJson = new JSONObject();
                    turnJson.put("operation", "UpdateTurn");
                    turnJson.put("currentPlayer", currentPlayer);
                    turnJson.put("nextPlayer", nextPlayer);
                    sendMessage(turnJson.toString());
                    System.out.println("Message ENDTURN DO SERVER sent\n");
                    break;
            }
        }

    private String getNextPlayerUsername(String lobbyName, String currentPlayer) {
        ArrayList<String> players = this.lobbys_and_players.get(lobbyName);
        int currentPlayerIndex = players.indexOf(currentPlayer);
        int nextPlayerIndex = (currentPlayerIndex + 1) % players.size();
        return players.get(nextPlayerIndex);
    }


    private String printLobbys() {
        int pos=0;
        StringBuilder lobbys = new StringBuilder();
        System.out.println(this.lobbys_and_players);

        if(this.lobbys_and_players.size()>0){
            lobbys.append("Insira o id do lobby que deseja entrar:\n");
            for (Map.Entry<String, ArrayList<String>> lobby : this.lobbys_and_players.entrySet()){//percorre lobbys
                String dono = lobby.getKey();
                int sizePlayer = lobby.getValue().size();
                lobbys.append("\t[").append(++pos).append("]").append("Lobby criado por ").append(dono).
                        append(" | ").append("\t mapa:").
                        append(this.lobbys_and_map.get(dono)).append("\t").append(sizePlayer).append("/").append(this.lobbys_and_size.get(dono)).append("\n");
            }
            lobbys.append("Ou insira '0' para criar um lobby\n");
        }else{
            lobbys.append("Não existem lobbies criados neste momento! Insira 0 para criar um lobby\n");
        }
        return lobbys.toString();
    }

    public void sendTestMessage() {
        JSONObject json = new JSONObject();
        json.put("operation", "TestMessage");
        json.put("message", "This is a test message from server.");
        try {
            sendMessage(json.toString());
        } catch(IOException e) {
            e.printStackTrace();
        }
    }


}
