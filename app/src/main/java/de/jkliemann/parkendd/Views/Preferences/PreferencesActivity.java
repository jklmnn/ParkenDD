package de.jkliemann.parkendd.Views.Preferences;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import de.jkliemann.parkendd.R;

public class PreferencesActivity extends AppCompatActivity implements PreferencesFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(R.id.settings_fragment_container, new PreferencesFragment()).commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
