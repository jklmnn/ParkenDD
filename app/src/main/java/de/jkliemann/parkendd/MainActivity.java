package de.jkliemann.parkendd;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends ActionBarActivity implements ServerInterface, FetchInterface {

    SharedPreferences preferences;
    private final MainActivity _this = this;
    private ProgressBar pg;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pg = (ProgressBar)findViewById(R.id.progressBar);
        pg.setIndeterminate(false);
        pg.setVisibility(View.VISIBLE);
        pg.setProgress(0);
        pg.setMax(Fetch.PROGRESS + Server.PROGRESS + 1);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        gs.initLocation(this);
        Server s = new Server(this);
        s.execute(getString(R.string.serveraddress));
        SearchView search = (SearchView)findViewById(R.id.searchView);
        search.setSubmitButtonEnabled(true);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent place = new Intent(_this, PlaceActivity.class);
                Bundle extra = new Bundle();
                extra.putString("query", s);
                place.putExtras(extra);
                startActivity(place);
                SearchView search = (SearchView)_this.findViewById(R.id.searchView);
                search.setIconified(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    public void onMetaFinished(ArrayList<City> cities){
        GlobalSettings.getGlobalSettings().setCitylist(cities);
        refresh();
    }

    public void onFetchFinished(City city){
        setList(city);
    }

    public void updateProgress(){
        progress += 1;
        pg.setProgress(progress);
    }

    private void refresh(){
        GlobalSettings.getGlobalSettings().setLocation(null);
        this.setTitle(getString(R.string.app_name) + " - " + preferences.getString("city", getString(R.string.default_city)));
        try{
            Fetch f = new Fetch(this);
            f.execute(getString(R.string.serveraddress), preferences.getString("city", getString(R.string.default_city)));
        }catch(Exception e){
            e.printStackTrace();
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
        updateProgress();
        pg.setVisibility(View.INVISIBLE);
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
            pg.setVisibility(View.VISIBLE);
            progress = 0;
            pg.setProgress(0);
            pg.setMax(Fetch.PROGRESS + 1);
            this.refresh();
        }
        if(id == R.id.action_forecast){
            Intent forecast = new Intent(this, ForecastActivity.class);
            startActivity(forecast);
        }

        return super.onOptionsItemSelected(item);
    }
}
