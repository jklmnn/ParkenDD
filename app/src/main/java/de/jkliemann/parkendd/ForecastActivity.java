package de.jkliemann.parkendd;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.TimePicker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class ForecastActivity extends ActionBarActivity {

    private HashMap<Date, Integer> forecast_data;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DatePicker datePicker;
    private TimePicker timePicker;
    private TextView tv;

    private void parseForecast(){
        forecast_data = new HashMap<Date, Integer>();
        InputStream inputStream = getResources().openRawResource(R.raw.forecast);
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        try{
            String line;
            while ((line = br.readLine()) != null){
                String[] raw = line.split(",");

                try {
                    forecast_data.put(dateFormat.parse(raw[0]), Integer.parseInt(raw[1]));
                }catch (ParseException e){
                    e.printStackTrace();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public int getForecastByDate(Date d){
        try {
            //tv.setText(forecast_data.get(d).toString());
            tv.setText("");
            return 100 - forecast_data.get(d).intValue();
        }catch(Exception e){
            e.printStackTrace();
            tv.setText(getString(R.string.nodata));
        }
        return 0;
    }

    private void setStars(int percentage){
        float rating = (float)percentage / 20;
        RatingBar ratingBar = (RatingBar)findViewById(R.id.ratingBar);
        ratingBar.setRating(rating);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        this.setTitle(R.string.action_forecast);
        tv = (TextView)findViewById(R.id.textView);
        tv.setText(getString(R.string.nodata));
        datePicker = (DatePicker)findViewById(R.id.datePicker);
        timePicker = (TimePicker)findViewById(R.id.timePicker);
        datePicker.setCalendarViewShown(false);
        timePicker.setIs24HourView(true);
        Calendar c = Calendar.getInstance();
        datePicker.init(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH), new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                setStars(getForecastByDate(new Date(year - 1900, monthOfYear, dayOfMonth)));
            }
        });
        parseForecast();
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

        return super.onOptionsItemSelected(item);
    }
}
