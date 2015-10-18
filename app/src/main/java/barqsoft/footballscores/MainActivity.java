package barqsoft.footballscores;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.service.FetchService;

public class MainActivity extends ActionBarActivity {
    private static final String PAGER_CURRENT = "Pager_Current";
    private static final String SELECTED_MATCH = "Selected_match";
    private static final String MAIN_FRAGMENT = "mainFragment";
    public static long selectedMatchId;
    public static int currentFragment = Constants.PAST_DAYS;    // today
    private static final String LOG_TAG = MainActivity.class.getName();
    private PagerFragment mainFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri contentUri = getIntent() != null ? getIntent().getData() : null;

        if (savedInstanceState == null) {
            mainFragment = new PagerFragment();
            if (contentUri != null) {
                String date = DatabaseContract.ScoreEntry.getDateFromWidgetUri(contentUri);
                int relativeDay = Utilities.getRelativeDay(date);
                Log.v(LOG_TAG, "onCreate, " + "relative day = [" + relativeDay + "]");
                if ( (-1) * Constants.PAST_DAYS <= relativeDay &&
                        relativeDay <= Constants.FUTURE_DAYS) {
                    currentFragment = relativeDay + Constants.PAST_DAYS;    // past days is offset
                } else {
                    Log.e(LOG_TAG, "onCreate " + " wrong relative day!!!");
                }

                selectedMatchId = DatabaseContract.ScoreEntry.getMatchIdFromWidgetUri(contentUri);

                Bundle args = new Bundle();
                args.putParcelable(Constants.DETAIL_URI, contentUri);
                mainFragment.setArguments(args);
            }

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, mainFragment)
                    .commit();
        }

        updateScores();
    }

    private void updateScores() {
        Log.v(LOG_TAG, "updateScores, " + "");
        Intent serviceStart = new Intent(this, FetchService.class);
        this.startService(serviceStart);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.v(LOG_TAG, "onCreateOptionsMenu, " + "menu = [" + menu + "]");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.v(LOG_TAG, "onOptionsItemSelected, " + "item = [" + item + "]");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_about) {
            Intent start_about = new Intent(this, AboutActivity.class);
            startActivity(start_about);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.v(LOG_TAG, "onSaveInstanceState, " + "outState = [" + outState + "]");
//        Log.v(LOG_TAG, "fragment: " + String.valueOf(mainFragment.mPagerHandler.getCurrentItem()));
//        Log.v(LOG_TAG, "selected id: " + selectedMatchId);
        outState.putInt(PAGER_CURRENT, mainFragment.mPagerHandler.getCurrentItem());
        outState.putLong(SELECTED_MATCH, selectedMatchId);
        getSupportFragmentManager().putFragment(outState, MAIN_FRAGMENT, mainFragment);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onRestoreInstanceState, " + "savedInstanceState = [" + savedInstanceState + "]");
//        Log.v(LOG_TAG, "fragment: " + String.valueOf(savedInstanceState.getInt("Pager_Current")));
//        Log.v(LOG_TAG, "selected id: " + savedInstanceState.getInt("Selected_match"));
        currentFragment = savedInstanceState.getInt(PAGER_CURRENT);
        selectedMatchId = savedInstanceState.getLong(SELECTED_MATCH);
        mainFragment = (PagerFragment) getSupportFragmentManager().getFragment(
                savedInstanceState, MAIN_FRAGMENT);
        super.onRestoreInstanceState(savedInstanceState);
    }
}
