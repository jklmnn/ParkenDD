package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;


public class PlaceActivity extends ActionBarActivity implements ServerInterface, FetchInterface, NominatimInterface{

    private SharedPreferences preferences;
    private final PlaceActivity _this = this;
    private ProgressBar pg;
    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        pg = (ProgressBar) findViewById(R.id.progressBar);
        pg.setIndeterminate(false);
        pg.setProgress(progress);
        pg.setMax(Fetch.PROGRESS + Server.PROGRESS + NominatimOSM.PROGRESS + 1);
        pg.setVisibility(View.VISIBLE);
        Object data = null;
        Intent intent = getIntent();
        if(intent.getAction() == null){
            String query = intent.getExtras().getString("query");
            try {
                if(!query.contains(preferences.getString("city", getString(R.string.default_city)))){
                    query = "Dresden " + query;
                }
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
        Log.i("DATA", data.toString());
        Server s = new Server(this);
        NominatimOSM nosm = new NominatimOSM(this);
        nosm.execute(data);
        s.execute(getString(R.string.serveraddress));
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

    public void onNominatimFinished(Location loc){
        GlobalSettings.getGlobalSettings().setLocation(loc);
        TextView tv = (TextView)findViewById(R.id.textView);
        try{
            tv.setText(loc.getExtras().getString("detail"));
        }catch (NullPointerException e){
            e.printStackTrace();
            tv.setText(getString(R.string.no_address_error));
            ListView spotView = (ListView)findViewById(R.id.spotListView);
            spotView.setVisibility(View.INVISIBLE);
        }
    }

    private void refresh(){
        Fetch f = new Fetch(this);
        f.execute(getString(R.string.serveraddress), preferences.getString("city", getString(R.string.default_city)));
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
        updateProgress();
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

        if(id == R.id.action_forecast){
            Intent forecast = new Intent(this, ForecastActivity.class);
            startActivity(forecast);
        }

        if(id == R.id.action_refresh){
            progress = 0;
            pg.setProgress(progress);
            pg.setVisibility(View.VISIBLE);
            refresh();
        }

        return super.onOptionsItemSelected(item);
    }
}
