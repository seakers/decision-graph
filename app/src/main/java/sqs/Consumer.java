package sqs;

import app.Files;
import app.Runs;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graph.Graph;
import graph.neo4j.DatabaseClient;
import moea.adg.AdgMoea;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.FileReader;
import java.util.HashMap;

public class Consumer implements Runnable{


    private boolean   running;
    private SqsClient sqsClient;
    private Graph     graph;


    public static class Builder{

        private HashMap<String, String> env;

        private SqsClient sqsClient;

        public Builder(HashMap<String, String> env){
            this.env = env;
            this.sqsClient = null;
//            this.sqsClient = SqsClient.builder()
//                    .region(Region.US_EAST_2)
//                    .endpointOverride(URI.create(env.get("localstackEndpoint")))
//                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
//                    .build();
        }

        private JsonObject getAdgSpecs() throws Exception{
            JsonObject adg_specs = new JsonObject();

            String graph_file   = Files.curr_formulation.toString();
            String problem_file = Files.curr_problem.toString();

            Gson gson_client = new Gson();
            JsonObject graph_object = gson_client.fromJson(new FileReader(graph_file), JsonObject.class);
            JsonObject problem_object = gson_client.fromJson(new FileReader(problem_file), JsonObject.class);

            adg_specs.add("graph", graph_object);
            adg_specs.add("inputs", problem_object);

            return adg_specs;
        }

        private Graph buildGraph() throws Exception{

            return new Graph.Builder(this.env.get("formulation"), this.env.get("problem"), this.getAdgSpecs())
                    .buildDatabaseClient(this.env.get("uri"), this.env.get("user"), this.env.get("password"), true, true)
                    .indexGraph()
                    .buildTopologicalOrdering()
                    .projectGraph()
                    .build();
        }

        public Consumer build() throws Exception{
            Consumer build        = new Consumer();
            build.sqsClient       = this.sqsClient;
            build.graph           = this.buildGraph();
            build.running         = true;
            return build;
        }
    }

    public void run(){
        System.out.println("--> RUNNING CONSUMER");

        // this.testCrossover();
        this.runMoea();



        while(this.running){
            this.running = false;
        }
    }






    public void runMoea(){
        int pop_size = Runs.pop_size;
        int nfe = Runs.nfe;
        int num_objectives = 2;

        AdgMoea moea = new AdgMoea.Builder(this.graph)
                .setProperties(nfe, 1.0, 0.05, num_objectives)
                .buildPopulaiton(pop_size)
                .build();
        try {
            Thread moea_thread = new Thread(moea);
            moea_thread.start();
            moea_thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    public void testCrossover(){
        try{
            int d1 = this.graph.generateRandomDesign();
            int d2 = this.graph.generateRandomDesign();

            System.out.println("\n------- CROSSOVER -------");
            this.graph.crossoverDesigns(d1, d2, 0.4);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }




}
