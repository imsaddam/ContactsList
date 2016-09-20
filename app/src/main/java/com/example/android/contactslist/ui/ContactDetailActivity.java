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

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.example.android.contactslist.BuildConfig;
import com.example.android.contactslist.util.Utils;


public class ContactDetailActivity extends FragmentActivity {

    private static final String TAG = "ContactDetailActivity";

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {

            Utils.enableStrictMode();
        }
        super.onCreate(savedInstanceState);


        if (getIntent() != null) {


            if (Utils.hasHoneycomb()) {

                getActionBar().setDisplayHomeAsUpEnabled(true);
            }


            final Uri uri = getIntent().getData();


            if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


                ft.add(android.R.id.content, ContactDetailFragment.newInstance(uri), TAG);
                ft.commit();
            }
        } else {

            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                NavUtils.navigateUpFromSameTask(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
