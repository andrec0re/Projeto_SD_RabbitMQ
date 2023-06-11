package edu.ufp.inf.sd.rmi.Project.server;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

public class GameFactoryImpl extends UnicastRemoteObject implements GameFactoryRI{

    private final DBMockup db = new DBMockup();
    private final HashMap<String, GameSessionRI> hashMap = new HashMap<>();

    private ArrayList<SubjectRI> games_list = new ArrayList<>();
    //private ArrayList<SubjectRI> games = new ArrayList<>();

    public SubjectRI getGames_list(int index, GameSessionRI gameSessionRI, int num_player, String map)throws RemoteException {
       if (index == 0){
           SubjectRI game = new SubjectImpl(map);
           if (num_player!=0){
               game.setNr_players(num_player);
               game.setMapname(map);
           }
           games_list.add(game);

           return game;
       }
       return games_list.get(--index);
    }

    public GameFactoryImpl() throws RemoteException {
        super();
    }

    public DBMockup getDb() {
        return db;
    }

    @Override
    public void Register(User user) {
        db.Register(user.getUname(), user.getPword());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "User Registado");
    }

    public void RemoveSession(String user) {
        hashMap.remove(user);
    }

    @Override
    public GameSessionRI Login(User user) throws RemoteException {
        if (db.exists(user.getUname(), user.getPword())) {
            if (hashMap.containsKey(user.getUname())) {
                // User is already logged in, return the existing session
                return hashMap.get(user.getUname());
            } else {
                // User is logging in for the first time, create a new session
                GameSessionRI g = new GameSessionImpl(this, user);
                hashMap.put(user.getUname(), g);
                return g;
            }
        }
        return null; // Invalid login credentials


        //token no lado server
        /*
            long expirationTimeMillis = System.currentTimeMillis() + (60 * 60 * 1000);  //1h token expiration
            Date expirationDate = new Date(expirationTimeMillis);
            // Create the JWT token
            String token = Jwts.builder()
                    .setSubject(user.getUname())  // Set the subject (e.g., username)
                    .setExpiration(expirationDate)  // Set the expiration date
                    .signWith(SignatureAlgorithm.HS256, "secret-key")  // Set the signing algorithm and secret key
                    .compact();
            System.out.println("Token " + token);
            //gameSessionRI.setToken(token);      //set token to user
        */
    }



    @Override
    public ArrayList<SubjectRI> getGames(GameSessionRI gameSessionRI) throws RemoteException {
        return games_list;
    }


    @Override
    public SubjectRI getGame(int index, GameSessionRI gameSessionRI, int num_player) throws RemoteException {
        if (index >= 0 && index < games_list.size()) {
            SubjectRI game = games_list.get(index);
            if (num_player != 0) {
                game.setNr_players(num_player);
                //game.setNivel(nivel);
            }
            return game;
        }
        return null;
    }


}
