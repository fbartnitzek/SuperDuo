package barqsoft.footballscores;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.Html;
import android.text.format.Time;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {

    private static final String LOG_TAG = Utilities.class.getName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat readableDayFormat = new SimpleDateFormat("EEEE");

    private static ArrayList supportedLeagues = new ArrayList<>(Arrays.asList(
            Constants.BUNDESLIGA1, Constants.BUNDESLIGA2, Constants.BUNDESLIGA3, Constants.CHAMPIONS_LEAGUE,
            Constants.PREMIER_LEAGUE));

    public static ArrayList<Integer> getSupportedLeagues() {
        return supportedLeagues;
    }

    public static boolean isSupportedLeague(int league) {
        return supportedLeagues.contains(league);
    }

    public static String getLeague(int league_num, Context context) {
        Log.v(LOG_TAG, "getLeague, " + "league_num = [" + league_num + "]");
        switch (league_num) {
            case Constants.SERIE_A:
                return context.getString(R.string.league_seria_a);
            case Constants.PREMIER_LEAGUE:
                return context.getString(R.string.league_premier_league);
            case Constants.CHAMPIONS_LEAGUE:
                return context.getString(R.string.league_champions_league);
            case Constants.PRIMERA_DIVISION:
                return context.getString(R.string.league_primera_division);
            case Constants.BUNDESLIGA1:
                return context.getString(R.string.league_1_bundesliga);
            case Constants.BUNDESLIGA2:
                return context.getString(R.string.league_2_bundesliga);
            case Constants.BUNDESLIGA3:
                return context.getString(R.string.league_3_bundesliga);
            default:
                return context.getString(R.string.unknown_league);
        }
    }

    public static String getMatchDay(int match_day, int league_num, Context context) {
        Log.v(LOG_TAG, "getMatchDay, " + "match_day = [" + match_day + "], league_num = [" + league_num + "]");
        if (league_num == Constants.CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return context.getString(R.string.champions_league_group_stage);
            } else if (match_day == 7 || match_day == 8) {
                return context.getString(R.string.champions_league_first_knockout_round);
            } else if (match_day == 9 || match_day == 10) {
                return context.getString(R.string.champions_league_quarter_final);
            } else if (match_day == 11 || match_day == 12) {
                return context.getString(R.string.champions_league_semi_final);
            } else {
                return context.getString(R.string.champions_league_final);
            }
        } else {
            return context.getString(R.string.matchday) + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals, Context ctx) {
        Log.v(LOG_TAG, "getScores, " + "home_goals = [" + home_goals + "], awaygoals = [" + awaygoals + "]");
        if (home_goals < 0 || awaygoals < 0) {
            return ctx.getString(R.string.no_scores);
        } else {
            return ctx.getString(R.string.scores, home_goals, awaygoals);
        }
    }

    public static String extractIdFromLink(String teamLink) {
        //example: "http://api.football-data.org/alpha/teams/254"

        if (teamLink != null) {
            int pos = teamLink.lastIndexOf("/");
            if (pos > 0) {
                return teamLink.substring(pos + 1);
            }
        }

        return null;
    }


    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static String formatDate(Date date) {
        return dateFormat.format(date);
    }

    public static String decodeHtml(String text) {
        if (text.contains("&")){    // faster
            //        Log.v(LOG_TAG, "decodeHtml, " + "text = [" + text + "]");
            String result = Html.fromHtml(text).toString();
            //        Log.v(LOG_TAG, "decodeHtml, " + "result = [" + result + "]");
            return result;
        } else {
            return text;
        }
    }

    public static String getReadableDayName(Context context, long dateInMillis, String dateString) {
        // If the date is today, return the localized version of "Today" instead of the actual
        // day name.
        Log.v(LOG_TAG, "getDayName, " + "context = [" + context + "], dateInMillis = [" + dateInMillis + "]");
        Time t = new Time();
        t.setToNow();
        int julianDay = 0;
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);

        if (dateInMillis <= 0 && dateString != null) {
            try {
                Date date = dateFormat.parse(dateString);
                dateInMillis = date.getTime();
            } catch (ParseException e) {
                dateInMillis = 0;
            }
        }
        julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);

        if (julianDay == currentJulianDay) {
            return context.getString(R.string.today);
        } else if (julianDay == currentJulianDay + 1) {
            return context.getString(R.string.tomorrow);
        } else if (julianDay == currentJulianDay - 1) {
            return context.getString(R.string.yesterday);
        } else {
            return readableDayFormat.format(dateInMillis);
        }
    }

    public static int getRelativeDay(String dateString) {
        Time t = new Time();
        t.setToNow();
        int julianDay = 0;
        int currentJulianDay = Time.getJulianDay(System.currentTimeMillis(), t.gmtoff);
        long dateInMillis;

        try {
            Date date = dateFormat.parse(dateString);
            dateInMillis = date.getTime();
        } catch (ParseException e) {
            dateInMillis = 0;
        }
        julianDay = Time.getJulianDay(dateInMillis, t.gmtoff);

        return julianDay - currentJulianDay;
    }

}
