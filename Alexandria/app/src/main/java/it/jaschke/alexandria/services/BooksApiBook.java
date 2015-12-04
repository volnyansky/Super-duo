package it.jaschke.alexandria.services;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

/**
 * Created by Stas on 02.11.15.
 */
public class BooksApiBook {
    String title;
    String subtitle;
    public static class Converter implements JsonDeserializer<BooksApiBook> {

        @Override
        public BooksApiBook deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return null;
        }
    }
}
