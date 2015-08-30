package it.jaschke.alexandria.services;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.TextView;

import it.jaschke.alexandria.Constants;
import it.jaschke.alexandria.R;

/**
 * Created by frank on 24.08.15.
 */
public class Utility {

    public static void updateAuthorsView(View rootView, String authors){
        TextView authorsView = ((TextView) rootView.findViewById(R.id.authors));
        if (authors != null){
            String[] authorsArr = authors.split(",");
            authorsView.setLines(authorsArr.length);
            authorsView.setText(authors.replace(",", "\n"));
        } else {
            authorsView.setLines(1);
            authorsView.setText(R.string.no_author_found);
        }
    }

    public static void updateCategoriesView(View rootView, String categories) {
        TextView categoriesView = ((TextView) rootView.findViewById(R.id.categories));
        if (categories != null){
            categoriesView.setText(categories);
        } else {
            categoriesView.setText(R.string.no_category_found);
        }
    }

    @SuppressWarnings("ResourceType")
    static public @BookService.BookStatus
    int getBookStatus(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        //suppressWarnings seems important...
        return sp.getInt(Constants.PREF_BOOK_STATUS,
                BookService.BOOK_STATUS_UNKNOWN);
    }

    public static void resetBookStatus(Context c){
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(Constants.PREF_BOOK_STATUS,
                BookService.BOOK_STATUS_UNKNOWN);
        spe.apply();
    }

    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
}
