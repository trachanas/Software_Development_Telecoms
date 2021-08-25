import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.sql.*;

class Backhaul {
    private static Socket socket;


    static  Connection getConnection(){
        Connection connection = null;

        //database name and URL
        String jdbcDriver = "com.mysql.cj.jdbc.Driver";
        String dataPath = "jdbc:mysql://localhost:3306/my_database";

        //Database credentials
        String username = "root";
        String password = "sarantaporou27";


        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(dataPath,username,password);
            //System.out.println("We are connecting to the database!");
        }catch(SQLException se){
            //handling exception for JDBC
            System.out.println("Something went wrong with the JDBC driver");
            se.printStackTrace();
        }catch(Exception e){
            //handling exception for class.forName
            System.out.println("Somethibg went wrong with the class.forName");
            e.printStackTrace();
        }finally {
            try{
                if(connection == null){
                    connection.close();
                }
                //return null;
            }catch(SQLException se2){
                System.out.println("Something went wrong with the connection");
                se2.printStackTrace();
            }
        }
        return connection;
    }


    static void sendToDatabase(String message){
        try {
            Connection con  = getConnection();
            String[] token = message.split("%");
//            for(int i = 0; i < token.length; i++){
//                System.out.println("token"+ "["+ i +"]= " +token[i]);
//            }
            System.out.println("Inserting data in the Database!");
            String query = "INSERT INTO Data(ΑndroidIdentifier,Timestamp,GpsSignal,CriticalityLevel)" +
                    "VALUES ('"+token[0]+"','"+token[1]+"','"+token[2]+"','"+token[3]+"');";
            PreparedStatement ps  = con.prepareStatement(query);
            ps.executeUpdate();
        }catch(Exception ex){
            System.out.println("Something went wrong!");
            ex.printStackTrace();
        }
    }

    public static void main(String [] args)
    {
        try {
            System.out.println("\n \n");
            int port = 6060;

            //we can change the port when we face a problem with the connection
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server Started and listening to the port " + port);
            //Server is running always.
            while(true)
            {
                //Reading the message from the client
                socket = serverSocket.accept();
                InputStream is = socket.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String message = br.readLine();
                //message = "DELETE FROM Data WHERE GpsSignal > 0; ";
               // System.out.println(message);
                //send the message to the database in Backhaul server to format a new query
                sendToDatabase(message);
            }
        }catch (Exception ex){
            System.out.println("Something went wrong!");
            ex.printStackTrace();
        }
    }
}

