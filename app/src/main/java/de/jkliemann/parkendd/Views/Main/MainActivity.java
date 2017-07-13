package de.jkliemann.parkendd.Views.Main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.Web.Loader;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Views.ForecastActivity;
import de.jkliemann.parkendd.Views.SettingsActivity;

public class MainActivity extends AppCompatActivity implements LocalParkingSlotListFragment.OnFragmentInteractionListener {

    SharedPreferences preferences;
    private final MainActivity _this = this;
    public ProgressBar progressBar;
    private Loader meta;
    private Loader cityLoader;
    private City city;
    private NavigationView navigationView;
    private FrameLayout fragmentLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ParkenDD) getApplication()).getTracker().trackAppDownload();
        setContentView(R.layout.activity_main);

        preferences = PreferenceManager.getDefaultSharedPreferences(this);

        setUpNavigationDrawer();
        setupProgressBar();
        setFragment(LocalParkingSlotListFragment.newInstance());

    }

    private void setupProgressBar() {
        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setIndeterminate(false);
        progressBar.setProgress(0);
        progressBar.setMax(6);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void setUpNavigationDrawer() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.open, R.string.closed);
        drawer.setDrawerListener(toggle);
        toggle.syncState();


        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                int id = menuItem.getItemId();

                menuItem.setChecked(true);
                switch (id)
                {
                    case R.id.action_settings:
                        Intent settings = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(settings);
                        break;
                    case R.id.action_cities:
                        // TODO : Activity to choose cities
                        break;
                    case R.id.action_spots:
                        // TODO : implement spots/map/prevision as fragments
                        break;
                    case R.id.action_forecast:
                        Intent forecast = new Intent(getApplicationContext(), ForecastActivity.class);
                        startActivity(forecast);
                        break;
                    case R.id.action_map:
                        Intent map = new Intent(getApplicationContext(), MapActivity.class);
                        startActivity(map);
                        break;

                }

                DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }


    public void onExceptionThrown(Exception e){
        if(e instanceof FileNotFoundException) {
            displaySnackBarMessage(getString(R.string.server_error));;
        }else if(e instanceof UnknownHostException){
            displaySnackBarMessage(getString(R.string.connection_error));
        }
        this.progressBar.setVisibility(View.INVISIBLE);
        this.progressBar.setProgress(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        setupSearchView(menu);

        return true;
    }

    private void setupSearchView(Menu menu) {
        SearchView search = (SearchView) menu.findItem(R.id.action_search).getActionView();

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                Intent place = new Intent(_this, PlaceActivity.class);
                Bundle extra = new Bundle();
                extra.putString("query", s);
                place.putExtras(extra);
                startActivity(place);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void displaySnackBarMessage(String message) {
        Snackbar snackbar = Snackbar.make(fragmentLayout, message, Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStop(){
        super.onStop();
        ((ParkenDD) getApplication()).getTracker().dispatch();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
