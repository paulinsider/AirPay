package com.air.yanrunfa.airpay;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.air.network.Cdata;
import com.air.network.ExitAppliation;
import com.air.network.Login;

import org.w3c.dom.CDATASection;


public class DepositActivity extends Activity {
    TextView moneyTextView;
    TextView numberTextView;
    Button reputButton;
    Button acceptButton;
    Button cancelButton;

    private void initUI(){
        moneyTextView=(TextView)findViewById(R.id.deposit_money);
        numberTextView=(TextView)findViewById(R.id.deposit_number);
        reputButton=(Button)findViewById(R.id.deposit_reput);
        acceptButton=(Button)findViewById(R.id.deposit_accept);
        acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String he="";
                byte[]  num={0x00,0x00,0x00,0x64};
                Cdata haha=new Cdata(Login.ip,Login.ie,3,he,num);
                haha.work();
                while(Cdata.ffflag==0)
                {

                }
                Cdata.ffflag=0;
                Toast.makeText(getApplicationContext(),"存钱成功！",Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        cancelButton=(Button)findViewById(R.id.deposit_cancel);
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
        setContentView(R.layout.activity_deposit);
        initUI();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_deposit, menu);
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
