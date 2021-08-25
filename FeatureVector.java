

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.lang.Comparable;


public class FeatureVector implements Comparable<FeatureVector>{
    String nameOfExperiment;
    double distance;    //the distance of the feature vector from the train set
    List<Double> entropyValues;

    public FeatureVector() {
        this.nameOfExperiment = " ";
        this.distance = -1;
        this.entropyValues = new ArrayList<>();
    }

    public ArrayList<FeatureVector> sorted(ArrayList<FeatureVector> fv){
        Collections.sort(fv);
        return fv;
    }


    @Override
    public String toString(){
        return "Experiment: " + getNameOfExperiment()
                + " with distance: " + getDistance();
    }

    public void setNameOfExperiment(String name) {
        this.nameOfExperiment = name;
    }

    public void setEntropyValues(Double entropy){
        entropyValues.add(entropy);
    }

    public void setDistance(double distance){
        this.distance = distance;
    }

    public String getNameOfExperiment(){
        return this.nameOfExperiment;
    }

    public double getDistance(){
        return this.distance;
    }
    @Override
    public int compareTo(FeatureVector fv) {
        return (this.getDistance() < fv.getDistance() ? -1 :
                (this.getDistance() == fv.getDistance() ? 0 : 1));
    }


    /*
     * Input:     the train set
     * Output:    a list of Feature Vectors objects, containing the data of the csv file
     * */
    public List<FeatureVector> readFile(File file) {
        List<FeatureVector> newlist = null;
        try {
            newlist = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                String attributes[] = line.split(",");

                FeatureVector fv = new FeatureVector();

                //the first element of the csv is name
                fv.setNameOfExperiment(attributes[0]);
                //the rest are the entropy values
                for (int i = 1; i < attributes.length; i++) {
                    if(attributes[i].equalsIgnoreCase("0.0")){break;}
                    fv.setEntropyValues(Double.parseDouble(attributes[i]));
                }
                newlist.add(fv);
            }
        }catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return newlist;
    }
}