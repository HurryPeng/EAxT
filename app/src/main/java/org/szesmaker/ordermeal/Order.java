package org.szesmaker.ordermeal;
import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.DatePicker.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
import android.view.View.OnClickListener;
public class Order extends Activity
{
    @Override
    private DatePicker dp;
    private TextView datey;
    private Button ksdc;
    private String name,cid;
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);
        dp = (DatePicker) this.findViewById(R.id.dp);
        datey = (TextView) this.findViewById(R.id.datey);
        ksdc = (Button) this.findViewById(R.id.ksdc);
        Intent dl = getIntent();
        cid = dl.getStringExtra("id");
        Document nm = Jsoup.parse(dl.getStringExtra("out"));
        name = nm.select("span#LblUserName").first().text();
        name = name.substring(name.indexOf("：") + 1);
        this.setTitle("欢迎, " + name + "同学");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        datey.setText("");
        datey.setText(year + "-");
        month++;
        if (month < 10)
            datey.setText(datey.getText() + "0");
        datey.setText(datey.getText() + "" + month + "-");
        month--;
        if (day < 10)
            datey.setText(datey.getText() + "0");
        datey.setText(datey.getText() + "" + day);
        dp.init(year, month, day, new OnDateChangedListener(){
                @Override
                public void onDateChanged(DatePicker view, int year, int month, int day)
                {
                    datey.setText("");
                    datey.setText(year + "-");
                    month++;
                    if (month < 10)
                        datey.setText(datey.getText() + "0");
                    datey.setText(datey.getText() + "" + month + "-");
                    if (day < 10)
                        datey.setText(datey.getText() + "0");
                    datey.setText(datey.getText() + "" + day);
                }
            });
        ksdc.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View v)
                {
                    ks k = new ks();
                    k.execute();    
                }
            });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(1, 1, 0, "查询余额");
        menu.add(1, 2, 0, "设置");
        menu.add(1, 3, 0, "登出");
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case 1:
                new cx().execute();
                break;
            case 2:
                Intent settingjump = new Intent();
                settingjump.setClass(this,Settings.class);
                startActivity(settingjump);
                break;
            case 3:
                this.finish();
                overridePendingTransition(0,R.anim.slide_in_bottom);
                break;
        }
        return true;
    }
    class cx extends AsyncTask<Void, Void, Void>
    {
        boolean dd = true;
        String resp = "";
        ProgressDialog winlod = new ProgressDialog(Order.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        @Override
        protected void onPreExecute()
        {
            winlod.setCancelable(false);
            winlod.setProgress(ProgressDialog.STYLE_SPINNER);
            winlod.setMessage("       正在查询");
            winlod.show();
        }
        @Override
        protected Void doInBackground(Void[] p1)
        {
            try
            {
                Document doc = Jsoup.connect("http://gzb.szsy.cn/card/Default.aspx").get();
                resp = doc.toString();
                if (resp.equals(""))
                    dd = false;
                else
                {
                    resp = doc.select("span#LblBalance").first().text();
                    resp = resp.substring(resp.indexOf("：") + 1);
                }
            }
            catch (Exception e)
            {
                dd = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            winlod.hide();
            if (dd)
                Toast.makeText(Order.this, "当前余额" + resp, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(Order.this, "查询失败", Toast.LENGTH_SHORT).show();
        }
    }
    class ks extends AsyncTask<Void, Void, Void>
    {
        boolean dd = true;
        String date = "",zao = "",wuu = "",wan = "",resp = "",view = "",gen = "",event = "";
        int flag = 2,ordered = 0;
        SharedPreferences sp = getSharedPreferences("orderlist",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        ProgressDialog winlod = new ProgressDialog(Order.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        @Override
        protected void onPreExecute()
        {
            date = datey.getText().toString();
            winlod.setCancelable(false);
            winlod.setProgress(ProgressDialog.STYLE_SPINNER);
            winlod.setMessage("       正在加载");
            winlod.show();
        }
        @Override
        protected Void doInBackground(Void[] p1)
        {
            try
            {
                String url = "http://gzb.szsy.cn/card/Restaurant/RestaurantUserMenu/RestaurantUserMenu.aspx?Date=" + date;
                Document doc = Jsoup.connect(url).followRedirects(true).timeout(5000).get();
                resp = doc.toString();
                int temp = 0;
                if (resp != "")
                {
                    flag = resp.indexOf("Repeater1_Label1_0");
                    if (flag != -1)
                    {
                        flag = resp.indexOf("value=\"+\"");
                        if (flag != -1)
                        {
                            flag = 1;
                            view = doc.select("input#__VIEWSTATE").first().attr("value");
                            gen = doc.select("input#__VIEWSTATEGENERATOR").first().attr("value");
                            event = doc.select("input#__EVENTVALIDATION").first().attr("value");
                        }
                        else
                            flag = 0;
                        Elements zaoc = doc.select("table#Repeater1_GvReport_0");
                        Elements wuuc = doc.select("table#Repeater1_GvReport_1");
                        Elements wanc = doc.select("table#Repeater1_GvReport_2");       
                        Elements zaoo = doc.select("input#Repeater1_CbkMealtimes_0");
                        Elements wuuo = doc.select("input#Repeater1_CbkMealtimes_1");
                        Elements wano = doc.select("input#Repeater1_CbkMealtimes_2");
                        zao = zaoc.text();
                        wuu = wuuc.text();
                        wan = wanc.text();
                        editor.clear();
                        editor.commit();
                        temp = zaoo.toString().indexOf("checked");
                        if (temp != -1)
                        {
                            ordered += 2;
                            editor.putBoolean("0", true);
                            editor.putBoolean("y0",true);
                            editor.commit();
                        }
                        temp = wuuo.toString().indexOf("checked");
                        if (temp != -1)
                        {
                            ordered += 4;
                            editor.putBoolean("1", true);
                            editor.putBoolean("y1",true);
                            editor.commit();
                        }
                        temp = wano.toString().indexOf("checked");
                        if (temp != -1)
                        {
                            ordered += 8;
                            editor.putBoolean("2", true);
                            editor.putBoolean("y2",true);
                            editor.commit();
                        }
                    }
                }
            }
            catch (Exception e)
            {
                dd = false;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result)
        {
            winlod.hide();
            Intent dca = new Intent();
            if (dd)
            {
                dca.putExtra("date", date);
                dca.putExtra("zao", zao);
                dca.putExtra("wuu", wuu);
                dca.putExtra("wan", wan);
                dca.putExtra("ordered", ordered);
                switch (flag)
                {
                    case -1:
                        Toast.makeText(Order.this, "该日无菜单", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        dca.setClass(Order.this, Prohibited.class);
                        startActivity(dca);
                        break;
                    case 1:
                        dca.putExtra("view", view);
                        dca.putExtra("gen", gen);
                        dca.putExtra("event", event);
                        dca.setClass(Order.this, Allowed.class);
                        startActivity(dca);
                        break;
                    default:
                        Toast.makeText(Order.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(Order.this, "加载失败", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
