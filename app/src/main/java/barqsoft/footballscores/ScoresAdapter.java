package barqsoft.footballscores;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.PictureDrawable;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.GenericRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.load.resource.file.FileToStreamDecoder;
import com.caverock.androidsvg.SVG;

import java.io.InputStream;

import barqsoft.footballscores.data.DatabaseContract.ScoreEntry;
import barqsoft.footballscores.data.DatabaseContract.TeamEntry;
import barqsoft.footballscores.svg.SvgDecoder;
import barqsoft.footballscores.svg.SvgDrawableTranscoder;
import barqsoft.footballscores.svg.SvgSoftwareLayerSetter;

/**
 * Created by yehya khaled on 2/26/2015.
 */
public class ScoresAdapter extends CursorAdapter {

    public double detailMatchId = 0;
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
    private static final int INDEX_SCORE_LEAGUE = 1;
    private static final int INDEX_SCORE_TIME = 2;
    private static final int INDEX_SCORE_HOME_GOALS= 3;
    private static final int INDEX_SCORE_AWAY_GOALS= 4;
    private static final int INDEX_SCORE_MATCH_ID = 5;
    private static final int INDEX_SCORE_MATCH_DAY = 6;

    private static final int INDEX_TEAM_HOME_NAME= 7;
    private static final int INDEX_TEAM_HOME_ICON= 8;

    private static final int INDEX_TEAM_AWAY_NAME= 9;
    private static final int INDEX_TEAM_AWAY_ICON= 10;

    final private ScoresAdapterScrollHandler mScrollHandler;

    private final GenericRequestBuilder<Uri, InputStream, SVG, PictureDrawable> requestBuilder;

    public ScoresAdapter(Context context, Cursor cursor, int flags, Fragment fragment,
                         ScoresAdapterScrollHandler scrollHandler) {
        super(context, cursor, flags);
        mScrollHandler = scrollHandler;
//        Log.v(LOG_TAG, "ScoresAdapter, " + "context = [" + context + "], cursor = [" + cursor + "], flags = [" + flags + "]");

        //create requestBuilder for svg-glide-usage as described here:
        // https://github.com/bumptech/glide/tree/v3.6.0/samples/svg/src/main/java/com/bumptech/svgsample/app
        // uses androidsvg: http://mvnrepository.com/artifact/com.caverock/androidsvg/1.2.1
        requestBuilder = Glide.with(fragment)
                .using(Glide.buildStreamModelLoader(Uri.class, fragment.getActivity()), InputStream.class)
                .from(Uri.class)
                .as(SVG.class)
                .transcode(new SvgDrawableTranscoder(), PictureDrawable.class)
                .sourceEncoder(new StreamEncoder())
                .cacheDecoder(new FileToStreamDecoder<>(new SvgDecoder()))
                .decoder(new SvgDecoder())
                .placeholder(R.drawable.image_loading)
                .error(R.drawable.no_icon)
                .animate(android.R.anim.fade_in)
                .listener(new SvgSoftwareLayerSetter<Uri>());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
//        Log.v(LOG_TAG, "newView, " + "context = [" + context + "], cursor = [" + cursor + "], parent = [" + parent + "]");
        View mItem = LayoutInflater.from(context).inflate(R.layout.scores_list_item, parent, false);
        ViewHolder mHolder = new ViewHolder(mItem);
        mItem.setTag(mHolder);
        return mItem;
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
//        Log.v(LOG_TAG, "bindView, " + "view = [" + view + "], context = [" + context + "], cursor = [" + cursor + "]");
        final ViewHolder mHolder = (ViewHolder) view.getTag();

        final String hName = cursor.getString(INDEX_TEAM_HOME_NAME);
        mHolder.homeName.setText(hName);
        mHolder.homeName.setContentDescription(context.getString(R.string.a11y_homename, hName));

        final String aName = cursor.getString(INDEX_TEAM_AWAY_NAME);
        mHolder.awayName.setText(aName);
        mHolder.awayName.setContentDescription(context.getString(R.string.a11y_awayname, aName));

        String time = cursor.getString(INDEX_SCORE_TIME);
        mHolder.date.setText(time);
        mHolder.date.setContentDescription(context.getString(R.string.a11y_time, time));

        final String score = Utilities.getScores(
                cursor.getInt(INDEX_SCORE_HOME_GOALS),
                cursor.getInt(INDEX_SCORE_AWAY_GOALS),
                mContext);
        mHolder.score.setText(score);
        mHolder.score.setContentDescription(context.getString(R.string.a11y_score, score));
        mHolder.matchId = cursor.getLong(INDEX_SCORE_MATCH_ID);

        String urlHomeIcon = cursor.getString(INDEX_TEAM_HOME_ICON);
        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .load(Uri.parse(urlHomeIcon))
                .into(mHolder.homeCrest);

        String urlAwayIcon = cursor.getString(INDEX_TEAM_AWAY_ICON);
        requestBuilder
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .load(Uri.parse(urlAwayIcon))
                .into(mHolder.awayCrest);

        LayoutInflater vi = (LayoutInflater) context.getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = vi.inflate(R.layout.detail_fragment, null);
        ViewGroup container = (ViewGroup) view.findViewById(R.id.details_fragment_container);
        if (mHolder.matchId == detailMatchId) {
            //Log.v(FetchScoreTask.LOG_TAG,"will insert extraView");

            container.addView(v, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                    , ViewGroup.LayoutParams.MATCH_PARENT));
            TextView matchDay = (TextView) v.findViewById(R.id.matchday_textview);
            int leagueNo = cursor.getInt(INDEX_SCORE_LEAGUE);
            matchDay.setText(Utilities.getMatchDay(cursor.getInt(INDEX_SCORE_MATCH_DAY),
                    leagueNo, context));
            TextView league = (TextView) v.findViewById(R.id.league_textview);
            league.setText(Utilities.getLeague(leagueNo, context));
            Button share_button = (Button) v.findViewById(R.id.share_button);

            share_button.setContentDescription(context.getString(R.string.share_text));
            share_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //add Share Action
                    context.startActivity(createShareForecastIntent(context.getString(
                            R.string.scores_share_text, hName, score, aName)));
                }
            });

            //TODO: Adapter used the wrong way - just 1 entry, not whole list...
            // always returns 0...
            mScrollHandler.onFound(view.getVerticalScrollbarPosition());

        } else {
            container.removeAllViews();
        }

    }

    public interface ScoresAdapterScrollHandler {
        void onFound(int position);
    }

    private Intent createShareForecastIntent(String text) {
//        Log.v(LOG_TAG, "createShareForecastIntent, " + "ShareText = [" + text + "]");
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        return shareIntent;
    }

}
