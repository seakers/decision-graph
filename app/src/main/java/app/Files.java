package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;

public class Files {


    public static String tdrs_formulation = "/decisions/formulations/TDRS/graph.json";
    public static String tdrs_formulation1 = "/decisions/formulations/TDRS/graph_1.json";
    public static String tdrs_problem     = "/decisions/formulations/TDRS/problems/base.json";


    public static String gnc_formulation = "/decisions/formulations/GNC/graph.json";
    public static String gnc_formulation1 = "/decisions/formulations/GNC/graph_1.json";
    public static String gnc_formulation2 = "/decisions/formulations/GNC/graph_2.json";
    public static String gnc_formulation3 = "/decisions/formulations/GNC/graph_3.json";
    public static String gnc_problem     = "/decisions/formulations/GNC/problems/base.json";


    public static String eoss_formulation = "/decisions/formulations/EOSS/graph.json";
    public static String eoss_problem     = "/decisions/formulations/EOSS/problems/base.json";


    public static String curr_formulation = Files.gnc_formulation;
    public static String curr_problem     = Files.gnc_problem;





    public static void writeDebugFile(String full_file_path, JsonArray elements){
        if(full_file_path == null){
            System.out.println("--> DEBUG FILE PATH IS NULL");
            return;
        }
        try{
            Gson gson       = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(full_file_path);

            gson.toJson(elements.deepCopy(), outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void writeDebugFile(String full_file_path, JsonObject elements){
        if(full_file_path == null){
            System.out.println("--> DEBUG FILE PATH IS NULL");
            return;
        }
        try{
            Gson       gson       = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(full_file_path);

            gson.toJson(elements.deepCopy(), outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }



}
