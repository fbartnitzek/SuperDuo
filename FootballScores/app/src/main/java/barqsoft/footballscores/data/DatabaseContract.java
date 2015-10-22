package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract {

    private static final String LOG_TAG = DatabaseContract.class.getName();

    public static final String PATH_TEAM = "team";
    public static final String PATH_SCORE = "scores";
    public static final String PATH_SCORE_WITH_TEAMS = "scores_with_teams";

    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Teams
    public static final class TeamEntry implements BaseColumns {

        public static final String TABLE_NAME = "team";
        public static final String ALIAS_HOME = " team_home";
        public static final String ALIAS_AWAY = " team_away";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_TEAM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/"
                        + PATH_TEAM;

        // id string for query, f.e. http://api.football-data.org/alpha/teams/556
        public static final String TEAM_ID_COL = "team_id";
        public static final String TEAM_NAME_COL = "team_name";
        public static final String TEAM_ICON_COL = "team_icon";

        public static Uri buildTeamUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    // Scores
    public static final class ScoreEntry implements BaseColumns {

        public static final String TABLE_NAME = "scores";
        public static final String ALIAS = "joined_scores";

        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_ID_COL = "home_id";
//        public static final String HOME_COL = "home";
        public static final String AWAY_ID_COL = "away_id";
//        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID_COL = "matchId";
        public static final String MATCH_DAY_COL = "match_day";

        // not ideal but easiest solution ...
//        public static final String HOME_ICON = "home_icon";
//        public static final String AWAY_ICON = "away_icon";

        //Types
        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
                + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;

        // base for score queries
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORE).build();

        public static final Uri CONTENT_URI_WITH_TEAMS =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_SCORE_WITH_TEAMS).build();

//        public static Uri buildScoreWithLeague() {
//            Log.v(LOG_TAG, "buildScoreWithLeague, " + "");
//            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
//        }



        public static Uri buildScoreWithDate(String date) {
            Log.v(LOG_TAG, "buildScoreWithDate, " + "date = [" + date + "]");
//            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
            return CONTENT_URI.buildUpon().appendPath(date).build();
        }

        // TODO: untested ...?
        public static Uri buildScoreUri(long id) {
            return CONTENT_URI.buildUpon().appendPath(Long.toString(id)).build();
//            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildScoreAndTeamsUri(long id) {
            return CONTENT_URI_WITH_TEAMS.buildUpon().appendPath(Long.toString(id)).build();
        }

        public static Uri buildScoreAndTeamsUri(String fragmentDate) {
            return CONTENT_URI_WITH_TEAMS.buildUpon().appendPath(fragmentDate).build();
        }

        public static Uri buildScoreAndTeamsUri(String fragmentDate, long id) {
            return CONTENT_URI_WITH_TEAMS.buildUpon().appendPath(fragmentDate)
                    .appendPath(Long.toString(id)).build();
        }

        public static String getDateFromWidgetUri(Uri contentUri) {
            Log.v(LOG_TAG, "getDateFromWidgetUri, " + "contentUri = [" + contentUri
                    + "], date: " + contentUri.getPathSegments().get(1) );
            return contentUri.getPathSegments().get(1);
        }

        public static long getMatchIdFromWidgetUri(Uri uri) {
            return Long.parseLong(uri.getPathSegments().get(2));
        }
    }


}
