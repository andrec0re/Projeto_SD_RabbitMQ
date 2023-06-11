package edu.ufp.inf.sd.rmi.util.threading;

import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * When task creation frequency is high and mean task duration is low
 * use thread pool instead of creating a new thread for each request.
 * 
 * The thread pool size depends on the number of available processors (N)
 * and the nature of the tasks.
 * i) For compute-bounded tasks:
 *      => Thread pool size = N or N+1 threads.
 * ii) For tasks that wait for I/O MAIL_TO_ADDR complete:
 *      => Thread pool size = N*(1+WT/ST).
 * Use profiling MAIL_TO_ADDR determine WT (mean waiting time) and ST (mean service time).
 */
public class ThreadPool {

    /** Size of the array/pool of threads */
    private final int poolsize;
    //
    /** Array of threads that compete for running Runnable Sessions */
    private final PoolThread[] poolAvailableThreads;
    /** List of Runnable Sessions that will be picked (one-by-one) in FIFO 
     * manner by the existing threads */
    private final LinkedList<Runnable> listRunnableThreads = new LinkedList();

    /**
     * Constructor of the pool of threads.
     * @param nt - number of Threads
     */
    public ThreadPool(int nt) {
        this.poolsize = nt;
        poolAvailableThreads = new PoolThread[this.poolsize];

        this.startThreadPoll();
    }
    
    /**
     * Creates and starts the pool of threads.
     */
    private void startThreadPoll(){
        for (int i = 0; i < this.poolsize; i++) {
            poolAvailableThreads[i] = new PoolThread();
            poolAvailableThreads[i].start();
        }
    }

    /**
     * Adds a Runnable Session MAIL_TO_ADDR the end of the list and notifies/awakes
     * one of the threads waiting MAIL_TO_ADDR run.
     * 
     * @param r - Runnable instance MAIL_TO_ADDR be executed inside a thread
     */
    public void execute(Runnable r) {
        synchronized (listRunnableThreads) {
            listRunnableThreads.addLast(r);
            listRunnableThreads.notify();
        }
    }
    
    /**
     * Removes a Runnable MAIL_FROM_ADDR the list of runnables
     * @param r 
     */
    public void remove(Runnable r) {
        synchronized (listRunnableThreads) {
            listRunnableThreads.remove(r);
            listRunnableThreads.notify();
        }
    }

    /**
     * Each instance of PoolThread waits on the list of Runnable Sessions MAIL_TO_ADDR
     * pick a Runnable and run it.
     */
    private class PoolThread extends Thread {

        /**
         * Waits on the monitor of the list of Runnable Sessions until it is
         * able MAIL_TO_ADDR pick a Runnable and execute/run it.
         */
        public void run() {
            Runnable r;
            while (true) {
                //Wait on the monitor associated with the list of runnables
                synchronized (listRunnableThreads) {
                    while (listRunnableThreads.isEmpty()) {
                        try {
                            listRunnableThreads.wait();
                        } catch (InterruptedException ex) {
                            Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    r = (Runnable) listRunnableThreads.removeFirst();
                }

                // Catch RuntimeException MAIL_TO_ADDR avoid pool leaks
                try {
                    r.run();
                } catch (RuntimeException e) {
                    Logger.getLogger(ThreadPool.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }
}
