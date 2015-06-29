package com.example.conor.dotaapp;


import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.conor.dotaapp.data.MatchContract;

public class MatchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MATCH_LOADER = 0;
    private MatchAdapter mMatchAdapter;

    public MatchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.matchfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateMatch();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        String steamAccountId = Utility.getSteamAccountId(getActivity());

        // Sort order:  Ascending, by date.
        String sortOrder = MatchContract.MatchEntry.COLUMN_START_TIME + " ASC";
        Uri weatherForLocationUri = MatchContract.MatchEntry.buildMatchPlayerWithDate(
                steamAccountId, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(weatherForLocationUri,
                null, null, null, sortOrder);

        // The CursorAdapter will take data from our cursor and populate the ListView
        // However, we cannot use FLAG_AUTO_REQUERY since it is deprecated, so we will end
        // up with an empty list the first time we run.
        mMatchAdapter = new MatchAdapter(getActivity(), cur, 0);


        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

//        // The ArrayAdapter will take data from a source and
//        // use it to populate the ListView it's attached to.
//        mMatchAdapter =
//                new ArrayAdapter<String>(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_item_match, // The name of the layout ID.
//                        R.id.list_item_match_textview, // The ID of the textview to populate.
//                        new ArrayList<String>());

        //Click listener & code to display toast
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
//                String match = mMatchAdapter.getItem(position);
//                Intent intent = new Intent(getActivity(), DetailActivity.class)
//                        .putExtra(Intent.EXTRA_TEXT, match);
//                startActivity(intent);
//            }
//        });

        ListView listView = (ListView) rootView.findViewById(R.id.listview_match);
        listView.setAdapter(mMatchAdapter);

        return rootView;
    }

    //Method to update match data when app starts
    private void updateMatch(){
        FetchMatchTask matchTask = new FetchMatchTask(getActivity());

        //Set defaultID of DOTA API in the settings menu
        String defaultID = Utility.getSteamAccountId(getActivity());
        String numMatches = Utility.getPreferredNumMatches(getActivity());
        matchTask.execute(defaultID, numMatches);
    }

    @Override
    public void onStart(){
        super.onStart();
        updateMatch();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String steamId = Utility.getSteamAccountId(getActivity());

        //sort order
        String sortOrder = MatchContract.MatchEntry.COLUMN_START_TIME + " ASC";
        Uri matchForPlayerUri = MatchContract.MatchEntry.buildMatchPlayerWithStartTime(
                steamId, System.currentTimeMillis()
        );

        return new CursorLoader(getActivity(),
                matchForPlayerUri,
                null,
                null,
                null,
                sortOrder
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mMatchAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mMatchAdapter.swapCursor(null);
    }
}
