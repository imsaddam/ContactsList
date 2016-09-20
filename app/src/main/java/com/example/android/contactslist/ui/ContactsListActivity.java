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

import android.app.SearchManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.R;
import com.example.android.contactslist.util.Utils;


public class ContactsListActivity extends FragmentActivity implements
        ContactsListFragment.OnContactsInteractionListener {

    // Defines a tag for identifying log entries
    private static final String TAG = "ContactsListActivity";

    private ContactDetailFragment mContactDetailFragment;

    // If true, this is a larger screen device which fits two panes
    private boolean isTwoPaneLayout;


    private boolean isSearchResultView = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        // Check if two pane bool is set based on resource directories
        isTwoPaneLayout = getResources().getBoolean(R.bool.has_two_panes);

        if (Intent.ACTION_SEARCH.equals(getIntent().getAction())) {


            String searchQuery = getIntent().getStringExtra(SearchManager.QUERY);
            ContactsListFragment mContactsListFragment = (ContactsListFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_list);


            isSearchResultView = true;
            mContactsListFragment.setSearchQuery(searchQuery);

            // Set special title for search results
            String title = getString(R.string.contacts_list_search_results_title, searchQuery);
            setTitle(title);
        }

        if (isTwoPaneLayout) {
            // If two pane layout, locate the contact detail fragment
            mContactDetailFragment = (ContactDetailFragment)
                    getSupportFragmentManager().findFragmentById(R.id.contact_detail);
        }
    }


    @Override
    public void onContactSelected(Uri contactUri) {
        if (isTwoPaneLayout && mContactDetailFragment != null) {
            // If two pane layout then update the detail fragment to show the selected contact
            mContactDetailFragment.setContact(contactUri);
        } else {

            Intent intent = new Intent(this, ContactDetailActivity.class);
            intent.setData(contactUri);
            startActivity(intent);
        }
    }


    @Override
    public void onSelectionCleared() {
        if (isTwoPaneLayout && mContactDetailFragment != null) {
            mContactDetailFragment.setContact(null);
        }
    }

    @Override
    public boolean onSearchRequested() {

        return !isSearchResultView && super.onSearchRequested();
    }
}
