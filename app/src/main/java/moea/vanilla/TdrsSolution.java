package moea.vanilla;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import graph.Decision;
import graph.chromosome.BitString;
import moea.adg.AdgProblem;
import moea.adg.AdgSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;
import scan.utils.NumArray;
import vanilla.TdrsFormulation;

import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class TdrsSolution extends Solution {

    public ArrayList<Integer> design;
    public boolean already_evaluated = false;
    public String ID;
    public Random rand;


    /*
        Decisions
        - Payload Assignment: 9 bits (3 consts) (3 payloads)
        - Payload Partitioning: 9 bits (1/2/3 possible for each bit)
        - Contract Modalities: 3 bits (0/1 for procurement or contract-modalities)
     */
    public TdrsSolution(int num_objectives){
        super(1, num_objectives, 0);
        this.rand = new Random();

        // --> 1. Create random design
        this.design = new ArrayList<>();
        for(int x = 0; x < 9; x++){
            this.design.add(this.rand.nextInt(2));
        }
        for(int x = 0; x < 9; x++){
            this.design.add(this.rand.nextInt(3) + 1);
        }
        for(int x = 0; x < 3; x++){
            this.design.add(this.rand.nextInt(2));
        }

        // --> 2. Instantiate other variables
        this.ID   = UUID.randomUUID().toString();
        this.already_evaluated = false;

        this.verifyDesign();
        BinaryIntegerVariable var = new BinaryIntegerVariable(0, 0, 10000);
        this.setVariable(0, var);
    }

    public TdrsSolution(int num_objectives, ArrayList<Integer> design){
        super(1, num_objectives, 0);

        System.out.println("----> NEW DESIGN: " + design);

        this.design = design;
        this.rand = new Random();
        this.ID = UUID.randomUUID().toString();
        this.already_evaluated = false;

        this.verifyDesign();
        BinaryIntegerVariable var = new BinaryIntegerVariable(0, 0, 10000);
        this.setVariable(0, var);
    }

    // COPY CONSTRUCTOR
    protected TdrsSolution(Solution solution){
        super(solution);

        TdrsSolution soln = (TdrsSolution) solution;
        this.design = soln.design;
        this.ID = UUID.randomUUID().toString();
        this.rand = soln.rand;
        this.already_evaluated = soln.already_evaluated;

        // this.verifyDesign();
    }

    public void verifyDesign(){

        // --> 1. Check if sat exists
        boolean sat_exists = false;
        for(int x = 0; x < 9; x++){
            if(this.design.get(x) != 0){
                sat_exists = true;
            }
        }

        // --> 2. Verify partitioning decision
        this.verifyPartitions();

        // --> 3. Verify contract modalities
        this.verifyContracts();

        if(!sat_exists){
            // --> If no sats, set evaluated and
            this.setObjective(0, 0);     // Benefit
            this.setObjective(1, 19000); // Cost
            this.already_evaluated = true;
        }
        System.out.println("--> VERIFICATION: " + this.design);
    }

    public void verifyPartitions(){
        int pa_idx = 9;
        int as_idx = 0;
        // x: iterates over constellations
        // y: iterates over antennas
        for(int x = 0; x < 3; x++){
            for(int y = 0; y < 3; y++){
                if(this.design.get(as_idx) == 0){
                    this.design.set(pa_idx, 0);
                }
                else{
                    if(this.design.get(pa_idx) == 0){
                        this.design.set(pa_idx, this.rand.nextInt(3) + 1);
                    }
                }
                pa_idx++;
                as_idx++;
            }
        }
    }

    public void verifyContracts(){
        for(int x = 18; x < 21; x++){
            if(design.get(x) != 0 && design.get(x) != 1){
                design.set(x, this.rand.nextInt(2));
            }
        }
    }




    public void mutateDesign(){

        for(int idx = 0; idx < this.design.size(); idx++){
            if(Decision.getProbabilityResult(1.0 / this.design.size())){
                if(idx < 9){
                    if(this.design.get(idx) == 0){
                        this.design.set(idx, 1);
                    }
                    else{
                        this.design.set(idx, 0);
                    }
                }
                else if(idx < 18){
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
        this.verifyDesign();
    }


    public NumArray getPayloadAssignment(){
        this.verifyDesign();

        NumArray pay_assignment = new NumArray();
        int idx = 0;
        for(int x = 0; x < 3; x++){
            String num = "";
            for(int y = 0; y < 3; y++){
                num += this.design.get(idx);
                idx++;
            }
            System.out.println(num + " " + AdgProblem.baseConversion(num, 2, 10));
            pay_assignment.add(AdgProblem.baseConversion(num, 2, 10));
        }
        return pay_assignment;
    }

    public ArrayList<Long> getPayloadAllocation(){
        this.verifyDesign();

        ArrayList<Long> pay_alloc = new ArrayList<Long>();

        // --> Apply ordering constraints on representation
        int pa_idx = 9;
        for(int x = 0; x < 3; x++) {
            ArrayList<Integer> broken = new ArrayList<>();
            for (int y = 0; y < 3; y++) {
                broken.add(this.design.get(pa_idx));
                pa_idx++;
            }
            pay_alloc.add(this.repair_partition(broken).intArray2LongDec());
        }

        return pay_alloc;
    }

    public String getProcurementInfo(){
        String procurement_string = " ";

        int const_idx = 0;
        for(int x = 18; x < 21; x++){
            if(this.validateConstIdx(const_idx)){
                if(this.design.get(x).equals(0)){
                    procurement_string += " procurement";
                }
                else{
                    procurement_string += " hosted-payloads";
                }
            }
            else{
                procurement_string += " N/A";
            }
            const_idx++;
        }

        return procurement_string;
    }

    private boolean validateConstIdx(int idx){
        int start = idx * 3;
        int count = 0;
        while(count < 3){
            if(this.design.get(start + count) != 0){
                return true;
            }
            count++;
        }
        return false;
    }


    public JsonArray getTdrsDesignJson(){
        JsonArray design_obj = new JsonArray();



        return design_obj;
    }




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
            System.out.println("--> Bitstring Helper: " + helper);
            ArrayList<Integer> fixed = BitString.repairPA(helper);
            System.out.println("--> Bitstring Repair: " + fixed);
            for(Integer var: fixed){
                tmp.add(var);
            }
        }
        return tmp;
    }

    @Override
    public String toString(){
        return this.ID;
    }

    @Override
    public Solution copy(){
        return new TdrsSolution(this);
    }

    @Override
    public boolean equals(Object obj){
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        if(this.ID == ((TdrsSolution) obj).ID){
            return true;
        }
        else{
            return false;
        }
    }
}
