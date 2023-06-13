package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FrontServer {
    private  static HashMap<String,ArrayList<String>> lobbys_and_players =new HashMap<>();// Nr de Lobby e o nome do jogador no lobby
    private  static HashMap<String, String> lobbys_and_map =new HashMap<String, String>();// Nr de Lobby e qts jogadores lá estão
    private  static HashMap<String,Integer> lobbys_and_size =new HashMap<>();// Nr de Lobby e qts jogadores lá estão


    public static void   main(String args[]) throws IOException, TimeoutException {
        try {
            RabbitUtils.printArgs(args);

            //Read args passed via shell command
            String host=args[0];
            int port=Integer.parseInt(args[1]);
            String exchangeName=args[2];

            String queueServers=args[3];
            String queueFrontServer=args[4];

            // Open a connection and a channel to rabbitmq broker/server
            Connection connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
            Channel channel=RabbitUtils.createChannel2Server(connection);
            Channel channelToServers=RabbitUtils.createChannel2Server(connection);

            //Declare queue from which to consume (declare it also here, because consumer may start before publisher)
            channel.queueDeclare(queueFrontServer, false, false, false, null);
            //channel.queueDeclare(Send.QUEUE_NAME, true, false, false, null);
            Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName()+": Will create Deliver Callback...");
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");


            channelToServers.exchangeDeclare(exchangeName, BuiltinExchangeType.FANOUT);
            String queueName =  channelToServers.queueDeclare().getQueue();
            channelToServers.queueBind(queueName,exchangeName,"");
            channelToServers.queuePurge(queueName);

            //DeliverCallback is an handler callback (lambda method) to consume messages pushed by the sender.
            //Create an handler callback to receive messages from queue
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), "UTF-8");
                Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName()+": Message received " +message);
                System.out.println(" [x] Received '" + message + "'");



                JSONObject json = new JSONObject(message);
                json.put("FrontServer","Sending message from FrontServer");

                if(json.getString("operation").equals("ServerOn")){
                    json.put("info","Server is ON");
                    getInfo1(json);
                    AMQP.BasicProperties prop = MessageProperties.PERSISTENT_TEXT_PLAIN;
                    channelToServers.basicPublish(exchangeName,"",prop,json.toString().getBytes("UTF-8"));
                    System.out.println("[x] Sent to Server to sync data: '"+json.toString() + "'");
                }else{
                    System.out.println("[x] Sent: '"+json.toString() + "'");
                    setReceivedMessage(message);
                    channel.basicPublish("", queueServers, null, json.toString().getBytes("UTF-8"));
                }


            };
            Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName()+": Register Deliver Callback...");
            //Associate callback with channel queue
            channel.basicConsume(queueFrontServer, true, deliverCallback, consumerTag -> {
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void setReceivedMessage(String receivedMessage) throws RemoteException {
        JSONObject json = new JSONObject(receivedMessage);
        String operation = json.getString("operation");
        switch (operation){
            case "Criar lobby":
                String donoLobby = json.getString("dono lobby");
                String mapa = json.getString("mapa");
                int nrJogadores = json.getInt("nr jogadores");
                System.out.println("FrontServer | " + donoLobby+ " criou novo lobby");

                ArrayList<String> playersNoLobby = new ArrayList<>();
                playersNoLobby.add(donoLobby);
                lobbys_and_players.put(donoLobby,playersNoLobby);
                lobbys_and_map.put(donoLobby,mapa);
                lobbys_and_size.put(donoLobby,nrJogadores);
                System.out.println("Criei um novo lobby:" + lobbys_and_players);
                break;

            case "Enter lobby":
                String lobbyID = json.getString("lobbyID"); // Use lobbyID from the JSON
                String user = json.getString("user");
                System.out.println("FrontServer | " + user + " entrou no lobby " + lobbyID);

                // Convert lobby ID to lobby name
                ArrayList<String> lobbyNames = new ArrayList<>(lobbys_and_players.keySet());
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
                ArrayList<String> players = lobbys_and_players.get(lobbyName);
                if (players == null) {
                    players = new ArrayList<>();
                    lobbys_and_players.put(lobbyName, players);
                }
                if (!players.contains(user)) {
                    players.add(user);
                }
                System.out.println("Jogador " + user + " entrou no lobby: " + lobbys_and_players);
                break;


        }
    }

    public static void getInfo1(JSONObject json){
        ArrayList<String> lobbynames = new ArrayList<>();
        ArrayList<String> lobbymap = new ArrayList<>();
        ArrayList<Integer> lobbysizes = new ArrayList<>();
        ArrayList<String> players;
        int j=0;
        for(Map.Entry<String,ArrayList<String>> entry : lobbys_and_players.entrySet()){
            lobbynames.add(entry.getKey());
            players=lobbys_and_players.get(entry.getKey());
            json.put("players"+j,players);
            j=j+1;
        }
        json.put("lobbys",lobbynames);

        for (int i=0;i<lobbynames.size();i++){
           lobbymap.add(lobbys_and_map.get(lobbynames.get(i)));
           lobbysizes.add(lobbys_and_size.get(lobbynames.get(i)));
        }
        json.put("mapa",lobbymap);
        json.put("size",lobbysizes);

    }
}
