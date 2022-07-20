package evaluation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import moea.adg.AdgProblem;
import moea.adg.AdgSolution;
import moea.vanilla.TdrsSolution;
import org.moeaframework.core.Solution;
import scan.EvaluatorWrapper;
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
            build.evaluator = new EvaluatorWrapper();
            build.gson = new Gson();
            TdrsEvaluator.instance = build;
            return build;
        }
    }

    public EvaluatorWrapper evaluator = null;
    public Gson gson;


    public ArrayList<Double> evaluateVanilla(Solution abstract_soln){
        TdrsSolution solution = (TdrsSolution) abstract_soln;

        System.out.println("--> DESIGN TO EVAL: " + solution.design);
        NumArray pay_assignment = solution.getPayloadAssignment();
        ArrayList<Long> pay_alloc = solution.getPayloadAllocation();
        String procurement_string = solution.getProcurementInfo();

        // -----------------------------
        // ----- EVALUATE / RETURN -----
        // -----------------------------
        System.out.println("--> ANTENNA ASSIGNMENT: " + pay_assignment);
        System.out.println("--> ANTENNA PARTITIONING: " + pay_alloc);
        Architecture arch = new Architecture();
        try{

            arch.setVariable( "id", "sm" + 1 );
            arch.setVariable("payload-assignment", pay_assignment);
            arch.setVariable("payload-allocation", pay_alloc);
            arch.setVariable("contract-modalities", procurement_string);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return this.evaluator.evaluate(arch);
    }


    public ArrayList<Double> evaluateAdg(Solution abstract_soln){
        AdgSolution solution = (AdgSolution) abstract_soln;

        // ------------------------------
        // ----- ANTENNA ASSIGNMENT -----
        // ------------------------------
//        JsonObject assignment_decision = solution.getDesignDecision("Antenna Assignment");
//        ArrayList<Integer> antenna_assignment = gson.fromJson(assignment_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
//        NumArray pay_assignment = new NumArray();
//        int idx = 0;
//        for(int x = 0; x < 3; x++){
//            String num = "";
//            for(int y = 0; y < 3; y++){
//                num += antenna_assignment.get(idx);
//                idx++;
//            }
//            System.out.println(AdgProblem.baseConversion(num, 2, 10));
//            pay_assignment.add(AdgProblem.baseConversion(num, 2, 10));
//        }
        NumArray pay_assignment = solution.getPayloadAssignment();


        // --------------------------------
        // ----- ANTENNA PARTITIONING -----
        // --------------------------------
//        JsonObject partitioning_decision = solution.getDesignDecision("Antenna Partitioning");
//        ArrayList<Long> pay_alloc = new ArrayList<Long>();
//        ArrayList<String> keys = new ArrayList<>();
//        keys.add("1");
//        keys.add("6");
//        keys.add("11");
//        for(String key: keys){
//            NumArray tmp = new NumArray();
//
//            if(partitioning_decision.keySet().contains(key)){
//                JsonObject sub_decision = partitioning_decision.getAsJsonObject(key);
//                ArrayList<Integer> sub_chromosome = gson.fromJson(sub_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
//                for(Integer var: sub_chromosome){
//                    tmp.add(var);
//                }
//
//            }
//            else{
//                tmp.add(0);
//            }
//            pay_alloc.add(tmp.intArray2LongDec());
//        }
        ArrayList<Long> pay_alloc = solution.getPayloadAllocation();

        // -------------------------------
        // ----- CONTRACT MODALITIES -----
        // -------------------------------
//        JsonObject sf_decision = solution.getDesignDecision("Contract Modalities");
//        String procurement_string = "";
//        ArrayList<String> keys = new ArrayList<>();
//        keys.add("1");
//        keys.add("6");
//        keys.add("11");
//        for(String key: keys){
//            if(sf_decision.has(key)){
//                procurement_string += (" " + sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
//            }
//            else{
//                procurement_string += " N/A";
//            }
//        }
        String procurement_string = solution.getProcurementInfo();





        // -----------------------------
        // ----- EVALUATE / RETURN -----
        // -----------------------------
        System.out.println("--> ANTENNA ASSIGNMENT: " + pay_assignment);
        System.out.println("--> ANTENNA PARTITIONING: " + pay_alloc);
        Architecture arch = new Architecture();
        try{

            arch.setVariable( "id", "sm" + 1 );
            arch.setVariable("payload-assignment", pay_assignment);
            arch.setVariable("payload-allocation", pay_alloc);
            arch.setVariable("contract-modalities", procurement_string);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return this.evaluator.evaluate(arch);
    }






}
