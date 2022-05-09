package graph.decisions;

import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import graph.Decision;
import graph.Graph;
import graph.chromosome.BitString;
import graph.chromosome.DesignBuilder;
import org.neo4j.driver.Record;

import java.util.ArrayList;
import java.util.Iterator;

public class Assigning extends Decision {



//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder extends Decision.Builder<Assigning.Builder>{

        public Builder(Record node){
            super(node);
        }

        public Assigning build() { return new Assigning(this); }
    }

    protected Assigning(Assigning.Builder builder){
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
        dependencies.add("to", new JsonArray());
        dependencies.add("to_src", new JsonArray());
        dependencies.add("to_uid", new JsonArray());
        dependencies.add("to_keys", new JsonArray());
        dependencies.add("from", new JsonArray());
        dependencies.add("from_src", new JsonArray());
        dependencies.add("from_uid", new JsonArray());
        dependencies.add("from_keys", new JsonArray());


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
        attributes.add("type");
        JsonArray parent_relationships = this.client.getMultiRelationshipAttributes(parent, this, attributes);

        // --> 2. Add decision dependencies for each parent node edge
        for(JsonElement element: parent_relationships){
            this.mergeParentRelationship(dependencies, element.getAsJsonObject(), is_root);
        }
    }

    public void mergeParentRelationship(JsonObject dependencies, JsonObject relationship, boolean is_root){

        // --> Relationship properties
        String operates_on = relationship.get("operates_on").getAsString();
        String direction = relationship.get("type").getAsString();


        /*
            - data_source can be set from two different sources depending on two respective cases
            - Case 1: If the parent node is a root node, pull from the graph input object
            - Case 2: If the parent node is a decision node, pull from the design builder object
         */
        JsonObject data_source;
        String source;
        if(is_root){
            source = "root";
            data_source = this.graph.inputs;
        }
        else{
            source = "decision";
            data_source = DesignBuilder.object;
        }

        // --> Get dependency references
        // --> TODO: Change such that assigning decisions remove the 'from' JsonArrays that are accessed with operates_on
        JsonObject dependency_refs = new JsonObject();
        this.recursiveJsonSearchAssigning(data_source, operates_on, dependency_refs, is_root, direction.equalsIgnoreCase("FROM"));

        // --> Copy dependency elements from references
        JsonArray dependency_elements = new JsonArray();
        for(String key: dependency_refs.keySet()){
            JsonArray ref_ary = dependency_refs.getAsJsonArray(key);
            for(JsonElement assign_obj: ref_ary){
                if(is_root){
                    dependency_elements.add(assign_obj.getAsJsonObject().deepCopy());
                }
                else{
                    dependency_elements.add(assign_obj.getAsJsonObject());
                }
            }
        }


        // --> Add dependency elements to decision dependencies
        if(direction.equalsIgnoreCase("FROM")){
            for(JsonElement dependency_element: dependency_elements){
                dependencies.get("from").getAsJsonArray().add(dependency_element.getAsJsonObject());
                dependencies.get("from_uid").getAsJsonArray().add(dependency_element.getAsJsonObject().get("uid").getAsInt());
                dependencies.get("from_keys").getAsJsonArray().add(operates_on);
                dependencies.get("from_src").getAsJsonArray().add(source);
            }
        }
        else if(direction.equalsIgnoreCase("TO")){
            for(JsonElement dependency_element: dependency_elements){
                dependencies.get("to").getAsJsonArray().add(dependency_element.getAsJsonObject());
                dependencies.get("to_uid").getAsJsonArray().add(dependency_element.getAsJsonObject().get("uid").getAsInt());
                dependencies.get("to_keys").getAsJsonArray().add(operates_on);
                dependencies.get("to_src").getAsJsonArray().add(source);
            }
        }
        else{
            System.out.println("---> PARENT RELATIONSHIP IMPROPERLY SET FOR ASSIGNATION DECISION !!! " + direction);
            System.exit(0);
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

        JsonArray assign_from = dependencies.getAsJsonArray("from");
        JsonArray assign_to   = dependencies.getAsJsonArray("to");


        if(assign_from.size() == 0 || assign_to.size() == 0){
            throw new Exception("Assigning - randomDesign - some dependencies do not exist " + this.gson.toJson(dependencies));
        }

        // --> 2. Generate random bit string (add to dependencies)
        int chromosome_length = assign_from.size() * assign_to.size();
        ArrayList<Integer> chromosome = BitString.getRandom(chromosome_length);
        chromosome = BitString.constraint_minAssignation(chromosome, assign_to.size(), assign_from.size());
        dependencies.add("chromosome", this.gson.toJsonTree(chromosome).getAsJsonArray());

        // --> 3. Build decision
        this.buildDecision(dependencies);

        this.writeRandomDebugFile(dependencies, "decision.json");

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
        JsonObject child_decision = dependencies.deepCopy();


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
        this.writeCrossoverDebugFile(child_decision, "child_dependencies.json");


        /*
            - Build crossover decision
         */

        this.buildDecision(child_decision);

        /*
            - Finally, index the decision
         */
        this.indexDecision(child_decision);
    }

    public void extractFeasibleParentInfo(JsonObject parent_decision, JsonObject child_decision, String info_key){

        ArrayList<Integer> parent_chromosome = this.gson.fromJson(parent_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
        JsonArray parent_to_uids   = parent_decision.getAsJsonArray("to_uid");
        JsonArray parent_from_uids = parent_decision.getAsJsonArray("from_uid");


        JsonArray child_to_uids   = child_decision.getAsJsonArray("to_uid");
        JsonArray child_from_uids = child_decision.getAsJsonArray("from_uid");

        // --> 1. Create empty info parent_info array
        ArrayList<Integer> parent_info = BitString.getEmptyInfo(child_to_uids.size() * child_from_uids.size());

        // --> 2. Iterate over child uids and fill un parent_info
        int counter = 0;
        for(JsonElement child_to_uid: child_to_uids){
            for(JsonElement child_from_uid: child_from_uids){
                if(parent_to_uids.contains(child_to_uid) && parent_from_uids.contains(child_from_uid)){
                    int parent_to_idx   = this.find_element_index(parent_to_uids, child_to_uid);
                    int parent_from_idx = this.find_element_index(parent_from_uids, child_from_uid);
                    int parent_chromosome_idx = (parent_to_idx * parent_from_uids.size()) + parent_from_idx;
                    parent_info.set(counter, parent_chromosome.get(parent_chromosome_idx));
                }
                counter++;
            }
        }

        // --> 3. Set parent_info chromosome in child_dependencies using info_key
        // child_dependencies.addProperty(info_key+"_str", BitString.toString(parent_info));
        child_decision.add(info_key, this.gson.toJsonTree(parent_info).getAsJsonArray());
    }

    private int find_element_index(JsonArray elements, JsonElement to_find){
        for(int x = 0; x < elements.size(); x++){
            JsonElement element = elements.get(x);
            if(element.equals(to_find)){
                return x;
            }
        }
        return -1;
    }

    public void crossoverUsableInfo(JsonObject child_decision){

        ArrayList<Integer> papa_info = this.gson.fromJson(child_decision.getAsJsonArray("papa_info"), new TypeToken<ArrayList<Integer>>(){}.getType());
        ArrayList<Integer> mama_info = this.gson.fromJson(child_decision.getAsJsonArray("mama_info"), new TypeToken<ArrayList<Integer>>(){}.getType());

        if(mama_info.size() != papa_info.size()){
            System.out.println("--> ERROR: papa_info, mama_info, and chromosome must be the same size (Assigning Crossover)");
        }

        ArrayList<Integer> chromosome = BitString.getEmptyInfo(papa_info.size());

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
        child_decision.add("chromosome", this.gson.toJsonTree(chromosome).getAsJsonArray());
    }



//     ____          _  _      _   _____               _       _
//    |  _ \        (_)| |    | | |  __ \             (_)     (_)
//    | |_) | _   _  _ | |  __| | | |  | |  ___   ___  _  ___  _   ___   _ __
//    |  _ < | | | || || | / _` | | |  | | / _ \ / __|| |/ __|| | / _ \ | '_ \
//    | |_) || |_| || || || (_| | | |__| ||  __/| (__ | |\__ \| || (_) || | | |
//    |____/  \__,_||_||_| \__,_| |_____/  \___| \___||_||___/|_| \___/ |_| |_|


    @Override
    protected void buildDecision(JsonObject decision){

        ArrayList<Integer> chromosome = this.gson.fromJson(decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());

        JsonArray assign_to          = decision.getAsJsonArray("to");
        JsonArray assign_to_src      = decision.getAsJsonArray("to_src");
        JsonArray assign_to_keys     = decision.getAsJsonArray("to_keys");


        JsonArray assign_from        = decision.getAsJsonArray("from");
        JsonArray assign_from_src    = decision.getAsJsonArray("from_src");
        JsonArray assign_from_keys   = decision.getAsJsonArray("from_keys");


        ArrayList<Boolean> assign_to_used   = BitString.assigningUsedToValues(chromosome, assign_to.size(), assign_from.size());
        ArrayList<Boolean> assign_from_used = BitString.assigningUsedFromValues(chromosome, assign_to.size(), assign_from.size());


        // --> 1. For each assign_from element
        /*
            - Case 1: assign_from element taken from 'inputs'
            -   Simply copy the assign_from object into its corresponding assign_to object

            - Case 2: assign_from element is taken from 'design_builder'
            -  Copy the assign_from object into the corresponding assign_to objects
            -  Delete the previous assign_from object from the 'design_builder' (~ not implementing this for now)
         */

        // --> 2. For each assign_to element
        /*
            - Case 1: assign_to element taken from 'inputs'
            -   In the design_builder obj, check to see if the corresponding assign_to_key exists on the bottom level
            -   If it does exist, index the assign_to element into the design_builder JsonArray that assign_to_key corresponds to

            - Case 2: assign_to element is taken from 'design_builder'
            -  Do nothing... because this assign_to element is a reference in 'design_builder', the previous step handles everything
         */

        int counter = 0;
        for(int x = 0; x < assign_to.size(); x++){

            JsonObject assign_to_obj = assign_to.get(x).getAsJsonObject();
            String     to_source     = assign_to_src.get(x).toString().replace("\"", "");
            String     to_key        = assign_to_keys.get(x).toString().replace("\"", "");
            boolean    to_used       = assign_to_used.get(x);

            if(to_source.equalsIgnoreCase("root") && to_used){
                if(!DesignBuilder.object.has(to_key)){
                    DesignBuilder.object.add(to_key, new JsonArray());
                }
                DesignBuilder.object.get(to_key).getAsJsonArray().add(assign_to_obj);
            }

            for(int y = 0; y < assign_from.size(); y++){

                JsonObject assign_from_obj = assign_from.get(y).getAsJsonObject();
                String     from_source     = assign_from_src.get(y).toString().replace("\"", "");
                String     from_key        = assign_from_keys.get(y).toString().replace("\"", "");

                Integer bit = chromosome.get(counter);
                if(bit == 1){
                    if(!assign_to_obj.has(from_key)){
                        assign_to_obj.add(from_key, new JsonArray());
                    }
                    assign_to_obj.get(from_key).getAsJsonArray().add(assign_from_obj.deepCopy());
                }
                counter++;
            }
        }
    }





}
