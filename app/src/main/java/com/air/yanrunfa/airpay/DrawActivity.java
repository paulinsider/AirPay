package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;


public class DrawActivity extends Activity {
    Button button_100;
    Button button_200;
    Button button_500;
    Button button_1000;
    Button button_2000;
    Button button_input;
    byte[] Money=new byte[4];
    private void initUI(){
        button_100=(Button)findViewById(R.id.draw_100);
        button_100.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (Login.usermoney<100)
                {
                    Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                }
                else{
                    Money[0]=0x00;
                    Money[1]=0x00;
                    Money[2]=0x00;
                    Money[3]=(byte)100;
                    connect();
                    finish();
                }

            }
        });
        button_200=(Button)findViewById(R.id.draw_200);
        button_200.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (Login.usermoney<200)
                {
                    Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                }
                else {
                    Money[0] = 0x00;
                    Money[1] = 0x00;
                    Money[2] = 0x00;
                    Money[3] = (byte) 200;
                    connect();
                    finish();
                }
            }
        });
        button_500=(Button)findViewById(R.id.draw_500);
        button_500.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (Login.usermoney<500)
                {
                    Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                }
                else {
                    Money[0] = 0x00;
                    Money[1] = 0x00;
                    Money[2] = 0x01;
                    Money[3] = (byte) 0xf4;
                    connect();
                    finish();
                }
            }
        });
        button_1000=(Button)findViewById(R.id.draw_1000);
        button_1000.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (Login.usermoney<1000)
                {
                    Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                }
                else {
                    Money[0] = 0x00;
                    Money[1] = 0x00;
                    Money[2] = 0x03;
                    Money[3] = (byte) 0xE8;
                    connect();
                    finish();
                }
            }
        });
        button_2000=(Button)findViewById(R.id.draw_2000);
        button_2000.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v)
            {
                if (Login.usermoney<2000)
                {
                    Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                }
                else {
                    Money[0] = 0x00;
                    Money[1] = 0x00;
                    Money[2] = 0x07;
                    Money[3] = (byte) 0xD0;
                    connect();
                    finish();
                }
            }
        });
        button_input=(Button)findViewById(R.id.draw_input);
        button_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder builder=new AlertDialog.Builder(DrawActivity.this);
                final EditText number=new EditText(DrawActivity.this);
                builder.setView(number);
                builder.setTitle("请输入要取出的金额");
                builder.setNegativeButton("取消", null);
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String tmp=number.getText().toString();
                        int num=0;
                        num=Integer.valueOf(tmp).intValue();
                        num-=num%100;
                        Money[3]=(byte)(num&0xff);
                        Money[2]=(byte)((num>>8)&0xff);
                        Money[1]=(byte)((num>>16)&0xff);
                        Money[0]=(byte)(num>>24);

                        if (Login.usermoney<num)
                        {
                            Toast.makeText(getApplicationContext(), "余额不足！", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            connect();
                            finish();
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ExitAppliation.getInstance().addActivity(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_draw, menu);
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

    public int connect()
    {
        String haha="";
        Cdata hehe=new Cdata(Login.ip,Login.ie,2,haha,Money);
        hehe.work();
        while (Cdata.ffflag==0)
        {

        }
        Cdata.ffflag=0;
        Toast.makeText(getApplicationContext(), "取钱成功", Toast.LENGTH_SHORT).show();
        return 1;
    }
}
