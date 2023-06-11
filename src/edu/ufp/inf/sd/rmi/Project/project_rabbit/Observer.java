package edu.ufp.inf.sd.rabbitmqservices.Project.project_rabbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rabbitmqservices.projeto.frogger.Game;
import edu.ufp.inf.sd.rabbitmqservices.projeto.util.RabbitUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
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
public class Observer {

    //Reference for gui
    private  ObserverGuiClient gui;

    //Preferences for exchange...
    private  Channel channelToRabbitMq;
    private  Channel channelToRabbitMqFrontServer;

    private  String exchangeName;
    private  BuiltinExchangeType exchangeType;
    private  String[] exchangeBindingKeys;
    private  String messageFormat;

    //Settings for specifying topics
    private  String user;
    private String queueName;
    private String queueNameFrontServer;

    public Game Game;
    public String donoLobby="";
    public int nivelJogo=0;
    public int position=0;

    //Store received message to be get by gui
    private String receivedMessage;
    boolean messagerecived=false;
    public ArrayList<String> jogadoresLobby = new ArrayList<>();


    public Observer(ObserverGuiClient gui, String host, int port, String user, String pass, String exchangeName,String queueName,String queueFrontServer, BuiltinExchangeType exchangeType, String messageFormat, String name) throws IOException, TimeoutException, InterruptedException {
        Scanner myObj = new Scanner(System.in);  // Create a Scanner object
        this.gui=gui;
        this.queueName=queueName;
        this.exchangeName=exchangeName;
        this.queueNameFrontServer=queueFrontServer;
        this.exchangeType=exchangeType;
        this.messageFormat=messageFormat;
        Connection connection=RabbitUtils.newConnection2Server(host, port, user, pass);
        this.channelToRabbitMq=RabbitUtils.createChannel2Server(connection);
        this.channelToRabbitMqFrontServer=RabbitUtils.createChannel2Server(connection);
        this.channelToRabbitMqFrontServer.queueDeclare(queueNameFrontServer,false,false,false,null);
        bindExchangeToChannelRabbitMQ("client");
        attachConsumerToChannelExchangeWithKey("client");
        this.user = name;
        JSONObject json = new JSONObject();
        json.put("operation","GETLOBBYS");
        json.put("user",this.user);
        this.sendMessage(json.toString());
        json.clear();
        while (!messagerecived){
            TimeUnit.MILLISECONDS.sleep(100);
        }
        messagerecived=false;

        String inp=myObj.nextLine();
        if(inp.equals("0")){
            System.out.println("Quantos jogadores quer no seu lobby escolha entre 2 a 4");
            int nr_jogadores= myObj.nextInt();
            System.out.println("Nivel de jogo entre 1 a 10");
            int nivel_jogo= myObj.nextInt();

            this.donoLobby=this.user;
            this.nivelJogo=nivel_jogo;
            this.jogadoresLobby.add(this.user);

            json.put("operation","Criar lobby");
            json.put("dono lobby",this.user);
            json.put("nr jogadores",nr_jogadores);
            json.put("nivel jogo",nivel_jogo);
            this.channelToRabbitMq.exchangeDelete(queueName+ "client");
            this.channelToRabbitMq.queueDelete(queueName+"client");
            bindExchangeToChannelRabbitMQ(this.user);
            attachConsumerToChannelExchangeWithKey(this.user);
            this.sendMessage(json.toString());

        }else {
            donoLobby=inp;
            json.put("operation","Enter lobby");
            json.put("lobby",donoLobby);
            json.put("user",this.user);
            bindExchangeToChannelRabbitMQ(donoLobby);
            attachConsumerToChannelExchangeWithKey(donoLobby);
            this.sendMessage(json.toString());

        }
        json.clear();
        int opt=0;
        while(true) {
            if (this.user.equals(this.donoLobby)) {
                System.out.println("1- Comecar Jogo");
                System.out.println("2- Imprimir jogadores no lobby");
                System.out.println("3- Sair");
                opt = myObj.nextInt();
                switch (opt) {
                    case 1:
                        json.put("type", "Comecar Jogo");
                        json.put("lobby", this.user);
                        sendMessage(json.toString());
                        break;

                    case 2:
                        System.out.println(this.printMeuLobby());

                        break;
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }

            } else {
                System.out.println("1- Imprimir jogadores no lobby");
                System.out.println("2- Sair");

                opt = myObj.nextInt();
                switch (opt) {
                    case 1:
                        System.out.println(this.printMeuLobby());
                        break;

                    case 2:
                        return;
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }

            }
        }

//            this.Game=new Game(this.position,nr_jogadores,this);
//            while (true){
//                System.out.println("SIZE DO ARRAY:"+NR_de_players_no_meu_lobby);
//                System.out.println("SIZE PARA COMEÇAR:"+this.nr_jogadores);
//                if (this.NR_de_players_no_meu_lobby==this.nr_jogadores)break;
//            }
//            this.Game.run();
    }

    private String printMeuLobby() {
        StringBuilder string = new StringBuilder("----- Players no meu fucking  Lobby -----\n");
        int i=0;
        for(String playerName : this.jogadoresLobby){
            string.append("\t[").append(++i).append("]").append(playerName).append("\n");
        }
        return string.toString();
    }





    /**
     * Declare exchange of specified type.
     */
    private void bindExchangeToChannelRabbitMQ(String exchangeName) throws IOException {
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Declaring Exchange '" + this.exchangeName + "' with type " + this.exchangeType);

        /* TODO: Declare exchange type  */
        this.channelToRabbitMq.exchangeDeclare(this.exchangeName+exchangeName,this.exchangeType);
    }

    /**
     * Creates a Consumer associated with an unnamed queue.
     */
    public void attachConsumerToChannelExchangeWithKey(String exchangeName) {
        try {
            /* TODO: Create a non-durable, exclusive, autodelete queue with a generated name.
                The string queueName will contain a random queue name (e.g. amq.gen-JzTY20BRgKO-HjmUJj0wLg) */
            String queueName =  channelToRabbitMq.queueDeclare().getQueue();


            /* TODO: Create binding: tell exchange to send messages to a queue; fanout exchange ignores the last parameter (binding key) */
            String routingKey = "";
            channelToRabbitMq.queueBind(queueName,this.exchangeName+exchangeName,routingKey);
            channelToRabbitMq.queuePurge(queueName);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Created consumerChannel bound to Exchange " + this.exchangeName + exchangeName + "...");

            /* Use a DeliverCallback lambda function instead of DefaultConsumer to receive messages from queue;
               DeliverCallback is an interface which provides a single method:
                void handle(String tag, Delivery delivery) throws IOException; */
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), messageFormat);

                //Store the received message
//                System.out.println(" [x] Consumer Tag [" + consumerTag + "] - Received pls send bro'" + message + "'");
                setReceivedMessage(message);

                // TODO: Notify the GUI about the new message arrive
                gui.updateUser();
            };
            CancelCallback cancelCallback=consumerTag -> {
                System.out.println(" [x] Consumer Tag [" + consumerTag + "] - Cancel Callback invoked!");
            };

            // TODO: Consume with deliver and cancel callbacks
            channelToRabbitMq.basicConsume(queueName,true,deliverCallback,cancelCallback);
        } catch (Exception e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString());
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
        channelToRabbitMq.basicPublish("", queueNameFrontServer, prop, msgToSend.getBytes("UTF-8"));
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
    public void setReceivedMessage(String receivedMessage) throws RemoteException {
        System.out.println("Mensagem recebida dos servidores : " + receivedMessage);
        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        switch (operation){
            case "GETLOBBYS":
                if(this.user.equals(json.getString("user"))){
                    System.out.println(json.getString("lobbys_and_players"));
                }
                this.messagerecived=true;
                break;
            case "Enter lobby":
                if(this.donoLobby.equals(json.getString("lobby"))){
                    JSONArray array = json.getJSONArray("jogadoresNoLobby");
                    for(int i=0; i<array.length(); i++){
                        if(!this.jogadoresLobby.contains(array.getString(i))){
                            this.jogadoresLobby.add(array.getString(i));
                        }
                    }
                    this.nivelJogo=json.getInt("nivel");
                    System.out.println(json.getString("user") + " entrou no lobby.");
                    System.out.println("Jogadores no lobby neste momento: " + this.jogadoresLobby);
                    if(json.getBoolean("comecar jogo")){
                        this.Game=new Game(this.getID(),this.jogadoresLobby.size(),this,nivelJogo);
                        ThreadJogo run = new ThreadJogo(this.Game);
                        new Thread(run).start();
                    }
                }
                break;
            case "LETS START THE GAME":
                if(json.getString("lobby").equals(this.donoLobby)){
                    this.Game.startGame();
                }
                break;
            case "MoveFrog":
                if(this.donoLobby.equals(json.getString("lobby")))
                    this.Game.update_players(receivedMessage);
                break;

        }
    }

    private int getID() {
        for(int i=0;i<this.jogadoresLobby.size();i++){
            if(this.jogadoresLobby.get(i).equals(this.user))
                return i;
        }
        return 0;
    }


}
