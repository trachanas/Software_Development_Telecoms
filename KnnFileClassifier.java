import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class KnnFileClassifier {

    String tag;         //classified tag after the classification
    String Topic;       //the origin of the warning message Topic1 or Topic2
    Boolean cLevel ;    //if we are in criticality level or not


    public KnnFileClassifier() {
        this.tag = null;
        this.cLevel = false;
    }

    // Setters - Getters

    void setTag(String tag){
        this.tag = tag;
    }

    void setcLevel(Boolean cLevel){
        this.cLevel = cLevel;
    }

    void setTopic(String Topic){
        this.Topic = Topic;
    }

    String getTag(){
        return this.tag;
    }

    Boolean getcLevel(){
        return this.cLevel;
    }

    String getTopic(){
        return this.Topic;
    }

    /*
    Input:  the path of a test's set 
    Output: the classified tag and the origin of the file 
    */

    public void classify(String pathName) {
        String tag = null;
        Boolean cLevel = false;


        try {
            CalculateDistance cd = new CalculateDistance();
            File file = new File("C:/Users/gandroulakakis/Desktop/neoarxeio.csv");


            File file_to_Classify = new File(pathName);
            BufferedReader br = new BufferedReader(new FileReader(file_to_Classify));
            String Topic = br.readLine();
            setTopic(Topic);



            readCSV r = new readCSV();
            FeatureVector f = new FeatureVector();

            //fv: list with the FeatureVectors with the data of the train set
            List<FeatureVector> fv = f.readFile(file);

            //nearestNeighbors: list with the k nn
            List<FeatureVector> nearestNeighbors = new ArrayList<>();

            //list to Matrix entropy values for each feature Vector
            double [] entropy = new double[fv.get(0).entropyValues.size()];

            double [] temp = new double[fv.get(0).entropyValues.size()];

            List<FeatureVector> testFV = new ArrayList<>();

            //array 'temp' contains the entropy values for each test set
            temp = r.entropyTestFile(pathName);

            int n = 0;
            int k = 11;
            int countOpened = 0, countClosed = 0;
            double distance ;
            double weightOpened = 0, weightClosed = 0;
            double openedWeight = 0, closedWeight = 0;

            /*
             *  for each FeatureVector
             *       we save its entropyValues to an array of doubles
             *       we calculate the euclidean distance with the qi
             *       we save its value
             * */
            for(FeatureVector vector : fv){
                for(int i = 0; i < vector.entropyValues.size(); i++){
                    entropy[i] = vector.entropyValues.get(i);
                }
                n++;
                distance = cd.EuclideanDistance(entropy,temp);
                vector.setDistance(distance);
            }
            
            Collections.sort(fv);

            //k nearest neighbors
            for(int i = 0; i < k; i++){
                nearestNeighbors.add(fv.get(i));
            }

            for(FeatureVector vector : nearestNeighbors){
                //System.out.println(vector.toString());
                if(vector.getNameOfExperiment().contains("opened")){
                    countOpened++;
                }
                else {
                    countClosed++;
                }
            }
            System.out.println("Eyes opened: " + countOpened);
            System.out.println("Eyes closed: " + countClosed);


            for(FeatureVector vector : fv){
                if(vector.getNameOfExperiment().contains("opened")){
                    weightOpened += 1 / cd.EuclideanDistance(entropy,temp);
                }
                else{
                    weightClosed += 1 / cd.EuclideanDistance(entropy,temp);
                }
            }

            openedWeight = weightOpened * countOpened;
            closedWeight = weightClosed * countClosed;

            System.out.println("Weight opened: " + openedWeight);
            System.out.println("Weight closed: " + closedWeight);
            if(openedWeight > closedWeight){
                tag = "Eyes opened";
            }
            else{
                tag = "Eyes closed";
            }

            setTag(tag);

            int countTopic1 = 0 , countTopic2 = 0 ;

            if(Topic.equals("Topic1")){

                if(tag.equals("Eyes closed")){ countTopic1++; }

                else{ countTopic1 = 0; }

            }
            else if (Topic.equals("Topic2")) {

                if(tag.equals("Eyes closed")){ countTopic2++; }

                else{ countTopic2 = 0; }

            }

            if (countTopic1==3 || countTopic2==3){
                cLevel = true;
            }

            setcLevel(cLevel);

            System.out.println("Classified tag: " + tag);


        }catch (Exception ex){
            System.out.println("Something went wrong!");
            ex.printStackTrace();
        }
    }

}


