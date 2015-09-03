package it.jaschke.alexandria.api;


import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import it.jaschke.alexandria.R;
import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.services.Utility;

/**
 * Created by saj on 11/01/15.
 */
public class BookListAdapter extends CursorAdapter {

    private static final String LOG_TAG = BookListAdapter.class.getName();
//    private static DownloadImage downloadImage;
//    private Context mContext;


    public static class ViewHolder {
        public final ImageView bookCover;
        public final TextView bookTitle;
        public final TextView bookSubTitle;

        public ViewHolder(View view) {
            bookCover = (ImageView) view.findViewById(R.id.fullBookCover);
            bookTitle = (TextView) view.findViewById(R.id.listBookTitle);
            bookSubTitle = (TextView) view.findViewById(R.id.listBookSubTitle);
        }
    }

    public BookListAdapter(Context context, Cursor c, int flags) {
//        mContext = context;
        super(context, c, flags);
        Log.v(LOG_TAG, "BookListAdapter, " + "context = [" + context + "], c = [" + c
                + "], flags = [" + flags + "]");
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        Log.v(LOG_TAG, "bindView, " + "view = [" + view + "], context = [" + context + "], cursor = [" + cursor + "]");
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        String imgUrl = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));
        Utility.insertImage(mContext, imgUrl, viewHolder.bookCover);
//        Glide.with(mContext)
//                .load(imgUrl)
//                .asBitmap()
//                .error(R.drawable.ic_launcher)
//                .fitCenter()
//                .into(viewHolder.bookCover);

        //to slow
//        downloadImage.download(ge);

//        new DownloadImage(viewHolder.bookCover).execute(imgUrl);

        String bookTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        viewHolder.bookTitle.setText(bookTitle);

        String bookSubTitle = cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        viewHolder.bookSubTitle.setText(bookSubTitle);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        Log.v(LOG_TAG, "newView, " + "context = [" + context + "], cursor = [" + cursor + "], parent = [" + parent + "]");
        View view = LayoutInflater.from(context).inflate(R.layout.book_list_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }
}
