package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.moeaframework.core.NondominatedPopulation;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Files {

    // --------------------------
    // ----- Root Directory -----
    // --------------------------

    public static String root_dir = Paths.get("/decisions").toString();
    // public static String root_dir = Paths.get("/home", "gapaza", "repos", "seakers", "decision-graph").toString();
    // public static String root_dir = Paths.get("C:", "Program Files", "Cameo Systems Modeler Demo", "plugins", "adg").toString();
    // public static String root_dir = Paths.get("C:", "Users", "apaza", "repos", "seakers", "decision-graph").toString();


    // ---------------------------
    // ----- Debug Directory -----
    // ---------------------------
    public static String debug_dir = Paths.get(Files.root_dir, "debug").toString();

    // -----------------------------
    // ----- Results Directory -----
    // -----------------------------
    public static String results_dir = Paths.get(Files.root_dir, "results").toString();

    public static String tdrs_results = Paths.get(Files.results_dir, "TDRS").toString();
    public static String gnc_results = Paths.get(Files.results_dir, "GNC").toString();
    public static String eoss_results = Paths.get(Files.results_dir, "EOSS").toString();

    // ------------------------
    // ----- Formulations -----
    // ------------------------
    public static String formulation_dir = Paths.get(Files.root_dir, "formulations").toString();

    // --> 1. TDRS
    public static String tdrs_formulation = Paths.get(Files.formulation_dir, "TDRS", "graph.json").toString();
    public static String tdrs_formulation1 = Paths.get(Files.formulation_dir, "TDRS", "graph_1.json").toString();
    public static String tdrs_formulation2 = Paths.get(Files.formulation_dir, "TDRS", "graph_2.json").toString();
    public static String tdrs_formulation3 = Paths.get(Files.formulation_dir, "TDRS", "graph_3.json").toString();
    public static String tdrs_problem = Paths.get(Files.formulation_dir, "TDRS", "problems", "base.json").toString();


    // --> 2. GNC
    public static String gnc_formulation  = Paths.get(Files.formulation_dir, "GNC", "graph.json").toString();
    public static String gnc_formulation1 = Paths.get(Files.formulation_dir, "GNC", "graph_1.json").toString();
    public static String gnc_formulation2 = Paths.get(Files.formulation_dir, "GNC", "graph_2.json").toString();
    public static String gnc_formulation3 = Paths.get(Files.formulation_dir, "GNC", "graph_3.json").toString();
    public static String gnc_problem = Paths.get(Files.formulation_dir, "GNC", "problems", "base.json").toString();


    // --> 3. EOSS
    public static String eoss_formulation  = Paths.get(Files.formulation_dir, "EOSS", "graph.json").toString();
    public static String eoss_problem  = Paths.get(Files.formulation_dir, "EOSS", "problems", "base.json").toString();


    // ---------------------------
    // ----- Current Problem -----
    // ---------------------------

    public static String curr_formulation = Files.tdrs_formulation3;
    public static String curr_problem     = Files.tdrs_problem;
    public static String curr_results     = Files.tdrs_results;
    





    public static void createDir(String path){
        File directory = new File(path);
        if (!directory.exists()){
            directory.mkdirs();
        }
    }





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
