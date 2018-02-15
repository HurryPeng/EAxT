package org.szesmaker.ordermeal;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.widget.*;
public class Prohibited extends TabActivity
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        int ordered = 0;
        Intent intentFromPickDate = getIntent();
        Common.Menu menu = intentFromPickDate.getParcelableExtra("menu");
        String date = menu.date;
        this.setTitle(date.substring(0,4)+"年"+date.substring(5,7)+"月"+date.substring(8)+"日菜单");
        TabHost tab = getTabHost();
        TabHost.TabSpec tab1 = tab.newTabSpec("tab1");
        TabHost.TabSpec tab2 = tab.newTabSpec("tab2");
        TabHost.TabSpec tab3 = tab.newTabSpec("tab3");
        Intent remote;

        if (menu.meals.get(0) != null)
        {
            remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            Common.Meal meal = menu.meals.get(0);
            remote.putExtra("meal", menu.meals.get(0));
            tab1.setContent(remote);
            tab1.setIndicator("早餐菜单");
            tab.addTab(tab1);
        }
        if (menu.meals.get(1) != null)
        {
            remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            remote.putExtra("meal", menu.meals.get(1));
            tab2.setContent(remote);
            tab2.setIndicator("午餐菜单");
            tab.addTab(tab2);
        }
        if (menu.meals.get(2) != null) {
            remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            remote.putExtra("meal", menu.meals.get(2));
            tab3.setContent(remote);
            tab3.setIndicator("晚餐菜单");
            tab.addTab(tab3);
        }
    }
}
