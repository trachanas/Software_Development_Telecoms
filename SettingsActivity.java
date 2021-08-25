package com.example.john.AndroidApp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private EditText ipset,topicset,fset;
    private Button save;

    private String BrokerIp;
    private String topic;
    private String frequency;

    private WifiManager  wifiManager;
    private Switch wifiSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ipset = (EditText) findViewById(R.id.ipSetting);
        topicset = (EditText) findViewById(R.id.topicString);
        fset = (EditText) findViewById(R.id.Frequency);
        save = (Button) findViewById(R.id.saveButton);

        ipset.setText(getIntent().getStringExtra("CURRENT_IP"));
        topicset.setText(getIntent().getStringExtra("CURRENT_TOPIC"));
        fset.setText(getIntent().getStringExtra("CURRENT_FREQUENCY"));

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                BrokerIp = String.valueOf(ipset.getText());
                topic = String.valueOf(topicset.getText());
                frequency = String.valueOf(fset.getText());
                System.out.println(frequency);
                Intent intent;
                intent = new Intent();
                intent.putExtra("CURRENT_IP",BrokerIp); //  Store new values in intent
                intent.putExtra("CURRENT_TOPIC", topic);
                intent.putExtra("CURRENT_FREQUENCY",frequency);
                setResult(Activity.RESULT_OK,intent);   //  send them back to mainActivity
                finish();
            }
        });

        ////////////////////////////////////////////////////////////////////////////////////////////
        ///                             Wifi Activation                                          ///
        ////////////////////////////////////////////////////////////////////////////////////////////

        // Defining Variables
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiSwitch = (Switch) findViewById(R.id.wifi_switch);

        /* Checking wifi state.
         * If wifi is enabled, display "wifi is on" and set toggle button to on position.
         * If Wifi is disabled, display "wifi is off" and set toggle button to off position.
         */
        if (wifiManager.isWifiEnabled()) {
            Toast.makeText(SettingsActivity.this , " Wifi is currently on" ,Toast.LENGTH_SHORT).show();
            wifiSwitch.setChecked(true);
        } else {
            Toast.makeText(SettingsActivity.this , " Wifi is currently off" ,Toast.LENGTH_SHORT).show();
            wifiSwitch.setChecked(false);
        }

         /* adds listener to toggle button
         * If toggle is checked, wifi is enabled and "Wifi is on" is displayed.
         * If toggle is unchecked, wifi is enabled and "Wifi is off" is displayed.
         */
        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    wifiManager.setWifiEnabled(true);
                    Toast.makeText(SettingsActivity.this, "Activating WiFi", Toast.LENGTH_SHORT).show();
                } else {

                    wifiManager.setWifiEnabled(false);
                }
            }

        });

    }

}
