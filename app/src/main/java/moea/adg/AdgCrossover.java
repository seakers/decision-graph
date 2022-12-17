package moea.adg;

import graph.Graph;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variation;

public class AdgCrossover implements Variation {

    private Graph graph;
    private int num_objectives;
    private double mutation_prob = 0.5;


    public AdgCrossover(Graph graph, int num_objectives){
        this.graph = graph;
        this.num_objectives = num_objectives;
    }

    @Override
    public Solution[] evolve(Solution[] parents){

        // TWO PARENTS FOR CROSSOVER
        Solution result1 = parents[0].copy();
        Solution result2 = parents[1].copy();

        // CAST APPROPRIATELY
        AdgSolution res1 = (AdgSolution) result1;
        AdgSolution res2 = (AdgSolution) result2;

        // CROSSOVER
        int child_id = -1;
        try {
            // CROSSOVER VS RANDOM
            child_id = this.graph.crossoverDesigns(res1.design_idx, res2.design_idx, this.mutation_prob);
            // child_id = this.graph.generateRandomDesign();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // CREATE CHILD
        AdgSolution child = new AdgSolution(this.graph, this.num_objectives, child_id);

        // RETURN CHILD
        Solution[] soln = new Solution[] { child };
        return soln;
    }

    // NUM PARENTS REQUIRED
    @Override
    public int getArity(){
        return 2;
    }

}
