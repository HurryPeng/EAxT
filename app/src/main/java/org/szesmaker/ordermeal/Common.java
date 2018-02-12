package org.szesmaker.ordermeal;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;

/**
 * Created by HurryPeng on 2018.02.11.
 * Includes common functions used in activities.
 */

class Common
{
    static String sendHttpRequest(String url, StringBuffer uploadContent) {
        String respond = "";
        StringBuffer buffer = new StringBuffer();
        byte[] data = uploadContent.toString().getBytes();
        try {
            HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setConnectTimeout(5000);
            connection.connect();
            connection.getOutputStream().write(data);
            InputStream stream = connection.getInputStream();
            byte[] b = new byte[4096];
            while (stream.read(b) != -1) buffer.append(new String(b));
            respond = buffer.toString();
            connection.disconnect();
        } catch (Exception e) {
            respond = "";
        }
        return respond;
    }

    static String encodeMD5(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] b = md.digest(str.getBytes("UTF-8"));
            str = new BigInteger(1, b).toString(16);
            return str;
        }
        catch (Exception e) {
            return "";
        }
    }

    static int atoi(String str) {
        /*
            Turn String into int.
            This function is written due to the exceptions that Integer.parseInt() throws.
            The function is named after a C function with the same name.
        */
        int result = 0;
        for(int i = 0; i < str.length(); i++) {
            result += str.charAt(i) - '0';
            result *= 10;
        }
        result /= 10;
        return result;
    }

    static double atof(String str) {
        /*
            Turn String into double.
            This function is written due to the exceptions that Double.parseDouble() throws.
            The function is named after a C function with the same name.
        */
        double result = 0;
        double decimal = 0;
        int l = str.length();
        int dotPosition = 0;
        for(int i = 0; i < l; i++) {
            if(str.charAt(i) == '.') {
                dotPosition = i;
                break;
            }
        }
        result += atoi(str.substring(0, dotPosition));
        decimal = atoi(str.substring(dotPosition + 1, l));
        for(int i = 1; i <= l - (dotPosition + 1); i++) decimal /= 10;
        result += decimal;
        return result;
    }
}
