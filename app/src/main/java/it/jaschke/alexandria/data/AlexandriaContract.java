package it.jaschke.alexandria.data;

/**
 * Created by saj on 22/12/14.
 */

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class AlexandriaContract{

    public static final String CONTENT_AUTHORITY = "it.jaschke.alexandria";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOOKS = "books";
    public static final String PATH_AUTHORS = "authors";
    public static final String PATH_CATEGORIES = "categories";
    private static final String LOG_TAG = AlexandriaContract.class.getName();
    public static final String PATH_FULLBOOK = "fullbook";

    public static final class BookEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKS).build();
        public static final Uri FULL_CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_FULLBOOK).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_BOOKS;
        public static final String TABLE_NAME = "books";
        public static final String TITLE = "title";
        public static final String IMAGE_URL = "imgurl";
        public static final String SUBTITLE = "subtitle";
        public static final String DESC = "description";

        public static Uri buildBookUri(long id) {
            Log.v(LOG_TAG, "buildBookUri, " + "id = [" + id + "]");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildFullBookUri(long id) {
            Log.v(LOG_TAG, "buildFullBookUri, " + "id = [" + id + "]");
            return ContentUris.withAppendedId(FULL_CONTENT_URI, id);
        }
    }

    public static final class AuthorEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_AUTHORS).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_AUTHORS;
        public static final String TABLE_NAME = "authors";
        public static final String AUTHOR = "author";

        public static Uri buildAuthorUri(long id) {
            Log.v(LOG_TAG, "buildAuthorUri, " + "id = [" + id + "]");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORIES).build();
        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORIES;
        public static final String TABLE_NAME = "categories";
        public static final String CATEGORY = "category";
        public static Uri buildCategoryUri(long id) {
            Log.v(LOG_TAG, "buildCategoryUri, " + "id = [" + id + "]");
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }
}