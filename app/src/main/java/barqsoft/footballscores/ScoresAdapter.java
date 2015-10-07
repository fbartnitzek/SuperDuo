package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import barqsoft.footballscores.data.ScoresDBHelper;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public double detail_match_id = 0;
    private String FOOTBALL_SCORES_HASHTAG = "#Football_Scores";
    private static final String LOG_TAG = ScoresAdapter.class.getName();

    public ScoresAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
        Log.v(LOG_TAG, "ScoresAdapter, " + "context = [" + context + "], cursor = [" + cursor + "], flags = [" + flags + "]");
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.v(LOG_TAG, "newView, " + "context = [" + context + "], cursor = [" + cursor + "], parent = [" + parent + "]");
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        //Log.v(FetchScoreTask.LOG_TAG,"new View inflated");
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        Log.v(LOG_TAG, "bindView, " + "view = [" + view + "], context = [" + context + "], cursor = [" + cursor + "]");
        final ViewHolder mHolder = (ViewHolder) view.getTag();
        mHolder.homeName.setText(cursor.getString(ScoresDBHelper.COL_HOME));
        mHolder.awayName.setText(cursor.getString(ScoresDBHelper.COL_AWAY));
        mHolder.date.setText(cursor.getString(ScoresDBHelper.COL_MATCHTIME));
        mHolder.score.setText(Utilities.getScores(cursor.getInt(ScoresDBHelper.COL_HOME_GOALS),
                cursor.getInt(ScoresDBHelper.COL_AWAY_GOALS)));
        mHolder.matchId = cursor.getDouble(ScoresDBHelper.COL_ID);
        mHolder.homeCrest.setImageResource(Utilities.getTeamCrestByTeamName(
                cursor.getString(ScoresDBHelper.COL_HOME)));
        mHolder.awayCrest.setImageResource(Utilities.getTeamCrestByTeamName(
                cursor.getString(ScoresDBHelper.COL_AWAY)
        ));
        //Log.v(FetchScoreTask.LOG_TAG,mHolder.homeName.getText() + " Vs. " + mHolder.awayName.getText() +" id " + String.valueOf(mHolder.matchId));
        //Log.v(FetchScoreTask.LOG_TAG,String.valueOf(detail_match_id));
        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if (mHolder.matchId == detail_match_id) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView match_day = (TextView) v.findViewById(R.id.matchday_textview);
            match_day.setText(Utilities.getMatchDay(cursor.getInt(ScoresDBHelper.COL_MATCHDAY),
                    cursor.getInt(ScoresDBHelper.COL_LEAGUE)));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(cursor.getInt(ScoresDBHelper.COL_LEAGUE)));
            Button share_button = (Button) v.findViewById(R.id.share_button);
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(mHolder.homeName.getText() + " "
                            + mHolder.score.getText() + " " + mHolder.awayName.getText() + " "));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    public Intent createShareForecastIntent(String ShareText) {
        Log.v(LOG_TAG, "createShareForecastIntent, " + "ShareText = [" + ShareText + "]");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText + FOOTBALL_SCORES_HASHTAG);
        return shareIntent;
    }

}
