package com.huhx0015.flirhotornot;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class HONMainActivity extends ActionBarActivity {

    @InjectView(R.id.thermalgram_toolbar) Toolbar thermalgram_toolbar; // Used for referencing the Toolbar object.

    /** ACTIVITY FUNCTIONALITY _________________________________________________________________ **/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hon_main_activity);
        ButterKnife.inject(this); // ButterKnife view injection initialization.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.hon_main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /** LAYOUT FUNCTIONALITY ___________________________________________________________________ **/

    // setUpToolbar(): Sets up the Material Design style toolbar for the activity.
    private void setUpToolbar() {

        // Initializes the Material Design style Toolbar object for the activity.
        if (thermalgram_toolbar != null) {
            thermalgram_toolbar.setTitle("");
            setSupportActionBar(thermalgram_toolbar);
        }
    }
}
