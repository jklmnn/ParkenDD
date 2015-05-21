package de.jkliemann.parkendd;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.DatePicker;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
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

    private Date date;
    private String name;
    private static final int dateOffset = 1900;
    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        RelativeLayout popup = (RelativeLayout)findViewById(R.id.main_layoutPageLoading);
        Intent i = getIntent();
        Calendar cal = Calendar.getInstance();
        date = new Date(i.getIntExtra("year", cal.get(Calendar.YEAR)) - dateOffset, i.getIntExtra("month", cal.get(Calendar.MONTH)), i.getIntExtra("day", cal.get(Calendar.DAY_OF_MONTH)));
        name = i.getStringExtra("name");
        FetchForecast fetchForecast = new FetchForecast();
        fetchForecast.init(this, popup, (TextView)findViewById(R.id.textView));
        fetchForecast.execute("?spot=" + name + "&date=" + dateFormat.format(date));
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
