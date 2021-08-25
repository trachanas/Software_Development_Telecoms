//Input: two arrays with doubles. they contain entropy values
//Output: the euclidean distance between the arrays values 

public class CalculateDistance {
    public double EuclideanDistance(double [] v1, double [] v2){
        double sum = 0;

        for(int i = 0; i < v1.length; i++){
            double distanceV = Math.pow(v1[i] - v2[i],2);
            sum += distanceV;
        }
        return Math.sqrt(sum);
    }

}
