package it.jaschke.alexandria.services;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.octo.android.robospice.retrofit.RetrofitGsonSpiceService;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Stas on 18.10.15.
 */
public class BookServiceSpice extends RetrofitGsonSpiceService {

    private final static String BASE_URL = "https://www.googleapis.com";

    @Override
    public void onCreate() {
        super.onCreate();
        addRetrofitInterface(GoogleBooks.class);

    }

    @Override
    protected String getServerUrl() {
        return BASE_URL;
    }
    public static interface GoogleBooks{
        @GET("/books/v1/volumes")
        public JsonElement getBook(@Query("q") String query);

    }

    @Override
    protected RestAdapter.Builder createRestAdapterBuilder() {
        GsonBuilder gson = new GsonBuilder();
        return new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL).setConverter(new GsonConverter(gson.create()));


    }
}
