package com.bkyzsa.autoalkatresz;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import java.util.ArrayList;

public class ShopActivity extends AppCompatActivity {
    private static final String LOG_TAG = ShopActivity.class.getName();
    private FirebaseUser mUser;

    private FrameLayout redCircle;
    private TextView countTextView;
    private int cartItems = 0;

    private RecyclerView mRecyclerView;
    private ArrayList<ShopItem> mItemList;
    private ShopItemAdapter mAdapter;

    private FirebaseFirestore mFirestore;
    private CollectionReference mItems, mCarts;
    private Cart ownCart;

    private boolean viewRow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);

        mUser = FirebaseAuth.getInstance().getCurrentUser();

        if(mUser != null) {
            Log.d(LOG_TAG, "gg van user");
        }
        else {
            Log.d(LOG_TAG, "bg nincs user");
            finish();
        }

        // recycle view
        mRecyclerView = findViewById(R.id.recyclerView);
        // Set the Layout Manager.

        //TODO: remove 1 grid or 2 idk?
        mRecyclerView.setLayoutManager(new GridLayoutManager(
                this, 1));
        // Initialize the ArrayList that will contain the data.
        mItemList = new ArrayList<>();
        // Initialize the adapter and set it to the RecyclerView.
        mAdapter = new ShopItemAdapter(this, mItemList);
        mRecyclerView.setAdapter(mAdapter);

        mFirestore = FirebaseFirestore.getInstance();
        mItems = mFirestore.collection("Items");
        mCarts = mFirestore.collection("Carts");

        // valami
        queryData();
        queryCart();
    }

    private void queryCart() {
        mCarts.document(mUser.getUid())
                .get()
                .addOnSuccessListener(task -> {
                    if(!task.exists()) {
                        mCarts.document(mUser.getUid()).set(new Cart(mUser.getUid(), new ArrayList<>()));
                    }
                    else {
                        ownCart = task.toObject(Cart.class);
                        cartItems = ownCart.getItems().size();
                        updateAlertIcon();
                    }
                });
    }

    private void initializeData() {
        // Get the resources from the XML file.
        String[] itemsList = getResources()
                .getStringArray(R.array.shopping_item_names);
        String[] itemsInfo = getResources()
                .getStringArray(R.array.shopping_item_desc);
        String[] itemsPrice = getResources()
                .getStringArray(R.array.shopping_item_price);
        TypedArray itemsImageResources =
                getResources().obtainTypedArray(R.array.shopping_item_images);
        TypedArray itemRate = getResources().obtainTypedArray(R.array.shopping_item_rates);

        // Create the ArrayList of Sports objects with the titles and
        // information about each sport.
        for (int i = 0; i < itemsList.length; i++) {
            mItems.add(new ShopItem(
                    itemsList[i],
                    itemsInfo[i],
                    itemsPrice[i],
                    itemRate.getFloat(i, 0),
                    itemsImageResources.getResourceId(i, 0)));
        }

        // Recycle the typed array.
        itemsImageResources.recycle();
    }

    private void queryData() {
        mItemList.clear();
        mItems.orderBy("name").limit(100).get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                ShopItem item = document.toObject(ShopItem.class);
                mItemList.add(item);
            }

            if (mItemList.size() == 0) {
                initializeData();
                queryData();
            }

            // Notify the adapter of the change.
            mAdapter.notifyDataSetChanged();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.shop_menu, menu);
        MenuItem menuItem = menu.findItem(R.id.search_bar);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                Log.d(LOG_TAG, s);
                mAdapter.getFilter().filter(s);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.log_out_button) {
            Log.d(LOG_TAG, "Logout clicked!");
            Toast.makeText(this, "Sikeres kijelentkezés!", Toast.LENGTH_LONG).show();
            if(FirebaseAuth.getInstance().getCurrentUser().isAnonymous()) {
                mCarts.document(FirebaseAuth.getInstance().getCurrentUser().getUid()).delete();
            }
            FirebaseAuth.getInstance().signOut();
            finish();
            return true;
        } else if (item.getItemId() == R.id.cart) {
            Log.d(LOG_TAG, "Cart clicked!");
            Toast.makeText(this, "A visszalépéshez kattints a kosár ikonra.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, CartActivity.class);
            intent.putExtra("cartItems", cartItems);
            startActivityForResult(intent, 1);

            return true;
        } else if (item.getItemId() == R.id.view_selector) {
            if (viewRow) {
                changeSpanCount(item, R.drawable.ic_view_grid, 1);
            } else {
                changeSpanCount(item, R.drawable.ic_view_row, 2);
            }
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                cartItems = data.getIntExtra("cartItems", 0);
                updateAlertIcon();
                // Handle the received data from the child activity
            }
        }
    }

    private void changeSpanCount(MenuItem item, int drawableId, int spanCount) {
        viewRow = !viewRow;
        item.setIcon(drawableId);
        GridLayoutManager layoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
        layoutManager.setSpanCount(spanCount);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        final MenuItem alertMenuItem = menu.findItem(R.id.cart);
        FrameLayout rootView = (FrameLayout) alertMenuItem.getActionView();

        redCircle = (FrameLayout) rootView.findViewById(R.id.view_alert_red_circle);
        countTextView = (TextView) rootView.findViewById(R.id.view_alert_count_textview);

        rootView.setOnClickListener(v -> onOptionsItemSelected(alertMenuItem));
        return super.onPrepareOptionsMenu(menu);
    }

    public void updateAlertIcon() {
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }
        redCircle.setVisibility((cartItems > 0) ? VISIBLE : GONE);
    }

    public void updateAlertIcon(ShopItem currentItem) {
        cartItems = (cartItems + 1);
        if (0 < cartItems) {
            countTextView.setText(String.valueOf(cartItems));
        } else {
            countTextView.setText("");
        }
        //TODO update cart
        mCarts.document(mUser.getUid()).get().addOnSuccessListener(documentSnapshot -> {
            Cart cart = documentSnapshot.toObject(Cart.class);
            if(cart != null) {
                cart.addToCart(currentItem);

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
        Animation anim = AnimationUtils.loadAnimation(this, R.anim.animation);
        countTextView.startAnimation(anim);
        redCircle.startAnimation(anim);
    }
}