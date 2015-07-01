package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class MainActivity extends ActionBarActivity {

    private final MainActivity _this = this;

    SharedPreferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ProgressBar pg = (ProgressBar)findViewById(R.id.progressBar);
        pg.setVisibility(View.VISIBLE);
        pg.setIndeterminate(false);
        pg.setProgress(0);
        Intent intent = getIntent();
        if(intent.ACTION_SEND.equals(intent.getAction())){
            String uri = intent.getStringExtra(intent.EXTRA_TEXT);
            //Uri geouri = Uri.parse()
            Log.i("Intent", uri.toString());
        }
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        gs.initLocation(this);
        Server s = new Server();
        s.execute(preferences.getString("fetch_url", getString(R.string.default_fetch_url)));
        try{
            ArrayList<City> citylist = s.get();
            gs.setCitylist(citylist);
            refresh(20);
        }catch (InterruptedException e){
            e.printStackTrace();
        }catch (ExecutionException e){
            e.printStackTrace();
        }
        pg.setProgress(0);
        pg.setVisibility(View.INVISIBLE);
    }

    private void refresh(int progress){
        ProgressBar pg = (ProgressBar)findViewById(R.id.progressBar);
        pg.setProgress(progress);
        this.setTitle(getString(R.string.app_name) + " - " + preferences.getString("city", getString(R.string.default_city)));
        try{
            Fetch f = new Fetch();
            f.execute(preferences.getString("fetch_url", getString(R.string.default_fetch_url)), preferences.getString("city", getString(R.string.default_city)));
            try{
                City city = f.get();
                pg.setProgress(70);
                setList(city);
            }catch (InterruptedException e){
                e.printStackTrace();
            }catch (ExecutionException e){
                e.printStackTrace();
            }
        }catch(Exception e){
            e.printStackTrace();
            Error.showLongErrorToast(this, e.getMessage());
        }
    }

    private void setList(City CITY){
        ListView spotView = (ListView)findViewById(R.id.spotListView);
        String sortOptions[] = getResources().getStringArray(R.array.setting_sort_options);
        String sortPreference = PreferenceManager.getDefaultSharedPreferences(this).getString("sorting", sortOptions[0]);
        Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_closed", true);
        Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_nodata", false);
        Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_full", true);
        final ParkingSpot[] spotArray;
        ParkingSpot[] preArray;
        ArrayList<ParkingSpot> spots = CITY.spots();
        ArrayList<ParkingSpot> cachelist = new ArrayList<>();
        for(ParkingSpot spot : spots){
            if(hide_closed && spot.state().equals("closed")){
                cachelist.add(spot);
            }
            if(hide_nodata && spot.state().equals("nodata")){
                cachelist.add(spot);
            }
            if(hide_full && spot.free() == 0 && !spot.state().equals("nodata") && !spot.state().equals("closed")){
                cachelist.add(spot);
            }
        }
        for (ParkingSpot spot : cachelist){
            spots.remove(spot);
        }
        if(sortPreference.equals(sortOptions[0])){
            try{
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byEUKLID.INSTANCE);
            }catch (NullPointerException e){
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[1])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byNAME.INSTANCE);
            }catch (NullPointerException e){
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[2])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byDISTANCE.INSTANCE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else if(sortPreference.equals(sortOptions[3])) {
            try {
                preArray = ParkingSpot.getSortedArray(spots.toArray(new ParkingSpot[spots.size()]), ParkingSpot.byFREE.INSTANCE);
            } catch (NullPointerException e) {
                e.printStackTrace();
                preArray = spots.toArray(new ParkingSpot[spots.size()]);
            }
        }else{
            preArray = spots.toArray(new ParkingSpot[spots.size()]);
        }
        spotArray = preArray;
        SlotListAdapter adapter = new SlotListAdapter(this, spotArray);
        spotView.setAdapter(adapter);
        spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent details = new Intent(_this, DetailsActivity.class);
                    details.putExtra("spot", spotArray[position]);
                    _this.startActivity(details);

                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                    Error.showLongErrorToast(_this, getString(R.string.intent_error));
                }
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
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        }
        if(id == R.id.action_about){
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
        }
        if(id == R.id.action_refresh){
            ProgressBar pg = (ProgressBar)findViewById(R.id.progressBar);
            pg.setVisibility(View.VISIBLE);
            this.refresh(0);
            pg.setVisibility(View.INVISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }
}
