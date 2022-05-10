package graph.chromosome;

import app.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class DesignBuilder {

    public static JsonObject object = null;

    public static int snapshot_count = 0;
    public static void takeSnapshot(){
        String file_name = "/decisions/debug/designs/stages/SNAPSHOT-" + DesignBuilder.snapshot_count + ".json";
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

    public static boolean removeObjectFromParent(JsonObject parent_obj, JsonObject to_remove){
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

    public static void recursiveJsonSearch(JsonElement data_source, String operates_on, JsonObject dependencies){
        DesignBuilder.recursiveJsonRefSearch(data_source, operates_on, dependencies);
    }
    public static void recursiveJsonRefSearch(JsonElement data_source, String operates_on, JsonObject dependencies){
        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder.recursiveJsonRefSearch(element, operates_on, dependencies);
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
                    DesignBuilder.recursiveJsonRefSearch(value, operates_on, dependencies);
                }
            }
        }
    }





    public static void recursiveJsonShallowSearch(JsonElement data_source, String operates_on, JsonObject dependencies, JsonArray decision_refs){
        DesignBuilder.recursiveJsonRefShallowSearch(data_source, operates_on, dependencies, decision_refs);
    }
    public static void recursiveJsonRefShallowSearch(JsonElement data_source, String operates_on, JsonObject dependencies, JsonArray decision_refs){
        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder.recursiveJsonRefShallowSearch(element, operates_on, dependencies, decision_refs);
            }
        }
        else if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            // --> Try to get element UID
            String uid = "null";
            if(json_dependency_obj.has("uid")){
                uid = json_dependency_obj.get("uid").getAsString();
            }

            // --> Check if JsonObject contains desired key
            if(json_dependency_obj.has(operates_on) && json_dependency_obj.get(operates_on).isJsonArray()){
                JsonArray value = json_dependency_obj.get(operates_on).getAsJsonArray();
                // --> Check if duplicate UID is present
                if(dependencies.keySet().contains(uid)){
                    System.out.println("--> ERROR: duplicate UIDs when searching for operates on in recursive search");
                    System.exit(0);
                }
                dependencies.add(uid, value.getAsJsonArray());
                if(!decision_refs.contains(json_dependency_obj)){
                    decision_refs.add(json_dependency_obj);
                }
            }
            else{
                for(String key: json_dependency_obj.keySet()){
                    JsonElement value = json_dependency_obj.get(key);
                    DesignBuilder.recursiveJsonRefShallowSearch(value, operates_on, dependencies, decision_refs);
                }
            }
        }
    }




    public static void recursiveJsonSearchAssigning(JsonElement data_source, String operates_on, JsonObject dependencies, boolean is_root, boolean delete_ref){
        DesignBuilder.recursiveJsonRefSearchAssigning(data_source, operates_on, dependencies, is_root, delete_ref);
    }

    public static void recursiveJsonRefSearchAssigning(JsonElement data_source, String operates_on, JsonObject dependencies, boolean is_root, boolean delete_ref){

        if(data_source.isJsonArray()){
            JsonArray json_dependency_ary = data_source.getAsJsonArray();
            for(JsonElement element: json_dependency_ary){
                DesignBuilder.recursiveJsonRefSearchAssigning(element, operates_on, dependencies, is_root, delete_ref);
            }
        }
        else if(data_source.isJsonObject()){
            JsonObject json_dependency_obj = data_source.getAsJsonObject();

            // --> Try to get element UID
            String uid = "null";
            if(json_dependency_obj.has("uid")){
                uid = json_dependency_obj.get("uid").getAsString();
            }

            boolean delete_instruction = false;
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
                        delete_instruction = true;
                        // json_dependency_obj.remove(key);
                    }
                    else{
                        dependencies.add(uid, value.getAsJsonArray());
                    }
                }
                else{
                    DesignBuilder.recursiveJsonRefSearchAssigning(value, operates_on, dependencies, is_root, delete_ref);
                }

            }

            if(delete_instruction){
                json_dependency_obj.remove(operates_on);
            }


        }

    }







}
