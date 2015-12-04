package it.jaschke.alexandria.services;

import java.util.List;

/**
 * Created by Stas on 02.11.15.
 */
public class BooksApiResponse {
    String king;
    int totalItems;
    List<BooksApiBook> items;

    public List<BooksApiBook> getItems() {
        return items;
    }

    public void setItems(List<BooksApiBook> items) {
        this.items = items;
    }

    public String getKing() {
        return king;
    }

    public void setKing(String king) {
        this.king = king;
    }

    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
}
