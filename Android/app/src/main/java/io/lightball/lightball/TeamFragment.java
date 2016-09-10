package io.lightball.lightball;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.lightball.lightball.entities.Player;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class TeamFragment extends Fragment {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    private static final String SAVED_PLAYERS = "saved-players";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private OnListFragmentInteractionListener mListener;
    private ArrayList<Player> mPlayers = new ArrayList<Player>();
    private RecyclerView mRecyclerView;
    private PlayerRecyclerViewAdapter mPlayerRecyclerViewAdapter;



    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public TeamFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static TeamFragment newInstance(int columnCount) {
        TeamFragment fragment = new TeamFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Make a grid of Player if we are in the GameInProgressActivity
        //Present them as a list otherwise
        mColumnCount = getActivity() instanceof GameInProgressActivity ? 2 : 1 ;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_list, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.list);
        // Set the adapter
        Context context = mRecyclerView.getContext();
        RecyclerView recyclerView = mRecyclerView;
        if (mColumnCount <= 1) {
            recyclerView.setLayoutManager(new LinearLayoutManager(context));
        } else {
            recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
        }
        mPlayerRecyclerViewAdapter = new PlayerRecyclerViewAdapter(mPlayers, mListener);
        recyclerView.setAdapter(mPlayerRecyclerViewAdapter);
        return mRecyclerView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVED_PLAYERS, mPlayers);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null){
            mPlayers = savedInstanceState.getParcelableArrayList(SAVED_PLAYERS);
        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public int addPlayerToTeam(Player player){
        Log.d("Debug","Added player to team");
        mPlayers.add(player);
        mPlayerRecyclerViewAdapter.notifyDataSetChanged();
        return mPlayers.size();
    }
    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onListFragmentInteraction(Player item);
    }
}
