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

public class Permuting extends Decision {

//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|


    public static class Builder extends Decision.Builder<Permuting.Builder>{

        public Builder(Record node){
            super(node);
        }

        public Permuting build() { return new Permuting(this); }
    }

    protected Permuting(Permuting.Builder builder) { super(builder); }


//     __  __                       _____                            _                 _
//    |  \/  |                     |  __ \                          | |               (_)
//    | \  / | ___ _ __ __ _  ___  | |  | | ___ _ __   ___ _ __   __| | ___ _ __   ___ _  ___  ___
//    | |\/| |/ _ \ '__/ _` |/ _ \ | |  | |/ _ \ '_ \ / _ \ '_ \ / _` |/ _ \ '_ \ / __| |/ _ \/ __|
//    | |  | |  __/ | | (_| |  __/ | |__| |  __/ |_) |  __/ | | | (_| |  __/ | | | (__| |  __/\__ \
//    |_|  |_|\___|_|  \__, |\___| |_____/ \___| .__/ \___|_| |_|\__,_|\___|_| |_|\___|_|\___||___/
//                      __/ |                  | |
//                     |___/                   |_|


    private JsonObject mergeDependencies() {

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
        DesignBuilder.referenceSearch(data_source, operates_on, dependency_refs, false);




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


//     _____                    _                     _____              _
//    |  __ \                  | |                   |  __ \            (_)
//    | |__) | __ _  _ __    __| |  ___   _ __ ___   | |  | |  ___  ___  _   __ _  _ __
//    |  _  / / _` || '_ \  / _` | / _ \ | '_ ` _ \  | |  | | / _ \/ __|| | / _` || '_ \
//    | | \ \| (_| || | | || (_| || (_) || | | | | | | |__| ||  __/\__ \| || (_| || | | |
//    |_|  \_\\__,_||_| |_| \__,_| \___/ |_| |_| |_| |_____/  \___||___/|_| \__, ||_| |_|
//                                                                           __/ |
//                                                                          |___/












    }





































}
