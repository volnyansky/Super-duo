package it.jaschke.alexandria;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.database.Cursor;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ViewDataBinding;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;
import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;
import it.jaschke.alexandria.data.BindableString;
import it.jaschke.alexandria.services.BookRequest;
import it.jaschke.alexandria.services.BookServiceSpice;


public class AddBook extends Fragment {
    private static final String TAG = "INTENT_TO_SCAN_ACTIVITY";
    private EditText ean;
    private final int LOADER_ID = 1;
    private View rootView;
    private final String EAN_CONTENT = "eanContent";
    private static final String SCAN_FORMAT = "scanFormat";
    private static final String SCAN_CONTENTS = "scanContents";

    private String mScanFormat = "Format:";
    private String mScanContents = "Contents:";

    private SpiceManager spiceManager = new SpiceManager(BookServiceSpice.class);

    private Model model;

    public static class Model extends BaseObservable {

        public final BindableString isbn = new BindableString();
        public final ObservableField<String> title = new ObservableField<>();
        public final ObservableField<String> subtitle = new ObservableField<>();
        public final ObservableField<String> description = new ObservableField<>();
        public final ObservableField<String> imageUrl = new ObservableField<>();
        public final ObservableField<Boolean> isConnected = new ObservableField<>();
        public final ObservableField<Boolean> bookFound = new ObservableField<>();
        public final ObservableField<Boolean> inProgress = new ObservableField<>();
        ObservableArrayList<String> authors = new ObservableArrayList<>();
        ObservableArrayList<String> categories = new ObservableArrayList<>();

        private ContentResolver contentResolver;
        private Context context;

        @Bindable
        public String getAuthorsList() {
            String out = "";
            for (String a : authors) {
                out += a + "\n";
            }
            return out;
        }

        @Bindable
        public String getCategoriesList() {
            String out = "";
            for (String a : categories) {
                out += a + "\n";
            }
            return out;
        }

        public Model(Context context) {
            bookFound.set(false);
            this.context = context;
            this.contentResolver = context.getContentResolver();
            inProgress.set(false);


        }

        public void saveBook() {

            //check if book is exists
            Cursor cursor = contentResolver.query(AlexandriaContract.BookEntry.CONTENT_URI,
                    new String[]{AlexandriaContract.BookEntry._ID},
                    AlexandriaContract.BookEntry._ID + "=?",
                    new String[]{isbn.get()}, null);

            if (cursor.getCount()>0) {
                Toast.makeText(context, R.string.already_exists, Toast.LENGTH_LONG).show();
                return;
            }
            ContentValues values = new ContentValues();

            values.put(AlexandriaContract.BookEntry._ID, isbn.get());
            values.put(AlexandriaContract.BookEntry.TITLE, title.get());
            values.put(AlexandriaContract.BookEntry.IMAGE_URL, imageUrl.get());
            values.put(AlexandriaContract.BookEntry.SUBTITLE, subtitle.get());
            values.put(AlexandriaContract.BookEntry.DESC, description.get());

            contentResolver.insert(AlexandriaContract.BookEntry.CONTENT_URI, values);

            if (authors.size() > 0) {
                values = new ContentValues();
                for (int i = 0; i < authors.size(); i++) {
                    values.put(AlexandriaContract.AuthorEntry._ID, isbn.get());
                    values.put(AlexandriaContract.AuthorEntry.AUTHOR, authors.get(i));
                    contentResolver.insert(AlexandriaContract.AuthorEntry.CONTENT_URI, values);
                    values = new ContentValues();
                }
            }
            if (categories.size() > 0) {
                values = new ContentValues();
                for (int i = 0; i < categories.size(); i++) {
                    values.put(AlexandriaContract.CategoryEntry._ID, isbn.get());
                    values.put(AlexandriaContract.CategoryEntry.CATEGORY, categories.get(i));
                    contentResolver.insert(AlexandriaContract.CategoryEntry.CONTENT_URI, values);
                    values = new ContentValues();
                }

            }
            bookFound.set(false);
            isbn.set("");
            title.set("");
            subtitle.set("");
            description.set("");
            authors.clear();
            categories.clear();


            Toast.makeText(context, R.string.book_saved, Toast.LENGTH_LONG).show();

        }

        public void clearFields() {
            title.set("");
            subtitle.set("");
            description.set("");
            authors.clear();
            categories.clear();
            imageUrl.set("");
            notifyPropertyChanged(it.jaschke.alexandria.BR.authorsList);
            notifyPropertyChanged(it.jaschke.alexandria.BR.categoriesList);
        }

    }

    private BroadcastReceiver mNetworkChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            model.isConnected.set(intent.getBooleanExtra("isConnected", false));
        }
    };


    public AddBook() {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (ean != null) {
            outState.putString(EAN_CONTENT, ean.getText().toString());
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        // Data binding
        ViewDataBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_book, container, false);
        model = new Model(getContext());
        ConnectivityManager cm =
                (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) {
            model.isConnected.set(activeNetwork.isConnected());
        } else {
            model.isConnected.set(false);
        }

        binding.setVariable(it.jaschke.alexandria.BR.model, model);
// bind reciver
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(mNetworkChangeReceiver,
                new IntentFilter("networkStateChanged"));

        rootView = binding.getRoot();


        ean = (EditText) rootView.findViewById(R.id.ean);

        ean.addTextChangedListener(new TextWatcher() {
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
                //catch isbn10 numbers
                if (ean.length() == 10 && !ean.startsWith("978")) {
                    ean = "978" + ean;
                }
                if (ean.length() < 13) {
                    model.clearFields();
                    return;
                }
                if (model.isConnected.get()) {
                    //Once we have an ISBN, request a book info
                    model.inProgress.set(true);
                    model.bookFound.set(false);
                    model.clearFields();
                    spiceManager.execute(new BookRequest(ean), "google.book", DurationInMillis.ONE_MINUTE, new BookRequestListener());
                }


            }
        });

        rootView.findViewById(R.id.scan_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This is the callback method that the system will invoke when your button is
                // clicked. You might do this by launching another app or by including the
                //functionality directly in this app.
                // Hint: Use a Try/Catch block to handle the Intent dispatch gracefully, if you
                // are using an external app.
                //when you're done, remove the toast below.
/*
                Context context = getActivity();
                CharSequence text = "This button should let you scan a book for its barcode!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
*/
                Intent intent = new Intent(getActivity(), BarcodescanActivity.class);
                startActivityForResult(intent, 1);

            }
        });

        rootView.findViewById(R.id.save_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model.saveBook();
            }
        });


        if (savedInstanceState != null) {
            ean.setText(savedInstanceState.getString(EAN_CONTENT));
            ean.setHint("");
        }

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            model.isbn.set(data.getStringExtra("bar_code"));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void saveBook(View view) {
        model.saveBook();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onStart() {
        spiceManager.start(getContext());
        super.onStart();
        getActivity().setTitle("Add book");

    }


    @Override
    public void onStop() {
        spiceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(mNetworkChangeReceiver);
        super.onDestroy();

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.deafault, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public final class BookRequestListener implements RequestListener<JsonElement> {

        @Override
        public void onRequestFailure(SpiceException spiceException) {

            model.inProgress.set(false);
            Toast.makeText(getActivity(),spiceException.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }

        /**
         * Parse the result
         *
         * @param result
         */
        @Override
        public void onRequestSuccess(final JsonElement result) {
            model.inProgress.set(false);
            final String ITEMS = "items";

            final String VOLUME_INFO = "volumeInfo";

            final String TITLE = "title";
            final String SUBTITLE = "subtitle";
            final String AUTHORS = "authors";
            final String DESC = "description";
            final String CATEGORIES = "categories";
            final String IMG_URL_PATH = "imageLinks";
            final String IMG_URL = "thumbnail";


            if (!result.getAsJsonObject().has(ITEMS)) {
                Toast.makeText(getContext(), R.string.book_not_found,Toast.LENGTH_LONG).show();
                model.bookFound.set(false);
                return;
            }
            model.clearFields();
            model.bookFound.set(true);
            JsonArray bookArray = result.getAsJsonObject().getAsJsonArray(ITEMS);


            JsonObject bookInfo = bookArray.get(0).getAsJsonObject().getAsJsonObject(VOLUME_INFO);

            model.title.set(bookInfo.get(TITLE).getAsString());
            String subtitle = "";
            if (bookInfo.has(SUBTITLE)) {
                model.subtitle.set(bookInfo.get(SUBTITLE).getAsString());
            }

            String desc = "";
            if (bookInfo.has(DESC)) {
                model.description.set(bookInfo.get(DESC).getAsString());
            }

            String imgUrl = "";
            if (bookInfo.has(IMG_URL_PATH) && bookInfo.getAsJsonObject(IMG_URL_PATH).has(IMG_URL)) {
                imgUrl = bookInfo.getAsJsonObject(IMG_URL_PATH).get(IMG_URL).getAsString();
                model.imageUrl.set(imgUrl);
            }
            //  writeBackBook(ean, title, subtitle, desc, imageUrl);
            model.authors.clear();
            if (bookInfo.has(AUTHORS)) {
                JsonArray authors = bookInfo.getAsJsonArray(AUTHORS);
                for (JsonElement elem : authors) {
                    model.authors.add(elem.getAsString());
                    model.notifyPropertyChanged(it.jaschke.alexandria.BR.authorsList);
                }
                //    writeBackAuthors(ean, bookInfo.getJSONArray(AUTHORS));
            }
            model.categories.clear();
            if (bookInfo.has(CATEGORIES)) {
                JsonArray categories = bookInfo.getAsJsonArray(CATEGORIES);

                for (JsonElement elem : categories) {
                    model.categories.add(elem.getAsString());
                    model.notifyPropertyChanged(it.jaschke.alexandria.BR.categoriesList);
                }

                //  writeBackCategories(ean,bookInfo.getJSONArray(CATEGORIES) );
            }


        }
    }
}
