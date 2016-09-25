package com.example.askapp;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TransportActivity extends AppCompatActivity {
    DbHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transport);
        Intent intent=getIntent();
        String clickedId=intent.getStringExtra("clickedId");

        dbHelper = new DbHelper(this);
        database=dbHelper.getReadableDatabase();

        String selection = dbHelper.KEY_TR_ID+"=\""+clickedId+"\" AND time > "+Long.toString(System.currentTimeMillis()-600000);
        Cursor cursor = database.query(DbHelper.TABLE_CONTACTS, null, selection, null, null, null,null);

        if (cursor.moveToFirst()) {
            int trIdIndex = cursor.getColumnIndex(DbHelper.KEY_TR_ID);
            int timeIndex = cursor.getColumnIndex(DbHelper.KEY_TIME);
            int fuelIndex = cursor.getColumnIndex(DbHelper.KEY_FUEL);
            do {
                DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                Log.d("myLogs", "ID = " + cursor.getString(trIdIndex) +
                        ", time = " + timeFormat.format(new Date(cursor.getLong(timeIndex))) +
                        ", fuel = " + cursor.getString(fuelIndex));
            } while (cursor.moveToNext());
        } else
            Log.d("mLog","0 rows");

        cursor.close();
    }
}
