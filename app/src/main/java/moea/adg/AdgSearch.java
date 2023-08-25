package moea.adg;

import app.Runs;
import org.moeaframework.Analyzer;
import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.analysis.collector.Accumulator;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.core.Solution;
import org.moeaframework.util.TypedProperties;

import java.util.concurrent.Callable;

public class AdgSearch  implements Callable<Algorithm> {

    public Algorithm alg;
    public boolean is_stopped;
    public int pop_size;
    public int nfe;
    public int run_num;


    // --> Algorithm Analysis
    private Analyzer analyzer;
    private Accumulator accumulator;

    public AdgSearch(Algorithm alg, int pop_size, int nfe, int run_num){
        this.alg = alg;
        this.pop_size = pop_size;
        this.nfe = nfe;
        this.is_stopped = false;
        this.analyzer = new Analyzer()
                .withProblem(this.alg.getProblem())
                .withIdealPoint(-10.1, -0.1)
                .withReferencePoint(0, 100)
                .includeHypervolume()
                .includeAdditiveEpsilonIndicator();
        this.accumulator = new Accumulator();
        this.run_num = run_num;
    }


    @Override
    public Algorithm call(){

        // OPERATIONS

        // --> First step evaluates the starting population (NFE = 30)
        alg.step();
        this.updateAnalysis();

        // Population current_pop = new Population(((AbstractEvolutionaryAlgorithm)alg).getArchive());
        int step_count = 0;
        Population current_pop = new Population(((AbstractEvolutionaryAlgorithm)this.alg).getPopulation());
        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < this.nfe) && !this.is_stopped){
            System.out.println("--> ALGORITHM STEP/NFE: " + Integer.toString(step_count) + " | " + Integer.toString(alg.getNumberOfEvaluations()));
            step_count = step_count + 1;
            // --> Step algorithm
            alg.step();

            // --> Get new population / update
            Population new_pop = ((AbstractEvolutionaryAlgorithm)alg).getPopulation();
//            if(this.new_design_check(current_pop, new_pop)){
//                System.out.println("--> NEW DESIGN FOUND");
//            }
            current_pop = new Population(new_pop);

            // --> Record metrics
            this.updateAnalysis();
        }

        // --> Record run
        Population final_pop = ((AbstractEvolutionaryAlgorithm) alg).getPopulation();
        Runs.writeRun(this.analyzer, this.accumulator, final_pop, this.run_num);

        return this.alg;
    }

    private void updateAnalysis(){
        this.analyzer.add("popADD", this.alg.getResult());
        int num_evals = this.alg.getNumberOfEvaluations();
        if(this.analyzer.getAnalysis().get("popADD") != null){
            double current_hv = this.analyzer.getAnalysis().get("popADD").get("Hypervolume").getMax();
            this.accumulator.add("NFE", (num_evals));
            this.accumulator.add("HV", current_hv);
        }
    }










    public boolean new_design_check(Population old_pop, Population new_pop){
        for (int i = 0; i < new_pop.size(); ++i){
            Solution newSol       = new_pop.get(i);
            if(!old_pop.contains(newSol)){
                return true;
            }
        }
        return false;
    }
}
