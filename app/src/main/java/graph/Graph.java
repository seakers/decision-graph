package graph;


import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.chromosome.DesignBuilder;
import graph.decisions.*;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

public class Graph {

    // -----------------------------
    // ----- SINGLETON PATTERN -----
    // -----------------------------
    // - Idea is to use Builder class to build static instance

    private static Graph instance = new Graph();

    public static Graph getInstance() { return instance; }

    public boolean is_built = false;

    // ---------------------
    // ----- VARIABLES -----
    // ---------------------

    private String                     problem;
    private String                     formulation;
    private DatabaseClient             client;
    private ArrayList<Record>          topologicalNodes;
    private HashMap<String, Decision>  decisions;
    private Decision                   root;
    private JsonObject                 adg_specs;



    public JsonObject inputs;
    public JsonArray  designs;


//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|



    public static class Builder {

        private String                    problem;
        private String                    formulation;
        private DatabaseClient            client;
        private Decision                  root;
        private HashMap<String, Decision> decisions;
        private ArrayList<Record>         depthFirstNodes;
        private ArrayList<Record>         topologicalNodes;
        private JsonObject adg_specs;

        private JsonObject inputs;
        private JsonArray designs;


        public Builder(String formulation, String problem, JsonObject adg_specs) {
            this.adg_specs = adg_specs;
            this.problem = problem;
            this.formulation = formulation;
            this.topologicalNodes  = new ArrayList<>();
            this.decisions         = new HashMap<>();
            this.inputs = new JsonObject();

        }

        public Builder buildDatabaseClient(String uri, String user, String password, boolean reset_nodes, boolean reset_graphs){

            this.client = new DatabaseClient.Builder(uri)
                                .setCredentials(user, password)
                                .setFormulation(this.formulation)
                                .setProblem(this.problem)
                                .build();
            if(reset_nodes){
                this.client.obliterateNodes();
            }
            if(reset_graphs){
                this.client.obliterateGraphs();
            }
            return this;
        }

        public Builder indexGraph(){

            // --> TODO: Add other problems
            // this.client.indexFormulation();
            this.client.indexCameoFormulation(this.adg_specs);

            // --> Finally, set problem info (inputs / designs) and return
            return this.setProblemInfo();
        }

        private Builder setProblemInfo(){
            JsonObject problem_info = this.client.getRootProblemInfo();
            this.inputs = problem_info.getAsJsonObject("inputs").deepCopy();
            int[] uid = {0};
            this.initInputs(this.inputs, uid);

            String debug_path = Paths.get(Files.debug_dir, "problem", "inputs.json").toString();
            Files.writeDebugFile(debug_path, this.inputs);

            this.designs = problem_info.getAsJsonArray("designs").deepCopy();
            return this;
        }

        private void initInputs(JsonElement inputs, int[] uid){
            if(inputs.isJsonArray()){
                JsonArray json_dependency_ary = inputs.getAsJsonArray();
                for(JsonElement element: json_dependency_ary){
                    this.initInputs(element, uid);
                }
            }
            else if(inputs.isJsonObject()){
                JsonObject input_obj = inputs.getAsJsonObject();

                // --> Add desired property to JsonObject
                // input_obj.addProperty("uid", uid[0]);
                input_obj.addProperty("uid", Integer.toString(uid[0]));
                uid[0]++;

                for(String key: input_obj.keySet()){
                    JsonElement value = input_obj.get(key);
                    this.initInputs(value, uid);
                }
            }
        }




        public Builder buildTopologicalOrdering(){

            // --> 1. Define node and edge labels
            String node_labels       = "['Decision', 'Root']";
            String dependency_labels = "['DEPENDENCY']";

            // --> 2. Build depth first ordering
            this.client.buildGDSGraph(node_labels, dependency_labels);
            this.depthFirstNodes = this.client.genericTraversal("dfs");

            // --> 3. Build topological ordering
            this.topologicalNodes = this.client.buildTopologicalOrdering(this.depthFirstNodes);
            System.out.println("\n----- TOPOLOGICAL ORDER -----");
            for(Record node: this.topologicalNodes){
                System.out.println(node);
            }
            System.out.println("-----------------------------\n");
            return this;
        }



        private void projectNode(Record node){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            if(node_name.equalsIgnoreCase("Root") && node_type.equalsIgnoreCase("Root")){
                this.root = new Root.Builder(node)
                        .setDatabaseClient(this.client)
                        .setDebugDir("root")
                        .setChildren()
                        .build();
                this.decisions.put(node_name, this.root);
            }
            else{
                Decision selection = null;
                if(node_type.equals("Assigning")){
                    selection = new Assigning.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("assigning")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                else if(node_type.equals("DownSelecting")){
                    selection = new DownSelecting.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("downselecting")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                else if(node_type.equals("StandardForm")){
                    selection = new StandardForm.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("standardform")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                else if(node_type.equals("Partitioning")){
                    selection = new Partitioning.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("partitioning")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                this.decisions.put(node_name, selection);
            }
        }

        public Builder projectGraph(){
            for(Record node: this.topologicalNodes){
                this.projectNode(node);
            }

            this.printDecisions();
            return this;
        }

        private void printDecisions(){
            System.out.println("\n----- ALL DECISIONS -----");
            for(String key: this.decisions.keySet()){
                System.out.println(key + ": " + this.decisions.get(key));
            }
            System.out.println("-------------------------\n");
        }

        public Graph build(){
            Graph build             = new Graph();
            build.client            = this.client;
            build.adg_specs         = this.adg_specs;
            build.topologicalNodes  = this.topologicalNodes;
            build.decisions         = this.decisions;
            build.root              = this.root;
            build.inputs            = this.inputs;
            build.designs           = this.designs;
            build.problem           = problem;
            build.formulation       = formulation;
            build.is_built = true;

            // --> Set graph for each decision node
            for(Decision node: this.decisions.values()){
                node.setGraph(build);
            }

            // --> Finally return graph object
            Graph.instance = build;
            return build;
        }
    }


//  _______          _    _
// |__   __|        | |  (_)
//    | |  ___  ___ | |_  _  _ __    __ _
//    | | / _ \/ __|| __|| || '_ \  / _` |
//    | ||  __/\__ \| |_ | || | | || (_| |
//    |_| \___||___/ \__||_||_| |_| \__, |
//                                   __/ |
//                                  |___/


    public void testDownSelecting(){

    }
    public void testPartitioning(){

    }
    public void testPermuting(){

    }
    public void testAssigning(){

    }
    public void testStandardForm(){

    }
    public void testConnecting(){

    }


//     _____         _  _         __  _____             _               _____              _
//    |_   _|       (_)| |       / / |_   _|           | |             |  __ \            (_)
//      | |   _ __   _ | |_     / /    | |   _ __    __| |  ___ __  __ | |  | |  ___  ___  _   __ _  _ __
//      | |  | '_ \ | || __|   / /     | |  | '_ \  / _` | / _ \\ \/ / | |  | | / _ \/ __|| | / _` || '_ \
//     _| |_ | | | || || |_   / /     _| |_ | | | || (_| ||  __/ >  <  | |__| ||  __/\__ \| || (_| || | | |
//    |_____||_| |_||_| \__| /_/     |_____||_| |_| \__,_| \___|/_/\_\ |_____/  \___||___/|_| \__, ||_| |_|
//                                                                                             __/ |
//                                                                                            |___/
// ----- This is called before any new design is created for methods: crossover / random


    public int indexDesignBuilder(){

        // --> 1. Make sure the design builder is not null
        if(DesignBuilder.object == null){
            System.out.println("--> ERROR: Cant index design because design_builder is null !!!");
            System.exit(0);
        }

        // --> 2. Save design
        // String debug_path = Paths.get(Files.debug_dir, "designs", "DESIGN-"+this.designs.size()+".json").toString();
        // Files.writeDebugFile(debug_path, DesignBuilder.object);
        this.designs.add(DesignBuilder.object.deepCopy());

        // --> 3. Reset design builder
        DesignBuilder.reset();

        // --> 4. Return the index of the newly created design
        return (this.designs.size()-1);
    }



//     _____                    _                     _____              _
//    |  __ \                  | |                   |  __ \            (_)
//    | |__) | __ _  _ __    __| |  ___   _ __ ___   | |  | |  ___  ___  _   __ _  _ __
//    |  _  / / _` || '_ \  / _` | / _ \ | '_ ` _ \  | |  | | / _ \/ __|| | / _` || '_ \
//    | | \ \| (_| || | | || (_| || (_) || | | | | | | |__| ||  __/\__ \| || (_| || | | |
//    |_|  \_\\__,_||_| |_| \__,_| \___/ |_| |_| |_| |_____/  \___||___/|_| \__, ||_| |_|
//                                                                           __/ |
//                                                                          |___/

    public int generateRandomDesign() throws Exception{

        // --> 1. Initialize the design builder
        DesignBuilder.reset();

        // --> 2. Iterate over nodes in topological order and create random design
        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.generateRandomDesign();
        }

        // --> 3. Index the design builder
        return this.indexDesignBuilder();
    }


//      _____                                                 _____              _
//     / ____|                                               |  __ \            (_)
//    | |      _ __  ___   ___  ___   ___ __   __ ___  _ __  | |  | |  ___  ___  _   __ _  _ __   ___
//    | |     | '__|/ _ \ / __|/ __| / _ \\ \ / // _ \| '__| | |  | | / _ \/ __|| | / _` || '_ \ / __|
//    | |____ | |  | (_) |\__ \\__ \| (_) |\ V /|  __/| |    | |__| ||  __/\__ \| || (_| || | | |\__ \
//     \_____||_|   \___/ |___/|___/ \___/  \_/  \___||_|    |_____/  \___||___/|_| \__, ||_| |_||___/
//                                                                                   __/ |
//                                                                                  |___/

    public int crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        System.out.println("--> CROSSING OVER DESIGNS: " + papa + " " + mama);

        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            current.crossoverDesigns(papa, mama, mutation_probability);
        }


        // --> 3. Index the design builder
        return this.indexDesignBuilder();
    }


//     ______                                             _    _
//    |  ____|                                           | |  (_)
//    | |__    _ __   _   _  _ __ ___    ___  _ __  __ _ | |_  _   ___   _ __
//    |  __|  | '_ \ | | | || '_ ` _ \  / _ \| '__|/ _` || __|| | / _ \ | '_ \
//    | |____ | | | || |_| || | | | | ||  __/| |  | (_| || |_ | || (_) || | | |
//    |______||_| |_| \__,_||_| |_| |_| \___||_|   \__,_| \__||_| \___/ |_| |_|


    public void enumerateDesignSpace(){

    }



//     _____              _                __      __
//    |  __ \            (_)               \ \    / /
//    | |  | |  ___  ___  _   __ _  _ __    \ \  / /__ _  _ __  ___
//    | |  | | / _ \/ __|| | / _` || '_ \    \ \/ // _` || '__|/ __|
//    | |__| ||  __/\__ \| || (_| || | | |    \  /| (_| || |   \__ \
//    |_____/  \___||___/|_| \__, ||_| |_|     \/  \__,_||_|   |___/
//                            __/ |
//                           |___/

    public JsonObject getDesign(int design_idx){

        JsonElement design = this.designs.get(design_idx).deepCopy();
        if(!design.isJsonObject()){
            System.out.println("--> ERROR: Design is not JsonObject type");
        }
        return design.getAsJsonObject();
    }

    public JsonObject getDesignDecision(int design_idx, String decision_name){
        return this.decisions.get(decision_name).decisions.get(design_idx).getAsJsonObject();
    }


    public int countDesignVariables(int design_idx){
        int count = 0;

        for(Record node: this.topologicalNodes){
            String node_name = Graph.getNodeName(node);
            String node_type = Graph.getNodeType(node);

            Decision current = this.decisions.get(node_name);
            count += current.countDesignVariables(design_idx);
        }

        return count;
    }




//     _    _        _
//    | |  | |      | |
//    | |__| |  ___ | | _ __    ___  _ __  ___
//    |  __  | / _ \| || '_ \  / _ \| '__|/ __|
//    | |  | ||  __/| || |_) ||  __/| |   \__ \
//    |_|  |_| \___||_|| .__/  \___||_|   |___/
//                     | |
//                     |_|


    public static String getNodeName(Record node){
        return node.get("names.name").toString().replace("\"", "");
    }
    public static String getNodeName(Record node, String key){
        return node.get(key).toString().replace("\"", "");
    }

    public static String getNodeType(Record node){
        return node.get("names.type").toString().replace("\"", "");
    }
    public static String getNodeType(Record node, String key){
        return node.get(key).toString().replace("\"", "");
    }

    public static String removeQuotes(String obj){
        return obj.replace("\"", "");
    }



}
