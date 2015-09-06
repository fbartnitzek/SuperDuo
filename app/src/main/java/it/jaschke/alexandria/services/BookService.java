package it.jaschke.alexandria.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.IntDef;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.HttpURLConnection;
import java.net.URL;

import it.jaschke.alexandria.Constants;
import it.jaschke.alexandria.data.AlexandriaContract;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class BookService extends IntentService {

    private final String LOG_TAG = BookService.class.getSimpleName();

    public static final String FETCH_BOOK = "it.jaschke.alexandria.services.action.FETCH_BOOK";
    public static final String DELETE_BOOK = "it.jaschke.alexandria.services.action.DELETE_BOOK";
    public static final String EAN = "it.jaschke.alexandria.services.extra.EAN";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({BOOK_STATUS_OK, BOOK_STATUS_SERVER_DOWN, BOOK_STATUS_SERVER_INVALID,
            BOOK_STATUS_UNKNOWN, BOOK_STATUS_INVALID_ISBN, BOOK_STATUS_NOT_FOUND,
            BOOK_STATUS_ALREADY_STORED})
    public @interface BookStatus {}

    public static final int BOOK_STATUS_OK = 0;
    public static final int BOOK_STATUS_SERVER_DOWN = 1;
    public static final int BOOK_STATUS_SERVER_INVALID = 2;
    public static final int BOOK_STATUS_UNKNOWN = 3;
    public static final int BOOK_STATUS_INVALID_ISBN = 4;
    public static final int BOOK_STATUS_NOT_FOUND = 5;
    public static final int BOOK_STATUS_ALREADY_STORED = 6;

    public BookService() {
        super("Alexandria");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (FETCH_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                searchBook(ean);
            } else if (DELETE_BOOK.equals(action)) {
                final String ean = intent.getStringExtra(EAN);
                deleteBook(ean);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void deleteBook(String ean) {
        if(ean!=null) {
            getContentResolver().delete(
                    AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)), null, null);
        }
    }


    private void searchBook (String ean){

        if(ean.length()!=13){
            return;
        }


        if (isEanInList(ean)){

            Log.v(LOG_TAG, "searchBook - book already stored, " + "ean = [" + ean + "]");
            broadcastBookStatus(getApplicationContext(), BOOK_STATUS_ALREADY_STORED);
            return;

        } else if (Utility.isNetworkAvailable(getApplicationContext())){

            String jsonBook = fetchJsonBook(ean);

            if (jsonBook != null){
                parseJsonAndStoreBook(ean, jsonBook);
            }
        }
    }


    private boolean isEanInList(String ean){
        Cursor cursor = getContentResolver().query(
                AlexandriaContract.BookEntry.buildBookUri(Long.parseLong(ean)),
                null,   // no select
                null,   // no where
                null,   // no where
                null    // no sorting
        );

        boolean isInList = cursor.getCount() > 0;
        cursor.close();
        return isInList;
    }

    /**
     * Handle action fetchBook in the provided background thread with the provided
     * parameters.
     */
    private String fetchJsonBook(String ean) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            final String FORECAST_BASE_URL = "https://www.googleapis.com/books/v1/volumes?";
//            final String FORECAST_BASE_URL = "https://www.google.c?";
            final String QUERY_PARAM = "q";

            final String ISBN_PARAM = "isbn:" + ean;

            Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                    .appendQueryParameter(QUERY_PARAM, ISBN_PARAM)
                    .build();

//            Log.v(LOG_TAG, "fetchBook, " + "url = [" + builtUri.toString()+ "]");
            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream != null) {
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                    buffer.append("\n");
                }

                if (buffer.length() > 0) {
                    return buffer.toString();
                } else {
                    broadcastBookStatus(getApplicationContext(), BOOK_STATUS_SERVER_DOWN);
                }
            }

        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            broadcastBookStatus(getApplicationContext(), BOOK_STATUS_SERVER_DOWN);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                    // data should be viewable - server not down, just strange
                    // setBookStatus(getApplicationContext(), BOOK_STATUS_SERVER_DOWN);
                }
            }
        }
        return null;
    }

    private void parseJsonAndStoreBook(String ean, String jsonBook) {

        final String ITEMS = "items";
        final String TOTAL_ITEMS = "totalItems";

        final String VOLUME_INFO = "volumeInfo";

        final String TITLE = "title";
        final String SUBTITLE = "subtitle";
        final String AUTHORS = "authors";
        final String DESC = "description";
        final String CATEGORIES = "categories";
        final String IMG_URL_PATH = "imageLinks";
        final String IMG_URL = "thumbnail";

        Log.v(LOG_TAG, "fetchBook, " + "bookJsonString= [" + jsonBook + "]");

        try {
            JSONObject bookJson = new JSONObject(jsonBook);
            JSONArray bookArray;
            if(bookJson.has(ITEMS)){
                bookArray = bookJson.getJSONArray(ITEMS);
            } else {

                if (!Utility.isValidISBN13(ean)){
                    Log.v(LOG_TAG, "parseJsonAndStoreBook, invalid isbn");
                    broadcastBookStatus(getApplicationContext(), BOOK_STATUS_INVALID_ISBN);
                    return;
                }

                String totalItems = null;
                if (bookJson.has(TOTAL_ITEMS)) {
                    totalItems = bookJson.getString(TOTAL_ITEMS);
                }

                if (totalItems != null && "0".equals(totalItems)) {
                    // no book found
                    Log.v(LOG_TAG, "fetchBook, " + "no book found");
                    broadcastBookStatus(getApplicationContext(), BOOK_STATUS_NOT_FOUND);
                } else {
                    // no valid answer
                    Log.v(LOG_TAG, "fetchBook, " + "no valid answer");
                    broadcastBookStatus(getApplicationContext(), BOOK_STATUS_SERVER_INVALID);
                }
                return;
            }

            JSONObject bookInfo = ((JSONObject) bookArray.get(0)).getJSONObject(VOLUME_INFO);

            String title = bookInfo.getString(TITLE);

            String subtitle = "";
            if(bookInfo.has(SUBTITLE)) {
                subtitle = bookInfo.getString(SUBTITLE);
            }

            String desc="";
            if(bookInfo.has(DESC)){
                desc = bookInfo.getString(DESC);
            }

            String imgUrl = "";
            if(bookInfo.has(IMG_URL_PATH) && bookInfo.getJSONObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getJSONObject(IMG_URL_PATH).getString(IMG_URL);
            }

            persistBook(ean, title, subtitle, desc, imgUrl);

            if(bookInfo.has(AUTHORS)) {
                persistAuthors(ean, bookInfo.getJSONArray(AUTHORS));
            }
            if(bookInfo.has(CATEGORIES)){
                persistCategories(ean, bookInfo.getJSONArray(CATEGORIES));
            }
            broadcastBookStatus(getApplicationContext(), BOOK_STATUS_OK);
        } catch (JSONException e) {
            broadcastBookStatus(getApplicationContext(), BOOK_STATUS_SERVER_INVALID);
        }

    }

    private void persistBook(String ean, String title, String subtitle, String desc, String imgUrl) {
        ContentValues values= new ContentValues();
        values.put(AlexandriaContract.BookEntry._ID, ean);
        values.put(AlexandriaContract.BookEntry.TITLE, title);
        values.put(AlexandriaContract.BookEntry.IMAGE_URL, imgUrl);
        values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle);
        values.put(AlexandriaContract.BookEntry.DESC, desc);
        getContentResolver().insert(AlexandriaContract.BookEntry.CONTENT_URI,values);
    }

    private void persistAuthors(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.AuthorEntry._ID, ean);
            values.put(AlexandriaContract.AuthorEntry.AUTHOR, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void persistCategories(String ean, JSONArray jsonArray) throws JSONException {
        ContentValues values= new ContentValues();
        for (int i = 0; i < jsonArray.length(); i++) {
            values.put(AlexandriaContract.CategoryEntry._ID, ean);
            values.put(AlexandriaContract.CategoryEntry.CATEGORY, jsonArray.getString(i));
            getContentResolver().insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
            values= new ContentValues();
        }
    }

    private void broadcastBookStatus(Context c, @BookStatus int bookStatus){
        Intent messageIntent = new Intent(Constants.ACTION_BROADCAST_BOOK_STATUS);
        messageIntent.putExtra(Constants.EXTRA_BOOK_STATUS, bookStatus);

//        if (title != null){
//            messageIntent.putExtra(Constants.EXTRA_BOOK_TITLE, title);
//        }
        Log.v(LOG_TAG, "broadcastBookStatus, " + "bookStatus = [" + bookStatus + "]");
        LocalBroadcastManager.getInstance(c).sendBroadcast(messageIntent);
    }

 }