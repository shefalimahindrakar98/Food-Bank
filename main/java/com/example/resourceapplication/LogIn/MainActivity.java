package com.example.resourceapplication.LogIn;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.resourceapplication.R;

import com.example.resourceapplication.LogIn.Presenter;


import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This class serves as View in this Model View Presenter structure
 * This class implements Presenter.Contract interface to enforce presenter contract on the View
 */

public class MainActivity extends AppCompatActivity implements Presenter.PresenterContract{

    protected EditText username;
    protected EditText password;
    protected ArrayList<String> authenticationInformation = new ArrayList<String>();
    protected Button login;
    protected Presenter presenter;
    protected String usernameString;
    protected String passwordString;
    protected ProgressBar progressbar;
    protected Spinner usernamedropdown;
    protected TextView connectiontest;
    private boolean dropdowncreated = false;
    private boolean disconnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkWifiOnAndConnected();


        //Making TextEdit and Button objects
       // username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        login = (Button) findViewById(R.id.login);
        progressbar = (ProgressBar) findViewById(R.id.progressbar);
        progressbar.setVisibility(progressbar.GONE);
        username = (EditText) findViewById(R.id.username);
        connectiontest = (TextView) findViewById(R.id.connectiontest);

        //Created presenter
        presenter = new Presenter(this);
        try {
            getConnectionObject();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //presenter.createDropdown();

        login.setOnClickListener(loginListener);


    }

    public void getConnectionObject() throws SQLException {
        Connection connection = ConnectionClass.CONN();
        if(connection != null) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String connectionserver = databaseMetaData.getURL();
            connectiontest.setVisibility(connectiontest.VISIBLE);
            connectiontest.setBackgroundColor(Color.parseColor("#7CFC00"));
            connectiontest.setText("CONNECTED!");
            connection.close();
        }
        checkConnection();

    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            usernameString = username.getText().toString();
            passwordString = password.getText().toString();
            authenticationInformation.add(usernameString);
            authenticationInformation.add(passwordString);
            presenter.SetAuthenticationInformation();

        }
    };


    @Override
    public ArrayList<String> getAuthenticationInformation(){

        return authenticationInformation;
    }


    public void checkConnection(){


        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        //Schedule a task to run every 5 seconds (or however long you want)
        scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //Check Connection Here

                final Connection connection = ConnectionClass.CONN();
                if(connection==null)
                {
                    Log.d("Running","YES");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Do stuff to update UI here!
                            connectiontest.setBackgroundColor(Color.parseColor("#FF0000"));
                            disconnected = true;
                        }
                    });
                }
                else {
                    try {
                        connection.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(disconnected == true) {
                                    connectiontest.setBackgroundColor(Color.parseColor("#7CFC00"));
                                    DatabaseMetaData databaseMetaData = null;
                                    String connectionserver = null;
                                    try {
                                        databaseMetaData = connection.getMetaData();
                                        connectionserver = databaseMetaData.getURL();

                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                    }
                                    if(connectionserver != null) {
                                        connectiontest.setVisibility(connectiontest.VISIBLE);
                                        connectiontest.setBackgroundColor(Color.parseColor("#7CFC00"));
                                        connectiontest.setText("SQL Server: " + connectionserver.substring(22, 34));
                                        disconnected = false;
                                    }
                                }
                                if(dropdowncreated == false) {
                                    //presenter.createDropdown();
                                    //dropdowncreated = true;
                                }
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 2, TimeUnit.SECONDS);


    }




    public void toggleVisibility(){
        if(progressbar.getVisibility() == progressbar.GONE){
            progressbar.setVisibility(progressbar.VISIBLE);
        }
        else{
            progressbar.setVisibility(progressbar.GONE);
        }
    }
    @Override
    public void setDropdown(ArrayList<String> dropdown){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, dropdown);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        usernamedropdown.setAdapter(adapter);
        usernamedropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
                usernameString = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub
            };
    });

    }

    private void checkWifiOnAndConnected() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (wifiMgr.isWifiEnabled()) { // Wi-Fi adapter is ON

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();

            if( wifiInfo.getNetworkId() == -1 ){
                Toast wifitoast = Toast.makeText(this,"Not connected to Wifi",Toast.LENGTH_LONG); // Not connected to an access point
                wifitoast.show();
            }

             // Connected to an access point
        }
        else {
             // Wi-Fi adapter is OFF
            Toast wifitoast = Toast.makeText(this,"Wifi is off or out of range",Toast.LENGTH_LONG);
            wifitoast.show();
        }
    }



}
