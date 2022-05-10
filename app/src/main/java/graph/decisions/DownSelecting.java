package graph.decisions;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import graph.Decision;
import graph.chromosome.BitString;
import graph.chromosome.DesignBuilder;
import org.neo4j.driver.Record;

import java.util.ArrayList;

public class DownSelecting extends Decision {

//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|

    public static class Builder extends Decision.Builder<DownSelecting.Builder>{

        public Builder(Record node){
            super(node);
        }

        public DownSelecting build() { return new DownSelecting(this); }
    }

    protected DownSelecting(DownSelecting.Builder builder){
        super(builder);
    }

//     __  __                       _____                            _                 _
//    |  \/  |                     |  __ \                          | |               (_)
//    | \  / | ___ _ __ __ _  ___  | |  | | ___ _ __   ___ _ __   __| | ___ _ __   ___ _  ___  ___
//    | |\/| |/ _ \ '__/ _` |/ _ \ | |  | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| |/ _ \/ __|
//    | |  | |  __/ | | (_| |  __/ | |__| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |  __/\__ \
//    |_|  |_|\___|_|  \__, |\___| |_____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|_|\___||___/
//                      __/ |                  | |
//                     |___/                   |_|


    private JsonObject mergeDependencies(){

        // ----> There are two types of dependencies
        // ----- Root dependency: keys searched for in problem inputs json object
        // - Decision dependency: keys searched for in constructed architecture json object

        JsonObject dependencies = new JsonObject();


        for(Decision parent: this.parents){
            this.mergeParent(dependencies, parent);
        }

        return dependencies;
    }


    public void mergeParent(JsonObject dependencies, Decision parent){
        boolean is_root = parent.node_type.equalsIgnoreCase("Root");

        // --> 1. Fetch all parent relationships
        ArrayList<String> attributes = new ArrayList<>();
        attributes.add("operates_on");
        JsonArray parent_relationships = this.client.getMultiRelationshipAttributes(parent, this, attributes);

        // --> 2. Add decision dependencies for each parent node edge
        for(JsonElement element: parent_relationships){
            this.mergeParentRelationship(dependencies, element.getAsJsonObject(), is_root, parent);
        }
    }


    public void mergeParentRelationship(JsonObject dependencies, JsonObject relationship, boolean is_root, Decision parent){

        // --> Relationship properties
        String operates_on = relationship.get("operates_on").getAsString();


        /*
            - data_source can be set from two different sources depending on two respective cases
            - Case 1: If the parent node is a root node, pull from the graph input object
            - Case 2: If the parent node is a decision node, pull from the design builder object
         */
        JsonElement data_source;
        String source;
        if(is_root){
            source = "root";
            data_source = this.graph.inputs;
        }
        else{
            source = "decision";
            data_source = DesignBuilder.object;
            // data_source = parent.getLastDecisionRefs();
        }


        // --> VERSION 1: Get dependency references
        JsonObject dependency_refs = new JsonObject();
        DesignBuilder.recursiveJsonSearch(data_source, operates_on, dependency_refs);

        // --> VERSION 2: Get dependency references
//        JsonObject dependency_refs = new JsonObject();
//        JsonArray decision_refs = new JsonArray();
//        this.recursiveJsonShallowSearch(data_source, operates_on, dependency_refs, decision_refs);




        // --> Copy dependency references to decision dependencies
        for(String parent_uid: dependency_refs.keySet()){
            JsonObject dependency_relationship = new JsonObject();
            if(dependencies.has(parent_uid)){
                System.out.println("--> ERROR: duplicate uids in decision dependencies down selecting");
                System.exit(0);
            }

            JsonArray ref_uids = new JsonArray();
            for(JsonElement element: dependency_refs.getAsJsonArray(parent_uid)){
                ref_uids.add(element.getAsJsonObject().get("uid").getAsString());
            }


            if(is_root){
                dependency_relationship.add("ref", dependency_refs.getAsJsonArray(parent_uid).deepCopy());
            }
            else{
                dependency_relationship.add("ref", dependency_refs.getAsJsonArray(parent_uid));
            }
            dependency_relationship.addProperty("source", source);
            dependency_relationship.addProperty("key", operates_on);
            dependency_relationship.add("ref_uids", ref_uids);

            // --> Add dependency relationship to object
            dependencies.add(parent_uid, dependency_relationship);
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

    @Override
    public void generateRandomDesign() throws Exception{

        JsonObject dependencies = this.mergeDependencies();

        this.generateRandomDesign(dependencies);
    }

    @Override
    public void generateRandomDesign(JsonObject dependencies) throws Exception{
        // --> 1. Each uid key in `dependencies` points to a JsonArray to down select upon
        // - Generate random design for each

        for(String uid: dependencies.keySet()){
            JsonObject dependency = dependencies.getAsJsonObject(uid);

            ArrayList<Integer> chromosome = BitString.getRandom(dependency.getAsJsonArray("ref").size());
            if(this.node_options.contains("by_count")){
                chromosome = BitString.getRandomCount(dependency.getAsJsonArray("ref").size());
            }

            dependency.add("chromosome", this.gson.toJsonTree(chromosome).getAsJsonArray());
        }

        // --> 3. Build decision
        this.buildDecision(dependencies);


        // --> 4. Index decision into store
        this.indexDecision(dependencies);
    }



//      _____                                                 _____              _
//     / ____|                                               |  __ \            (_)
//    | |      _ __  ___   ___  ___   ___ __   __ ___  _ __  | |  | |  ___  ___  _   __ _  _ __   ___
//    | |     | '__|/ _ \ / __|/ __| / _ \\ \ / // _ \| '__| | |  | | / _ \/ __|| | / _` || '_ \ / __|
//    | |____ | |  | (_) |\__ \\__ \| (_) |\ V /|  __/| |    | |__| ||  __/\__ \| || (_| || | | |\__ \
//     \_____||_|   \___/ |___/|___/ \___/  \_/  \___||_|    |_____/  \___||___/|_| \__, ||_| |_||___/
//                                                                                   __/ |
//                                                                                  |___/

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability) throws Exception{
        JsonObject dependencies = this.mergeDependencies();

        this.crossoverDesigns(papa, mama, mutation_probability, dependencies);
    }

    @Override
    public void crossoverDesigns(int papa, int mama, double mutation_probability, JsonObject dependencies) throws Exception{

        // --> Papa
        JsonObject papa_decision = this.decisions.get(papa).getAsJsonObject();

        // --> Mama
        JsonObject mama_decision = this.decisions.get(mama).getAsJsonObject();

        // --> Child Dependencies
        JsonObject child_decision = dependencies;

        this.writeCrossoverDebugFile(papa_decision, "papa_decision.json");
        this.writeCrossoverDebugFile(mama_decision, "mama_decision.json");

        /*
            - In the decision, only so much information can be taken from each parent. We
                try to maximize the information that can be taken from each parent

            - The new uid system can be used to determine exactly which elements are assigned to / from
                for each parent decision. The links for each parent are assessed and encoded into the child as
                to maximize information extraction while balancing between parents
         */
        this.extractFeasibleParentInfo(papa_decision, child_decision, "papa_info");
        this.extractFeasibleParentInfo(mama_decision, child_decision, "mama_info");


        /*
            - After extracting all usable information from the parents, crossover the usable info and
                randomly select any bits that don't correspond to parent info
         */
        this.crossoverUsableInfo(child_decision);


        /*
            - Mutate chromosome probability is found true
         */
        this.mutateChromosome(child_decision, mutation_probability);


        /*
            - Build crossover decision
         */
        this.writeCrossoverDebugFile(child_decision, "child_dependencies.json");
        this.buildDecision(child_decision);


        // --> 4. Index decision into store
        this.indexDecision(child_decision);
    }

    public void extractFeasibleParentInfo(JsonObject parent_decision, JsonObject child_decision, String info_key) {

        /*
            For each component decision (denoted with a uid), extract parent info
         */

        for(String child_uid: child_decision.keySet()){
            JsonObject         child_decision_component = child_decision.getAsJsonObject(child_uid);
            ArrayList<String> child_decision_uids       = this.gson.fromJson(child_decision_component.getAsJsonArray("ref_uids"), new TypeToken<ArrayList<String>>(){}.getType());
            ArrayList<Integer> parent_info              = BitString.getEmptyInfo(child_decision_uids.size());
            if(parent_decision.has(child_uid)){
                JsonObject         parent_decision_component  = parent_decision.getAsJsonObject(child_uid);
                ArrayList<String> parent_decision_uids       = this.gson.fromJson(parent_decision_component.getAsJsonArray("ref_uids"), new TypeToken<ArrayList<String>>(){}.getType());
                ArrayList<Integer> parent_decision_chromosome = this.gson.fromJson(parent_decision_component.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
                for(int x = 0; x < parent_info.size(); x++){
                    String child_decision_uid = child_decision_uids.get(x);
                    if(parent_decision_uids.contains(child_decision_uid)){
                        int parent_uid_idx = parent_decision_uids.indexOf(child_decision_uid);
                        parent_info.set(x, parent_decision_chromosome.get(parent_uid_idx));
                    }
                }
            }
            child_decision_component.add(info_key, this.gson.toJsonTree(parent_info).getAsJsonArray());
        }
    }

    public void crossoverUsableInfo(JsonObject child_decision){

        /*
            - For each decision component, crossover mama_info and papa_info to create chromosome
         */

        for(String child_uid: child_decision.keySet()) {
            JsonObject child_decision_component = child_decision.getAsJsonObject(child_uid);
            ArrayList<Integer> papa_info        = this.gson.fromJson(child_decision_component.getAsJsonArray("papa_info"), new TypeToken<ArrayList<Integer>>(){}.getType());
            ArrayList<Integer> mama_info        = this.gson.fromJson(child_decision_component.getAsJsonArray("mama_info"), new TypeToken<ArrayList<Integer>>(){}.getType());
            ArrayList<Integer> chromosome       = BitString.getZeros(papa_info.size());


            for(int x = 0; x < chromosome.size(); x++){
                int papa_bit = papa_info.get(x);
                int mama_bit = mama_info.get(x);
                if(papa_bit != -1 && mama_bit != -1){
                    if(this.rand.nextBoolean()){
                        chromosome.set(x, mama_bit);
                    }
                    else{
                        chromosome.set(x, papa_bit);
                    }
                }
                else if(papa_bit != -1){
                    chromosome.set(x, papa_bit);
                }
                else if(mama_bit != -1){
                    chromosome.set(x, mama_bit);
                }
                else{
                    if(this.rand.nextBoolean()){
                        chromosome.set(x, 0);
                    }
                    else{
                        chromosome.set(x, 1);
                    }
                }
            }
            child_decision_component.add("chromosome", this.gson.toJsonTree(chromosome).getAsJsonArray());
        }
    }


//     __  __         _          _             _____  _
//    |  \/  |       | |        | |           / ____|| |
//    | \  / | _   _ | |_  __ _ | |_  ___    | |     | |__   _ __  ___   _ __ ___    ___   ___   ___   _ __ ___    ___
//    | |\/| || | | || __|/ _` || __|/ _ \   | |     | '_ \ | '__|/ _ \ | '_ ` _ \  / _ \ / __| / _ \ | '_ ` _ \  / _ \
//    | |  | || |_| || |_| (_| || |_|  __/   | |____ | | | || |  | (_) || | | | | || (_) |\__ \| (_) || | | | | ||  __/
//    |_|  |_| \__,_| \__|\__,_| \__|\___|    \_____||_| |_||_|   \___/ |_| |_| |_| \___/ |___/ \___/ |_| |_| |_| \___|

    @Override
    public void mutateChromosome(JsonObject decision, double probability){
        if(Decision.getProbabilityResult(probability)){



//            ArrayList<Integer> chromosome = this.gson.fromJson(decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
//            decision.add("chromosome_bm", this.gson.toJsonTree(chromosome).getAsJsonArray().deepCopy());
//
//            // --> 1. Get a random bit index to flip
//            int rand_idx = this.rand.nextInt(chromosome.size());
//            if(chromosome.get(rand_idx) == 0){
//                chromosome.set(rand_idx, 1);
//            }
//            else if(chromosome.get(rand_idx) == 1){
//                chromosome.set(rand_idx, 0);
//            }
//            else{
//                System.out.println("--> ERROR: chromosome element to mutate not in proper form (assigning)");
//                System.exit(0);
//            }
//
//            decision.add("chromosome", this.gson.toJsonTree(chromosome).getAsJsonArray().deepCopy());
        }
    }






//     ____          _  _      _   _____               _       _
//    |  _ \        (_)| |    | | |  __ \             (_)     (_)
//    | |_) | _   _  _ | |  __| | | |  | |  ___   ___  _  ___  _   ___   _ __
//    |  _ < | | | || || | / _` | | |  | | / _ \ / __|| |/ __|| | / _ \ | '_ \
//    | |_) || |_| || || || (_| | | |__| ||  __/| (__ | |\__ \| || (_) || | | |
//    |____/  \__,_||_||_| \__,_| |_____/  \___| \___||_||___/|_| \___/ |_| |_|


    @Override
    protected void buildDecision(JsonObject decision){

        /*
            - Steps to process each decision component reference
            - 1. If the reference comes from root, copy a new design base key using operates_on as the key
         */

        for(String uid: decision.keySet()){
            JsonObject decision_component = decision.getAsJsonObject(uid);
            JsonArray array_ref = decision_component.getAsJsonArray("ref");
            String operates_on = decision_component.get("key").getAsString().replace("\"", "");
            ArrayList<Integer> chromosome = this.gson.fromJson(decision_component.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());

            // --> Step 1
            if(decision_component.get("source").getAsString().contains("root")){
                if(!DesignBuilder.object.has(operates_on)){
                    DesignBuilder.object.add(operates_on, array_ref);
                }
                else{
                    System.out.println("--> ERROR: overwriting decision data in SF decision");
                    System.exit(0);
                }
            }

            // --> Step 2
            for(int x = chromosome.size()-1; x >= 0; x--){
                if(chromosome.get(x).equals(0)){
                    JsonObject to_remove = array_ref.get(x).getAsJsonObject();
                    // DesignBuilder.findAndRemoveObject(Integer.parseInt(uid), to_remove, DesignBuilder.object);
                    array_ref.remove(x);
                }
            }
        }
    }






}
