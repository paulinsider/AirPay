package com.air.network;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.air.yanrunfa.airpay.QueryActivity;
import com.air.yanrunfa.airpay.RegisterActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.Socket;


/**
 * Created by Paul Insider on 2015/5/19.
 */
public class Cdata {
    private String ip;
    private byte[] IMEI=new byte[32];
    private int kind=0;
    private byte[] IMEI2=new byte[32];
    private byte[] money=new byte[4];
    private Handler handler;
    public static char[] name=new char[15];
    public static int Money=0;
    public static int ffflag=0;
    public static int memeda=0;
    public Cdata(String IP,String ie,int category,String ie2,byte[] mon) throws IOException
    {
        ip=IP;
        IMEI=SM3.hash(ie.getBytes());
        kind=category;
        if (ie2!=null)
        {
            IMEI2=SM3.hash(ie2.getBytes());
        }
        money=mon;
    }
    public Cdata(int kin)
    {
        kind=kin;
        ip=Login.ip;
        memeda=0;
    }
    public void work()
    {
        WorkThread Wthread=new WorkThread();
        Wthread.start();
        Wthread.interrupt();
    }

    class WorkThread extends Thread {
        Socket socket;
        int RecvNumber = 0;
        byte[] check={0x0c,0x00,0x00,0x00,0x43,0x48,0x45,0x4B};
        byte[] getm={0x0c,0x00,0x00,0x00,0x47,0x45,0x54,0x4D};
        byte[] save={0x0c,0x00,0x00,0x00,0x53,0x41,0x56,0x45};
        byte[] change={0x0c,0x00,0x00,0x00,0x43,0x48,0x41,0x4E};
        byte[] exit={(byte)0xFF,0x00,0x00,0x00};
        public void run() {
            try {
                name=new char[15];
                Money=0;
                socket = new Socket(ip, 10000);
                OutputStream out = socket.getOutputStream();
                InputStream bin = socket.getInputStream();
                while (true)
                {
                    byte[] temp=new byte[800];
                    if (kind==0)
                    {
                        break;
                    }
                    ffflag=0;
                    switch (kind)
                    {
                        case 1:
                            System.arraycopy(check,0,temp,0,8);
                            System.arraycopy(IMEI,0,temp,8,32);
                            send(out,temp,40);
                            break;
                        case 2:
                            System.arraycopy(getm,0,temp,0,8);
                            System.arraycopy(IMEI,0,temp,8,32);
                            System.arraycopy(money,0,temp,40,4);
                            send(out,temp,44);
                            break;
                        case 3:
                            System.arraycopy(save,0,temp,0,8);
                            System.arraycopy(IMEI,0,temp,8,32);
                            System.arraycopy(money,0,temp,40,4);
                            send(out,temp,44);
                            break;
                        case 4:
                            System.arraycopy(change,0,temp,0,8);
                            System.arraycopy(IMEI,0,temp,8,32);
                            System.arraycopy(IMEI2,0,temp,40,32);
                            System.arraycopy(money,0,temp,72,4);
                            send(out,temp,76);
                            break;
                        case  5:
                            send(out,exit,4);
                            memeda=1;
                            return;

                    }
                    byte[] Recv = new byte[800];
                    RecvNumber = recv(bin, Recv);
                    int flag = Recv[0];
                    byte[] sendbuff = new byte[800];
                    for (int i = 4; i < RecvNumber; i++) {
                        sendbuff[i - 4] = Recv[i];
                    }

                    if (flag==14||flag==15||flag==16||flag==17)
                    {

                        int offset=4;
                        if (flag==14)
                        {
                            if ((int)sendbuff[4]<0) {
                                Money += 256;
                            }
                            Money = (Money+(int)sendbuff[4]);

                            Money<<=8;
                            if ((int)sendbuff[5]<0) {
                                Money += 256;
                            }
                            Money =(Money+(int)sendbuff[5]);
                            Money<<=8;
                            if ((int)sendbuff[6]<0) {
                                Money += 256;
                            }
                            Money = (Money+(int)sendbuff[6]);
                            Money<<=8;
                            if ((int)sendbuff[7]<0) {
                                Money += 256;
                            }
                            Money = (Money+(int)sendbuff[7]);
                            for (int i=12;i<RecvNumber-4;i++)
                            {
                                name[i-12]=(char)sendbuff[i];
                            }
                        }
                        /*Bundle data=new Bundle();
                        data.putInt("money",Money);
                        data.putCharArray("name",name);
                        Message msg=new Message();
                        msg.what=0x456;
                        msg.setData(data);
                        */
                        kind=0;
                        ffflag=1;
                    }
                }
                socket.close();
            }
            catch (IOException e)
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
}
