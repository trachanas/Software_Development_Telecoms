package com.example.john.AndroidApp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.hardware.SensorEventListener;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.lang.String.valueOf;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    Toolbar myToolbar;
    Spinner mySpinner;

    static String Username = "root";
    static String Password = "toor";
    MqttAndroidClient client;
    MqttConnectOptions options;

    private String BrokerIp = "tcp://192.168.1.16:1883";
    private int qos = 1;
    private int  frequency = 5;

    String topicStr = "Topic2";

    String uniqueID = UUID.randomUUID().toString(); // unique identifier

    String clientId =  uniqueID;


    EditText message;




    private static final String TAG = "MainActivity";

    private SensorManager sensorManager;
    Sensor accelerometer;
    private long lastUpdate = 0;

    String xValue, yValue, zValue;

    MediaPlayer alert ;
    Camera camera;
    Camera.Parameters parameters;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        //WifiInfo wInfo = wifiManager.getConnectionInfo();
        //String macAddress = wInfo.getMacAddress();

        Log.d(TAG, "onCreate: Initializing Sensor ");
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        Log.d(TAG, "onCreate: Register accelerometer listener");

        myToolbar = (Toolbar) findViewById(R.id.toolbar);
        mySpinner = (Spinner) findViewById(R.id.spinner);
        myToolbar.setTitle(getResources().getString(R.string.app_name));


        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(MainActivity.this,
                R.layout.custom_spinner_itam,
                getResources().getStringArray(R.array.Toolbar_dropdown_entries));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mySpinner.setAdapter(myAdapter);


        alert = MediaPlayer.create(this,R.raw.rudy_rooster_crowing);

        message = (EditText) findViewById(R.id.MessageInput);


        mySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Toast.makeText(MainActivity.this,
                        mySpinner.getSelectedItem().toString(),
                        Toast.LENGTH_SHORT)
                        .show();

                if (mySpinner.getSelectedItem().toString().equals("Exit")) {
                    exit();
                } else if (mySpinner.getSelectedItem().toString().equals("Settings")) {

                    Intent intent;
                    intent = new Intent(MainActivity.this, SettingsActivity.class);
                    intent.putExtra("CURRENT_IP", client.getServerURI());
                    intent.putExtra("CURRENT_QOS", Integer.toString(qos));
                    intent.putExtra("CURRENT_TOPIC", topicStr);
                    intent.putExtra("CURRENT_FREQUENCY", Integer.toString(frequency));
                    startActivityForResult(intent, 1);   //  take  results back to mainActivity
                    Log.d("Frequency: ", Integer.toString(frequency));
                    /// PUBLISH MESSAGE TO JAVA ///

                } else {
                    System.out.println("Nothing selected");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });


        //Flashlight on Create start
        if(isFlashAvailable())   //  check if flash is available on this device, if it is open camera (module) and make button clickable
        {
            Log.v("Is flash available?" , "Yes");
            camera = Camera.open();
            parameters = camera.getParameters();
        };


        client = new MqttAndroidClient(MainActivity.this, BrokerIp, clientId);
        options = new MqttConnectOptions();
        options.setUserName(Username);
        options.setPassword(Password.toCharArray());
        message = (EditText) findViewById(R.id.MessageInput);

        try {
            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // Connection established
                    Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
                    setSubscription(client, topicStr, qos);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.w("Mqtt", "Failed to connect to:" + BrokerIp + exception.toString());
                    Toast.makeText(MainActivity.this, "Failed to connect to:" + exception.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                Log.d("Connection:", " Lost");
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                myMessageArrived(s, mqttMessage);
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                Log.d("Delivery", " completed with iMqttDeliveryToken: " + iMqttDeliveryToken);
            }
        });


        // Pop up to ask permissions
        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123 );

//#######################################################################################################################################################


        /// Publishing id, location and accelerometer in the background using threads
        /// Publishing random csv file
        final Handler mHandler = new Handler();
        final boolean isRunning = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                while (isRunning) {
                    try {
                        sleep(frequency * 1000);
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                // TODO Auto-generated method stub
                                checkWifiConnection();
                                publishGPS();
                                publishPhoneData();
                                publishRandomFile();
                            }
                        });
                    } catch (Exception e) {
                        // TODO: handle exception
                        e.printStackTrace();
                    }
                }
            }
        }).start();

//#######################################################################################################################################################

    }

    // PUBLISH RANDOM FILES
    public void publishRandomFile() {

        String topic = topicStr;

        //File dir =  new File("/storage/6639-6530/Test_Set"); // Android 1 xiaomi

        File dir =  new File("/storage/emulated/0/Test_Set");         // Android 2 samsung

        File[] files = dir.listFiles();
        Random rand = new Random();

        File file = files[rand.nextInt(files.length)];
        String fileName = file.getName();

        // String topic = topicStr;

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {


            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);

            if(bytes != null) {
                //Toast.makeText(MainActivity.this, "FILE FOUND", Toast.LENGTH_SHORT).show();
                try {


                    String payload = topic + "\n" + bytes.toString();
                    // MqttMessage myFile = new MqttMessage(bytes);
                    MqttMessage myFile = new MqttMessage(payload.getBytes());

                    //MqttMessage myFile = new MqttMessage(bytes);
                    MqttMessage title = new MqttMessage(fileName.getBytes());

                    myFile.setQos(qos);
                    // publish file name
                    client.publish(topicStr, title);
                    // publish file
                    client.publish(topicStr, myFile);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(MainActivity.this, "FILE NOT FOUND", Toast.LENGTH_SHORT).show();
            }

            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // PUBLISH GPS
    public void publishGPS() {

        String topic = topicStr;
        String myLocation = "";
        GPS g = new GPS(getApplicationContext());
        Location loc = g.getLocation();

        try {

            if (loc!=null){
                double latitude = loc.getLatitude();
                double longitude = loc.getLongitude();
                myLocation = "Latitude: " + valueOf(latitude) + " Longitude: " + valueOf(longitude);

            }
            String stringforPayload = '\n' + myLocation;
            MqttMessage payload = new MqttMessage(stringforPayload.getBytes());
            payload.setQos(qos);
            client.publish(topic, payload);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }
    // PUBLISH ID & ACCELEROMETER
    public void publishPhoneData() {

        String topic = topicStr;
        String accel = "x: " + xValue + '\n' + "y: " + yValue + '\n' + "z: " + zValue;

        try {

            String stringforPayload = '\n' + "Android ID: " + uniqueID + '\n' +  accel;
            MqttMessage payload = new MqttMessage(stringforPayload.getBytes());
            payload.setQos(qos);
            client.publish(topic, payload);

        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    // PUBLISH MESSAGE
    public void publish(View v) {
        String topic = topicStr;

        // Sending the csv through a buffer

        //File file = new File("/storage/6639-6530/Test_Set/1.EyesClosed1_1.csv");

        File file = new File("/storage/emulated/0/Test_Set/1.EyesClosed1_1.csv");

        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            //Toast.makeText(MainActivity.this, "reading csv", Toast.LENGTH_SHORT).show();

            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length );
            Log.d(TAG, "My file is: " + buf);

            if(bytes != null) {
                Toast.makeText(MainActivity.this, "FILE FOUND", Toast.LENGTH_SHORT).show();

                try {

                    String payload = topic + " " + bytes.toString() ;
                    // MqttMessage myFile = new MqttMessage(bytes);
                    MqttMessage myFile = new MqttMessage(payload.getBytes());

                    myFile.setQos(qos);
                    client.publish(topic, myFile);
                } catch (MqttException e) {
                    e.printStackTrace();
                }
            }else{
                Toast.makeText(MainActivity.this, "FILE NOT FOUND", Toast.LENGTH_SHORT).show();

                Log.d(TAG, "onCreate: EMPTY FILEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE");
            }


            buf.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        try {
            MqttMessage payload = new MqttMessage((valueOf(message.getText())+uniqueID).getBytes());
            payload.setQos(qos);
            client.publish(topic, payload);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    // Frequency to Java

    public void pubFreq(){
        String topic = topicStr;
        try{
            String f_message = "Frequency:"+Integer.toString(frequency);
            client.publish(topic,f_message.getBytes(),qos,false);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }


    private void setSubscription(MqttAndroidClient client, String topic, int qos){
        try{
            client.subscribe(topic, qos);
        }
        catch (MqttException e){
            e.printStackTrace();
        }
    }

    private void unsetSubscription(MqttAndroidClient client,String topic){
        try{
            client.unsubscribe(topic);
        }catch (MqttException e){
            e.printStackTrace();
        }
    }


    public void connect(View v) {
        if (!client.isConnected()) {
            try {
                IMqttToken token = client.connect(options);
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                        setSubscription(client, topicStr, qos);

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(MainActivity.this, "Not connected", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(MainActivity.this, "Client is already connected", Toast.LENGTH_LONG).show();
        }
    }

    public void disconnect(View v){
        if (client.isConnected()) {
            try {
                IMqttToken token = client.disconnect();
                token.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // We are connected
                        Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        // Something went wrong e.g. connection timeout or firewall problems
                        Toast.makeText(MainActivity.this, "Could not disconnect", Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }else {
            Toast.makeText(MainActivity.this, "Client is already disconnected", Toast.LENGTH_LONG).show();
        }
    }


    private void checkWifiConnection() {
        ConnectivityManager cn=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nf=cn.getActiveNetworkInfo();

        if(nf == null || nf.isConnected()!= true)
            Toast.makeText(this, "Network Not Available. Please Activate WiFi", Toast.LENGTH_SHORT).show();

    }


    private void myMessageArrived(String s, MqttMessage mqttMessage){

        if ((mqttMessage.toString()).equals("Critical level 1")) {


            flash(camera);

            try {
                sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            flash(camera);



        }else if((mqttMessage.toString()).equals("Critical level 2")) {

            alert.start();

            for (int i=0 ; i<=4 ; i++) {

                flash(camera);

                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                flash(camera);
            }
        }

    }

    public boolean isFlashAvailable(){  //  boolean function that returns true if flash is supported on this device

        return getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }

    public boolean isFlashOn(){
        return (this.parameters.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_ON) || this.parameters.getFlashMode().equals(android.hardware.Camera.Parameters.FLASH_MODE_TORCH));
    }

    public void flash(Camera camera) {//  self explanatory
        Camera.Parameters parameters = camera.getParameters();
        if (isFlashOn()){  //  if the flash is on torch mode
            //this.flashBtn.setImageResource(R.drawable.off);
            this.parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);  //  turn it off
        }else{
            //this.flashBtn.setImageResource(R.drawable.on);
            this.parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);    //  else turn it on
        }
        camera.setParameters(this.parameters);
        camera.startPreview();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String tempBrokerIp;
        String temptopic="";

        Boolean BrokerIpChanged = true;
        Boolean qosChanged = true;
        Boolean topicChanged = true;

        if (isFlashAvailable()){
            camera = Camera.open();
            parameters = camera.getParameters();
        }

        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case (1) : {
                if (resultCode == Activity.RESULT_OK) {
                    frequency = Integer.parseInt(data.getStringExtra("CURRENT_FREQUENCY"));
                    tempBrokerIp = data.getStringExtra("CURRENT_IP");

                    temptopic = data.getStringExtra("CURRENT_TOPIC");
                    pubFreq();

                    if (tempBrokerIp.equals(BrokerIp)) {
                        BrokerIpChanged=false;
                        Log.i("BrokerIpChanged =", BrokerIpChanged.toString());
                    }

                    if (temptopic.equals(topicStr)){
                        topicChanged=false;
                        Log.i("topicChanged =", topicChanged.toString());
                    }else{
                        topicStr = temptopic;
                    }
                    if (!BrokerIpChanged && !qosChanged && !topicChanged) return;

                    if (BrokerIpChanged){
                        try {
                            client.disconnect();

                            BrokerIp = tempBrokerIp;
                            topicStr = temptopic;

                            client = new MqttAndroidClient(MainActivity.this, BrokerIp, clientId);

                            options = new MqttConnectOptions();
                            options.setUserName(Username);
                            options.setPassword(Password.toCharArray());


                            try {
                                IMqttToken token = client.connect(options);
                                token.setActionCallback(new IMqttActionListener() {
                                    @Override
                                    public void onSuccess(IMqttToken asyncActionToken) {
                                        // We are connected
                                        Toast.makeText(MainActivity.this, "connected", Toast.LENGTH_SHORT).show();
                                        Log.w("Mqtt","Connected to:"+ BrokerIp);

                                        try{
                                            Log.v("INFO 11111:", "about to subscribe with: "+ topicStr + qos);
                                            client.subscribe(topicStr,qos);
                                        }catch (MqttException e){
                                            e.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                        // Something went wrong e.g. connection timeout or firewall problems
                                        Log.w("Mqtt","Failed to connect to:"+ BrokerIp + exception.toString());
                                        Toast.makeText(MainActivity.this, "Failed to connect to:" +exception.toString(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                                System.out.println(client.isConnected());
                                if (client.isConnected()){
                                    Log.v("INFO 22222:", "about to subscribe with: "+ temptopic + qos);
                                    client.subscribe(temptopic,qos);
                                }

                            } catch (MqttException e) {
                                e.printStackTrace();
                            }

                            client.setCallback(new MqttCallback() {
                                @Override
                                public void connectionLost(Throwable throwable) {

                                }

                                @Override
                                public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
                                    myMessageArrived(s,mqttMessage);
                                }

                                @Override
                                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

                                }
                            });

                        }catch (MqttException e){
                            e.printStackTrace();
                        }
                    }else
                    {
                        unsetSubscription(client,topicStr);
                        setSubscription(client,temptopic,qos);

                        topicStr = temptopic;

                    }
                }

                break;
            }
        }

    }


    public void exit(){

        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing MyApp")
                .setMessage("Are you sure you want to close this application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }

                })
                .setNegativeButton("No", null)
                .show();

    }


    @Override
    public void onSensorChanged(SensorEvent event) {

        Log.d(TAG, "onSensorChanged: X: "+ event.values[0] + "Y: " + event.values[1] + "Z: " + event.values[2]);

        Sensor mySensor = event.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();

            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;
            }

            xValue = String.valueOf(x);
            yValue = String.valueOf(y);
            zValue = String.valueOf(z);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
