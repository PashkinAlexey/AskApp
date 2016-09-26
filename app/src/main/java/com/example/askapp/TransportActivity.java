package com.example.askapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TransportActivity extends AppCompatActivity {
    DbHelper dbHelper;
    SQLiteDatabase database;
    ListView clickedLView;
    ProgressBar speedBar,fuelBar;
    TextView fuelTextBar,stateTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport);
        Intent intent=getIntent();
        String clickedId=intent.getStringExtra("clickedId");
        String clickedStateNum=intent.getStringExtra("clickedStateNum");
        clickedLView=(ListView)findViewById(R.id.clickedLView);
        speedBar=(ProgressBar)findViewById(R.id.speedBar);
        fuelBar=(ProgressBar)findViewById(R.id.fuelBar);
        fuelTextBar=(TextView) findViewById(R.id.fuelTextBar);
        stateTitle=(TextView) findViewById(R.id.stateTitle);

        stateTitle.setText(clickedStateNum);
        dbHelper = new DbHelper(this);
        database=dbHelper.getReadableDatabase();

        String selection = dbHelper.KEY_TR_ID+"=\""+clickedId+"\" AND time > "+Long.toString(System.currentTimeMillis()-600000);
        Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, selection, null, null, null,null);

        ArrayList<Map<String, String>> data = new ArrayList<Map<String, String>>();
        long speed=0; //будет хранить последнее показание скорости после завершения цикла
        String fuel="noData"; //будет хранить последнее показание топлива после завершения цикла
        if (cursor.moveToFirst()) {
            //int trIdIndex = cursor.getColumnIndex(DbHelper.KEY_TR_ID);
            int timeIndex = cursor.getColumnIndex(DbHelper.KEY_TIME);
            int fuelIndex = cursor.getColumnIndex(DbHelper.KEY_FUEL);
            int speedIndex = cursor.getColumnIndex(DbHelper.KEY_SPEED);
            do {
                Map<String, String> m;
                m = new HashMap<String, String>();
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                m.put(DbHelper.KEY_TIME, timeFormat.format(new Date(cursor.getLong(timeIndex))));
                fuel= cursor.getString(fuelIndex);
                m.put(DbHelper.KEY_FUEL, fuel);
                speed=(long)Double.parseDouble(cursor.getString(speedIndex));
                data.add(m);
            } while (cursor.moveToNext());
        } else
            Log.d("mLog","0 rows");
        cursor.close();

        String[] from = {DbHelper.KEY_TIME, DbHelper.KEY_FUEL};
        int[] to = {R.id.clickedTime, R.id.clickedFuel};

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.clickedlist,
                from, to);

        //назначаем адаптер списку
        clickedLView.setAdapter(sAdapter);
        //ставим значение шкале скорости
        if (speed==0){
            speedBar.setProgress(0);
        }
        else {
            speedBar.setProgress((int)(100/(160.0/speed)));
        }
        //ставим значение шкале топлива
        if (fuel.equals("noData")){
            fuelBar.setVisibility(View.GONE);
            fuelTextBar.setVisibility(View.GONE);
        }
        else{
            Long longFuel=(long)Double.parseDouble(fuel);
            if (longFuel==0){
                fuelBar.setProgress(0);
            }
            else{
                fuelBar.setProgress((int)(100/(2500.0/longFuel)));
                fuelBar.setVisibility(View.VISIBLE);
                fuelTextBar.setVisibility(View.VISIBLE);
            }
        }
    }
}
