package com.ocrapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String Table_name="MTable";
    private static final String col1="ID";
    private static final String col2="Data";
    public SQLiteHandler(Context context)
    {
        super(context,Table_name,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //String createtable="CREATE TABLE "+Table_name+"(ID INT PRIMARY KEY AUTOINCREMENT,"+col2+" TEXT);";
        db.execSQL( "create table MTable"+
                "(ID integer primary key, Data text)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS MTable ");
        onCreate(db);
    }

    public boolean addData(String item)
    {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues ct=new ContentValues();
        ct.put("Data",item);
        long res = db.insert("MTable",null,ct);
        if(res==-1)
        {
            return  false;
        }
        else
            return  true;
    }


    public Cursor getData()
    {
        SQLiteDatabase db=this.getReadableDatabase();
        String query="select * from MTable ";
        Cursor data=db.rawQuery(query,null);
        return data;
    }

    // Useless data
    public static String sqlTranslate, sqlDetect;
}