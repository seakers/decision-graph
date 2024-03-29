package moea.adg;


import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import graph.Graph;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import scan.utils.NumArray;

import java.util.ArrayList;
import java.util.UUID;

public class AdgSolution extends Solution {


    public int design_idx;
    public boolean already_evaluated = false;
    public String ID;
    public Graph graph;
    public String design_str;


    public AdgSolution(Graph graph, int num_objectives){
        super(1, num_objectives, 0);

        int design_idx = -1;
        try{
            design_idx = graph.generateRandomDesign();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        this.design_idx        = design_idx;
        this.already_evaluated = false;
        this.ID                = UUID.randomUUID().toString();
        this.graph             = graph;
        this.design_str = "";

        BinaryIntegerVariable var = new BinaryIntegerVariable(design_idx, 0, 10000);
        this.setVariable(0, var);
    }

    public AdgSolution(Graph graph, int num_objectives, int design_idx){
        super(1, num_objectives, 0);

        this.design_idx        = design_idx;
        this.already_evaluated = false;
        this.ID                = UUID.randomUUID().toString();
        this.graph             = graph;
        this.design_str = "";

        BinaryIntegerVariable var = new BinaryIntegerVariable(design_idx, 0, 10000);
        this.setVariable(0, var);
    }

    protected AdgSolution(Solution solution){
        super(solution);

        AdgSolution design     = (AdgSolution) solution;
        this.design_idx        = design.design_idx;
        this.graph             = design.graph;
        this.already_evaluated = design.already_evaluated;
        this.ID                = design.ID;
        this.design_str        = design.design_str;
    }

    public JsonObject getDesign(){
        return this.graph.getDesign(this.design_idx);
    }

    public JsonObject getDesignDecision(String decision_name){
        return this.graph.getDesignDecision(this.design_idx, decision_name);
    }



    // --> INDIVIDUAL DESIGN DECISIONS

    public NumArray getPayloadAssignment(){
        Gson gson = new Gson();
        JsonObject assignment_decision = this.getDesignDecision("Antenna Assignment");
        ArrayList<Integer> antenna_assignment = gson.fromJson(assignment_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
        NumArray pay_assignment = new NumArray();

        int idx = 0;
        for(int x = 0; x < 5; x++){
            String num = "";
            for(int y = 0; y < 3; y++){
                num += antenna_assignment.get(idx);
                idx++;
            }
            System.out.println(AdgProblem.baseConversion(num, 2, 10));
            pay_assignment.add(AdgProblem.baseConversion(num, 2, 10));
        }

        return pay_assignment;
    }

    public ArrayList<Long> getPayloadAllocation(){
        Gson gson = new Gson();
        JsonObject partitioning_decision = this.getDesignDecision("Antenna Partitioning");
        ArrayList<Long> pay_alloc = new ArrayList<Long>();
        ArrayList<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");

        for(int x = 0; x < keys.size(); x++){
            String key = keys.get(x);
            NumArray tmp = new NumArray();

            if(partitioning_decision.keySet().contains(key)){
                JsonObject sub_decision = partitioning_decision.getAsJsonObject(key);
                ArrayList<Integer> sub_chromosome = gson.fromJson(sub_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
                for(Integer var: sub_chromosome){
                    tmp.add(var);
                }

            }
            else{
                tmp.add(0);
            }
            pay_alloc.add(tmp.intArray2LongDec());
        }

        return pay_alloc;
    }

    public ArrayList<String> getProcurementInfo(){
        JsonObject sf_decision = this.getDesignDecision("Contract Modalities");
        String procurement_string = "";
        ArrayList<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");
        ArrayList<String> sf_values = new ArrayList<>();

        for(String key: keys){
            if(sf_decision.has(key)){
                sf_values.add(sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
                procurement_string += (" " + sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
            }
            else{
                procurement_string += " N/A";
                sf_values.add("N/A");
            }
        }
        return sf_values;
    }

    public ArrayList<String> getNetworkTypes(){
        JsonObject sf_decision = this.getDesignDecision("Network Types");
        String procurement_string = "";
        ArrayList<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");

        ArrayList<String> sf_values = new ArrayList<>();

        for(String key: keys){
            if(sf_decision.has(key)){
                sf_values.add(sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
//                procurement_string += (" " + sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
            }
            else{
                procurement_string += " 0";
                sf_values.add("0");
            }
        }
        return sf_values;
    }

    public ArrayList<Integer> getISLPayloads(){
        JsonObject sf_decision = this.getDesignDecision("ISL Payloads");
        ArrayList<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");
        ArrayList<Integer> sf_values = new ArrayList<>();
        for(String key: keys){
            if(sf_decision.has(key)){
                String name = sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString();
                if(name.equals("yes")){
                    sf_values.add(1);
                }
                else{
                    sf_values.add(0);
                }
//                procurement_string += (" " + sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
            }
            else{
                sf_values.add(0);
            }
        }
        return sf_values;
    }

    public ArrayList<String> getGroundStations(){
        Gson gson = new Gson();
        JsonObject gs_decision = this.getDesignDecision("Ground Stations");
        JsonObject gs_obj = gs_decision.getAsJsonObject("0");
        ArrayList<Integer> chromosome_list = gson.fromJson(gs_obj.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
        ArrayList<String> gs_string_list = new ArrayList<>();
        for(JsonElement element: gs_obj.getAsJsonArray("ref")){
            gs_string_list.add(element.getAsJsonObject().get("name").getAsString());
        }
        return gs_string_list;
    }

    public ArrayList<String> getUserGroundStations(){
        Gson gson = new Gson();
        JsonObject user_gs_decision = this.getDesignDecision("User Ground Stations");
        JsonObject gs_obj = user_gs_decision.getAsJsonObject("0");
        ArrayList<Integer> chromosome_list = gson.fromJson(gs_obj.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
        ArrayList<String> user_gs_string_list = new ArrayList<>();
        for(JsonElement element: gs_obj.getAsJsonArray("ref")){
            String name = element.getAsJsonObject().get("name").getAsString();
            if(!name.equals("None")){
                user_gs_string_list.add(element.getAsJsonObject().get("name").getAsString());
            }
        }
        return user_gs_string_list;
    }

    public ArrayList<String> getNumAntennas(){
        JsonObject sf_decision = this.getDesignDecision("Num Ground Station Antennas");
        ArrayList<String> keys = new ArrayList<>();
//        Gson gson       = new GsonBuilder().setPrettyPrinting().create();
//        System.out.println(gson.toJson(sf_decision));
        keys.add("43");
        keys.add("46");
        ArrayList<String> sf_values = new ArrayList<>();
        for(String key: keys){
            if(sf_decision.has(key)){
                sf_values.add(sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
//                procurement_string += (" " + sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString());
            }
//            else{
//                sf_values.add("0");
//            }
        }
        return sf_values;
    }



    public String getFracStrategy(){
        Gson gson = new Gson();
        JsonObject assignment_decision = this.getDesignDecision("Frac Strategy");
        JsonObject gs_obj = assignment_decision.getAsJsonObject("0");

        String strategy = gs_obj.getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString();
        return strategy;
    }


    public JsonArray getDesignBits(){
        Gson gson = new Gson();
        ArrayList<Integer> design_bits = new ArrayList<>();
        // --> Basically just write array of Ints for VANILLA TdrsSolution

        // --> 1. Payload Assignment Bits
        JsonObject assignment_decision = this.getDesignDecision("Antenna Assignment");
        ArrayList<Integer> antenna_assignment = gson.fromJson(assignment_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
        for(Integer bit: antenna_assignment){
            design_bits.add(bit);
        }

        // --> 2. Payload Allocation Bits
        JsonObject partitioning_decision = this.getDesignDecision("Antenna Partitioning");
        ArrayList<String> keys = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");
        for(int x = 0; x < keys.size(); x++){
            String key = keys.get(x);
            if(partitioning_decision.keySet().contains(key)){
                JsonObject sub_decision = partitioning_decision.getAsJsonObject(key);
                ArrayList<Integer> sub_chromosome = gson.fromJson(sub_decision.getAsJsonArray("chromosome"), new TypeToken<ArrayList<Integer>>(){}.getType());
                ArrayList<String> ref_uids = gson.fromJson(sub_decision.getAsJsonArray("ref_uids"), new TypeToken<ArrayList<String>>(){}.getType());
                design_bits.add(0);
                design_bits.add(0);
                design_bits.add(0);

                int count = 0;
                for(Integer var: sub_chromosome){
                    if(ref_uids.get(count).equals("36")){
                        design_bits.set(design_bits.size()-3, var);
                    }
                    else if(ref_uids.get(count).equals("37")){
                        design_bits.set(design_bits.size()-2, var);
                    }
                    else if(ref_uids.get(count).equals("38")){
                        design_bits.set(design_bits.size()-1, var);
                    }
                    count++;
                }
            }
            else{
                design_bits.add(0);
                design_bits.add(0);
                design_bits.add(0);
            }
        }

        // --> 3. Contract Modality Bits
        JsonObject sf_decision = this.getDesignDecision("Contract Modalities");
        ArrayList<String> keys2 = new ArrayList<>();
        keys.add("1");
        keys.add("8");
        keys.add("15");
        keys.add("22");
        keys.add("29");
        for(int x = 0; x < keys2.size(); x++){
            String key = keys2.get(x);
            if(sf_decision.has(key)){
                if(sf_decision.getAsJsonObject(key).getAsJsonArray("ref").get(0).getAsJsonObject().get("name").getAsString().equals("procurement")){
                    design_bits.add(0);
                }
                else{
                    design_bits.add(1);
                }
            }
            else{
                design_bits.add(0);
            }
        }

        return gson.toJsonTree(design_bits).getAsJsonArray().deepCopy();
    }





















    @Override
    public String toString(){
        return Integer.toString(this.design_idx);
    }

    @Override
    public int hashCode(){
        return this.design_idx;
    }

    @Override
    public Solution copy(){
        return new AdgSolution(this);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if(this.design_idx == ((AdgSolution) obj).design_idx){
            return true;
        }
        else{
            return false;
        }
    }
}
