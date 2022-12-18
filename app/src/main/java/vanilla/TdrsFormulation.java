package vanilla;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import evaluation.TdrsEvaluator;
import graph.Decision;

import java.util.ArrayList;
import java.util.Random;

public class TdrsFormulation {


    public ArrayList<ArrayList<Integer>> designs;
    public Gson gson;
    public Random rand;



    public TdrsFormulation(){
        this.designs = new ArrayList<>();
        this.gson = new Gson();
        this.rand = new Random();
    }





    /*
        Decisions
        - Payload Assignment: 9 bits (3 consts) (3 payloads)
        - Payload Partitioning: 9 bits (1/2/3 possible for each bit)
     */
    public int generateRandomDesign(){
        int design_idx = this.designs.size();

        ArrayList<Integer> design = new ArrayList<>();

        for(int x = 0; x < 9; x++){
            design.add(this.rand.nextInt(2));
        }

        for(int x = 0; x < 9; x++){
            design.add(this.rand.nextInt(3) + 1);
        }

        this.designs.add(design);

        return design_idx;
    }

    public int crossoverDesigns(int papa_idx, int mama_idx){
        int design_idx = this.designs.size();

        ArrayList<Integer> papa = this.designs.get(papa_idx);
        ArrayList<Integer> mama = this.designs.get(mama_idx);
        ArrayList<Integer> child = new ArrayList<>();

        // --> Crossover Assigning Bits
        for(int idx = 0; idx < 9; idx++){
            if(this.rand.nextBoolean()){
                child.add(papa.get(idx));
            }
            else{
                child.add(mama.get(idx));
            }
        }

        // --> Mutate design
        child = this.mutateDesign(child);

        this.designs.add(child);

        return design_idx;
    }

    private ArrayList<Integer> mutateDesign(ArrayList<Integer> design){

        for(int idx = 0; idx < design.size(); idx++){
            if(Decision.getProbabilityResult(1.0 / design.size())){
                if(idx < 9){
                    if(design.get(idx) == 0){
                        design.set(idx, 1);
                    }
                    else{
                        design.set(idx, 0);
                    }
                }
                else{
                    design.set(idx, this.rand.nextInt(3) + 1);
                }
            }
        }

        return design;
    }













}
