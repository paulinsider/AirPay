package com.air.network;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Created by Paul Insider on 2015/4/29.
 */
public class Register {
    private final static String LOCK_PASSWORD_SALT_FILE = "password_salt";
    private final static String LOCK_PASSWORD_SALT_KEY = "lockscreen.password_salt";
    private byte[] pw=new byte[320];
    private String ip;
    private byte[] s0=new byte[16];
    private byte[] IMEI=new byte[16];
    private byte[] k;
    private byte[] Original;
    private byte[] error={0x00,0x10,0x10,0x10,0x11,0x11,0x11,0x11};
    private byte[] success={0x0b,0x10,0x10,0x10,0x53,0x55,0x43,0x45};
    private int fffflag=0;
    private byte[] save=new byte[352];
    private Context ctx;
    private int hah=0;
        private String ie;
    Handler handl;
    public Register(byte[] pw0,String imei,Context cctx,String IP, Handler hand)
    {
        handl=hand;
        ip=IP;
        pw=pw0;
        ctx=cctx;
        ie=imei;
        IMEI=MD5.strToMD5(imei);
        s0[0]=0;
        //s0=MD5.strToMD5("93310981");
    }
    public void work()
    {
        WorkThread Wthread=new WorkThread();
        Wthread.start();
        Wthread.interrupt();
    }

    class WorkThread extends Thread {
        Socket socket;
        byte[] RegSignal = {0x01, 0x01, 0x01, 0x01, 0x52, 0x45, 0x47, 0x54};
        int RecvNumber = 0;
        SMS4 JustUseFunction = new SMS4();
        byte[] Encode = new byte[432];
        byte[] Decode01 = new byte[416];
        public void run() {
            try {
                int fflag=0;
                socket = new Socket(ip, 10000);
                OutputStream out = socket.getOutputStream();
                InputStream bin = socket.getInputStream();
                //BufferedReader bin=new BufferedReader(new InputStreamReader(bbin));
                while (true) {
                   /* socket.close();
                    socket = new Socket(ip, 10000);
                    out = socket.getOutputStream();
                    bin=socket.getInputStream();
                    */if (fflag==0) {
                        send(out, RegSignal, 8);
                        fflag=1;
                        fffflag=0;
                    }
                    byte[] Recv = new byte[800];
                        RecvNumber = recv(bin, Recv);
                    int flag = Recv[0];
                    byte[] sendbuff = new byte[800];
                    for (int i = 4; i < RecvNumber; i++) {
                        sendbuff[i - 4] = Recv[i];
                    }
                    if (flag==13)
                    {
                        fflag=0;
                        fffflag=0;
                    }
                    if (flag == 2) {
                        /*socket.close();
                        socket = new Socket(ip, 10000);
                        out = socket.getOutputStream();
                        bin = socket.getInputStream();
                        */while(s0[0]==0||fffflag==0)
                        {

                        }
                        JustUseFunction.sms4(sendbuff, RecvNumber - 4, s0, Encode, 0);
                        k = Encode;
                        Decode01 = Encryt(k);
                        sendbuff[0] = 0x03;
                        sendbuff[1] = 0x10;
                        sendbuff[2] = 0x10;
                        sendbuff[3] = 0x10;
                        for (int i = 4; i < 356; i++) {
                            sendbuff[i] = Decode01[i - 4];
                        }
                        send(out, sendbuff, 356);
                        //RecvNumber = recv(bin, Recv);
                    }
                    if (flag == 4) {
                        /*socket.close();
                        socket = new Socket(ip, 10000);
                        out = socket.getOutputStream();
                        bin = socket.getInputStream();
                        */JustUseFunction.sms4(sendbuff, RecvNumber - 4, k, Encode, 0);
                        if (save(Encode) == 1) {
                            SharedPreferences sp = ctx.getSharedPreferences(LOCK_PASSWORD_SALT_FILE, ctx.MODE_PRIVATE);
                            SharedPreferences saveOriginal = ctx.getSharedPreferences("Original", ctx.MODE_PRIVATE);
                            long t=0;
                            while(t==0)
                            {
                                t= sp.getLong(LOCK_PASSWORD_SALT_KEY, 0);
                                //t=1231;
                            }
                            byte[] MustSave = new byte[352];
                            char[] turn = new char[352];
                            byte[] b = new byte[16];
                            String FinalSave;

                            for (int i = 0; i < b.length; i++) {
                                b[i] = new Long(t & 0xff).byteValue();// 将最低位保存在最低位
                                t = t >> 8; // 向右移8位
                            }
                            for (int i=8;i<16;i++)
                            {
                                b[i]=0x01;
                            }
                            JustUseFunction.sms4(save, 352, b, MustSave, 1);
                            for (int i = 0; i < 352; i++) {
                                turn[i] = (char) MustSave[i];
                            }
                            FinalSave = String.valueOf(turn);
                            SharedPreferences.Editor editor = saveOriginal.edit();
                            editor.putString("cipher", FinalSave);
                            editor.commit();
                            send(out, success, 8);
                            socket.close();
                            hah=1;
                            Message msg=new Message();
                            msg.what=0x123;
                            handl.sendMessage(msg);
                            Login hehe=new Login(pw,ie,ctx,ip,handl);
                            hehe.work();
                            return;
                        } else {
                            fflag=0;
                            fffflag=0;
                        }
                    }

                }
                /*
                while (true) {

                }\
                    int flag = 1;
                    RecvNumber = recv(bin, Recv);
                    for (int i = RecvNumber - 8; i < RecvNumber - 1 && RecvNumber % 4 == 0; i++) {
                        if (Recv[i] != 0x10)
                            flag = 0;
                    }
                    if (flag == 0) {
                        continue;
                    }
                    byte[] Receive = new byte[800];
                    for (int i = 0; i < RecvNumber - 8; i++) {
                        Receive[i] = Recv[i];
                    }
                    switch ((int) Recv[RecvNumber - 1]) {
                        case 1:
                            JustUseFunction.sms4(Receive, RecvNumber, s0, Encode, 0);
                            k = Encode;
                            Decode01 = Encryt(k);
                            send(out, Decode01, 416);
                            break;
                        case 2:
                            JustUseFunction.sms4(Receive, RecvNumber, k, Encode, 0);
                            if (save(Encode) == 1) {
                                send(out, success, 4);
                                socket.close();
                                Login logn = new Login(pw, ip, IMEI, Original, k);
                                logn.work();
                                break;
                            } else {
                                send(out, error, 20);
                                continue;
                            }
                    }

                while(true)
                {
                    int flag=1;
                    send(out,RegSignal,4);
                    RecvNumber=recv(bin,Recv);
                    if (RecvNumber==-1)
                        flag=0;
                    if (RecvNumber==16)
                    {
                        int fflag=0;
                        for (int i=0;i<16;i++)
                        {
                            if (Recv[i]!=((byte)0xFF))
                            {
                                fflag=1;
                            }
                        }
                        if (fflag==0)
                            flag=0;
                    }
                    if (flag==0)
                    {
                        send(out,error,20);
                        continue;
                    }
                    else
                        break;
                }

                JustUseFunction.sms4(Recv,RecvNumber,s0,Encode,0);
                k=Encode;
                while (true)
                {
                    int flag=1;
                    Decode01=Encryt(k);
                    send(out,Decode01,416);
                    RecvNumber=recv(bin,Recv);
                    if(RecvNumber!=432)
                    {
                        send(out,error,20);
                        continue;
                    }
                    JustUseFunction.sms4(Recv,RecvNumber,k,Encode,0);
                    if (save(Encode)==1)
                    {
                        SharedPreferences sp=ctx.getSharedPreferences(LOCK_PASSWORD_SALT_FILE,ctx.MODE_PRIVATE);
                        SharedPreferences saveOriginal=ctx.getSharedPreferences("Original",ctx.MODE_PRIVATE);
                        long t=sp.getLong(LOCK_PASSWORD_SALT_KEY,0);
                        byte[] MustSave=new byte[416];
                        char[] turn=new char[416];
                        byte[] b=new byte[8];
                        String FinalSave;

                        for (int i = 0; i < b.length; i++) {
                            b[i] = new Long(t & 0xff).byteValue();// 将最低位保存在最低位
                            t= t>> 8; // 向右移8位
                        }
                        JustUseFunction.sms4(save,416,b,MustSave,1);
                        for (int i=0;i<416;i++)
                        {
                            turn[i]=(char)MustSave[i];
                        }
                        FinalSave=String.valueOf(turn);
                        SharedPreferences.Editor editor =saveOriginal.edit();
                        editor.putString("cipher",FinalSave);
                        editor.commit();

                        send(out,success,4);
                        socket.close();
                        break;
                    }
                    else
                    {
                        send(out,error,20);
                        continue;
                    }
                }*/

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }

        public void send(OutputStream out,byte[] data,int len)
        {
            byte[] ans=new byte[800];
            byte[] temp=new byte[4];
            try
            {

                for (int i=0;i<len;i++)
                    ans[i]=data[i];
                for (int i=len;i<len+4;i++)
                    ans[i]=0x10;
                out.write(ans,0,len+4);
                out.flush();
                /*for (int i=0;i<len+4;i++)
                {
                    temp[i%4]=ans[i];
                    if ((i+1)%4==0)
                    {
                        out.write(temp);
                        out.flush();
                    }
                }*/
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        public int recv(InputStream bin,byte[] buff)
        {

            int a=0;
            int flag=1;
            try
            {

                while (true)
                {
                    a=bin.read(buff);
                    if(a>0)
                        break;
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return a;
        }

        public byte[] Encryt(byte[] key)
        {
            char[] aaa=new char[336];
            String bbb;
            byte[] EncrytAnswer=new byte[352];
            byte[] value=new byte[352];
            byte[] PwIMEI=new byte[336];
            byte[] MD5PwIMEI=new byte[16];
            SMS4 temp=new SMS4();
            System.arraycopy(pw,0,PwIMEI,0,320);
            System.arraycopy(IMEI,0,PwIMEI,320,16);
            for (int i=0;i<336;i++)
            {
                aaa[i]=(char)PwIMEI[i];
            }
            bbb=String.valueOf(aaa);
            MD5PwIMEI=MD5.bytesToMD5(PwIMEI);
            System.arraycopy(pw,0,value,0,320);
            System.arraycopy(IMEI,0,value,320,16);
            System.arraycopy(MD5PwIMEI,0,value,336,16);
            temp.sms4(value,352,key,EncrytAnswer,1);
            return EncrytAnswer;
        }

        public int save(byte[] value)
        {
            byte[] Front=new byte[352];
            byte[] Down=new byte[16];
            byte[] Md5Front=new byte[16];
            byte[] NewK=new byte[16];
            for (int i=0;i<368;i++)
            {
                if (i<352)
                {
                    Front[i]=value[i];
                    if (i>=336&&i<352)
                    {
                        NewK[i-336]=value[i];
                    }
                }
                else
                {
                    Down[i-352]=value[i];
                }
            }
            Md5Front=MD5.bytesToMD5(Front);
            for (int i=0;i<16;i++)
            {
                if (Md5Front[i]!=Down[i])
                {
                    return 0;
                }
            }
            k=NewK;
            Original=Front;
            byte[] tmp=new byte[416];
            System.arraycopy(Original,0,tmp,0,336);
            System.arraycopy(k,0,tmp,336,16);
            save=tmp;
            return 1;
        }


    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0x123:
                    s0=MD5.strToMD5(msg.getData().getString("ss"));
                    fffflag=1;
                    break;
            }
        }
    };
}
