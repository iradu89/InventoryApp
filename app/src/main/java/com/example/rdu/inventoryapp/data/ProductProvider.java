package com.example.rdu.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.rdu.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Rdu on 27/12/2017.
 */

public class ProductProvider extends ContentProvider {

    public static final String LOG_TAG = ProductProvider.class.getSimpleName();

    //database helper object
    private ProductDbHelper mDbHelper;

    private final static int PRODUCTS = 100;
    private final static int PRODUCT_ID = 101;
    private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS, PRODUCTS);
        sUriMatcher.addURI(ProductContract.CONTENT_AUTHORITY, ProductContract.PATH_PRODUCTS + "/#", PRODUCT_ID);
    }

    //Innit
    @Override
    public boolean onCreate() {
        mDbHelper = new ProductDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {

        //get readable database
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        Cursor cursor;

        //figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};

                cursor = db.query(ProductEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        //set notification uri on the Cursor
        //so we know what content URI the Cursor was created for.
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {

        String productName = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
        if (productName == null || productName.isEmpty()) {
            throw new IllegalArgumentException("Product requires a name");
        }

        //price cannot be null or lower/equal to 0
        Integer productPrice = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
        if (productPrice == null || productPrice <= 0) {
            throw new IllegalArgumentException("Product requires a valid price");
        }

        //quantity cannot be null, but can be 0
        Integer productQuantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        if (productQuantity == null || productQuantity < 0) {
            throw new IllegalArgumentException("Product requires a valid quantity");
        }

        String productImage = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
        if (productImage == null) {
            throw new IllegalArgumentException("Product requires an image");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        // Insert the new product with the given values
        long id = database.insert(ProductEntry.TABLE_NAME, null, contentValues);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        //return new uri with the ID appended at the end
        return ContentUris.withAppendedId(uri, id);
    }


    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //check if the product name is not null
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_NAME)) {
            String productName = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_NAME);
            if (productName == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        //check if the price is valid
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_PRICE)) {
            Integer productPrice = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_PRICE);
            if (productPrice == null || productPrice <= 0) {
                throw new IllegalArgumentException("Product requires a valid price");
            }
        }

        //check if the quantity is valid
        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer productQuantity = contentValues.getAsInteger(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            if (productQuantity == null || productQuantity < 0) {
                throw new IllegalArgumentException("Product requires a valid quantity");
            }
        }

        if (contentValues.containsKey(ProductEntry.COLUMN_PRODUCT_IMAGE)) {
            String productImage = contentValues.getAsString(ProductEntry.COLUMN_PRODUCT_IMAGE);
            if (productImage == null) {
                throw new IllegalArgumentException("Product requires an image");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (contentValues.size() == 0) {
            return 0;
        }

        //get writable database
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //return the number of database rows affected by the update statement
        //return db.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        //update the database and get the number of rows affected
        int rowsUpdated = db.update(ProductEntry.TABLE_NAME, contentValues, selection, selectionArgs);

        //if 1 or more rows were updated, notify all listeners that the data has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;

    }


    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //get writable db
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        //track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case PRODUCT_ID:
                selection = ProductEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = db.delete(ProductEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }

        //if 1 or more rows were deleted notify all listeners that the data was changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    //returns the MIME type of data for content URI
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return ProductEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return ProductEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri + " with match " + match);
        }
    }
}
