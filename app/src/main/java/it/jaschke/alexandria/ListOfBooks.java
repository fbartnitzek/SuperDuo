package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter mBookListAdapter;
    private static final String LOG_TAG = ListOfBooks.class.getName();
    private ListView mBookList;
    private int mPosition = ListView.INVALID_POSITION;
    private EditText mSearchText;
    private final String selection = AlexandriaContract.BookEntry.TITLE
            +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";

    private final int LOADER_ID = 10;

    public ListOfBooks() {
//        Log.v(LOG_TAG, "ListOfBooks, " + this.hashCode());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        Log.v(LOG_TAG, "onCreate, " + "savedInstanceState = [" + savedInstanceState + "]");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.v(LOG_TAG, "onCreateView, " + "inflater = [" + inflater + "], container = ["
                + container + "], savedInstanceState = [" + savedInstanceState + "]");

        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);
        mSearchText = (EditText) rootView.findViewById(R.id.searchText);
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                ListOfBooks.this.restartLoader();
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.STATE_SEARCH_TEXT)){
            mSearchText.setText(savedInstanceState.getString(Constants.STATE_SEARCH_TEXT));
        }

        rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.v(LOG_TAG, "onClick, " + "v = [" + v + "]");
                        ListOfBooks.this.restartLoader();
                    }
                }
        );

        String searchString = null;
        if (mSearchText != null && !"".equals(mSearchText.getText().toString())){
            searchString = "%" + mSearchText.getText().toString() + "%";
        }
        Log.v(LOG_TAG, "onCreateView, searchString=" + searchString);
        // dynamic where clause
        // might be useless through restartLoader...
        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                searchString==null?null:selection, // cols for "where" clause
                searchString==null?null:new String[]{searchString, searchString}, // values for "where" clause
                null  // sort order
        );

        mBookListAdapter = new BookListAdapter(getActivity(), cursor, 0);

        mBookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        mBookList.setAdapter(mBookListAdapter);

        mBookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.v(LOG_TAG, "onItemClick, " + "adapterView = [" + adapterView + "], view = ["
                        + view + "], mPosition = [" + position + "], l = [" + l + "]");
                Cursor cursor = mBookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(
                                    AlexandriaContract.BookEntry._ID)));
                }
            }
        });

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mSearchText != null){
            outState.putString(Constants.STATE_SEARCH_TEXT, mSearchText.getText().toString());
        }
        super.onSaveInstanceState(outState);
    }

    private void restartLoader(){
        Log.v(LOG_TAG, "restartLoader, " + "");
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Log.v(LOG_TAG, "onCreateLoader, " + "id = [" + id + "], args = [" + args + "]");
//        final String selection = AlexandriaContract.BookEntry.TITLE
//                +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = mSearchText.getText().toString();

        if(searchString.length()>0){
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        Log.v(LOG_TAG, "onLoadFinished, " + "loader = [" + loader + "], data = [" + data + "]");
        mBookListAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mBookList.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        Log.v(LOG_TAG, "onLoaderReset, " + "loader = [" + loader + "]");
        mBookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        Log.v(LOG_TAG, "onAttach, " + "activity = [" + activity + "]");
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }
}
