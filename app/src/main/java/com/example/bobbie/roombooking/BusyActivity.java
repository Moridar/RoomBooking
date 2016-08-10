package com.example.bobbie.roombooking;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

public class BusyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_busy);

        final ImageButton buttonRefresh = (ImageButton) findViewById(R.id.imageButton_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refresh();
            }
        });



    }

    class CountdownThread extends Thread {
        long endVal;
        TextView countDown = (TextView) findViewById(R.id.textView_countdown);
        CountdownThread(long endVal){
            this.endVal = endVal;
        }

        public void run(){
            while(endVal > System.currentTimeMillis()){
                long millis = endVal - System.currentTimeMillis();
                if(millis > 0){
                countDown.setText(String.format("%02d min, %02d sec",
                        TimeUnit.MILLISECONDS.toMinutes(millis),
                        TimeUnit.MILLISECONDS.toSeconds(millis) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
                ));
                }
                else{
                    returnToMain();
                }
                try {
                    wait(1000); //Refresher hver 2 min.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void returnToMain(){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }
    public void refresh() {

        final String DEBUG_TAG = "MyActivity";
        final String[] INSTANCE_PROJECTION = new String[]{
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
        long endMillis = System.currentTimeMillis() + (1000 * 60 * 60);

        Cursor cur = null;
        ContentResolver cr = getContentResolver();

// The ID of the recurring event whose instances you are searching
// for in the Instances table
        String selection = "";
        String[] selectionArgs = new String[]{};

// Construct the query with the desired date range.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, startMillis);
        ContentUris.appendId(builder, endMillis);

// Submit the query
        cur = cr.query(builder.build(),
                INSTANCE_PROJECTION,
                selection,
                selectionArgs,
                null);

        boolean busy = false;
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

            if (beginVal < System.currentTimeMillis() && endVal > System.currentTimeMillis()) {
                busy = true;

            }
        }
        if(!busy){
            returnToMain();
        }
    }


}



