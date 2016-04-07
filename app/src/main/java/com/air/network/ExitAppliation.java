package com.air.network;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by Paul Insider on 2015/5/23.
 */
public class ExitAppliation extends Application {
    private ArrayList<Activity>activityList=new ArrayList<Activity>();
    private static ExitAppliation instance;

    // 单例模式中获取唯一的MyApplication实例
    public static ExitAppliation getInstance() {
        if (null == instance) {
            instance = new ExitAppliation();
        }
        return instance;
    }
    public void addActivity(Activity activity)
    {
        activityList.add(activity);
    }
    //添加Activity到容器中
    // 遍历所有Activity并finish
    public void exit() {
        for (Activity activity : activityList) {
            activity.finish();
        }
        System.exit(0);

    }
}

