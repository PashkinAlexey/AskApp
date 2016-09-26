package com.example.askapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    final String TAG="myLogs";
    final String ATTRIBUTE_NAME_STATENUMS = "statenums";
    final String ATTRIBUTE_NAME_FUEL = "fuel";
    final String ATTRIBUTE_NAME_ID = "id";
    final String ATTRIBUTE_NAME_SPEED = "speed";
    final String ATTRIBUTE_NAME_TIME = "time";
    final String URL="http://195.93.229.66:4242/main?func=state&uid=d8f9e2b6-678d-4036-ae31-9e7967d2987f&fuel&out=json";
    boolean listThread=true;
    ListView lvMain;
    BufferedWriter bw;
    Thread t;
    Handler h;
    DbHelper dbHelper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMain=(ListView)findViewById(R.id.mainListView);

        dbHelper = new DbHelper(this);
        database = dbHelper.getWritableDatabase();

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //получаем мап
                final Map<String, Object> handMap=(Map<String, Object>)msg.obj;
                //получаем адаптер из мап
                SimpleAdapter sAdapter=(SimpleAdapter)handMap.get("adapter");
                //отключаем видимый Scroll
                lvMain.setScrollContainer(false);
                //сохраняем положение ListView
                Parcelable state = lvMain.onSaveInstanceState();
                //обновляем ListView
                lvMain.setAdapter(sAdapter);
                //Восстанавливаем положение ListView
                lvMain.onRestoreInstanceState(state);
                //вешаем обработчик нажатия
                lvMain.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        try {
                            //получаем адаптер из мап
                            final JSONObject jObj=(JSONObject)handMap.get("json");
                            JSONArray jArr=jObj.getJSONArray("objects");
                            JSONObject clickedObject=(JSONObject)jArr.get(position);
                            String clickedId=clickedObject.getString("id"); //серверный Id нажатого элемента
                            String clickedStateNum; //гос. номер нажатого элемента
                            if (clickedObject.getString("statenum").equals("")){
                                clickedStateNum=clickedObject.getString("garagenum");
                            }
                            else{
                                clickedStateNum=clickedObject.getString("statenum");
                            }
                            Intent intent= new Intent(MainActivity.this, TransportActivity.class);
                            intent.putExtra("clickedId", clickedId);
                            intent.putExtra("clickedStateNum", clickedStateNum);
                            startActivity(intent);
                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Ошибка данных", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
                });
            }
        };
        t = new Thread(new Runnable() {
            Message msg;
            public void run() {
                while(true) {
                    if (listThread)
                    {
                        try {
                            JSONObject jObj = JSONParser.getJSONFromUrl(URL);
                            SimpleAdapter sAdapter = adapterCreator(jObj);
                            //создаем мап для передачи 2х объектов хендлеру
                            Map<String, Object> handMap = new HashMap<String, Object>();
                            handMap.put("adapter", sAdapter);
                            handMap.put("json", jObj);

                            //создаем сообщение для хендлера
                            msg = h.obtainMessage(1, 0, 0, handMap);
                            //передаем сообщение
                            h.sendMessage(msg);
                        } catch (JSONException e) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Ошибка загрузки данных", Toast.LENGTH_SHORT);
                            toast.show();
                        } catch (IOException e) {
                            Toast toast = Toast.makeText(getApplicationContext(),
                                    "Ошибка загрузки данных", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                        try {
                            TimeUnit.MILLISECONDS.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }

    public void bdWrite(String trId, Long currTime, String fuel, String speed) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.KEY_TR_ID, trId);
        contentValues.put(DbHelper.KEY_TIME, currTime);
        contentValues.put(DbHelper.KEY_FUEL, fuel);
        contentValues.put(DbHelper.KEY_SPEED, speed);
        database.insert(DbHelper.TABLE_CONTACTS, null, contentValues);
    }

    public SimpleAdapter adapterCreator(JSONObject jObj) throws JSONException {
        JSONArray objects;
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;
        objects = jObj.getJSONArray("objects");
        String trId,statenum,time,fuel,speed="";
        Long currTime;
        //Парсинг данных о каждом транспорте
        for (int i=0; i<objects.length(); i++){
            m = new HashMap<String, Object>();
            JSONObject objCurrent=(JSONObject) objects.get(i);
            //Парсинг айдишника
            trId=objCurrent.getString(ATTRIBUTE_NAME_ID);
            //Парсинг гос. номера
            if (objCurrent.getString("statenum").equals("")){
                statenum=objCurrent.getString("garagenum");
            }
            else{
                statenum=objCurrent.getString("statenum");
            }
            //Парсинг времени
            if (objCurrent.has("time")) {
                String inputDateStr=objCurrent.getString("time");
                try {
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    Date date = inputFormat.parse(inputDateStr);
                    time=outputFormat.format(date);
                } catch (ParseException e) {
                    time=inputDateStr;
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Ошибка даты", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
            else{
                time="noData";
            }
            //Парсинг топлива
            if (objCurrent.has("fuel")) {
                fuel=objCurrent.getString("fuel");
            }
            else{
                fuel="noData";
            }
            //Парсинг скорости
            if (objCurrent.has("speed")) {
                speed=objCurrent.getString("speed");
            }
            else{
                speed="noData";
            }
            //Задаем время запроса
            currTime=System.currentTimeMillis();
            m.put(ATTRIBUTE_NAME_STATENUMS, statenum);
            m.put(ATTRIBUTE_NAME_TIME, time);
            m.put(ATTRIBUTE_NAME_FUEL, fuel);
            m.put(ATTRIBUTE_NAME_SPEED, speed);
            data.add(m);
            //Запись данных в БД
            bdWrite(trId,currTime,fuel,speed);

        }
        String[] from = {ATTRIBUTE_NAME_STATENUMS, ATTRIBUTE_NAME_FUEL,
                ATTRIBUTE_NAME_SPEED, ATTRIBUTE_NAME_TIME };
        int[] to = { R.id.statenum, R.id.fuel, R.id.speed, R.id.time };

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.listadapter,
                from, to);
        return sAdapter;
    }
}
