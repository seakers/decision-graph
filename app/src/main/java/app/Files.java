package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;

public class Files {

    // --------------------------
    // ----- Root Directory -----
    // --------------------------

    // public static String root_dir = "/decisions";
    // public static String root_dir = "/home/gapaza/repos/seakers/decision-graph";
    // public static String root_dir = "C:\\Program Files\\Cameo Systems Modeler Demo\\plugins\\adg";

    // ---------------------------
    // ----- Debug Directory -----
    // ---------------------------

    public static String debug_dir        = "/decisions/debug";
    // public static String debug_dir        = "C:\\Program Files\\Cameo Systems Modeler Demo\\plugins\\adg\\debug";


    // ------------------------
    // ----- Formulations -----
    // ------------------------

    // --> 1. TDRS
    public static String tdrs_formulation = "/decisions/formulations/TDRS/graph.json";
    public static String tdrs_formulation1 = "/decisions/formulations/TDRS/graph_1.json";
    public static String tdrs_formulation2 = "/decisions/formulations/TDRS/graph_2.json";
    public static String tdrs_problem     = "/decisions/formulations/TDRS/problems/base.json";


    // --> 2. GNC
    public static String gnc_formulation = "/decisions/formulations/GNC/graph.json";
    public static String gnc_formulation1 = "/decisions/formulations/GNC/graph_1.json";
    public static String gnc_formulation2 = "/decisions/formulations/GNC/graph_2.json";
    public static String gnc_formulation3 = "/decisions/formulations/GNC/graph_3.json";
    public static String gnc_problem     = "/decisions/formulations/GNC/problems/base.json";


    // --> 3. EOSS
    public static String eoss_formulation = "/decisions/formulations/EOSS/graph.json";
    public static String eoss_problem     = "/decisions/formulations/EOSS/problems/base.json";


    // ---------------------------
    // ----- Current Problem -----
    // ---------------------------

    public static String curr_formulation = Files.tdrs_formulation2;
    public static String curr_problem     = Files.tdrs_problem;
    






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
