package com.example.android.inventoryapp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.R.attr.description;
import static android.R.attr.name;
import static com.example.android.inventoryapp.data.ProductContract.ProductEntry.PRODUCT_CONTACT;

//Allows user to create a new product or edit an existing one.
public class EditorActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String MAILTO = "mailto:";
    //Identifier for the product data loader
    private static final int EXISTING_PRODUCT_LOADER = 0;
    private final static int SELECT_PICTURE = 800;

    //Content URI for the existing product (null if it's a new product)
    private Uri mCurrentProductUri;

    // User inputs:
    private EditText mNameEditText;
    private EditText mDescriptionEditText;
    private TextView mQuantityTextView;
    private TextView mPriceTextView;

    // Buttons to modify the product's quantity
    private Button mQuantityAddOne;
    private Button mQuantitySubstractOne;

    // Image
    private ImageView mImageView;
    private Button mSelectImageButton;
    private Bitmap mImageBitmap;
    private Uri mImageUri;

    //Rest of product data
    private String mName;
    private String mDescription;
    private int mQuantity;
    private int mPrice;

    // Order Button
    private Button mOrderButton;


    //Boolean flag that keeps track of whether the product has been edited (true) or not (false)
    private boolean mProductHasChanged = false;

    //OnTouchListener that listens for any user touches on a View, implying that they are modifying it
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    //region (region)Add/Substract from quantity if buttons are pressed:

    private View.OnClickListener quantityAdd = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quantity_buffer = mQuantityTextView.getText().toString().trim();
            int quantity;
            if (TextUtils.isEmpty(quantity_buffer)) {
                quantity = 0;
            } else {
                quantity = Integer.parseInt(quantity_buffer);
            }
            quantity++;
            mQuantityTextView.setText(String.valueOf(quantity));
        }
    };

    private View.OnClickListener quantitySubstract = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String quantity_buffer = mQuantityTextView.getText().toString().trim();
            int quantity;
            if (TextUtils.isEmpty(quantity_buffer)) {
                quantity = 0;
            } else {
                quantity = Integer.parseInt(quantity_buffer);
            }
            if (quantity > 0) {
                quantity--;
                mQuantityTextView.setText(String.valueOf(quantity));
            }
        }
    };
    //endregion

    // region (region) Select Image implementation
    private View.OnClickListener selectImage = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ActivityCompat.requestPermissions(EditorActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);

        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission granted
                    Intent getIntent = new Intent(Intent.ACTION_PICK);
                    getIntent.setType("image/*");
                    startActivityForResult(getIntent, SELECT_PICTURE);
                } else {
                    // permission denied
                    Toast.makeText(EditorActivity.this, "Permission denied to read external storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_PICTURE && resultCode == RESULT_OK) {
            // Get image Uri
            mImageUri = data.getData();
            // Get image file path
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

            // Create cursor object and query image
            Cursor cursor = getContentResolver().query(mImageUri, filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // Get image path from cursor
            String picturePath = cursor.getString(columnIndex);
            cursor.close();
            // Set the image to a Bitmap object
            mImageBitmap = BitmapFactory.decodeFile(picturePath);
            // Set Bitmap to the image view
            mImageView.setImageBitmap(mImageBitmap);
        }
    }
    // endregion  meth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        // Examine the intent that was used to launch this activity,
        // in order to figure out if we're creating a new product or editing an existing one.
        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        // If the intent DOES NOT contain a product content URI, then we know that we are
        // creating a new product.
        if (mCurrentProductUri == null) {
            // This is a new product, so change the app bar to say "Add a Product"
            setTitle(getString(R.string.editor_activity_title_new_product));

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a product that hasn't been created yet.)
            invalidateOptionsMenu();

            // Hide the Order Button
            mOrderButton = (Button) findViewById(R.id.button_order_more);
            mOrderButton.setVisibility(View.GONE);


        } else {
            // Otherwise this is an existing product, so change app bar to say "Edit Product"
            setTitle(getString(R.string.editor_activity_title_edit_product));

            // Show the Order Button and set a listener
            mOrderButton = (Button) findViewById(R.id.button_order_more);
            mOrderButton.setVisibility(View.VISIBLE);
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String [] recipient = new String[1];
                    recipient[0] = PRODUCT_CONTACT;
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setData(Uri.parse(MAILTO));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, recipient);
                    intent.putExtra(Intent.EXTRA_SUBJECT, "New order of " + mName);
                    startActivity(Intent.createChooser(intent, "Send mail to..."));
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                    }
                }
            });

            // Initialize a loader to read the product data from the database
            // and display the current values in the editor
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_product_name);
        mDescriptionEditText = (EditText) findViewById(R.id.edit_product_description);
        mQuantityTextView = (TextView) findViewById(R.id.edit_product_quantity);
        mPriceTextView = (TextView) findViewById(R.id.edit_product_price);

        mQuantityAddOne = (Button) findViewById(R.id.quantity_button_plus_one);
        mQuantitySubstractOne = (Button) findViewById(R.id.quantity_button_minus_one);

        mImageView = (ImageView) findViewById(R.id.edit_product_image);
        mSelectImageButton = (Button) findViewById(R.id.button_select_image);


        // Setup OnTouchListeners on all the input fields, so we can determine if the user
        // has touched or modified them. This will let us know if there are unsaved changes
        // or not, if the user tries to leave the editor without saving.
        mNameEditText.setOnTouchListener(mTouchListener);
        mDescriptionEditText.setOnTouchListener(mTouchListener);
        mQuantityTextView.setOnTouchListener(mTouchListener);
        mPriceTextView.setOnTouchListener(mTouchListener);

        mQuantityAddOne.setOnClickListener(quantityAdd);
        mQuantitySubstractOne.setOnClickListener(quantitySubstract);
        mSelectImageButton.setOnClickListener(selectImage);
    }

    // Get user input from editor and save product into database.
    private void saveProduct() {


        // Read from input fields
        String nameString = mNameEditText.getText().toString().trim();
        String descriptionString = mDescriptionEditText.getText().toString().trim();
        String quantityString = mQuantityTextView.getText().toString().trim();
        String priceString = mPriceTextView.getText().toString().trim();

        // Check if this is supposed to be a new product
        if (mCurrentProductUri == null &&
                TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(descriptionString)
                && TextUtils.isEmpty(quantityString)
                && TextUtils.isEmpty(priceString)
                && mImageUri == null) {
            // Since no fields were modified:
            return;
        }

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.toast_no_image), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(nameString)) {
            Toast.makeText(this, getString(R.string.toast_no_name), Toast.LENGTH_SHORT).show();
        } else if (TextUtils.isEmpty(priceString)) {
            Toast.makeText(this, getString(R.string.toast_no_price), Toast.LENGTH_SHORT).show();
        }    else if (TextUtils.isEmpty(quantityString)) {
            Toast.makeText(this, getString(R.string.toast_no_quantity), Toast.LENGTH_SHORT).show();
        }else {
            //ContentValues
            ContentValues values = new ContentValues();
            values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
            values.put(ProductEntry.COLUMN_PRODUCT_DESCRIPTION, descriptionString);
            values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());
            values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
            values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);
            values.put(ProductEntry.COLUMN_PRODUCT_CONTACT, PRODUCT_CONTACT);

            // Determine if this is a new or existing product by checking if mCurrentProductUri is null or not
            if (mCurrentProductUri == null) {
                // New:
                Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Existing product:
                int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);
                if (rowsAffected == 0) {
                    // No rows were affected, error with the update
                    Toast.makeText(this, getString(R.string.editor_update_product_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Update  successful
                    Toast.makeText(this, getString(R.string.editor_update_product_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    // Menu items can be hidden or made visible
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Save product to database
            case R.id.action_save:
                saveProduct();
                finish();
                return true;
            //Delete
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            //"Up" arrow button in the app bar
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // If there are unsaved changes, setup a dialog to warn the user.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //back button is pressed.
    @Override
    public void onBackPressed() {
        // If the product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, warn the user.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_DESCRIPTION,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE,
                ProductEntry.COLUMN_PRODUCT_CONTACT
        };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Move to the first row of the cursor and read data from it
        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
            int contactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CONTACT);

            // Extract the values from the Cursor
            mName = cursor.getString(nameColumnIndex);
            mDescription = cursor.getString(descriptionColumnIndex);
            mQuantity = cursor.getInt(quantityColumnIndex);
            mPrice = cursor.getInt(priceColumnIndex);
            String imageString = cursor.getString(imageColumnIndex);
            mImageUri = Uri.parse(imageString);

            // Update the views
            mNameEditText.setText(mName);
            mDescriptionEditText.setText(mDescription);
            mQuantityTextView.setText(Integer.toString(mQuantity));
            mPriceTextView.setText(Integer.toString(mPrice));
            mImageView.setImageURI(mImageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mDescriptionEditText.setText("");
        mQuantityTextView.setText("");
        mPriceTextView.setText("");
        mImageView.setImageBitmap(mImageBitmap);
    }

    //Warn the user there are unsaved changes that will be lost if they continue leaving the editor.
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    //Confirm deletion of the product?
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
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

    // Deletion of the product in the database.
    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }
}