package com.ocrapplication;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.ArrayList;

public class ListHistoryActivity extends AppCompatActivity {
    private ListView lv;
    SQLiteHandler sh;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        sh=new SQLiteHandler(this);
        lv=(ListView)findViewById(R.id.listView);

        populatelistview();
    }

    private void populatelistview() {
        Cursor data=sh.getData();
        ArrayList<String> ls=new ArrayList<>();
        while (data.moveToNext())
        {
            ls.add(data.getString(1));

        }
        ListAdapter la= new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,ls);
        lv.setAdapter(la);
    }
}
