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
            TeamEntry.ALIAS_AWAY + "." + TeamEntry.TEAM_ICON_COL

    };
    // these indices must match the projection
    static final int INDEX_SCORE_ID = 0;
    static final int INDEX_SCORE_LEAGUE = 1;
//    static final int INDEX_SCORE_HOME = 2;
//    static final int INDEX_SCORE_AWAY= 3;
    static final int INDEX_SCORE_HOME_GOALS= 3;
    static final int INDEX_SCORE_AWAY_GOALS= 4;
    static final int INDEX_SCORE_TIME= 5;
    static final int INDEX_TEAM_HOME_NAME= 6;
    static final int INDEX_TEAM_HOME_ICON= 7;
    static final int INDEX_TEAM_AWAY_NAME= 8;
    static final int INDEX_TEAM_AWAY_ICON= 9;

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Context mContext = getApplicationContext();
            private Cursor data = null;
//            private String[] date = null;
            private String date = null;

            @Override
            public void onCreate() {
                Log.v(LOG_TAG, "onCreate, " + "");
            }

            @Override
            public void onDataSetChanged() {

                if (null != data) {
                    data.close();
                }
                Log.v(LOG_TAG, "onDataSetChanged, " + "data closed");

                final long identityToken = Binder.clearCallingIdentity();

                data = getNextMatchDay();
                if (data.getCount()>0){
                    Log.v(LOG_TAG, "onDataSetChanged, " + "matchDate: " + date + ", matches: " + data.getCount());
                    updateWidgetDate(date);
                } else {
                    Log.v(LOG_TAG, "onDataSetChanged, " + "no matches... ");
                    updateWidgetDate(date);
                }


                Binder.restoreCallingIdentity(identityToken);
            }

            private void updateWidgetDate(String date) {
                Log.v(LOG_TAG, "updateWidgetDate, " + "date = [" + date + "]");


//                views.setTextViewText(R.id.widget_next_match_day, date[0]);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                int appWidgetIds[] = appWidgetManager.getAppWidgetIds(
                        new ComponentName(mContext, ListWidgetProvider.class));
                for (int appWidgetId : appWidgetIds) {
                    RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_list);
                    views.setTextViewText(R.id.widget_next_match_day,
                            Utilities.getReadableDayName(mContext, 0, date));
//                            date[0]);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                }

            }

            private Cursor getNextMatchDay() {
//                Uri uri = DatabaseContract.ScoreEntry.buildScoreWithDate();

                for (int i=0 ; i<=Constants.FUTURE_DAYS; ++i){
//                    Uri uri = DatabaseContract.ScoreEntry.buildScoreWithDate(
                    Uri uri = DatabaseContract.ScoreEntry.buildScoreAndTeamsUri(
                            Utilities.formatDate(
                                    new Date(System.currentTimeMillis() + i * Constants.DAY_IN_MILLIS)));
                    data = getContentResolver().query(uri,
                            SCORE_COLUMNS,
                            null,
                            // next day
                            null,
//                            Utilities.formatDate(new Date(System.currentTimeMillis() +
//                                    i * Constants.DAY_IN_MILLIS)),
                            null);

                    Log.v(LOG_TAG, "getNextMatchDay, " + "data: " + data.getCount());
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
                Log.v(LOG_TAG, "getCount, " + "");
                return data == null ? 0 : data.getCount();
            }

            @Override
            public RemoteViews getViewAt(int position) {
                Log.v(LOG_TAG, "getViewAt, " + "position = [" + position + "]");
                if (position == AdapterView.INVALID_POSITION || null == data
                        || !data.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.widget_detail_list_item);

//                views.setTextViewText(R.id.widget_date, formattedDate);
//                views.setTextViewText(R.id.widget_description, desc);
//                views.setTextViewText(R.id.widget_high_temperature, formattedMaxTemp);
//                views.setTextViewText(R.id.widget_low_temperature, formattedMinTemp);
                String home = data.getString(INDEX_TEAM_HOME_NAME);
                String away = data.getString(INDEX_TEAM_AWAY_NAME);
                String time = data.getString(INDEX_SCORE_TIME);
                String score = Utilities.getScores(data.getInt(INDEX_SCORE_HOME_GOALS),
                        data.getInt(INDEX_SCORE_AWAY_GOALS));

                String desc = time + ": " + home + " vs " + away + ", score: " + score;
                Log.v(LOG_TAG, "getViewAt, " + "desc = [" + desc + "]");
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
//                    setRemoteContentDescription(views, desc);
//                }

                views.setTextViewText(R.id.widget_team_home, home);
                views.setTextViewText(R.id.widget_team_away, away);
                views.setTextViewText(R.id.widget_time, time);
                views.setTextViewText(R.id.widget_score, score);

                String urlHomeIcon = data.getString(INDEX_TEAM_HOME_ICON);
                String urlAwayIcon = data.getString(INDEX_TEAM_AWAY_ICON);
                // TODO: get pics and use them
//                mHolder.homeCrest.setImageResource(Utilities.getTeamCrestByTeamName(
//                        cursor.getString(DatabaseHelper.COL_MATCH_HOME)));
//                mHolder.awayCrest.setImageResource(Utilities.getTeamCrestByTeamName(
//                        cursor.getString(DatabaseHelper.COL_AWAY)


//                final Intent fillIntent = new Intent();
//                Uri scoreUri = DatabaseContract.ScoreEntry.buildScoreWithDate();

                // TODO: update provider and call matching date
                Intent intent = new Intent(mContext, MainActivity.class);
//                PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
//                views.setOnClickPendingIntent(R.id.widget, pi);
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
