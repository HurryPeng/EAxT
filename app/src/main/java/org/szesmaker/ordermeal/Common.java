package org.szesmaker.ordermeal;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.jsoup.nodes.Document;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
        }
        catch (Exception e) {
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

    static class Menu implements Serializable {

        Menu() {
            date = null;
            prohibited = true;
            meals = new Meal[3];
            cost = 0;
            return;
        }
        Menu(String date, boolean prohibited, Document doc) {
            this.date = date;
            this.prohibited = prohibited;
            meals = new Meal[3];
            for(int i = 0; i <= 2; i++) {
                String meal = doc.select("table#Repeater1_GvReport_" + i).text();
                if(!meal.equals("")) {
                    boolean orderNothing = doc.select("input#Repeater1_CbkMealtimes_"+i).toString().contains("checked");
                    meals[i] = new Meal(meal, orderNothing);
                }
                else meals[i] = null;
            }
            cost = 0;
            for(Meal meal : meals) if(meal != null) if(!meal.orderNothing) cost += meal.cost;
            return;
        }
        Menu(String serialized) {
            this();
            try {
                byte bytes[] = new byte[serialized.length()];
                for(int i = 0; i < serialized.length(); i++) bytes[i] = (byte) serialized.codePointAt(i);
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
                Menu menu = (Menu) ois.readObject();
                date = menu.date;
                prohibited = menu.prohibited;
                meals = menu.meals;
                cost = menu.cost;
                return;
            }
            catch(Exception e) {
                return;
            }
        }

        String date;
        boolean prohibited;
        Meal meals[];
        double cost;

        private static final long serialVersionUID = 1L;

        void countCost() {
            cost = 0;
            for(Meal meal : meals) if(meal != null) cost += meal.cost;
            return;
        }
        void order(int mealType, int dishId, int num) {
            if(mealType < 0 || mealType > 2) return;
            Meal meal = meals[mealType];
            if(meal == null) return;
            meal.order(dishId, num);
            countCost();
            return;
        }

        public String toString() {
            String str = date + " " + prohibited + "\n";
            for(Meal meal :meals) {
                if(meal == null) str += "nullMeal\n";
                else str += "    " + meal.toString();
            }
            str += cost + "\n";
            return str;
        }

        String serialize() {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(this);
                oos.close();
                byte bytes[] = baos.toByteArray();
                String str = "";
                for(byte b : bytes) str += (char) b;
                return str;
            }
            catch (Exception e) {
                Log.d("TAG", "serialize: "+e.toString());
                return "";
            }
        }
    }

    static class Meal implements Serializable {

        Meal() {
            mealType = 0;
            orderNothing = true;
            dishes = new Dish[9];
            cost = 0;
        }
        Meal(String meal, boolean orderNothing) {
            String temp = meal.substring(meal.indexOf("0"));
            int arrayIndex = 0;
            dishes = new Dish[9];
            for (int i = 0; i <= 9; i++) {
                if (!temp.substring(0, 1).equals(i + "")) continue;
                Dish tempDish = new Dish();
                tempDish.id = i;
                int j = 0, k = 0;
                for (int t = 1; t <= 8; t++) {
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
                            tempDish.unitPrice = Double.valueOf(item);
                            break;
                        }
                        case 6: {
                            tempDish.numCap = Integer.valueOf(item);
                            break;
                        }
                        case 7: {
                            tempDish.order(Integer.valueOf(item));
                        }
                    }
                }
                temp = temp.substring(k + 1);
                dishes[arrayIndex++] = tempDish;
            }

            this.orderNothing = orderNothing;

            cost = 0;
            for(Dish dish : dishes) cost += dish.cost;

            String setMeal;
            setMeal = dishes[0].name;
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
        Dish dishes[];
        double cost;

        private static final long serialVersionUID = 1L;

        void countCost() {
            cost = 0;
            if(orderNothing) return;
            for(Dish dish : dishes) cost += dish.cost;
            return;
        }
        void order(int dishId, int num) {
            if(dishId < 0 || dishId >9) return;
            Dish dish = dishes[dishId];
            dish.order(num);
            countCost();
            return;
        }

        public String toString() {
            String str = mealType + " " + orderNothing + "\n";
            for(Dish dish : dishes) {
                if(dish == null) str += "nullDish\n";
                else str += "    " + dish.toString();
            }
            str += cost + "\n";
            return str;
        }
    }

    static class Dish implements Serializable {

        Dish() {
            id = 0;
            type = null;
            name = null;
            numOrdered = 0;
            numCap = 0;
            unitPrice = 0;
            cost = 0;
            return;
        }
        Dish(int id, String type, String name, int numOrdered, int numCap, int unitPrice) {
            this.id = id;
            this.type = type;
            this.name = name;
            this.numCap = numCap;
            this.unitPrice = unitPrice;
            order(numOrdered);
            return;
        }

        int id;
        String type;
        String name;
        int numOrdered;
        int numCap;
        double unitPrice;
        double cost;

        private static final long serialVersionUID = 1L;

        void countCost() {
            cost = unitPrice * numOrdered;
            return;
        }
        void order(int num) {
            if(num < 0 || num > numCap) return;
            numOrdered = num;
            countCost();
            return;
        }

        public String toString() {
            return id + " " + type + " " + name + " " +numOrdered + " " + numCap + " " + unitPrice + " " + cost + "\n";
        }
    }
}
