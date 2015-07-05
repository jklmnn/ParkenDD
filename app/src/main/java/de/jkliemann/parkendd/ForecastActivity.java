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
import android.widget.RelativeLayout;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;


public class ForecastActivity extends ActionBarActivity implements FetchForecastInterface{

    private final ForecastActivity _this = this;
    private static final int dateOffset = 1900;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
        datePickerLayout.setVisibility(View.INVISIBLE);
        Button okbutton = (Button)findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout datePickerLayout = (RelativeLayout) _this.findViewById(R.id.datePickerLayout);
                datePickerLayout.setVisibility(View.INVISIBLE);
            }
        });
        Button cancelbutton = (Button)findViewById(R.id.cancelbutton);
        cancelbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout datePickerLayout = (RelativeLayout)_this.findViewById(R.id.datePickerLayout);
                datePickerLayout.setVisibility(View.INVISIBLE);
            }
        });
        Calendar cal = Calendar.getInstance();
        Date today = new Date(cal.get(Calendar.YEAR) - dateOffset, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_WEEK));
        FetchForecast ff = new FetchForecast(this);
        City city = GlobalSettings.getGlobalSettings().getCityByName(preferences.getString("city", getString(R.string.default_city)));
        ff.execute(getString(R.string.default_fetch_url), city, today);
    }

    public void onForecastFinished(final Date date, Map<ParkingSpot, Map<Date, Integer>> forecastMap){
        final Map<ParkingSpot, Map<Date, Integer>> spotmap = forecastMap;
        TimePicker timePicker = (TimePicker)findViewById(R.id.timePicker);
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                ArrayList<ParkingSpot> spotList = new ArrayList<>();
                Iterator it = spotmap.entrySet().iterator();
                date.setHours(hourOfDay);
                date.setMinutes(minute);
                while (it.hasNext()){
                    Map.Entry pair = (Map.Entry)it.next();
                    ParkingSpot spot = (ParkingSpot)pair.getKey();
                    Map<Date, Integer> dataMap = (Map)pair.getValue();
                    it.remove();
                    try{
                        spot.setState("open");
                        spot.setFree(dataMap.get(date));
                    }catch (NullPointerException e){
                        e.printStackTrace();
                        spot.setState("nodata");
                    }
                    spotList.add(spot);
                }
                setList(spotList);
            }
        });
    }

    public void updateProgress(){

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
            RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
            datePickerLayout.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setList(ArrayList<ParkingSpot> spots){
        ListView spotView = (ListView)findViewById(R.id.spotListView);
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
        /*spotView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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
        });*/
    }
}
