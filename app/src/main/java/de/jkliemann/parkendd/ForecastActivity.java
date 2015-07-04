package de.jkliemann.parkendd;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;


public class ForecastActivity extends ActionBarActivity {

    private final ForecastActivity _this = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);
        RelativeLayout datePickerLayout = (RelativeLayout)findViewById(R.id.datePickerLayout);
        datePickerLayout.setVisibility(View.INVISIBLE);
        Button okbutton = (Button)findViewById(R.id.okbutton);
        okbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout datePickerLayout = (RelativeLayout)_this.findViewById(R.id.datePickerLayout);
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
}
