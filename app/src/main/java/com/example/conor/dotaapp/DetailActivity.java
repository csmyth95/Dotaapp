package com.example.conor.dotaapp;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.conor.dotaapp.data.MatchContract;


public class DetailActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        private static final String MATCH_SHARE_HASHTAG = " #DotaApp";
        private static final String LOG_TAG = DetailFragment.class.getSimpleName();

        private ShareActionProvider mShareActionProvider;
        private String mMatch;

        private static final int DETAIL_LOADER = 0;

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

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            //Inflate menu; adds items to the action bar if it is present
            inflater.inflate(R.menu.detailfragment, menu);

            //Retrieve share menu item
            MenuItem menuItem = menu.findItem(R.id.action_share);

            //Get provider and hold onto it to set/change share intent
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);

            //Attach intent to this ShareProvider
            if(mMatch != null){
                mShareActionProvider.setShareIntent(createShareMatchIntent());
            }
        }

        private Intent createShareMatchIntent(){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            return shareIntent;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            Log.v(LOG_TAG, "In onCreateLoader");
            Intent intent = getActivity().getIntent();
            if(intent == null){
                return null;
            }

            //create and return loader
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MATCH_COLUMNS,
                    null,
                    null,
                    null
            );
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            Log.v(LOG_TAG, "In onLoadFinished");

            if(!data.moveToFirst()){
                return;
            }
            //FORMAT DATA
            String dateString = Utility.formatDate(data.getLong(COL_START_TIME));
            String matchId = data.getString(COL_MATCH_KEY);
            String playerId = Utility.getSteamAccountId(getActivity());
            String heroId = data.getString(COL_HERO_ID);
            String playerSlot = data.getString(COL_P_SLOT);

            mMatch = String.format("Date: %s, MatchID: %s, HeroID: %s, SteamID: %s", dateString, matchId, heroId, playerId);

            TextView detailTextView = (TextView)getView().findViewById(R.id.detail_text);
            detailTextView.setText(mMatch);

            //If onCreateOptionsMenu has already happened, we need to update share intent
            if (mShareActionProvider != null){
                mShareActionProvider.setShareIntent(createShareMatchIntent());
            }

        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {

        }
    }
}
