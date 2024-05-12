package com.bkyzsa.autoalkatresz;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private Context mContext;
    private List<ShopItem> cartItemList;
    private int lastPosition = -1;


    public CartAdapter(List<ShopItem> cartItemList, Context context) {
        this.cartItemList = cartItemList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.cart_list, parent, false);
        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        ShopItem cartItem = cartItemList.get(position);

        holder.bindTo(cartItem, position);

        if(holder.getAdapterPosition() > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.slide_in_row);
            holder.itemView.startAnimation(animation);
            lastPosition = holder.getAdapterPosition();
        }
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public class CartViewHolder extends RecyclerView.ViewHolder {
        TextView itemNameTextView, itemPriceTextView;
        ImageView itemImageView;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            itemNameTextView = itemView.findViewById(R.id.itemTitle);
            itemPriceTextView = itemView.findViewById(R.id.price);
            itemImageView = itemView.findViewById(R.id.itemImage);
        }

        void bindTo(ShopItem cartItem, int position) {
            itemNameTextView.setText(cartItem.getName());
            itemPriceTextView.setText(cartItem.getPrice());
            Glide.with(mContext).load(cartItem.getImageResource()).into(itemImageView);

            itemView.findViewById(R.id.remove_from_cart).setOnClickListener(view -> ((CartActivity)mContext).updateAlertIcon(cartItem, position));
        }
    }
}