package de.jkliemann.parkendd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

public class MainActivity extends ActionBarActivity {
    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();
        if(intent.ACTION_SEND.equals(intent.getAction())){
            String uri = intent.getStringExtra(intent.EXTRA_TEXT);
            //Uri geouri = Uri.parse()
            Log.i("Intent", uri.toString());
        }
        if(intent.ACTION_VIEW.equals(intent.getAction())){
            Uri geouri = intent.getData();
            Log.i("geo", geouri.toString());
        }
        ProgressBar popup = (ProgressBar)findViewById(R.id.progressBar);
        popup.setIndeterminate(false);
        popup.setProgress(0);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        gs.initLocation(this);
        Server s = new Server();
        s.setUi((ListView)findViewById(R.id.spotListView), this, popup);
        s.execute(this);
    }

    private void refresh(){
        try{
            Fetch f = new Fetch();
            ProgressBar popup = (ProgressBar)findViewById(R.id.progressBar);
            f.setUi((ListView) findViewById(R.id.spotListView), this, popup);
            f.execute(preferences.getString("fetch_url", getString(R.string.default_fetch_url)));
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
