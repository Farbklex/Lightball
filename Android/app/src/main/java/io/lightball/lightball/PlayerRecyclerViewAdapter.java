package io.lightball.lightball;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import io.lightball.lightball.entities.Player;

/**
 * {@link RecyclerView.Adapter} that can display a {@link Player} and makes a call to the
 * specified {@link TeamFragment.OnListFragmentInteractionListener}.
 * TODO: Replace the implementation with code for your data type.
 */
public class PlayerRecyclerViewAdapter extends RecyclerView.Adapter<PlayerRecyclerViewAdapter.ViewHolder> {

    private final List<Player> mValues;
    private final TeamFragment.OnListFragmentInteractionListener mListener;

    public PlayerRecyclerViewAdapter(List<Player> items, TeamFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_player_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mName.setText(mValues.get(position).name);
        if(holder.mHealth != null){
            holder.mHealth.setProgress(mValues.get(position).health);
        }

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
        public final TextView mName;
        public final ProgressBar mHealth;
        public Player mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mName = (TextView) view.findViewById(R.id.player_name);
            if(view.findViewById(R.id.playerHealth) != null){
                mHealth = (ProgressBar) view.findViewById(R.id.playerHealth);
            }else {
                mHealth = null;
            }
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mHealth.getProgress() + "'";
        }
    }

    public void addItem(Player p){
        mValues.add(p);
        this.notifyDataSetChanged();
    }
}
