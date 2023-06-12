package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ThreadJogo extends UnicastRemoteObject implements Runnable {
    Game mainGame;
    String map;
    ArrayList<String> players;

    public ThreadJogo(Game main, ArrayList<String> players, String mapa) throws RemoteException {
        this.mainGame = main;
        this.players=players;
        this.map=mapa;
    }

    public void run() {
        try {
            this.mainGame = new Game(map, players);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
