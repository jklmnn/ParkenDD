package de.jkliemann.parkendd;

import android.graphics.Typeface;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class AboutActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView aboutView = (TextView)findViewById(R.id.aboutView);
        aboutView.setText(getString(R.string.app_name) + " " + getString(R.string.version) + "\n" + getString(R.string.app_url) + "\n" + getString(R.string.disclaimer));

        TextView serverView = (TextView)findViewById(R.id.serverView);
        if(GlobalSettings.getGlobalSettings().getMail() != null && !GlobalSettings.getGlobalSettings().getMail().equals("")){

            serverView.setText(getString(R.string.server) + "\n" + GlobalSettings.getGlobalSettings().getMail());
        }else{
            serverView.setText("");
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_about, menu);
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

        return super.onOptionsItemSelected(item);
    }
}
