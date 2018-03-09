package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

/**
 * Created by MohamedFadel on 2/8/2018.
 */

public class ProductCursorAdapter extends CursorAdapter {
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        TextView productName = view.findViewById(R.id.list_product_name);
        TextView productQuatity = view.findViewById(R.id.list_product_quantity);
        TextView productPrice = view.findViewById(R.id.list_product_price);

        int price = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE));
        int quantity =cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        // store the item position because the cursor will move when displaying new item
        final int position = cursor.getPosition();

        productName.setText(cursor.getString(cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME)));
        productQuatity.setText(context.getString(R.string.quantity) + String.valueOf(quantity));
        productPrice.setText(context.getString(R.string.price) + String.valueOf(price));

        Button button = view.findViewById(R.id.list_btn_sale);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(position);
                int quantity = cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
                int productID = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
                quantity--;
                try {
                    // update the quantity
                    update(productID, quantity, context);
                }catch (IllegalArgumentException ex){
                    // return the the value zero if the current value will go below zero
                    quantity++;
                    Toast.makeText(context, ex.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        // list item action
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // move the cursor to the current product position
                cursor.moveToPosition(position);
                int id = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
                Uri productUri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, "/" + id);
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.setData(productUri);
                context.startActivity(intent);
            }
        });
    }


    private void update(int id, int quantity, Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_QUANTITY, quantity);
        Uri uri = Uri.withAppendedPath(ProductEntry.CONTENT_URI, "/" + id);
        context.getContentResolver().update(uri, contentValues, null, null);
    }


}
