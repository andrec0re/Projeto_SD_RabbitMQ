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
package edu.ufp.inf.sd.rmi.Project.project_rabbit;

import com.rabbitmq.client.BuiltinExchangeType;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author rjm
 */
public class ServerGuiClient extends JFrame {

    private ObserverServer server;


    /**
     * Creates new form ChatClientFrame
     *
     * @param args
     */
    public ServerGuiClient(String args[]) throws IOException, TimeoutException {
//1. Init the GUI components
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, " After initComponents()...");

        RabbitUtils.printArgs(args);

        //Read args passed via shell command
        String host=args[0];
        int port=Integer.parseInt(args[1]);
        String exchangeName=args[2];
        String queueName=args[3];
        String queueNameFrontServer=args[4];


        //2. Create the _05_observer object that manages send/receive of messages to/from rabbitmq
        this.server = new ObserverServer();
        //this.observer= new Observer(this, host, port, "guest", "guest", exchangeName, BuiltinExchangeType.FANOUT, "UTF-8", user);
        this.server.setServer(this, host, port, "guest", "guest", exchangeName,queueName, queueNameFrontServer,BuiltinExchangeType.FANOUT, "UTF-8");
    }

    /**
     * Receives notification about the reception of a message, to update the text area.
     */
    public void updateUser() {

    }





    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException, TimeoutException {
        new ServerGuiClient(args);
    }

}
