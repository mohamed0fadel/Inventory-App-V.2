package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.data.InventoryContract.ProductEntry;

import java.net.URL;

/**
 * Created by MohamedFadel on 2/8/2018.
 */

public class InventoryProvider extends ContentProvider {

    InventoryDbHelper inventoryDbHelper;

    @Override
    public boolean onCreate() {
        inventoryDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    private static final int PRODUCTS = 1;
    private static final int PRODUCT_ID = 2;
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT, PRODUCTS);
        uriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCT  + "/#", PRODUCT_ID);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        int match = uriMatcher.match(uri);
        SQLiteDatabase sqLiteDatabase = inventoryDbHelper.getReadableDatabase();
        Cursor cursor;
        switch (match){
            case PRODUCTS:
                cursor = sqLiteDatabase.query(ProductEntry.TABLE_NAME, selectionArgs, selection,
                        null,null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
               cursor = sqLiteDatabase.query(ProductEntry.TABLE_NAME, projection,
                       selection, selectionArgs, null, null, null);
                break;
                default:
                    throw new IllegalArgumentException("Cannot query unknown query " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        int match = uriMatcher.match(uri);
        switch (match){
            case PRODUCTS:
                return insertProduct(uri, contentValues);
                default:
                    throw new IllegalArgumentException("Cannot query unknown query " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues){

        String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if(name == null || name.length() == 0)
            throw new IllegalArgumentException("Product requires a name");

        Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_QUANTITY);
        if (quantity == null || quantity < 1)
            throw new IllegalArgumentException("cant add product with quantity less than or equal to Zero");

        Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRICE);
        if (price == null || price < 1)
            throw new IllegalArgumentException("Price cannot be less than or equal to Zero");

        String supplier = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
        if (supplier == null || supplier.length() == 0)
            throw new IllegalArgumentException("Supplier requires name");

        String supplierMail = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
        if (supplierMail == null || supplierMail.length() == 0)
            throw new IllegalArgumentException("Supplier requires Email");

        String supplierPhone = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
        if (supplierPhone == null || supplierPhone.length() == 0)
            throw new IllegalArgumentException("Supplier requires Phone Number");

        SQLiteDatabase sqLiteDatabase = inventoryDbHelper.getWritableDatabase();
        long resultID = sqLiteDatabase.insert(ProductEntry.TABLE_NAME, null, contentValues);
        if (resultID == -1){
            Log.e(getContext().toString(), "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, resultID);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        int match = uriMatcher.match(uri);
        int result;
        switch (match){
            case PRODUCTS:
                result = updateProduct(uri, contentValues, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                result = updateProduct(uri, contentValues, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown query " + uri);
        }
        return result;
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs){
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if(name == null || name.length() == 0)
                throw new IllegalArgumentException("Product requires a name");
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(ProductEntry.COLUMN_QUANTITY);
            if (quantity == null || quantity < 0)
                throw new IllegalArgumentException("cant update product with quantity less than or equal to Zero");
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(ProductEntry.COLUMN_PRICE);
            if (price == null || price < 1)
                throw new IllegalArgumentException("Price cannot be less than or equal to Zero");
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_SUPPLIER_NAME)) {
            String supplier = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_NAME);
            if (supplier == null || supplier.length() == 0)
                throw new IllegalArgumentException("Supplier requires name");
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_SUPPLIER_EMAIL)) {
            String supplierMail = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_EMAIL);
            if (supplierMail == null || supplierMail.length() == 0)
                throw new IllegalArgumentException("Supplier requires Email");
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER)) {
            String supplierPhone = contentValues.getAsString(ProductEntry.COLUMN_SUPPLIER_PHONE_NUMBER);
            if (supplierPhone == null || supplierPhone.length() == 0)
                throw new IllegalArgumentException("Supplier requires Phone Number");
        }

        if (contentValues.size() == 0)
            return 0;

        SQLiteDatabase sqLiteDatabase = inventoryDbHelper.getWritableDatabase();
        int rowsUpdated = sqLiteDatabase.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase database = inventoryDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        final int match = uriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
