package com.bkyzsa.autoalkatresz;

import java.util.ArrayList;

public class Cart {
    private String id;
    private ArrayList<ShopItem> items;

    public Cart() {}

    public Cart(String id, ArrayList<ShopItem> items) {
        this.id = id;
        this.items = items;
    }


    public ArrayList<ShopItem> getItems() {
        return items;
    }

    public void addToCart(ShopItem item) {
        items.add(item);
    }
    public void removeFromCart(int position) {
        items.remove(position);
    }

    public void wipeCart() {
        items.clear();
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
}
