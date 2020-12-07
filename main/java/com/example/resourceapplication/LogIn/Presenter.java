package com.example.resourceapplication.LogIn;
import com.example.resourceapplication.LogIn.UserLogIn;
import com.example.resourceapplication.LogIn.MainActivity;
import com.example.resourceapplication.Scanner.Scanner;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.view.View;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import android.util.Log;
import android.widget.Toast;

public class Presenter {
MainActivity view;
UserLogIn user = new UserLogIn();
String username;
String password;
String authorizations;
Connection connect = null;
ArrayList<String> dropdownarray = new ArrayList<String>();
    public Presenter(MainActivity view){
        this.view = view;
    }

    public Presenter(){};

    public void SetAuthenticationInformation(){
        ArrayList<String> userInformation = view.getAuthenticationInformation();
        user.userInfo(userInformation.get(0),userInformation.get(1));

        //Get the username and password for the authentication query.

        this.username = '\''+userInformation.get(0)+'\'';
        this.password = '\''+userInformation.get(1)+'\'';


        Authenticate authenticate = new Authenticate();
        authenticate.execute();

    }


    /*
    public void createDropdown(){
        UsernameList usernamelist = new UsernameList();
        usernamelist.execute();
    }

     */

    interface PresenterContract {
        public ArrayList<String> getAuthenticationInformation();
        public void toggleVisibility();
        public void setDropdown(ArrayList<String> dropdown);
    }

    //Make Asynch communication with the database.
    class Authenticate extends AsyncTask<String,String,ResultSet> {

        @Override
        protected void onPreExecute() {
            view.toggleVisibility();
            view.login.setVisibility(view.login.GONE);
        }

        @Override
        protected ResultSet doInBackground(String... strings) {

                /**
                 * Make request to the database for the login authentication.
                 */
                //ConnectionClass connection = new ConnectionClass();
                //Connection connect = connection.CONN();

            connect = ConnectionClass.CONN();

            //Run the authentication query on username and password

            String sql = "SELECT * FROM dbo.[EMPLOYEE] WHERE USERNAME = "+username+" AND PASSWORD = "+password;

            boolean resultvalue = false;
            ResultSet resultset = null;
            if(connect != null) {
                try {

                    assert connect != null;
                    Statement fetchstatement = connect.createStatement();
                    resultset = fetchstatement.executeQuery(sql);
                    if (resultset != null) {
                        resultset.next();
                        authorizations = resultset.getString("AUTHORIZATION");
                        user.getAuthorization(authorizations);
                    }
                    connect.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            return resultset;
        }

        @Override
        protected void onPostExecute(ResultSet resultset){

            //view.toggleVisibility();
            //view.login.setVisibility(view.login.VISIBLE);

            /**
             * Have to check if the authentication was successfull.
             * Else Redirect back to the MainActivity view.
             */

            //Go to the Scanner class
            view.toggleVisibility();
            if(resultset == null || authorizations == null){
                if(connect == null){
                    Toast connectiontoast = Toast.makeText(view,"Not Connected to the database",Toast.LENGTH_LONG);
                    connectiontoast.show();
                    Intent restart = view.getIntent();
                    view.finish();
                    view.startActivity(restart);
                }
                else {
                    view.login.setVisibility(view.login.GONE);
                    Toast toast = Toast.makeText(view, "Incorrect Login Credentials", Toast.LENGTH_LONG);
                    toast.show();
                    //view.login.setVisibility(view.login.VISIBLE);
                    Intent restart = view.getIntent();
                    view.finish();
                    view.startActivity(restart);
                }
            }
            else {
                Toast toast = Toast.makeText(view,"Login successfull",Toast.LENGTH_LONG);
                toast.show();
                Intent intent = new Intent(view, Scanner.class);
                intent.putExtra("username", username);
                intent.putExtra("password",password);
                intent.putExtra("Authorizations",authorizations);
                view.startActivity(intent);
                view.finish();
            }
        }

    }

class UsernameList extends AsyncTask<String,String,ResultSet>{
    @Override
    protected void onPreExecute() {

    }

    @Override
    protected ResultSet doInBackground(String... strings) {

        /**
         * Make request to the database for the login authentication.
         */
        //ConnectionClass connection = new ConnectionClass();
        //Connection connect = connection.CONN();

        connect = ConnectionClass.CONN();

        //Run the authentication query on username and password

        String sql = "SELECT USERNAME FROM dbo.[EMPLOYEE]";

        boolean resultvalue = false;
        ResultSet resultset = null;
        if(connect != null) {
            try {

                assert connect != null;
                Statement fetchstatement = connect.createStatement();
                resultset = fetchstatement.executeQuery(sql);
                if (resultset != null) {
                   while(resultset.next()){
                       dropdownarray.add(resultset.getString("USERNAME"));
                   }
                    Collections.sort(dropdownarray);
                }
                //connect.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return resultset;
    }

    @Override
    protected void onPostExecute(ResultSet resultset){

        if(resultset == null ){
            if(connect == null){
                Toast connectiontoast = Toast.makeText(view,"Not Connected to the database",Toast.LENGTH_LONG);
                connectiontoast.show();
                Intent restart = view.getIntent();
                view.finish();
                view.startActivity(restart);
            }
            else {
                Toast connectiontoast = Toast.makeText(view,"Cannot fetch usernames",Toast.LENGTH_LONG);
                connectiontoast.show();
            }
        }
        else {
            view.setDropdown(dropdownarray);
            /*
            try {
                view.getConnectionObject(connect);
            } catch (SQLException e) {
                e.printStackTrace();


            }

             */
        }
    }
}




}
