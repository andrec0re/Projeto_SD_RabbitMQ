package edu.ufp.inf.sd.rmi.Project.server;

import edu.ufp.inf.sd.rmi.Project.client.ObserverRI;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class SubjectImpl extends UnicastRemoteObject implements SubjectRI {

    //criar uma array list para dar attach de observers
    private ArrayList<ObserverRI> observers = new ArrayList<ObserverRI>();

    public State subjectState;
    private int nr_players;
    private int current_player;
    private String mapname;

    public SubjectImpl(String mapa) throws RemoteException {
        super();
        this.mapname=mapa;
        String mapa2 = "C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\FourCorners.txt";
        if (this.mapname.equals(mapa2)) {
            this.nr_players = 4;
        } else {
            this.nr_players = 2;
        }

        this.subjectState = new State(null, null); // Initialize subjectState
    }

    public SubjectImpl() throws RemoteException {
        this.subjectState = new State(null, null); // Initialize subjectState

    }

    @Override
    public int getObserversSize() throws RemoteException {
        return observers.size();
    }

    @Override
    public State getSubjectState() throws RemoteException{
        return subjectState;
    }

    @Override
    public ArrayList<ObserverRI> getObservers() throws RemoteException{
        return observers;
    }

    @Override
    public void attach(ObserverRI obsRI) throws RemoteException {
        if (!observers.contains(obsRI))observers.add(obsRI);
        System.out.println("TAMANHO DO OBS:"+observers.size());
        //System.out.println("INFO: Player Count: " + this.nr_players);

        if(this.observers.size() == 1){ //first player gets turn to play
            this.observers.get(0).setTurn(true);
        }
    }

    @Override
    public void detach(ObserverRI obsRI) throws RemoteException {
        this.observers.remove(obsRI);
        System.out.println("Removed obs\nTAMANHO DO OBS:"+observers.size());
        //System.out.println("INFO: Player Count: " + this.nr_players);
    }

    public void setSubjectState(String state) throws RemoteException {
        //check if it is my turn to play
        if (this.observers.get(current_player).isTurn()) {
            System.out.println("subjectState " + this.subjectState);
            this.subjectState.setJogada(state);
            System.out.println("Update jogada : " + state + "\n");
            this.notifyAllObservers();

            //pass turn
            if (state.equals("endTurn")) {
                this.observers.get(current_player).setTurn(false);
                this.current_player = (this.current_player + 1) % this.nr_players;
                this.observers.get(current_player).setTurn(true);   //pass turn to play
                System.out.println("Turn updated\n");
            }

            //improvement
            //passToken()
        } else {
            System.out.println("Not my turn to play!");
        }
    }

    public void notifyAllObservers() throws RemoteException {
        for (ObserverRI observerRI : observers){
            observerRI.updateGame();            //no updateGame tem keys pressed para executar action em todos
        }
    }

    @Override
    public int getNr_players() throws RemoteException {
        return nr_players;
    }

    @Override
    public void setNr_players(int nr_players) throws RemoteException{
        this.nr_players = nr_players;
    }


    @Override
    public String getMapname() throws RemoteException {
        return mapname;
    }

    public String setMapname(String mapname) throws RemoteException{
        this.mapname = mapname;
        return mapname;
    }


}
