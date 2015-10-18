package barqsoft.footballscores.service;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;

import barqsoft.footballscores.Utilities;
import barqsoft.footballscores.data.DatabaseHelper;

/**
 * Created by frank on 16.10.15.
 */
public class JsonExtractor {

    private static final String LOG_TAG = JsonExtractor.class.getName();

    private static final String SEASON_LINK = "http://api.football-data.org/alpha/soccerseasons/";
    private static final String MATCH_LINK = "http://api.football-data.org/alpha/fixtures/";
    private static final String LINKS = "_links";
    private static final String SOCCER_SEASON = "soccerseason";
    private static final String SELF = "self";
    private static final String MATCH_DATE = "date";
    private static final String HOME_TEAM = "homeTeamName";
    private static final String AWAY_TEAM = "awayTeamName";
    private static final String RESULT = "result";
    private static final String HOME_GOALS = "goalsHomeTeam";
    private static final String AWAY_GOALS = "goalsAwayTeam";
    private static final String MATCH_DAY = "matchday";
    private static final String AWAY_TEAM_OBJ = "awayTeam";
    private static final String HOME_TEAM_OBJ = "homeTeam";
    private static final String HREF = "href";
    public static final String FIXTURES = "fixtures";

    public static final String TEAMS = "teams";
    private static final String NAME = "name";
    private static final String CREST_URL = "crestUrl";


    public static ArrayList<ContentValues> processTeamsJsonData(String jsonData, Context appContext) {

        ArrayList<ContentValues> values = null;
        try {
            JSONArray teams = new JSONObject(jsonData).getJSONArray(TEAMS);

            //ContentValues to be inserted
            values = new ArrayList<>(teams.length());
//            values = new ContentValues[matches.length()];
            for (int i = 0; i < teams.length(); i++) {

                JSONObject teamData = teams.getJSONObject(i);
                JSONObject links = teamData.getJSONObject(LINKS);

                String selfLink = links.getJSONObject(SELF).getString(HREF);
                String id = Utilities.extractIdFromLink(selfLink);

                String name = teamData.getString(NAME);

                String picUrl = teamData.getString(CREST_URL);

                // bugfix for Würzburger Kickers (3. Bundesliga)
                values.add(DatabaseHelper.buildTeamCVs(id, Utilities.decodeHtml(name), picUrl));
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return values;
    }

    public static ArrayList<ContentValues> processScoresJsonData(String jsonData, Context context) {
        Log.v(LOG_TAG, "processScoresJsonData, " + "jsonData = [" + jsonData + "], context = [" + context + "]");


        ArrayList<ContentValues> values = null;

        try {
            JSONArray matches = new JSONObject(jsonData).getJSONArray(FIXTURES);

            //ContentValues to be inserted
            values = new ArrayList<>(matches.length());
//            values = new ContentValues[matches.length()];
            for (int i = 0; i < matches.length(); i++) {

                JSONObject matchData = matches.getJSONObject(i);
                JSONObject links = matchData.getJSONObject(LINKS);
                String league = links.getJSONObject(SOCCER_SEASON).getString(HREF);
                league = Utilities.extractIdFromLink(league);

                //This if statement controls which leagues we're interested in the data from.
                //add leagues here in order to have them be added to the DB.
                // If you are finding no data in the app, check that this contains all the leagues.
                // If it doesn't, that can cause an empty DB, bypassing the dummy data routine.
                if (Utilities.isSupportedLeague(Integer.parseInt(league))) {

                    String matchId = links.getJSONObject(SELF).getString(HREF);
                    matchId = Utilities.extractIdFromLink(matchId);

                    // both teams
                    String awayLink = links.getJSONObject(AWAY_TEAM_OBJ).getString(HREF);
                    String homeLink = links.getJSONObject(HOME_TEAM_OBJ).getString(HREF);
                    String awayId = Utilities.extractIdFromLink(awayLink);
                    String homeId = Utilities.extractIdFromLink(homeLink);

                    // TODO: looks quite wrong ...
                    String date = matchData.getString(MATCH_DATE);
                    String time = date.substring(date.indexOf("T") + 1, date.indexOf("Z"));
                    date = date.substring(0, date.indexOf("T"));
                    SimpleDateFormat utcDate = new SimpleDateFormat("yyyy-MM-ddHH:mm:ss");
                    utcDate.setTimeZone(TimeZone.getTimeZone("UTC"));
                    try {
                        Date parsedDate = utcDate.parse(date + time);
                        SimpleDateFormat newDate = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
                        newDate.setTimeZone(TimeZone.getDefault());
                        date = newDate.format(parsedDate);
                        time = date.substring(date.indexOf(":") + 1);
                        date = date.substring(0, date.indexOf(":"));

                    } catch (Exception e) {
                        Log.d(LOG_TAG, "error here!");
                        Log.e(LOG_TAG, e.getMessage());
                    }

                    String homeGoals = matchData.getJSONObject(RESULT).getString(HOME_GOALS);
                    String awayGoals = matchData.getJSONObject(RESULT).getString(AWAY_GOALS);

                    String matchDay = matchData.getString(MATCH_DAY);

                    values.add(DatabaseHelper.buildMatchCVs(date, time, homeId, awayId,
                            league, homeGoals, awayGoals, matchId, matchDay));
                }
            }

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return values;
    }

}
