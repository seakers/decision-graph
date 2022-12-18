package moea.vanilla;

import com.google.gson.JsonArray;
import graph.Decision;
import graph.chromosome.BitString;
import moea.adg.AdgProblem;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import scan.elements.Architecture;
import scan.utils.NumArray;

import java.util.ArrayList;
import java.util.Random;

public class TdrsFullSolution extends Solution {

    public Random rand = new Random();
    public ArrayList<Integer> design;
    public ArrayList<Double> objective_values = new ArrayList<>();
    public boolean already_evaluated = false;

    /*
        Decisions (65 bits)
        - 1. Payload Assignment: 21 bits (7 consts) (3 payloads)
        - 2. Payload Partitioning: 21 bits (1/2/3 possible for each bit)
        - 3. Network Type: 7 bits (0/1/2 for: bent-pipe/circuit-switched/packet-switched)
        - 4. Contract Modalities: 7 bits (0/1 for procurement or contract-modalities)
        - 5. ISL Payload: 7 bits (0/1 for: no/yes)
        - 6. Ground Stations: 2 bits (0/1 for: yes/no) (White-Sands/Guam)


        Decisions (43 bits)
        - 1. Payload Assignment: 15 bits (5 consts) (3 payloads)
        - 2. Payload Partitioning: 15 bits (1/2/3 possible for each bit)
        - 3. Network Type: 5 bits (0/1 for: bent-pipe/circuit-switched)
        - 4. Contract Modalities: 5 bits (0/1 for procurement or contract-modalities)
        - 6. Ground Stations: 2 bits (0/1 for: yes/no) (White-Sands/Guam)
        - 7. Fractination Strategy: 1 bit (0/1 for: all-mothers/mother-daughter)
     */
    public int num_consts = 5;
    public int num_pays = 3;
    public int num_gs = 2;


    public int pay_assign_bits = num_consts * num_pays;
    public int pay_part_bits = num_consts * num_pays;
    public int net_type_bits = num_consts;
    public int con_mod_bits = num_consts;
    public int gs_bits = num_gs;
    public int frac_strat_bits = 1;



    protected TdrsFullSolution(Solution solution){
        super(solution);
        TdrsFullSolution soln = (TdrsFullSolution) solution;
        this.already_evaluated = soln.already_evaluated;
        this.rand = soln.rand;
        this.design = soln.design;
        this.objective_values = soln.objective_values;
        this.num_consts = soln.num_consts;
        this.num_pays = soln.num_pays;
        this.num_gs = soln.num_gs;
        this.pay_assign_bits = soln.pay_assign_bits;
        this.pay_part_bits = soln.pay_part_bits;
        this.net_type_bits = soln.net_type_bits;
        this.con_mod_bits = soln.con_mod_bits;
        this.gs_bits = soln.gs_bits;
        this.frac_strat_bits = soln.num_consts;
    }

    public TdrsFullSolution(int num_objectives, ArrayList<Integer> design){
        super(1, num_objectives, 0);
        this.design = design;
        this.setVariable(0, new BinaryIntegerVariable(0, 0, 10000));
    }

    public TdrsFullSolution(int num_objectives){
        super(1, num_objectives, 0);
        this.design = this.generateRandomDesign();
        this.setVariable(0, new BinaryIntegerVariable(0, 0, 10000));
    }



    private ArrayList<Integer> generateRandomDesign(){
        ArrayList<Integer> random_design = new ArrayList<>();

        for(int x = 0; x < this.pay_assign_bits; x++){ // - Payload Assignment: 21 bits (7 consts) (3 payloads)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < this.pay_part_bits; x++){ // - Payload Partitioning: 21 bits (1/2/3 possible for each bit)
            random_design.add(this.rand.nextInt(3) + 1);
        }
        for(int x = 0; x < this.net_type_bits; x++){ // - Network Type: 7 bits (0/1/2 for: bent-pipe/circuit-switched/packet-switched)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < this.con_mod_bits; x++){ // - Contract Modalities: 7 bits (0/1 for procurement or contract-modalities)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < this.gs_bits; x++){ // - Ground Stations: 2 bits (0/1 for: yes/no) (White-Sands/Guam)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < this.frac_strat_bits; x++){ // - Fractionation Strategy: 1 bit (0/1 for: all-mothers/mother-daughter)
            random_design.add(this.rand.nextInt(2));
        }

        return random_design;
    }

    private ArrayList<Integer> generateRandomDesignOld(){
        ArrayList<Integer> random_design = new ArrayList<>();

        for(int x = 0; x < 21; x++){ // - Payload Assignment: 21 bits (7 consts) (3 payloads)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 21; x++){ // - Payload Partitioning: 21 bits (1/2/3 possible for each bit)
            random_design.add(this.rand.nextInt(3) + 1);
        }
        for(int x = 0; x < 7; x++){ // - Network Type: 7 bits (0/1/2 for: bent-pipe/circuit-switched/packet-switched)
            random_design.add(this.rand.nextInt(3));
        }
        for(int x = 0; x < 7; x++){ // - Contract Modalities: 7 bits (0/1 for procurement or contract-modalities)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 7; x++){ // - ISL Payload: 7 bits (0/1 for: no/yes)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 2; x++){ // - Ground Stations: 2 bits (0/1 for: yes/no) (White-Sands/Guam)
            random_design.add(this.rand.nextInt(2));
        }

        return random_design;
    }

    private ArrayList<Integer> generateRandomDesignShort(){
        ArrayList<Integer> random_design = new ArrayList<>();

        for(int x = 0; x < 15; x++){ // - Payload Assignment: 21 bits (7 consts) (3 payloads)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 15; x++){ // - Payload Partitioning: 21 bits (1/2/3 possible for each bit)
            random_design.add(this.rand.nextInt(3) + 1);
        }
        for(int x = 0; x < 5; x++){ // - Network Type: 7 bits (0/1/2 for: bent-pipe/circuit-switched/packet-switched)
            random_design.add(this.rand.nextInt(3));
        }
        for(int x = 0; x < 5; x++){ // - Contract Modalities: 7 bits (0/1 for procurement or contract-modalities)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 2; x++){ // - Ground Stations: 2 bits (0/1 for: yes/no) (White-Sands/Guam)
            random_design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 1; x++){ // - Fractionation Strategy: 1 bit (0/1 for: all-mothers/mother-daughter)
            random_design.add(this.rand.nextInt(2));
        }

        return random_design;
    }


    // ------------------
    // --- Validation ---
    // ------------------

    public void validateDesign(){

        this.validateAntennaAssignment();
        this.validateAntennaPartitions();
        this.validateNetworkTypes();
        this.validateContracts();
        this.validateGroundStations();
        this.validateFractionationStrategy();
    }

    private void validateAntennaAssignment(){
        boolean sat_exists = false;
        for(int x = 0; x < this.pay_assign_bits; x++){
            if(this.design.get(x) != 0){
                sat_exists = true;
            }
        }
        if(!sat_exists){
            this.design.set(this.rand.nextInt(this.pay_assign_bits), 1);
        }
    }

    private void validateAntennaPartitions(){
        int pa_idx = this.pay_assign_bits;
        int as_idx = 0;
        // x: iterates over constellations
        // y: iterates over antennas
        for(int x = 0; x < this.num_consts; x++){
            for(int y = 0; y < this.num_pays; y++){
                if(this.design.get(as_idx) == 0){
                    this.design.set(pa_idx, 0);
                }
                else{
                    if(this.design.get(pa_idx) == 0){
                        this.design.set(pa_idx, 1);
                    }
                }
                pa_idx++;
                as_idx++;
            }
        }
    }

    private void validateNetworkTypes(){
        int num_network_options = 2;

        int as_idx = 0;
        int nt_idx = this.pay_assign_bits + this.pay_part_bits;
        for(int x = 0; x < this.num_consts; x++){
            boolean exists = false;
            for(int y = 0; y < this.num_pays; y++) {
                if(this.design.get(as_idx) != 0){
                    exists = true;
                }
                as_idx++;
            }
            if(!exists){
                this.design.set(nt_idx + x, -1);
            }
            else{
                if(this.design.get(nt_idx + x) == -1){
                    this.design.set(nt_idx + x, this.rand.nextInt(num_network_options));
                }
            }
        }



        for(int x = nt_idx; x < (nt_idx + this.net_type_bits); x++){
            if(design.get(x) != -1 && design.get(x) != 0 && design.get(x) != 1 && design.get(x) != 2){
                design.set(x, this.rand.nextInt(num_network_options));
            }
        }
    }

    private void validateContracts(){
        int as_idx = 0;
        int cm_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits;
        for(int x = 0; x < this.num_consts; x++){
            boolean exists = false;
            for(int y = 0; y < this.num_pays; y++) {
                if(this.design.get(as_idx) != 0){
                    exists = true;
                }
                as_idx++;
            }
            if(!exists){
                this.design.set(cm_idx + x, -1);
            }
            else{
                if(this.design.get(cm_idx + x) == -1){
                    this.design.set(cm_idx + x, this.rand.nextInt(2));
                }
            }
        }
        for(int x = cm_idx; x < (cm_idx + this.con_mod_bits); x++){
            if(this.design.get(x) != -1 && this.design.get(x) != 0 && this.design.get(x) != 1){
                design.set(x, this.rand.nextInt(2));
            }
        }
    }

    private void validateGroundStations(){
        int gs_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits + this.con_mod_bits;
        for(int x = gs_idx; x < (gs_idx + this.gs_bits); x++){
            if(this.design.get(x) != 0 && this.design.get(x) != 1){
                this.design.set(x, this.rand.nextInt(2));
            }
        }
        if(this.design.get(gs_idx) == 0 && this.design.get(gs_idx+1) == 0){
            this.design.set(gs_idx + this.rand.nextInt(2), 1);
        }
    }

    private void validateFractionationStrategy(){
        int fs_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits + this.con_mod_bits + this.gs_bits;
        if(this.design.get(fs_idx) != 0 || this.design.get(fs_idx) != 1){
            this.design.set(fs_idx, this.rand.nextInt(2));
        }
    }


    // -----------------
    // --- Decisions ---
    // -----------------

    public NumArray getAntennaAssignment(){
        NumArray pay_assignment = new NumArray();
        int idx = 0;
        for(int x = 0; x < this.num_consts; x++){
            String num = "";
            for(int y = 0; y < this.num_pays; y++){
                num += this.design.get(idx);
                idx++;
            }
            // System.out.println(num + " " + AdgProblem.baseConversion(num, 2, 10));
            pay_assignment.add(AdgProblem.baseConversion(num, 2, 10));
        }
        return pay_assignment;
    }

    public ArrayList<Long> getAntennaPartitions(){
        ArrayList<Long> pay_alloc = new ArrayList<Long>();
        int pa_idx = this.pay_assign_bits;
        for(int x = 0; x < this.num_consts; x++) {
            ArrayList<Integer> broken = new ArrayList<>();
            for (int y = 0; y < this.num_pays; y++) {
                broken.add(this.design.get(pa_idx));
                pa_idx++;
            }
            pay_alloc.add(this.repair_partition(broken).intArray2LongDec());
        }
        return pay_alloc;
    }

    public ArrayList<String> getNetworkTypes(){
        ArrayList<String> network_types = new ArrayList<>();
        int nt_idx = this.pay_assign_bits + this.pay_part_bits;
        for(int x = nt_idx; x < (nt_idx + this.net_type_bits); x++){
            int val = this.design.get(x);
            if(val == 0){
                network_types.add("bent-pipe");
            }
            else if(val == 1){
                network_types.add("circuit-switched");
            }
            else if(val == -1){
                network_types.add("0");
            }
            else{
                System.out.println("--> NETWORK TYPES INCORRECT: " + val);
            }
        }
        return network_types;
    }

    public ArrayList<String> getContractModalities(){
        ArrayList<String> contract_modalities = new ArrayList<>();
        int cm_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits;
        for(int x = cm_idx; x < (cm_idx + this.con_mod_bits); x++){
            int val = this.design.get(x);
            if(val == 0){
                contract_modalities.add("procurement");
            }
            else if(val == 1){
                contract_modalities.add("hosted-payloads");
            }
            else if(val == -1){
                contract_modalities.add("N/A");
            }
            else{
                System.out.println("--> CONTRACT MODALITIES INCORRECT: " + val);
            }
        }
        return contract_modalities;
    }

    public ArrayList<String> getGroundStations(){
        ArrayList<String> ground_stations = new ArrayList<>();
        int gs_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits + this.con_mod_bits;
        if(this.design.get(gs_idx) == 1){
            ground_stations.add("White-Sands");
        }
        if(this.design.get(gs_idx + 1) == 1){
            ground_stations.add("Guam");
        }
        return ground_stations;
    }

    public String getFractionationStrategy(){
        int fs_idx = this.pay_assign_bits + this.pay_part_bits + this.net_type_bits + this.con_mod_bits + this.gs_bits;
        if(this.design.get(fs_idx) == 1){
            return "mother-daughter";
        }
        return "all-mothers";
    }


    // -----------------
    // --- Operators ---
    // -----------------

    public void mutateDesign(){
        for(int idx = 0; idx < this.design.size(); idx++){
            if(Decision.getProbabilityResult(1.0 / this.design.size())){
                if(idx < this.pay_assign_bits){
                    if(this.design.get(idx) == 0){
                        this.design.set(idx, 1);
                    }
                    else{
                        this.design.set(idx, 0);
                    }
                }
                else if(idx < (this.pay_assign_bits + this.pay_part_bits)){
                    this.design.set(idx, this.rand.nextInt(3) + 1);
                }
                else{
                    if(this.design.get(idx) == 0){
                        this.design.set(idx, 1);
                    }
                    else{
                        this.design.set(idx, 0);
                    }
                }
            }
        }
        this.validateDesign();
    }


    // --------------------
    // --- Architecture ---
    // --------------------

    public Architecture getArchitecture(){
        Architecture arch = new Architecture();

        try{
            arch.setVariable( "id", "sm" + 1 );
            arch.setVariable( "payload-assignment", this.getAntennaAssignment() );
            arch.setVariable("payload-allocation", this.getAntennaPartitions() );
            arch.setVariable( "network-type", this.getNetworkTypes() );
            arch.setVariable( "contract-modalities", this.getContractModalities() );
            arch.setVariable("ground-stations", this.getGroundStations() );
            arch.setVariable("fractionation-strategy", this.getFractionationStrategy() );
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        return arch;
    }


    // ---------------
    // --- Helpers ---
    // ---------------


    public NumArray repair_partition(ArrayList<Integer> partition){
        NumArray tmp = new NumArray();

        ArrayList<Integer> helper = new ArrayList<>();
        for(Integer item: partition){
            if(item != 0){
                helper.add(item);
            }
        }
        if(helper.isEmpty()){
            tmp.add(0);
        }
        else{
            // System.out.println("--> Bitstring Helper: " + helper);
            ArrayList<Integer> fixed = BitString.repairPA(helper);
            // System.out.println("--> Bitstring Repair: " + fixed);
            for(Integer var: fixed){
                tmp.add(var);
            }
        }
        return tmp;
    }

    public void print(){
        System.out.println("\n--------- ARCHITECTURE ---------");
        System.out.println("---> ANTENNA ASSIGNMENT:" + this.getAntennaAssignment());
        System.out.println("---> ANTENNA PARTITIONS:" + this.getAntennaPartitions());
        System.out.println("--------> NETWORK TYPES:" + this.getNetworkTypes());
        System.out.println("--> CONTRACT MODALITIES:" + this.getContractModalities());
        System.out.println("------> GROUND STATIONS:" + this.getGroundStations());
        System.out.println("--------> FRAC STRATEGY:" + this.getFractionationStrategy());
        System.out.println(this.design.toString() + "\n");
    }





    public JsonArray getTdrsDesignJson(){
        JsonArray design_obj = new JsonArray();



        return design_obj;
    }



    @Override
    public Solution copy(){
        return new TdrsFullSolution(this);
    }


}
