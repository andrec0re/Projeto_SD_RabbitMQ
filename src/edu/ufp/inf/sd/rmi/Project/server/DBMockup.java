package edu.ufp.inf.sd.rmi.Project.server;

import edu.ufp.inf.sd.rmi.Project.client.engine.Game;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * This class simulates a DBMockup for managing users and books.
 *
 * @author rmoreira
 *
 */
public class DBMockup {

   // private final ArrayList<Book> books;// = new ArrayList();
    private ArrayList<User> users;
    private ArrayList<AdvancedWarsGame> games;
    private int index=1;

    public DBMockup() throws RemoteException {
        users = new ArrayList();
        games = new ArrayList();
        users.add(new User("teste1", "ufp1"));
        users.add(new User("teste2", "ufp2"));
        //games.add(new AdvancedWarsGame(2,"C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\FourCorners.txt","teste1",2));
    }

    public ArrayList<AdvancedWarsGame> getGames() {
        return games;
    }

    public void addGame(AdvancedWarsGame game) {
        games.add(game);
    }
    /**
     * Registers a new user.
     *
     * @param u username
     * @param p passwd
     */
    public boolean Register(String u, String p) {
        if (!exists(u, p)) {
            users.add(new User(u, p));
            return true;
        }
        return false;
    }

    /**
     * Checks the credentials of an user.
     *
     * @param u username
     * @param p passwd
     * @return
     */
    public boolean exists(String u, String p) {
        for (User usr : this.users) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPword().compareTo(p) == 0) {
                return true;
            }
        }
        return false;
        //return ((u.equalsIgnoreCase("guest") && p.equalsIgnoreCase("ufp")) ? true : false);
    }

    /**
     * Inserts a new book into the DigLib.
     *
     * @param t title
     * @param a authors
     */
    public void insert(String t, String a) {
        users.add(new User(t, a));
    }

    public AdvancedWarsGame searchGame(int id) {
        for (AdvancedWarsGame game : games) {
            if (game.getId() == id) {
                return game; // Found the game with the matching ID
            }
        }
        return null; // Game not found
    }

    public AdvancedWarsGame insertGame(String mapa, String username,SubjectRI subjectRI) {
        int maxPlayers = 0;
        if (mapa.equals("C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\SmallVs.txt")) {
            maxPlayers = 2;
        } else if (mapa.equals("C:\\Users\\olaso\\IdeaProjects\\ProjetoSD\\maps\\FourCorners.txt")) {
            maxPlayers = 4;
        }

        //int id = games.size() + 1;
        AdvancedWarsGame game = new AdvancedWarsGame(index,mapa, username,subjectRI,maxPlayers);
        games.add(game);
        index++;        //update id
        return game;
    }

    public ArrayList<AdvancedWarsGame> listGames() {
        return games;
    }
}

