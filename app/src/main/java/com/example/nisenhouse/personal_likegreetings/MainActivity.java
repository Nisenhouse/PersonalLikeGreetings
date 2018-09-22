package com.example.nisenhouse.personal_likegreetings;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.nisenhouse.personal_likegreetings.services.WhatsappAccessibilityService;

import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    public static final String NAME = "<name>";
    private final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 0;
    private List<WhatsAppContact> contacts;
//    private List<WhatsAppContact> selectedContacts;
    private boolean hideUnchecked = false;
    private boolean disabled = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    findViewById(R.id.contacts_list).setVisibility(View.VISIBLE);
                    findViewById(R.id.message_sender).setVisibility(View.INVISIBLE);
                    hideUnchecked = false;
                    disabled = false;
                    return true;
                case R.id.navigation_notifications:
                    findViewById(R.id.contacts_list).setVisibility(View.INVISIBLE);
                    findViewById(R.id.message_sender).setVisibility(View.VISIBLE);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        final EditText filterView = findViewById(R.id.filter_text);

        findViewById(R.id.send_button).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendMessages(false);
            }
        });

        findViewById(R.id.test_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessages(true);
            }
        });

        contacts = new LinkedList<>();
        readWhatsAppContacts();
        ListView listView = findViewById(R.id.contacts_list_itself);
        final CustomContactsAdapter customContactsAdapter = new CustomContactsAdapter(this, contacts);
        listView.setAdapter(customContactsAdapter);

        final ToggleButton toggleButton = findViewById(R.id.toggle_button);
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomContactsAdapter.ItemTextualFilter filter = (CustomContactsAdapter.ItemTextualFilter) customContactsAdapter.getFilter();
                filter.setFilterChecked(((ToggleButton) view).isChecked());
                filter.filter(filterView.getText().toString());
            }
        });

        filterView.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                customContactsAdapter.getFilter().filter(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        if (isAccessibilityOn (this, WhatsappAccessibilityService.class)) {
            Intent intent = new Intent (Settings.ACTION_ACCESSIBILITY_SETTINGS);
            this.startActivity (intent);
        } else {
            Toast.makeText(this, "שליחה אוטומטית מושבתת", Toast.LENGTH_LONG).show();
        }
    }

    private void refreshContantsList() {
        ((ListView) findViewById(R.id.contacts_list_itself)).invalidateViews();
    }

    private boolean isAccessibilityOn (Context context, Class<? extends AccessibilityService> clazz) {
        int accessibilityEnabled = 0;
        final String service = context.getPackageName () + "/" + clazz.getCanonicalName ();
        try {
            accessibilityEnabled = Settings.Secure.getInt(context.getApplicationContext().getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED);
        } catch (Settings.SettingNotFoundException ignored) {  }

        if (accessibilityEnabled != 1) {
            return false;
        }
        TextUtils.SimpleStringSplitter colonSplitter = new TextUtils.SimpleStringSplitter (':');

        String settingValue = Settings.Secure.getString(context.getApplicationContext().getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
        if (settingValue != null) {
            colonSplitter.setString (settingValue);
            while (colonSplitter.hasNext ()) {
                String accessibilityService = colonSplitter.next ();

                if (accessibilityService.equalsIgnoreCase (service)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void sendMessages(boolean test) {
        String message = ((EditText) findViewById(R.id.edit_text)).getText().toString();
        for (WhatsAppContact wc : contacts) {
            if (wc.isChecked()) {
                Log.v("SendTest", wc.toString());
                String msg = formatMessage(message, wc.getName());
                Log.v("SendTest", msg);
                if (!test) {
                    sendWhatsAppMessage(wc.getNumber(), msg);
                }
            }
        }
    }

    private void sendWhatsAppMessage(String number, String msg) {
        String whNumber = number.replace("+", "").replace(" ", "");

        PackageManager packageManager = this.getPackageManager();
        Intent i = new Intent(Intent.ACTION_VIEW);

        try {
            String url = "https://api.whatsapp.com/send?phone="+ whNumber +"&text=" + URLEncoder.encode(msg + getApplicationContext ().getString (R.string.whatsapp_suffix), "UTF-8");
            i.setPackage("com.whatsapp");
            i.setData(Uri.parse(url));
            if (i.resolveActivity(packageManager) != null) {
                this.startActivity(i);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private String formatMessage(String message, String name) {
        return message.replace(NAME, name);
    }

    private void readWhatsAppContacts() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
            return;
        }


        final String[] projection = {
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.Data.MIMETYPE,
                "account_type",
                ContactsContract.Data.DATA3,
        };

        final String selection = ContactsContract.Data.MIMETYPE + " =? and account_type=?";
        final String[] selectionArgs = {
                "vnd.android.cursor.item/vnd.com.whatsapp.profile",
                "com.whatsapp"
        };

        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(
                ContactsContract.Data.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null);

        if (c == null) {
            return;
        }
        contacts.clear();
        while (c.moveToNext()) {
//            String id = c.getString(c.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            String fullName = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            String number = c.getString(c.getColumnIndex(ContactsContract.Data.DATA3)).replace("הודעה אל ", "");
            String aproxName = fullName != null ? fullName.split(" ")[0] : null;
            WhatsAppContact contact = new WhatsAppContact(
                    number,
                    fullName,
                    aproxName
            );

            contacts.add(contact);
            Log.v("WhatsApp", contact.toString());

        }
        Log.v("WhatsApp", "Total WhatsApp Contacts: " + c.getCount());
        c.close();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    refreshContantsList();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }
}
