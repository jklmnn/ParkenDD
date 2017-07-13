package de.jkliemann.parkendd.Views.Main;

import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.Model.ParkingSpot;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Views.Drawables.SlotPopup;
import de.jkliemann.parkendd.Views.Drawables.SpotIcon;
import de.jkliemann.parkendd.Views.Drawables.SpotIconBitmap;

public class MapActivity extends ActionBarActivity {

    private ItemizedIconOverlay<OverlayItem> popupOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        City city  = ((ParkenDD)getApplication()).currentCity();
        Location self;
        if(((ParkenDD)getApplication()).autoCity()){
            self = ((ParkenDD)getApplication()).location();
        }else{
            self = city.location();
        }
        setTitle(city.name());
        final MapView map = (MapView)findViewById(R.id.osmap);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        final IMapController mapctl = map.getController();
        mapctl.setZoom(15);
        ItemizedIconOverlay<OverlayItem> spotOverlay = new ItemizedIconOverlay<>(this, createItemList(city.spots()),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    @Override
                    public boolean onItemSingleTapUp(int index, OverlayItem item) {
                        mapctl.setCenter(item.getPoint());
                        SpotIconBitmap icon = (SpotIconBitmap) item.getDrawable();
                        ParkingSpot spot = icon.getSpot();
                        setPopup(map, spot, (GeoPoint)item.getPoint());
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int index, OverlayItem item) {
                        return false;
                    }
                }
        );
        map.getOverlays().add(spotOverlay);
        try {
            GeoPoint selfLoc = new GeoPoint(self.getLatitude(), self.getLongitude());
            mapctl.setCenter(selfLoc);
            OverlayItem selfItem = new OverlayItem("Location", "", selfLoc);
            ArrayList<OverlayItem> selfList = new ArrayList<>();
            selfList.add(selfItem);
            ItemizedIconOverlay<OverlayItem> selfOverlay = new ItemizedIconOverlay<>(this, selfList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                @Override
                public boolean onItemSingleTapUp(int index, OverlayItem item) {
                    mapctl.setCenter(item.getPoint());
                    unsetPopup(map);
                    return false;
                }

                @Override
                public boolean onItemLongPress(int index, OverlayItem item) {
                    return false;
                }
            });
            map.getOverlays().add(selfOverlay);
        }catch (NullPointerException e){
            e.printStackTrace();
            mapctl.setCenter(new GeoPoint(city.location().getLatitude(), city.location().getLongitude()));
        }
        map.invalidate();
    }

    private ArrayList<OverlayItem> createItemList(ArrayList<ParkingSpot> spotlist){
        ArrayList<OverlayItem> itemList = new ArrayList<>();
        for(ParkingSpot spot : spotlist){
            String desc = "";
            SpotIcon marker = new SpotIcon(spot, this);
            switch (spot.state()){
                case "closed":
                    desc = getString(R.string.closed);
                    break;
                case "nodata":
                    desc = getString(R.string.nodata);
                    break;
                default:
                    desc = String.valueOf(spot.free()) + " " + getString(R.string.of)  + " " + String.valueOf(spot.count());
                    break;
            }
            try {
                OverlayItem olItem = new OverlayItem(spot.name(), desc, new GeoPoint(spot.location().getLatitude(), spot.location().getLongitude()));
                olItem.setMarker(marker.getBitmapDrawable());
                olItem.setMarkerHotspot(OverlayItem.HotspotPlace.CENTER);
                itemList.add(olItem);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
        return itemList;
    }

    private void setPopup(MapView mapv, ParkingSpot spot, GeoPoint point){
        final MapView map = mapv;
        if(popupOverlay != null) {
            map.getOverlays().remove(popupOverlay);
        }
        OverlayItem popup = new OverlayItem(spot.name(), spot.state(), point);
        String val;
        switch (spot.state()){
            case "closed":
                val = getString(R.string.closed);
                break;
            case "nodata":
                val = getString(R.string.nodata);
                break;
            default:
                val = String.valueOf(spot.free()) + " " + getString(R.string.of) + " " + String.valueOf(spot.count());
                break;
        }
        SlotPopup marker = new SlotPopup(spot.name(), val, this);
        popup.setMarker(marker.getBitmapDrawable());
        ArrayList<OverlayItem> popupList = new ArrayList<>();
        popupList.add(popup);
        popupOverlay = new ItemizedIconOverlay<>(this, popupList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                map.getOverlays().remove(popupOverlay);
                return false;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                return false;
            }
        });
        map.getOverlays().add(popupOverlay);
    }

    private void unsetPopup(MapView map){
        if(popupOverlay != null) {
            map.getOverlays().remove(popupOverlay);
            popupOverlay = null;
        }
    }

}
