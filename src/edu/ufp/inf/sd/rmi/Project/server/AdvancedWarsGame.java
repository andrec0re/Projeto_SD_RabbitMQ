package edu.ufp.inf.sd.rmi.Project.server;
import java.io.Serializable;

public class AdvancedWarsGame implements Serializable{
    private SubjectRI subjectRI;
    private int maxPlayers;
    private int id;
    private String map;
    private String owner;

    public AdvancedWarsGame(int id, String map, String creator,SubjectRI subjectRI, int maxPlayers) {
        this.subjectRI = subjectRI;
        this.maxPlayers = maxPlayers;
        this.id = id;
        this.map = map;
        this.owner = creator;
    }

    public AdvancedWarsGame(int id,String map,String creator,int maxPlayers) {
        this.maxPlayers = maxPlayers;
        this.owner=creator;
        this.id = id;
        this.map = map;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMap() {
        return map;
    }

    public void setMap(String map) {
        this.map = map;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public SubjectRI getSubjectRI() {
        return subjectRI;
    }

    public void setSubjectRI(SubjectRI subjectRI) {
        this.subjectRI = subjectRI;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers(int maxPlayers) {
        this.maxPlayers = maxPlayers;
    }
}
