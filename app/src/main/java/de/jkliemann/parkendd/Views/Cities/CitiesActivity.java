package de.jkliemann.parkendd.Views.Cities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import org.json.JSONException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Views.Main.MainActivity;
import de.jkliemann.parkendd.Web.Loader;
import de.jkliemann.parkendd.Web.LoaderInterface;
import de.jkliemann.parkendd.Web.Parser;

public class CitiesActivity extends AppCompatActivity implements CityFragment.OnListFragmentInteractionListener, LoaderInterface{

    private List<City> mCities;
    private City mActiveCity;

    private Loader meta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cities);
        reloadIndex();
    }


    private void reloadIndex(){
        URL[] serverurl = new URL[1];
        try {
            serverurl[0] = Loader.getMetaUrl(getString(R.string.serveraddress));
            meta = new Loader(this);
            meta.execute(serverurl);
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onExceptionThrown(Exception e) {

    }

    @Override
    public void onLoaderFinished(String[] data, Loader loader) {
        try{
            mCities = Parser.meta(data[0]);
            mActiveCity = ((ParkenDD)getApplication()).currentCity();
        }catch (JSONException e) {
            e.printStackTrace();
        }

        getFragmentManager().beginTransaction().replace(R.id.cities_fragment_container, CityFragment.newInstance(1, mCities, mActiveCity)).commit();
    }

    @Override
    public void onProgressUpdated() {

    }

    @Override
    public void onListFragmentInteraction(City city) {
        ((ParkenDD) getApplication()).setCurrentCity(city);

        // We go back home and reload the spot lists
        Intent backToHome = new Intent(this, MainActivity.class);
        backToHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(backToHome);
    }
}
