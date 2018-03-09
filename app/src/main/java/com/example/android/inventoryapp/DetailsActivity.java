package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class DetailsActivity extends AppCompatActivity {

   private int ProductQuantity;
   private String supplierMail;
   private String supplierPhone;
   private TextView productNameText;
   private TextView productQuantityText;
   private TextView productPriceText;
   private TextView supplierNameText;
   private TextView supplierMailText;
   private TextView supplierPhoneText;
   private Button addOne;
   private Button takeOne;
   private Button deleteProduct;
   private Button phoneCall;
   private Button sendEmail;
   private Cursor productCursor;
   private Uri productUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        productNameText = findViewById(R.id.details_txt_product_name);
        productQuantityText = findViewById(R.id.details_txt_product_quantity);
        productPriceText = findViewById(R.id.details_txt_product_price);
        supplierNameText = findViewById(R.id.details_txt_supplier_name);
        supplierMailText = findViewById(R.id.details_txt_supplier_mail);
        supplierPhoneText = findViewById(R.id.details_txt_supplier_phone);
        addOne = findViewById(R.id.details_btn_addone);
        takeOne = findViewById(R.id.details_btn_takeone);
        deleteProduct = findViewById(R.id.details_btn_delete);
        phoneCall = findViewById(R.id.details_btn_call);
        sendEmail = findViewById(R.id.details_btn_mail);

        final Uri uri = getIntent().getData();
        productUri = uri;
        updateViewsWithData(uri);

        // ask the user to confirm the delete operation
        deleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                getContentResolver().delete(uri, null, null);
                                finish();
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
            }
        });

        // reduces the product quantity and saves the new value in the database
        takeOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductQuantity --;
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProductEntry.COLUMN_QUANTITY, ProductQuantity);
                try{
                    getContentResolver().update(uri, contentValues, null, null);
                    productQuantityText.setText(String.valueOf(ProductQuantity));
                }catch (IllegalArgumentException ex){
                    ProductQuantity++;
                    Toast.makeText(DetailsActivity.this, ex.getMessage() , Toast.LENGTH_SHORT).show();
                }

            }
        });

        // increases the product quantity and saves the new value in the database
        addOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ProductQuantity ++;
                ContentValues contentValues = new ContentValues();
                contentValues.put(ProductEntry.COLUMN_QUANTITY, ProductQuantity);
                try{
                    getContentResolver().update(uri, contentValues, null, null);
                    productQuantityText.setText(String.valueOf(ProductQuantity));
                }catch (IllegalArgumentException ex){
                    Toast.makeText(DetailsActivity.this, ex.getMessage() , Toast.LENGTH_SHORT).show();
                }
            }
        });

        // makes a phone call the the provider using the Phone number stored in the database
        phoneCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhoneCall(supplierPhone);
            }
        });

        // sends Email to the provider using the Email stored in the database
        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String arr[] = {supplierMail};
                composeEmail(arr);
            }
        });
    }

    /**
     * gets the product details from the data base
     * @param uri the product Uri
     * @return Cursor containing the product details
     */
    private Cursor query(Uri uri){
        String[] projection = {ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
               Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, projection,
                    null, null, null);
        } catch (Exception ex) {
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return cursor;
    }

    /**
     * makes the database query and updates the views with the product details
     * @param uri the product Uri
     */
    private void updateViewsWithData(Uri uri){
        productCursor = query(uri);
        productCursor.moveToFirst();
        productNameText.setText(productCursor.getString(productCursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
        ProductQuantity = productCursor.getInt(productCursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY));
        productQuantityText.setText(String.valueOf(ProductQuantity));
        productPriceText.setText(productCursor.getString(productCursor.getColumnIndex(ProductEntry.COLUMN_PRICE)));
        supplierNameText.setText(productCursor.getString(productCursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME)));
        supplierMail = productCursor.getString(productCursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL));
        supplierMailText.setText(supplierMail);
        supplierPhone = productCursor.getString(productCursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER));
        supplierPhoneText.setText(supplierPhone);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateViewsWithData(productUri);
    }

    /**
     * display alert dialog asking the user to confirm operation
     * @param discardButtonClickListener
     */
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_delete));
        builder.setPositiveButton(getString(R.string.yes), discardButtonClickListener);
        builder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    /**
     * starts intent to make phone call
     * @param phone the Phone number to be called
     */
    private void makePhoneCall(String phone){
        String phoneNumber = "tel:" + phone;
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phoneNumber));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    /**
     * starts intent to send email
     * @param addresses
     */
    public void composeEmail(String[] addresses) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_EMAIL, addresses);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.edit, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.menu_item_add){
            Intent intent = new Intent(this, EditorActivity.class);
           intent.setData(productUri);
            startActivity(intent);
        }else
            onBackPressed();
        return true;
    }
}
