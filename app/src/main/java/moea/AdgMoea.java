package moea;

import graph.Graph;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.*;
import org.moeaframework.core.comparator.ChainedComparator;
import org.moeaframework.core.comparator.ParetoObjectiveComparator;
import org.moeaframework.core.operator.InjectedInitialization;
import org.moeaframework.core.operator.TournamentSelection;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AdgMoea implements Runnable{


    // -----------------------------
    // ----- SINGLETON PATTERN -----
    // -----------------------------

    private static AdgMoea instance = new AdgMoea();

    public static AdgMoea getInstance() { return instance; }


//     ____        _ _     _
//    |  _ \      (_) |   | |
//    | |_) |_   _ _| | __| | ___ _ __
//    |  _ <| | | | | |/ _` |/ _ \ '__|
//    | |_) | |_| | | | (_| |  __/ |
//    |____/ \__,_|_|_|\__,_|\___|_|



    public static class Builder {

        public Graph graph;
        private int pop_size;
        private int nfe;
        private double crossover_prob;
        private double mutation_prob;
        private int num_objectives;
        private List<Solution> population;


        public Builder(Graph graph){
            this.population = new ArrayList<>();
            this.graph = graph;
        }

        public Builder setProperties(int nfe, double crossover_prob, double mutation_prob, int num_objectives){
            this.nfe = nfe;
            this.crossover_prob = crossover_prob;
            this.mutation_prob = mutation_prob;
            this.num_objectives = num_objectives;
            return this;
        }

        public Builder buildPopulaiton(int pop_size){
            this.pop_size = pop_size;
            this.population = new ArrayList<>(pop_size);
            for(int x = 0; x < this.pop_size; x++){

                AdgSolution solution = new AdgSolution(this.graph, this.num_objectives);
                this.population.add(solution);
            }
            return this;
        }
        private EpsilonMOEA initialize(Problem adg_problem){

            InjectedInitialization initialization = new InjectedInitialization(adg_problem, this.population.size(), this.population);

            double[] epsilonDouble = new double[]{0.001, 1};
            EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilonDouble);

            Population population = new Population();
            ChainedComparator comp = new ChainedComparator(new ParetoObjectiveComparator());
            TournamentSelection selection = new TournamentSelection(2, comp);

            // BUILD: Variation Operator
            AdgCrossover var = new AdgCrossover(this.graph, this.num_objectives, this.mutation_prob);

            return new EpsilonMOEA(adg_problem, population, archive, selection, var, initialization);
        }

        public AdgMoea build(){
            AdgMoea build = new AdgMoea();
            build.graph = graph;
            build.population = this.population;
            build.adg_problem = new AdgProblem(this.graph, this.num_objectives);
            build.num_objectives = this.num_objectives;
            build.moea = this.initialize(build.adg_problem);
            build.nfe = this.nfe;

            AdgMoea.instance = build;
            return build;
        }
    }


    public Graph graph;
    private int pop_size;
    private int nfe;
    private double crossover_prob;
    private double mutation_prob;
    private int num_objectives;
    private EpsilonMOEA     moea;
    private List<Solution> population;
    private Problem adg_problem;




    @Override
    public void run() {


        // --> 1. Create executor service to run
        ExecutorService pool = Executors.newFixedThreadPool(1);
        CompletionService<Algorithm> ecs = new ExecutorCompletionService<>(pool);

        // --> 2. Submit moea run
        ecs.submit(new AdgSearch(this.moea, this.pop_size, this.nfe));

        // --> 3. Join moea
        try {
            org.moeaframework.core.Algorithm alg = ecs.take().get();

            // ANALYZE
            NondominatedPopulation result = alg.getResult();
        }
        catch (InterruptedException | ExecutionException ex) {
            ex.printStackTrace();
        }

        // --> 4. Shutdown
        pool.shutdown();
        System.out.println("DONE");
    }
}
