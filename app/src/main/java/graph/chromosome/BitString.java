package graph.chromosome;

import java.util.ArrayList;
import java.util.Random;

public class BitString {


    public static ArrayList<Integer> getEmptyInfo(int length){
        ArrayList<Integer> bit_str = new ArrayList<>();
        for(int x = 0; x < length; x++){
            bit_str.add(-1);
        }
        return bit_str;
    }

    public static ArrayList<Integer> getZeros(int length){
        ArrayList<Integer> bit_str = new ArrayList<>();
        for(int x = 0; x < length; x++){
            bit_str.add(0);
        }
        return bit_str;
    }


    public static ArrayList<Integer> getRandom(int length){
        Random rand = new Random();
        ArrayList<Integer> bit_str = new ArrayList<>();
        boolean valid = false;
        while(!valid){
            bit_str = new ArrayList<>();
            for(int x = 0; x < length; x++){
                if(rand.nextBoolean()){
                    bit_str.add(1);
                }
                else{
                    bit_str.add(0);
                }
            }
            valid = BitString.validate(bit_str);
        }
        return bit_str;
    }

    public static ArrayList<Integer> getRandomSF(int length){
        Random rand = new Random();
        ArrayList<Integer> bit_str = new ArrayList<>();
        for(int x = 0; x < length; x++){
            bit_str.add(0);
        }
        int rand_idx = rand.nextInt(length);
        bit_str.set(rand_idx, 1);
        return bit_str;
    }


    public static boolean validate(ArrayList<Integer> bitstring){
        for(Integer bit: bitstring){
            if(bit.equals(1)){
                return true;
            }
        }
        return false;
    }


    public static String toString(ArrayList<Integer> bitstring){
        String ret = "";
        for(Integer x: bitstring){
            ret += x.toString();
        }
        return ret;
    }

    public static ArrayList<Integer> toArray(String bitstring){
        bitstring = bitstring.replace("\"", "");
        ArrayList<Integer> bit_array = new ArrayList<>();
        for(char ch: bitstring.toCharArray()){
            if(ch == '0'){
                bit_array.add(0);
            }
            else{
                bit_array.add(1);
            }
        }
        return bit_array;
    }




    public static ArrayList<Boolean> assigningUsedToValues(ArrayList<Integer> bitstring, int size_to, int size_from){
        ArrayList<Boolean> values = new ArrayList<>();
        for(int x = 0; x < size_to; x++){
            values.add(false);
        }
        int counter = 0;
        for(int x = 0; x < size_to; x++){
            for(int y = 0; y < size_from; y++){
                Integer bit = bitstring.get(counter);
                if(bit == 1){
                    values.set(x, true);
                }
                counter++;
            }
        }
        return values;
    }


    public static ArrayList<Boolean> assigningUsedFromValues(ArrayList<Integer> bitstring, int size_to, int size_from){
        ArrayList<Boolean> values = new ArrayList<>();
        for(int x = 0; x < size_from; x++){
            values.add(false);
        }
        int counter = 0;
        for(int x = 0; x < size_to; x++){
            for(int y = 0; y < size_from; y++){
                Integer bit = bitstring.get(counter);
                if(bit == 1){
                    values.set(y, true);
                }
                counter++;
            }
        }
        return values;
    }



    public static ArrayList<Integer> constraint_minAssignation(ArrayList<Integer> bitstring, int num_assign_to, int num_assign_from){
        Random rand = new Random();

        // 1. Enforce first constraint
        int idx = 0;
        for(int x = 0; x < num_assign_to; x++){

            boolean assign_to_sat = false;
            ArrayList<Integer> assign_to_indices = new ArrayList<>();
            for(int y = 0; y < num_assign_from; y++){
                Integer bit = bitstring.get(idx);
                if(bit.equals(1)){
                    // This assign_to element has at least one assign_from element assigned to it
                    assign_to_sat = true;
                }
                assign_to_indices.add(idx);
                idx++;
            }
            // Assign 1 to a random bit
            if(!assign_to_sat){
                int rand_idx = assign_to_indices.get(rand.nextInt(assign_to_indices.size()));
                bitstring.set(rand_idx, 1);
            }
        }

        // 2. Enforce second constraint
        for(int x = 0; x < num_assign_from; x++){

            // For each assign_from element, find all its corresponding bit positions in the chromosome
            ArrayList<Integer> bit_positions = new ArrayList<>();
            for(int y = 0; y < num_assign_to; y++){
                int pos = x + (num_assign_from * y);
                bit_positions.add(pos);
            }

            // Check if each assign_from element is assigned to at least one assign_to element
            boolean assign_from_sat = false;
            for(Integer pos: bit_positions){
                Integer bit = bitstring.get(pos);
                if(bit.equals(1)){
                    assign_from_sat = true;
                }
            }

            // Assign 1 to a random bit pos if the constraint isn't satisfied
            if(!assign_from_sat){
                int rand_idx = bit_positions.get(rand.nextInt(bit_positions.size()));
                bitstring.set(rand_idx, 1);
            }
        }

        return bitstring;



    }









}
