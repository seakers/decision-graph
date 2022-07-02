package moea.adg;


import com.google.gson.JsonObject;
import graph.Graph;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.BinaryIntegerVariable;

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
