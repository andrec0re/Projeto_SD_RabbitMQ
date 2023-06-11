package edu.ufp.inf.sd.rmi.Project.server;

import edu.ufp.inf.sd.rmi.Project.client.ObserverRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface GameSessionRI extends Remote {
    public void setUser(User user) throws RemoteException;
    public void setUsername(String username) throws RemoteException ;
    public String getUsername() throws RemoteException ;
    public AdvancedWarsGame new_game(String mapa, String username) throws RemoteException;
    public AdvancedWarsGame available_games(ObserverRI observerRI, int id) throws RemoteException;
    ArrayList<AdvancedWarsGame> listGames() throws RemoteException;
    public void addGame(AdvancedWarsGame game) throws RemoteException;
    public String getToken() throws RemoteException;
    public void setToken(String token) throws RemoteException;
}
