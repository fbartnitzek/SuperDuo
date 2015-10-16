package barqsoft.footballscores;

import android.database.Cursor;
import android.net.Uri;
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
import android.widget.TextView;

import barqsoft.footballscores.data.DatabaseContract;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainScreenFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    public ScoresAdapter mAdapter;
    public static final int SCORES_LOADER = 0;
    private String fragmentDate;
    private static final String LOG_TAG = MainScreenFragment.class.getName();
    private int lastSelectedItem = -1;

    public MainScreenFragment() {
        Log.v(LOG_TAG, "MainScreenFragment, " + "");
    }

    public void setFragmentDate(String date) {
        Log.v(LOG_TAG, "setFragmentDate, " + "date = [" + date + "]");
        fragmentDate = date;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = ["
                + container + "], savedInstanceState = [" + savedInstanceState + "]");

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        final ListView scoreList = (ListView) rootView.findViewById(R.id.scores_list);

        mAdapter = new ScoresAdapter(getActivity(), null, 0);
        scoreList.setAdapter(mAdapter);

        getLoaderManager().initLoader(SCORES_LOADER, null, this);
        mAdapter.detail_match_id = MainActivity.selectedMatchId;

        scoreList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewHolder selected = (ViewHolder) view.getTag();
                mAdapter.detail_match_id = selected.matchId;
                MainActivity.selectedMatchId = (int) selected.matchId;
                mAdapter.notifyDataSetChanged();
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Log.v(LOG_TAG, "onCreateLoader, " + "i = [" + i + "], bundle = [" + bundle + "]");

//        Uri uri = DatabaseContract.ScoreEntry.buildScoreWithDate(fragmentDate);
        Uri uri = DatabaseContract.ScoreEntry.buildScoreAndTeamsUri(fragmentDate);

        Log.v(LOG_TAG, "onCreateLoader, " + "i = [" + i + "], uri = [" + uri.toString()+ "]");
        return new CursorLoader(getActivity(),
                uri,
                ScoresAdapter.SCORE_COLUMNS, null, null, null);
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


        cursor.moveToFirst();
        Log.v(LOG_TAG, "onLoadFinished, " + " cursor-entries = [" + cursor.getCount() + "]");
        if (cursor.getCount() > 0) {
            getView().findViewById(R.id.scores_list).setVisibility(View.VISIBLE);
            getView().findViewById(R.id.scores_list_empty).setVisibility(View.GONE);
        } else {
            getView().findViewById(R.id.scores_list).setVisibility(View.GONE);
            getView().findViewById(R.id.scores_list_empty).setVisibility(View.VISIBLE);
            updateEmptyView();
        }

//        int i = 0;
        while (!cursor.isAfterLast()) {
//            i++;
            cursor.moveToNext();
        }
        //Log.v(FetchScoreTask.LOG_TAG,"Loader query: " + String.valueOf(i));
        mAdapter.swapCursor(cursor);
        //mAdapter.notifyDataSetChanged();
    }

    private void updateEmptyView() {
        TextView tv = (TextView) getView().findViewById(R.id.scores_list_empty);
        if (null != tv) {
            if (!Utilities.isNetworkAvailable(getActivity())) {
                tv.setText(R.string.empty_score_list_no_network);
            } else {
                tv.setText(R.string.no_match_day);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        Log.v(LOG_TAG, "onLoaderReset, " + "cursorLoader = [" + cursorLoader + "]");
        mAdapter.swapCursor(null);
    }


}
