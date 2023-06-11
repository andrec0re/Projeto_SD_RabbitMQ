/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2011</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui Moreira
 * @version 2.0
 */
package edu.ufp.inf.sd.rabbitmqservices.Project.project_rabbit;

import com.rabbitmq.client.BuiltinExchangeType;
import edu.ufp.inf.sd.rabbitmqservices.projeto.util.RabbitUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rjm
 */
public class ObserverGuiClient extends JFrame {

    private Observer observer;
    private String generalTopic;

    /**
     * Creates new form ChatClientFrame
     *
     * @param args
     */
    public ObserverGuiClient(String args[]) throws IOException, TimeoutException, InterruptedException {
        //1. Init the GUI components
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, " After initComponents()...");

        RabbitUtils.printArgs(args);

        //Read args passed via shell command
        String host=args[0];
        int port=Integer.parseInt(args[1]);
        String exchangeName=args[2];
        //String room=args[3];
        String queue=args[3];
        String queueFrontServer=args[4];

        String user=args[5];
        //String general=args[5];

        if(Objects.equals(user, "")){
            System.out.println("oi");
        }
        //2. Create the _05_observer object that manages send/receive of messages to/from rabbitmq
        this.observer = new Observer(this, host, port, "guest", "guest", exchangeName,queue,queueFrontServer, BuiltinExchangeType.FANOUT, "UTF-8", user);


    }

    /**
     * Receives notification about the reception of a message, to update the text area.
     */
    public void updateUser() {

    }

    /**
     * Allows sending a message via the _05_observer, to some user or to generalTopic (all).
     * - Send message to general when strFromJTextField = "some message"
     * - Send message to given user when strFromJTextField = "user@some message"
     *
     * @param strFromJTextField
     */
    private void sendMsg(String strFromJTextField,String user) {
        try {
            strFromJTextField = "["+user+"]- " + strFromJTextField;
            this.observer.sendMessage(strFromJTextField);
        } catch (IOException ex) {
            Logger.getLogger(ObserverGuiClient.class.getName()).log(Level.SEVERE, null, ex);
        }
    }



    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException, TimeoutException, InterruptedException {
        new ObserverGuiClient(args);

    }

}
