package edu.ufp.inf.sd.rmi.Project.consumer;

import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * Client that makes RPC call to server fibonacci function.
 *
 *
 *
 * ==========================================
 * Challenge:
 * Implement a calculator for basic operations: add, sub, mult, div.
 *
 * Requests must be coded in json
 *  - download jar from: https://github.com/stleary/JSON-java
 *  - add jar to *lib* directory
 *  - add reference to jar into setenv shell script
 *
 * e.g.
 * Request: {"operation":"add", "values":[10.0, 8.0]}
 * Reply: {"result":18.0}
 *
 */
public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");

        connection = factory.newConnection();
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        //try-with-resources...
        try (RPCClient fibonacciRpc = new RPCClient()) {
            //RPC for calculating fibonacci of numbers between [0..31]
          do {
              System.out.println("1-ADD\n2-Sub\n3-Mult\n4-Div\n");
              BufferedReader reader = new BufferedReader(
                      new InputStreamReader(System.in));
              String name = reader.readLine();
              // Reading data using readLine
              int h=0;
              do{
                  h++;
              }while(h<5);

              fibonacciRpc.call(name);
          }while(true);

        } catch (IOException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public String call(String message) throws IOException, InterruptedException {
        //Generate a correlation ID (between call and reply)
        final String correlationID = UUID.randomUUID().toString();

        //Get automatic queue for receiving reply
        String replyQueueName = channel.queueDeclare().getQueue();

        //Build properties with correlationID and replyQueueName
        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(correlationID)
                .replyTo(replyQueueName)
                .build();

        //Publish call to requestQueue (empty exchange) with given properties
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        //Create array blocking queue for handler to store reply
        final BlockingQueue<String> responseArrayBlockQueue = new ArrayBlockingQueue<>(1);

        //Create callback that will receive reply message
        DeliverCallback deliverCallback=(consumerTag, delivery) -> {
            if (delivery.getProperties().getCorrelationId().equals(correlationID)) {
                responseArrayBlockQueue.offer(new String(delivery.getBody(), StandardCharsets.UTF_8));
            }
        };
        CancelCallback cancelCallback=(consumerTag) -> {
            System.out.println(" [x] Cancel callback activated: " + consumerTag);
        };

        //Associate callback for consuming RPC reply
        String ctag=channel.basicConsume(replyQueueName, true, deliverCallback, cancelCallback);

        //Get reply from array blocking queue
        String result=responseArrayBlockQueue.take();

        channel.basicCancel(ctag);
        return result;
    }

    /**
     * Close connection to rabbitmq.
     *
     * @throws IOException
     */
    public void close() throws IOException {
        connection.close();
    }
}

