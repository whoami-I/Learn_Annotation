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
        textView.setText("I am the first view");
        textView1.setText("I am the second view");
        textView2.setText("I am the third view");
    }
    @OnClick(R.id.tv)
    public void click(View v){
        Log.d("TAG","I am the first view");
    }

    @OnClick(R.id.tv1)
    public void click1(View v){
        Log.d("TAG","I am the second view");
    }

    @OnClick(R.id.tv2)
    public void click2(View v){
        Log.d("TAG","I am the third view");
    }
}
