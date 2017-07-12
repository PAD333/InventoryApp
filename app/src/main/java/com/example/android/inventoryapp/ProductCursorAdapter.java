package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.R.attr.data;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.item_name);
        TextView summaryTextView = (TextView) view.findViewById(R.id.item_summary);
        TextView quantityTextView = (TextView) view.findViewById(R.id.item_quantity);
        TextView priceTextView = (TextView) view.findViewById(R.id.item_price);
        ImageView imageImageView = (ImageView) view.findViewById(R.id.item_image);
        Button saleButton = (Button) view.findViewById(R.id.item_sale);

        // Find the columns of product attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_DESCRIPTION);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);
        int contactColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_CONTACT);

        int idColumnIndex = cursor.getColumnIndexOrThrow(ProductEntry._ID);
        final Uri productEntryUri = ContentUris.withAppendedId(ProductEntry.CONTENT_URI, cursor.getInt(idColumnIndex));

        // Read the product attributes from the Cursor for the current product
        // (it could have been done directly, but it's clearer for me like this)
        String productName = cursor.getString(nameColumnIndex);
        String productDescription = cursor.getString(descriptionColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        String picturePath = cursor.getString(imageColumnIndex);

        // If the product description is empty string or null, then use some default text
        // that says "Unknown description", so the TextView isn't blank.
        if (TextUtils.isEmpty(productDescription)) {
            productDescription = context.getString(R.string.unknown_description);
        }

        // Update the Views with the attributes for the current product
        nameTextView.setText(productName);
        summaryTextView.setText(productDescription);
        quantityTextView.setText("Quantity: " + String.valueOf(productQuantity));
        priceTextView.setText("Price: " + String.valueOf(productPrice));

        Uri imageUri = Uri.parse(picturePath);
        imageImageView.setImageURI(imageUri);

        View.OnClickListener saleOne = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Substract one only if quantity is higher than 0
                if (productQuantity > 0) {
                    int newProductQuantity = productQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newProductQuantity);
                    context.getContentResolver().update(productEntryUri, values, null, null);
                } else {
                    // Toast saying that quantity is 0
                    Toast.makeText(context, context.getString(R.string.toast_no_stock), Toast.LENGTH_SHORT).show();
                }
            }
        };
        saleButton.setOnClickListener(saleOne);
    }
}
