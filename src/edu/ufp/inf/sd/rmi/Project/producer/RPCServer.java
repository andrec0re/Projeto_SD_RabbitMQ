package edu.ufp.inf.sd.rabbitmqservices.Project.producer;

import com.rabbitmq.client.*;

/**
 * Server that accepts RPC calls for executing fibonacci function and returning result.
 */
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        /* try-with-resources will close resources automatically in reverse order... avoids finally */
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            //Create channel to wait for RPC calls
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            channel.queuePurge(RPC_QUEUE_NAME);

            //Set prefetchCount (may be used to spread the load equally over multiple servers)
            int prefetchCount=1;
            channel.basicQos(prefetchCount);

            System.out.println(" [x] Awaiting RPC requests");

            //Create an object as a monitor for syncing threads consumer/request (main) and producer/reply (delivery).
            Object monitor = new Object();

            //Consumer: receive call from client
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                //Reply properties
                AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                        .Builder()
                        .correlationId(delivery.getProperties().getCorrelationId())
                        .build();

                //Init reply string
                String response = "";

                try {
                    String message = new String(delivery.getBody(), "UTF-8");
                    int n = Integer.parseInt(message);
                    System.out.println(" [.] fib(" + message + ")");
                    response += Calculator.fib(n);
                   // String message2 = new String(delivery.getBody(), "UTF-8");
                //    System.out.println(Calculator.calculate(message2));
                } catch (RuntimeException e) {
                    System.out.println(" [.] " + e.toString());
                } finally {
                    //Producer: send reply back to client
                    System.out.println(" [.] Sending reply:" + response + "...");

                    //Use nameless exchange when sending directly to queue
                    channel.basicPublish("",
                            delivery.getProperties().getReplyTo(),
                            replyProps,
                            response.getBytes("UTF-8"));

                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);

                    // RabbitMq consumer worker thread notifies the RPC server owner thread
                    synchronized (monitor) {
                        monitor.notify();
                    }
                }
            };

            CancelCallback cancelCallback=(consumerTag) -> {
                System.out.println(" [x] Cancel callback activated: " + consumerTag);
            };

            //Register Consumer to wait for reply (deliveryCallback)
            boolean autoAck=false;
            channel.basicConsume(RPC_QUEUE_NAME, autoAck, deliverCallback, cancelCallback);

            //Block main thread... waits for deliveryCallback to consume the message from RPC client.
            while (true) {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
