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





}
