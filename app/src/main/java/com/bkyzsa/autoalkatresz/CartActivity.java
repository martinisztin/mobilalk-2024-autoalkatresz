package com.bkyzsa.autoalkatresz;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CartActivity extends AppCompatActivity {
    private FrameLayout redCircle;
    private TextView countTextView;
    private TextView priceTextView;
    private int cartItems;

    private RecyclerView mRecyclerView;
    private ArrayList<ShopItem> mItemList;
    private CartAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mCarts;
    private FirebaseUser mUser;

    private static final String LOG_TAG = CartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mUser != null) {
            Log.d(LOG_TAG, "gg van user");
        }
        else {
            Log.d(LOG_TAG, "bg nincs user");
            finish();
        }

        mFirestore = FirebaseFirestore.getInstance();
        mCarts = mFirestore.collection("Carts");

        Intent intent = getIntent();
        cartItems = intent.getIntExtra("cartItems", 0);
        mItemList = new ArrayList<>();

        initializeCart();
    }
    public void initializeCart() {
        mCarts.document(mUser.getUid())
                .get()
                .addOnSuccessListener(task -> {

                    if(task.exists()) {
                        Cart cart = task.toObject(Cart.class);
                        if(cart != null && cart.getItems() != null) {
                            Log.d(LOG_TAG, "We got here");
                            mItemList = cart.getItems();

                            mRecyclerView = findViewById(R.id.recycler_view_cart_items);
                            // Set the Layout Manager.
                            mRecyclerView.setLayoutManager(new GridLayoutManager(
                                    this, 2));

                            mAdapter = new CartAdapter(mItemList, this);
                            mRecyclerView.setAdapter(mAdapter);

                            priceTextView = findViewById(R.id.text_view_total_price);

                            int total_price = 0;
                            for (ShopItem item: mItemList) {
                                total_price += Integer.parseInt(item.getPrice().split(" ")[0]);
                                Log.d(LOG_TAG, "atom fasz: " + item.getPrice().split(" ")[0]);
                            }
                            String price = "Összesen: " + total_price + " Ft";
                            priceTextView.setText(price);

                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_menu_cart, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.cart) {
            Log.d(LOG_TAG, "Cart clicked!");
            Intent resultIntent = new Intent();
            resultIntent.putExtra("cartItems", cartItems);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void pay(View view) {
        if(cartItems == 0) {
            return;
        }
        cartItems = 0;
        updateAlertIcon();

        mCarts.document(mUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Cart cart = documentSnapshot.toObject(Cart.class);
            if(cart != null) {
                cart.wipeCart();
                mItemList.clear();
                mAdapter.notifyDataSetChanged();
                int total_price = 0;
                String price = "Összesen: " + total_price + " Ft";
                priceTextView.setText(price);

                mCarts.document(mUser.getUid()).set(cart)
                        .addOnSuccessListener(aVoid -> {
                            // Cart updated successfully
                            Log.d(LOG_TAG, "Cart updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            // Error occurred while updating cart
                            Log.e(LOG_TAG, "Error updating cart", e);
                        });
            }
        });

        Toast.makeText(this, "Sikeres rendelés! Erről az információról kaptál értesítést is!",
                Toast.LENGTH_LONG).show();

        //TODO: értesítés
        NotificationHelper.showNotification(this, "Sikeres rendelés!", "Gratulálok, sikeresen megrendelted a kívánt tárgyakat!");
        Intent resultIntent = new Intent();
        resultIntent.putExtra("cartItems", cartItems);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        updateAlertIcon();

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        if (0 < cartItems) {
            Log.d(LOG_TAG, "cart: " + cartItems);
            //Log.d(LOG_TAG, "countTextView: " + countTextView.getText().toString());
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }
        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);
    }
    public void updateAlertIcon(ShopItem currentItem, int position) {
        cartItems = (cartItems - 1);
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }
        //TODO update cart
        mCarts.document(mUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Cart cart = documentSnapshot.toObject(Cart.class);
            if(cart != null) {
                cart.removeFromCart(position);
                Log.d(LOG_TAG, "cart: " + cart.getItems().size());
                mItemList.remove(currentItem);
                mAdapter.notifyDataSetChanged();

                int total_price = 0;
                for (ShopItem item: mItemList) {
                    total_price += Integer.parseInt(item.getPrice().split(" ")[0]);
                    Log.d(LOG_TAG, "atom fasz: " + item.getPrice().split(" ")[0]);
                }
                String price = "Összesen: " + total_price + " Ft";
                priceTextView.setText(price);

                mCarts.document(mUser.getUid()).set(cart)
                        .addOnSuccessListener(aVoid -> {
                            // Cart updated successfully
                            Log.d(LOG_TAG, "Cart updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            // Error occurred while updating cart
                            Log.e(LOG_TAG, "Error updating cart", e);
                        });
            }
        });
        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);
    }
}