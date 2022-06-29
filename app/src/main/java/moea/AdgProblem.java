package moea;

import graph.Graph;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;

import java.util.Random;

public class AdgProblem extends AbstractProblem {

    public Graph graph;
    public int num_objectives;

    public AdgProblem(Graph graph, int num_objectives){
        super(1, num_objectives);
        this.graph = graph;
        this.num_objectives = num_objectives;
    }


    @Override
    public void evaluate(Solution sltn){

        // --> 1. Cast to AdgSolution
        AdgSolution solution = (AdgSolution) sltn;

        // --> 2. Evaluate if not evaluated
        if(!solution.already_evaluated){
            this.evaluateDesign(solution);
        }
    }

    public void evaluateDesign(AdgSolution solution){
        Random rand = new Random();

        double benefit = rand.nextInt(50);
        double cost = rand.nextInt(2000);

        solution.setObjective(0, -benefit);
        solution.setObjective(1, cost);
        solution.already_evaluated = true;
    }










    @Override
    public Solution newSolution(){
        return new AdgSolution(this.graph, this.num_objectives);
    }
}
