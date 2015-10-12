package barqsoft.footballscores.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by yehya khaled on 2/25/2015.
 */
public class DatabaseContract {
    public static final String SCORES_TABLE = "scores_table";
    public static final String TEAM_TABLE = "teams";
    private static final String LOG_TAG = DatabaseContract.class.getName();
//    public static final String PATH_TEAM = "team";
    public static final String PATH_SCORE = "scores";
//    public static final String PATH = "scores";


//    public static final class TeamEntry implements BaseColumns {
//
//        public static final Uri CONTENT_URI =
//                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TEAM).build();
//
//        public static final String CONTENT_TYPE =
//                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEAM;
//        public static final String CONTENT_ITEM_TYPE =
//                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TEAM;
//
//        public static final String TABLE_NAME = "team";
//
//        // id string for query, f.e. http://api.football-data.org/alpha/teams/556
//        public static final String COLUMN_TEAM_ID = "team_id";
//        public static final String COLUMN_TEAM_NAME = "team_name";
//        public static final String COLUMN_TEAM_ICON = "team_icon";
//
//        public static Uri buildTeamUri(long id) {
//            return ContentUris.withAppendedId(CONTENT_URI, id);
//        }
//    }


    /*  Inner class that defines the contents of the scores table     */
    public static final class ScoreEntry implements BaseColumns {
        //Table data
        public static final String LEAGUE_COL = "league";
        public static final String DATE_COL = "date";
        public static final String TIME_COL = "time";
        public static final String HOME_COL = "home";
        public static final String AWAY_COL = "away";
        public static final String HOME_GOALS_COL = "home_goals";
        public static final String AWAY_GOALS_COL = "away_goals";
        public static final String MATCH_ID_COL = "matchId";
        public static final String MATCH_DAY_COL = "match_day";
        public static final String HOME_ID_COL = "home_id";
        public static final String AWAY_ID_COL = "away_id";
        // not ideal but easiest solution ...
        public static final String HOME_ICON = "home_icon";
        public static final String AWAY_ICON = "away_icon";

        //public static Uri SCORES_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH)
        //.build();

        //Types
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_SCORE;

        public static Uri buildScoreWithLeague() {
            Log.v(LOG_TAG, "buildScoreWithLeague, " + "");
            return BASE_CONTENT_URI.buildUpon().appendPath("league").build();
        }

        public static Uri buildScoreWithId() {
            Log.v(LOG_TAG, "buildScoreWithId, " + "");
            return BASE_CONTENT_URI.buildUpon().appendPath("id").build();
        }

        public static Uri buildScoreWithDate() {
            Log.v(LOG_TAG, "buildScoreWithDate, " + "");
            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
        }

        // TODO: explicitly query with date for opening corresponding tab from widget
//        public static Uri buildScoreWithDate(String date) {
//            Log.v(LOG_TAG, "buildScoreWithDate, " + "");
//            return BASE_CONTENT_URI.buildUpon().appendPath("date").build();
//        }
    }

    //URI data
    public static final String CONTENT_AUTHORITY = "barqsoft.footballscores";

    public static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
}
