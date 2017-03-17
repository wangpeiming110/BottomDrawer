package com.wangpm.bottomdrawer;

import android.app.Activity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;

import java.util.ArrayList;

public class MainActivity extends Activity {
    ListView listView;
    private BottomDrawerLayout bottom_drawer_layout;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.list_view);
        ArrayList<String> list=new ArrayList<>();
        for(int i = 0;i<30;i++) {
            list.add("item" + i);
        }
        listView.setAdapter(new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                android.R.id.text1,
                list)
        );

        bottom_drawer_layout = (BottomDrawerLayout) findViewById(R.id.bottom_drawer_layout);
        bottom_drawer_layout.setOnDrawerStatusChanged(new BottomDrawerLayout.OnDrawerStatusChanged() {
            @Override
            public void onChanged(int parentHeight, int drawerTop) {

                //LayoutParams 的类型要用设置时的view的parentview的类型
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                lp.setMargins(0,(int) (1*density), 0, parentHeight - drawerTop+(int) (1*density));
//                lp.addRule(RelativeLayout.BELOW,R.id.head);
                listView.setLayoutParams(lp);

            }
        });

    }


    private void initData() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        density  = dm.density;        // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
    }
}
