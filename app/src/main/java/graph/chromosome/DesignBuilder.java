package graph.chromosome;

import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Paths;


/*
    Data Structure Rules (JsonObject ~ Gson)

    1. Base JsonObject
    - Contains only key:pair values where:
        - key: String
        - Value: JsonArray (containing only type JsonObject)

    2. Nested JsonObject
    - Contains two String fields: name, id
    - Every other field points to a JsonArray (containing only type JsonObject)

 */



public class DesignBuilder {

    public static JsonObject object = null;

    public static int snapshot_count = 0;
    public static void takeSnapshot(){
        String file_name = Paths.get(Files.debug_dir, "designs", "stages", "SNAPSHOT-" + DesignBuilder.snapshot_count + ".json").toString();
        Files.writeDebugFile(file_name, DesignBuilder.object.deepCopy());
        DesignBuilder.snapshot_count++;
    }

    public static void reset(){
        DesignBuilder.object = new JsonObject();
        DesignBuilder.snapshot_count = 0;
    }






    public static void findAndRemoveObject(int parent_uid, JsonObject to_remove, JsonElement data_source){

        // --> Condition
        if(parent_uid == 0){
            if(!DesignBuilder.removeObjectFromParent(DesignBuilder.object, to_remove)){
                System.out.println("--> ERROR: base DesignBuilder object list item not deleted");
            }
            return;
        }


        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder.findAndRemoveObject(parent_uid, to_remove, element);
            }
        }
        else if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            // --> Try to get element UID
            int uid = -1;
            if(json_dependency_obj.has("uid")){
                uid = json_dependency_obj.get("uid").getAsInt();
            }

            // --> If uid patches, we have the parent object
            if(parent_uid == uid){
                JsonObject parent_obj = json_dependency_obj;
                DesignBuilder.removeObjectFromParent(parent_obj, to_remove);
            }
            else{
                for(String key: json_dependency_obj.keySet()){
                    JsonElement value = json_dependency_obj.get(key);
                    DesignBuilder.findAndRemoveObject(parent_uid, to_remove, value);
                }
            }
        }
    }

    private static boolean removeObjectFromParent(JsonObject parent_obj, JsonObject to_remove){
        /*
            - Search through the json object until item is found... then delete it
         */
        boolean found = false;
        for(String key: parent_obj.keySet()){
            if(parent_obj.get(key).isJsonArray()){
                JsonArray parent_field_ary = parent_obj.get(key).getAsJsonArray();
                if(parent_field_ary.contains(to_remove)){
                    parent_field_ary.remove(to_remove);
                    found = true;
                    // System.out.println("--> OBJECT REMOVED");
                }
            }
        }
        return found;
    }



//     _____         __                                        _____                          _
//    |  __ \       / _|                                      / ____|                        | |
//    | |__) | ___ | |_  ___  _ __  ___  _ __    ___  ___    | (___    ___   __ _  _ __  ___ | |__
//    |  _  / / _ \|  _|/ _ \| '__|/ _ \| '_ \  / __|/ _ \    \___ \  / _ \ / _` || '__|/ __|| '_ \
//    | | \ \|  __/| | |  __/| |  |  __/| | | || (__|  __/    ____) ||  __/| (_| || |  | (__ | | | |
//    |_|  \_\\___||_|  \___||_|   \___||_| |_| \___|\___|   |_____/  \___| \__,_||_|   \___||_| |_|



    /*
        If the proper key is found, does not recur further into JsonObject
     */

    public static void referenceSearch(JsonElement data_source, String operates_on, JsonObject dependencies, boolean delete_ref){
        DesignBuilder._referenceSearch(data_source, operates_on, dependencies, delete_ref);
    }
    private static void _referenceSearch(JsonElement data_source, String operates_on, JsonObject dependencies, boolean delete_ref){

        // --> If data_source is JsonArray: recursively call search for each JsonObject in array
        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder._referenceSearch(element, operates_on, dependencies, delete_ref);
            }
        }

        // --> If data_source is JsonObject: check to see if it contains the search key
        if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            if(!json_dependency_obj.keySet().contains(operates_on)){
                // --> 1. Recursively search object
                for(String key: json_dependency_obj.keySet()){
                    DesignBuilder._referenceSearch(json_dependency_obj.get(key), operates_on, dependencies, delete_ref);
                }
            }
            else {

                // --> 1. Get JsonObject uid
                String uid = "null";
                if(json_dependency_obj.has("uid")){
                    uid = json_dependency_obj.get("uid").getAsString();
                }

                // --> 2. Get reference object
                JsonArray reference_object;
                if(delete_ref){
                    reference_object = json_dependency_obj.remove(operates_on).getAsJsonArray();
                }
                else{
                    reference_object = json_dependency_obj.getAsJsonArray(operates_on);
                }

                // --> 3. Add reference object to dependency object
                dependencies.add(uid, reference_object);
            }
        }
    }



    public static JsonArray parentSearch(JsonElement data_source, String operates_on){
        JsonArray dependencies = new JsonArray();
        DesignBuilder._parentSearch(data_source, operates_on, dependencies);
        return dependencies;
    }

    private static void _parentSearch(JsonElement data_source, String operates_on, JsonArray dependencies){

        // --> If data_source is JsonArray: recursively call search for each JsonObject in array
        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder._parentSearch(element, operates_on, dependencies);
            }
        }

        // --> If data_source is JsonObject: check to see if it contains the search key
        if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            if(json_dependency_obj.keySet().contains(operates_on)){
                dependencies.add(json_dependency_obj);

            }
            else {
                for(String key: json_dependency_obj.keySet()){
                    DesignBuilder._parentSearch(json_dependency_obj.get(key), operates_on, dependencies);
                }
            }
        }
    }







}
