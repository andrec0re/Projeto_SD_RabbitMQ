package edu.ufp.inf.sd.rmi.Project.producer;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * RabbitMQ speaks multiple protocols, e.g., AMQP, which is an open and
 * general-purpose protocol for messaging.
 * <p>
 * There are a number of clients for RabbitMQ in many different languages.
 * We will use the Java client (amqp-client-5.9.0.jar and dependencies) provided by RabbitMQ.
 * Download most recent libraries:
 * client library (amqp-client-x.y.z.jar) and dependencies (SLF4J API and SLF4J Simple)
 * and copy them into *lib* directory.
 *
 * <p>
 * Jargon terms:
 * - RabbitMQ is a message broker, i.e., a server that accepts and forwards messages.
 * - Producer: program that sends messages (Producing means sending).
 * - Queue: post box which lives inside a RabbitMQ broker (large message buffer).
 * - Consumer: program that waits to receive messages (Consuming means receiving).
 * <p>
 * NB: the producer, consumer and broker do not have to run/reside on the same host.
 *
 * @author rui
 */
public class EmitLog {

    /*+ name of the queue */
    //public final static String QUEUE_NAME="hello_queue";

    /**
     * Run publisher Send several times from terminal (will send msg "hello world" to Recv):
     * $ ./runproducer
     *
     * Challenge: concatenate several words from command line args (args[3].. args[n]):
     * $ ./runcnsumer hello world again (will concatenate msg "hello world again" to send)
     *
     * @param args
     */
    public static void main(String[] args) {
        RabbitUtils.printArgs(args);

        //Read args passed via shell command
        String host=args[0];
        int port=Integer.parseInt(args[1]);
        String exancheName=args[2];

        /* try-with-resources will close resources automatically in reverse order... avoids finally */
        try (Connection connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
             Channel channel=RabbitUtils.createChannel2Server(connection)) {
            // Declare a queue where to send msg (idempotent, i.e., it will only be created if it doesn't exist);
          //  channel.queueDeclare(queueName, false, false, false, null);
            //channel.queueDeclare(QUEUE_NAME, true, false, false, null);

         //   String message="Hello World!";
            // Publish a message to the queue (content is byte array encoded with UTF-8)
      //      channel.basicPublish("", queueName, null, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + exancheName + "'"+ BuiltinExchangeType.FANOUT.toString());
            channel.exchangeDeclare(exancheName,BuiltinExchangeType.FANOUT);
            String message=RabbitUtils.getMessage(args,3);
            String routingkey="";
            channel.basicPublish(exancheName,routingkey,null,message.getBytes(StandardCharsets.UTF_8));
            System.out.println("Sent"+ message+"?00");

        } catch (IOException | TimeoutException e) {
            Logger.getLogger(EmitLog.class.getName()).log(Level.INFO, e.toString());
        }
        /* try-with-resources will close resources automatically in reverse order, thus avoiding finally clause.
          finally {
            try {
                // Lastly, we close the channel and the connection
                if (channel != null) { channel.close(); }
                if (connection != null) { connection.close(); }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        */
    }
}
