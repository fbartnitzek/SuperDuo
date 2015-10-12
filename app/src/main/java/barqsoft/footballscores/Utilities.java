package barqsoft.footballscores;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yehya khaled on 3/3/2015.
 */
public class Utilities {
//    public static final int SERIE_A = 357;
//    public static final int PREMIER_LEGAUE = 354;
//    public static final int CHAMPIONS_LEAGUE = 362;
//    public static final int PRIMERA_DIVISION = 358;
//    public static final int BUNDESLIGA = 351;
    private static final String LOG_TAG = Utilities.class.getName();
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    public static String getLeague(int league_num) {
        Log.v(LOG_TAG, "getLeague, " + "league_num = [" + league_num + "]");
        switch (league_num) {
            case Constants.SERIE_A:
                return "Seria A";
            case Constants.PREMIER_LEAGUE:
                return "Premier League";
            case Constants.CHAMPIONS_LEAGUE:
                return "UEFA Champions League";
            case Constants.PRIMERA_DIVISION:
                return "Primera Division";
            case Constants.BUNDESLIGA1:
                return "1. Bundesliga";
            case Constants.BUNDESLIGA2:
                return "2. Bundesliga";
            case Constants.BUNDESLIGA3:
                return "3. Bundesliga";
            default:
                return "Not known League Please report";
        }
    }

    public static String getMatchDay(int match_day, int league_num) {
        Log.v(LOG_TAG, "getMatchDay, " + "match_day = [" + match_day + "], league_num = [" + league_num + "]");
        if (league_num == Constants.CHAMPIONS_LEAGUE) {
            if (match_day <= 6) {
                return "Group Stages, Matchday : 6";
            } else if (match_day == 7 || match_day == 8) {
                return "First Knockout round";
            } else if (match_day == 9 || match_day == 10) {
                return "QuarterFinal";
            } else if (match_day == 11 || match_day == 12) {
                return "SemiFinal";
            } else {
                return "Final";
            }
        } else {
            return "Matchday : " + String.valueOf(match_day);
        }
    }

    public static String getScores(int home_goals, int awaygoals) {
        Log.v(LOG_TAG, "getScores, " + "home_goals = [" + home_goals + "], awaygoals = [" + awaygoals + "]");
        if (home_goals < 0 || awaygoals < 0) {
            return "?? - ??";
        } else {
            return String.valueOf(home_goals) + " - " + String.valueOf(awaygoals);
        }
    }

    public static int getTeamCrestByTeamName(String teamname) {
        Log.v(LOG_TAG, "getTeamCrestByTeamName, " + "teamname = [" + teamname + "]");
        if (teamname == null) {
            return R.drawable.no_icon;
        }
        switch (teamname) { //This is the set of icons that are currently in the app. Feel free to find and add more
            //as you go.
            case "Arsenal London FC":
                return R.drawable.arsenal;
            case "Manchester United FC":
                return R.drawable.manchester_united;
            case "Swansea City":
                return R.drawable.swansea_city_afc;
            case "Leicester City":
                return R.drawable.leicester_city_fc_hd_logo;
            case "Everton FC":
                return R.drawable.everton_fc_logo1;
            case "West Ham United FC":
                return R.drawable.west_ham;
            case "Tottenham Hotspur FC":
                return R.drawable.tottenham_hotspur;
            case "West Bromwich Albion":
                return R.drawable.west_bromwich_albion_hd_logo;
            case "Sunderland AFC":
                return R.drawable.sunderland;
            case "Stoke City FC":
                return R.drawable.stoke_city;
            default:
                return R.drawable.no_icon;
        }
    }

    public static String extractTeamIdFromLink(String teamLink) {
        //example: "http://api.football-data.org/alpha/teams/254"

        if (teamLink != null) {
            int pos = teamLink.lastIndexOf("/");
            if (pos > 0) {
                return teamLink.substring(pos + 1);
            }
        }

        return null;
    }

    public static boolean isSupportedLeague(int league) {
//        if (league == Constants.PREMIER_LEAGUE ||
//                league == Constants.SERIE_A ||
//                league == Constants.BUNDESLIGA1 ||
//                league == Constants.BUNDESLIGA2 ||
//                league == Constants.PRIMERA_DIVISION ||
//                league == Constants.BUNDESLIGA3) {
//            return true;
//        } else {
//            return false;
//        }
        return true;
    }

    public static String[] formatDate(Date date) {
        String[] result = new String[1];
        result[0] = dateFormat.format(date);
        return result;
    }
}
