package graph;


import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import graph.chromosome.DesignBuilder;
import graph.decisions.Assigning;
import graph.decisions.DownSelecting;
import graph.decisions.Root;
import graph.decisions.StandardForm;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {


    private String                    problem;
    private String                    formulation;
    private DatabaseClient            client;
    private ArrayList<Record>         topologicalNodes;
    private HashMap<String, Decision> decisions;
    private Decision                  root;



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
        private JsonObject inputs;
        private JsonArray designs;

        public Builder(DatabaseClient client, String formulation, String problem, boolean reset_nodes, boolean reset_graphs) {
            this.client  = client;
            this.problem = problem;
            this.formulation = formulation;
            this.topologicalNodes  = new ArrayList<>();
            this.decisions         = new HashMap<>();
            this.inputs = new JsonObject();
            if(reset_nodes){
                this.client.obliterateNodes();
            }
            if(reset_graphs){
                this.client.obliterateGraphs();
            }
        }

        public Builder indexGraph(){

            // --> TODO: Add other problems
            if(this.formulation.equals("TDRS")){
                // this.client.indexTDRS();
                this.client.indexFormulation();
            }

            // --> Finally, set problem info (inputs / designs) and return
            return this.setProblemInfo();
        }

        private Builder setProblemInfo(){
            JsonObject problem_info = this.client.getRootProblemInfo();
            this.inputs = problem_info.getAsJsonObject("inputs").deepCopy();
            int[] uid = {0};
            this.initInputs(this.inputs, uid);
            Files.writeDebugFile("/decisions/debug/problem/tdrs.json", this.inputs);
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
                input_obj.addProperty("uid", uid[0]);
                uid[0]++;
                input_obj.addProperty("active", Boolean.TRUE);

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
                        .setDebugDir("/decisions/debug/root/")
                        .setChildren()
                        .build();
                this.decisions.put(node_name, this.root);
            }
            else{
                Decision selection = null;
                if(node_type.equals("Assigning")){
                    selection = new Assigning.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("/decisions/debug/assigning/")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                else if(node_type.equals("DownSelecting")){
                    selection = new DownSelecting.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("/decisions/debug/downselecting/")
                            .setParents(this.decisions)
                            .setChildren()
                            .setDecisions()
                            .build();
                }
                else if(node_type.equals("StandardForm")){
                    selection = new StandardForm.Builder(node)
                            .setDatabaseClient(this.client)
                            .setDebugDir("/decisions/debug/standardform/")
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
            build.topologicalNodes  = this.topologicalNodes;
            build.decisions         = this.decisions;
            build.root              = this.root;
            build.inputs            = this.inputs;
            build.designs           = this.designs;
            build.problem           = problem;
            build.formulation       = formulation;

            // --> Set graph for each decision node
            for(Decision node: this.decisions.values()){
                node.setGraph(build);
            }

            // --> Finally return graph object
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
        Files.writeDebugFile("/decisions/debug/designs/DESIGN-"+this.designs.size()+".json", DesignBuilder.object);
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
