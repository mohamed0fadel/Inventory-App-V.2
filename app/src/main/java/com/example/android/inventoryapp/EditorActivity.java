package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

public class EditorActivity extends AppCompatActivity {

    private Uri productUri = null;
    private EditText productNameEdit;
    private EditText productQuantityEdit;
    private EditText productPriceEdit;
    private EditText supplierNameEdit;
    private EditText supplierMailEdit;
    private EditText supplierPhoneEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        productNameEdit = findViewById(R.id.edt_product_name);
        productQuantityEdit = findViewById(R.id.edt_product_quantity);
        productPriceEdit = findViewById(R.id.edt_product_price);
        supplierNameEdit = findViewById(R.id.edt_supplier_name);
        supplierMailEdit = findViewById(R.id.edt_supplier_mail);
        supplierPhoneEdit = findViewById(R.id.edt_supplier_phone);

        try {
            if(getIntent().getData() != null){
                productUri = getIntent().getData();
                setTitle(R.string.update);
                getProductDetails();
            }
        }catch (NullPointerException ex){
            Log.e("EditorActivity", "productUri is Empty");
            setTitle(R.string.add);
        }catch (CursorIndexOutOfBoundsException e){
            Log.e("EditorActivity", e.getMessage());
            setTitle(R.string.add);
        }
    }

    /**
     * takes the values fro the EditText views and put them in a ContentValues object
     * @return ContentValues containing product details
     */
    private ContentValues getInputData(){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ProductEntry.COLUMN_PRODUCT_NAME, productNameEdit.getText().toString().trim());
        contentValues.put(ProductEntry.COLUMN_QUANTITY, productQuantityEdit.getText().toString().trim());
        contentValues.put(ProductEntry.COLUMN_PRICE, productPriceEdit.getText().toString().trim());
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_NAME, supplierNameEdit.getText().toString().trim());
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_EMAIL, supplierMailEdit.getText().toString().trim());
        contentValues.put(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER, supplierPhoneEdit.getText().toString().trim());
        return contentValues;
    }

    /**
     * clears the EditText views content
     */
    private void clearInputFields(){
        productNameEdit.setText("");
        productQuantityEdit.setText("");
        productPriceEdit.setText("");
        supplierNameEdit.setText("");
        supplierMailEdit.setText("");
        supplierPhoneEdit.setText("");
    }

    /**
     * checks if there is at least one EditText containing data
     * @return boolean vales true when all the EditText views are empty and true if there is no data
     */
    private boolean hasData(){
        if (! productNameEdit.getText().toString().trim().equals(""))
            return true;
        else if (! productQuantityEdit.getText().toString().trim().equals(""))
            return true;
        else if (! productPriceEdit.getText().toString().trim().equals(""))
            return true;
        else if (! supplierNameEdit.getText().toString().trim().equals(""))
            return true;
        else if (! supplierMailEdit.getText().toString().trim().equals(""))
            return true;
        else if (! supplierPhoneEdit.getText().toString().trim().equals(""))
            return  true;
        else
            return false;
    }

    /**
     * gets the current product data and displays it in the views
     */
    private void getProductDetails() {
        String[] projection = {ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_QUANTITY,
                ProductEntry.COLUMN_PRICE,
                ProductEntry.COLUMN_SUPPLIER_NAME,
                ProductEntry.COLUMN_SUPPLIER_EMAIL,
                ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER};
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(ProductEntry.CONTENT_URI, projection, null, null, null);
        } catch (Exception ex) {
            Log.e("EditorActivity", ex.getMessage());
        }
        cursor.moveToFirst();
        productNameEdit.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME)));
        productQuantityEdit.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_QUANTITY))));
        productPriceEdit.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex(ProductEntry.COLUMN_PRICE))));
        supplierNameEdit.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_NAME)));
        supplierMailEdit.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_EMAIL)));
        supplierPhoneEdit.setText(cursor.getString(cursor.getColumnIndex(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER)));
    }

    /**
     * updates the product with the new values
     */
    private void updateProduct(){
        try {
            int result = getContentResolver().update(productUri, getInputData(), null, null);
            clearInputFields();
            Toast.makeText(this, "" + result + " Item Updated", Toast.LENGTH_SHORT).show();
        }catch (IllegalArgumentException ex){
            Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.editor_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_item_add:
                //check if the activity is opened to update an existing product
                if(productUri != null){
                    updateProduct();
                }else {
                    // if the activity is opened to add new product
                    try{
                        getContentResolver().insert(ProductEntry.CONTENT_URI, getInputData());
                        clearInputFields();
                        Toast.makeText(this, getString(R.string.successful), Toast.LENGTH_SHORT).show();
                    }catch (IllegalArgumentException ex){
                        Toast.makeText(this, ex.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                // when the user presses the up arrow
                if (hasData()){
                    DialogInterface.OnClickListener discardButtonClickListener =
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    finish();
                                }
                            };
                    showUnsavedChangesDialog(discardButtonClickListener);
                }else
                    finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
       /* if(productUri != null){
            Intent intent = new Intent(EditorActivity.this, MainActivity.class);
            startActivity(intent);
        }*/
        if (hasData()){
            DialogInterface.OnClickListener discardButtonClickListener =
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    };
            showUnsavedChangesDialog(discardButtonClickListener);
        }else
            finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.confirm_leave));
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

}
