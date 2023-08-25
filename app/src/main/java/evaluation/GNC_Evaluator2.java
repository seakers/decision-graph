package evaluation;

import com.google.gson.*;
import evaluation.reliability.evaluation.Evaluation_Model_2;
import graph.chromosome.DesignBuilder;

import java.util.*;

public class GNC_Evaluator2 {


    public HashMap<String, Double> sensor_types;
    public HashMap<String, Double> computer_types;
    public HashMap<String, Double> actuator_types;

    public HashMap<String, Double> mass_properties;



    public GNC_Evaluator2(){

        this.sensor_types = new HashMap<>();
        this.sensor_types.put("s1", 0.9985);
        this.sensor_types.put("s2", 0.999);
        this.sensor_types.put("s3", 0.9995);

        this.computer_types = new HashMap<>();
        this.computer_types.put("c1", 0.999);
        this.computer_types.put("c2", 0.9996);
        this.computer_types.put("c3", 0.9998);

        this.actuator_types = new HashMap<>();
        this.actuator_types.put("a1", 0.9992);
        this.actuator_types.put("a2", 0.998);
        this.actuator_types.put("a3", 0.999);


        this.mass_properties = new HashMap<>();

        this.mass_properties.put("s1", 3.0);
        this.mass_properties.put("s2", 6.0);
        this.mass_properties.put("s3", 9.0);

        this.mass_properties.put("c1", 3.0);
        this.mass_properties.put("c2", 5.0);
        this.mass_properties.put("c3", 10.0);

        this.mass_properties.put("a1", 3.5);
        this.mass_properties.put("a2", 5.5);
        this.mass_properties.put("a3", 9.5);


    }

    public JsonArray parseDesignString(String design){
        return (new JsonParser().parse(design).getAsJsonArray());
    }


    public double get_component_probability(String component){
        String prefix = component.substring(0, 2);
        if(this.sensor_types.keySet().contains(prefix)){
            return this.sensor_types.get(prefix);
        }
        if(this.computer_types.keySet().contains(prefix)){
            return this.computer_types.get(prefix);
        }
        if(this.actuator_types.keySet().contains(prefix)){
            return this.actuator_types.get(prefix);
        }
        System.out.println("--> COULD NOT GET COMPONENT PROBABILITY: " + prefix);
        System.exit(0);
        return 0;
    }

    public double get_component_mass(String component){
        String prefix = component.substring(0, 2);
        return this.mass_properties.get(prefix);
    }





    public ArrayList<String> get_actuators2(JsonObject design){
        ArrayList<String> actuators = new ArrayList<>();
        JsonArray actuators_ary = design.getAsJsonArray("actuator_count")
                .get(0)
                .getAsJsonObject()
                .getAsJsonArray("actuators");

        for(JsonElement actuator_obj: actuators_ary){
            String actuator_type = actuator_obj.getAsJsonObject()
                    .getAsJsonArray("actuator_type")
                    .get(0).getAsJsonObject().get("name").getAsString();
            String actuator_uid = actuator_obj.getAsJsonObject()
                    .get("uid").getAsString();
            actuators.add(actuator_type + "-" + actuator_uid);
        }
        return actuators;
    }

    public ArrayList<String> get_computers2(JsonObject design){
        JsonObject results = new JsonObject();
        DesignBuilder.referenceSearch(design, "computers", results, false);
        ArrayList<String> computers = new ArrayList<>();
        Set<String> actuator_uids = results.keySet();
        for(String uid: actuator_uids){
            for(JsonElement computer_obj: results.getAsJsonArray(uid)){
                String computer_type = computer_obj.getAsJsonObject()
                        .getAsJsonArray("computer_type")
                        .get(0).getAsJsonObject().get("name").getAsString();
                String computer_uid = computer_obj.getAsJsonObject()
                        .get("uid").getAsString();
                String computer_name = computer_type + "-" + computer_uid;
                if(!computers.contains(computer_name)){
                    computers.add(computer_name);
                }
            }
        }
        return computers;
    }

    public ArrayList<String> get_sensors2(JsonObject design){
        JsonObject results = new JsonObject();
        DesignBuilder.referenceSearch(design, "sensors", results, false);
        ArrayList<String> sensors = new ArrayList<>();
        Set<String> computer_uids = results.keySet();
        for(String uid: computer_uids){
            for(JsonElement sensor_obj: results.getAsJsonArray(uid)){
                String sensor_type = sensor_obj.getAsJsonObject()
                        .getAsJsonArray("sensor_type")
                        .get(0).getAsJsonObject().get("name").getAsString();
                String sensor_uid = sensor_obj.getAsJsonObject()
                        .get("uid").getAsString();
                String sensor_name = sensor_type + "-" + sensor_uid;
                if(!sensors.contains(sensor_name)){
                    sensors.add(sensor_name);
                }
            }
        }
        return sensors;
    }

    public String get_sensors_to_computers(JsonObject design, ArrayList<String> sensors, ArrayList<String> computers){
        String bit_str = "";
        JsonObject results = new JsonObject();
        DesignBuilder.referenceSearch(design, "sensors", results, false);
        for(String computer: computers){
            String computer_uid = computer.split("-")[1];
            JsonArray computer_sensors = results.getAsJsonArray(computer_uid);
            ArrayList<String> computer_sensor_uids = new ArrayList<>();
            for(JsonElement sensor_obj: computer_sensors){
                computer_sensor_uids.add(sensor_obj.getAsJsonObject().get("uid").getAsString());
            }
            for(String sensor: sensors){
                String sensor_uid = sensor.split("-")[1];
                if(computer_sensor_uids.contains(sensor_uid)){
                    bit_str += "1";
                }
                else{
                    bit_str += "0";
                }
            }
        }
        return bit_str;
    }

    public String get_computers_to_actuators(JsonObject design, ArrayList<String> computers, ArrayList<String> actuators){
        String bit_str = "";
        JsonObject results = new JsonObject();
        DesignBuilder.referenceSearch(design, "computers", results, false);
        for(String actuator: actuators){
            String actuator_uid = actuator.split("-")[1];
            JsonArray actuator_computers = results.getAsJsonArray(actuator_uid);
            ArrayList<String> actuator_computer_uids = new ArrayList<>();
            for(JsonElement sensor_obj: actuator_computers){
                actuator_computer_uids.add(sensor_obj.getAsJsonObject().get("uid").getAsString());
            }
            for(String computer: computers){
                String computer_uid = computer.split("-")[1];
                if(actuator_computer_uids.contains(computer_uid)){
                    bit_str += "1";
                }
                else{
                    bit_str += "0";
                }
            }
        }
        return bit_str;
    }







    public ArrayList<String> get_actuators(JsonArray design){
        ArrayList<String> actuators = new ArrayList<>();
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'a'){
                actuators.add(component_name);
            }
        }
        Collections.sort(actuators);
        return actuators;
    }

    public ArrayList<String> get_computers(JsonArray design){
        ArrayList<String> computers = new ArrayList<>();
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'c'){
                computers.add(component_name);
            }
        }
        Collections.sort(computers);
        return computers;
    }

    public ArrayList<String> get_sensors(JsonArray design){
        ArrayList<String> sensors = new ArrayList<>();
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            String component_name = component.get("name").getAsString();
            if(component_name.charAt(0) == 'c'){
                JsonArray sub_components = component.getAsJsonArray("elements");
                for(int y = 0; y < sub_components.size(); y++){
                    JsonObject sub_component = sub_components.get(y).getAsJsonObject();
                    String sub_component_name = sub_component.get("name").getAsString();
                    if(!sensors.contains(sub_component_name)){
                        sensors.add(sub_component_name);
                    }
                }
            }
        }
        Collections.sort(sensors);
        return sensors;
    }



    public boolean does_component_have_assignation(JsonArray design, String component_to, String component_from){
        for(int x = 0; x < design.size(); x++){
            JsonObject component = design.get(x).getAsJsonObject();
            if(component.get("name").getAsString().equals(component_to)){
                JsonArray sub_components = component.getAsJsonArray("elements");
                for(int y = 0; y < sub_components.size(); y++){
                    JsonObject sub_component = sub_components.get(y).getAsJsonObject();
                    if(sub_component.get("name").getAsString().equals(component_from)){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public String get_component_assignation(JsonArray design, ArrayList<String> sensors, ArrayList<String> computers){

        String topology = "";
        for(String computer: computers){
            for(String sensor: sensors){
                if(this.does_component_have_assignation(design, computer, sensor)){
                    topology += "1";
                }
                else{
                    topology += "0";
                }
            }
        }

        return topology;
    }

    public ArrayList<Double> get_probability_list(ArrayList<String> components){
        ArrayList<Double> probabilities = new ArrayList<>();
        for(String component: components){
            probabilities.add(this.get_component_probability(component));

        }
        return probabilities;
    }




    public ArrayList<Double> evaluate2(JsonObject design_obj){
        double connection_success_rate = 1;
        ArrayList<Double> results = new ArrayList<>();

        ArrayList<String> actuators = this.get_actuators2(design_obj);
        ArrayList<String> computers = this.get_computers2(design_obj);
        ArrayList<String> sensors = this.get_sensors2(design_obj);

        String sensor_to_computer = this.get_sensors_to_computers(design_obj, sensors, computers);
        String computer_to_actuator = this.get_computers_to_actuators(design_obj, computers, actuators);

        ArrayList<Double> actuator_probs = this.get_probability_list(actuators);
        ArrayList<Double> computer_probs = this.get_probability_list(computers);
        ArrayList<Double> sensor_probs = this.get_probability_list(sensors);

//        System.out.println(actuators);
//        System.out.println(actuator_probs);
//        System.out.println(computers);
//        System.out.println(computer_probs);
//        System.out.println(sensors);
//        System.out.println(sensor_probs);
//        System.out.println(sensor_to_computer);
//        System.out.println(computer_to_actuator);

        Evaluation_Model_2 model = new Evaluation_Model_2.Builder(sensor_probs, computer_probs, actuator_probs, connection_success_rate)
                .connection_sensor_to_computer(sensor_to_computer)
                .connection_computer_to_actuator(computer_to_actuator)
                .build();

        double reliability = model.evaluate_reliability(true);
        double mass = this.evaluate_mass(sensors, computers, actuators);
        results.add(mass);
        results.add(reliability);
//        System.out.println("---> MASS: " + mass);
//        System.out.println("---> RELIABILITY: " + reliability);
        return results;
    }




    public ArrayList<Double> evaluate(String design_str){
        double connection_success_rate = 1;

        System.out.println("\n\n---> EVALUATING DESIGN");
        System.out.println(design_str);

        ArrayList<Double> results = new ArrayList<>();

        JsonArray design = this.parseDesignString(design_str);

        ArrayList<String> actuators = this.get_actuators(design);
        ArrayList<String> computers = this.get_computers(design);
        ArrayList<String> sensors = this.get_sensors(design);

        System.out.println(actuators);
        System.out.println(computers);
        System.out.println(sensors);

        String sensor_to_computer = this.get_component_assignation(design, sensors, computers);
        String computer_to_actuator = this.get_component_assignation(design, computers, actuators);

        ArrayList<Double> actuator_probs = this.get_probability_list(actuators);
        ArrayList<Double> computer_probs = this.get_probability_list(computers);
        ArrayList<Double> sensor_probs = this.get_probability_list(sensors);


        Evaluation_Model_2 model = new Evaluation_Model_2.Builder(sensor_probs, computer_probs, actuator_probs, connection_success_rate)
                .connection_sensor_to_computer(sensor_to_computer)
                .connection_computer_to_actuator(computer_to_actuator)
                .build();

        double reliability = model.evaluate_reliability(true);
        double mass = this.evaluate_mass(sensors, computers, actuators);

        results.add(reliability);
        results.add(mass);


        System.out.println("---> MASS: " + mass);
        System.out.println("---> RELIABILITY: " + reliability);


        return results;
    }


    public double evaluate_mass(ArrayList<String> sensors, ArrayList<String> computers, ArrayList<String> actuators){
        double dissimilar_component_penalty = 5/3;
        double mass = 0;

        for(String sensor: sensors){
            mass += this.get_component_mass(sensor);
        }

        for(String computer: computers){
            mass += this.get_component_mass(computer);
        }

        for(String actuator: actuators){
            mass += this.get_component_mass(actuator);
        }

        if(this.is_heterogeneous(sensors, computers, actuators)){
            mass += dissimilar_component_penalty;
        }

        return mass;
    }

    public boolean is_heterogeneous(ArrayList<String> sensors, ArrayList<String> computers, ArrayList<String> actuators){

        // SENSORS
        Set<String> sensors_shrt = new HashSet<>(sensors);
        if(sensors_shrt.size() == 1){
            Set<String> computers_shrt = new HashSet<>(computers);
            if(computers_shrt.size() == 1){
                Set<String> actuators_shrt = new HashSet<>(actuators);
                if(actuators_shrt.size() == 1){
                    return false;
                }
            }
        }
        return true;
    }

















}
