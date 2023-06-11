package edu.ufp.inf.sd.rmi.Project.server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;

public interface GameFactoryRI extends Remote {
    public void Register(User user) throws RemoteException;;
    public GameSessionRI Login(User user) throws RemoteException;

    public ArrayList<SubjectRI> getGames(GameSessionRI gameSessionRI)throws RemoteException;

    public SubjectRI getGame(int index, GameSessionRI gameSessionRI, int num_player) throws RemoteException;

}
