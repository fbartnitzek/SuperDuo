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

import barqsoft.footballscores.data.DatabaseContract.ScoreEntry;
import barqsoft.footballscores.data.DatabaseContract.TeamEntry;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public double detail_match_id = 0;
//    private String FOOTBALL_SCORES_HASHTAG = ;
    private static final String LOG_TAG = ScoresAdapter.class.getName();

    public static final String[] SCORE_COLUMNS = {
            ScoreEntry.TABLE_NAME+ "." + ScoreEntry._ID,    //1
            ScoreEntry.LEAGUE_COL,                          //2
            ScoreEntry.TIME_COL,                            //3
            ScoreEntry.HOME_GOALS_COL,                      //4
            ScoreEntry.AWAY_GOALS_COL,                      //5
            ScoreEntry.MATCH_ID_COL,                        //6
            ScoreEntry.MATCH_DAY_COL,                       //7

            // inner join home team
            TeamEntry.ALIAS_HOME + "." + TeamEntry.TEAM_NAME_COL,
            TeamEntry.ALIAS_HOME + "." + TeamEntry.TEAM_ICON_COL,

            // inner join away team
            TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_NAME_COL,
            TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_ICON_COL

    };

    // these indices must match the projection
    static final int INDEX_SCORE_ID = 0;    // TODO: useless?
    static final int INDEX_SCORE_LEAGUE = 1;
    static final int INDEX_SCORE_TIME = 2;
    static final int INDEX_SCORE_HOME_GOALS= 3;
    static final int INDEX_SCORE_AWAY_GOALS= 4;
    static final int INDEX_SCORE_MATCH_ID = 5;
    static final int INDEX_SCORE_MATCH_DAY = 6;

    static final int INDEX_TEAM_HOME_NAME= 7;
    static final int INDEX_TEAM_HOME_ICON= 8;

    static final int INDEX_TEAM_AWAY_NAME= 9;
    static final int INDEX_TEAM_AWAY_ICON= 10;

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

//        final String hName = cursor.getString(DatabaseHelper.COL_MATCH_HOME);
        final String hName = cursor.getString(INDEX_TEAM_HOME_NAME);
        mHolder.homeName.setText(hName);
        mHolder.homeName.setContentDescription(context.getString(R.string.a11_homename, hName));
//
        final String aName = cursor.getString(INDEX_TEAM_AWAY_NAME);
        mHolder.awayName.setText(aName);
        mHolder.awayName.setContentDescription(context.getString(R.string.a11_awayname, aName));

        String time = cursor.getString(INDEX_SCORE_TIME);
        mHolder.date.setText(time);
        mHolder.date.setContentDescription(context.getString(R.string.a11_time, time));

        final String score = Utilities.getScores(cursor.getInt(INDEX_SCORE_HOME_GOALS),
                cursor.getInt(INDEX_SCORE_AWAY_GOALS));
        mHolder.score.setText(score);
        mHolder.score.setContentDescription(context.getString(R.string.a11_score, score));
        mHolder.matchId = cursor.getDouble(INDEX_SCORE_MATCH_ID);

        // TODO: not filled / out of bounce?
//        String urlHomeIcon = cursor.getString(INDEX_TEAM_HOME_ICON);
//        String urlAwayIcon = cursor.getString(INDEX_TEAM_AWAY_ICON);
//        mHolder.homeCrest.setImageResource(Utilities.getTeamCrestByTeamName(hName));
//        mHolder.awayCrest.setImageResource(Utilities.getTeamCrestByTeamName(aName));
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
            TextView matchDay = (TextView) v.findViewById(R.id.matchday_textview);
            int leagueNo = cursor.getInt(INDEX_SCORE_LEAGUE);
            matchDay.setText(Utilities.getMatchDay(cursor.getInt(INDEX_SCORE_MATCH_DAY),
                    leagueNo, context));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(leagueNo ,context));
            Button share_button = (Button) v.findViewById(R.id.share_button);

            share_button.setContentDescription(context.getString(R.string.share_text));
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(hName + " "
                            + score + " " + aName + " ", context));
                }
            });
        } else {
            container.removeAllViews();
        }

    }

    public Intent createShareForecastIntent(String ShareText, Context context) {
        Log.v(LOG_TAG, "createShareForecastIntent, " + "ShareText = [" + ShareText + "]");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, ShareText
                + context.getString(R.string.share_football_scores));
        return shareIntent;
    }

}
