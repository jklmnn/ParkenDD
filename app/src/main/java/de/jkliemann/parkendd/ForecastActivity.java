package de.jkliemann.parkendd;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import org.json.JSONException;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;


public class ForecastActivity extends ActionBarActivity implements LoaderInterface{

    private final ForecastActivity _this = this;
    private static final int dateOffset = 1900;
    private Map<ParkingSpot, Map<Date, Integer>> spotmap;
    private Date date;
    ProgressBar pg;
    ParkingSpot[] spotList;
    Loader forecastLoader;
    DateFormat dateFormat;
    City city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        pg = (ProgressBar)findViewById(R.id.progressBar2);
        pg.setVisibility(View.VISIBLE);
        pg.setIndeterminate(true);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
        datePickerLayout.setVisibility(View.INVISIBLE);
        Button okbutton = (Button)findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout layout = (RelativeLayout) findViewById(R.id.RelativeLayout);
                layout.setVisibility(View.VISIBLE);
                RelativeLayout datePickerLayout = (RelativeLayout) _this.findViewById(R.id.datePickerLayout);
                datePickerLayout.setVisibility(View.INVISIBLE);
                DatePicker datePicker = (DatePicker) findViewById(R.id.datePicker);
                date = new Date(datePicker.getYear() - dateOffset, datePicker.getMonth(), datePicker.getDayOfMonth());
                loadDate();
                String locDate = dateFormat.format(date);
                setTitle(city.name() + " - " + locDate);
            }
        });
        Button cancelbutton = (Button)findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout);
                layout.setVisibility(View.VISIBLE);
                RelativeLayout datePickerLayout = (RelativeLayout)_this.findViewById(R.id.datePickerLayout);
                datePickerLayout.setVisibility(View.INVISIBLE);
            }
        });
        Calendar cal = Calendar.getInstance();
        date = new Date(cal.get(Calendar.YEAR) - dateOffset, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        TimeZone tz = Calendar.getInstance().getTimeZone();
        dateFormat = android.text.format.DateFormat.getDateFormat(this);
        dateFormat.setTimeZone(tz);
        String locDate = dateFormat.format(date);
        city = GlobalSettings.getGlobalSettings().getCityByName(preferences.getString("city", getString(R.string.default_city)));
        setTitle(city.name() + " - " + locDate);
        loadDate();
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setIs24HourView(true);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateList(hourOfDay);
            }
        });
        ((ParkenDD) getApplication()).getTracker().trackScreenView("/forecast/" + city.id(), "Vorhersage-" + city.name());
    }

    private void loadDate(){
        pg.setProgress(0);
        pg.setVisibility(View.VISIBLE);
        City city = GlobalSettings.getGlobalSettings().getCityByName(PreferenceManager.getDefaultSharedPreferences(this).getString("city", getString(R.string.default_city)));
        ArrayList<ParkingSpot> spots = city.spots();
        ArrayList<ParkingSpot> forecastSpots = new ArrayList<>();
        for(ParkingSpot spot : spots){
            if(spot.forecast()){
                forecastSpots.add(spot);
            }
        }
        spotList = forecastSpots.toArray(new ParkingSpot[forecastSpots.size()]);
        URL[] urlList = new URL[forecastSpots.size()];
        try{
            for(int i = 0; i < forecastSpots.size(); i++){
                urlList[i] = Loader.getForecastUrl(getString(R.string.serveraddress), city, spotList[i], date);
            }
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
        pg.setMax(urlList.length + 3);
        pg.setProgress(1);
        pg.setIndeterminate(false);
        forecastLoader = new Loader(this);
        forecastLoader.execute(urlList);
    }

    private void updateList(int hour){
        ArrayList<ParkingSpot> spotList = new ArrayList<>();
        try {
            date.setHours(hour);
            date.setMinutes(0);
            for(Map.Entry<ParkingSpot, Map<Date, Integer>> pair : spotmap.entrySet()){
                ParkingSpot spot = pair.getKey();
                Map<Date, Integer> dataMap = (Map) pair.getValue();
                try {
                    spot.setState("open");
                    double perc = 1 - (double)dataMap.get(date) / 100;
                    double free = (double)spot.count() * perc;
                    spot.setFree((int) free);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    spot.setState("nodata");
                }
                spotList.add(spot);
            }
            setList(spotList);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void onLoaderFinished(String data[], Loader loader){
        spotmap = new HashMap<>();
        Map<Date, Integer> dateMap;
        for (int i = 0; i < data.length; i++) {
            try {
                dateMap = Parser.forecast(data[i]);
            } catch (JSONException e) {
                e.printStackTrace();
                dateMap = new HashMap<>();
            } catch (ParseException e) {
                e.printStackTrace();
                dateMap = new HashMap<>();
            }
            spotmap.put(spotList[i], dateMap);
        }
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        updateList(timePicker.getCurrentHour());
        onProgressUpdated();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_datePicker) {
            RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout);
            layout.setVisibility(View.INVISIBLE);
            RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
            datePickerLayout.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setList(ArrayList<ParkingSpot> spots){
        ExpandableListView spotView = (ExpandableListView)findViewById(R.id.listView);
        String sortOptions[] = getResources().getStringArray(R.array.setting_sort_options);
        String sortPreference = PreferenceManager.getDefaultSharedPreferences(this).getString("sorting", sortOptions[0]);
        Boolean hide_closed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_closed", true);
        Boolean hide_nodata = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_nodata", false);
        Boolean hide_full = PreferenceManager.getDefaultSharedPreferences(this).getBoolean("hide_full", true);
        final ParkingSpot[] spotArray;
        ParkingSpot[] preArray;
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
}
