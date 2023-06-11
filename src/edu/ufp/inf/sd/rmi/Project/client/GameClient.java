package edu.ufp.inf.sd.rmi.Project.client;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;
import edu.ufp.inf.sd.rmi.Project.server.*;
import edu.ufp.inf.sd.rmi.util.rmisetup.SetupContextRMI;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class GameClient {

    /**
     * Context for connecting a RMI client MAIL_TO_ADDR a RMI Servant
     */
    private SetupContextRMI contextRMI;

    private GameFactoryRI gameFactoryRI;

    private GameSessionRI gameSessionRI;

    public Game start_game;

    public static void main(String[] args) throws RemoteException {
        if (args != null && args.length < 2) {
            System.err.println("usage: java [options] edu.ufp.sd.inf.rmi._01_helloworld.server.HelloWorldClient <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Setup client RMI context ============
            GameClient hwc = new GameClient(args);
            //2. ============ Lookup service ============
            hwc.lookupService();
            //3. ============ Play with service ============
            hwc.playService();
        }
    }

    public GameClient(String args[]) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(GameClient.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private Remote lookupService() {
        try {
            //Get proxy MAIL_TO_ADDR rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Lookup service on rmiregistry and wait for calls
            if (registry != null) {
                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going MAIL_TO_ADDR lookup service @ {0}", serviceUrl);

                //============ Get proxy MAIL_TO_ADDR HelloWorld service ============
                gameFactoryRI = (GameFactoryRI) registry.lookup(serviceUrl);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return gameFactoryRI;
    }

    private void playService() {

        //Scanner sc = new Scanner(System.in);
        try {
            Scanner e = new Scanner(System.in);
            String username = "";
            String pass = "";

            Boolean maxPlayersReached = false;

            System.out.println("         |-----------------------------------------------|");
            System.out.println("         |Welcome to Advance Wars Game Multiplayer|");
            System.out.println("         |-----------------------------------------------|");
            System.out.println("");

            Boolean autoRun = true;
            int op1 = 0;
            while (autoRun) {

                System.out.println("          Would you like to Register or Login?");
                System.out.println("");
                System.out.println("                     Choose: 1-REGISTER | 2-LOGIN");
                System.out.println("");
                System.out.print("                        Waiting for input: ");

                op1 = e.nextInt();
                if (op1 == 1) {
                    System.out.println();
                    System.out.println("Enter Username: ");
                    username = e.next();
                    System.out.println("Enter Password: ");
                    pass = e.next();
                    User player = new User(username, pass);
                    this.gameFactoryRI.Register(player);        //GameFactory
                    System.out.println("User Registado com sucesso\n\n");

                } else if (op1 == 2) {
                    System.out.println();
                    System.out.println("Enter Username: ");
                    username = e.next();
                    System.out.println("Enter Password: ");
                    pass = e.next();
                    User player = new User(username, pass);
                    gameSessionRI = this.gameFactoryRI.Login(player);           //GameFactory
                    gameSessionRI.setUser(player);
                    gameSessionRI.setUsername(username);
                    if (gameSessionRI != null) {
                        autoRun = false;
                    } else {
                        System.out.println("WRONG LOGIN CREDENTIALS, TRY AGAIN");
                    }

                } else {
                    System.out.println("WRONG INPUT TRY AGAIN");
                }
            }

            System.out.println("");
            System.out.println("                          |--------------------|");
            System.out.println("                          |Logged In Sucefully!|");
            System.out.println("                          |--------------------|");
            System.out.println("");


            System.out.println("--------------------------------------------------------------------");
            int value = 0;
            Scanner option = new Scanner(System.in);
            System.out.println("1-CREATE NEW GAME\n2-JOIN A GAME");
            while (value != 1 && value != 2) {
                value = option.nextInt();
                if (value == 1) {
                    createGame(this.start_game, gameSessionRI, username);
                } else if (value == 2) {
                    joinGame(this.start_game, gameSessionRI, username);
                }
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "ALL GOOD");
        } catch (RemoteException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void createGame(Game new_game, GameSessionRI gameSessionRI, String user) throws RemoteException {
        System.out.println("Mapas:\n1- SmallVls \t 2-FourCorners\n");
        int mapa = 0;
        while (mapa != 1 && mapa != 2) {
            Scanner option = new Scanner(System.in);
            mapa = option.nextInt();
        }
        String map;
        if (mapa == 1) {
            map = "C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\SmallVs.txt";
            System.out.println("Mapa1: " + map);
        } else {
            map = "C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\FourCorners.txt";
        }

        ObserverRI observerRI = new ObserverImpl(user);//System.out.println("Obs criado");
        observerRI.setGame(new_game);//System.out.println("setgame criado");
        AdvancedWarsGame game = gameSessionRI.new_game(map, user); System.out.println("new_game criado");    //new server game

        //create Game
        observerRI.setSubjectRI(game.getSubjectRI());System.out.println("getsubject criado");
        observerRI.getSubjectRI().attach(observerRI);System.out.println("attach criado\n");

        //server - set mapxplayers
        if (observerRI.getSubjectRI().getObservers().size() != game.getMaxPlayers()) {
            System.out.println("Max players - " + game.getMaxPlayers());
            System.out.println("Current players - " + observerRI.getSubjectRI().getObservers().size());
            System.out.println("Waiting for players...");
            Scanner scanner = new Scanner(System.in);   //refresh with enter
            scanner.nextLine();
        }

        new_game = new Game(map, observerRI);
    }


    public static void joinGame(Game new_game, GameSessionRI gameSessionRI, String user) throws RemoteException {
        System.out.println("All created Games:\n");
        ArrayList<AdvancedWarsGame> games = gameSessionRI.listGames();
        for (AdvancedWarsGame game : games) {
            System.out.println("ID:"+ game.getId() + " Mapa:" + game.getMap() + " owner:"+ game.getOwner()+ "\n");
        }

        Scanner option = new Scanner(System.in);
        System.out.println("Choose game ID to join:\n");
        int id = option.nextInt();
        ObserverRI observerRI = new ObserverImpl(user);

        AdvancedWarsGame game= gameSessionRI.available_games(observerRI, id);

        observerRI.setSubjectRI(game.getSubjectRI());
        observerRI.getSubjectRI().attach(observerRI);
        observerRI.setGame(new_game);

        if (observerRI.getSubjectRI().getObservers().size() != game.getMaxPlayers()) {
            System.out.println("Max players - " + game.getMaxPlayers());
            System.out.println("N players - " + observerRI.getSubjectRI().getObservers().size());
            System.out.println("Waiting for players...");
            Scanner scanner = new Scanner(System.in);   //refresh with enter
            scanner.nextLine();
        }
        String mapa = observerRI.getSubjectRI().getMapname();
        new_game = new Game(mapa, observerRI);              //same game lobby
    }

}