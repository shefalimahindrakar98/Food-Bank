package com.example.resourceapplication.LogIn;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import android.annotation.SuppressLint;
import android.os.StrictMode;
import android.util.Log;

public class ConnectionClass {
    static String ip = "192.168.0.102";
    static String classs = "net.sourceforge.jtds.jdbc.Driver";
    static String db = "EKANBAN";
    static String username = "shefali";
    static String password = "root";

    public static Connection CONN(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Connection conn = null;
        String ConnURL = null;
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
            ConnURL = "jdbc:jtds:sqlserver://" + ip + ";"
                    + "databaseName=" + db + ";user=" + username + ";password="
                    + password + ";";
            DriverManager.setLoginTimeout(2);
            conn = DriverManager.getConnection(ConnURL);
            Log.d("Hello","It Worked");

        } catch (SQLException se) {
            Log.e("ERRO", se.getMessage());
            return null;
        } catch (ClassNotFoundException e) {
            Log.e("ERRO", e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e("ERRO", e.getMessage());
            return null;
        }
        return conn;
    }
}