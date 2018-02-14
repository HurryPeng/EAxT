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

public class PickDate extends Activity {

    // UI widgets
    private DatePicker datePicker;
    private Button buttonLoad;

    private String username;

    Document responseDoc;

    String dateSelected;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_picker);

        // Initialise UI widgets
        datePicker = (DatePicker) this.findViewById(R.id.dp);
        buttonLoad = (Button) this.findViewById(R.id.buttonOrder);

        // Receive intent, get username and Chinese name and set it to display
        Intent intentReceived = getIntent();
        username = intentReceived.getStringExtra("username");
        responseDoc = Jsoup.parse(intentReceived.getStringExtra("httpResponse"));
        String chineseName = responseDoc.select("span#LblUserName").first().text();
        chineseName = chineseName.substring(chineseName.indexOf("：") + 1);
        setTitle("欢迎, " + chineseName + "同学");

        // Get the date today and initialise datePicker
        final Calendar today = Calendar.getInstance();
        int year = today.get(Calendar.YEAR);
        int month = today.get(Calendar.MONTH);
        int day = today.get(Calendar.DAY_OF_MONTH);

        datePicker.init(year, month, day, new OnDateChangedListener() {
            @Override public void onDateChanged(DatePicker view, int year, int month, int day) {
                dateSelected = Common.getDateString(year, month, day);
            }
        });

        // Set entry for menu page
        buttonLoad.setOnClickListener(new OnClickListener() {
            @Override public void onClick(View v){
                new loadMenu().execute();
            }
        });
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Prevent the app from returning to the login page on back pressed
        // This can maintain the login state as long as the activity isn't manually killed
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            startActivity(intent);
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

    private void checkBalance() {
        // The problem is, if the user has never killed the activity or reboot the system,
        // which means that s/he has never logged in twice, his/her balance will never be updated
        // To be improved
        String response = responseDoc.toString();
        if(response.equals("")) {
            Toast.makeText(this, "查询失败", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Toast.makeText(this, responseDoc.select("span#LblBalance").first().text(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private class loadMenu extends AsyncTask<Void, Void, Void> {
        boolean successful = true;
        String breakfast = "", lunch = "", dinner = "", response = "",
                paramView = "", paramGen = "", paramEvent = "";
        int flag = 2, ordered = 0;
        SharedPreferences spOrderlist = getSharedPreferences("orderlist",MODE_PRIVATE);
        SharedPreferences.Editor editorSpOrderlist = spOrderlist.edit();
        ProgressDialog progressDialog = new ProgressDialog(PickDate.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);
        @Override protected void onPreExecute() {
            progressDialog.setCancelable(false);
            progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("       正在加载");
            progressDialog.show();
        }
        @Override protected Void doInBackground(Void[] params) {
            try {
                String urlOrder = getString(R.string.urlOrderDateless) + dateSelected;
                Log.d("TAG", "doInBackground: "+urlOrder);
                Document doc = Jsoup.connect(urlOrder).followRedirects(true).timeout(5000).get();
                response = doc.toString();
                int temp = 0;
                if (response != "") {
                    if (!response.contains("Repeater1_Label1_0")) {
                        flag = -1; //if flag == -1 then there's no menu for the day
                    }
                    else {
                        flag = response.indexOf("value=\"+\"");
                        if (flag != -1) {
                            flag = 1;
                            //if flag==1 then it is allowed
                            paramView = doc.select("input#__VIEWSTATE").first().attr("value");
                            paramGen = doc.select("input#__VIEWSTATEGENERATOR").first().attr("value");
                            paramEvent = doc.select("input#__EVENTVALIDATION").first().attr("value");
                            //load the three parameters for ordering paramEvent
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

                        breakfast = zaoc.text();
                        //编号 类别 菜名 套餐 必选 单价 最大份数 订购份数 订餐状态 0 套餐 早餐套餐 套餐   5.00 1 0   1 牛奶 学生奶     2.04 3 0   2 蛋类 鲜鸡蛋     1.40 3 0   3 牛奶 玉米面蛋糕     1.40 3 0   4 点心 橄榄香卷     1.40 3 0   5 点心 韭黄煎饼     1.40 3 0   6 点心 糯米糍     1.40 3 0   7 粉面类 汤通心粉     1.60 3 0   9 必订菜 早餐必订菜（粥、小菜）   必选 1.00 1 0           合计: 0 0 0  
                        lunch = wuuc.text();
                        //编号 类别 菜名 套餐 必选 单价 最大份数 订购份数 订餐状态 0 套餐 午餐套餐 套餐   12.00 1 0   1 水果 芦柑     1.30 3 0   2 菜肴 蒜茸炒麦菜     1.80 3 1 已定 3 菜肴 炒土豆丝     1.80 3 0   4 菜肴 清蒸鱼     4.40 3 1 已定 5 菜肴 油泡脆皮肠     4.40 3 1 已定 6 菜肴 红烧豆腐     3.40 3 0   7 菜肴 香辣翅根2个     6.70 3 0   9 必订菜 午餐必订菜(米饭汤)   必选 1.50 1 1 已定         合计: 12.10 0 0  
                        dinner = wanc.text();

                        editorSpOrderlist.clear();
                        editorSpOrderlist.commit();
                        //if checked then buttonLoad nothing
                        temp = zaoo.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 2;
                            //1 for digit 2
                            editorSpOrderlist.putBoolean("0", true);
                            editorSpOrderlist.putBoolean("y0", true);
                            editorSpOrderlist.commit();
                        }
                        temp = wuuo.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 4;
                            //1 for digit 3
                            editorSpOrderlist.putBoolean("1", true);
                            editorSpOrderlist.putBoolean("y1", true);
                            editorSpOrderlist.commit();
                        }
                        temp = wano.toString().indexOf("checked");
                        if (temp != -1) {
                            ordered += 8;
                            //1 for digit 4
                            editorSpOrderlist.putBoolean("2", true);
                            editorSpOrderlist.putBoolean("y2", true);
                            editorSpOrderlist.commit();
                        }
                    }
                }
            }
            catch (Exception e) {
                successful = false;
            }
            return null;
        }
        @Override protected void onPostExecute(Void result) {
            progressDialog.hide();
            Intent dca = new Intent();
            if (successful) {
                dca.putExtra("date", dateSelected);
                dca.putExtra("breakfast", breakfast);
                dca.putExtra("lunch", lunch);
                dca.putExtra("dinner", dinner);
                dca.putExtra("ordered", ordered);
                switch (flag) {
                    case -1:
                        Toast.makeText(PickDate.this, "该日无菜单", Toast.LENGTH_SHORT).show();
                        break;
                    case 0:
                        //dca.setClass(PickDate.this, Prohibited.class);
                        //startActivity(dca);
                        dca.putExtra("paramView", paramView);
                        dca.putExtra("paramGen", paramGen);
                        dca.putExtra("paramEvent", paramEvent);
                        dca.setClass(PickDate.this, Allowed.class);
                        startActivity(dca);
                        break;
                    case 1:
                        dca.putExtra("paramView", paramView);
                        dca.putExtra("paramGen", paramGen);
                        dca.putExtra("paramEvent", paramEvent);
                        dca.setClass(PickDate.this, Allowed.class);
                        startActivity(dca);
                        break;
                    default:
                        Toast.makeText(PickDate.this, "加载失败", Toast.LENGTH_SHORT).show();
                }
            }
            else
                Toast.makeText(PickDate.this, "加载失败", Toast.LENGTH_SHORT).show();
        }
    }
}
