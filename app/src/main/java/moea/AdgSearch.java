package moea;

import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
import org.moeaframework.algorithm.EpsilonMOEA;
import org.moeaframework.core.Algorithm;
import org.moeaframework.core.Population;
import org.moeaframework.util.TypedProperties;

import java.util.concurrent.Callable;

public class AdgSearch  implements Callable<Algorithm> {

    public Algorithm alg;
    public TypedProperties properties;
    public boolean is_stopped;

    public int pop_size;

    public int nfe;

    public AdgSearch(Algorithm alg, int pop_size, int nfe){
        this.alg = alg;
        this.pop_size = pop_size;
        this.nfe = nfe;
        this.is_stopped = false;
    }


    @Override
    public Algorithm call(){

        // OPERATIONS
        alg.step();

        Population archive = new Population(((AbstractEvolutionaryAlgorithm)alg).getArchive());


        while (!alg.isTerminated() && (alg.getNumberOfEvaluations() < this.nfe) && !this.is_stopped){


            // ALGORITHM STEP
            alg.step();

            // NEW POPULATION
            Population newArchive = ((AbstractEvolutionaryAlgorithm)alg).getArchive();
            // System.out.println("---> Archive size: " + newArchive.size());

            // UPDATE REFERENCE POPULATION
            archive = new Population(newArchive);
        }

        return this.alg;
    }
}
