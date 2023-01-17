package evaluation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import moea.adg.AdgProblem;
import moea.adg.AdgSolution;
import moea.vanilla.TdrsFullSolution;
import moea.vanilla.TdrsSolution;
import org.moeaframework.core.Solution;
import scan.wrapper.ScanWrapper;
import scan.elements.Architecture;
import scan.utils.NumArray;

import java.util.ArrayList;

public class TdrsEvaluator {

    // -----------------------------
    // ----- SINGLETON PATTERN -----
    // -----------------------------

    private static TdrsEvaluator instance = new TdrsEvaluator();

    public static TdrsEvaluator getInstance() { return instance; }


    public static class Builder{
        public TdrsEvaluator build(){
            TdrsEvaluator build = new TdrsEvaluator();
            build.evaluator = new ScanWrapper("/scan/TestCase_wgs.INPUTS");
            build.gson = new Gson();
            TdrsEvaluator.instance = build;
            return build;
        }
    }

    public ScanWrapper evaluator = null;
    public Gson gson;


    public ArrayList<Double> evaluateVanilla(Solution abstract_soln){
        TdrsFullSolution solution = (TdrsFullSolution) abstract_soln;

        solution.validateDesign();
        solution.print();

//        System.out.println("--> DESIGN TO EVAL: " + solution.design);
//        NumArray pay_assignment = solution.getPayloadAssignment();
//        ArrayList<Long> pay_alloc = solution.getPayloadAllocation();
//        String procurement_string = solution.getProcurementInfo();
//
//        // -----------------------------
//        // ----- EVALUATE / RETURN -----
//        // -----------------------------
//        System.out.println("--> ANTENNA ASSIGNMENT: " + pay_assignment);
//        System.out.println("--> ANTENNA PARTITIONING: " + pay_alloc);
//        Architecture arch = new Architecture();
//        try{
//
//            arch.setVariable( "id", "sm" + 1 );
//            arch.setVariable("payload-assignment", pay_assignment);
//            arch.setVariable("payload-allocation", pay_alloc);
//            arch.setVariable("contract-modalities", procurement_string);
//        }
//        catch (Exception ex){
//            ex.printStackTrace();
//        }
        return this.evaluator.evaluate(solution.getArchitecture());
    }


    public ArrayList<Double> evaluateAdg(Solution abstract_soln){
        System.out.println("--> EVALUATING ADG SOLUTION");

        AdgSolution solution = (AdgSolution) abstract_soln;

        // ------------------------------
        // ----- ANTENNA ASSIGNMENT -----
        // ------------------------------
        NumArray pay_assignment = solution.getPayloadAssignment();


        // --------------------------------
        // ----- ANTENNA PARTITIONING -----
        // --------------------------------
        ArrayList<Long> pay_alloc = solution.getPayloadAllocation();


        // -------------------------
        // ----- NETWORK TYPES -----
        // -------------------------
        ArrayList<String> network_types = solution.getNetworkTypes();


        // -------------------------------
        // ----- CONTRACT MODALITIES -----
        // -------------------------------
        ArrayList<String> procurement_string = solution.getProcurementInfo();

        // ---------------------------
        // ----- GROUND STATIONS -----
        // ---------------------------
        ArrayList<String> ground_stations = solution.getGroundStations();


        // ----------------------------------
        // ----- FRACTIONATION STRATEGY -----
        // ----------------------------------
        String frac_strategy = solution.getFracStrategy();


        // --------------------------------
        // ----- USER GROUND STATIONS -----
        // --------------------------------
        ArrayList<String> user_gs = solution.getUserGroundStations();

        // ---------------------------
        // ----- NUM GS ANTENNAS -----
        // ---------------------------
        ArrayList<String> num_user_gs_antennas = solution.getNumAntennas();
        if(user_gs.isEmpty()){
            num_user_gs_antennas = new ArrayList<>();
        }

        // ------------------------
        // ----- ISL PAYLOADS -----
        // ------------------------
        ArrayList<Integer> isl_payloads = solution.getISLPayloads();



        // -----------------------------
        // ----- EVALUATE / RETURN -----
        // -----------------------------
        System.out.println("--> ANTENNA ASSIGNMENT: " + pay_assignment);
        System.out.println("--> ANTENNA PARTITIONING: " + pay_alloc);
        System.out.println("--> NETWORK TYPE: " + network_types);
        System.out.println("--> CONTRACT MODALITIES: " + procurement_string);
        System.out.println("--> GROUND STATIONS: " + ground_stations);
        System.out.println("--> FRAC STRATEGY: " + frac_strategy);
        System.out.println("--> USER GROUND STATIONS: " + user_gs);
        System.out.println("--> NUM GS ANTENNAS: " + num_user_gs_antennas);
        System.out.println("--> ISL PAYLOADS: " + isl_payloads);
        Architecture arch = new Architecture();
        try{
            arch.setVariable( "id", "sm" + 1 );
            arch.setVariable("payload-assignment", pay_assignment);
            arch.setVariable("payload-allocation", pay_alloc);
            arch.setVariable( "network-type", network_types );
            arch.setVariable("contract-modalities", procurement_string);
            arch.setVariable("ground-stations", ground_stations );
            arch.setVariable("fractionation-strategy", frac_strategy );
            arch.setVariable("user-ground-stations", user_gs );
            arch.setVariable("number-antennas-user-gs", num_user_gs_antennas );
            arch.setVariable("isl-payloads", isl_payloads );
            if(!arch.checkConsistency()){
                throw new Exception("INCONSISTENT ARCHITECTURE");
            }
//            System.out.println(arch);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        ArrayList<Double> results = this.evaluator.evaluate(arch);
//        System.out.println("--> RESULTS: " + results);
//        System.exit(0);
        return results;
    }






}
