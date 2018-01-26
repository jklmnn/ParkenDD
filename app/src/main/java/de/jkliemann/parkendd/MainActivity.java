package de.jkliemann.parkendd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements LoaderInterface, NavigationView.OnNavigationItemSelectedListener{

    SharedPreferences preferences;
    private final MainActivity _this = this;
    private ProgressBar pg;
    private Loader meta;
    private Loader cityLoader;
    private City city;
    private NavigationView navigationView;
    private MenuItem map_action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.closed);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);



        pg = (ProgressBar)findViewById(R.id.progressBar);
        pg.setIndeterminate(false);
        pg.setProgress(0);
        pg.setMax(6);
        pg.setVisibility(View.VISIBLE);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        reloadIndex();
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

    private void updateMenu(ArrayList<City> citylist){
        Menu menu = navigationView.getMenu();
        menu.clear();
        MenuItem auto;
        auto = menu.add(0, 0, 0, getString(R.string.setting_city_auto));
        auto.setCheckable(true);
        int id = 1;
        try {
            for (City city : citylist) {
                id++;
                MenuItem item;
                try {
                    String dst = "(" + Util.getViewDistance(Util.getDistance(city.location(), ((ParkenDD) getApplication()).location())) + ")";
                    item = menu.add(0, id, 0, city.name() + " " + dst);
                }catch (NullPointerException e){
                    e.printStackTrace();
                    item = menu.add(0, id, 0, city.name());
                }
                item.setCheckable(true);
                ((ParkenDD)getApplication()).addCityPair(id, city);
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    private void reloadIndex(){
        URL[] serverurl = new URL[1];
        try {
            serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
            meta = new Loader(this);
            meta.execute(serverurl);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public void onLoaderFinished(String data[], Loader loader){
        if(loader.equals(meta)){
            ArrayList<City> citylist;
            try{
                citylist = Parser.meta(data[0]);
                updateMenu(((ParkenDD) getApplication()).getActiveCities(citylist));
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
                TimeZone tz = Calendar.getInstance().getTimeZone();
                DateFormat dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
                dateFormat.setTimeZone(tz);
                DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
                timeFormat.setTimeZone(tz);
                String locDate = dateFormat.format(city.last_updated());
                String locTime = timeFormat.format(city.last_updated());
                Error.showLongErrorToast(this, getString(R.string.last_update) + ": " + locDate + " " + locTime);
                map_action.setEnabled(true);
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
        if(!((ParkenDD)getApplication()).locationEnabled()){
            ((ParkenDD)getApplication()).initLocation(this);
        }
        ((ParkenDD)getApplication()).setLocation(null, this);
        URL[] cityurl = new URL[1];
        try{
            city = ((ParkenDD)getApplication()).currentCity();
            String comment = "";
            if(!city.contributor().equals("")){
                comment += city.contributor();
                String license = city.license();
                if(!license.equals("")){
                    if(license.contains("http")){
                        comment += " - " + license.split("http")[0].trim();
                    }else {
                        comment += " - " + license;
                    }
                }
            }
            this.setTitle(city.name());
            ((TextView)findViewById(R.id.comment)).setText(comment);
            ((TextView)findViewById(R.id.title)).setText(getString(R.string.app_name) + " - " + city.name());
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
        map_action = menu.findItem(R.id.action_map);
        map_action.setEnabled(false);
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
            map_action.setEnabled(false);
            pg.setMax(4);
            pg.setProgress(0);
            pg.setVisibility(View.VISIBLE);
            reloadIndex();
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        ((ParkenDD) getApplication()).setCurrentCity(id);

        pg.setMax(4);
        pg.setProgress(0);
        pg.setVisibility(View.VISIBLE);

        refresh();

        /*if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
