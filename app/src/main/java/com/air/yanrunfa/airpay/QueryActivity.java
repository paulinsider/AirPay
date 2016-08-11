package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;

import java.io.IOException;
import java.lang.ref.WeakReference;


public class QueryActivity extends Activity {
    TextView moneyTextView;
    TextView nameTextView;
    TextView idTextView;
    Button acceptButton;
    private char[] name=new char[15];
    private int money;
    private int flag=1;

    private void initUI(){
        moneyTextView=(TextView)findViewById(R.id.query_money);
        nameTextView=(TextView)findViewById(R.id.query_name);
        idTextView=(TextView)findViewById(R.id.query_id);
        acceptButton=(Button)findViewById(R.id.query_accept);

        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query);
        initUI();
        String hehe="";
        byte[] haha=new byte[4];
        try {
            Cdata cha=new Cdata(Login.ip,Login.ie,1,hehe,haha);
            cha.work();
        }catch (IOException e){

        }

        while (Cdata.ffflag==0)
        {
        }
        Cdata.ffflag=0;
        name=Cdata.name;
        money=Cdata.Money;
        flag=0;
        char[] getmoney=new char[100];
        int len=0,temp=money;
        while (temp>0)
        {
            len++;
            temp/=10;
        }
        for (int i=len-1;i>=0;i--)
        {
            getmoney[i]=(char)((money%10)+'0');
            money/=10;
        }
        String finalmoney=new String(getmoney);
        String finalname=new String(name);
        finalmoney="余额："+finalmoney;
        finalname="姓名："+finalname;
        CharSequence Money=finalmoney;
        CharSequence Name=finalname;
        String imei=((TelephonyManager) getSystemService(TELEPHONY_SERVICE)).getDeviceId();
        imei="卡号："+imei;
        CharSequence ie=imei;
        idTextView.setText(ie);
        moneyTextView.setText(Money);
        nameTextView.setText(Name);
        //handler=new mHandler(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_query, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
