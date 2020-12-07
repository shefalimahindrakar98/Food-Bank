package com.example.resourceapplication.Scanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import com.example.resourceapplication.LogIn.ConnectionClass;
import com.example.resourceapplication.R;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.zip.Inflater;



public class TableDisplay extends AppCompatActivity {
    ArrayList<ArrayList<String>> resultlist1 = new ArrayList<ArrayList<String>>();
    ArrayList<ArrayList<String>> resultlist2 = new ArrayList<ArrayList<String>>();
    ArrayList<String> columnlist = new ArrayList<String>();
    RelativeLayout relativelayout;
    LayoutInflater inflater;
    RecyclerView recyclerView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table_display);
        recyclerView = findViewById(R.id.recyclerView);
        (new CreateTable()).execute();
    }


    protected void iniTable() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        MyListAdapter adapter = new MyListAdapter(resultlist1,this);
        recyclerView.setAdapter(adapter);
        }







    class CreateTable extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
            Toast toastwait = Toast.makeText(TableDisplay.this, "Loading List", Toast.LENGTH_LONG);
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            Connection connect = ConnectionClass.CONN();
            String query1 = "SELECT CATNO, STORAGEBIN, STATION, LOCATION FROM dbo.[FOODTABLE] where SHORTAGE = 1";
            ResultSet resultset1 = null;
            if (connect != null) {
                try {
                    assert connect != null;
                    Statement fetchstatement1 = connect.createStatement();
                    resultset1 = fetchstatement1.executeQuery(query1);

                    Log.d("Here", "I AM HERE");

                    while (resultset1.next()) {
                        ArrayList<String> temparraylist1 = new ArrayList<String>();
                        for (int i = 1; i <= 4; i++) {
                            Log.d("HEY",resultset1.getString(i));
                            temparraylist1.add(resultset1.getString(i));
                        }
                        resultlist1.add(temparraylist1);
                    }

                    connect.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                if (resultlist1.isEmpty()) {
                    return false;
                }

                return true;
            } else {
                return false;
            }


        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean.equals(true)) {
                iniTable();
            } else {
                Toast toastsuccess = Toast.makeText(TableDisplay.this, "Table cannot be loaded", Toast.LENGTH_LONG);

            }
        }
    }

    public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.MyViewHolder>{

        ArrayList<ArrayList<String>> resultlist;
        MyListAdapter( ArrayList<ArrayList<String>> resultlist, Context context){
            this.resultlist = resultlist;
        }
        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ArrayList<String> row = resultlist.get(position);
            holder.CATNO.setText(row.get(0));
            holder.BIN.setText(row.get(1));
            holder.STATION.setText(row.get(2));
            holder.LOCATION.setText(row.get(3));
        }

        @Override
        public int getItemCount() {
            return resultlist.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder{
            TextView CATNO;
            TextView BIN;
            TextView STATION;
            TextView LOCATION;

            public MyViewHolder(@NonNull View itemView) {
                super(itemView);
                CATNO = itemView.findViewById(R.id.CATNOText);
                BIN = itemView.findViewById(R.id.BinText);
                STATION = itemView.findViewById(R.id.StationText);
                LOCATION = itemView.findViewById(R.id.LocationText);
            }
        }
    }

}



















