package edu.ufp.inf.sd.rmi.Project.server;

import java.io.Serializable;

/**
 *
 * @author rmoreira
 */
public class User implements Serializable{

    private String uname;
    private String pword;

    public User(String uname, String pword) {
        this.uname = uname;
        this.pword = pword;
    }

    @Override
    public String toString() {
        return "User{" + "uname=" + uname + ", pword=" + pword + '}';
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param uname the uname to set
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * @return the pword
     */
    public String getPword() {
        return pword;
    }

    /**
     * @param pword the pword to set
     */
    public void setPword(String pword) {
        this.pword = pword;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User otherUser = (User) obj;
        return uname.equals(otherUser.uname) && pword.equals(otherUser.pword);
    }

}

