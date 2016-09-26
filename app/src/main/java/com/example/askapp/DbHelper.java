package com.example.askapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Алексей on 25.09.2016.
 */

public class DbHelper  extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "fuelDb"; //Название БД
    public static final String TABLE_CONTACTS = "timeFuel"; //Название таблицы

    public static final String KEY_ID = "_id"; //Автоинкремент
    public static final String KEY_TR_ID = "transportId"; //Id трансопрта с сервера
    public static final String KEY_TIME = "time"; //время записи в таблицу
    public static final String KEY_FUEL = "fuel"; //топливо
    public static final String KEY_SPEED = "speed"; //скорость

    public DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_CONTACTS + "(" + KEY_ID
                + " integer primary key," + KEY_TR_ID + " text," + KEY_TIME + " LONG," + KEY_SPEED + " text," + KEY_FUEL + " text" + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + TABLE_CONTACTS);

        onCreate(db);
    }
}