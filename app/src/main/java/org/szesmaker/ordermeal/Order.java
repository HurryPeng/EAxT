package org.szesmaker.ordermeal;
import android.app.*;
import android.content.*;
import android.os.*;
import android.util.Log;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.DatePicker.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;
public class Order extends Activity {
    private DatePicker dp;
    private TextView dateText;
    private Button order;
    private String cardID;
    private static final String TAG = "Order";

    Document doc;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.order);
        dp = (DatePicker) this.findViewById(R.id.dp);
        dateText = (TextView) this.findViewById(R.id.textDate);
        order = (Button) this.findViewById(R.id.buttonOrder);
        Intent intent = getIntent();
        cardID = intent.getStringExtra("username");
        doc = Jsoup.parse(intent.getStringExtra("httpResponse"));
        String name = doc.select("span#LblUserName").first().text();
        name = name.substring(name.indexOf("：") + 1);
        this.setTitle("欢迎, " + name + "同学");
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        dateText.setText(getDate(year, month, day));
        dp.init(year, month, day, new OnDateChangedListener() {
            @Override public void onDateChanged(DatePicker view, int year, int month, int day) {
                dateText.setText(getDate(year, month, day));
            }
        });
        order.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v){
                new startOrder().execute();    
            }
        });
    }
    private String getDate(int year, int month, int day) {
        String date = year + "-";
        month++;
        if (month < 10)
            date += "0";
        date = date + month + "-";
        if (day < 10)
            date += "0";
        date += day;
        return date;
    }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
            Log.d(TAG, "onKeyDown: backpushed");
            return true;
        }
        return false;
    }
    @Override public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 0, "查询余额");
        menu.add(1, 2, 0, "设置");
        menu.add(1, 3, 0, "登出");
        return true;
    }
    @Override public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                //new cx().execute();
                checkBalance();
                break;
            case 2:
                Intent intent = new Intent();
                intent.setClass(this, Settings.class);
                startActivity(intent);
                break;
            case 3:
                this.finish();
                overridePendingTransition(0, R.anim.slide_in_bottom);
                break;
        }
        return true;
    }
    /*
    private class cx extends AsyncTask<Void, Void, Void> {
        boolean dd = true;
        String resp = "";
        ProgressDialog progressDialog = new ProgressDialog(Order.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        @Override protected void onPreExecute() {
            progressDialog.setCancelable(false);
            progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("       正在查询");
            progressDialog.show();
        }
        @Override protected Void doInBackground(Void[] p1) {
            try {
                Document doc = Jsoup.connect("http://gzb.szsy.cn/card/Default.aspx").get();
                resp = doc.toString();
                if (resp.equals(""))
                    dd = false;
                else {
                    resp = doc.select("span#LblBalance").first().text();
                    resp = resp.substring(resp.indexOf("：") + 1);
                }
            }
            catch (Exception e) {
                dd = false;
            }
            return null;
        }
        @Override protected void onPostExecute(Void result) {
            progressDialog.hide();
            if (dd)
                Toast.makeText(Order.this, "余额" + resp, Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(Order.this, "查询失败", Toast.LENGTH_SHORT).show();
        }
    }
    */

    private void checkBalance()
    {
        String response = doc.toString();
        if(response.equals(""))
        {
            Toast.makeText(this, "查询失败", Toast.LENGTH_SHORT).show();
            return;
        }
        else
        {
            Toast.makeText(this, doc.select("span#LblBalance").first().text(), Toast.LENGTH_SHORT).show();
            return;
        }

    }

    private class startOrder extends AsyncTask<Void, Void, Void> {
        boolean dd = true;
        String date = "",zao = "",wuu = "",wan = "",resp = "",view = "",gen = "",event = "";
        int flag = 2, ordered = 0;
        SharedPreferences sp = getSharedPreferences("orderlist",MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        ProgressDialog window = new ProgressDialog(Order.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        @Override protected void onPreExecute() {
            date = dateText.getText().toString();
            window.setCancelable(false);
            window.setProgress(ProgressDialog.STYLE_SPINNER);
            window.setMessage("       正在加载");
            window.show();
        }
        @Override protected Void doInBackground(Void[] p1) {
            try {
                String url = "http://gzb.szsy.cn/card/Restaurant/RestaurantUserMenu/RestaurantUserMenu.aspx?Date=" + date;
                Document doc = Jsoup.connect(url).followRedirects(true).timeout(5000).get();
                resp = doc.toString();
                int temp = 0;
                if (resp != "") {
                    flag = resp.indexOf("Repeater1_Label1_0");
                    //if flag == -1 then there's no menu for today
                    if (flag != -1) {
                        flag = resp.indexOf("value=\"+\"");
                        if (flag != -1) {
                            flag = 1;
                            //if flag==1 then it is allowed
                            view = doc.select("input#__VIEWSTATE").first().attr("value");
                            gen = doc.select("input#__VIEWSTATEGENERATOR").first().attr("value");
                            event = doc.select("input#__EVENTVALIDATION").first().attr("value");
                            //load the three parameters for ordering event
                        }
                        else
                            flag = 0;
                        //if flag==0 then it is prohibited

                        //???c is menu
                        Elements zaoc = doc.select("table#Repeater1_GvReport_0");
                        Elements wuuc = doc.select("table#Repeater1_GvReport_1");
                        Elements wanc = doc.select("table#Repeater1_GvReport_2");

                        //???o is ordered
                        Elements zaoo = doc.select("input#Repeater1_CbkMealtimes_0");
                        Elements wuuo = doc.select("input#Repeater1_CbkMealtimes_1");
                        Elements wano = doc.select("input#Repeater1_CbkMealtimes_2");
                        
                        zao = zaoc.text();
                        //编号 类别 菜名 套餐 必选 单价 最大份数 订购份数 订餐状态 0 套餐 早餐套餐 套餐   5.00 1 0   1 牛奶 学生奶     2.04 3 0   2 蛋类 鲜鸡蛋     1.40 3 0   3 牛奶 玉米面蛋糕     1.40 3 0   4 点心 橄榄香卷     1.40 3 0   5 点心 韭黄煎饼     1.40 3 0   6 点心 糯米糍     1.40 3 0   7 粉面类 汤通心粉     1.60 3 0   9 必订菜 早餐必订菜（粥、小菜）   必选 1.00 1 0           合计: 0 0 0  
                        Log.i(TAG, "doInBackground: "+zao);
                        wuu = wuuc.text();
                        //编号 类别 菜名 套餐 必选 单价 最大份数 订购份数 订餐状态 0 套餐 午餐套餐 套餐   12.00 1 0   1 水果 芦柑     1.30 3 0   2 菜肴 蒜茸炒麦菜     1.80 3 1 已定 3 菜肴 炒土豆丝     1.80 3 0   4 菜肴 清蒸鱼     4.40 3 1 已定 5 菜肴 油泡脆皮肠     4.40 3 1 已定 6 菜肴 红烧豆腐     3.40 3 0   7 菜肴 香辣翅根2个     6.70 3 0   9 必订菜 午餐必订菜(米饭汤)   必选 1.50 1 1 已定         合计: 12.10 0 0  
                        Log.i(TAG, "doInBackground: "+wuu);
                        wan = wanc.text();
                        Log.i(TAG, "doInBackground: "+wan);

                        Log.i(TAG, "doInBackground: "+zaoo.toString());

                        editor.clear();
                        editor.commit();
                        //if checked then order nothing
                        temp = zaoo.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 2;
                            //1 for digit 2
                            editor.putBoolean("0", true);
                            editor.putBoolean("y0", true);
                            editor.commit();
                        }
                        temp = wuuo.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 4;
                            //1 for digit 3
                            editor.putBoolean("1", true);
                            editor.putBoolean("y1", true);
                            editor.commit();
                        }
                        temp = wano.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 8;
                            //1 for digit 4
                            editor.putBoolean("2", true);
                            editor.putBoolean("y2", true);
                            editor.commit();
                        }
                    }
                }
            }
            catch (Exception e) {
                dd = false;
            }
            return null;
        }
        @Override protected void onPostExecute(Void result) {
            window.hide();
            Intent dca = new Intent();
            if (dd) {
                dca.putExtra("date", date);
                dca.putExtra("zao", zao);
                dca.putExtra("wuu", wuu);
                dca.putExtra("wan", wan);
                dca.putExtra("ordered", ordered);
                switch (flag) {
                    case -1:
                        Toast.makeText(Order.this, "该日无菜单", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        //dca.setClass(Order.this, Prohibited.class);
                        //startActivity(dca);
                        dca.putExtra("view", view);
                        dca.putExtra("gen", gen);
                        dca.putExtra("event", event);
                        dca.setClass(Order.this, Allowed.class);
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
}
