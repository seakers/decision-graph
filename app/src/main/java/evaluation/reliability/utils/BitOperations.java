package evaluation.reliability.utils;


import java.util.ArrayList;

public class BitOperations {


    public BitOperations(){

    }


    public ArrayList<String> enumerate_binary_strings(int length){
        int num_components = length;
        int[] component_arr = new int[num_components];
        ArrayList<String> component_bit_strings = new ArrayList<>();
        this.generateAllBinaryStrings(num_components, component_arr, 0, component_bit_strings);
        return component_bit_strings;
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
}
