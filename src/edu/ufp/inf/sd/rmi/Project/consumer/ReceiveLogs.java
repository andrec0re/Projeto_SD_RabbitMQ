package edu.ufp.inf.sd.rmi.Project.consumer;

/**
 * 1. Run the rabbitmq server as a shell process, by calling:
 * $ rabbitmq-server
 *
 * <p>
 * 2. Run consumer Recv that keeps running, waiting for messages from publisher Send (Use Ctrl-C to stop):
 * $ ./runconsumer
 *
 * <p>
 * 3. Run publisher Send several times from terminal (will send mesg "hello world"):
 * $ ./runproducer
 *
 * <p>
 * 4. Check RabbitMQ Broker runtime info (credentials: guest/guest4rabbitmq) from:
 * http://localhost:15672/
 *
 * @author rui
 */

import com.rabbitmq.client.*;
import edu.ufp.inf.sd.rmi.util.RabbitUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ReceiveLogs {

    /**
     * Run consumer Recv that keeps running, waiting for messages from publisher Send (Use Ctrl-C to stop):
     * $ ./runconsumer
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            RabbitUtils.printArgs(args);

            //Read args passed via shell command
            String host=args[0];
            int port=Integer.parseInt(args[1]);
            String queueName=args[2];

            // Open a connection and a channel to rabbitmq broker/server
            Connection connection=RabbitUtils.newConnection2Server(host, port, "guest", "guest");
            Channel channel=RabbitUtils.createChannel2Server(connection);

            //Declare queue from which to consume (declare it also here, because consumer may start before publisher)
        //    channel.queueDeclare(queueName, false, false, false, null);
            //channel.queueDeclare(Send.QUEUE_NAME, true, false, false, null);
          //  Logger.getAnonymousLogger().log(Level.INFO, Thread.currentThread().getName()+": Will create Deliver Callback...");
            //System.out.println(" [*] Waiting for messages. To exit press CTRL+C");
channel.exchangeDeclare(queueName,BuiltinExchangeType.FANOUT);
            // Publisher pushes messages asynchronously, hence use DefaultConsumer callback
            //   that buffers messages until consumer is ready to handle them.
            /*Consumer client = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    String message=new String(body, "UTF-8");
                    System.out.println(" [x] Received '" + message + "'");
                }
            };
            channel.basicConsume(queueName, true, client);
            */

String queuename=channel.queueDeclare().getQueue();
String routingkey="";
channel.queueBind(queuename,queueName,routingkey);
Logger.getAnonymousLogger().log(Level.INFO,Thread.currentThread().getName()+":will create callback");
System.out.println("waiting msg");
            //DeliverCallback is an handler callback (lambda method) to consume messages pushed by the sender.
            //Create an handler callback to receive messages from queue
            DeliverCallback deliverCallback=(consumerTag, delivery) -> {
                String message=new String(delivery.getBody(), "UTF-8");
                System.out.println(" [x] Received '" +consumerTag+ "Message"+ message + "'");

            };
           CancelCallback cancelCallback=(consumerTag)->{
               System.out.println(" [x] Received '" +consumerTag+ "Cancel" );
           };
           channel.basicConsume(queuename,true,deliverCallback,cancelCallback);

        } catch (Exception e) {
            //Logger.getLogger(Recv.class.getName()).log(Level.INFO, e.toString());
            e.printStackTrace();
        }
    }
}
