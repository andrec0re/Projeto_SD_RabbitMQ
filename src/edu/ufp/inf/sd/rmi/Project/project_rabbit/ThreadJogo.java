package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ThreadJogo extends UnicastRemoteObject implements Runnable {
    Game mainGame;
    String map;

    public ThreadJogo(Game main, String mapa) throws RemoteException {
        super();
        this.mainGame = main;
        this.map=mapa;
    }

    public void run() {
        try {
            mainGame.StartGame(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
