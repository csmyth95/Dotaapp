package com.example.conor.dotaapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends ActionBarActivity {

    private static String mSteamId, mNumMatches;
    private final String MATCHFRAGMENT_TAG = "MFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mSteamId = Utility.getSteamAccountId(this);
        mNumMatches = Utility.getPreferredNumMatches(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MatchFragment(), MATCHFRAGMENT_TAG)
                    .commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @Override
    protected void onResume() {
        super.onResume();
        String steamId = Utility.getSteamAccountId( this );
        // update the steam id in our second pane using the fragment manager
        if (steamId != null && !steamId.equals(mSteamId)) {
            MatchFragment ff = (MatchFragment)getSupportFragmentManager().findFragmentByTag(MATCHFRAGMENT_TAG);
            if ( null != ff ) {
                ff.onSteamIdChanged();
            }
            mSteamId = steamId;
        }

        String numMatches = Utility.getPreferredNumMatches( this );
        // update the number of matches to display in our second pane using the fragment manager
        if (numMatches != null && !numMatches.equals(mNumMatches)) {
            MatchFragment ff = (MatchFragment)getSupportFragmentManager().findFragmentByTag(MATCHFRAGMENT_TAG);
            if ( null != ff ) {
                ff.onSteamIdChanged();
            }
            mNumMatches = numMatches;
        }
    }

}
