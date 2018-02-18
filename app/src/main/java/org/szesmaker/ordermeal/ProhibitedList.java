package org.szesmaker.ordermeal;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.util.Log;
import android.widget.*;
import java.util.*;
public class ProhibitedList extends Activity {
    private CheckBox ordered;
    private ListView list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_holder);
        ordered = (CheckBox) this.findViewById(R.id.ordered);
        list = (ListView) this.findViewById(R.id.list);
        Intent remote = getIntent();
        Common.Meal meal = (Common.Meal) remote.getSerializableExtra("meal");
        ordered.setChecked(true);//To be improved
        ordered.setClickable(false);

        ArrayList<HashMap<String,Object>> ol = new ArrayList<>();

        HashMap<String, Object> map0 = new HashMap<>();
        map0.put("bh","编号");
        map0.put("lb","类别");
        map0.put("cm","菜名");
        map0.put("dj","单价");
        map0.put("dg","份数");
        ol.add(map0);

        for(Common.Dish dish : meal.dishes) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("bh", dish.id);
            map.put("lb", dish.type);
            map.put("cm", dish.name);
            map.put("dj", String.format(Locale.CHINA, "%.2f", dish.unitPrice));
            map.put("dg", dish.numOrdered);
            ol.add(map);
        }

        SimpleAdapter sap = new SimpleAdapter(this, ol, R.layout.listitem, new String[]{"bh", "lb", "cm", "dj", "dg"}, new int[]{R.id.bh, R.id.lb, R.id.cm, R.id.dj, R.id.fs});
        list.setAdapter(sap);
    }
}
