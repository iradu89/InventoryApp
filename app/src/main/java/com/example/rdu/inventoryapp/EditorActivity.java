package com.example.rdu.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.rdu.inventoryapp.data.ProductContract.ProductEntry;

/**
 * Created by Rdu on 21.12.2017.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    //identifier for the product loader
    private final static int EXISTING_PRODUCT_LOADER = 0;

    //content URI for the existing product
    private Uri mCurrentProductUri;

    //variable names
    private EditText mProdName;
    private EditText mProdPrice;
    private EditText mProdQuantity;
    private ImageView mProdImage;
    private Uri mImageUri;
    private Button mImageUploadButton;
    private Button mIncrementButton;
    private Button mDecrementButton;
    private Button mSupplierOrderButton;
    private boolean mIsValid;
    private boolean mProductHasChanged = false;


    //listens for any user touches on the view, implying they are modifying the view
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //figure out if the intent is adding a new product or editing an existing one
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        //if we are adding a new product
        if (mCurrentProductUri == null) {
            setTitle(R.string.editor_activity_add_product);

            //invalidate the options menu so the Delete menu becomes hidden
            invalidateOptionsMenu();
        } else {
            setTitle(R.string.editor_activity_edit_product);

            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        //Find all the views to read the user input from
        mProdName = findViewById(R.id.product_name);
        mProdPrice = findViewById(R.id.product_price);
        mProdQuantity = findViewById(R.id.product_quantity);
        mProdImage = findViewById(R.id.product_image);
        mImageUploadButton = findViewById(R.id.upload_image_button);
        mIncrementButton = findViewById(R.id.plus);
        mDecrementButton = findViewById(R.id.minus);
        mSupplierOrderButton = findViewById(R.id.order_button);

        //Setup OnTouchListeners on all the input fields so we can see if the user touched
        mProdName.setOnTouchListener(mTouchListener);
        mProdPrice.setOnTouchListener(mTouchListener);
        mProdQuantity.setOnTouchListener(mTouchListener);
        mProdImage.setOnTouchListener(mTouchListener);
        mImageUploadButton.setOnTouchListener(mTouchListener);
        mIncrementButton.setOnTouchListener(mTouchListener);
        mDecrementButton.setOnTouchListener(mTouchListener);

        mImageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                trySelector();
            }
        });

        mIncrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qt = Integer.parseInt(mProdQuantity.getText().toString().trim());
                if (qt == 999) {
                    Toast.makeText(getApplicationContext(), getString(R.string.increment_toast),
                            Toast.LENGTH_SHORT).show();
                } else {
                    qt++;
                    mProdQuantity.setText(Integer.toString(qt));
                }
            }
        });

        mDecrementButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int qt = Integer.parseInt(mProdQuantity.getText().toString().trim());
                if (qt == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.decrement_toast),
                            Toast.LENGTH_SHORT).show();
                } else {
                    qt--;
                    mProdQuantity.setText(Integer.toString(qt));
                }
            }
        });

        mSupplierOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //get text for e-mail. Only product name and quantity relevant
                String productName = mProdName.getText().toString().trim();
                String productQuantity = mProdQuantity.getText().toString().trim();

                //creates the message for the supplier
                String message = "Hello, please replenish my stock on the following product:\n" +
                        productName + "\n" +
                        "Quantity: " + productQuantity + "\n.";

                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:")); //only email apps should handle this
                intent.putExtra(Intent.EXTRA_SUBJECT, "Order from Rdu");
                intent.putExtra(Intent.EXTRA_TEXT, message);

                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    private void saveProduct() {

        //read from input fields
        String nameString = mProdName.getText().toString().trim();
        String priceString = mProdPrice.getText().toString().trim();
        String quantityString = mProdQuantity.getText().toString().trim();
        String imageString = "not good";
        if (mImageUri != null) {
            imageString = mImageUri.toString();
        }

        //create a contentvalues object where column names are the keys
        //and attributes are the values.

        //checks if name field is empty
        ContentValues values = new ContentValues();
        if (TextUtils.isEmpty(nameString)) {
            mIsValid = false;
            Toast.makeText(this, getString(R.string.name_required), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mIsValid = true;
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        }

        //checks if price field is empty and higher than 0
        if (TextUtils.isEmpty(priceString) || priceString.equals("0")) {
            mIsValid = false;
            Toast.makeText(this, getString(R.string.price_required), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mIsValid = true;
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
        }

        //checks if quantity field is empty and higher than 0
        if (TextUtils.isEmpty(quantityString) || quantityString.equals("0")) {
            mIsValid = false;
            Toast.makeText(this, getString(R.string.quantity_required), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mIsValid = true;
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        }

        //checks if image is empty
        if (TextUtils.isEmpty(imageString) || imageString.equals("not good")) {
            mIsValid = false;
            Toast.makeText(this, getString(R.string.image_required), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mIsValid = true;
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, imageString);
        }


        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_fail), Toast.LENGTH_SHORT).show();
            } else {
                //otherwise the insertion is successful
                Toast.makeText(this, getString(R.string.editor_insert_success), Toast.LENGTH_SHORT).show();
                //temp log
                Log.v("LOOK AT THIS !!!", "IMAGE URI is" + mImageUri.toString());
            }
        } else {
            //if this is an existing product
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, getString(R.string.editor_insert_fail), Toast.LENGTH_SHORT).show();
            } else {
                //otherwise the insertion is successful
                Toast.makeText(this, getString(R.string.editor_insert_success), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //inserts the product
                saveProduct();
                if (mIsValid) {
                    finish();
                }
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                //Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (MainActivity)
                //NavUtils.navigateUpFromSameTask(this);
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                //otherwise if there are unsaved changes a dialog appears to warn the user
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //user clicked discard button, navigate to parent activity
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                //show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_message);
        builder.setPositiveButton(R.string.delete_action, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel_action, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //delete the product from the database
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);

            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_failed, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_succesful, Toast.LENGTH_SHORT).show();
            }
        }
        //close activity
        finish();
    }
    //method called when back button pressed


    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        //if there are unsaved changes a dialog warns the user
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        //show dialog that notifies the user they have unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.editor_unsaved);
        builder.setPositiveButton(R.string.editor_unsaved_discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.editor_unsaved_keep_editing, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    public void trySelector() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openSelector();
    }


    private void openSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select picture"), 0);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openSelector();
                }
        }
    }

    //upload the image
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mProdImage.setImageURI(mImageUri);
                mProdImage.invalidate();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        //if this is a new product hide the Delete menu item
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_IMAGE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        //bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            //find the columns of product attributes
            int productNameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int productPriceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int productQuantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int productImageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            //extract out the value from the cursor at the given index
            String productName = cursor.getString(productNameColumnIndex);
            int productPrice = cursor.getInt(productPriceColumnIndex);
            int productQuantity = cursor.getInt(productQuantityColumnIndex);
            String image = cursor.getString(productImageColumnIndex);

            //update the views on the screen with values from the database
            mProdName.setText(productName);
            mProdPrice.setText(String.valueOf(productPrice));
            mProdQuantity.setText(String.valueOf(productQuantity));
            mProdImage.setImageURI(Uri.parse(image));
            mImageUri = Uri.parse(image);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mProdName.setText("");
        mProdPrice.setText("");
        mProdQuantity.setText("");
    }
}
