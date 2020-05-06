package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.ioc_annotation.AutoBind;
import com.example.ioc_annotation.BindView;
import com.example.ioc_annotation.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv)
    public TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //HelloWorld.main();
        AutoBind.getInstance().inject(this);
        textView.setText("hahaha");

    }
    @OnClick(R.id.tv)
    public void click(View v){
        Log.d("TAG","hello");
    }
}
