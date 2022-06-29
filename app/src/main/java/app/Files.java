package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Files {

    // --------------------------
    // ----- Root Directory -----
    // --------------------------

    public static Path root_dir = Paths.get("/decisions");
    // public static Path root_dir = Paths.get("/home", "gapaza", "repos", "seakers", "decision-graph");
    // public static Path root_dir = Paths.get("C:", "Program Files", "Cameo Systems Modeler Demo", "plugins", "adg");


    // ---------------------------
    // ----- Debug Directory -----
    // ---------------------------
    public static Path debug_dir = Paths.get(Files.root_dir.toString(), "debug");


    // ------------------------
    // ----- Formulations -----
    // ------------------------

    public static Path formulation_dir = Paths.get(Files.root_dir.toString(), "formulations");

    // --> 1. TDRS
    public static Path tdrs_formulation = Paths.get(Files.formulation_dir.toString(), "TDRS", "graph.json");
    public static Path tdrs_formulation1 = Paths.get(Files.formulation_dir.toString(), "TDRS", "graph_1.json");
    public static Path tdrs_formulation2 = Paths.get(Files.formulation_dir.toString(), "TDRS", "graph_2.json");
    public static Path tdrs_problem = Paths.get(Files.formulation_dir.toString(), "TDRS", "problems", "base.json");


    // --> 2. GNC
    public static Path gnc_formulation  = Paths.get(Files.formulation_dir.toString(), "GNC", "graph.json");
    public static Path gnc_formulation1 = Paths.get(Files.formulation_dir.toString(), "GNC", "graph_1.json");
    public static Path gnc_formulation2 = Paths.get(Files.formulation_dir.toString(), "GNC", "graph_2.json");
    public static Path gnc_formulation3 = Paths.get(Files.formulation_dir.toString(), "GNC", "graph_3.json");
    public static Path gnc_problem = Paths.get(Files.formulation_dir.toString(), "GNC", "problems", "base.json");


    // --> 3. EOSS
    public static Path eoss_formulation  = Paths.get(Files.formulation_dir.toString(), "EOSS", "graph.json");
    public static Path eoss_problem  = Paths.get(Files.formulation_dir.toString(), "EOSS", "problems", "base.json");


    // ---------------------------
    // ----- Current Problem -----
    // ---------------------------

    public static Path curr_formulation = Files.tdrs_formulation2;
    public static Path curr_problem     = Files.tdrs_problem;
    






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
