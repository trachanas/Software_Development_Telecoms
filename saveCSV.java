import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class saveCSV {
    private List<String> toSave;

    public saveCSV(){
        this.toSave = new ArrayList<>();
    }

    public void printList(){
        for(int i = 0; i < toSave.size(); i++){
            System.out.println(this.toSave.get(i));
        }
    }

    public void readCSV(String file){
        try{

            String csvFile = file;
            String csvSplitBy = "\n";
            BufferedReader br = new BufferedReader(new FileReader(csvFile));
            String line = null;


            while ((line = br.readLine()) != null) {
                //String data[] = line.split(csvSplitBy);
                this.toSave.add(line);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }
}


/*
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 * */