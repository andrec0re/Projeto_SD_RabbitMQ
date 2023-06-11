package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ThreadJogo extends UnicastRemoteObject implements Runnable {
    Game mainGame;

    public ThreadJogo(Game main) throws RemoteException {
        this.mainGame = main;
    }

    public void run() {
        try {
            mainGame.rpcStartGame();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
