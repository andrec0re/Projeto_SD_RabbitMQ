package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import edu.ufp.inf.sd.rmi.Project.client.menus.MenuHandler;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
    private edu.ufp.inf.sd.rmi.Project.project_rabbit.ObserverGuiClient gui;

    //Preferences for exchange...
    public   Channel channelToRabbitMq;
    public   Channel channelToRabbitMqFrontServer;

    public   String exchangeName;
    private  BuiltinExchangeType exchangeType;
    private  String[] exchangeBindingKeys;
    private  String messageFormat;

    //Settings for specifying topics
    private  String user;
    private String queueName;
    private String queueNameFrontServer;

    public String map;
    public String mapa;
    public Game Game;
    public String donoLobby="";
    public int nr_jogadores=0;
    public boolean startGame = false;
    public Boolean duplicated=false;
    public Boolean duplicatedturn=false;

    //Store received message to be get by gui
    private String receivedMessage;
    boolean messagereceived =false;
    public ArrayList<String> jogadoresLobby = new ArrayList<>();
    private boolean turn=false;

    public boolean isTurn() throws RemoteException {
        return turn;
    }

    public void setTurn(boolean turn) throws RemoteException{
        this.turn = turn;
        if (turn){
            System.out.println("Your turn\n");
        }
        else {
            System.out.println("Waiting turn\n");
        }
    }
    public String currentPlayer;
    public String nextPlayer;

    public void setCurrentPlayer(String currentPlayer) {
        this.currentPlayer = currentPlayer;
    }

    public String getCurrentPlayer() {
        return currentPlayer;
    }

    public String getNextPlayer() {
        return nextPlayer;
    }

    public String setNextPlayer(String nextPlayer) {
        return this.nextPlayer;
    }

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
        while (!messagereceived){
            TimeUnit.MILLISECONDS.sleep(100);
        }
        messagereceived =false;

        String inp=myObj.nextLine();
        if(inp.equals("0")){
            System.out.println("Escolher Mapa:\n1-SmallVs\t2-FourCorners");
            int mapa = myObj.nextInt(); // get the map selection

            if (mapa == 1) { // SmallVs
               this.nr_jogadores = 2;
                map="C:\\Users\\olaso\\IdeaProjects\\Projeto_SD_RabbitMQ\\maps\\SmallVs.txt";
            } else if (mapa == 2) { // FourCorners
                this.nr_jogadores = 4;
                map="C:\\Users\\olaso\\IdeaProjects\\Projeto_SD_RabbitMQ\\maps\\FourCorners.txt";
            } else {
                System.out.println("Invalid selection");
                return; // or handle this scenario as you see fit
            }

            this.donoLobby=this.user;
            this.jogadoresLobby.add(this.user);

            json.put("operation","Criar lobby");
            json.put("dono lobby",this.user);
            json.put("mapa",map);       //path do mapa
            json.put("nr jogadores",nr_jogadores);
            //this.setTurn(true); //1st player starts to play
            this.channelToRabbitMq.exchangeDelete(queueName+ "client");
            this.channelToRabbitMq.queueDelete(queueName+"client");
            bindExchangeToChannelRabbitMQ("client");
            attachConsumerToChannelExchangeWithKey("client");
            this.sendMessage(json.toString());

        }else {
            json.put("operation","Enter lobby");
            json.put("lobby","teste1");
            json.put("lobbyID",inp);
            json.put("user",this.user);
            json.put("mapa", map); // Always include the map when a player enters the lobby
            this.jogadoresLobby.add(this.user); // Add this line to add user to lobby when they join
            this.setNextPlayer(this.user); // Update the currentPlayer with the lobby creator's username
            bindExchangeToChannelRabbitMQ("client");
            attachConsumerToChannelExchangeWithKey("client");
            this.sendMessage(json.toString());
        }
        json.clear();
        int opt=0;
        while (true) {
            if (this.user.equals(this.donoLobby)) { // Lobby owner
                //System.out.println("1- Comecar Jogo");
                System.out.println("2- Imprimir jogadores no lobby");
                System.out.println("3- Sair");
                opt = myObj.nextInt();
                switch (opt) {
                    /*case 1:
                        json.put("type", "comecar jogo");
                        json.put("lobby", this.user);
                        json.put("mapa", this.map); // Include the value of this.map in the message
                        this.sendMessage(json.toString());
                        System.out.println("Player 1\n Mapa-"+this.map);
                        startGame=true;
                        // Lobby owner starts the game
                        //this.Game = new Game(this.map, this.user,this);
                        ThreadJogo run = new ThreadJogo(this.user, this.map,this);
                        //ThreadJogo run = new ThreadJogo(this.Game);
                        new Thread(run).start();
                        break; */
                    case 2:
                        System.out.println(this.printMeuLobby());
                        break;
                    case 3:
                        return;
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }
            } else { // Lobby normal player

                System.out.println("1- Imprimir jogadores no lobby");
                System.out.println("2- Sair");
                //System.out.println("3- Start game");
                opt = myObj.nextInt();
                switch (opt) {
                    case 1:
                        System.out.println(this.printMeuLobby());
                        break;
                    case 2:
                        System.out.println("Leaving...\n");
                        return;
                   /* case 3:
                        System.out.println("Starting game...");
                        System.out.println("Mapa: " + this.mapa);
                        System.out.println("Lobby: " + this.jogadoresLobby);
                        ThreadJogo run = new ThreadJogo(this.Game, this.user, this.map,this);
                        new Thread(run).start();
                        break;*/
                    default:
                        System.out.println("Opcao invalida");
                        break;
                }
            }
        }


    }

    private String printMeuLobby() {
        StringBuilder string = new StringBuilder("----- Lobby Players-----\n");
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
        this.channelToRabbitMq.exchangeDeclare(this.exchangeName,this.exchangeType);
    }

    /**
     * Creates a Consumer associated with an unnamed queue.
     */
    public void attachConsumerToChannelExchangeWithKey(String exchangeName) {
        try {
            /* TODO: Create a non-durable, exclusive, autodelete queue with a generated name.
                The string queueName will contain a random queue name (e.g. amq.gen-JzTY20BRgKO-HjmUJj0wLg) */
            String queueName =  channelToRabbitMq.queueDeclare().getQueue();
            this.queueName=queueName;

            /* TODO: Create binding: tell exchange to send messages to a queue; fanout exchange ignores the last parameter (binding key) */
            String routingKey = "";
            channelToRabbitMq.queueBind(queueName,"client",routingKey);
            channelToRabbitMq.queuePurge(queueName);

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, " Created consumerChannel bound to Exchange " + this.exchangeName + "...");

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
    public void setReceivedMessage(String receivedMessage) throws IOException {
        System.out.println("Mensagem recebida do servidor : " + receivedMessage);
        this.receivedMessage = receivedMessage; // Set the value of receivedMessage
        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        if(duplicated) {
            duplicated = false;
            return;
        }
        if(duplicatedturn) {
            duplicatedturn = false;
            return;
        }
        switch (operation){
            case "GETLOBBYS":
                if(this.user.equals(json.getString("user"))){
                    System.out.println(json.getString("lobbys_and_players"));
                }
                this.messagereceived =true;
                break;
            case "Enter lobby":
                JSONArray array = json.getJSONArray("jogadoresNoLobby");

                for (int i = 0; i < array.length(); i++) {
                    String playerName = array.getString(i);
                    if (!this.jogadoresLobby.contains(playerName)) {
                        this.jogadoresLobby.add(playerName);
                    }
                }

                if (json.has("mapa")) { // Check if the "mapa" field is present
                    this.map = json.getString("mapa");
                    System.out.println(json.getString("user") + " entrou no lobby.");
                    System.out.println("Jogadores no lobby neste momento: " + this.jogadoresLobby);

                    if (json.getBoolean("comecar jogo") && !startGame) {
                        if (this.user.equals(this.donoLobby)) { // Lobby owner
                            // Set the turn for the lobby creator
                            this.setTurn(true);
                            this.setCurrentPlayer(this.user); // Update the currentPlayer with the lobby creator's username
                        } else {
                            this.setCurrentPlayer(this.user); //next players
                            this.setTurn(false);
                        }
                            System.out.println("Received boolean comecar jogo -> Max players reached\nStarting game...\n");
                        System.out.println("Mapa " + this.map);
                        //this.Game = new Game(this.map, this.user,this);
                        ThreadJogo run = new ThreadJogo(this.user, this.map,this);
                        //ThreadJogo run = new ThreadJogo(this.Game);
                        new Thread(run).start();
                        this.Game=run.getMainGame();
                        startGame=true;
                    }
                }
                break;

            case "LETS START THE GAME":
                //if(json.has("mapa")) { // Check if the "mapa" field is present
                    //this.map=json.getString("mapa");
                    //this.Game.StartGame(map);
                //}
                break;

            case "MovePlayer":
                if(!duplicated) {
                    System.out.println("ENTREI MOVEPLAYER USER\n");
                    //json.put("user", json.getString("user"));
                    json.put("move", json.getString("move"));
                    // this.Game.movePlayers(json.getString("user"),json.getString("move"));
                    movePlayers(json.getString("move"));
                    duplicated = true;
                    break;
                }
            case "UpdateTurn":
                if (!duplicatedturn) {
                    System.out.println("Entrei updateTurn observer");
                    String currentPlayer = json.getString("currentPlayer");
                    String nextPlayer = json.getString("nextPlayer");
                    System.out.println("MyplayerID " + this.getCurrentPlayer() + "\n");
                    String myPlayerId = this.getCurrentPlayer();

                    if(this.isTurn()) {
                        if (myPlayerId.equals(currentPlayer)) {
                            System.out.println("My turn to play  | player " + myPlayerId + "\n");
                            this.setTurn(true);
                            this.setCurrentPlayer(nextPlayer);
                            movePlayers("endturn");
                            duplicatedturn = true;
                            this.setTurn(false);
                        } else if (myPlayerId.equals(nextPlayer)) {
                            this.setTurn(true);
                            this.setCurrentPlayer(nextPlayer);
                            movePlayers("endturn");
                            duplicatedturn = true;
                            this.setTurn(false);
                        }
                    }
                    break;
                }

            /*case "TestMessage":
                System.out.println("Received test message: " + json.getString("message"));
                break;*/
        }
    }


    public void movePlayers(String move) {
        System.out.println("Entrei Move Players!");
        if (edu.ufp.inf.sd.rmi.Project.client.engine.Game.GameState == edu.ufp.inf.sd.rmi.Project.client.engine.Game.State.PLAYING) {
            edu.ufp.inf.sd.rmi.Project.client.players.Base ply = edu.ufp.inf.sd.rmi.Project.client.engine.Game.player.get(edu.ufp.inf.sd.rmi.Project.client.engine.Game.btl.currentplayer);

            //JSONObject json = new JSONObject(user);
            //String user = json.getString("user");
            switch (move) {
                case "up":
                    ply.selecty--;
                    if (ply.selecty < 0) {
                        ply.selecty++;
                    }
                    break;
                case "down":
                    ply.selecty++;
                    if (ply.selecty >= edu.ufp.inf.sd.rmi.Project.client.engine.Game.map.height) {
                        ply.selecty--;
                    }
                    break;
                case "left":
                    ply.selectx--;
                    if (ply.selectx < 0) {
                        ply.selectx++;
                    }
                    break;
                case "right":
                    ply.selectx++;
                    if (ply.selectx >= edu.ufp.inf.sd.rmi.Project.client.engine.Game.map.width) {
                        ply.selectx--;
                    }
                    break;
                case "select":
                    edu.ufp.inf.sd.rmi.Project.client.engine.Game.btl.Action();
                    break;
                case "cancel":
                    edu.ufp.inf.sd.rmi.Project.client.engine.Game.player.get(edu.ufp.inf.sd.rmi.Project.client.engine.Game.btl.currentplayer).Cancle();
                    break;
                case "start":
                    new edu.ufp.inf.sd.rmi.Project.client.menus.Pause(this);
                    break;
                case "endturn":
                    MenuHandler.CloseMenu();
                    edu.ufp.inf.sd.rmi.Project.client.engine.Game.btl.EndTurn();
                    break;
                default:
                    String[] params = move.split(":");
                    edu.ufp.inf.sd.rmi.Project.client.engine.Game.btl.Buyunit(Integer.parseInt(params[1]),
                            Integer.parseInt(params[2]),
                            Integer.parseInt(params[3]));
                    MenuHandler.CloseMenu();
            }
        }
    }



    public int getID() {
        for(int i=0;i<this.jogadoresLobby.size();i++){
            if(this.jogadoresLobby.get(i).equals(this.user))
                return i;
        }
        return 0;
    }


    public void setGame(Game mainGame) {
        this.Game=mainGame;
    }
}
