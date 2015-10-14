package barqsoft.footballscores;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;

/**
 * Created by yehya khaled on 2/27/2015.
 */
public class PagerFragment extends Fragment {
    public static final int NUM_PAGES = Constants.PAST_DAYS + 1 + Constants.FUTURE_DAYS;
    public ViewPager mPagerHandler;
    private myPageAdapter mPagerAdapter;
    private MainScreenFragment[] viewFragments = new MainScreenFragment[NUM_PAGES];
    private static final String LOG_TAG = PagerFragment.class.getName();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        View rootView = inflater.inflate(R.layout.pager_fragment, container, false);
        mPagerHandler = (ViewPager) rootView.findViewById(R.id.pager);
        mPagerAdapter = new myPageAdapter(getChildFragmentManager());

        //TODO: use default format - but matching with queries...
//            DateFormat defaultFormat = android.text.format.DateFormat.getDateFormat(getActivity());
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        for (int i = 0; i < NUM_PAGES; i++) {

            Date fragmentDate = new Date(System.currentTimeMillis() +
                    ((i - Constants.PAST_DAYS) * Constants.DAY_IN_MILLIS));


            viewFragments[i] = new MainScreenFragment();
//            viewFragments[i].setFragmentDate(dateFormat.format(fragmentDate));
            viewFragments[i].setFragmentDate(Utilities.formatDate(fragmentDate));
        }
        mPagerHandler.setAdapter(mPagerAdapter);
        mPagerHandler.setCurrentItem(MainActivity.currentFragment);
        return rootView;
    }

    private class myPageAdapter extends FragmentStatePagerAdapter {
        @Override
        public Fragment getItem(int i) {
//            Log.v(LOG_TAG, "getItem, " + "i = [" + i + "]");
            return viewFragments[i];
        }

        @Override
        public int getCount() {
//            Log.v(LOG_TAG, "getCount, " + "");
            return NUM_PAGES;
        }

        public myPageAdapter(FragmentManager fm) {
            super(fm);
            Log.v(LOG_TAG, "myPageAdapter, " + "fm = [" + fm + "]");
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            Log.v(LOG_TAG, "getPageTitle, " + "position = [" + position + "]");
            return Utilities.getReadableDayName(getActivity(),
                    System.currentTimeMillis() + ((position - 2) * Constants.DAY_IN_MILLIS), null);
        }

    }
}
