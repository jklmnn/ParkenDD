package de.jkliemann.parkendd;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;


public class ForecastActivity extends ActionBarActivity implements FetchForecastInterface{

    private final ForecastActivity _this = this;
    private static final int dateOffset = 1900;
    private Map<ParkingSpot, Map<Date, Integer>> spotmap;
    private Date date;
    private int progress = 0;
    ProgressBar pg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        pg = (ProgressBar)findViewById(R.id.progressBar2);
        pg.setVisibility(View.VISIBLE);
        pg.setIndeterminate(false);
        pg.setMax(FetchForecast.PROGRESS + 3);
        pg.setProgress(progress);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
        datePickerLayout.setVisibility(View.INVISIBLE);
        Button okbutton = (Button)findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout layout = (RelativeLayout)findViewById(R.id.RelativeLayout);
                layout.setVisibility(View.VISIBLE);
                RelativeLayout datePickerLayout = (RelativeLayout) _this.findViewById(R.id.datePickerLayout);
                datePickerLayout.setVisibility(View.INVISIBLE);
                DatePicker datePicker = (DatePicker)findViewById(R.id.datePicker);
                Date date = new Date(datePicker.getYear() - dateOffset, datePicker.getMonth(), datePicker.getDayOfMonth());
                loadDate(date);
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
        Date today = new Date(cal.get(Calendar.YEAR) - dateOffset, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        FetchForecast ff = new FetchForecast(this);
        City city = GlobalSettings.getGlobalSettings().getCityByName(preferences.getString("city", getString(R.string.default_city)));
        String fetchUrl = preferences.getString("fetch_url", getString(R.string.default_fetch_url));
        ff.execute(fetchUrl, city, today);
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                updateList(hourOfDay);
            }
        });
        updateProgress();
    }

    private void loadDate(Date dt){
        pg.setProgress(0);
        pg.setVisibility(View.VISIBLE);
        date = dt;
        FetchForecast ff = new FetchForecast(this);
        City city = GlobalSettings.getGlobalSettings().getCityByName(PreferenceManager.getDefaultSharedPreferences(this).getString("city", getString(R.string.default_city)));
        String fetchUrl = PreferenceManager.getDefaultSharedPreferences(this).getString("fetch_url", getString(R.string.default_fetch_url));
        ff.execute(fetchUrl, city, date);
        updateProgress();
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
            updateProgress();
            setList(spotList);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public void onForecastFinished(final Date date, Map<ParkingSpot, Map<Date, Integer>> forecastMap){
        spotmap = forecastMap;
        this.date = date;
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        updateList(timePicker.getCurrentHour());
    }

    public void updateProgress(){
        progress += 1;
        pg.setProgress(progress);
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
        ListView spotView = (ListView)findViewById(R.id.listView);
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
}
