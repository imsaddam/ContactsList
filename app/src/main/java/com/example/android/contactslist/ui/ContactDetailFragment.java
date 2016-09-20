/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.contactslist.ui;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.provider.ContactsContract.Data;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.R;
import com.example.android.contactslist.util.ImageLoader;
import com.example.android.contactslist.util.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;


public class ContactDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_CONTACT_URI =
            "com.example.android.contactslist.ui.EXTRA_CONTACT_URI";

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactDetailFragment";


    private static final String GEO_URI_SCHEME_PREFIX = "geo:0,0?q=";

    // Whether or not this fragment is showing in a two pane layout
    private boolean mIsTwoPaneLayout;

    private Uri mContactUri; // Stores the contact Uri for this fragment instance
    private ImageLoader mImageLoader; // Handles loading the contact image in a background thread


    private ImageView mImageView;
    private LinearLayout mDetailsLayout;
    private TextView mEmptyView;
    private TextView mContactName;
    private MenuItem mEditContactMenuItem;


    public static ContactDetailFragment newInstance(Uri contactUri) {
        // Create new instance of this fragment
        final ContactDetailFragment fragment = new ContactDetailFragment();

        // Create and populate the args bundle
        final Bundle args = new Bundle();
        args.putParcelable(EXTRA_CONTACT_URI, contactUri);

        // Assign the args bundle to the new fragment
        fragment.setArguments(args);

        // Return fragment
        return fragment;
    }


    public ContactDetailFragment() {}


    public void setContact(Uri contactLookupUri) {


        if (Utils.hasHoneycomb()) {
            mContactUri = contactLookupUri;
        } else {

            mContactUri = Contacts.lookupContact(getActivity().getContentResolver(),
                    contactLookupUri);
        }

        // If the Uri contains data, load the contact's image and load contact details.
        if (contactLookupUri != null) {
            // Asynchronously loads the contact image
            mImageLoader.loadImage(mContactUri, mImageView);

            // Shows the contact photo ImageView and hides the empty view
            mImageView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            // Shows the edit contact action/menu item
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(true);
            }


            getLoaderManager().restartLoader(ContactDetailQuery.QUERY_ID, null, this);
            getLoaderManager().restartLoader(ContactAddressQuery.QUERY_ID, null, this);
        } else {

            mImageView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mDetailsLayout.removeAllViews();
            if (mContactName != null) {
                mContactName.setText("");
            }
            if (mEditContactMenuItem != null) {
                mEditContactMenuItem.setVisible(false);
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        mIsTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);


        setHasOptionsMenu(true);


        mImageLoader = new ImageLoader(getActivity(), getLargestScreenDimension()) {
            @Override
            protected Bitmap processBitmap(Object data) {
                // This gets called in a background thread and passed the data from
                // ImageLoader.loadImage().
                return loadContactPhoto((Uri) data, getImageSize());

            }
        };

        // Set a placeholder loading image for the image loader
        mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_180_holo_light);

        // Tell the image loader to set the image directly when it's finished loading
        // rather than fading in
        mImageLoader.setImageFadeIn(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        // Inflates the main layout to be used by this fragment
        final View detailView =
                inflater.inflate(R.layout.contact_detail_fragment, container, false);

        // Gets handles to view objects in the layout
        mImageView = (ImageView) detailView.findViewById(R.id.contact_image);
        mDetailsLayout = (LinearLayout) detailView.findViewById(R.id.contact_details_layout);
        mEmptyView = (TextView) detailView.findViewById(android.R.id.empty);

        if (mIsTwoPaneLayout) {

            mContactName = (TextView) detailView.findViewById(R.id.contact_name);
            mContactName.setVisibility(View.VISIBLE);
        }

        return detailView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // If not being created from a previous state
        if (savedInstanceState == null) {
            // Sets the argument extra as the currently displayed contact
            setContact(getArguments() != null ?
                    (Uri) getArguments().getParcelable(EXTRA_CONTACT_URI) : null);
        } else {

            setContact((Uri) savedInstanceState.getParcelable(EXTRA_CONTACT_URI));
        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saves the contact Uri
        outState.putParcelable(EXTRA_CONTACT_URI, mContactUri);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // When "edit" menu option selected
            case R.id.menu_edit_contact:
                // Standard system edit contact intent
                Intent intent = new Intent(Intent.ACTION_EDIT, mContactUri);

                intent.putExtra("finishActivityOnSaveCompleted", true);

                // Start the edit activity
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Inflates the options menu for this fragment
        inflater.inflate(R.menu.contact_detail_menu, menu);

        // Gets a handle to the "find" menu item
        mEditContactMenuItem = menu.findItem(R.id.menu_edit_contact);


        mEditContactMenuItem.setVisible(mContactUri != null);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            // Two main queries to load the required information
            case ContactDetailQuery.QUERY_ID:

                return new CursorLoader(getActivity(), mContactUri,
                        ContactDetailQuery.PROJECTION,
                        null, null, null);
            case ContactAddressQuery.QUERY_ID:

                final Uri uri = Uri.withAppendedPath(mContactUri, Contacts.Data.CONTENT_DIRECTORY);
                return new CursorLoader(getActivity(), uri,
                        ContactAddressQuery.PROJECTION,
                        ContactAddressQuery.SELECTION,
                        null, null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {


        if (mContactUri == null) {
            return;
        }

        switch (loader.getId()) {
            case ContactDetailQuery.QUERY_ID:
                // Moves to the first row in the Cursor
                if (data.moveToFirst()) {

                    final String contactName = data.getString(ContactDetailQuery.DISPLAY_NAME);
                    if (mIsTwoPaneLayout && mContactName != null) {

                        mContactName.setText(contactName);
                    } else {

                        getActivity().setTitle(contactName);
                    }
                }
                break;
            case ContactAddressQuery.QUERY_ID:

                final LinearLayout.LayoutParams layoutParams =
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);


                mDetailsLayout.removeAllViews();

                // Loops through all the rows in the Cursor
                if (data.moveToFirst()) {
                    do {
                        // Builds the address layout
                        final LinearLayout layout = buildAddressLayout(
                                data.getInt(ContactAddressQuery.TYPE),
                                data.getString(ContactAddressQuery.LABEL),
                                data.getString(ContactAddressQuery.ADDRESS));

                        mDetailsLayout.addView(layout, layoutParams);
                    } while (data.moveToNext());
                } else {
                    // If nothing found, adds an empty address layout
                    mDetailsLayout.addView(buildEmptyAddressLayout(), layoutParams);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


    private LinearLayout buildEmptyAddressLayout() {
        return buildAddressLayout(0, null, null);
    }


    private LinearLayout buildAddressLayout(int addressType, String addressTypeLabel,
            final String address) {

        // Inflates the address layout
        final LinearLayout addressLayout =
                (LinearLayout) LayoutInflater.from(getActivity()).inflate(
                        R.layout.contact_detail_item, mDetailsLayout, false);

        // Gets handles to the view objects in the layout
        final TextView headerTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_header);
        final TextView addressTextView =
                (TextView) addressLayout.findViewById(R.id.contact_detail_item);
        final ImageButton viewAddressButton =
                (ImageButton) addressLayout.findViewById(R.id.button_view_address);


        if (addressTypeLabel == null && addressType == 0) {
            headerTextView.setVisibility(View.GONE);
            viewAddressButton.setVisibility(View.GONE);
            addressTextView.setText(R.string.no_address);
        } else {
            // Gets postal address label type
            CharSequence label =
                    StructuredPostal.getTypeLabel(getResources(), addressType, addressTypeLabel);

            // Sets TextView objects in the layout
            headerTextView.setText(label);
            addressTextView.setText(address);

            // Defines an onClickListener object for the address button
            viewAddressButton.setOnClickListener(new View.OnClickListener() {
                // Defines what to do when users click the address button
                @Override
                public void onClick(View view) {

                    final Intent viewIntent =
                            new Intent(Intent.ACTION_VIEW, constructGeoUri(address));


                    final PackageManager packageManager = getActivity().getPackageManager();


                    if (packageManager.resolveActivity(
                            viewIntent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                        startActivity(viewIntent);
                    } else {

                        Toast.makeText(getActivity(),
                                R.string.no_intent_found, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
        return addressLayout;
    }


    private Uri constructGeoUri(String postalAddress) {

        return Uri.parse(GEO_URI_SCHEME_PREFIX + Uri.encode(postalAddress));
    }


    private int getLargestScreenDimension() {

        final DisplayMetrics displayMetrics = new DisplayMetrics();

        // Retrieves a displayMetrics object for the device's default display
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;

        // Returns the larger of the two values
        return height > width ? height : width;
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private Bitmap loadContactPhoto(Uri contactUri, int imageSize) {


        if (!isAdded() || getActivity() == null) {
            return null;
        }


        final ContentResolver contentResolver = getActivity().getContentResolver();


        AssetFileDescriptor afd = null;

        if (Utils.hasICS()) {

            try {

                Uri displayImageUri = Uri.withAppendedPath(contactUri, Photo.DISPLAY_PHOTO);


                afd = contentResolver.openAssetFileDescriptor(displayImageUri, "r");
                // If the file exists
                if (afd != null) {
                    // Reads and decodes the file to a Bitmap and scales it to the desired size
                    return ImageLoader.decodeSampledBitmapFromDescriptor(
                            afd.getFileDescriptor(), imageSize, imageSize);
                }
            } catch (FileNotFoundException e) {
                // Catches file not found exceptions
                if (BuildConfig.DEBUG) {

                    Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                            + ": " + e.toString());
                }
            } finally {

                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {

                    }
                }
            }
        }


        try {

            Uri imageUri = Uri.withAppendedPath(contactUri, Photo.CONTENT_DIRECTORY);

            // Retrieves an AssetFileDescriptor from the Contacts Provider, using the constructed
            // Uri
            afd = getActivity().getContentResolver().openAssetFileDescriptor(imageUri, "r");

            // If the file exists
            if (afd != null) {
                // Reads the image from the file, decodes it, and scales it to the available screen
                // area
                return ImageLoader.decodeSampledBitmapFromDescriptor(
                        afd.getFileDescriptor(), imageSize, imageSize);
            }
        } catch (FileNotFoundException e) {
            // Catches file not found exceptions
            if (BuildConfig.DEBUG) {

                Log.d(TAG, "Contact photo not found for contact " + contactUri.toString()
                        + ": " + e.toString());
            }
        } finally {

            if (afd != null) {
                try {
                    afd.close();
                } catch (IOException e) {

                }
            }
        }

        // If none of the case selectors match, returns null.
        return null;
    }


    public interface ContactDetailQuery {

        final static int QUERY_ID = 1;

        // The query projection (columns to fetch from the provider)
        @SuppressLint("InlinedApi")
        final static String[] PROJECTION = {
                Contacts._ID,
                Utils.hasHoneycomb() ? Contacts.DISPLAY_NAME_PRIMARY : Contacts.DISPLAY_NAME,
        };

        // The query column numbers which map to each value in the projection
        final static int ID = 0;
        final static int DISPLAY_NAME = 1;
    }


    public interface ContactAddressQuery {

        final static int QUERY_ID = 2;

        // The query projection (columns to fetch from the provider)
        final static String[] PROJECTION = {
                StructuredPostal._ID,
                StructuredPostal.FORMATTED_ADDRESS,
                StructuredPostal.TYPE,
                StructuredPostal.LABEL,
        };

        final static String SELECTION =
                Data.MIMETYPE + "='" + StructuredPostal.CONTENT_ITEM_TYPE + "'";


        final static int ID = 0;
        final static int ADDRESS = 1;
        final static int TYPE = 2;
        final static int LABEL = 3;
    }
}
