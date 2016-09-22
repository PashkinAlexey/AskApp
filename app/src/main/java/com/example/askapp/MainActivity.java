package com.example.askapp;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    final String ATTRIBUTE_NAME_SPEED = "speed";
    final String ATTRIBUTE_NAME_TIME = "time";
    final String URL="http://195.93.229.66:4242/main?func=state&uid=d8f9e2b6-678d-4036-ae31-9e7967d2987f&fuel&out=json";
    ListView lvMain;
    Handler h;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        lvMain=(ListView)findViewById(R.id.listView);

        h = new Handler() {
            public void handleMessage(android.os.Message msg) {
                //получаем мап
                Map<String, Object> handMap=(Map<String, Object>)msg.obj;
                //получаем объекты из мап
                final JSONObject JOBJ=(JSONObject)handMap.get("json");
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
                            JSONArray jArr=JOBJ.getJSONArray("objects");
                            JSONObject clickedObject=(JSONObject)jArr.get(position);
                            String clickedId=clickedObject.getString("id");
                            Intent intent= new Intent(MainActivity.this, TransportActivity.class);
                            intent.putExtra("clickedId", clickedId);
                            startActivity(intent);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        Thread t = new Thread(new Runnable() {
            Message msg;
            public void run() {
                while(true)
                {
                    try {
                        JSONObject jObj=JSONParser.getJSONFromUrl(URL);
                        SimpleAdapter sAdapter=listCreator(jObj);
                        //создаем мап для передачи 2х объектов хендлеру
                        Map<String, Object> handMap = new HashMap<String, Object>();
                        handMap.put("adapter",sAdapter);
                        handMap.put("json",jObj);
                        //создаем сообщение для хендлера
                        msg = h.obtainMessage(1, 0, 0, handMap);
                        //передаем сообщение
                        h.sendMessage(msg);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(10000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public SimpleAdapter listCreator(JSONObject jObj) throws JSONException {
        JSONArray objects;
        ArrayList<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
        Map<String, Object> m;
        objects = jObj.getJSONArray("objects");
        for (int i=0; i<objects.length(); i++){
            m = new HashMap<String, Object>();
            JSONObject objCurrent=(JSONObject) objects.get(i);
            if (!objCurrent.getString("statenum").equals("")){
                    m.put(ATTRIBUTE_NAME_STATENUMS, objCurrent.getString("statenum"));
            }
            else{
                m.put(ATTRIBUTE_NAME_STATENUMS, objCurrent.getString("garagenum"));
            }
            if (objCurrent.has("time")) {
                String inputDateStr=objCurrent.getString("time");
                try {
                    DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    DateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    Date date = inputFormat.parse(inputDateStr);
                    m.put(ATTRIBUTE_NAME_TIME, outputFormat.format(date));
                } catch (ParseException e) {
                    e.printStackTrace();
                    m.put(ATTRIBUTE_NAME_TIME, inputDateStr);
                }
            }
            else{
                m.put(ATTRIBUTE_NAME_SPEED, "noData");
            }
            if (objCurrent.has("fuel")) {
                m.put(ATTRIBUTE_NAME_FUEL, objCurrent.getString("fuel"));
            }
            else{
                m.put(ATTRIBUTE_NAME_FUEL, "noData");
            }
            if (objCurrent.has("speed")) {
                m.put(ATTRIBUTE_NAME_SPEED, objCurrent.getString("speed"));
            }
            else{
                m.put(ATTRIBUTE_NAME_SPEED, "noData");
            }
            data.add(m);
        }
        Log.d(TAG, " ");
        String[] from = {ATTRIBUTE_NAME_STATENUMS, ATTRIBUTE_NAME_FUEL,
                ATTRIBUTE_NAME_SPEED, ATTRIBUTE_NAME_TIME };
        int[] to = { R.id.statenum, R.id.fuel, R.id.speed, R.id.time };

        // создаем адаптер
        SimpleAdapter sAdapter = new SimpleAdapter(this, data, R.layout.listadapter,
                from, to);
        return sAdapter;
    }
}
