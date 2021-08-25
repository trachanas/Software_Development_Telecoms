import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.io.FileReader;

/*
* Input:    the name of a csv file
* Output:   a string containing the entropy values of this file
*/

//

public class readingCSV {
    public String createCSV(String file) {
        String entropyString = null;
        BufferedReader br = null;
        try {
            String csvFile = file;
            String cvsSplitBy = ",";
            String nameOfExperiment = null;
            br = new BufferedReader(new FileReader(csvFile));

            //the first line
            String line = br.readLine();

            //list of strings for the different channels
            List<String> AF3_list = new ArrayList<>();
            List<String> F7_list = new ArrayList<>();
            List<String> F3_list = new ArrayList<>();
            List<String> FC5_list = new ArrayList<>();
            List<String> T7_list = new ArrayList<>();
            List<String> P7_list = new ArrayList<>();
            List<String> O1_list = new ArrayList<>();
            List<String> O2_list = new ArrayList<>();
            List<String> P8_list = new ArrayList<>();
            List<String> T8_list = new ArrayList<>();
            List<String> FC6_list = new ArrayList<>();
            List<String> F4_list = new ArrayList<>();
            List<String> F8_list = new ArrayList<>();
            List<String> AF4_list = new ArrayList<>();


            while ((line = br.readLine()) != null) {

                // use comma as separator
                String[] data = line.split(cvsSplitBy);
                AF3_list.add(data[0]);
                F7_list.add(data[1]);
                F3_list.add(data[2]);
                FC5_list.add(data[3]);
                T7_list.add(data[4]);
                P7_list.add(data[5]);
                O1_list.add(data[6]);
                O2_list.add(data[7]);
                P8_list.add(data[8]);
                T8_list.add(data[9]);
                FC6_list.add(data[10]);
                F4_list.add(data[11]);
                F8_list.add(data[12]);
                AF4_list.add(data[13]);
            }

            //matrix of double for each channel's value
            double[] AF3_values = new double[AF3_list.size()];
            double[] F7_values = new double[F7_list.size()];
            double[] F3_values = new double[F3_list.size()];
            double[] FC5_values = new double[FC5_list.size()];
            double[] T7_values = new double[T7_list.size()];
            double[] P7_values = new double[P7_list.size()];
            double[] O1_values = new double[O1_list.size()];
            double[] O2_values = new double[O2_list.size()];
            double[] P8_values = new double[P8_list.size()];
            double[] T8_values = new double[T8_list.size()];
            double[] FC6_values = new double[FC6_list.size()];
            double[] F4_values = new double[F4_list.size()];
            double[] F8_values = new double[F8_list.size()];
            double[] AF4_values = new double[AF4_list.size()];

            //converting strings to double values
            for (int i = 0; i < AF3_list.size(); i++) {
                AF3_values[i] = Double.parseDouble(AF3_list.get(i));
                F7_values[i] = Double.parseDouble(F7_list.get(i));
                F3_values[i] = Double.parseDouble(F3_list.get(i));
                FC5_values[i] = Double.parseDouble(FC5_list.get(i));
                T7_values[i] = Double.parseDouble(T7_list.get(i));
                P7_values[i] = Double.parseDouble(P7_list.get(i));
                O1_values[i] = Double.parseDouble(O1_list.get(i));
                O2_values[i] = Double.parseDouble(O2_list.get(i));
                P8_values[i] = Double.parseDouble(P8_list.get(i));
                T8_values[i] = Double.parseDouble(T8_list.get(i));
                FC6_values[i] = Double.parseDouble(FC6_list.get(i));
                F4_values[i] = Double.parseDouble(F4_list.get(i));
                F8_values[i] = Double.parseDouble(F8_list.get(i));
                AF4_values[i] = Double.parseDouble(AF4_list.get(i));

            }

            //saving the final matrix: the matrix of the entropy_values
            double[] entropy_values = new double[14];
            entropy_values[0] = Entropy.calculateEntropy(AF3_values);
            entropy_values[1] = Entropy.calculateEntropy(F7_values);
            entropy_values[2] = Entropy.calculateEntropy(F3_values);
            entropy_values[3] = Entropy.calculateEntropy(FC5_values);
            entropy_values[4] = Entropy.calculateEntropy(T7_values);
            entropy_values[5] = Entropy.calculateEntropy(P7_values);
            entropy_values[6] = Entropy.calculateEntropy(O1_values);
            entropy_values[7] = Entropy.calculateEntropy(O2_values);
            entropy_values[8] = Entropy.calculateEntropy(P8_values);
            entropy_values[9] = Entropy.calculateEntropy(T8_values);
            entropy_values[10] = Entropy.calculateEntropy(FC6_values);
            entropy_values[11] = Entropy.calculateEntropy(F4_values);
            entropy_values[12] = Entropy.calculateEntropy(F8_values);
            entropy_values[13] = Entropy.calculateEntropy(AF4_values);

            //get the experiment name
            if (csvFile.contains("Opened")) {
                nameOfExperiment = "Eyes opened";
            } else {
                nameOfExperiment = "Eyes closed";
            }

            //each line for the new csv file
            entropyString = nameOfExperiment + ','
                    + String.valueOf(entropy_values[0]) + ','
                    + String.valueOf(entropy_values[1]) + ','
                    + String.valueOf(entropy_values[2]) + ','
                    + String.valueOf(entropy_values[3]) + ','
                    + String.valueOf(entropy_values[4]) + ','
                    + String.valueOf(entropy_values[5]) + ','
                    + String.valueOf(entropy_values[6]) + ','
                    + String.valueOf(entropy_values[7]) + ','
                    + String.valueOf(entropy_values[8]) + ','
                    + String.valueOf(entropy_values[9]) + ','
                    + String.valueOf(entropy_values[10]) + ','
                    + String.valueOf(entropy_values[11]) + ','
                    + String.valueOf(entropy_values[12]) + ','
                    + String.valueOf(entropy_values[13]);
        }catch(IllegalArgumentException ex){
                System.out.println("Something went wrong with the BufferedReader");
                ex.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
           try{
              //

            } catch (Exception ex) {
                System.out.println("Exception occured during closing files!");
                ex.printStackTrace();
                ex.getMessage();
            }
        }
        return entropyString ;
    }
}
