package com.example.android.inventoryapp.data;

import android.net.Uri;
import android.content.ContentResolver;
import android.provider.BaseColumns;

import com.example.android.inventoryapp.R;

import static android.text.style.TtsSpan.GENDER_FEMALE;
import static android.text.style.TtsSpan.GENDER_MALE;

public final class ProductContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    public static final String ANDROID_RESOURCE_URI = "android.resource://";
    public static final String DRAWABLE_URI = "/drawable/";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTS = "Products";

    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        /**
         * The content URI to access the Product data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        //The MIME type  for a list of Products.
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        //The MIME type for a single Product.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;


        // Name of database table
        public final static String TABLE_NAME = "Products";

        //ID number
        public final static String _ID = BaseColumns._ID;


        //Name of the Product (TEXT)
        public final static String COLUMN_PRODUCT_NAME = "name";

        //Description of the Product (TEXT)
        public final static String COLUMN_PRODUCT_DESCRIPTION = "description";

        //Quantity of the Product (INTEGER)
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";

        //Price of the Product (INTEGER)
        public final static String COLUMN_PRODUCT_PRICE = "price";

        // URI of the image of the Product (TEXT)
        public final static String COLUMN_PRODUCT_IMAGE = "image";

        // URI of the "order more" contact (TEXT)
        public final static String COLUMN_PRODUCT_CONTACT = "contact";

        public final static String PRODUCT_CONTACT = "mysupplier@allthingsintheworld.com";
    }
}
