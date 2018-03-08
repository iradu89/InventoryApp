package com.example.rdu.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.rdu.inventoryapp.data.ProductContract.ProductEntry;
import com.example.rdu.inventoryapp.data.ProductDbHelper;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static int PRODUCT_LOADER = 0;

    ProductCursorAdapter mCursorAdapter;

    //database helper
    private ProductDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FAB which opens the EditorActivity
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView productListView = findViewById(R.id.list);

        //find and set empty view on the listview so that it shows when the list has 0 items
        View emptyView = findViewById(R.id.empty_view);
        productListView.setEmptyView(emptyView);

        //instantiate the subclass of SQLitehelper and pass the current activity context
        mDbHelper = new ProductDbHelper(this);

        //Setup an adapter to create a list item for each row of the product data in the cursor
        mCursorAdapter = new ProductCursorAdapter(this, null);
        productListView.setAdapter(mCursorAdapter);

        //setup the item click listener on the list
        productListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                //create new intent to go to EditorActivity
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                Uri currentProductUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, id);

                //set the URI on the data field of the intent
                intent.setData(currentProductUri);

                startActivity(intent);
            }
        });

        //start the loader
        getLoaderManager().initLoader(PRODUCT_LOADER, null, this);
    }


    private void insertProduct() {

        ContentValues values = new ContentValues();

        Uri imgUri = Uri.parse("android.resource://com.example.rdu.inventoryapp" + "/" + R.drawable.tool01);
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imgUri.toString());
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, "TOOL 01");
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, "35");
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, "2");

        Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the options menu
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertProduct();
                return true;
            case R.id.action_delete_all_entries:
                deleteAllProducts();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //helper method to delete all products in the database
    private void deleteAllProducts() {
        int rowsDeleted = getContentResolver().delete(ProductEntry.CONTENT_URI, null, null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        //this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,
                ProductEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //Update with this new cursor containing updated data
        mCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mCursorAdapter.swapCursor(null);
    }
}
