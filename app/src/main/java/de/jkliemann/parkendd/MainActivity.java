package de.jkliemann.parkendd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;


public class MainActivity extends ActionBarActivity {
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        setContentView(R.layout.activity_main);
        refresh();
    }

    private void refresh(){
        try{
            Fetch f = new Fetch();
            RelativeLayout popup = (RelativeLayout)findViewById(R.id.main_layoutPageLoading);
            f.setUi((ListView)findViewById(R.id.spotListView), this, popup);
            f.execute(preferences.getString("fetch_url", null));
        }catch(Exception e){
            e.printStackTrace();
            Error.showLongErrorToast(this, e.getMessage());
        }
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
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
       }
        if(id == R.id.action_about){
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
        }
        if(id == R.id.action_refresh){
            this.refresh();
        }

        return super.onOptionsItemSelected(item);
    }
}
