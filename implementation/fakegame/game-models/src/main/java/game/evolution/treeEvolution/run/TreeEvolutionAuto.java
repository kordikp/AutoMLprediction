package game.evolution.treeEvolution.run;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.util.Properties;
import java.util.concurrent.Semaphore;

/**
 * Main class for running TreeEvolution algorithm
 * Author: cernyjn
 */
public class TreeEvolutionAuto {

    public static void main(String[] args) {
        //String filename = "./data/arff/vehicle.arff";
        String filename = "./data/iris.txt";
        int time = 60;
        String logLevel = "INFO";
        int repeat = 4;
        int threads = 2;


        Logger log = Logger.getLogger("AutomatedDataMining");

        if (args.length == 5) {
            filename = args[0];
            time = Integer.parseInt(args[1]);
            repeat = Integer.parseInt(args[2]);
            logLevel = args[3];
            threads = Integer.parseInt(args[4]);
        } else if (args.length != 0) {
            log.fatal("PARAMETERS: [pathToDataFile] [runtimeInSeconds] [repeatNumberOfTimes] [logLevel] [numberOfExperimentThreads]");
            log.fatal("help:");
            log.fatal("[logLevel] = {DEBUG, INFO, WARN, ERROR, FATAL}");
            return;
        }

        //SETUP LOGGER
        Properties p = new Properties();
        p.setProperty("log4j.rootLogger", logLevel + ", A1");
        p.setProperty("log4j.appender.A1", "org.apache.log4j.ConsoleAppender");
        p.setProperty("log4j.appender.A1.layout", "org.apache.log4j.PatternLayout");
        p.setProperty("log4j.appender.A1.layout.ConversionPattern", "%d{ABSOLUTE};%m%n");
        PropertyConfigurator.configure(p);

        runExperiment(filename, time, repeat, threads);
    }

    /**
     * Runs current experiment setup, executing each experiment in separate thread. Each experiment thread then can have
     * multiple computation threads.
     *
     * @param filename         Path to data file.
     * @param computationTimeS Limit of computation time.
     * @param repeat           How many times to repeat experiment.
     * @param numberOfThreads  Number of experiment threads.
     */
    private static void runExperiment(String filename, int computationTimeS, int repeat, int numberOfThreads) {
        int computed = 0;
        ExperimentThread thread;
        Semaphore activeThreads = new Semaphore(numberOfThreads);
        try {
            while (repeat != computed) {
                activeThreads.acquire();
                thread = new ExperimentThread(filename, computationTimeS, activeThreads);
                thread.start();
                computed++;
            }
            //wait for last threads
            activeThreads.acquire(numberOfThreads);
        } catch (InterruptedException e) {
            Logger log = Logger.getLogger("AutomatedDataMining");
            log.error("parallel error: " + e.getMessage());
        }
    }

}
