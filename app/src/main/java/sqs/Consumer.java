package sqs;

import app.Files;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import graph.Graph;
import graph.neo4j.DatabaseClient;
import moea.AdgMoea;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.FileReader;
import java.net.URI;
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

        private DatabaseClient buildNeo4jClient(){

            return new DatabaseClient.Builder(this.env.get("uri"))
                    .setCredentials(this.env.get("user"), this.env.get("password"))
                    .setFormulation(this.env.get("formulation"))
                    .setProblem(this.env.get("problem"))
                    .build();
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
            DatabaseClient client = this.buildNeo4jClient();

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
        this.testMoea();



        while(this.running){
            this.running = false;
        }
    }

    public void testMoea(){

        AdgMoea moea = new AdgMoea.Builder(this.graph)
                .setProperties(30, 1.0, 0.05, 2)
                .buildPopulaiton(20)
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
