package com.huhx0015.thermalgram.Activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.huhx0015.flirhotornot.R;
import com.huhx0015.thermalgram.Fragments.TGFlirFragment;
import com.huhx0015.thermalgram.Intent.TGShareIntent;
import com.huhx0015.thermalgram.Interface.OnFlirUpdateListener;
import com.huhx0015.thermalgram.Interface.OnFlirViewListener;
import com.huhx0015.thermalgram.Preferences.TGPreferences;
import com.huhx0015.thermalgram.UI.TGSpeech;
import com.huhx0015.thermalgram.UI.TGUnbind;
import java.lang.ref.WeakReference;
import butterknife.ButterKnife;
import butterknife.Bind;

/** ------------------------------------------------------------------------------------------------
 *  [TGMainActivity] CLASS
 *  PROGRAMMER: Michael Yoon Huh (HUHX0015)
 *  DESCRIPTION: This class is the main activity for the application. This activity class handles
 *  all of the views for the lifecycle of the application, including the handling of the
 *  FLIR-related fragment for capturing thermal images, as well as loading the WebView of the
 *  Thermalgram rankings board.
 *  ------------------------------------------------------------------------------------------------
 */

public class TGMainActivity extends AppCompatActivity implements OnFlirUpdateListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // FRAGMENT VARIABLES
    private Boolean isRemovingFragment = false; // Used to determine if the fragment is currently being removed.
    private Boolean showFlirFragment = false; // Used to determine if the FLIR fragment is being shown or not.
    private TGFlirFragment flirFragment; // References the TGFlirFragment class.

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGMainActivity.class.getSimpleName();

    // PREFERENCE VARIABLES
    private static final String TG_OPTIONS = "tg_options"; // Used to reference the name of the preference XML file.
    private String currentImageFile = ""; // References the current image file name.

    // SYSTEM VARIABLES
    private static WeakReference<TGMainActivity> weakRefActivity = null; // Used to maintain a weak reference to the activity.

    // WEBVIEW VARIABLES
    private String currentURL = "http://thermalgram.com/webview.php"; // Used to reference the URL for the WebView object.

    // VIEW INJECTION VARIABLES
    @Bind(R.id.tg_action_button) FloatingActionButton tgActionButton; // References the floating action button object.
    @Bind(R.id.tg_fragment_container) FrameLayout fragmentDisplay; // Used to reference the fragment container.
    @Bind(R.id.tg_webview_progress_bar) ProgressBar tgProgressBar; // References the loading progress bar.
    @Bind(R.id.tg_toolbar) Toolbar tgToolbar; // Used for referencing the Toolbar object.
    @Bind(R.id.tg_main_webview) WebView tgWebView; // Used to reference the WebView object.

    /** ACTIVITY FUNCTIONALITY _________________________________________________________________ **/

    // onCreate(): The initial function that is called when the activity is run. onCreate() only runs
    // when the activity is first started.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        weakRefActivity = new WeakReference<TGMainActivity>(this); // Creates a weak reference of this activity.

        // PREFERENCES:
        // Resets the temporary preference values to default values.
        TGPreferences.setDefaultPreferences(TG_OPTIONS, true, this);
        loadPreferences(); // Loads the values from the application preferences.

        setUpLayout(); // Sets up the layout for the activity.

        // Sets up the WebView fragment with the specified URL.
        if (currentURL != null) {
            Log.d(LOG_TAG, "Current URL:  1["+currentURL+"]");
            setUpWebView(); // Sets up the WebView object for the fragment.
        }
    }

    // onDestroy(): This function runs when the activity has terminated and is being destroyed.
    // Calls recycleMemory() to free up memory allocation.
    @Override
    public void onDestroy() {
        recycleMemory(); // Recycles all View objects to free up memory resources.
        super.onDestroy();
    }

    /** ACTIVITY EXTENSION FUNCTIONALITY _______________________________________________________ **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.tg_main_activity_menu, menu);
        return true;
    }

    // onConfigurationChanged(): If the screen orientation changes, this function loads the proper
    // layout, as well as updating all layout-related objects.
    @Override
    public void onConfigurationChanged(Configuration newConfig){
        super.onConfigurationChanged(newConfig);

        setUpLayout(); // Updates the layout for the TGMainActivity activity.
    }

    // onShareAction(): Defines the action to take if the Share menu option is selected.
    public void onShareAction(MenuItem item) {

        // Shares the data with external activities.
        TGShareIntent.shareThermalIntent(currentImageFile, this);
    }

    /** PHYSICAL BUTTON FUNCTIONALITY __________________________________________________________ **/

    // BACK KEY:
    // onBackPressed(): Defines the action to take when the physical back button key is pressed.
    @Override
    public void onBackPressed() {

        // FLIR: If the FLIR fragment is currently being displayed, pressing the back button
        // will remove the fragment view and display the primary view.
        if (showFlirFragment) {

            // Checks to see if fragment removal is already underway.
            if (!isRemovingFragment) {
                removeFragment("FLIR");
                isRemovingFragment = true;
            }
        }

        else { finish(); } // Finishes the activity.
    }

    /** LAYOUT FUNCTIONALITY ___________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the activity.
    private void setUpLayout() {

        // Sets the XML layout file for the activity.
        setContentView(R.layout.tg_main_activity);
        ButterKnife.bind(this); // ButterKnife view bindion initialization.

        setUpToolbar(); // Sets up the toolbar for the activity.
        setUpButtons(); // Sets up the button listeners for the activity.
    }

    // setUpToolbar(): Sets up the Material Design style toolbar for the activity.
    private void setUpToolbar() {

        // Initializes the Material Design style Toolbar object for the activity.
        if (tgToolbar != null) {
            tgToolbar.setTitle(R.string.app_name);
            setSupportActionBar(tgToolbar);
        }
    }

    // setUpButtons(): Sets up the button images and listeners for the activity.
    private void setUpButtons() {

        // FLOATING ACTION BUTTON: Click listener for the floation action button.
        tgActionButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                openFlirView(true); // Sets up the FLIR fragment view.
            }
        });
    }

    /** FRAGMENT FUNCTIONALITY _________________________________________________________________ **/

    // setUpFragment(): Sets up the fragment view and the fragment view animation.
    private void setUpFragment(Fragment fragment, final String fragType, Boolean isAnimated) {

        if ((weakRefActivity.get() != null) && (!weakRefActivity.get().isFinishing())) {

            // Initializes the manager and transaction objects for the fragments.
            android.support.v4.app.FragmentManager fragMan = weakRefActivity.get().getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragTrans = fragMan.beginTransaction();
            fragTrans.replace(R.id.tg_fragment_container, fragment);

            // Makes the changes to the fragment manager and transaction objects.
            fragTrans.addToBackStack(null);
            fragTrans.commitAllowingStateLoss();

            // Sets up the transition animation.
            if (isAnimated) {

                int animationResource; // References the animation XML resource file.

                // Sets the animation XML resource file, based on the fragment type.
                if (fragType.equals("FLIR")) { animationResource = R.anim.bottom_up; } // FLIR
                else { animationResource = R.anim.slide_down; } // MISCELLANEOUS

                Animation fragmentAnimation = AnimationUtils.loadAnimation(this, animationResource);

                // Sets the AnimationListener for the animation.
                fragmentAnimation.setAnimationListener(new Animation.AnimationListener() {

                    // onAnimationStart(): Runs when the animation is started.
                    @Override
                    public void onAnimationStart(Animation animation) {
                        fragmentDisplay.setVisibility(View.VISIBLE); // Displays the fragment.
                    }

                    // onAnimationEnd(): The fragment is removed after the animation ends.
                    @Override
                    public void onAnimationEnd(Animation animation) {

                        Log.d(LOG_TAG, "setUpFragment(): Fragment animation has ended.");

                        // Hides the Floating Action Button.
                        tgActionButton.setVisibility(View.INVISIBLE);
                    }

                    // onAnimationRepeat(): Runs when the animation is repeated.
                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }
                });

                fragmentDisplay.startAnimation(fragmentAnimation); // Starts the animation.
            }

            // Displays the fragment view without any transition animations.
            else {

                fragmentDisplay.setVisibility(View.VISIBLE); // Displays the fragment.

                // Hides the Floating Action Button.
                tgActionButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    // removeFragment(): This method is responsible for displaying the remove fragment animation, as
    // well as removing the fragment view.
    private void removeFragment(final String fragType) {

        if ((weakRefActivity.get() != null) && (!weakRefActivity.get().isFinishing())) {

            int animationResource; // References the animation XML resource file.

            // FLIR
            if (fragType.equals("FLIR")) {

                animationResource = R.anim.bottom_down; // Sets the animation XML resource file.

                // Signals the FLIR fragment to disconnect the FLIR One device.
                disconnectFlirFromFragment();
            }
            else { animationResource = R.anim.slide_up; } // MISCELLANEOUS

            Animation fragmentAnimation = AnimationUtils.loadAnimation(this, animationResource);

            // Sets the AnimationListener for the animation.
            fragmentAnimation.setAnimationListener(new Animation.AnimationListener() {

                // onAnimationStart(): Runs when the animation is started.
                @Override
                public void onAnimationStart(Animation animation) {  }

                // onAnimationEnd(): The fragment is removed after the animation ends.
                @Override
                public void onAnimationEnd(Animation animation) {

                    Log.d(LOG_TAG, "removeFragment(): Fragment animation has ended. Attempting to remove fragment.");

                    // Initializes the manager and transaction objects for the fragments.
                    FragmentManager fragMan = getSupportFragmentManager();
                    fragMan.popBackStack(); // Pops the fragment from the stack.
                    fragmentDisplay.removeAllViews(); // Removes all views in the layout.

                    // ACTION: Indicates that the action fragment is no longer active.
                    if (fragType.equals("FLIR")) { showFlirFragment = false; }

                    tgToolbar.setTitle(R.string.app_name); // Changes the title of the toolbar.
                    fragmentDisplay.setVisibility(View.INVISIBLE); // Hides the fragment.
                    isRemovingFragment = false; // Indicates that the fragment is no longer being removed.

                    // Displays the action button.
                    tgActionButton.setVisibility(View.VISIBLE);

                    Log.d(LOG_TAG, "removeFragment(): Fragment has been removed.");
                }

                // onAnimationRepeat(): Runs when the animation is repeated.
                @Override
                public void onAnimationRepeat(Animation animation) { }
            });

            fragmentDisplay.startAnimation(fragmentAnimation); // Starts the animation.
            loadPreferences(); // Updates values from preferences.
        }
    }

    // openFlirView(): Sets up and displays the TGFlirFragment view.
    private void openFlirView(Boolean isAnimated) {

        // Changes the title of the toolbar.
        tgToolbar.setTitle(R.string.thelfie);

        // Sets up the TGFlirFragment view.
        flirFragment = new TGFlirFragment();
        setUpFragment(flirFragment, "FLIR", isAnimated);

        // Indicates that the TGFlirFragment is currently being shown.
        showFlirFragment = true;
    }

    /** WEBVIEW FUNCTIONALITY __________________________________________________________________ **/

    // setUpWebView(): Sets up the WebView object for Internet viewing and connectivity.
    private void setUpWebView() {

        tgWebView.getSettings().setJavaScriptEnabled(true);
        tgWebView.addJavascriptInterface(new TGWebAppInterface(this), "Android");
        tgWebView.getSettings().setUserAgentString("Mozilla/5.0 (iPhone; U; CPU like Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Version/3.0 Mobile/1A543a Safari/419.3");

        // Sets a listener for the WebView.
        tgWebView.setWebViewClient(new WebViewClient() {

            // onPageFinished(): Runs when the WebView has fully loaded.
            public void onPageFinished(WebView view, String url) {
                tgProgressBar.setVisibility(View.GONE); // Hides the progress bar.
            }
        });

        tgWebView.loadUrl(currentURL); // Loads the URL for the WebView.
    }

    // TGWebClient(): A method which extends the WebViewClient class to handle the override of URL
    // loading behavior.
    private class TGWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return false;
        }
    }

    // TGWebAppInterface(): This method is responsible for binding JavaScript code to Android code.
    public class TGWebAppInterface {

        // Instantiates the interface and sets the context.
        Context mContext;
        TGWebAppInterface(Context c) {
            mContext = c;
        }

        // getHotRating(): Determines if the view should be closed.
        @JavascriptInterface
        public void getHotRating(Double value) {

            Log.d(LOG_TAG, "getHotRating(): Javascript Interface activated with value: " + value);

            // Initiates speech output.
            TGSpeech.startSpeech(TGSpeech.getPhrase(value), TGMainActivity.this);
        }
    }

    /** PREFERENCES FUNCTIONALITY ______________________________________________________________ **/

    // loadPreferences(): Loads the SharedPreference values from the stored SharedPreferences object.
    private void loadPreferences() {

        // Initializes the SharedPreferences object.
        SharedPreferences TG_prefs = TGPreferences.initializePreferences(TG_OPTIONS, this);

        // Retrieves the current image file name.
        currentImageFile = TGPreferences.getCurrentImage(TG_prefs);
    }

    /** RECYCLE FUNCTIONALITY __________________________________________________________________ **/

    // recycleMemory(): Recycles ImageView and View objects to clear up memory resources prior to
    // Activity destruction.
    private void recycleMemory() {

        // Unbinds all Drawable objects attached to the current layout.
        try { TGUnbind.unbindDrawables(findViewById(R.id.tg_main_activity_layout)); }
        catch (NullPointerException e) { e.printStackTrace(); } // Prints error message.
    }

    /** INTERFACE FUNCTIONALITY ________________________________________________________________ **/

    // disconnectFlirFromFragment(): An interface method that signals the TGFlirFragment class to
    // disconnect the FLIR One device.
    public void disconnectFlirFromFragment() {
        try { ((OnFlirViewListener) flirFragment).disconnectFlirDevice(); }
        catch (ClassCastException cce) { } // Catch for class cast exception errors.
    }

    // updatePreferences(): An interface method that is signalled by TGFlirFragment class to update
    // the preference values.
    @Override
    public void updatePreferences() {
        loadPreferences(); // Loads the SharedPreference values.
    }
}
