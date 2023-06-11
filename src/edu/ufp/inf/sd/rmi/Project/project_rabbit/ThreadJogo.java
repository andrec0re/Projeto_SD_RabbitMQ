package edu.ufp.inf.sd.rabbitmqservices.Project.project_rabbit;

import edu.ufp.inf.sd.rabbitmqservices.projeto.frogger.Game;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ThreadJogo extends UnicastRemoteObject implements Runnable {
    Game main;

    public ThreadJogo(Game main) throws RemoteException {
        this.main = main;
    }

    public void run() {
        try {
            main.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
