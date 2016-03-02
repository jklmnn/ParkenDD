package de.jkliemann.parkendd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SearchView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends ActionBarActivity implements LoaderInterface{

    SharedPreferences preferences;
    private final MainActivity _this = this;
    private ProgressBar pg;
    private Loader meta;
    private Loader cityLoader;
    private City city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ParkenDD) getApplication()).getTracker().trackAppDownload();
        setContentView(R.layout.activity_main);
        pg = (ProgressBar)findViewById(R.id.progressBar);
        pg.setIndeterminate(false);
        pg.setProgress(0);
        pg.setMax(6);
        pg.setVisibility(View.VISIBLE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GlobalSettings gs = GlobalSettings.getGlobalSettings();
        gs.initLocation(this);
        URL[] serverurl = new URL[1];
        try {
            serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
            meta = new Loader(this);
            meta.execute(serverurl);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
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
                SearchView search = (SearchView) _this.findViewById(R.id.searchView);
                search.setIconified(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
        onProgressUpdated();
    }

    public void onLoaderFinished(String data[], Loader loader){
        if(loader.equals(meta)){
            ArrayList<City> citylist;
            try{
                citylist = Parser.meta(data[0]);
                GlobalSettings.getGlobalSettings().setCitylist(citylist);
                refresh();
            }catch (JSONException e){
                e.printStackTrace();
                this.pg.setVisibility(View.INVISIBLE);
                this.pg.setProgress(0);
            }
        }
        if(loader.equals(cityLoader)){
            try{
                city = Parser.city(data[0], city);
                setList(city);
                ((ParkenDD) getApplication()).getTracker().trackScreenView("/" + city.id(), city.name());
                TimeZone tz = Calendar.getInstance().getTimeZone();
                DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
                dateFormat.setTimeZone(tz);
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
                timeFormat.setTimeZone(tz);
                String locDate = dateFormat.format(city.last_updated());
                String locTime = timeFormat.format(city.last_updated());
                Error.showLongErrorToast(this, getString(R.string.last_update) + ": " + locDate + " " + locTime);
                onProgressUpdated();
            }catch (JSONException e){
                e.printStackTrace();
                this.pg.setVisibility(View.INVISIBLE);
                this.pg.setProgress(0);
            }
        }
    }

    public void onExceptionThrown(Exception e){
        if(e instanceof FileNotFoundException) {
            Error.showLongErrorToast(this, getString(R.string.server_error));
        }else if(e instanceof UnknownHostException){
            Error.showLongErrorToast(this, getString(R.string.connection_error));
        }
        this.pg.setVisibility(View.INVISIBLE);
        this.pg.setProgress(0);
    }

    public void onProgressUpdated(){
        pg.setProgress(pg.getProgress() + 1);
    }

    private void refresh(){
        if(!GlobalSettings.getGlobalSettings().locationEnabled()){
            GlobalSettings.getGlobalSettings().initLocation(this);
        }
        GlobalSettings.getGlobalSettings().setLocation(null);
        URL[] cityurl = new URL[1];
        try{
            city = GlobalSettings.getGlobalSettings().getCityByName(preferences.getString("city", getString(R.string.default_city)));
            this.setTitle(getString(R.string.app_name) + " - " + city.name());
            cityurl[0] = Loader.getCityUrl(getString(R.string.serveraddress), city);
            cityLoader = new Loader(this);
            cityLoader.execute(cityurl);
            onProgressUpdated();
        }catch (MalformedURLException e){
            this.setTitle(getString(R.string.app_name));
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
            URL[] serverurl = new URL[1];
            try {
                serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
                meta = new Loader(this);
                meta.execute(serverurl);
            }catch (MalformedURLException me){
                me.printStackTrace();
            }
        }
    }

    private void setList(City CITY){
        ExpandableListView spotView = (ExpandableListView)findViewById(R.id.spotListView);
        String sortOptions[] = getResources().getStringArray(R.array.setting_sort_options);
        String sortPreference = PreferenceManager.getDefaultSharedPreferences(this).getString("sorting", sortOptions[0]);
        Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_closed", true);
        Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_nodata", false);
        Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_full", true);
        final ParkingSpot[] spotArray;
        ParkingSpot[] preArray;
        ArrayList<ParkingSpot> spots;
        try {
            spots = CITY.spots();
        }catch (NullPointerException e){
            spots = new ArrayList<>();
            e.printStackTrace();
        }
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
        onProgressUpdated();
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
            pg.setMax(4);
            pg.setProgress(0);
            pg.setVisibility(View.VISIBLE);
            this.refresh();
        }
        if(id == R.id.action_forecast){
            Intent forecast = new Intent(this, ForecastActivity.class);
            startActivity(forecast);
        }
        if(id == R.id.action_map){
            Intent map = new Intent(this, MapActivity.class);
            startActivity(map);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop(){
        super.onStop();
        ((ParkenDD) getApplication()).getTracker().dispatch();
    }
}
