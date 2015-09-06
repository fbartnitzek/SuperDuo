package barqsoft.footballscores;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import barqsoft.footballscores.service.myFetchService;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public scoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String[] fragmentdate = new String[1];
    private static final String LOG_TAG = MainScreenFragment.class.getName();
    private int last_selected_item = -1;

    public MainScreenFragment() {
        Log.v(LOG_TAG, "MainScreenFragment, " + "");
    }

    private void updateScores() {
        Log.v(LOG_TAG, "updateScores, " + "");
        Intent service_start = new Intent(getActivity(), myFetchService.class);
        getActivity().startService(service_start);
    }

    public void setFragmentDate(String date) {
        Log.v(LOG_TAG, "setFragmentDate, " + "date = [" + date + "]");
        fragmentdate[0] = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = ["
                + container + "], savedInstanceState = [" + savedInstanceState + "]");
        updateScores();
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView score_list = (ListView) rootView.findViewById(R.id.scores_list);
        mAdapter = new scoresAdapter(getActivity(), null, 0);
        score_list.setAdapter(mAdapter);
        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.detail_match_id = MainActivity.selected_match_id;
        score_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.matchId;
                MainActivity.selected_match_id = (int) selected.matchId;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader, " + "i = [" + i + "], bundle = [" + bundle + "]");
        return new CursorLoader(getActivity(), DatabaseContract.scores_table.buildScoreWithDate(),
                null, null, fragmentdate, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.v(LOG_TAG, "onLoadFinished, " + "cursorLoader = [" + cursorLoader + "], cursor = [" + cursor + "]");
        //Log.v(FetchScoreTask.LOG_TAG,"loader finished");
        //cursor.moveToFirst();
        /*
        while (!cursor.isAfterLast())
        {
            Log.v(FetchScoreTask.LOG_TAG,cursor.getString(1));
            cursor.moveToNext();
        }
        */

        int i = 0;
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "onLoaderReset, " + "cursorLoader = [" + cursorLoader + "]");
        mAdapter.swapCursor(null);
    }


}
