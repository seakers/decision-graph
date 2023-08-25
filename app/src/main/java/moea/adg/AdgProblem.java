package moea.adg;

import app.Runs;
import evaluation.TdrsEvaluator;
import graph.Graph;
import moea.vanilla.TdrsFullSolution;
import moea.vanilla.TdrsSolution;
import org.moeaframework.core.Solution;
import org.moeaframework.problem.AbstractProblem;
import evaluation.GNC_Evaluator2;

import java.util.ArrayList;

public class AdgProblem extends AbstractProblem {

    public Graph graph;
    public int num_objectives;

    public GNC_Evaluator2 gnc_evaluator;

    public AdgProblem(Graph graph, int num_objectives){
        super(1, num_objectives);
        this.graph = graph;
        this.num_objectives = num_objectives;
        this.gnc_evaluator = new GNC_Evaluator2();
    }


    @Override
    public void evaluate(Solution sltn){

        // --> 1. Evaluate if not evaluated
        if(!this.isEvaluated(sltn)){
            this.evaluateGncDesign(sltn);
//            this.evaluateTdrsDesign(sltn);
            this.setEvaluated(sltn);
        }
    }

    public void evaluateGncDesign(Solution solution){

        AdgSolution adg_solution = (AdgSolution) solution;
        ArrayList<Double> results = this.gnc_evaluator.evaluate2(adg_solution.getDesign());

        double mass = results.get(0);
        double reliability = results.get(1);

        solution.setObjective(0, -reliability);
        solution.setObjective(1, mass);
    }



    public void evaluateTdrsDesign(Solution solution){


        ArrayList<Double> results;

        // --> EOSS Evaluator

        // --> GN&C Evaluator

        // --> TDRS Evaluator
        if(TdrsEvaluator.getInstance().evaluator == null){
            new TdrsEvaluator.Builder().build();
        }

        // VANILLA CHANGE
        if(Runs.type.equals("ADG")){
            results = TdrsEvaluator.getInstance().evaluateAdg(solution);
        }
        else{
            results = TdrsEvaluator.getInstance().evaluateVanilla(solution);
        }



        double benefit = results.get(0);
        double cost = results.get(1);

        solution.setObjective(0, -benefit);
        solution.setObjective(1, cost);
    }

    public boolean isEvaluated(Solution solution){
        if(solution instanceof AdgSolution){
            return ((AdgSolution) solution).already_evaluated;
        }
        if(solution instanceof TdrsSolution){
            return ((TdrsSolution) solution).already_evaluated;
        }
        return false;
    }

    public void setEvaluated(Solution solution){
        if(solution instanceof AdgSolution){
            ((AdgSolution) solution).already_evaluated = true;
        }
        if(solution instanceof TdrsSolution){
            ((TdrsSolution) solution).already_evaluated = true;
        }
    }




    @Override
    public Solution newSolution(){

        // VANILLA CHANGE
        Solution solution;
        if(Runs.type.equals("ADG")){
            solution = new AdgSolution(this.graph, this.num_objectives);
        }
        else{
            solution = new TdrsFullSolution(this.num_objectives);
        }

        return solution;
    }


    public static Integer baseConversion(String number, int s_base, int d_base){
        // --> For the record this line of code disgusts me
        return Integer.parseInt(Integer.toString( Integer.parseInt(number, s_base), d_base), d_base);
    }
}
