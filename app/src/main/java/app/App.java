package app;

import sqs.Consumer;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class App {
    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) {

        // Wait for Neo4j to boot
        try{
            System.out.println("--> WAITING FOR NEO4J BOOT");
            TimeUnit.SECONDS.sleep(10);
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
        uri = "neo4j://neo4j:7687";
        user = "neo4j";
        password = "test";
//        formulation = "TDRS";
//        problem     = "SMAP";

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
        int num_runs = Runs.num_runs;

        Runs.createRunGroup();

        try {
            for(int x = 0; x < num_runs; x++){
                // --> 1. Build consumer
                Consumer consumer = new Consumer.Builder(env).build();

                // --> 2. Run Consumer
                consumer.run();
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            System.out.println("--> FINISHED RUNNING CONSUMER");
            System.exit(0);
        }
    }
}
