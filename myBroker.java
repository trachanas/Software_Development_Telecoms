


import java.io.*;
import java.sql.Timestamp;

import java.util.*;



import java.net.Socket;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;


public class myBroker {


    static String title = " ";
    static String filePath = " ";

    volatile static String Lat1 = "0";
    volatile static String Long1 = "0";
    volatile static String Lat2 = "0";
    volatile static String Long2 = "0";

    volatile static boolean ackReceived = true;
    volatile static int freq = 0;
    volatile static BlockingQueue<String> senderQueue = new LinkedBlockingDeque<>(20);

    static class Receiver implements Runnable, MqttCallback{
        MqttClient client;
        private String topic;
        private int qos;
        private int frequency;
        String AndroidID;

        Receiver(int Rfrequency, MqttClient Rclient, int Rqos, String Rtopic){
            client = Rclient;
            topic = Rtopic;
            qos = Rqos;
            frequency = Rfrequency;
            System.out.println("A receiver was created");
        }

        void setAndroidID(String AndroidID){
            this.AndroidID = AndroidID;
        }

        String getAndroidID(){
            return this.AndroidID;
        }


        public void run() {	//	Run only initializes the client to receive the messages
            System.out.println("Receiver running");
            this.client.setCallback(this);
        }

        //		@Override
        public void connectionLost(Throwable t) {
            System.out.println("Connection lost!");
            // code to reconnect to the broker would go here if desired
//			Reconnect(client);
        }

        public void deliveryComplete(IMqttDeliveryToken token) {
            // TODO Auto-generated method stub
            System.out.println("Delivery completed with delivery token: "+ token);
        }

        //		@Override
        public synchronized void messageArrived(String topic, MqttMessage message){
            // TODO Auto-generated method stub

          //itle = " " ;
            String temp = " " ;

            if(message.toString().contains("exit")) {
                System.out.println("bye bye...");
                exit(0);

            }else if (message.toString().contains("Latitude")){

                System.out.println("Message received");
                System.out.println("##################################################");
                System.out.println("| Topic: " + topic);

                String payload = new String(message.getPayload());
                System.out.println("| GPS values: " + payload);
                String[] parts = payload.split(" ");


                if (topic.equals("Topic1")){

                    Lat1 = parts[1];
                    Long1 = parts[3];

                    System.out.println("MALAKAAAAAAAAAAAAAAAAAAAAAAAAA 11111111 " + Lat1 + "   " + Long1);

                }else if (topic.equals("Topic2")){

                    Lat2 = parts[1];
                    Long2 = parts[3];

                    System.out.println("MALAKAAAAAAAAAAAAAAAAAAAAAAAAA 2222222222" + Lat2 + "   " + Long2);
                }

                System.out.println("##################################################");

            }else if (message.toString().contains("Android ID")){

                System.out.println("");
                System.out.println("Message received");
                System.out.println("##################################################");
                System.out.println("| Topic: " + topic);

                String payload = new String(message.getPayload());

                System.out.println("| Message Received: " + payload);

                String parts[] = payload.split(" ");
                setAndroidID(parts[1]); // Set androidID for getter
                System.out.println("##################################################");

            }else if (message.toString().contains("Eyes")){

                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

                title = new String(message.getPayload()) ;

                System.out.println("| Title Received: " + title);

                System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
            }
            else{

                System.out.println("Creating file...");

                System.out.println("| THE FUCKING TITLE IS: " + title);

                filePath = String.format("C:/Users/gandroulakakis/Desktop/%s", title);
                //filePath = String.format("F:/Desktop/%s", title);


                //System.out.println("| THE FUCKING PATH IS: " + filePath);


                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                //try (FileOutputStream fos = new FileOutputStream("F:/Desktop/csv_to_be_sent.csv")) {


//                    fos.write(myFile);
                    fos.write(message.getPayload());
                    //fos.close(); There is no more need for this line since you had created the instance of "fos" inside the try. And this will automatically close the OutputStream
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

        }

    }

    static class Sender implements Runnable{
        private MqttClient client;
        private String topic,broker,clientId;
        private int qos;

        Sender(MqttClient Sclient, String Stopic, int Sqos){
            client = Sclient;
            topic = Stopic;
            qos = Sqos;
            System.out.println("A sender was created");
        }

        public void run() {
            // TODO Auto-generated method stub
            System.out.println("A sender is running");

            MqttMessage message;

            while (true) {	//	this will send messages/commands until no is given as command
                try {
                    sleep(freq*1000);
                    if (ackReceived) {
                        if (senderQueue.size()>0) {

                            String command = senderQueue.take();

                            System.out.println("Sender would send this: "+senderQueue.take());

                            message = new MqttMessage(command.getBytes());

                            message.setQos(qos);

                            sendMessage(message,client);

                        }

                    }
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }

        public synchronized void sendMessage(MqttMessage message, MqttClient client) {
            try {
                this.client.publish(this.topic, message);
            } catch (MqttPersistenceException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MqttException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    static class Distance implements Runnable{

        private String myLat1, myLong1, myLat2, myLong2;

        Distance(String Dlatitude1, String Dlongitude1, String Dlatitude2, String Dlongitude2){

            System.out.println("Distance thread was created");

            myLat1 = Dlatitude1;
            myLong1 = Dlongitude1;
            myLat2 = Dlatitude2;
            myLong2= Dlongitude2;

        }


        public void run() {
            // TODO Auto-generated method stub
            System.out.println("A distance thread is running");


            while(true) {

                try {
                    sleep(4000);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                float myDistance = distFrom(Float.valueOf(myLat1), Float.valueOf(myLong1), Float.valueOf(myLat2), Float.valueOf(myLong2));


                System.out.println("\n Distance Between Lat1= " + myLat1 + " Long1=" + myLong1 + " Lat2= " + myLat2 + " Long2= " + myLong2 );

                System.out.println("THE DISTANCE IS " + myDistance);



            }

        }

    }



    public static void main( String[] args ) throws InterruptedException, IOException {
        int argc = 0;
        int frequency = 0;
        for (String s : args) {
            argc++;
        }


        String topic = "Topic1";
        String topic2 = "Topic2";


        int qos = 2;
        String broker = "tcp://localhost:1883";
        String clientId = "JavaMqttClient";


        MemoryPersistence persistence = new MemoryPersistence();
        MqttClient client;

        Receiver receiver = null;
        Receiver receiver2 = null;

        try {
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            client.connect(connOpts);
            client.subscribe(topic, qos);
            client.subscribe(topic2, qos);


            Sender sender = new myBroker.Sender(client, topic, qos);
            Thread t1;
            t1 = new Thread(sender);
            t1.start();

            Sender sender2 = new myBroker.Sender(client, topic2, qos);
            Thread t3;
            t3 = new Thread(sender2);
            t3.start();


            receiver = new Receiver(frequency, client, qos, topic);
            Thread t2;
            t2 = new Thread(receiver);
            t2.start();
            //ANDROIDID1 = receiver.getAndroidID();


            receiver2 = new Receiver(frequency, client, qos, topic2);
            Thread t4;
            t4 = new Thread(receiver2);
            t4.start();
            //ANDROIDID2 = receiver.getAndroidID();


            sleep(4000);
            Distance mydistance = new Distance(Lat1, Long1, Lat2, Long2);
            Thread dist_thread;
            dist_thread = new Thread(mydistance);
            dist_thread.start();


        } catch (MqttException e) {
            System.out.println("reason " + e.getReasonCode());
            System.out.println("msg " + e.getMessage());
            System.out.println("loc " + e.getLocalizedMessage());
            System.out.println("cause " + e.getCause());
            System.out.println("excep " + e);
            e.printStackTrace();
        }


//        ///////////////////////////////////////////////////////////////////////////////////////////
//        ////////////////////////       RECEIVE FILE FROM BACKHAUL FOR TRAINING      ///////////////
//        ///////////////////////////////////////////////////////////////////////////////////////////
//
//        saveCSV save = new saveCSV();
//        String toSave = null;
//        String getinput = null;
//        //String serverName = "192.168.1.3";
//        byte[] buffer = new byte[maxsize];
//       // Socket socket = new Socket("192.168.1.8", 1312);
//        Socket socket = new Socket("192.168.1.3", 9099);
//
//        InputStream is = socket.getInputStream();
//
//       // File test = new File("neoarxeio.csv");
//        sleep(1000);
//        File test = new File("/Users/gandroulakakis/Desktop/neoarxeio.csv");
//        test.createNewFile();
//        FileOutputStream fos = new FileOutputStream(test);
//        BufferedOutputStream out = new BufferedOutputStream(fos);
//        byteread = is.read(buffer, 0, buffer.length);
//        current = byteread;
//
//        while ((byteread = is.read(buffer, 0, buffer.length)) != -1) {
//            out.write(buffer, 0, byteread);
//        }
//
//        out.flush();
//        out.write(buffer, 0, current);
//        out.flush();
//
//        socket.close();
//        fos.close();
//        is.close();
//        toSave = test.getAbsolutePath();
//        save.readCSV(toSave);
//
//        save.printList();

        ///////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////////////////

        //##############################################################################################
        //##############################################################################################
        //##############################################################################################
        double accuracy = 0;
        int countAll = 0;
        double classifiedTrue = 0;
        double classifiedFalse = 0;
        KnnFileClassifier knn = new KnnFileClassifier();
        //File pathName = new File("/Users/georgetrachanas/Desktop/testset");
        //listFile listfile = new listFile();
        //List<String> randomTestCSV = listfile.listFilesForFolder(pathName);
        // random = new Random();

        int index; // gia to tyxaio arxeio
        //apo to fakelo me ta csv arxeio, ta topothetoume se lista kai pairnw tyxaia ena apo afta

        //apo to path tou tyxaiou arxeiou pairnw to real tag gia na to sygkrinw me to classified
        String realTag = null;
        String classifiedTag = null;
        Boolean clevel = false;
        String currTopic;

        //for(int i = 0; i < 10; i++){

        // index = random.nextInt(10);
        //String randomFile = randomTestCSV.get(index);

        //File myFi


        while (true) {
            //System.out.println(myFile);
            countAll++;
            if (countAll == 10) {
                break;
            }
            if (title.contains("Opened")) {
                //realOpened++;
                realTag = "Eyes opened";
            } else {
                //realClosed++;
                realTag = "Eyes closed";
            }
            sleep(5000);
            System.out.println("The filepath before classification " + filePath);

            knn.classify(filePath);


            classifiedTag = knn.getTag();

            clevel = knn.getcLevel();

            currTopic = knn.getTopic();

            sleep(4000);
            float currDistance = distFrom(Float.valueOf(Lat1), Float.valueOf(Long1), Float.valueOf(Lat2), Float.valueOf(Long2));

            if (clevel == true ) {

                String toSend;

                if(currDistance<30){ //   if crit level 2
                // Send Command to Android
                senderQueue.put("Critical level 2");
                senderQueue.put("Critical level 2");
                senderQueue.put("Critical level 2");
                senderQueue.put("Critical level 2");

                // Send data to Backhaul server for logging
                String androidId1 = receiver.getAndroidID();
                String androidId2 = receiver2.getAndroidID();
                Timestamp t = new Timestamp(System.currentTimeMillis());
                String timeStamp = t.toString();
                String lat1 = Lat1;
                String long1 = Long1;
                String lat2 = Lat2;
                String long2 = Long2;
                String cLevel = "2";
                toSend = androidId1 + "__" + androidId2 + "%" + timeStamp + "%" + lat1 + "," + long1 + "__" + lat2 + "," + long2 + "%" + cLevel;
                //Send.sendString(toSend);
                }

                else {  //   if crit level 1

                    // Send Command to Android
                    senderQueue.put("Critical level 1");
                    senderQueue.put("Critical level 1");
                    senderQueue.put("Critical level 1");
                    senderQueue.put("Critical level 1");


                    if (currTopic.equals("Topic1")) {
                        // Send data to Backhaul server for logging
                        String androidId = receiver.getAndroidID();
                        Timestamp t = new Timestamp(System.currentTimeMillis());
                        String timeStamp = t.toString();

                        String mylat = Long1;
                        String mylong = Long1;

                        String cLevel = "1";
                        toSend = androidId + "%" + timeStamp + "%" + mylat + "," + mylong + "%" + cLevel;
                        //Send.sendString(toSend);

                    } else {
                        // Send data to Backhaul server for logging
                        String androidId = receiver2.getAndroidID();

                        Timestamp t = new Timestamp(System.currentTimeMillis());
                        String timeStamp = t.toString();

                        String mylat = Long2;
                        String mylong = Long2;

                        String cLevel = "1";
                        toSend = androidId + "%" + timeStamp + "%" + mylat + "," + mylong + "%" + cLevel;
                        //Send.sendString(toSend);
                    }

                }         Send.sendString(toSend);

            }

            System.out.println(classifiedTag);

            ////////////////////////////////////////////////////////////////////////////////////////
            //////////   tag returned  /////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////

            if (classifiedTag.equalsIgnoreCase(realTag)) {
                classifiedTrue++;
                System.out.println("Successful classification!");
                System.out.println("true: " + classifiedTrue);
            } else {
                classifiedFalse++;
                System.out.println("Unuccessful classification!");
                System.out.println("false: " + classifiedFalse);
            }
            System.out.println();

        }

        // }

        System.out.println(countAll);
        System.out.println(classifiedTrue);

        accuracy = (classifiedTrue / countAll) * 100;
        //accuracy = (double) 5/10;
        System.out.println("Successful classification: " + accuracy + "%");


        //##############################################################################################
        //##############################################################################################
        //##############################################################################################


    }

    public static float distFrom(float lat1, float lng1, float lat2, float lng2) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private void Reconnect(MqttClient client) {
    	String topic = "Topic1";
    	int qos = 2;
	    String broker = "tcp://localhost:1883";
	    String clientId = "JavaMqttClient";
    	MqttConnectOptions connOpts = new MqttConnectOptions();
    	connOpts.setCleanSession(true);
    	try {
			client.connect(connOpts);
			client.subscribe(topic,qos);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


}
