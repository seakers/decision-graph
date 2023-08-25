package evaluation.reliability.evaluation;


import evaluation.reliability.utils.BitOperations;
import org.ejml.simple.SimpleMatrix;

import java.util.ArrayList;

public class Evaluation_Model_2 {

    private ArrayList<Double> sensors;
    private ArrayList<Double> computers;
    private ArrayList<Double> actuators;

    private Double connection_failure_probability;

    private SimpleMatrix sensor_computer_assignation;
    private SimpleMatrix computer_actuator_assignation;


    private ArrayList<SimpleMatrix> sensor_failures;   // 1 x (sensors.size())
    private ArrayList<SimpleMatrix> computer_failures; // 1 x (computers.size())
    private ArrayList<SimpleMatrix> actuator_failures; // 1 x (actuators.size())

    private ArrayList<SimpleMatrix> sensor_computer_connection_failures;   // (sensors.size())   x (computers.size())
    private ArrayList<SimpleMatrix> computer_actuator_connection_failures; // (computers.size()) x (actuators.size())


    public static class Builder{

        private ArrayList<Double> sensors;
        private ArrayList<Double> computers;
        private ArrayList<Double> actuators;

        private Double connection_failure_probability;

        private SimpleMatrix sensor_computer_assignation;
        private SimpleMatrix computer_actuator_assignation;

        private ArrayList<SimpleMatrix> sensor_failures;   // 1 x (sensors.size())
        private ArrayList<SimpleMatrix> computer_failures; // 1 x (computers.size())
        private ArrayList<SimpleMatrix> actuator_failures; // 1 x (actuators.size())

        private ArrayList<SimpleMatrix> sensor_computer_connection_failures;   // (sensors.size())   x (computers.size())
        private ArrayList<SimpleMatrix> computer_actuator_connection_failures; // (computers.size()) x (actuators.size())

        private BitOperations bitops;


        public Builder(ArrayList<Double> sensors, ArrayList<Double> computers, ArrayList<Double> actuators, Double connection_failure_probability){
            this.bitops    = new BitOperations();

            this.sensors   = sensors;
            this.computers = computers;
            this.actuators = actuators;

            this.sensor_failures   = this.generate_component_failures(sensors);
            this.computer_failures = this.generate_component_failures(computers);
            this.actuator_failures = this.generate_component_failures(actuators);

            this.connection_failure_probability = connection_failure_probability;
        }

        private ArrayList<SimpleMatrix> generate_component_failures(ArrayList<Double> component){
            ArrayList<SimpleMatrix> failure_matricies = new ArrayList<>();

            ArrayList<String> failure_scenarios = this.bitops.enumerate_binary_strings(component.size());
            for(String failure_scenario: failure_scenarios){
                double[][] matrix = new double[1][component.size()];
                char[] bit_ary = failure_scenario.toCharArray();

                int counter = 0;
                for(char bit: bit_ary){
                    if(Character.compare(bit, '1') == 0){
                        matrix[0][counter] = 1;
                    }
                    else{
                        matrix[0][counter] = 0;
                    }
                    counter++;
                }

                failure_matricies.add(new SimpleMatrix(matrix));
            }
            return failure_matricies;
        }


        public Builder connection_sensor_to_computer(String bit_string){
            if(bit_string.length() != (this.sensors.size() * this.computers.size())){
                System.out.println("Invalid sensor to computer connection length");
                System.exit(0);
            }

            this.sensor_computer_assignation = this.get_failure_matrix(bit_string, this.sensors.size(), this.computers.size());

            this.sensor_computer_connection_failures = this.generate_connection_failures(bit_string, this.sensors.size(), this.computers.size());
            return this;
        }

        public Builder connection_computer_to_actuator(String bit_string){
            if(bit_string.length() != (this.computers.size() * this.actuators.size())){
                System.out.println("Invalid computer to actuator connection length");
                System.exit(0);
            }

            this.computer_actuator_assignation = this.get_failure_matrix(bit_string, this.computers.size(), this.actuators.size());

            this.computer_actuator_connection_failures = this.generate_connection_failures(bit_string, this.computers.size(), this.actuators.size());
            return this;
        }

        private ArrayList<SimpleMatrix> generate_connection_failures(String bit_string, int rows, int cols){
            ArrayList<SimpleMatrix> failure_scenarios    = new ArrayList<>();
            ArrayList<String> connection_failure_strings = this.generate_connection_failure_strings(bit_string);

            for(String connection_failure_string: connection_failure_strings){
                failure_scenarios.add(this.get_failure_matrix(connection_failure_string, rows, cols));
            }

            return failure_scenarios;
        }

        private ArrayList<String> generate_connection_failure_strings(String component_assignation){
            // 1. Find all indices where component assignation bit == 1
            ArrayList<Integer> active_indices = new ArrayList<>();

            int counter = 0;
            for(int x = 0; x < component_assignation.length(); x++){
                String bit = (component_assignation.charAt(x) + "");
                if(bit.equals("1")){
                    active_indices.add(x);
                    counter++;
                }
            }

            // 2. Generate all possible connection failures
            ArrayList<String> failure_scenarios = this.bitops.enumerate_binary_strings(counter);

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

        public Evaluation_Model_2 build(){
            Evaluation_Model_2 model = new Evaluation_Model_2();
            model.sensors   = this.sensors;
            model.computers = this.computers;
            model.actuators = this.actuators;

            model.connection_failure_probability = this.connection_failure_probability;

            model.sensor_computer_assignation   = this.sensor_computer_assignation;
            model.computer_actuator_assignation = this.computer_actuator_assignation;

            model.sensor_failures   = this.sensor_failures;
            model.computer_failures = this.computer_failures;
            model.actuator_failures = this.actuator_failures;

            model.sensor_computer_connection_failures   = this.sensor_computer_connection_failures;
            model.computer_actuator_connection_failures = this.computer_actuator_connection_failures;

            return model;
        }
    }

    public double evaluate_reliability(boolean transform){
        double reliability = this.full_factorial_reliability_evaluation();

        if(transform){
            return this.transform_reliability(reliability);
        }
        return reliability;
    }


    public double full_factorial_reliability_evaluation(){
        double reliability = 0;

        // 1. Iterate over possible sensor failures
        for(SimpleMatrix sensor_failure: this.sensor_failures){
            double prob_sensor_failure = this.component_failure_probability(this.sensors, sensor_failure);

            // 2. Iterate over possible connection failures
            for(SimpleMatrix sensor_computer_connection_failure: this.sensor_computer_connection_failures){
                double prob_sensor_computer_connection_failure = this.connection_failure_probability(this.sensor_computer_assignation, sensor_computer_connection_failure);

                // 3. Iterate over possible computer failures
                for(SimpleMatrix computer_failure: this.computer_failures){
                    double prob_computer_failure = this.component_failure_probability(this.computers, computer_failure);

                    // 4. Iterate over possible connection failures
                    for(SimpleMatrix computer_actuator_connection_failure: this.computer_actuator_connection_failures){
                        double prob_computer_actuator_connection_failure = this.connection_failure_probability(this.computer_actuator_assignation, computer_actuator_connection_failure);

                        // 5. Iterate over possible actuator failures
                        for(SimpleMatrix actuator_failure: this.actuator_failures){
                            double prob_actuator_failure = this.component_failure_probability(this.actuators, actuator_failure);

                            boolean system_status = this.evaluate_system_failure(
                                    sensor_failure,
                                    sensor_computer_connection_failure,
                                    computer_failure,
                                    computer_actuator_connection_failure,
                                    actuator_failure
                            );

                            if(system_status){
//                                System.out.println("\n\n--------");
//                                System.out.println(prob_sensor_failure);
//                                System.out.println(prob_sensor_computer_connection_failure);
//                                System.out.println(prob_computer_failure);
//                                System.out.println(prob_computer_actuator_connection_failure);
//                                System.out.println(prob_actuator_failure);
//                                System.out.println("--------\n\n");


                                reliability += (prob_sensor_failure*prob_sensor_computer_connection_failure*prob_computer_failure*prob_computer_actuator_connection_failure*prob_actuator_failure);
                            }
                        }
                    }
                }
            }
        }

        return reliability;
    }

    // Determines if the system failed
    private boolean evaluate_system_failure(SimpleMatrix sensor_failure,
                                            SimpleMatrix sensor_computer_connection_failure,
                                            SimpleMatrix computer_failure,
                                            SimpleMatrix computer_actuator_connection_failure,
                                            SimpleMatrix actuator_failure) {

        SimpleMatrix sensor_connection_status   = sensor_failure.mult(sensor_computer_connection_failure);
        SimpleMatrix computer_status            = this.vector_multiplication(sensor_connection_status, computer_failure);
        SimpleMatrix computer_connection_status = computer_status.mult(computer_actuator_connection_failure);
        SimpleMatrix actuator_status            = computer_connection_status.mult(actuator_failure.transpose());

        double result = actuator_status.get(0, 0);
        if(result > 0.0){
            return true;
        }
        else{
            return false;
        }
    }


    // Both matrix_a and matrix_b will be 1 x n size
    private SimpleMatrix vector_multiplication(SimpleMatrix matrix_a, SimpleMatrix matrix_b){
        double[][] matrix = new double[1][matrix_a.numCols()];

        for(int x = 0; x < matrix_a.numCols(); x++){
            matrix[0][x] = matrix_a.get(0, x) * matrix_b.get(0, x);
        }

        return (new SimpleMatrix(matrix));
    }


    private double connection_failure_probability(SimpleMatrix assignation_matrix, SimpleMatrix failure_matrix){
        double prob = 1;

        for(int row = 0; row < assignation_matrix.numRows(); row++){
            for(int col = 0; col < assignation_matrix.numCols(); col++){
                if(assignation_matrix.get(row, col) != 0){
                    if(failure_matrix.get(row,col) != 0){
                        prob = prob * this.connection_failure_probability;
                    }
                    else{
                        prob = prob * (1 - this.connection_failure_probability);
                    }
                }
            }
        }

        return prob;
    }



    private double component_failure_probability(ArrayList<Double> probabilities, SimpleMatrix bit_matrix){
        double prob = 1;

        for(int x = 0; x < probabilities.size(); x++){
            Double bit = bit_matrix.get(0, x);
            if(bit.equals(1.0)){
                prob = prob * probabilities.get(x);
            }
            else{
                prob = prob * (1 - probabilities.get(x));
            }
        }

        return prob;
    }


    private double transform_reliability(double reliability){

        double result = -(Math.log10(1.0-reliability));
        return result;
    }









}


