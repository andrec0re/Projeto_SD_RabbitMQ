package edu.ufp.inf.sd.rmi.Project.client;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import edu.ufp.inf.sd.rmi.Project.server.State;
import edu.ufp.inf.sd.rmi.Project.server.SubjectRI;

import java.rmi.Remote;
import java.rmi.RemoteException;


public interface ObserverRI extends Remote {
    public String getUsername() throws RemoteException;
    public int getCommander() throws RemoteException;
    public State getLastObserverState() throws RemoteException;
    public void updateGame() throws RemoteException;

    public void setUsername(String username) throws RemoteException;

    public Game getGame() throws RemoteException;
    public void setGame(Game game) throws RemoteException;

    public void setLastObserverState(State lastObserverState) throws RemoteException;

    public SubjectRI getSubjectRI() throws RemoteException;

    public void setSubjectRI(SubjectRI subjectRI) throws RemoteException;

    public void setCommander(int commander) throws RemoteException;

    public boolean isTurn() throws RemoteException;
    public void setTurn(boolean turn) throws RemoteException;
}

