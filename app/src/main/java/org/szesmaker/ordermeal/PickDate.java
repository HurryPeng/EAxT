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

public class PickDate extends Activity {

    // UI widgets
    private DatePicker datePicker;
    private Button buttonLoad;

    private String username;
    Document docResponse;
    String dateSelected;

    SharedPreferences spMenu;
    SharedPreferences.Editor spMenuEditor;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.date_picker);

        // Initialise UI widgets
        datePicker = (DatePicker) this.findViewById(R.id.dp);
        buttonLoad = (Button) this.findViewById(R.id.buttonOrder);

        spMenu = getSharedPreferences("menu", MODE_PRIVATE);
        spMenuEditor = spMenu.edit();

        // Receive intent, get username and Chinese name and set it to display
        Intent intentReceived = getIntent();
        username = intentReceived.getStringExtra("username");
        docResponse = Jsoup.parse(intentReceived.getStringExtra("httpResponse"));
        String chineseName = docResponse.select("span#LblUserName").first().text();
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
            Intent intent = new Intent();
            intent.putExtra("finishAll", true);
            setResult(RESULT_OK, intent);
            finish();
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
            case 1: {
                checkBalance();
                break;
            }
            case 2: {
                Intent intent = new Intent();
                intent.setClass(this, Settings.class);
                startActivity(intent);
                break;
            }
            case 3: {
                Intent intent = new Intent();
                intent.putExtra("finishAll", false);
                setResult(RESULT_OK, intent);
                finish();
                overridePendingTransition(0, R.anim.slide_in_bottom);
                break;
            }
        }
        return true;
    }

    private void checkBalance() {
        String response = docResponse.toString();
        if(response.equals("")) {
            Toast.makeText(this, "查询失败", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            Toast.makeText(this, docResponse.select("span#LblBalance").first().text(), Toast.LENGTH_SHORT).show();
            return;
        }
    }

    private class loadMenu extends AsyncTask<Void, Void, Void> {
        boolean successful = true;
        String strResponse = "", paramView = "", paramGen = "", paramEvent = "";
        Common.Menu menu;

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
                Document docResponse = Jsoup.connect(urlOrder).followRedirects(true).timeout(5000).get();
                strResponse = docResponse.toString();
                if (!strResponse.equals("")) {
                    if (!strResponse.contains("Repeater1_Label1_0")) {
                        // There's no menu this day
                        menu = null;
                    }
                    else {
                        if (strResponse.contains("value=\"+\"")) {
                            // Allowed to order
                            paramView = docResponse.select("input#__VIEWSTATE").first().attr("value");
                            paramGen = docResponse.select("input#__VIEWSTATEGENERATOR").first().attr("value");
                            paramEvent = docResponse.select("input#__EVENTVALIDATION").first().attr("value");
                            menu = new Common.Menu(dateSelected, false, docResponse);
                        }
                        else menu = new Common.Menu(dateSelected, true, docResponse);// Prohibited to order
                    }
                }
                else successful = false;
            }
            catch (Exception e) {
                successful = false;
            }
            return null;
        }
        @Override protected void onPostExecute(Void result) {
            progressDialog.hide();
            if (successful) {
                if(menu == null) {
                    Toast.makeText(PickDate.this, "该日无菜单", Toast.LENGTH_SHORT).show();
                }
                else {
                    Intent intentToSend = new Intent();
                    intentToSend.putExtra("date", menu.date);// Pass date to the next activity as a key to find menu of the day in spMenu
                    spMenuEditor.putString(menu.date, menu.serialize());
                    spMenuEditor.commit();
                    if(menu.prohibited) {
                        intentToSend.setClass(PickDate.this, Prohibited.class);
                        startActivity(intentToSend);
                    }
                    else {
                        intentToSend.putExtra("paramView", paramView);
                        intentToSend.putExtra("paramGen", paramGen);
                        intentToSend.putExtra("paramEvent", paramEvent);
                        intentToSend.setClass(PickDate.this, Allowed.class);
                    }
                }
            }
            else {
                Toast.makeText(PickDate.this, "加载失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
