package de.jkliemann.parkendd;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PlaceActivity extends AppCompatActivity implements LoaderInterface, NavigationView.OnNavigationItemSelectedListener{

    private SharedPreferences preferences;
    private ProgressBar pg;
    private Loader metaLoader;
    private Loader cityLoader;
    private City city;
    private HashMap<Integer, Location> addressMap;
    NavigationView navigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pg = (ProgressBar) findViewById(R.id.progressBar);
        pg.setIndeterminate(false);
        pg.setMax(6);
        pg.setVisibility(View.VISIBLE);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.closed);
        drawer.setDrawerListener(toggle);
        drawer.openDrawer(Gravity.LEFT);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        Uri data = null;
        Intent intent = getIntent();
        if(intent.getAction() == null){
            String query = intent.getExtras().getString("query");
            try {
                query = "geo:0,0?q=" + URLEncoder.encode(query, "UTF-8").replace("+", "%20");
                data = Uri.parse(query);
            }catch (UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
        TextView tv = (TextView) findViewById(R.id.textView);
        tv.setText("");
        if (intent.ACTION_VIEW.equals(intent.getAction())) {
            data = intent.getData();
        }
        URL[] serverurl = new URL[2];
        try {
            serverurl[1] = Loader.getNominatimURL(data);
            serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
            metaLoader = new Loader(this);
            metaLoader.execute(serverurl);
            onProgressUpdated();
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        addressMap = new HashMap<>();
    }

    public void onLoaderFinished(String data[], Loader loader){
        if(loader.equals(metaLoader)){
            ArrayList<City> citylist;
            try{
                citylist = Parser.meta(data[0]);
                ((ParkenDD)getApplication()).updateCities(citylist);
            }catch (JSONException e){
                e.printStackTrace();
            }
            try{
                Location[] loc = Parser.nominatim(data[1]);
                Menu menu = navigationView.getMenu();
                for(int i = 0; i < loc.length; i++) {
                    Logger l = Logger.getLogger("DETAIL");

                    l.log(Level.INFO, loc[i].getExtras().getString("item_detail"));
                    menu.add(0, i, 0, loc[i].getExtras().getString("item_detail"));
                    addressMap.put(i, loc[i]);
                }
                /*TextView tv = (TextView)findViewById(R.id.textView);
                try{
                    tv.setText(loc.getExtras().getString("detail"));
                    refresh();
                }catch (NullPointerException e){
                    e.printStackTrace();
                    tv.setText(getString(R.string.no_address_error));
                    pg.setProgress(0);
                    pg.setVisibility(View.INVISIBLE);
                    ListView spotView = (ListView)findViewById(R.id.spotListView);
                    spotView.setVisibility(View.INVISIBLE);
                }*/

            }catch (JSONException e){
                e.printStackTrace();
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
            }
        }
    }

    public void onExceptionThrown(Exception e){
        if(e instanceof FileNotFoundException) {
            Error.showLongErrorToast(this, getString(R.string.server_error));
        }else if(e instanceof UnknownHostException){
            Error.showLongErrorToast(this, getString(R.string.connection_error));
        }
        pg.setVisibility(View.INVISIBLE);
    }

    public void onProgressUpdated(){
        pg.setProgress(pg.getProgress() + 1);
    }

    private void refresh(){
        URL[] cityurl = new URL[1];
        try{
            city = ((ParkenDD)getApplication()).currentCity();
            setTitle(getString(R.string.app_name) + " - " + city.name());
            cityurl[0] = Loader.getCityUrl(getString(R.string.serveraddress), city);
            cityLoader = new Loader(this);
            cityLoader.execute(cityurl);
            onProgressUpdated();
        }catch (MalformedURLException e){
            e.printStackTrace();
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
        onProgressUpdated();
        pg.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_place, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if(id == R.id.action_map){
            Intent map = new Intent(this, MapActivity.class);
            startActivity(map);
        }

        if (id == R.id.action_settings) {
            Intent settings = new Intent(this, SettingsActivity.class);
            startActivity(settings);
        }

        if(id == R.id.action_forecast){
            Intent forecast = new Intent(this, ForecastActivity.class);
            startActivity(forecast);
        }

        if(id == R.id.action_about){
            Intent about = new Intent(this, AboutActivity.class);
            startActivity(about);
        }

        if(id == R.id.action_refresh){
            pg.setMax(4);
            pg.setProgress(0);
            pg.setVisibility(View.VISIBLE);
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        ((ParkenDD)getApplication()).setLocation(addressMap.get(id));

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
