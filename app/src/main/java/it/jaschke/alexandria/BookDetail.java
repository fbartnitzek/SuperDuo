package it.jaschke.alexandria;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.Utility;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private static final String LOG_TAG = BookDetail.class.getName();
    private final int LOADER_ID = 10;
    private View mRootView;
    private String ean;
    private String mBookTitle;
    private ShareActionProvider mShareActionProvider;

    // TODO: database handling: 2 books of an author, delete one - does author still exist?

    public BookDetail(){
//        Log.v(LOG_TAG, "BookDetail, " + this.hashCode());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            Log.v(LOG_TAG, "onCreateView with args, " + "inflater = [" + inflater + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        } else {
            Log.v(LOG_TAG, "onCreateView without args, " + "inflater = [" + inflater
                    + "], container = [" + container + "], savedInstanceState = [" + savedInstanceState + "]");
        }

        mRootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        return mRootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.book_detail, menu);
        Log.v(LOG_TAG, "onCreateOptionsMenu, " + "menu = [" + menu + "], inflater = [" + inflater + "]");

        MenuItem menuItem = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader, " + "id = [" + id + "], args = [" + args + "]");
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        Log.v(LOG_TAG, "onLoadFinished, " + "loader = [" + loader + "], data = [" + data + "]");
        if (!data.moveToFirst()) {
            return;
        }

        mBookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) mRootView.findViewById(R.id.fullBookTitle)).setText(mBookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) mRootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) mRootView.findViewById(R.id.fullBookDesc)).setText(desc);

        // books without author possible:
        // https://www.googleapis.com/books/v1/volumes?q=isbn:9783864050176

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        Log.v(LOG_TAG, "onLoadFinished - authors: " + authors);
        Utility.updateAuthorsView(mRootView, authors);

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){

            ImageView bookCover = (ImageView) mRootView.findViewById(R.id.fullBookCover);
            Utility.insertImage(getActivity(), imgUrl, bookCover);
            // new DownloadImage((ImageView) mRootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            bookCover.setVisibility(View.VISIBLE);
        }

        String categories = data.getString(
                data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        Utility.updateCategoriesView(mRootView, categories);

        if(mRootView.findViewById(R.id.right_container)!=null){
            mRootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }

        if (mShareActionProvider != null){
            shareBook();
        }

    }

    private void shareBook() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + mBookTitle);
        mShareActionProvider.setShareIntent(shareIntent);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset, " + "loader = [" + loader + "]");
    }

    @Override
    public void onPause() {
        Log.v(LOG_TAG, "onPause, " + "");
        super.onDestroyView();
        if(MainActivity.IS_TABLET && mRootView.findViewById(R.id.right_container)==null){
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}