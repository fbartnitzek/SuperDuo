package it.jaschke.alexandria;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.extern.IntentIntegrator;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.services.Utility;


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AddBook.class.getName();
    private EditText mEanView;
//    private Button mScanButton;
//    private TextView mTitleView;
//    private TextView mSubtitleView;
//    private TextView mAuthorsView;
//    private ImageView mCoverView;
//    private TextView mCategoryView;
//    private Button mDeleteButton;
//    private Button mSaveButton;

    private final int LOADER_ID = 1;
    private View mRootView;


    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";
    private String mBookTitle;

    public AddBook(){
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mEanView !=null) {
            Log.v(LOG_TAG, "onSaveInstanceState with mEanView, " + "outState = [" + outState + "]");
            outState.putString(Constants.STATE_EAN_CONTENT, mEanView.getText().toString());
        } else {
            Log.v(LOG_TAG, "onSaveInstanceState without mEanView, " + "outState = [" + outState + "]");
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mRootView = inflater.inflate(R.layout.fragment_add_book, container, false);
        mEanView = (EditText) mRootView.findViewById(R.id.ean);
        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = ["
                + container + "], savedInstanceState = [" + savedInstanceState + "]");

        mEanView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no need
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //no need
            }

            @Override
            public void afterTextChanged(Editable s) {
                String ean = s.toString();
                Log.v(LOG_TAG, "afterTextChanged, " + "s = [" + s + "]");
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }

                if (ean.length() < 13) {
                    clearFields();
                    return;
                }

                // editText limited to 13 characters

                //Once we have an ISBN, start a book intent
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.FETCH_BOOK);
                getActivity().startService(bookIntent);
                AddBook.this.restartLoader();
            }
        });

        mRootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
                Log.v(LOG_TAG, "onClick - scan intent, " + "v = [" + v + "]");
                IntentIntegrator integrator = new IntentIntegrator(getActivity());
                integrator.initiateScan();

            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "onClick, " + "view = [" + view + "]");
                if (mBookTitle != null) {
                    Toast.makeText(getActivity(),
                            mBookTitle + getString(R.string.added_to_list),
                            Toast.LENGTH_LONG).show();
                }

                mEanView.setText("");
            }
        });

        mRootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "onClick, " + "view = [" + view + "]");
                Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, mEanView.getText().toString());
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                mEanView.setText("");
            }
        });

        if(savedInstanceState!=null){
            mEanView.setText(savedInstanceState.getString(Constants.STATE_EAN_CONTENT));
            mEanView.setHint("");
        }

        return mRootView;
    }

    private void restartLoader(){

        Log.v(LOG_TAG, "restartLoader, " + "");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.v(LOG_TAG, "onCreateLoader, " + "id = [" + id + "], args = [" + args + "]");
        if(mEanView.getText().length()==0){
            return null;
        }
        String eanStr= mEanView.getText().toString();
        if(eanStr.length()==10 && !eanStr.startsWith("978")){
            eanStr="978"+eanStr;
        }
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(eanStr)),
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
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText(mBookTitle);

        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        if (bookSubTitle == null){
            bookSubTitle = "";
        }
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText(bookSubTitle);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));
        Log.v(LOG_TAG, "onLoadFinished: " + authors);
        Utility.updateAuthorsView(mRootView, authors);

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        if(Patterns.WEB_URL.matcher(imgUrl).matches()){
            new DownloadImage((ImageView) mRootView.findViewById(R.id.bookCover)).execute(imgUrl);
            mRootView.findViewById(R.id.bookCover).setVisibility(View.VISIBLE);
        }

        String categories = data.getString(
                data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        Utility.updateCategoriesView(mRootView, categories);

        mRootView.findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {
        Log.v(LOG_TAG, "onLoaderReset, " + "loader = [" + loader + "]");
    }

    private void clearFields(){
        Log.v(LOG_TAG, "clearFields, " + "");
        ((TextView) mRootView.findViewById(R.id.bookTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.bookSubTitle)).setText("");
        ((TextView) mRootView.findViewById(R.id.authors)).setText("");
        ((TextView) mRootView.findViewById(R.id.categories)).setText("");
        mRootView.findViewById(R.id.bookCover).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.save_button).setVisibility(View.INVISIBLE);
        mRootView.findViewById(R.id.delete_button).setVisibility(View.INVISIBLE);
    }

    public void setIsbn(String isbn) {
        if (mEanView != null) {
            mEanView.setText(isbn);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(LOG_TAG, "onAttach, " + "activity = [" + activity + "]");
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }
}
