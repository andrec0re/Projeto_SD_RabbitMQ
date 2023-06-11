package edu.ufp.inf.sd.rmi.Project.server;

import edu.ufp.inf.sd.rmi.Project.client.ObserverRI;
import edu.ufp.inf.sd.rmi.Project.server.State;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Random;


public interface SubjectRI extends Remote {

    public int getObserversSize()throws RemoteException;

    public void attach(ObserverRI obsRI) throws RemoteException;
    public void detach(ObserverRI obsRI) throws RemoteException;

    public State getSubjectState() throws RemoteException;
    public void setSubjectState(String state) throws RemoteException;

    public int getNr_players() throws RemoteException;
    public void setNr_players(int nr_players) throws RemoteException;

    public ArrayList<ObserverRI> getObservers() throws RemoteException;

    public String getMapname() throws RemoteException;
    public String setMapname(String mapname) throws RemoteException;

}
