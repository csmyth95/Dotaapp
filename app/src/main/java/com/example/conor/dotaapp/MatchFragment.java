package com.example.conor.dotaapp;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.conor.dotaapp.data.MatchContract;
import com.example.conor.dotaapp.data.MatchDbHelper;
import com.example.conor.dotaapp.data.MatchProvider;

public class MatchFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int MATCH_LOADER = 0;

    private static final String[] MATCH_COLUMNS = {
            MatchContract.MatchEntry.TABLE_NAME + "." + MatchContract.MatchEntry._ID,
            MatchContract.MatchEntry.COLUMN_MATCH_KEY,
            MatchContract.MatchEntry.COLUMN_START_TIME,
            MatchContract.MatchEntry.COLUMN_P_ACCOUNT_ID,
            MatchContract.PlayerEntry.COLUMN_HERO_ID,
            MatchContract.PlayerEntry.COLUMN_P_SLOT
    };

    static final int COL_MATCH_KEY = 0;
    static final int COL_START_TIME = 1;
    static final int COL_STEAM_ID = 2;
    static final int COL_HERO_ID = 3;
    static final int COL_P_SLOT = 4;

    private MatchAdapter mMatchAdapter;

    public MatchFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //NEW TUTORIAL
        String[] projection = { "2", "match.db" };
        String[] uiBindFrom = {"match.db"};
        int[] uiBindTo = { R.id.title };

        Cursor matches = getActivity().managedQuery(
                MatchContract.MatchEntry.CONTENT_URI, projection, null, null, null);

        CursorAdapter adapter = new SimpleCursorAdapter(getActivity()
                .getApplicationContext(), R.layout.list_item_match, matches,
                uiBindFrom, uiBindTo);

        View view;
        ListView listView = (ListView) view.findViewById(R.id.listview_match);
        listView.setAdapter(adapter);
        setHasOptionsMenu(true);

    }
    //NEW TUTORIAL
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String projection[] = { MatchContract.PATH_PLAYER };
        Cursor matchCursor = getActivity().getContentResolver().query(
                Uri.withAppendedPath(MatchContract.PlayerEntry.CONTENT_URI,
                        String.valueOf(id)), projection, null, null, null);
        if (matchCursor.moveToFirst()) {
            String matchPlayer = matchCursor.getString(0);
            matchSelectedListener.onMatchSelected(matchPlayer);
        }
        matchCursor.close();
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
        Uri matchForPlayerUri = MatchContract.MatchEntry.buildMatchPlayerWithDate(
                steamAccountId, System.currentTimeMillis());

        Cursor cur = getActivity().getContentResolver().query(matchForPlayerUri,
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

        //Call MainActivity
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
           @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l){
               //CursorAdapter returns a cursor at the correct position for getItem(), or null
               // if it cannot seek to that position
               Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
               if(cursor != null){
                   String steamId = Utility.getSteamAccountId(getActivity());
                   Intent intent = new Intent(getActivity(), DetailActivity.class)
                           .setData(MatchContract.MatchEntry.buildMatchPlayerWithDate(
                              steamId, cursor.getLong(COL_START_TIME)
                           ));
                   startActivity(intent);
               }
           }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MATCH_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    // since we read the location when we create the loader, all we need to do is restart things
    void onSteamIdChanged( ) {
        updateMatch();
        getLoaderManager().restartLoader(MATCH_LOADER, null, this);
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String steamId = Utility.getSteamAccountId(getActivity());

        //sort order
        String sortOrder = MatchContract.MatchEntry.COLUMN_START_TIME + " ASC";
        Uri matchForPlayerUri = MatchContract.MatchEntry.buildMatchPlayerWithStartTime(
                steamId, System.currentTimeMillis()
        );

        return new CursorLoader(getActivity(),
                matchForPlayerUri,
                MATCH_COLUMNS,
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
