package com.example.resourceapplication.Scanner;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.example.resourceapplication.LogIn.ConnectionClass;
import com.example.resourceapplication.LogIn.MainActivity;
import com.example.resourceapplication.LogIn.UserLogIn;
import com.google.zxing.integration.android.IntentIntegrator;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ScannerPresenter {
    protected Scanner view;
    protected Activity activity;
    private String barcode;
    private UserLogIn user;
    private String authorizations;
    private String username;
    private String password;
    private double longitude = 19.1164;
    private double latitude = 72.90471;
    String defautlAdress = "Mumbai";


    public ScannerPresenter() {

    }

    public ScannerPresenter(Scanner view) {
        this.view = view;
        ArrayList<String> credentials = view.getCredentials();

        setCredentials(credentials);

        //this.connection = view.getIntent().getParcelableExtra("ConnectionObject");
        // this.user = view.getIntent().getParcelableExtra("UserObject");
    }

    public void setCredentials(ArrayList<String> credentials) {
        this.username = credentials.get(0);
        this.password = credentials.get(1);
        this.authorizations = credentials.get(2);
    }

    public void getBarcode(String barcodeValue) {
        this.barcode = '\'' + barcodeValue + '\'';
        //Test on logcat
        Log.d("Barcode", barcode);

    }

    public void StartConnection() {
        DatabaseOperations databaseOperations = new DatabaseOperations();
        databaseOperations.execute();
    }


    public interface ScannerViewContract {

        public void getScanText();

        public Activity giveContext();

        public ArrayList<String> getCredentials();

    }

    public void StartReset() {
        ResetOperations resetoperations = new ResetOperations();
        resetoperations.execute();
    }

    public void setLatAndLong() {
        /** PROCESS for Get Longitude and Latitude **/
        LocationManager locationManager = (LocationManager) view.getSystemService(Context.LOCATION_SERVICE);

        // getting GPS status
        boolean isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        Log.d("msg", "GPS:" + isGPSEnabled);

        // check if GPS enabled
        if (isGPSEnabled) {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            if (ActivityCompat.checkSelfPermission(view, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(view, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            Location location = locationManager.getLastKnownLocation(provider);

            //new LoadPlaces().execute();
            //Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(location != null)
            {
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                Log.d("msg", "first lat long : "+latitude +" "+ longitude);
                //new LoadPlaces().execute();
            }else
            {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProviderEnabled(String provider) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onProviderDisabled(String provider) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onLocationChanged(Location location) {
                        // TODO Auto-generated method stub
                        longitude = location.getLongitude();
                        latitude = location.getLatitude();
                        Log.d("msg", "changed lat long : "+latitude +" "+ longitude);
                    }
                });
            }

        }

    }

    public String getAddress() throws IOException {
        setLatAndLong();
        Log.d("Longitude ",longitude+"");
        Log.d("Latitdue ",latitude+"");
        Geocoder geocoder = new Geocoder(view, Locale.getDefault());
        List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

        if(addresses != null && addresses.size() > 0) {
            //use addresses
            String cityName = addresses.get(0).getAddressLine(0);
            return cityName;
        } else {
            //call web API async task
            return defautlAdress;
        }
    }


    class DatabaseOperations extends AsyncTask<String,String,Integer> {

        @Override
        protected Integer doInBackground(String... strings) {
            Connection connect = ConnectionClass.CONN();
            //String sql = "UPDATE dbo.[EKANBAN DATABASE] SET [SHORTAGE] = 1 WHERE [CATNO] ="+barcode;
            //String sql = "UPDATE EKANBAN DATABASE SET SHORTAGE = 1 WHERE CATNO ="+barcode;
            String date = "\'"+java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())+"\'";
            String tempusername = "\'"+username+"\'";
            String address="";
            try {
                address = getAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }

            address = "\'"+address+"\'";

            String sql = "UPDATE dbo.[FOODTABLE] SET SHORTAGE = 1, INPUT_DATE = "+date+", CLEAR_DATE = '', LOCATION = "+address+" WHERE CATNO = "+barcode;
            //String sql = "SELECT CATNO FROM dbo.[EKANBAN DATABASE]";
            Integer result = 0;
            boolean res = false;

            try {
                assert connect != null;
                Statement updatestatement = connect.createStatement();
                result = updatestatement.executeUpdate(sql);
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 0) {
                Toast toast = Toast.makeText(view, "Material not registered, please contact the store", Toast.LENGTH_LONG);
                toast.show();
                Intent restartback = view.getIntent();
                view.finish();
                view.startActivity(restartback);

            } else {
                Toast toast = Toast.makeText(view, "Request Sent", Toast.LENGTH_LONG);
                toast.show();
                Intent restartback = view.getIntent();
                view.finish();
                view.startActivity(restartback);

            }


        }

    }



    class ResetOperations extends AsyncTask<String,String,Integer> {

        @Override
        protected Integer doInBackground(String... strings) {

            Connection connect = ConnectionClass.CONN();

            //String sql = "UPDATE dbo.[EKANBAN DATABASE] SET [SHORTAGE] = 1 WHERE [CATNO] ="+barcode;
            //String sql = "UPDATE EKANBAN DATABASE SET SHORTAGE = 1 WHERE CATNO ="+barcode;
            String date = "\'"+java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime())+"\'";
            username = "\'"+username+"\'";
            String sql = "UPDATE dbo.[FOODTABLE] SET SHORTAGE = 0, CLEAR_DATE ="+date+", USERNAME='', LOCATION = '' WHERE CATNO = "+barcode;
            //String sql = "SELECT CATNO FROM dbo.[EKANBAN DATABASE]";

            Integer result = 0;
            boolean res = false;

            try {
                assert connect != null;
                Statement updatestatement = connect.createStatement();
                result = updatestatement.executeUpdate(sql);
                connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }



            return result;
        }

        @Override
        protected void onPostExecute(Integer result) {

            if (result == 0) {
                Toast toast = Toast.makeText(view, "Material not registered, please contact the store", Toast.LENGTH_LONG);
                toast.show();
                Intent restartback = view.getIntent();
                view.finish();
                view.startActivity(restartback);

            } else {
                Toast toast = Toast.makeText(view, "Successfull", Toast.LENGTH_LONG);
                toast.show();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Intent restartback = view.getIntent();
                view.finish();
                view.startActivity(restartback);

            }


        }

    }


}