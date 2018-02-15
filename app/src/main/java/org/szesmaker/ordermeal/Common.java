package org.szesmaker.ordermeal;

import android.os.Parcel;
import android.os.Parcelable;
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

    static class Menu implements Parcelable {
        /*
            A Menu class that holds the menu of a whole day.
            Can be initialised with the original web page Document.
            Implements Parcelable so that it can be transmitted with an Intent.
        */
        Menu(){
            date = null;
            prohibited = true;
            cost = 0;
        }
        Menu(String _date, boolean _prohibited, Document doc){
            for(int i = 0; i <= 2; i++) {
                String meal = doc.select("table#Repeater1_GvReport_" + i).text();
                if(!meal.equals("")) {
                    boolean orderNothing = doc.select("input#Repeater1_CbkMealtimes_"+i).toString().contains("checked");
                    meals.add(new Meal(meal, orderNothing));
                }
                else meals.add(null);
            }
            date = _date;
            prohibited = _prohibited;

            cost = 0;
            for(Meal meal : meals) if(meal != null) if(!meal.orderNothing) cost += meal.cost;
            return;
        }

        String date;
        boolean prohibited;
        ArrayList<Meal> meals = new ArrayList<>();
        double cost;

        void countCost() {
            cost = 0;
            for(Meal meal : meals) cost += meal.cost;
            return;
        }
        void order(int mealType, int dishId, int num){
            if(mealType < 0 || mealType > 2) return;
            Meal meal = meals.get(mealType);
            if(meal == null) return;
            meal.order(dishId, num);
            countCost();
            return;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if(this == null) {
                dest.writeByte((byte)0);
                return;
            }
            dest.writeByte((byte)1);
            // The process above is used to prevent reading from a null object.
            // Similar ones can be seen below, which will not be noted like this again.

            dest.writeString(date);
            dest.writeByte((byte)(prohibited ? 1 : 0));
            for(Meal meal : meals){
                if(meal == null){
                    dest.writeByte((byte)0);
                    continue;
                }
                dest.writeByte((byte)1);

                dest.writeInt(meal.mealType);
                dest.writeByte((byte)(meal.orderNothing ? 1 : 0));
                for(Dish dish : meal.dishes){
                    dest.writeInt(dish.id);
                    dest.writeString(dish.type);
                    dest.writeString(dish.name);
                    dest.writeInt(dish.numOrdered);
                    dest.writeInt(dish.numCap);
                    dest.writeDouble(dish.unitPrice);
                    dest.writeDouble(dish.cost);
                }
                dest.writeDouble(meal.cost);
            }
            dest.writeDouble(cost);
        }

        public static final Parcelable.Creator<Menu> CREATOR = new Parcelable.Creator<Menu>() {

            @Override
            public Menu createFromParcel(Parcel source) {
                if(source.readByte() == 0) return null;

                Menu menu = new Menu();
                menu.date = source.readString();
                menu.prohibited = (source.readByte() == 1);
                for(int i = 0; i <= 2; i++){
                    if(source.readByte() == 0) {
                        menu.meals.add(null);
                        continue;
                    }

                    Meal meal = new Meal();
                    meal.mealType = source.readInt();
                    meal.orderNothing = (source.readByte() == 1);
                    for(int j = 0; j <= 9; j++) {
                        if(j == 8) continue;// There's no dish no.8
                        Dish dish = new Dish();
                        dish.id = source.readInt();
                        dish.type = source.readString();
                        dish.name = source.readString();
                        dish.numOrdered = source.readInt();
                        dish.numCap = source.readInt();
                        dish.unitPrice = source.readDouble();
                        dish.cost = source.readDouble();
                        meal.dishes.add(dish);
                    }
                    meal.cost = source.readDouble();
                    menu.meals.add(meal);
                }
                menu.cost = source.readDouble();
                return menu;
            }

            @Override
            public Menu[] newArray(int size) {
                // TODO Auto-generated method stub
                return new Menu[size];
            }
        };
    }

    static class Meal implements Parcelable {
        Meal() {
            mealType = 0;
            orderNothing = true;
            cost = 0;
        }
        Meal(String meal, boolean _orderNothing) {
            String temp = meal.substring(meal.indexOf("0"));
            for (int i = 0; i <= 9; i++)
            {
                if (!temp.substring(0, 1).equals(i + "")) continue;
                Dish tempDish = new Dish();
                tempDish.id = i;
                int j = 0, k = 0;
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
        ArrayList<Dish> dishes = new ArrayList<>();;
        double cost;

        void countCost(){
            cost = 0;
            for(Dish dish : dishes) cost += dish.cost;
            return;
        }
        void order(int dishId, int num){
            if(dishId < 0 || dishId >9) return;
            Dish dish = dishes.get(dishId);
            dish.order(num);
            countCost();
            return;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if(this == null){
                dest.writeByte((byte)0);
                return;
            }
            dest.writeByte((byte)1);
            dest.writeInt(mealType);
            dest.writeByte((byte)(orderNothing ? 1 : 0));
            for(Dish dish : dishes) {
                dest.writeInt(dish.id);
                dest.writeString(dish.type);
                dest.writeSerializable(dish.name);
                dest.writeInt(dish.numOrdered);
                dest.writeInt(dish.numCap);
                dest.writeDouble(dish.unitPrice);
                dest.writeDouble(dish.cost);
            }
            dest.writeDouble(cost);
            return;
        }

        public static final Parcelable.Creator<Meal> CREATOR = new Parcelable.Creator<Meal>() {

            @Override
            public Meal createFromParcel(Parcel source) {
                if(source.readByte() == 0) return null;
                Meal meal = new Meal();
                meal.mealType = source.readInt();
                meal.orderNothing = (source.readByte() == 1);
                for(int i = 0; i <= 9; i++) {
                    if(i == 8) continue;// There's no dish no.8
                    Dish dish = new Dish();
                    dish.id = source.readInt();
                    dish.type = source.readString();
                    dish.name = source.readString();
                    dish.numOrdered = source.readInt();
                    dish.numCap = source.readInt();
                    dish.unitPrice = source.readDouble();
                    dish.cost = source.readDouble();
                    meal.dishes.add(dish);
                }
                meal.cost = source.readDouble();
                return meal;
            }

            @Override
            public Meal[] newArray(int size) {
                return new Meal[0];
            }
        };
    }

    static class Dish {
        Dish(){
            id = 0;
            type = null;
            name = null;
            numOrdered = 0;
            numCap = 0;
            unitPrice = 0;
            cost = 0;
            return;
        }
        Dish(int _id, String _type, String _name, int _numOrdered, int _numCap, int _unitPrice){
            id = _id;
            type = _type;
            name = _name;
            numCap = _numCap;
            unitPrice = _unitPrice;
            order(_numOrdered);
            return;
        }

        int id;
        String type;
        String name;
        int numOrdered;
        int numCap;
        double unitPrice;
        double cost;

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
    }
}
