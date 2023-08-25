package app;

import sqs.Consumer;
import sqs.ConsumerProcess;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        // Wait for Neo4j to boot
        try{
            System.out.println("--> WAITING FOR NEO4J BOOT");
            TimeUnit.SECONDS.sleep(3);
            System.out.println("--> FINISHED WAITING");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }


//     ______               _                                             _
//    |  ____|             (_)                                           | |
//    | |__    _ __ __   __ _  _ __  ___   _ __   _ __ ___    ___  _ __  | |_
//    |  __|  | '_ \\ \ / /| || '__|/ _ \ | '_ \ | '_ ` _ \  / _ \| '_ \ | __|
//    | |____ | | | |\ V / | || |  | (_) || | | || | | | | ||  __/| | | || |_
//    |______||_| |_| \_/  |_||_|   \___/ |_| |_||_| |_| |_| \___||_| |_| \__|


        // --> 1. Get environment variables
        String uri                = System.getenv("NEO4J_URI");
        String user               = System.getenv("NEO4J_USER");
        String password           = System.getenv("NEO4J_PASSWORD");
        String problem            = System.getenv("PROBLEM");
        String formulation        = System.getenv("FORMULATION");

        // --> 2. Override variables as necessary
        // uri = "neo4j://localhost:7687";
        uri = "neo4j://localhost:7687";
        user = "neo4j";
        password = "test";
        formulation = "GNC2";
        problem     = "GNC2";

        // --> 3. Place variables into hashmap
        HashMap<String, String> env = new HashMap<>();
        env.put("uri", uri);
        env.put("user", user);
        env.put("password", password);
        env.put("problem", problem);
        env.put("formulation", formulation);
        env.put("mutation_type", "DISJOINT");

//      _____
//     / ____|
//    | |      ___   _ __   ___  _   _  _ __ ___    ___  _ __
//    | |     / _ \ | '_ \ / __|| | | || '_ ` _ \  / _ \| '__|
//    | |____| (_) || | | |\__ \| |_| || | | | | ||  __/| |
//     \_____|\___/ |_| |_||___/ \__,_||_| |_| |_| \___||_|
//        int num_runs = Runs.num_runs;






        int num_runs = 2;
        boolean print_output = false;

        // The processes we'll start
        Process[] processes = new Process[num_runs];

        for (int i = 0; i < num_runs; i++) {
            try {
                ProcessBuilder builder = new ProcessBuilder(
                        "java",
                        "-cp", System.getProperty("java.class.path"),
                        ConsumerProcess.class.getName(),
                        String.valueOf(i));  // You can pass arguments like this
                Process process = builder.start();
                processes[i] = process;

                // Capture output and print it
                if(print_output == true){
                    new Thread(() -> {
                        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                            String line;
                            while ((line = reader.readLine()) != null) {
                                System.out.println(line);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Wait for processes to finish
        for (int i = 0; i < num_runs; i++) {
            try {
                processes[i].waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }





//        Runs.createRunGroup();
//        Runs.initRun();
//
//        int num_runs = 2;
//        ExecutorService executor = Executors.newFixedThreadPool(num_runs);
//        try{
//            for (int i = 0; i < num_runs; i++) {
//                Consumer consumer = new Consumer.Builder(i).build();
//                executor.submit(consumer);
//            }
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//        // Shutdown the executor after submitting all tasks
//        executor.shutdown();
//        try {
//            // waits for termination, indefinitely
//            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }




//        try {
//            for(int x = 0; x < num_runs; x++){
//                // --> 1. Build consumer
//                Consumer consumer = new Consumer.Builder(env).build();
//
//                // --> 2. Run Consumer
//                consumer.run();
//            }
//        }
//        catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        finally {
//            System.out.println("--> FINISHED RUNNING CONSUMER");
//            System.exit(0);
//        }
    }
}
