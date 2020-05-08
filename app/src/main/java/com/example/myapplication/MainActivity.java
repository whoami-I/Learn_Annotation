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

    @BindView(R.id.tv1)
    public TextView textView1;

    @BindView(R.id.tv2)
    public TextView textView2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AutoBind.getInstance().inject(this);
        textView.setText("hahaha");
        textView1.setText("hahaha111");
        textView2.setText("hahaha222");
    }
    @OnClick(R.id.tv)
    public void click(View v){
        Log.d("TAG","hello");
    }

    @OnClick(R.id.tv1)
    public void click1(View v){
        Log.d("TAG","hello11");
    }

    @OnClick(R.id.tv2)
    public void click2(View v){
        Log.d("TAG","hello22");
    }
}
