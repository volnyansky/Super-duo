package it.jaschke.alexandria.services;

import com.google.gson.JsonElement;
import com.octo.android.robospice.request.retrofit.RetrofitSpiceRequest;

import retrofit.client.Response;

/**
 * Created by Stas on 18.10.15.
 */
public class BookRequest extends RetrofitSpiceRequest<JsonElement, BookServiceSpice.GoogleBooks> {

    private String isbn;


    public BookRequest(String isbn) {
        super(JsonElement.class, BookServiceSpice.GoogleBooks.class);
        this.isbn=isbn;
    }

    @Override
    public JsonElement loadDataFromNetwork() {
//        Ln.d("Call web service ");
        return getService().getBook("isbn:"+isbn);
    }
}