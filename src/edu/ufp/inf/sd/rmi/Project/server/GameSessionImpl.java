package edu.ufp.inf.sd.rmi.Project.server;

import edu.ufp.inf.sd.rmi.Project.client.ObserverRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class GameSessionImpl extends UnicastRemoteObject implements GameSessionRI {
    private GameFactoryImpl gameFactory;
    private String username;
    private User user;
    private ArrayList<AdvancedWarsGame> games;
    private GameSessionRI gameSessionRI;
    private String token;

    public GameSessionImpl() throws RemoteException {
        super();
        games = new ArrayList<>();
        gameSessionRI = this;
    }

    public String getToken()  throws RemoteException {
        return token;
    }

    public void setToken(String token)  throws RemoteException{
        this.token = token;
    }

    public GameSessionImpl(GameFactoryImpl gameFactory, User user) throws RemoteException {
        this.gameFactory = gameFactory;
        this.user = user;
    }

    public void setUser(User user) throws RemoteException {
        this.user = user;
    }

    public void setUsername(String username) throws RemoteException {
        this.username = username;
    }

    public String getUsername() throws RemoteException {
        return username;
    }

    @Override
    public void addGame(AdvancedWarsGame game) throws RemoteException {
        games.add(game);
    }


    public AdvancedWarsGame new_game(String mapa, String username) throws RemoteException {
        SubjectRI subjectRI = new SubjectImpl(mapa); // Create a new instance of SubjectRI
        subjectRI.setMapname(mapa); // Set the map name for the game
        /*int maxPlayers = 0;
        if (mapa.equals("C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\SmallVs.txt")) {
            maxPlayers = 2;
        } else if (mapa.equals("C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\FourCorners.txt")) {
            maxPlayers = 4;
        }
        //definição max_players é feita agora no insertGame(), vemos pelo mapa
        */
        //AdvancedWarsGame game = new AdvancedWarsGame(1, mapa, username, subjectRI, maxPlayers); // Create a new instance of AdvancedWarsGame
        AdvancedWarsGame game = gameFactory.getDb().insertGame(mapa,username,subjectRI);
        return game;
    }


    public AdvancedWarsGame available_games(ObserverRI observerRI, int id) throws RemoteException{
        return gameFactory.getDb().searchGame(id);
    }

    @Override
    public ArrayList<AdvancedWarsGame> listGames() throws RemoteException {
        return this.gameFactory.getDb().listGames();
    }

}
