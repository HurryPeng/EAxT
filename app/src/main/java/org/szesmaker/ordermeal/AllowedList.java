package org.szesmaker.ordermeal;

import android.app.*;
import android.content.*;
import android.content.res.Configuration;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.util.*;

public class AllowedList extends Activity
{
    private CheckBox ordered;
    private ListView list;
    private int meal_num = -1, order_num = -1;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    Adpa adapterPortrait;
    Adpa adapterLandscape;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_holder);
        ordered = (CheckBox) this.findViewById(R.id.ordered);
        list = (ListView) this.findViewById(R.id.list);
        sp = getSharedPreferences("orderlist", MODE_PRIVATE);
        editor = sp.edit();
        Intent remote = getIntent();
        int flag = remote.getIntExtra("flag", -1);
        meal_num = flag - 1;
        int order = remote.getIntExtra("ordered", 0);
        String caidan = null;
        switch (flag)
        {
            case 1:
                caidan = remote.getStringExtra("zao");
                break;
            case 2:
                caidan = remote.getStringExtra("wuu");
                break;
            case 3:
                caidan = remote.getStringExtra("wan");
                break;
        }
        //Fake hash. To be improved.
        switch (order)
        {
            case 0:
                ordered.setChecked(false);
                break;
            case 2:
                if (flag == 1)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 4:
                if (flag == 2)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 6:
                if (flag == 1 || flag == 2)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 8:
                if (flag == 3)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 10:
                if (flag == 1 || flag == 3)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 12:
                if (flag == 2 || flag == 3)
                    ordered.setChecked(true);
                else
                    ordered.setChecked(false);
                break;
            case 14:
                ordered.setChecked(true);
                break;
        }

        ArrayList<HashMap<String,Object>> ol = wcd(caidan);
        adapterPortrait = new Adpa(this, ol, Configuration.ORIENTATION_PORTRAIT);
        adapterLandscape = new Adpa(this, ol, Configuration.ORIENTATION_LANDSCAPE);
        Configuration config = getResources().getConfiguration();
        if(config.orientation == Configuration.ORIENTATION_PORTRAIT) list.setAdapter(adapterPortrait);
        else list.setAdapter(adapterLandscape);
    }

    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        switch(config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                list.setAdapter(adapterPortrait);
                break;
            }
            case Configuration.ORIENTATION_LANDSCAPE: {
                list.setAdapter(adapterLandscape);
                break;
            }
        }
    }

    public ArrayList<HashMap<String,Object>> wcd(String caidan)
    {
        ArrayList<HashMap<String,Object>> cd = new ArrayList<HashMap<String,Object>>();
        HashMap<String,Object> map;
        String key[]={"bh","lb","cm","","","dj","zd","fs",""};
        map = new HashMap<String,Object>();
        String str[]={"编号","类别","菜名","","","单价","最大份数","份数",""};
        for (int l = 0;l < 9;l++)
            if (l == 0 || l == 1 || l == 2 || l == 5 || l == 6 || l == 7)
                map.put(key[l], str[l]);
        cd.add(map);
        String temp = caidan.substring(caidan.indexOf("0"));
        for (int i = 0;i <= 9;i++)
        {
            if (!temp.substring(0, 1).equals(i + ""))
                continue;
            map = new HashMap<String,Object>();
            map.put(key[0], i);
            int j = 0,k = 0;
            for (int t=1;t <= 8;t++)
            {
                j = temp.indexOf(" ", k);
                k = temp.indexOf(" ", j + 2);
                String item = temp.substring(j + 1, k);
                if (t == 1 || t == 2 || t == 5 || t == 6 || t == 7)
                    map.put(key[t], item);
            }
            cd.add(map);
            temp = temp.substring(k + 1);
        }
        return cd;
    }
    //Rewrite BaseAdapter
    //To be replaced with TabLayout
    class Adpa extends BaseAdapter
    {
        Context context;
        ArrayList<HashMap<String,Object>> ol;
        int orientation;

        public Adpa(Context context, ArrayList<HashMap<String,Object>> ol, int orientation)
        {
            this.context = context;
            this.ol = ol;
            this.orientation = orientation;
        }

        @Override
        public int getCount()
        {
            return ol.size();
        }

        @Override
        public Object getItem(int position)
        {
            return null;
        }

        @Override
        public long getItemId(int posotion)
        {
            return 0;
        }

        @Override
        public View getView(int position, View convert, ViewGroup parent)
        {
            final int dish_num = position;
            ViewHolder viewholder;
            if (convert == null)
            {
                viewholder = new ViewHolder();
                convert = LayoutInflater.from(context).inflate(R.layout.listitem, null);
                viewholder.bh = (TextView) convert.findViewById(R.id.bh);
                if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    viewholder.lb = (TextView) convert.findViewById(R.id.lb);
                }
                viewholder.cm = (TextView) convert.findViewById(R.id.cm);
                viewholder.dj = (TextView) convert.findViewById(R.id.dj);
                viewholder.fs = (TextView) convert.findViewById(R.id.fs);
                convert.setTag(viewholder);
            }
            else
            {
                viewholder = (ViewHolder) convert.getTag();
            }

            list.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                    if(position == 0) return;
                    else
                    {

                        int numCap=ol.get(position).get("zd").toString().charAt(0)-'0';
                        TextView numTv = view.findViewById(R.id.fs);
                        int num=numTv.getText().charAt(0)-'0';
                        num=(num+1)%(numCap+1);
                        numTv.setText(String.valueOf(num));

                        //Keep state in ol updated
                        HashMap<String,Object> map;
                        map = ol.get(position);
                        map.put("fs", Integer.toString(num));
                        ol.set(position, map);
                        editor.putString("Repeater1_GvReport_" + meal_num + "_TxtNum_" + (position - 1) + "@", num + "|");
                        editor.apply();
                        return;
                    }
                }
            });

            viewholder.bh.setText(ol.get(position).get("bh").toString());
            if(orientation == Configuration.ORIENTATION_LANDSCAPE) {
                viewholder.lb.setText(ol.get(position).get("lb").toString());
            }
            viewholder.cm.setText(ol.get(position).get("cm").toString());
            viewholder.dj.setText(ol.get(position).get("dj").toString());
            viewholder.fs.setText(ol.get(position).get("fs").toString());

            viewholder.fs.setVisibility(View.VISIBLE);
            String num = ol.get(position).get("fs").toString();
            String top = ol.get(position).get("zd").toString();
            viewholder.fs.setText(num);
            viewholder.fs.setTextColor(0xff00b0ff);

            return convert;
        }
        class ViewHolder
        {
            TextView bh,lb,cm,dj,fs;
        }
    }
    private long exittime = -2001;
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (keyCode == KeyEvent.KEYCODE_BACK)
        {
            if (System.currentTimeMillis() - exittime > 2000)
            {
                //To be replaced with Snackbar
                Toast.makeText(this, "订单未提交，返回？", Toast.LENGTH_SHORT).show();
                exittime = System.currentTimeMillis();
                return true;
            }
            else
            {
                editor.clear();
                editor.commit();
                this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
