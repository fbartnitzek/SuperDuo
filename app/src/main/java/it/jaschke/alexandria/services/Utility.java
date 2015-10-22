package it.jaschke.alexandria.services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

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

//    @SuppressWarnings("ResourceType")
//    static public @BookService.BookStatus
//    int getBookStatus(Context c) {
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
//        //suppressWarnings seems important...
//        return sp.getInt(Constants.PREF_BOOK_STATUS,
//                BookService.BOOK_STATUS_UNKNOWN);
//    }

//    public static void resetBookStatus(Context c){
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
//        SharedPreferences.Editor spe = sp.edit();
//        spe.putInt(Constants.PREF_BOOK_STATUS,
//                BookService.BOOK_STATUS_UNKNOWN);
//        spe.apply();
//    }

    public static void insertImage(Context context, String imgUrl, ImageView imgView){
        Glide.with(context)
                .load(imgUrl)
                .asBitmap()
                .error(R.drawable.ic_launcher)
                .fitCenter()
                .into(imgView);
    }

    static public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    // isbn-validation source:
    // https://github.com/MichaelVdheeren/Bookshelf/blob/master/src/bookshelf/ISBN.java

//    /**
//     * Check if the given value is a valid ISBN.
//     * @param	value
//     * 			The <value> to check for it's ISBN validity.
//     * @return	True if the given <value> is a valid ISBN in the ISBN-10
//     * 			or ISBN-13 format. False otherwise.
//     */
//    public static boolean isValidISBN(String value) {
//        if (value.length() == 10)
//            return isValidISBN10(value);
//        else if (value.length() == 13)
//            return isValidISBN13(value);
//
//        return false;
//    }
//
//    /**
//     * Check if the given value is a valid ISBN-10.
//     * @param 	value
//     * 			The <value> to check for it's ISBN validity.
//     * @return	True if the given <value> is a valid ISBN in the ISBN-10
//     * 			format. False otherwise.
//     */
//    private static boolean isValidISBN10(String value) {
//        if (value.length() != 10)
//            return false;
//
//        int sum = 0, n;
//        String s;
//
//        for (int i=10; i>0; i--) {
//            s = value.substring(10-i, 10-i+1);
//
//            if (s.equals("X") || s.equals("x")) {
//                n = 10;
//            } else {
//                try {
//                    n = Integer.parseInt(s);
//                } catch (NumberFormatException e) {
//                    return false;
//                }
//            }
//            sum += i*n;
//        }
//
//        return (sum%11 == 0);
//    }

    /**
     * Check if the given value is a valid ISBN-13.
     * @param 	value
     * 			The <value> to check for it's ISBN validity.
     * @return	True if the given <value> is a valid ISBN in the ISBN-13
     * 			format. False otherwise.
     */
    public static boolean isValidISBN13(String value) {
        int sum = 0, n, m;
        String s;

        for (int i=1; i<13; i++) {
            s = value.substring(i-1,i);
            try {
                n = Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return false;
            }

            m = ((i%2 == 1) ? 1 : 3);
            sum += n*m;
        }

        try {
            n = Integer.parseInt(value.substring(12,13));
        } catch (NumberFormatException e) {
            return false;
        }

        return (((10-sum%10)%10)-n == 0);
    }
}
