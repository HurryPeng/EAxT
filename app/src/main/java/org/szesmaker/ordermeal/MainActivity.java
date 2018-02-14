package org.szesmaker.ordermeal;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

public class MainActivity extends Activity {
    
    // UI widgets
    private Button buttonLogin;
    private EditText editTextLogin, editTextPassword;
    private ImageView imageViewSZSY;

    // SharedPreferences "code"
    private SharedPreferences spCode;
    private SharedPreferences.Editor editorSpCode;

    // Username and password
    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Initialise UI widgets
        buttonLogin = (Button) this.findViewById(R.id.login);
        editTextLogin = (EditText) this.findViewById(R.id.username);
        editTextPassword = (EditText) this.findViewById(R.id.password);
        imageViewSZSY = (ImageView) this.findViewById(R.id.szsy);

        // Initialise SharedPreferences
        spCode = getSharedPreferences("code", MODE_PRIVATE);
        editorSpCode = spCode.edit();

        // Set initial values for EditText(s)
        editTextLogin.setText(spCode.getString("username", ""));

        if (spCode.getBoolean("savePassword", true)) {
            editTextPassword.setText(spCode.getString("password", ""));
        }

        // Setup entrance for settings page
        imageViewSZSY.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, Settings.class);
                startActivity(intent);
            }
        });

        // Setup buttonLogin
        buttonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(MainActivity.this.getCurrentFocus().getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);

                // Get username and password from ExitText
                username = editTextLogin.getText().toString();
                password = editTextPassword.getText().toString();

                // Save username to SharedPreferences "code"
                editorSpCode.putString("username", username);
                editorSpCode.commit();
                new Login().execute();
            }
        });
    }

    private class Login extends AsyncTask<Void, Void, Integer> {
        // Send http request, get response and check whether it was successful
        String response = "";
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, ProgressDialog.THEME_DEVICE_DEFAULT_LIGHT);

        @Override
        protected void onPreExecute() {
            progressDialog.setCancelable(false);
            progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage("       正在登录");
            progressDialog.show();
        }

        @Override
        protected Integer doInBackground(Void[] p1) {
            String passwordEncoded = Common.encodeMD5(password);
            if (passwordEncoded.equals("")) return 0;
            Document doc;

            // Cookie stuffs
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);
            CookieStore cookieStore = cookieManager.getCookieStore();

            // Connect and save the response into responseDoc in date_picker to get the two parameters needed to post
            try {
                doc = Jsoup.connect(getString(R.string.urlLogin)).followRedirects(true).timeout(5000).get();
            }
            catch (IOException e) {
                return 0;
            }

            // Login and get response
            response = post(doc.select("input[name=execution]").first().attr("value"),
                    doc.select("input[name=lt]").first().attr("value"),
                    passwordEncoded);

            //check whether successful
            if (response.contains("深圳实验学校一卡通管理系统")) return 1;
            return 0;
        }

        @Override
        protected void onPostExecute(Integer loginSuccessful) {
            progressDialog.hide();
            if (loginSuccessful == 1) {
                // Save the valid password into SharedPreferences "code"
                editorSpCode.putString("password", password);
                editorSpCode.commit();

                // Parse response and username to date_picker page and start it
                Intent intent = new Intent();
                intent.putExtra("httpResponse", response);
                intent.putExtra("username", username);
                intent.setClass(MainActivity.this, PickDate.class);
                startActivity(intent);

                overridePendingTransition(R.anim.slide_out_bottom, 0);
            } else {
                // Clear the saved password if it's invalid
                editorSpCode.putString("password", "");
                editorSpCode.commit();

                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String post(String paramExecution, String paramLt, String passwordEncoded) {
        Map<String, String> params = new HashMap<>(8);

        params.put("_eventId", "submit");
        params.put("captcha", "null");
        params.put("code", "");
        params.put("execution", paramExecution);
        params.put("lt", paramLt);
        params.put("password", passwordEncoded);
        params.put("phone", "");
        params.put("username", username);

        return Common.sendHttpRequest(getString(R.string.urlLogin), encapsulate(params));
    }

    private StringBuffer encapsulate(Map<String, String> parameters) {
        StringBuffer buffer = new StringBuffer();
        try {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                buffer
                        .append(entry.getKey())
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), "utf-8"))
                        .append("&");
            }
            buffer.deleteCharAt(buffer.length() - 1);
        }
        catch (Exception e) {
            return null;
        }
        return buffer;
    }
}
