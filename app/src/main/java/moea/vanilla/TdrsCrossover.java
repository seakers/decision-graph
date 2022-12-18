package moea.vanilla;

import moea.adg.AdgSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;
import moea.vanilla.TdrsFullSolution;

import java.util.ArrayList;
import java.util.Random;

public class TdrsCrossover implements Variation {


    private int num_objectives;

    public TdrsCrossover(int num_objectives){
        this.num_objectives = num_objectives;
    }

    @Override
    public Solution[] evolve(Solution[] parents){
        Random rand = new Random();

        // TWO PARENTS FOR CROSSOVER
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        // CAST APPROPRIATELY
        TdrsFullSolution res1 = (TdrsFullSolution) result1;
        TdrsFullSolution res2 = (TdrsFullSolution) result2;

        ArrayList<Integer> papa = res1.design;
        ArrayList<Integer> mama = res2.design;
        ArrayList<Integer> child = new ArrayList<>();

        // CROSSOVER
        for(int idx = 0; idx < papa.size(); idx++){
            if(rand.nextBoolean()){
                child.add(papa.get(idx));
            }
            else{
                child.add(mama.get(idx));
            }
        }

        // CREATE CHILD
        // RANDOM CHANGE
        // TdrsFullSolution child_design = new TdrsFullSolution(this.num_objectives);
        TdrsFullSolution child_design = new TdrsFullSolution(this.num_objectives, child);
        child_design.mutateDesign();

        // RETURN CHILD
        Solution[] soln = new Solution[] { child_design };
        return soln;
    }

    @Override
    public int getArity(){
        return 2;
    }
}
