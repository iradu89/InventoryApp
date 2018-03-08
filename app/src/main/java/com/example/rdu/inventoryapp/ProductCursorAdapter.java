package com.example.rdu.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rdu.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Rdu on 28/12/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {

    //constructor
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    //makes a new blank list item view. No data is set or bound to the views yet.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        //inflate a list item view using the layout list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    //binds the product data to the given list item layout
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        //find individual views
        TextView productNameTextView = view.findViewById(R.id.list_name);
        TextView productPriceTextView = view.findViewById(R.id.list_price);
        TextView productQuantityTextView = view.findViewById(R.id.list_quantity);
        ImageView productImageView = view.findViewById(R.id.list_image);
        Button saleButton = view.findViewById(R.id.list_sale_button);

        //find the columns of attributes we're interested in
        int productId = cursor.getColumnIndex(ProductEntry._ID);
        int productNameColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_NAME);
        int productPriceColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_PRICE);
        int productQuantityColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int productImageColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry.COLUMN_PRODUCT_IMAGE);

        //read the product attributes from the cursor of the current product
        String productName = cursor.getString(productNameColumnIndex);
        int productPrice = cursor.getInt(productPriceColumnIndex);
        final int productQuantity = cursor.getInt(productQuantityColumnIndex);
        String image = cursor.getString(productImageColumnIndex);
        final int id = cursor.getInt(productId);

        //update textview with attributes for the current product
        productNameTextView.setText(productName);
        productPriceTextView.setText(String.valueOf(productPrice));
        productQuantityTextView.setText(String.valueOf(productQuantity));
        productImageView.setImageURI(Uri.parse(image));

        //sale button
        //onclicklistener on sale button to update quantity if pressed
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productQuantity > 0) {
                    int qt = productQuantity - 1;
                    Uri qtUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, qt);
                    context.getContentResolver().update(qtUri, values, null, null);
                    Toast.makeText(context, R.string.sale_button_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, R.string.sale_button_fail, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
