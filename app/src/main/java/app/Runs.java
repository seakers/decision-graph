package app;




import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import moea.adg.AdgSolution;
import moea.vanilla.TdrsSolution;
import org.moeaframework.Analyzer;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.List;

public class Runs {

    /*
        Run Specifications
     */
    public static int num_runs = Integer.parseInt(System.getenv("NUM_RUNS"));
    // public static int num_runs = 20;

    public static int nfe = Integer.parseInt(System.getenv("NFE"));
    // public static int nfe = 250;

    public static int pop_size = Integer.parseInt(System.getenv("POP_SIZE"));
    // public static int pop_size = 30;

    public static String type = System.getenv("RUN_TYPE"); // ADG | VANILLA
    // public static String type = "VANILLA"; // ADG | VANILLA

    public static int run_number = Integer.parseInt(System.getenv("RUN_NUMBER"));



    public static String group_path;
    public static String group_run_path;

    public static String run_path;

    public static void createRunGroup(){
        String results_path = Files.curr_results;
        System.out.println(results_path);
        Files.createDir(results_path);

        // RUNGROUP CHANGE
        int group_num = new File(results_path).list().length;
        // String group_dir_name = "group_" + group_num;
        String group_dir_name = System.getenv("RUN_GROUP");

        Runs.group_path = Paths.get(results_path, group_dir_name).toString();
        Runs.group_run_path = Paths.get(Runs.group_path, "runs").toString();
        Files.createDir(Runs.group_path);
        Files.createDir(Runs.group_run_path);

        Runs.writeRunGroupSpecs();
    }

    public static void writeRunGroupSpecs(){

        String specs_file = Paths.get(Runs.group_path, "run_specs.json").toString();
        if((new File(specs_file).exists())){
            return;
        }

        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(specs_file);

            JsonObject specs = new JsonObject();
            specs.addProperty("NFE", Runs.nfe);
            specs.addProperty("INITIAL POPULATION SIZE", Runs.pop_size);
            specs.addProperty("type", Runs.type);
            specs.addProperty("formulation", Files.curr_formulation);
            specs.addProperty("problem", Files.curr_problem);

            gson.toJson(specs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
        }

    }






    /*
        Each moea run records
        - Final population (single JsonArray file containing all JsonObject designs)
        - Hypervolume progression as a function of NFE
     */

    public static void initRun(){
        String results_path = Runs.group_run_path;
        String run_dir_name = "run_" + Runs.run_number;
        String run_path = Paths.get(results_path, run_dir_name).toString();
        Files.createDir(run_path);
        Runs.run_path = run_path;
    }

    public static void writeRun(Analyzer analyzer, Accumulator accumulator, NondominatedPopulation final_pop, int nfe){

        // --> 1. Save population
        Runs.writePopulation(final_pop, Runs.run_path);

        // --> 2. Save hv data
        Runs.writeHVProgression(accumulator, Runs.run_path);

        analyzer.printAnalysis();
    }



    public static void writeHVProgression(Accumulator accumulator, String run_path){
        String hv_file_path = Paths.get(run_path, "hypervolume.txt").toString();
        File hv_file = new File(hv_file_path);
        try{
            accumulator.saveCSV(hv_file);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
    
    public static void writePopulation(NondominatedPopulation population, String run_path){
        String pop_file = Paths.get(run_path, "population.json").toString();
        JsonArray designs   = new JsonArray();
        try{
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(pop_file);

            for (Solution solution : population) {
                designs.add(Runs.getDesignJson(solution));
            }
            gson.toJson(designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
        }
    }

    public static JsonObject getDesignJson(Solution solution){
        Gson gson = new Gson();
        JsonObject design = new JsonObject();
        if(solution instanceof AdgSolution){
            AdgSolution soln = (AdgSolution) solution;
            design = soln.getDesign();
            design.addProperty("benefit", soln.getObjective(0) * -1);
            design.addProperty("cost", soln.getObjective(1));
        }
        if(solution instanceof TdrsSolution){
            TdrsSolution soln = (TdrsSolution) solution;
            design.add("design", gson.toJsonTree(soln.design).getAsJsonArray());
            design.addProperty("benefit", soln.getObjective(0) * -1);
            design.addProperty("cost", soln.getObjective(1));
        }
        return design;
    }






    public static void writeTdrsPopulation(List<Solution> population){
        String pop_file = Paths.get(run_path, "starting_pop.json").toString();
        JsonArray designs = new JsonArray();

        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter outputfile = new FileWriter(pop_file);
            for(Solution item: population){
                if(item instanceof TdrsSolution){
                    TdrsSolution solution = (TdrsSolution) item;
                    designs.add(solution.getTdrsDesignJson());
                }
                else if(item instanceof AdgSolution){
                    AdgSolution solution = (AdgSolution) item;
                    designs.add(solution.getDesignBits());
                }
                else{
                    System.out.println("--> ERROR: can't write design as class type not recognized");
                    System.exit(0);
                }
            }
            gson.toJson(designs, outputfile);
            outputfile.flush();
            outputfile.close();
        }
        catch (Exception e){
            e.printStackTrace();
            System.out.println("WRITING EXCEPTION");
        }
    }





}
