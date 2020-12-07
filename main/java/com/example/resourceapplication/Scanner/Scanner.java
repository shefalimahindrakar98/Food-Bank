package com.example.resourceapplication.Scanner;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.resourceapplication.LogIn.ConnectionClass;
import com.example.resourceapplication.LogIn.MainActivity;
import com.example.resourceapplication.R;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Scanner extends AppCompatActivity implements ScannerPresenter.ScannerViewContract {
    private ScannerPresenter scannerPresenter;
    private String barcodeValue;
    private Button scanbutton;
    private Button sendbutton;
    private Button cancelbutton;
    private Button resetbutton;
    private EditText barcodetext;
    private String authorizations;
    private String username;
    private String password;
    private boolean resetflag = false;
    private Button tablebutton;
    private TextView authorizationtext;
    private TextView connectiontest;
    private boolean cancelledcheck;
    private Button setbutton;
    private AlertDialog.Builder builder;
    private EditText input;
    private static final int DELAY = 900000;
    private boolean textchange = false;

    public boolean inactive = true;

    Handler HideHandler = new Handler();
    Runnable HideRunnable = new Runnable() {
        @Override
        public void run() {
            if(inactive) {
                Toast inactivitytoast = Toast.makeText(Scanner.this,"Inactive for too long",Toast.LENGTH_LONG);
                inactivitytoast.show();
                Intent gobackactivity = new Intent(Scanner.this, MainActivity.class);
                Scanner.this.startActivity(gobackactivity);
                finish();
            } else {
                HideHandler.postDelayed(HideRunnable, DELAY);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scanbutton = (Button) findViewById(R.id.scanbutton);
        sendbutton = (Button) findViewById(R.id.sendbutton);
        cancelbutton = (Button) findViewById(R.id.cancelbutton);
        resetbutton = (Button) findViewById(R.id.resetbutton);
        barcodetext = (EditText) findViewById(R.id.barcodetext);
        tablebutton = (Button) findViewById(R.id.tablebutton);
        //setbutton = (Button) findViewById(R.id.setbutton);
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Barcode value");
        input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);


        //Get authorization of the user
       authorizations = getIntent().getExtras().getString("Authorizations");
       authorizations = authorizations.trim();
       username = getIntent().getExtras().getString("username");
       username = username.trim();
       password = getIntent().getExtras().getString("password");
       password = password.trim();
        Log.d("Authorization",authorizations);
       resetbutton.setVisibility(resetbutton.INVISIBLE);
       authorizationtext = (TextView) findViewById(R.id.authorizationtext);
       connectiontest = (TextView) findViewById(R.id.connectiontest);

       barcodetext.addTextChangedListener(filterTextWatcher);


        if("STORE".equals(authorizations)){
            scanbutton.setVisibility(scanbutton.GONE);
            sendbutton.setVisibility(sendbutton.GONE);
            resetbutton.setVisibility(resetbutton.VISIBLE);
            cancelbutton.setVisibility(cancelbutton.GONE);
        }

        //When Scan button is clicked
        scanbutton.setOnClickListener(scanbuttonclick);

        //When Send button is clicked
        sendbutton.setOnClickListener(sendbuttonclick);

        //When Cancel button is clicked
        cancelbutton.setOnClickListener(cancelbuttonclick);

        //When Reset button is clicked
        resetbutton.setOnClickListener(resetbuttonclick);

        tablebutton.setOnClickListener(tablebuttonclick);

        //setbutton.setOnClickListener(setbuttonclick);

        scannerPresenter = new ScannerPresenter(Scanner.this);
        authorizationtext.setText("Mode: "+authorizations);



        try {
            getConnectionObject();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        HideHandler.postDelayed(HideRunnable, DELAY);
    }

    public void getConnectionObject() throws SQLException {
        Connection connection = ConnectionClass.CONN();
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        String connectionserver = databaseMetaData.getURL();
        connectiontest.setVisibility(connectiontest.VISIBLE);
        connectiontest.setBackgroundColor(Color.parseColor("#7CFC00"));
        connectiontest.setText("SERVER CONNECTED");
        checkConnection();


    }

    private View.OnClickListener scanbuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            inactive = false;
            BarcodeScan();

            //Making the UI Thread sleep because Send Button appears before Scan in done.


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }




        }
    };

    private TextWatcher filterTextWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // DO THE CALCULATIONS HERE AND SHOW THE RESULT AS PER YOUR CALCULATIONS

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if("PRODUCTION".equals(authorizations) || "ADMIN".equals(authorizations)){
                sendbutton.setVisibility(sendbutton.VISIBLE);
            }

            if("STORE".equals(authorizations) || "ADMIN".equals(authorizations)){
                resetbutton.setVisibility(sendbutton.VISIBLE);
                textchange = true;
            }
        }
    };

    public void checkConnection(){


        ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(1);

        //Schedule a task to run every 5 seconds (or however long you want)
        scheduleTaskExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                //Check Connection Here

                Connection connection = ConnectionClass.CONN();
                if(connection==null)
                {
                    Log.d("Running","YES");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Do stuff to update UI here!
                            connectiontest.setBackgroundColor(Color.parseColor("#FF0000"));
                        }
                    });
                }
                else {
                    try {
                        connection.close();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                connectiontest.setBackgroundColor(Color.parseColor("#7CFC00"));
                            }
                        });
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, 0, 2, TimeUnit.SECONDS);
    }



    public void doneScanning(){
        //sendbutton.setVisibility(sendbutton.VISIBLE);
        //resetbutton.setVisibility(resetbutton.VISIBLE);
        if(cancelledcheck==true){
            return;
        }
        if("PRODUCTION".equals(authorizations) || "ADMIN".equals(authorizations)){
            sendbutton.setVisibility(sendbutton.VISIBLE);
        }

        if("STORE".equals(authorizations) || "ADMIN".equals(authorizations)){
            resetbutton.setVisibility(sendbutton.VISIBLE);
        }
    }

    private View.OnClickListener sendbuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            //Call the Presenter  Now to collect and store the Data in Model
            scannerPresenter.getBarcode(barcodetext.getText().toString().trim());
            scanbutton.setVisibility(scanbutton.GONE);
            cancelbutton.setVisibility(cancelbutton.GONE);
            scannerPresenter.StartConnection();

        }
    };

    private View.OnClickListener resetbuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //Call the presenter to reset the shortage value
            inactive = false;

            if(textchange){
                adminReset();
            }
            else {

                if ("ADMIN".equals(authorizations)) {
                    adminReset();
                } else {
                    resetflag = true;
                    BarcodeScan();
                }
            }
            //scannerPresenter.getBarcode(barcodeValue);
            //scanbutton.setVisibility(scanbutton.GONE);
            //cancelbutton.setVisibility(cancelbutton.GONE);
            //scannerPresenter.StartReset();

        }
    };

    public void adminReset(){
        scannerPresenter.getBarcode(barcodetext.getText().toString().trim());
        scanbutton.setVisibility(scanbutton.GONE);
        cancelbutton.setVisibility(cancelbutton.GONE);
        scannerPresenter.StartReset();

    }

    public void proceedtoReset(){
        scannerPresenter.getBarcode(barcodetext.getText().toString().trim());
        //scanbutton.setVisibility(scanbutton.GONE);
        //cancelbutton.setVisibility(cancelbutton.GONE);
        scannerPresenter.StartReset();
    }

    private View.OnClickListener cancelbuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            inactive = false;
            barcodeValue = null;
            sendbutton.setVisibility(sendbutton.GONE);
            barcodetext.setText("");
        }
    };

    public ArrayList<String> getCredentials(){
        ArrayList<String> credentials = new ArrayList<String>();

        credentials.add(username);
        credentials.add(password);
        credentials.add(authorizations);

        return credentials;
    }

    View.OnClickListener tablebuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent tableviewintent = new Intent(Scanner.this,TableDisplay.class);
            Scanner.this.startActivity(tableviewintent);

        }
    };

    View.OnClickListener setbuttonclick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String barcodemanual = input.getText().toString().trim();
                    barcodetext.setText(barcodemanual);
                    //sendbutton.setVisibility(sendbutton.VISIBLE);
                    //resetbutton.setVisibility(resetbutton.VISIBLE);
                    if("PRODUCTION".equals(authorizations) || "ADMIN".equals(authorizations)){
                        sendbutton.setVisibility(sendbutton.VISIBLE);
                    }

                    if("STORE".equals(authorizations) || "ADMIN".equals(authorizations)){
                        resetbutton.setVisibility(sendbutton.VISIBLE);
                    }

                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            inactive = false;
            builder.show();
        }
    };


    public void getScanText(){

    }

    public Activity giveContext(){
        return Scanner.this;
    }

    public void BarcodeScan(){
        inactive = false;
        IntentIntegrator integrator = new IntentIntegrator(Scanner.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result !=null){
            if(result.getContents()==null){
                Toast.makeText(this, "You cancelled the scanning", Toast.LENGTH_LONG).show();
                cancelledcheck = true;
            }
            else{
                cancelledcheck = false;
                Log.d("Here in Barcode Scan","Hi");
                barcodeValue = result.getContents();
                barcodetext.setText(barcodeValue);
                if(resetflag){
                    proceedtoReset();
                }
                doneScanning();
            }

        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent gobackactivity = new Intent(Scanner.this, MainActivity.class);
        Scanner.this.startActivity(gobackactivity);
        finish();
    }
}