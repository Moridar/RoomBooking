package com.example.bobbie.roombooking;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.CalendarContract;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity {
    RefreshThread rt = new RefreshThread();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button buttonView = (Button) findViewById(R.id.button_view);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });

        final Button buttonCreate = (Button) findViewById(R.id.button_create);
        buttonCreate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                addNewEventIntent();
            }
        });

        final ImageButton buttonRefresh = (ImageButton) findViewById(R.id.imageButton_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                refresh();
            }
        });


        rt.start();
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(rt.isAlive()) rt.interrupt();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(rt.isInterrupted()) rt.start();
    }

    private void addNewEventIntent(){
        /*Calendar beginTime = Calendar.getInstance();
        beginTime.set(2012, 0, 19, 7, 30);
        Calendar endTime = Calendar.getInstance();
        endTime.set(2012, 0, 19, 8, 30);*/
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, System.currentTimeMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, System.currentTimeMillis()+(1000*60*60)) // 1 time varighed.
                .putExtra(CalendarContract.Events.TITLE, "Møde")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Mødelokalet")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        startActivity(intent);
    }

    private void showCalendar(){
        long startMillis = System.currentTimeMillis();

        Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
        builder.appendPath("time");
        ContentUris.appendId(builder, startMillis);
        Intent intent = new Intent(Intent.ACTION_VIEW)
                .setData(builder.build());
        startActivity(intent);
    }

    public void refresh(){

        final String DEBUG_TAG = "MyActivity";
                final String[] INSTANCE_PROJECTION = new String[] {
                CalendarContract.Instances.EVENT_ID,      // 0
                CalendarContract.Instances.BEGIN,         // 1
                CalendarContract.Instances.END,           // 2
                CalendarContract.Instances.TITLE          // 3
        };
        // The indices for the projection array above.
            final int PROJECTION_ID_INDEX = 0;
            final int PROJECTION_BEGIN_INDEX = 1;
            final int PROJECTION_END_INDEX = 2;
            final int PROJECTION_TITLE_INDEX = 3;


// Specify the date range you want to search for recurring
// event instances
           /*Calendar beginTime = Calendar.getInstance();
            beginTime.set(2011, 9, 23, 8, 0);*/
            long startMillis = System.currentTimeMillis();
            /* Calendar endTime = Calendar.getInstance();
            endTime.set(2011, 10, 24, 8, 0); */
            long endMillis = System.currentTimeMillis()+(1000*60*60);

            Cursor cur = null;
            ContentResolver cr = getContentResolver();

// The ID of the recurring event whose instances you are searching
// for in the Instances table
            String selection = "";
            String[] selectionArgs = new String[] {};

// Construct the query with the desired date range.
            Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
            ContentUris.appendId(builder, startMillis);
            ContentUris.appendId(builder, endMillis);

// Submit the query
            cur =  cr.query(builder.build(),
                    INSTANCE_PROJECTION,
                    selection,
                    selectionArgs,
                    null);


            while (cur.moveToNext()) {
                String title = null;
                long eventID = 0;
                long beginVal = 0;
                long endVal = 0;

                // Get the field values
                eventID = cur.getLong(PROJECTION_ID_INDEX);
                beginVal = cur.getLong(PROJECTION_BEGIN_INDEX);
                endVal = cur.getLong(PROJECTION_END_INDEX);
                title = cur.getString(PROJECTION_TITLE_INDEX);

                // Do something with the values.
                Log.i(DEBUG_TAG, "Event:  " + title);
                Log.i(DEBUG_TAG, "Starts: " + beginVal);
                Log.i(DEBUG_TAG, "Ends: " + endVal);
                Log.i(DEBUG_TAG, "Currently time: " + System.currentTimeMillis());

                if(beginVal < System.currentTimeMillis() && endVal > System.currentTimeMillis()){

                    Intent intent = new Intent(this, BusyActivity.class);
                    intent.putExtra("endVal", endVal);
                    intent.putExtra("title", title);
                    startActivity(intent);
                }
            }
        }

    class RefreshThread extends Thread {

        public void run(){
            while(true){
                refresh();
                try {
                    sleep(1000*60*2); //Refresher hver 2 min.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
    }
    }
}


