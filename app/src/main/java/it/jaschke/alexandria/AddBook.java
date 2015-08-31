package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.LocalBroadcastManager;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.BookService;
import it.jaschke.alexandria.services.DownloadImage;
import it.jaschke.alexandria.services.Utility;

// integrated scanner:
// https://github.com/journeyapps/zxing-android-embedded


public class AddBook extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = AddBook.class.getName();
    private EditText mEanView;

    private final int LOADER_ID = 1;
    private View mRootView;

    private String mBookTitle;

    public AddBook(){
//        Log.v(LOG_TAG, "AddBook, " + this.hashCode());
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Constants.ACTION_BROADCAST_BOOK_STATUS.equals(intent.getAction())) {
                int status = intent.getIntExtra(Constants.EXTRA_BOOK_STATUS, -1);
                Log.v(LOG_TAG, "onReceive, " + "status=" + status);
                if (status != BookService.BOOK_STATUS_OK) {
                    updateErrorView(status);
                } else {
                    // TODO: update book?
                }
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                mReceiver, new IntentFilter(Constants.ACTION_BROADCAST_BOOK_STATUS));
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(
                mReceiver);

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

                // convert isbn-10
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }

                if (ean.length() < 13) {
                    return;
                }

                // editText limited to 13 characters - no validation needed
                // maybe except hash-function... :-)
                clearFields();

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
                launchEmbeddedScanner();

            }
        });

        mRootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v(LOG_TAG, "conClick, " + "view = [" + view + "]");
                if (mBookTitle != null) {
                    Toast.makeText(getActivity(),
                            mBookTitle + getString(R.string.added_to_list),
                            Toast.LENGTH_LONG).show();
                    //  TODO: how to check if list contains item?
                    // add vs already contains
//                    Utility.resetBookStatus(getActivity());
                    clearFields();
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
                clearFields();
            }
        });

        if(savedInstanceState!=null){
            mEanView.setText(savedInstanceState.getString(Constants.STATE_EAN_CONTENT));
//            mEanView.setHint("");
        }

        return mRootView;
    }

    private void launchEmbeddedScanner() {
        // instead of:
        //  IntentIntegrator integrator = new IntentIntegrator(getActivity());
        //  integrator.initiateScan();

        IntentIntegrator.forSupportFragment(this).initiateScan();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(LOG_TAG, "onActivityResult, " + "requestCode = [" + requestCode
                + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        IntentResult scanResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, data);
        if (scanResult != null) {
            Log.v(LOG_TAG, "onActivityResult with scanResult, " + "requestCode = ["
                    + requestCode + "], resultCode = [" + resultCode + "], data = [" + data + "]");
            String barCode = scanResult.getContents();
            if (barCode != null){
                mEanView.setText(barCode);
            }

        } else {
            Log.v(LOG_TAG, "onActivityResult without scanResult, " + "requestCode = [" + requestCode
                    + "], resultCode = [" + resultCode + "], data = [" + data + "]");
        }

        super.onActivityResult(requestCode, resultCode, data);
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
        if (!data.moveToFirst()) {  //empty
            Log.v(LOG_TAG, "onLoadFinished - empty loader");
//            updateErrorView();
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

    private void updateErrorView(int status) {

        TextView tv = (TextView) getView().findViewById(R.id.bookTitle);
        TextView subTv = (TextView) getView().findViewById(R.id.bookSubTitle);
        ImageView imgv = (ImageView) getView().findViewById(R.id.bookCover);
        if (tv != null && subTv != null) {
            int detailedMessage = R.string.status_empty;
            boolean errorHappened = false;
            // first test network
            if (!Utility.isNetworkAvailable(getActivity())) {
                Log.v(LOG_TAG, "updateErrorView, " + "no network");
                detailedMessage = R.string.status_no_network;
                errorHappened = true;
            } else {
                // then check against all sorts of other errors
                switch (status) {
                    case BookService.BOOK_STATUS_SERVER_DOWN:
                        detailedMessage = R.string.status_server_down;
                        errorHappened = true;
                        Log.v(LOG_TAG, "updateErrorView, " + "server down");
                        break;
                    case BookService.BOOK_STATUS_SERVER_INVALID:
                        detailedMessage = R.string.status_server_error;
                        errorHappened = true;
                        Log.v(LOG_TAG, "updateErrorView, " + "server invalid");
                        break;
                    case BookService.BOOK_STATUS_UNKNOWN:
                        detailedMessage = R.string.status_unknown_error;
                        errorHappened = true;
                        break;
                    case BookService.BOOK_STATUS_NOT_FOUND:
                        detailedMessage = R.string.status_book_not_found;
                        errorHappened = true;
                        Log.v(LOG_TAG, "updateErrorView, " + "no book found");
                        break;
                    default:
                        Log.v(LOG_TAG, "updateErrorView, " + "other case, status=" + status);
                }
            }

            if (errorHappened) {
                clearFields();
                tv.setText(R.string.status_no_information);
                tv.setVisibility(View.VISIBLE);
                subTv.setText(detailedMessage);
                subTv.setVisibility(View.VISIBLE);
                imgv.setImageResource(android.R.drawable.stat_notify_error);
                imgv.setVisibility(View.VISIBLE);
                Log.v(LOG_TAG, "updateErrorView, " + "errorHappened, shown, resetting state");


            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(LOG_TAG, "onAttach, " + "activity = [" + activity + "]");
        super.onAttach(activity);
        activity.setTitle(R.string.scan);
    }

//    @Override
//    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
//        if (Constants.PREF_BOOK_STATUS.equals(s)) {
//            updateErrorView();
//        }
//    }
}
