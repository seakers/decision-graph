package graph;

//   _____            _     _
//  |  __ \          (_)   (_)
//  | |  | | ___  ___ _ ___ _  ___  _ __
//  | |  | |/ _ \/ __| / __| |/ _ \| '_ \
//  | |__| |  __/ (__| \__ \ | (_) | | | |
//  |_____/ \___|\___|_|___/_|\___/|_| |_|


import app.Files;
import com.google.gson.*;
import graph.chromosome.DesignBuilder;
import graph.neo4j.DatabaseClient;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class Decision {



//     __      __        _       _     _
//     \ \    / /       (_)     | |   | |
//      \ \  / /_ _ _ __ _  __ _| |__ | | ___  ___
//       \ \/ / _` | '__| |/ _` | '_ \| |/ _ \/ __|
//        \  / (_| | |  | | (_| | |_) | |  __/\__ \
//         \/ \__,_|_|  |_|\__,_|_.__/|_|\___||___/


    public String                       node_name;
    public String                       node_type;
    protected JsonArray                 decisions;



    protected DatabaseClient client;
    protected Graph          graph;
    protected Record node;
    protected ArrayList<Decision> parents;
    protected ArrayList<Record>         children;
    protected HashMap<String, Decision> decision_nodes;
    protected Gson gson;
    protected Random rand;
    protected String debug_dir;


//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|

    public static class Builder<T extends Builder<T>>{

        protected DatabaseClient            client;
        protected String                    node_name;
        protected String                    node_type;
        protected Record                    node;
        protected ArrayList<Decision>       parents;
        protected ArrayList<Record>         children;
        protected HashMap<String, Decision> decision_nodes;
        protected Gson                      gson;
        protected JsonArray                 decisions;
        protected JsonArray                 parameters;
        protected Random                    rand;

        protected String                    debug_dir;

        public Builder(Record node){
            this.node        = node;
            this.node_name   = node.get("names.name").toString().replace("\"", "");
            this.node_type   = node.get("names.type").toString().replace("\"", "");
            this.parents        = new ArrayList<>();
            this.decision_nodes = new HashMap<>();
            this.gson           = new GsonBuilder().setPrettyPrinting().create();
            this.decisions      = new JsonArray();
            this.parameters     = new JsonArray();
            this.rand           = new Random();
            this.debug_dir      = null;
        }

        public Builder setDatabaseClient(DatabaseClient client){
            this.client = client;
            return this;
        }

        public Builder setDebugDir(String debug_dir){
            this.debug_dir = debug_dir;
            return this;
        }


        public Builder setParents(HashMap<String, Decision> decisions){
            this.decision_nodes = decisions;
            ArrayList<Record> parents = this.client.getNodeParents(this.node_name);
            for(Record parent: parents){
                String parent_name = parent.get("m.name").toString().replace("\"", "");
                if(this.decision_nodes.containsKey(parent_name) && !this.parents.contains(this.decision_nodes.get(parent_name))){
                    this.parents.add(this.decision_nodes.get(parent_name));
                }
            }
            return this;
        }

        public Builder setChildren(){
            this.children = this.client.getNodeChildren(this.node_name);
            return this;
        }

        public Builder setDecisions(){
            this.decisions = this.client.getNodeDecisions(this.node_name);
            return this;
        }

        public Decision build() { return new Decision(this);}
    }

    protected Decision(Builder<?> builder) {
        this.graph          = null;
        this.debug_dir      = builder.debug_dir;
        this.client         = builder.client;
        this.node           = builder.node;
        this.node_name      = builder.node_name;
        this.node_type      = builder.node_type;
        this.parents        = builder.parents;
        this.decision_nodes = builder.decision_nodes;
        this.children       = builder.children;
        this.gson           = builder.gson;
        this.decisions      = builder.decisions;
        this.rand           = builder.rand;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }


//    _____         _
//    |  __ \       | |
//    | |  | |  ___ | |__   _   _   __ _
//    | |  | | / _ \| '_ \ | | | | / _` |
//    | |__| ||  __/| |_) || |_| || (_| |
//    |_____/  \___||_.__/  \__,_| \__, |
//                                  __/ |
//                                 |___/


    protected void writeRandomDebugFile(JsonElement to_write, String file_name){
        this.writeDebugFile(to_write, "random/" + file_name);
    }
    protected void writeCrossoverDebugFile(JsonElement to_write, String file_name){
        this.writeDebugFile(to_write, "crossover/" + file_name);
    }
    protected void writeEnumerationDebugFile(JsonElement to_write, String file_name){
        this.writeDebugFile(to_write, "enumeration/" + file_name);
    }

    protected void writeDebugFile(JsonElement to_write, String file_name){
        if(to_write.isJsonObject()){
            Files.writeDebugFile(this.debug_dir+file_name, to_write.getAsJsonObject());
        }
        else if(to_write.isJsonArray()){
            Files.writeDebugFile(this.debug_dir+file_name, to_write.getAsJsonArray());
        }
        else{
            System.out.println("--> COULD NOT WRITE DECISION DEBUG FILE");
        }
    }



//     _____             _                 _____               _       _
//    |_   _|           | |               |  __ \             (_)     (_)
//      | |   _ __    __| |  ___ __  __   | |  | |  ___   ___  _  ___  _   ___   _ __
//      | |  | '_ \  / _` | / _ \\ \/ /   | |  | | / _ \ / __|| |/ __|| | / _ \ | '_ \
//     _| |_ | | | || (_| ||  __/ >  <    | |__| ||  __/| (__ | |\__ \| || (_) || | | |
//    |_____||_| |_| \__,_| \___|/_/\_\   |_____/  \___| \___||_||___/|_| \___/ |_| |_|
//


    protected void indexDecision(JsonObject dependencies){

        // --> The dependencies object should contain the architecture decision
        JsonObject decision = dependencies.deepCopy();

        this.decisions.add(decision);

        // --> Take snapshot of design decision
        DesignBuilder.takeSnapshot();
    }



//     ____          _  _      _   _____               _       _
//    |  _ \        (_)| |    | | |  __ \             (_)     (_)
//    | |_) | _   _  _ | |  __| | | |  | |  ___   ___  _  ___  _   ___   _ __
//    |  _ < | | | || || | / _` | | |  | | / _ \ / __|| |/ __|| | / _ \ | '_ \
//    | |_) || |_| || || || (_| | | |__| ||  __/| (__ | |\__ \| || (_) || | | |
//    |____/  \__,_||_||_| \__,_| |_____/  \___| \___||_||___/|_| \___/ |_| |_|


    protected void buildDecision(JsonObject decision){
        /*
            - The purpose of this function is to take a decision object and mutate the DesignBuilder
                object to reflect the decision

            - Each decision class requires a well defined operation it must execute on the DesignBuilder
                object. Each one of these operations must consider the two data sources the decision inputs
                come from (Input JsonObject, DesignBuilder JsonObject)
         */
    }




//     ______  _             _   _____                                 _
//    |  ____|(_)           | | |  __ \                               | |
//    | |__    _  _ __    __| | | |  | |  ___  _ __    ___  _ __    __| |  ___  _ __    ___  _   _
//    |  __|  | || '_ \  / _` | | |  | | / _ \| '_ \  / _ \| '_ \  / _` | / _ \| '_ \  / __|| | | |
//    | |     | || | | || (_| | | |__| ||  __/| |_) ||  __/| | | || (_| ||  __/| | | || (__ | |_| |
//    |_|     |_||_| |_| \__,_| |_____/  \___|| .__/  \___||_| |_| \__,_| \___||_| |_| \___| \__, |
//                                            | |                                             __/ |
//                                            |_|                                            |___/


    protected void recursiveJsonSearch(JsonElement data_source, String operates_on, JsonObject dependencies){
        this.recursiveJsonRefSearch(data_source, operates_on, dependencies);
    }

    protected void recursiveJsonRefSearch(JsonElement data_source, String operates_on, JsonObject dependencies){

        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                this.recursiveJsonRefSearch(element, operates_on, dependencies);
            }
        }
        else if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            // --> Try to get element UID
            String uid = "null";
            if(json_dependency_obj.has("uid")){
                uid = json_dependency_obj.get("uid").getAsString();
            }

            for(String key: json_dependency_obj.keySet()){
                JsonElement value = json_dependency_obj.get(key);
                if(key.equalsIgnoreCase(operates_on) && value.isJsonArray()){

                    // --> Check if duplicate UID is present
                    if(dependencies.keySet().contains(uid)){
                        System.out.println("--> ERROR: duplicate UIDs when searching for operates on in recursive search");
                        System.exit(0);
                    }
                    dependencies.add(uid, value.getAsJsonArray());
                }
                else{
                    this.recursiveJsonRefSearch(value, operates_on, dependencies);
                }
            }
        }

    }




    protected void recursiveJsonSearchAssigning(JsonElement data_source, String operates_on, JsonObject dependencies, boolean is_root, boolean delete_ref){
        this.recursiveJsonRefSearchAssigning(data_source, operates_on, dependencies, is_root, delete_ref);
    }

    protected void recursiveJsonRefSearchAssigning(JsonElement data_source, String operates_on, JsonObject dependencies, boolean is_root, boolean delete_ref){

        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                this.recursiveJsonRefSearchAssigning(element, operates_on, dependencies, is_root, delete_ref);
            }
        }
        else if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            // --> Try to get element UID
            String uid = "null";
            if(json_dependency_obj.has("uid")){
                uid = json_dependency_obj.get("uid").getAsString();
            }

            for(String key: json_dependency_obj.keySet()){
                JsonElement value = json_dependency_obj.get(key);
                if(key.equalsIgnoreCase(operates_on) && value.isJsonArray()){

                    // --> Check if duplicate UID is present
                    if(dependencies.keySet().contains(uid)){
                        System.out.println("--> ERROR: duplicate UIDs when searching for operates on in recursive search");
                        System.exit(0);
                    }
                    if(!is_root && delete_ref){
                        dependencies.add(uid, value.getAsJsonArray().deepCopy());
                        json_dependency_obj.remove(key);
                    }
                    else{
                        dependencies.add(uid, value.getAsJsonArray());
                    }
                }
                else{
                    this.recursiveJsonRefSearchAssigning(value, operates_on, dependencies, is_root, delete_ref);
                }
            }
        }

    }


//     _____                    _                     _____              _
//    |  __ \                  | |                   |  __ \            (_)
//    | |__) | __ _  _ __    __| |  ___   _ __ ___   | |  | |  ___  ___  _   __ _  _ __
//    |  _  / / _` || '_ \  / _` | / _ \ | '_ ` _ \  | |  | | / _ \/ __|| | / _` || '_ \
//    | | \ \| (_| || | | || (_| || (_) || | | | | | | |__| ||  __/\__ \| || (_| || | | |
//    |_|  \_\\__,_||_| |_| \__,_| \___/ |_| |_| |_| |_____/  \___||___/|_| \__, ||_| |_|
//                                                                           __/ |
//                                                                          |___/

    public void generateRandomDesign() throws Exception{

    }

    public void generateRandomDesign(JsonObject dependencies) throws Exception{

    }

//      _____                                                 _____              _
//     / ____|                                               |  __ \            (_)
//    | |      _ __  ___   ___  ___   ___ __   __ ___  _ __  | |  | |  ___  ___  _   __ _  _ __   ___
//    | |     | '__|/ _ \ / __|/ __| / _ \\ \ / // _ \| '__| | |  | | / _ \/ __|| | / _` || '_ \ / __|
//    | |____ | |  | (_) |\__ \\__ \| (_) |\ V /|  __/| |    | |__| ||  __/\__ \| || (_| || | | |\__ \
//     \_____||_|   \___/ |___/|___/ \___/  \_/  \___||_|    |_____/  \___||___/|_| \__, ||_| |_||___/
//                                                                                   __/ |
//                                                                                  |___/

    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{

    }

    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonObject dependencies) throws Exception{

    }


//     ______                                             _    _
//    |  ____|                                           | |  (_)
//    | |__    _ __   _   _  _ __ ___    ___  _ __  __ _ | |_  _   ___   _ __
//    |  __|  | '_ \ | | | || '_ ` _ \  / _ \| '__|/ _` || __|| | / _ \ | '_ \
//    | |____ | | | || |_| || | | | | ||  __/| |  | (_| || |_ | || (_) || | | |
//    |______||_| |_| \__,_||_| |_| |_| \___||_|   \__,_| \__||_| \___/ |_| |_|


    public void enumerateDesignSpace(){

    }


//     _    _  _    _  _
//    | |  | || |  (_)| |
//    | |  | || |_  _ | |
//    | |  | || __|| || |
//    | |__| || |_ | || |
//     \____/  \__||_||_|

}
