package com.example.rdu.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Rdu on 22.12.2017.
 */

public final class ProductContract {

    private ProductContract() {
    }

    //the entire content provider name as a content authority
    public final static String CONTENT_AUTHORITY = "com.example.rdu.inventoryapp";

    public final static Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //possible path for the content URI
    public final static String PATH_PRODUCTS = "products";

    public final static class ProductEntry implements BaseColumns {

        //the content URI to access the product data in the provider
        public final static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        //the MIME type of the CONTENT_URI for a list of products
        public final static String CONTENT_LIST_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //the MIME type of the CONTENT_URI for a single product
        public final static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //database table name
        public final static String TABLE_NAME = "products";

        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_PRODUCT_NAME = "product";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_IMAGE = "image";
    }
}
