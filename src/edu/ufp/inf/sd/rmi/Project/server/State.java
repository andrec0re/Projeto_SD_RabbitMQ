package edu.ufp.inf.sd.rmi.Project.server;

import java.io.Serializable;


public class State implements Serializable {

    public String jogada;
    public int jogador;
    public String id;
    /**
     *
     * @param id
     * @param m
     */
    public State(String id, String m) {
        this.id = id;
        this.jogada = m;
    }

    public String getJogada() {
        return jogada;
    }

    public void setJogada(String jogada) {
        this.jogada = jogada;
    }

    public int getJogador() {
        return jogador;
    }

    public void setJogador(int jogador) {
        this.jogador = jogador;
    }
}