package edu.ufp.inf.sd.rmi.Project.server;

import java.time.Clock;
import java.time.Instant;

public class TokenRingAlgo {

    private int numNodes;
    private int tokenHolder;
    private Instant instant;

    public TokenRingAlgo(Clock clock, int numNodes) {
        this.instant = clock.instant();
        this.numNodes = numNodes;
        this.tokenHolder = 0;
    }

    public void passToken() {
        this.tokenHolder++;
        if (this.tokenHolder >= this.numNodes) {
            this.tokenHolder = 0;
        }
    }

    public int getTokenHolder() {
        return this.tokenHolder;
    }

}
