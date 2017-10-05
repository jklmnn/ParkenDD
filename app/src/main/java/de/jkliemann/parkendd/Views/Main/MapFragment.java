package de.jkliemann.parkendd.Views.Main;

import android.app.Activity;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.util.ArrayList;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.Model.ParkingSpot;
import de.jkliemann.parkendd.ParkenDD;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Views.Drawables.SlotPopup;
import de.jkliemann.parkendd.Views.Drawables.SpotIcon;
import de.jkliemann.parkendd.Views.Drawables.SpotIconBitmap;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private City city;
    private Location self;
    private ItemizedIconOverlay<OverlayItem> popupOverlay;

    public MapFragment() {
        // Required empty public constructor
    }


    public static MapFragment newInstance() {
        MapFragment fragment = new MapFragment();

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        city = ((ParkenDD) getActivity().getApplication()).currentCity();

        if(((ParkenDD) getActivity().getApplication()).autoCity()){
            self = ((ParkenDD) getActivity().getApplication()).location();
        }else{
            self = city.location();
        }
        
        getActivity().setTitle(city.name());
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        setUpMapView(view);
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(menu != null)
        {
            menu.clear();
        }
        super.onCreateOptionsMenu(menu,inflater);
    }

    private void setUpMapView(View view) {
        final MapView map = (MapView) view.findViewById(R.id.osmap);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        final IMapController mapctl = map.getController();
        mapctl.setZoom(15);
        ItemizedIconOverlay<OverlayItem> spotOverlay = new ItemizedIconOverlay<>(getActivity(), createItemList(city.spots()),
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
            ItemizedIconOverlay<OverlayItem> selfOverlay = new ItemizedIconOverlay<>(getActivity(), selfList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
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
        if(spotlist != null) {
            for(ParkingSpot spot : spotlist){
                String desc = "";
                SpotIcon marker = new SpotIcon(spot, getActivity());
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
        SlotPopup marker = new SlotPopup(spot.name(), val, getActivity());
        popup.setMarker(marker.getBitmapDrawable());
        ArrayList<OverlayItem> popupList = new ArrayList<>();
        popupList.add(popup);
        popupOverlay = new ItemizedIconOverlay<>(getActivity(), popupList, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
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

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
