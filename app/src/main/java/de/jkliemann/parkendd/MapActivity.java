package de.jkliemann.parkendd;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.util.ArrayList;

public class MapActivity extends ActionBarActivity {

    private ArrayList<ParkingSpot> spotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        City city  = GlobalSettings.getGlobalSettings().getCityByName(PreferenceManager.getDefaultSharedPreferences(this).getString("city", getString(R.string.default_city)));
        setTitle(city.name());
        spotList = city.spots();
        MapView map = (MapView)findViewById(R.id.osmap);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        IMapController mapctl = map.getController();
        mapctl.setZoom(13);
        GeoPoint start = new GeoPoint(city.location().getLatitude(), city.location().getLongitude());
        mapctl.setCenter(start);
    }

}
