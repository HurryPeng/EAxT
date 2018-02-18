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

        SharedPreferences spMenu = getSharedPreferences("menu", MODE_PRIVATE);
        SharedPreferences.Editor spMenuEditor = spMenu.edit();

        Intent intentFromPickDate = getIntent();
        String date = intentFromPickDate.getStringExtra("date");
        Common.Menu menu = new Common.Menu(spMenu.getString(date, ""));

        this.setTitle(date.substring(0,4)+"年"+date.substring(5,7)+"月"+date.substring(8)+"日菜单");
        TabHost tab = getTabHost();
        TabHost.TabSpec tab1 = tab.newTabSpec("tab1");
        TabHost.TabSpec tab2 = tab.newTabSpec("tab2");
        TabHost.TabSpec tab3 = tab.newTabSpec("tab3");

        if (menu.meals[0] != null) {
            Intent remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            remote.putExtra("meal", menu.meals[0]);
            tab1.setContent(remote);
            tab1.setIndicator("早餐菜单");
            tab.addTab(tab1);
        }
        if (menu.meals[1] != null) {
            Intent remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            remote.putExtra("meal", menu.meals[1]);
            tab2.setContent(remote);
            tab2.setIndicator("午餐菜单");
            tab.addTab(tab2);
        }
        if (menu.meals[2] != null) {
            Intent remote = new Intent();
            remote.setClass(this, ProhibitedList.class);
            remote.putExtra("meal", menu.meals[2]);
            tab3.setContent(remote);
            tab3.setIndicator("晚餐菜单");
            tab.addTab(tab3);
        }
    }
}
