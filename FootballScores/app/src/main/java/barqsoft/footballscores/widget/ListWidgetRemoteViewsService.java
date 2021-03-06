package barqsoft.footballscores.widget;

import android.annotation.TargetApi;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.Date;

import barqsoft.footballscores.Constants;
import barqsoft.footballscores.MainActivity;
import barqsoft.footballscores.R;
import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseContract;
import barqsoft.footballscores.data.DatabaseContract.*;

/**
 * Created by frank on 08.10.15.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ListWidgetRemoteViewsService extends RemoteViewsService {

    private static final String LOG_TAG = ListWidgetRemoteViewsService.class.getName();
    private static final String[] SCORE_COLUMNS = {
            ScoreEntry.TABLE_NAME+ "." + ScoreEntry._ID,
            ScoreEntry.LEAGUE_COL,
//            DatabaseContract.ScoreEntry.HOME_COL,
//            DatabaseContract.ScoreEntry.AWAY_COL,
            ScoreEntry.HOME_GOALS_COL,
            ScoreEntry.AWAY_GOALS_COL,
            ScoreEntry.TIME_COL,

            // inner join home team
            TeamEntry.ALIAS_HOME + "." + TeamEntry.TEAM_NAME_COL,
            TeamEntry.ALIAS_HOME + "." + TeamEntry.TEAM_ICON_COL,

            // inner join away team
            TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_NAME_COL,
            TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_ICON_COL,

            ScoreEntry.MATCH_ID_COL,
            ScoreEntry.DATE_COL

    };
    // these indices must match the projection
    private static final int INDEX_SCORE_ID = 0;
    static final int INDEX_SCORE_LEAGUE = 1;
    private static final int INDEX_SCORE_HOME_GOALS= 2;
    private static final int INDEX_SCORE_AWAY_GOALS= 3;
    private static final int INDEX_SCORE_TIME= 4;
    private static final int INDEX_TEAM_HOME_NAME= 5;
    static final int INDEX_TEAM_HOME_ICON= 6;
    private static final int INDEX_TEAM_AWAY_NAME= 7;
    static final int INDEX_TEAM_AWAY_ICON= 8;
    private static final int INDEX_SCORE_MATCH_ID= 9;
    private static final int INDEX_SCORE_DATE= 10;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private final Context mContext = getApplicationContext();
            private Cursor data = null;
            private String date = null;

            @Override
            public void onCreate() {
//                Log.v(LOG_TAG, "onCreate, " + "");
            }

            @Override
            public void onDataSetChanged() {

                if (null != data) {
                    data.close();
                }
//                Log.v(LOG_TAG, "onDataSetChanged, " + "data closed");

                final long identityToken = Binder.clearCallingIdentity();

                data = getNextMatchDay();

                updateWidgetDate(date, data.getCount()>0);

                Binder.restoreCallingIdentity(identityToken);
            }

            private void updateWidgetDate(String date, boolean filled) {
//                Log.v(LOG_TAG, "updateWidgetDate, " + "date = [" + date + "], filled: " + filled);

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                        new ComponentName(mContext, ListWidgetProvider.class));
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(mContext.getPackageName(),
                            R.layout.widget_list);
                    views.setTextViewText(R.id.widget_next_match_day,
                            Utilities.getReadableDayName(mContext, 0, date));

                    if (!filled){
                        String readableDate = Utilities.getReadableDayName(mContext, 0, date);
                        views.setTextViewText(R.id.widget_empty,
                                mContext.getString(R.string.widget_no_matches_until, readableDate));
                    }

                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

            }

            private Cursor getNextMatchDay() {

                for (int i=0 ; i<=Constants.FUTURE_DAYS; ++i){
                    Uri uri = DatabaseContract.ScoreEntry.buildScoreAndTeamsUri(
                            Utilities.formatDate(
                                    new Date(System.currentTimeMillis() + i * Constants.DAY_IN_MILLIS)));
                    data = getContentResolver().query(uri,
                            SCORE_COLUMNS,
                            null,
                            null,
                            null);

//                    Log.v(LOG_TAG, "getNextMatchDay, " + "data: " + data.getCount());
                    if (data != null && data.getCount()>0) {
                        date = Utilities.formatDate(new Date(System.currentTimeMillis() +
                                i * Constants.DAY_IN_MILLIS));
                        return data;
                    }

                }
                date = null;
                return data;
            }


            @Override
            public void onDestroy() {
                Log.v(LOG_TAG, "onDestroy, " + "");
                if (data != null) {
                    data.close();
                    data = null;
                }
            }



            @Override
            public int getCount() {
//                Log.v(LOG_TAG, "getCount, " + "");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
//                Log.v(LOG_TAG, "getViewAt, " + "position = [" + position + "]");
                if (position == AdapterView.INVALID_POSITION || null == data
                        || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);

                String home = data.getString(INDEX_TEAM_HOME_NAME);
                String away = data.getString(INDEX_TEAM_AWAY_NAME);
                String time = data.getString(INDEX_SCORE_TIME);
                String score = Utilities.getScores(data.getInt(INDEX_SCORE_HOME_GOALS),
                        data.getInt(INDEX_SCORE_AWAY_GOALS), mContext);
                long matchId = data.getLong(INDEX_SCORE_MATCH_ID);
                String date = data.getString(INDEX_SCORE_DATE);

                String desc = mContext.getString(R.string.complete_description, date, time, home, away, score);

//                Log.v(LOG_TAG, "getViewAt, " + "desc = [" + desc + "]");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                    setRemoteContentDescription(views, desc);
//                }

                views.setTextViewText(R.id.widget_team_home, home);
                views.setTextViewText(R.id.widget_team_away, away);
                views.setTextViewText(R.id.widget_time, time);
                views.setTextViewText(R.id.widget_score, score);

                // TODO: get pics and use them?
//                String urlHomeIcon = data.getString(INDEX_TEAM_HOME_ICON);
//                String urlAwayIcon = data.getString(INDEX_TEAM_AWAY_ICON);

                Intent intent = new Intent(mContext, MainActivity.class);
                Uri matchUri = DatabaseContract.ScoreEntry.buildScoreAndTeamsUri(date, matchId);
                intent.setData(matchUri);
                views.setOnClickFillInIntent(R.id.widget_list_item, intent);

                return views;
            }

//            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
//            private void setRemoteContentDescription(RemoteViews views, String description) {
//                views.setContentDescription(R.id.widget_icon, description);
//            }

            @Override
            public RemoteViews getLoadingView() {
                Log.v(LOG_TAG, "getLoadingView, " + "");
                return new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);
            }

            @Override
            public int getViewTypeCount() {
                Log.v(LOG_TAG, "getViewTypeCount, " + "");
                return 1;
            }

            @Override
            public long getItemId(int position) {
                Log.v(LOG_TAG, "getItemId, " + "position = [" + position + "]");
                if (data.moveToPosition(position))
                    return data.getLong(INDEX_SCORE_ID);
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
