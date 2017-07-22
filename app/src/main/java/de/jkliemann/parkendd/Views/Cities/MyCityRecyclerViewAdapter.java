package de.jkliemann.parkendd.Views.Cities;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.jkliemann.parkendd.Model.City;
import de.jkliemann.parkendd.R;
import de.jkliemann.parkendd.Utilities.Util;
import de.jkliemann.parkendd.Views.Cities.CityFragment.OnListFragmentInteractionListener;

/**
 * {@link RecyclerView.Adapter} that can display a {@link City} and makes a call to the
 * specified {@link OnListFragmentInteractionListener}.
 */
public class MyCityRecyclerViewAdapter extends RecyclerView.Adapter<MyCityRecyclerViewAdapter.ViewHolder> {

    private final List<City> mValues;
    private final City mActiveCity;
    private final OnListFragmentInteractionListener mListener;
    private final Context mContext;

    public MyCityRecyclerViewAdapter(List<City> items, City activeCity, Context context, OnListFragmentInteractionListener listener) {
        mValues = items;
        mActiveCity = activeCity;
        mContext = context;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_city, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mItem = mValues.get(position);
        holder.mNameView.setText(mValues.get(position).name());

        String distance;
        if(!mValues.get(position).id().equals(mActiveCity.id()))
            distance = "(" + Util.getViewDistance(Util.getDistance(mValues.get(position).location(), mActiveCity.location())) + ")";
        else
        {
            distance = mContext.getString(R.string.chosen_city);
        }

        holder.mDistanceView.setText(distance);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final CardView mBackground;
        public final TextView mNameView;
        public final TextView mDistanceView;
        public City mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mBackground = (CardView) view.findViewById(R.id.card_view);
            mNameView = (TextView) view.findViewById(R.id.name);
            mDistanceView = (TextView) view.findViewById(R.id.distance);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mDistanceView.getText() + "'";
        }
    }
}
