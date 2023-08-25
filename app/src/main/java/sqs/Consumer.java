package sqs;

import app.Files;
import app.Runs;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import evaluation.GNC_Evaluator2;

import com.google.gson.reflect.TypeToken;
import graph.Graph;
import graph.neo4j.DatabaseClient;
import moea.adg.AdgMoea;
import moea.vanilla.TdrsFullSolution;
//import scan.elements.Architecture;
//import scan.wrapper.ScanWrapper;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Consumer implements Runnable{


    private boolean   running;
    private SqsClient sqsClient;
    private Graph     graph;
    private int run_num;


    public static class Builder{

        private HashMap<String, String> env;

        private SqsClient sqsClient;
        private int run_num;

        public Builder(int run_num){
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

            this.env = env;
            this.sqsClient = null;
            this.run_num = run_num;
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
            String run_formulation = this.env.get("formulation") + "_" + Integer.toString(this.run_num);
            String run_problem = this.env.get("problem") + "_" + Integer.toString(this.run_num);

            return new Graph.Builder(run_formulation, run_problem, this.getAdgSpecs())
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
            build.run_num = run_num;
            return build;
        }
    }

    public void run(){
        System.out.println("--> RUNNING CONSUMER");

//        this.testRandomDesign();
//        this.testCrossover();
        this.runMoea();
        // this.testFunc();
//        this.testfunc2();


        while(this.running){
            this.running = false;
        }
    }


    public void testfunc2() {

        try {

            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            int design_idx = this.graph.generateRandomDesign();
            JsonObject antenna_assignment = this.graph.getDesignDecision(design_idx, "Antenna Assignment");
            JsonObject antenna_partitioning = this.graph.getDesignDecision(design_idx, "Antenna Partitioning");
            JsonObject contract_modalities = this.graph.getDesignDecision(design_idx, "Contract Modalities");
            JsonObject network_types = this.graph.getDesignDecision(design_idx, "Network Types");
            JsonObject ground_station = this.graph.getDesignDecision(design_idx, "Ground Stations");
            JsonObject frac_strategy = this.graph.getDesignDecision(design_idx, "Frac Strategy");

            ArrayList<Integer> aa_chromosome = gson.fromJson(antenna_assignment.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
            System.out.println(gson.toJson(ground_station));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }



    public void runMoea(){
        int pop_size = Runs.pop_size;
        int nfe = Runs.nfe;
        int num_objectives = 2;
        AdgMoea moea = new AdgMoea.Builder(this.graph, this.run_num)
                .setProperties(nfe, num_objectives)
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


    public void testRandomDesign(){
        try{
            int d1 = this.graph.generateRandomDesign();
            System.out.println("\n------- RANDOM DESIGN -------");
            JsonObject design = this.graph.getDesign(d1);

            GNC_Evaluator2 evaluator = new GNC_Evaluator2();
            evaluator.evaluate2(design);


            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(gson.toJson(design));
        }
        catch (Exception ex){
            ex.printStackTrace();
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


//    public void testFunc(){
//        ScanWrapper wrapper = new ScanWrapper("/scan/TestCase_wgs.INPUTS");
//
//        System.out.println("--> TESTING");
//
//        try{
//            for(int x = 0; x < 1; x++){
//                TdrsFullSolution test_soln = new TdrsFullSolution(2);
//                test_soln.validateDesign();
//                test_soln.print();
//                Architecture arch = test_soln.getArchitecture();
//                if(!arch.checkConsistency()) {
//                    System.out.println( "Inconsistent random architecture: " + arch);
//                    System.exit(0);
//                }
//                ArrayList<Double> results = wrapper.evaluate(arch);
//                System.out.println(results);
//            }
//
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//        }
//
//
//        System.out.println("--> NO INCONSISTENT ARCHITECTURES");
//    }


}
