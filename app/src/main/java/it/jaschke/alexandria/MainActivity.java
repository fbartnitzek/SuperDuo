package it.jaschke.alexandria;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import it.jaschke.alexandria.api.Callback;


public class MainActivity extends ActionBarActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {


    private static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
//            Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        }else {
            setContentView(R.layout.activity_main);
//            Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        }

        // IntentFilter and Receiver useless?

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        title = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

//        Log.v(LOG_TAG, "onNavigationDrawerItemSelected, position " + position);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment = positionToFragment(position);

//        hideKeyboard();   //seems to do something wrong...

        if (nextFragment != null){
            fragmentManager.beginTransaction()
                    .replace(R.id.container, nextFragment)
                    .addToBackStack(nextFragment.getClass().getSimpleName())
                    .commit();
        }
    }

//    void hideKeyboard(){
//        //http://stackoverflow.com/questions/1109022/close-hide-the-android-soft-keyboard
//        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        if (getCurrentFocus()!=null){
//            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
//        }
//    }

    private Fragment positionToFragment (int position){
        switch (position) {
            case 0:
                return new ListOfBooks();
            case 1:
                return new AddBook();
            case 2:
                return new About();
            default:
                return null;
        }
    }

    private int fragmentToPosition (String name){
        if (ListOfBooks.class.getSimpleName().equals(name)){
            return 0;
        } else if (AddBook.class.getSimpleName().equals(name)){
            return 1;
        } else if (About.class.getSimpleName().equals(name)){
            return 2;
        } else {
            return -1;
        }
    }

    public void setTitle(int titleId) {
        title = getString(titleId);


//        Log.v(LOG_TAG, "setTitle, " + "titleId = [" + titleId + "]");
    }

    private void restoreActionBar() {

//        Log.v(LOG_TAG, "restoreActionBar, " + "");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(title);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        Log.v(LOG_TAG, "onCreateOptionsMenu, " + "menu = [" + menu + "]");
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        Log.v(LOG_TAG, "onOptionsItemSelected, " + "item = [" + item + "]");
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
//        Log.v(LOG_TAG, "onDestroy, " + "");
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
//        Log.v(LOG_TAG, "onItemSelected, " + "ean = [" + ean + "]");
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    public void goBack(View view){
//        Log.v(LOG_TAG, "goBack, " + "view = [" + view + "]");
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
//        Log.v(LOG_TAG, "isTablet, " + "");
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
//        logBackStack(fm);
        if(fm.getBackStackEntryCount()<2){
            finish();
        } else if (mNavigationDrawerFragment != null){
            int i = fragmentToPosition(
                        fm.getBackStackEntryAt(fm.getBackStackEntryCount() -2).getName());
            if (i>=0){
                mNavigationDrawerFragment.setSelectedItem(i);
            }
        }

        super.onBackPressed();
    }

//    private void logBackStack(FragmentManager fm) {
//        for (int i=0; i<fm.getBackStackEntryCount();++i){
////            Log.v(LOG_TAG, "logBackStack, position " + i + ": "
////                    + fm.getBackStackEntryAt(i).getName());
//        }
//        if (fm.getBackStackEntryCount()==0){
////            Log.v(LOG_TAG, "logBackStack, no backstack");
//        }
//    }


}