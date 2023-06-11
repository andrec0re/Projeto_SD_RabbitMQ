package edu.ufp.inf.sd.rmi.Project.client;
import edu.ufp.inf.sd.rmi.Project.client.menus.MenuHandler;
import edu.ufp.inf.sd.rmi.Project.client.players.Base;
import edu.ufp.inf.sd.rmi.Project.server.State;
import edu.ufp.inf.sd.rmi.Project.server.SubjectRI;
import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import edu.ufp.inf.sd.rmi.Project.client.menus.Pause;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ObserverImpl extends UnicastRemoteObject implements ObserverRI {

    private String username;
    protected Game game;
    private State lastObserverState;
    public SubjectRI subjectRI;
    private int commander;
    private boolean turn=false;

    public boolean isTurn() throws RemoteException{
        return turn;
    }

    public void setTurn(boolean turn) throws RemoteException{
        this.turn = turn;
        if (turn){
            System.out.println("Your turn\n");
        }
        else {
            System.out.println("Waiting turn\n");
        }
    }

    public ObserverImpl(String username, int commander, SubjectRI subjectRI, Game g) throws RemoteException {
        super();
        this.username = username;
        this.game=g;
        this.commander = commander;
        this.lastObserverState=new State(null,null);
        this.subjectRI = subjectRI;
        this.subjectRI.attach(this);
    }

    public ObserverImpl(String username) throws RemoteException {
        this.username = username;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    public int getCommander() throws RemoteException {
        return this.commander;
    }

    public State getLastObserverState() throws RemoteException {
        return this.subjectRI.getSubjectState();
    }

    public String getLastObserver() throws RemoteException {
        return this.subjectRI.getSubjectState().getJogada();
    }

    public void setUsername(String username) throws RemoteException{
        this.username = username;
    }

    public Game getGame() throws RemoteException{
        return game;
    }

    public void setGame(Game game) throws RemoteException{
        this.game = game;
    }

    public void setLastObserverState(State lastObserverState) throws RemoteException{
        this.lastObserverState = lastObserverState;
    }

    public SubjectRI getSubjectRI() throws RemoteException{
        return subjectRI;
    }

    public void setSubjectRI(SubjectRI subjectRI) throws RemoteException{
        this.subjectRI = subjectRI;
    }

    public void setCommander(int commander) throws RemoteException{
        this.commander = commander;
    }

    @Override
    public void updateGame() throws RemoteException {
        /*this.lastObserverState = this.subjectRI.getState();
        //    this.chatFrame.updateTextArea();
        System.out.println("UPDATE:"+getLastObserverState().getJogador()+"|||||||||||||||||||"+getLastObserverState().getJogada());
        game.update_players(this.lastObserverState);*/
        String state = this.getLastObserver();
        if(Game.GameState == Game.State.PLAYING) {

            if (state.startsWith("Buy")) {                    //check it starts with Buy + check 3 values
                String[] params = state.split("-");
                if (params.length != 4) {
                    System.err.println("Error in format");
                } else {
                    try {
                        int c1 = Integer.parseInt(params[1]);
                        int c2 = Integer.parseInt(params[2]);
                        int c3 = Integer.parseInt(params[3]);
                        System.out.println("items " + c1 + " " + c2 + " " + c3);
                        Game.btl.Buyunit(c1, c2, c3);                                 //add items
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing items");
                        e.printStackTrace();
                    }

                    MenuHandler.CloseMenu();
                }
                return;
            }

            Base ply = Game.player.get(Game.btl.currentplayer);
            switch (state) {
                case "up":
                    ply.selecty--;
                    if (ply.selecty < 0) {
                        ply.selecty++;
                    }
                    break;
                case "down":
                    ply.selecty++;
                    if (ply.selecty >= Game.map.height) {
                        ply.selecty--;
                    }
                    break;
                case "left":
                    ply.selectx--;
                    if (ply.selectx < 0) {
                        ply.selectx++;
                    }
                    break;
                case "right":
                    ply.selectx++;
                    if (ply.selectx >= Game.map.width) {
                        ply.selectx--;
                    }
                    break;
                case "select":
                    Game.btl.Action();
                    break;
                case "cancel":
                    Game.player.get(Game.btl.currentplayer).Cancle();
                    break;
                case "start":
                    new edu.ufp.inf.sd.rmi.Project.client.menus.Pause();
                    break;
                case "endTurn":
                    MenuHandler.CloseMenu();
                    Game.btl.EndTurn();
                    break;
                default:
                /*// TODO: Find a way to handle buy state
                String[] params = state.split(":");
                Game.btl.Buyunit(Integer.parseInt(params[1]),
                        Integer.parseInt(params[2]),
                        Integer.parseInt(params[3]));
                MenuHandler.CloseMenu();*/
            }
        }
    }

}
