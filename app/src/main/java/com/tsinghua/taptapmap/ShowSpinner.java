package com.tsinghua.taptapmap;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import java.util.ArrayList;

public class ShowSpinner extends AppCompatActivity {
    private Spinner mSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_spinner);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        ArrayList<String> data = new ArrayList<>();
        data.add("1");
        data.add("2");
        SpinnerAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,data);
        mSpinner.setAdapter(adapter);
    }
}