package org.szesmaker.ordermeal;

import org.jsoup.nodes.Document;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;

/**
 * Created by HurryPeng on 2018.02.11.
 * Includes common functions and classes used in activities.
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

    static String getDateString(int year, int month, int day) {
        String str = year + "-";
        month++;
        if (month < 10)
            str += "0";
        str = str + month + "-";
        if (day < 10)
            str += "0";
        str += day;
        return str;
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

    class Menu {
        Menu(String _date, Document doc){
            for(int i = 0; i <= 2; i++){
                String meal = doc.select("table#Repeater1_GvReport_" + i).text();
                if(meal != ""){
                    boolean orderNothing = doc.select("input#Repeater1_CbkMealtimes_"+i).toString().contains("checked");
                    meals.add(new Meal(meal, orderNothing));
                }
                else meals.add(null);
            }

            date = _date;

            cost = 0;
            for(Meal meal : meals) if(meal != null) if(!meal.orderNothing) cost += meal.cost;

            return;
        }

        String date;
        ArrayList<Meal> meals;
        double cost;

        void order(int mealType, int dishId, int num){
            if(mealType < 0 || mealType > 2) return;
            Meal meal = meals.get(mealType);
            if(meal == null) return;
            if(!meal.orderNothing) cost -= meal.cost;
            meal.order(dishId, num);
            if(!meal.orderNothing) cost += meal.cost;
            return;
        }
    }

    class Meal {
        Meal(String meal, boolean _orderNothing) {
            String temp = meal.substring(meal.indexOf("0"));
            for (int i = 0; i <= 9; i++)
            {
                if (!temp.substring(0, 1).equals(i + "")) continue;
                Dish tempDish = new Dish();
                int j = 0,k = 0;
                for (int t = 1; t <= 8; t++)
                {
                    j = temp.indexOf(" ", k);
                    k = temp.indexOf(" ", j + 2);
                    String item = temp.substring(j + 1, k);
                    switch(t) {
                        case 1: {
                            tempDish.type = item;
                            break;
                        }
                        case 2: {
                            tempDish.name = item;
                            break;
                        }
                        case 5: {
                            tempDish.unitPrice = atof(item);
                            break;
                        }
                        case 6: {
                            tempDish.numCap = atoi(item);
                            break;
                        }
                        case 7: {
                            tempDish.order(atoi(item));
                        }
                    }
                }
                temp = temp.substring(k + 1);
                dishes.add(tempDish);
            }

            orderNothing = _orderNothing;

            cost = 0;
            for(Dish dish : dishes) cost += dish.cost;

            String setMeal;
            setMeal = dishes.get(0).name;
            switch (setMeal){
                case "早餐套餐": {
                    mealType = 0;
                    break;
                }
                case "午餐套餐": {
                    mealType = 1;
                    break;
                }
                case "晚餐套餐": {
                    mealType = 2;
                    break;
                }
            }

            return;
        }

        int mealType;
        boolean orderNothing;
        ArrayList<Dish> dishes;
        double cost;

        void order(int dishId, int num){
            if(dishId < 0 || dishId >9) return;
            Dish dish = dishes.get(dishId);
            cost -= dish.cost;
            dish.order(num);
            cost += dish.cost;
            return;
        }
    }

    class Dish {
        Dish(){
            type = null;
            name = null;
            numOrdered = 0;
            numCap = 0;
            unitPrice = 0;
            cost = 0;
            return;
        }
        Dish(String _type, String _name, int _numOrdered, int _numCap, int _unitPrice){
            type = _type;
            name = _name;
            numCap = _numCap;
            unitPrice = _unitPrice;
            order(_numOrdered);
            return;
        }

        String type;
        String name;
        int numOrdered;
        int numCap;
        double unitPrice;
        double cost;

        void order(int num){
            if(num < 0 || num > numCap) return;
            numOrdered = num;
            cost = unitPrice * num;
            return;
        }
    }
}
