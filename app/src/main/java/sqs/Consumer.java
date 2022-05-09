package sqs;

import graph.Graph;
import graph.neo4j.DatabaseClient;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

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
            this.sqsClient = SqsClient.builder()
                    .region(Region.US_EAST_2)
                    .endpointOverride(URI.create(env.get("localstackEndpoint")))
                    .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                    .build();
        }

        private DatabaseClient buildNeo4jClient(){
            return new DatabaseClient.Builder(this.env.get("uri"))
                    .setCredentials(this.env.get("user"), this.env.get("password"))
                    .setFormulation(this.env.get("formulation"))
                    .setProblem(this.env.get("problem"))
                    .build();
        }

        private Graph buildGraph(){
            DatabaseClient client = this.buildNeo4jClient();
            return new Graph.Builder(client, this.env.get("formulation"), this.env.get("problem"), true, true)
                    .indexGraph()
                    .buildTopologicalOrdering()
                    .projectGraph()
                    .build();
        }

        public Consumer build(){
            Consumer build        = new Consumer();
            build.sqsClient       = this.sqsClient;
            build.graph           = this.buildGraph();
            build.running         = true;
            return build;
        }
    }



    public void run(){
        System.out.println("--> RUNNING CONSUMER");


        try{
            int d1 = this.graph.generateRandomDesign();
            int d2 = this.graph.generateRandomDesign();
            this.graph.crossoverDesigns(d1, d2, 0.5);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }


        while(this.running){
            this.running = false;
        }
    }
}
