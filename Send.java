import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Send{
    private static Socket socket;

    static void sendString(String message){
        try
        {
            String host = "192.168.1.2";
            int port = 25000;
            InetAddress address = InetAddress.getByName(host);
            socket = new Socket(address, 6060);
            //Send the message to the server
            OutputStream os = socket.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os);
            BufferedWriter bw = new BufferedWriter(osw);
            //String number = message;
            String sendMessage = message; //+ "\n";
            bw.write(sendMessage);
            bw.flush();
            System.out.println("Message sent to the server : "+sendMessage);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
        finally
        {
            try
            {
                socket.close();
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
    }
    //afto tha
}





