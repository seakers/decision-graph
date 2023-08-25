package evaluation.reliability.evaluation;


import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

/**
 * This class evaluates the reliability of network containing only source and sink nodes
 */



public class Evaluation_Model_1 {



    private ArrayList<Double> sensors;
    private ArrayList<Double> computers;
    private String            sensor_computer_assignation;
    private double            connection_reliability;


    /**
     *
     * @param source_probs probability of functioning without failure for each of the source nodes
     * @param sink_probs probability  of functioning without failure for each of the sink nodes
     * @param source_sink_assignation binary string encoding assignation of sink nodes to source nodes
     */

    public Evaluation_Model_1(ArrayList<Double> source_probs, ArrayList<Double> sink_probs, String source_sink_assignation){

        this.sensors                     = source_probs;
        this.computers                   = sink_probs;
        this.sensor_computer_assignation = source_sink_assignation;
        this.connection_reliability      = 1;
    }


    private ArrayList<String> generate_component_failures(ArrayList<Double> components){
        int num_components = components.size();
        int[] component_arr = new int[num_components];
        ArrayList<String> component_bit_strings = new ArrayList<>();
        this.generateAllBinaryStrings(num_components, component_arr, 0, component_bit_strings);
        return component_bit_strings;
    }

    private ArrayList<String> generate_component_failures2(ArrayList<Integer> components){
        int num_components = components.size();
        int[] component_arr = new int[num_components];
        ArrayList<String> component_bit_strings = new ArrayList<>();
        this.generateAllBinaryStrings(num_components, component_arr, 0, component_bit_strings);
        return component_bit_strings;
    }

    private ArrayList<String> generate_connection_failures(String component_assignation){

        // 1. Find all indices where component assignation bit == 1
        ArrayList<Integer> active_indices = new ArrayList<>();
        for(int x = 0; x < component_assignation.length(); x++){
            String bit = (component_assignation.charAt(x) + "");
            if(bit.equals("1")){
                active_indices.add(x);
            }
        }

        // 2. Generate all possible connection failures
        ArrayList<String> failure_scenarios = this.generate_component_failures2(active_indices);

        // 3. Integrate possible connection failures into original chromosome and record
        ArrayList<String> connection_failure_chromosomes = new ArrayList<>();
        for(String failure_scenario: failure_scenarios){
            StringBuilder scenario_integration = new StringBuilder(component_assignation);

            for(int x = 0; x < active_indices.size(); x++){
                int idx = active_indices.get(x);
                char bit = failure_scenario.charAt(x);
                scenario_integration.setCharAt(idx, bit);
            }

            connection_failure_chromosomes.add(scenario_integration.toString());
        }

        return connection_failure_chromosomes;
    }

    private void generateAllBinaryStrings(int n, int arr[], int i, ArrayList<String> bit_strings) {
        if (i == n)
        {
            String bit_string = "";
            for (int c = 0; c < n; c++)
            {
                bit_string += Integer.toString(arr[c]);
            }
            bit_strings.add(bit_string);
            return;
        }

        arr[i] = 0;
        this.generateAllBinaryStrings(n, arr, i + 1, bit_strings);

        arr[i] = 1;
        this.generateAllBinaryStrings(n, arr, i + 1, bit_strings);
    }




    private double component_failure_probability(ArrayList<Double> probabilities, String bit_string){
        double prob = 1;

        for(int x = 0; x < bit_string.length(); x++){
            String bit = (bit_string.charAt(x) + "");
            if(bit.equals("1")){
                prob = prob * probabilities.get(x);
            }
            else{
                prob = prob * (1 - probabilities.get(x));
            }
        }
        return prob;
    }

    private SimpleMatrix get_failure_matrix(String bitstring, int num_rows, int num_cols){
        double[][] matrix = new double[num_rows][num_cols];

        int counter = 0;
        for(int row = 0; row < num_rows; row++){

            for(int col = 0; col < num_cols; col++){
                String bit = (bitstring.charAt(counter) + "");
                if(bit.equals("1")){
                    matrix[row][col] = 1;
                }
                else{
                    matrix[row][col] = 0;
                }
                counter++;
            }
        }
        return (new SimpleMatrix(matrix));
    }

    private double connection_failure_probability(String connection_bit_string, String failure_bit_string){
        double prob = 1;

        for(int x = 0; x < connection_bit_string.length(); x++){
            String orig_bit = (connection_bit_string.charAt(x) + "");
            String new_bit  = (failure_bit_string.charAt(x) + "");

            // Check if there was an original connection
            if(orig_bit.equals("1")){

                // Connection passed
                if(new_bit.equals("1")){
                    prob *= (this.connection_reliability);
                }

                // Connection failed - returned prob is automatically 0
                else{
                    prob *= (1 - this.connection_reliability);
                }

            }
        }

        return prob;
    }

    private int determine_system_failure(SimpleMatrix sensor_failure, SimpleMatrix computer_failure, SimpleMatrix connection_failure){

        SimpleMatrix first_result  = sensor_failure.mult(connection_failure);
        SimpleMatrix result_matrix = first_result.mult(computer_failure);

        double result = result_matrix.get(0, 0);
        if(result > 0.0){
            return 1;
        }
        else{
            return 0;
        }
    }

    private double transform_reliability(double reliability){

        double result = -(Math.log10(1.0-reliability));
        return result;
    }


    /**
     *
     * @param transform if true, the network reliability is returned in terms of "number of 9s"
     * @return the reliability of the network
     */
    public double evaluate(boolean transform){
        double reliability = this.evaluate_reliability();
        if(transform){
            return this.transform_reliability(reliability);
        }
        return reliability;
    }

    private double evaluate_reliability(){

        double result_reliability = 0;

        double prob_conneciton_success = 1;

        ArrayList<String> sensor_failures = this.generate_component_failures(sensors);

        ArrayList<String> computer_failures = this.generate_component_failures(computers);

        ArrayList<String> connection_failures = this.generate_connection_failures(sensor_computer_assignation);

        int counter = 0;
        for(String sensor_failure: sensor_failures){

            double       prob_s                = this.component_failure_probability(sensors, sensor_failure);
            SimpleMatrix sensor_failure_matrix = this.get_failure_matrix(sensor_failure, 1, sensors.size());
            // 1xn matrix

            for(String computer_failure: computer_failures){

                double       prob_c                  = this.component_failure_probability(computers, computer_failure);
                SimpleMatrix computer_failure_matrix = this.get_failure_matrix(computer_failure, computers.size(), 1);
                // 1xm matrix

                for(String connection_failure: connection_failures){

                    // If any of the connections fail prob_i = 0
                    double       prob_i                    = this.connection_failure_probability(sensor_computer_assignation, connection_failure);
                    SimpleMatrix connection_failure_matrix = this.get_failure_matrix(connection_failure, sensors.size(), computers.size());
                    // nxm matrix

                    double system_status = (double) this.determine_system_failure(sensor_failure_matrix, computer_failure_matrix, connection_failure_matrix);

                    result_reliability += (prob_s*prob_c*prob_i*system_status);

                    // System.out.println("--> RELIABILITY ITR: " + prob_s + " " + prob_c + " " + prob_i + " " + system_status);

                    counter++;
                }
            }

        }

        return result_reliability;
    }










}
