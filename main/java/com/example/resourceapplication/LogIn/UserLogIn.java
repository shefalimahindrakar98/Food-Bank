package com.example.resourceapplication.LogIn;


import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.example.resourceapplication.LogIn.Presenter;

public class UserLogIn {

    private String username;
    private String password;
    private String authorization;

    public UserLogIn(){}

    public Presenter presenter;

    public void userInfo(String username, String password){
        presenter = new Presenter();
        this.username = username;
        this.password = password;

    }

    public void getAuthorization(String authorization){
        this.authorization = authorization;
    }

}
