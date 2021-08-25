
import java.util.ArrayList;
import java.util.List;
import java.io.File;


public class listFile {

    //input:    a folder with Files such as CSV
    //output:   list of strings with the paths of each CSV file

    //default constructor
    public listFile(){}

    public List<String> listFilesForFolder(final File folder) {
        List<String> filenames = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
//
            if (fileEntry.getName().contains(".csv"))
                filenames.add(fileEntry.getAbsolutePath());
        }
        return filenames;
    }
}
