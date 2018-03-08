package com.example.rdu.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.rdu.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Rdu on 22.12.2017.
 */

public class ProductDbHelper extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "store.db";
    private final static int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String SQL_CREATE_PRODUCTS_TABLE = "CREATE TABLE " + ProductEntry.TABLE_NAME + " ("
                + ProductEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + ProductEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_PRICE + " STRING NOT NULL,"
                + ProductEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER DEFAULT 0,"
                + ProductEntry.COLUMN_PRODUCT_IMAGE + " TEXT" + ");";

        //execute
        sqLiteDatabase.execSQL(SQL_CREATE_PRODUCTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
