package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;

import java.io.IOException;


public class TransferActivity extends Activity {
    EditText moneyTextView;
    EditText idTextView;
    Button acceptButton;
    Button cancelButton;


    private void initUI(){
        moneyTextView=(EditText)findViewById(R.id.transfer_money);
        idTextView=(EditText)findViewById(R.id.transfer_account);
        acceptButton=(Button)findViewById(R.id.transfer_accept);
        cancelButton=(Button)findViewById(R.id.transfer_cancel);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ie2=idTextView.getText().toString();
                String tmp=moneyTextView.getText().toString();
                byte[] Money=new byte[4];
                int num=0;
                num=Integer.valueOf(tmp).intValue();
                num-=num%100;
                Money[3]=(byte)(num&0xff);
                Money[2]=(byte)((num>>8)&0xff);
                Money[1]=(byte)((num>>16)&0xff);
                Money[0]=(byte)(num>>24);
                try {
                    Cdata h=new Cdata(Login.ip,Login.ie,4,ie2,Money);
                    h.work();
                }catch (IOException e){

                }
                while (Cdata.ffflag==0)
                {

                }
                Cdata.ffflag=0;
                Toast.makeText(getApplicationContext(),"转账成功",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
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
        setContentView(R.layout.activity_transfer);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_transfer, menu);
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
