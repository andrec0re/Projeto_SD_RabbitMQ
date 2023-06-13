package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ThreadJogo extends UnicastRemoteObject implements Runnable {
    Game mainGame;
    String map;
    String username;
    ArrayList<String> players;
    Observer observer;

    public ThreadJogo(Game main, String username, String mapa, Observer observer) throws RemoteException {
        this.mainGame = main;
        this.username=username;
        this.map=mapa;
        this.observer=observer;
    }

    public ThreadJogo(Game game) throws RemoteException {
        this.mainGame=game;
    }

    public void run() {
        try {
            //this.mainGame = new Game(map, username,observer);
            //new Game();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
