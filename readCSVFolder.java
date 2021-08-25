import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/*
    Main class of the BackHaul Server

    Input:  a directory of csv Files
    Output: the new training set
*
* */

public class readCSVFolder {

    static String FILE_NAME = "entropyCSV.csv";
    private static ServerSocket receiver;
    private static OutputStream out;
    private static Socket socket;
    private static File myFile ;
    private static byte[] buffer ;

    public readCSVFolder(){
        receiver = null;
        out = null;
        socket = null;
        myFile = new File(FILE_NAME);
        buffer = new byte[(int) myFile.length()];
    }


    public void sendFile(){
        try {
            receiver = new ServerSocket(9099);
            socket = receiver.accept();
            System.out.println("Accepted connection from : " + socket);
            FileInputStream fis = new FileInputStream(FILE_NAME);

            BufferedInputStream in = new BufferedInputStream(fis);
            in.read(buffer, 0, buffer.length);
            out = socket.getOutputStream();

            System.out.println("Sending files");
            out.write(buffer, 0, buffer.length);
            out.flush();


        /*while ((count = in.read(buffer)) > 0){
            out.write(buffer,0,count);
            out.flush();
        }*/
            out.close();
            in.close();
            socket.close();
            System.out.println("Finished sending");
        }catch(IOException ioe){
            System.out.println("Something went wrong with the connection!");
            ioe.printStackTrace();
        }
    }

    public static void main(String args[]){
        try {
            readCSVFolder fold = new readCSVFolder();
            readingCSV read = new readingCSV();
            FileWriter writer = new FileWriter(FILE_NAME);
            File folder = new File("test1");

            listFile listOfFiles = new listFile();

            List<String> filenames = listOfFiles.listFilesForFolder(folder);



            for (int i = 0; i < filenames.size(); i++) {
                writer.append(read.createCSV(filenames.get(i)));
                writer.append("\n");
            }
            System.out.println("Training set is ready!");
            System.out.println("Backhaul server is ready to send the training set to the Edge Server!");
            writer.close();


            fold.sendFile();

        }catch (Exception ex){
            System.out.println("Exception occured!");
            ex.printStackTrace();
            }
    }
}
