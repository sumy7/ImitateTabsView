package com.sumy.imitatetabsview.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.sumy.imitatetabsview.ImitateTabsView;

public class MainActivity extends Activity {
    public static String[] TITLES = new String[]{"asdag", "adfadf", "111111", "adfadfadf", "asdfaghqewtgadg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImitateTabsView view = (ImitateTabsView) findViewById(R.id.tabview);
        final ImitateTabsView view1 = (ImitateTabsView) findViewById(R.id.tabview1);

        view.addAllTabs(TITLES);
        view1.addAllTabs(TITLES);

        view.setOnTabItemClickListener(new ImitateTabsView.OnTabItemClickListener() {
            @Override
            public void onItemClick(int position) {
                view1.setCurrentSelectTab(position);
            }
        });
        view1.setOnTabItemClickListener(new ImitateTabsView.OnTabItemClickListener() {
            @Override
            public void onItemClick(int position) {
                view.setCurrentSelectTab(position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
