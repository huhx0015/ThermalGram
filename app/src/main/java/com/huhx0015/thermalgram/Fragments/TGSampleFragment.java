package com.huhx0015.thermalgram.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ToggleButton;
import com.flir.flironeexampleapplication.util.SystemUiHider;
import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.flir.flironesdk.SimulatedDevice;
import com.huhx0015.flirhotornot.R;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import butterknife.ButterKnife;
import butterknife.InjectView;

/** -----------------------------------------------------------------------------------------------
 *  [TGFlirFragment] CLASS
 *  DESCRIPTION: GTNTutorialFragment class is a Fragment class that is used for displaying the FLIR
 *  camera preview image.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGSampleFragment extends Fragment implements Device.Delegate,
        FrameProcessor.Delegate, Device.StreamDelegate, Device.PowerUpdateDelegate {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // FLIR VARIABLES
    static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    static final String ACTION_USB_DISCONNECT = "android.hardware.usb.action.USB_DEVICE_DETACHED";
    static final String ACTION_USB_CONNECT = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    ImageView thermalImageView;
    private volatile boolean imageCaptureRequested = false;
    private volatile Socket streamSocket = null;
    private boolean chargeCableIsConnected = true;

    private int deviceRotation= 0;
    private OrientationEventListener orientationEventListener;
    
    private volatile Device flirOneDevice;
    private FrameProcessor frameProcessor;

    private String lastSavedPath;

    private Device.TuningState currentTuningState = Device.TuningState.Unknown;

    private ColorFilter originalChargingIndicatorColor = null;

    private Bitmap thermalBitmap = null;

    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    ScaleGestureDetector mScaleDetector;


    // SYSTEM VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.
    
    // VIEW INJECTION VARIABLES
    @InjectView(R.id.fullscreen_content_controls) View controlsView;
    @InjectView(R.id.fullscreen_content_controls_top) View controlsViewTop;
    @InjectView(R.id.fullscreen_content) View contentView;
    @InjectView(R.id.imageTypeListView) ListView imageTypeListView;
    @InjectView(R.id.imageTypeListContainer) LinearLayout imageTypeListContainer;
    @InjectView(R.id.paletteListView) ListView paletteListView;
    @InjectView(R.id.imageButton) ImageButton imageButton;
    @InjectView(R.id.change_view_button) ToggleButton changeViewButton;
    @InjectView(R.id.chargeCableToggle) ToggleButton chargeCableButton;
    @InjectView(R.id.streamButton) ToggleButton streamButton;
    @InjectView(R.id.switch_rotate) ToggleButton switchRotateButton;
    @InjectView(R.id.connect_sim_button) Button connectSimButton;
    @InjectView(R.id.tuneButton) Button tuneButton;
    @InjectView(R.id.batteryChargeIndicator) ImageView chargingIndicator;
    @InjectView(R.id.batteryLabelTextView) TextView levelTextView;

    /** FRAGMENT FUNCTIONALITY _________________________________________________________________ **/

    // onAttach(): The initial function that is called when the Fragment is run. The activity is
    // attached to the fragment.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.currentActivity = activity; // Sets the currentActivity to attached activity object.
    }

    // NOTE: Reassigned from onPostCreate(); testing is needed.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    // onCreateView(): Creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View tg_flir_fragment_view = (ViewGroup) inflater.inflate(R.layout.tg_flir_fragment, container, false);
        ButterKnife.inject(this, tg_flir_fragment_view); // ButterKnife view injection initialization.

        setUpLayout(); // Sets up the layout for the fragment.

        return tg_flir_fragment_view;
    }

    // NOTE: Reassigned from onRestart(); testing is needed.
    @Override
    public void onResume(){
        try {
            Device.startDiscovery(currentActivity, this);
        } catch (IllegalStateException e) {
            Log.e("PreviewActivity", "Somehow we've started discovery twice");
            e.printStackTrace();
        }
        super.onResume();
    }

    // onDestroyView(): This function runs when the screen is no longer visible and the view is
    // destroyed.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this); // Sets all injected views to null.
    }


    @Override
    public void onStop() {
        // We must unregister our usb receiver, otherwise we will steal events from other apps
        Log.e("PreviewActivity", "onStop, stopping discovery!");
        Device.stopDiscovery();
        super.onStop();
    }


    /** LAYOUT FUNCTIONALITY ___________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the fragment.
    private void setUpLayout() {
        
        String[] imageTypeNames = new String[RenderedImage.ImageType.values().length];
        // Massage the type names for display purposes
        for (RenderedImage.ImageType t : RenderedImage.ImageType.values()){
            String name = t.name().replaceAll("(RGBA)|(YCbCr)|(8)","").replaceAll("([a-z])([A-Z])", "$1 $2");

            if (name.contains("YCbCr888")){
                name = name.replace("YCbCr888", "Aligned");
            }
            imageTypeNames[t.ordinal()] = name;
        }
        RenderedImage.ImageType defaultImageType = RenderedImage.ImageType.BlendedMSXRGBA8888Image;
        frameProcessor = new FrameProcessor(currentActivity, this, EnumSet.of(defaultImageType));
        
        imageTypeListView.setAdapter(new ArrayAdapter<>(currentActivity, R.layout.emptytextview,imageTypeNames));
        imageTypeListView.setSelection(defaultImageType.ordinal());
        imageTypeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (frameProcessor != null) {
                    RenderedImage.ImageType imageType = RenderedImage.ImageType.values()[position];
                    frameProcessor.setImageTypes(EnumSet.of(imageType));
                    if (imageType.isColorized()){
                        paletteListView.setVisibility(View.VISIBLE);
                    }else{
                        paletteListView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        });
        imageTypeListView.setDivider(null);

        // Palette List View Setup

        paletteListView.setDivider(null);
        paletteListView.setAdapter(new ArrayAdapter<>(currentActivity, R.layout.emptytextview, RenderedImage.Palette.values()));
        paletteListView.setSelection(frameProcessor.getImagePalette().ordinal());
        paletteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (frameProcessor != null){
                    frameProcessor.setImagePalette(RenderedImage.Palette.values()[position]);
                }
            }
        });

        /*
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(currentActivity, contentView, HIDER_FLAGS);
        mSystemUiHider.setup();

        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                            controlsViewTop.animate().translationY(visible ? 0 : -1 * mControlsHeight).setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                            controlsViewTop.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && !(changeViewButton.isChecked() && AUTO_HIDE)) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });
                */

        // Set up the user interaction to manually show or hide the system UI.
        contentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        changeViewButton.setOnTouchListener(mDelayHideTouchListener);

        orientationEventListener = new OrientationEventListener(currentActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                deviceRotation = orientation;
            }
        };
        mScaleDetector = new ScaleGestureDetector(currentActivity, new ScaleGestureDetector.OnScaleGestureListener() {
            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
            }
            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                Log.d("ZOOM", "zoom ongoing, scale: " + detector.getScaleFactor());
                frameProcessor.setMSXDistance(detector.getScaleFactor());
                return false;
            }
        });

        contentView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mScaleDetector.onTouchEvent(event);
                return true;
            }
        });

        setUpButtons(); // Sets up the button listeners for the fragment.
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };


    private void setUpButtons() {

        changeViewButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onChangeViewClicked(v);
            }

        });



        chargeCableButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onSimulatedChargeCableToggleClicked(v);
            }

        });

        connectSimButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onConnectSimClicked(v);
            }

        });

        imageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onCaptureImageClicked(v);
            }

        });

        streamButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onNetStreamClicked(v);
            }

        });

        tuneButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onTuneClicked(v);
            }

        });

        switchRotateButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onRotateClicked(v);
            }

        });
    }
    
    /** THREADING FUNCTIONALITY ________________________________________________________________ **/
    
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
    /** FLIR FUNCTIONALITY _____________________________________________________________________ **/

    private void updateThermalImageView(final Bitmap frame){
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermalImageView.setImageBitmap(frame);
            }
        });
    }

    public void onTuneClicked(View v){
        if (flirOneDevice != null){
            flirOneDevice.performTuning();
        }

    }
    public void onCaptureImageClicked(View v){


        // if nothing's connected, let's load an image instead?

        if(flirOneDevice == null && lastSavedPath != null) {
            // load!
            File file = new File(lastSavedPath);


            Frame frame = new Frame(file);

            // load the frame
            onFrameReceived(frame);
        } else {
            this.imageCaptureRequested = true;
        }
    }
    public void onConnectSimClicked(View v){
        if(flirOneDevice == null){
            try {
                flirOneDevice = new SimulatedDevice(this, getResources().openRawResource(R.raw.sampleframes), 10);
                flirOneDevice.setPowerUpdateDelegate(this);
                chargeCableIsConnected = true;
            } catch(Exception ex) {
                flirOneDevice = null;
                Log.w("FLIROneExampleApp", "IO EXCEPTION");
                ex.printStackTrace();
            }
        }else if(flirOneDevice instanceof SimulatedDevice) {
            flirOneDevice.close();
            flirOneDevice = null;
        }
    }

    public void onSimulatedChargeCableToggleClicked(View v){
        if(flirOneDevice instanceof SimulatedDevice){
            chargeCableIsConnected = !chargeCableIsConnected;
            ((SimulatedDevice)flirOneDevice).setChargeCableState(chargeCableIsConnected);
        }
    }
    public void onRotateClicked(View v){
        ToggleButton theSwitch = (ToggleButton)v;
        if (theSwitch.isChecked()){
            thermalImageView.setRotation(180);
        }else{
            thermalImageView.setRotation(0);
        }
    }
    public void onChangeViewClicked(View v){
        if (frameProcessor == null){
            ((ToggleButton)v).setChecked(false);
            return;
        }

        if (((ToggleButton)v).isChecked()){
            // only show palette list if selected image type is colorized
            paletteListView.setVisibility(View.INVISIBLE);
            for (RenderedImage.ImageType imageType : frameProcessor.getImageTypes()){
                if (imageType.isColorized()) {
                    paletteListView.setVisibility(View.VISIBLE);
                    break;
                }
            }
            imageTypeListView.setVisibility(View.VISIBLE);
            imageTypeListContainer.setVisibility(View.VISIBLE);
        }else{
            imageTypeListContainer.setVisibility(View.GONE);
        }


    }

    public void onImageTypeListViewClicked(View v){
        int index = ((ListView) v).getSelectedItemPosition();
        RenderedImage.ImageType imageType = RenderedImage.ImageType.values()[index];
        frameProcessor.setImageTypes(EnumSet.of(imageType));
        int paletteVisibility = (imageType.isColorized()) ? View.VISIBLE : View.GONE;
        paletteListView.setVisibility(paletteVisibility);
    }

    public void onPaletteListViewClicked(View v){
        RenderedImage.Palette pal = (RenderedImage.Palette )(((ListView)v).getSelectedItem());
        frameProcessor.setImagePalette(pal);
    }

    /**
     * Example method of starting/stopping a frame stream to a host
     * @param v The toggle button pushed
     */
    public void onNetStreamClicked(View v){
        final ToggleButton button = (ToggleButton)v;
        button.setChecked(false);

        if (streamSocket == null || streamSocket.isClosed()){
            AlertDialog.Builder alert = new AlertDialog.Builder(currentActivity);

            alert.setTitle("Start Network Stream");
            alert.setMessage("Provide hostname:port to connect");

            // Set an EditText view to get user input
            final EditText input = new EditText(currentActivity);

            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    final String[] parts = value.split(":");
                    (new Thread(){
                        @Override
                        public void run() {
                            super.run();
                            try {
                                streamSocket = new Socket(parts[0], Integer.parseInt(parts[1], 10));
                                currentActivity.runOnUiThread(new Thread() {
                                    @Override
                                    public void run() {
                                        super.run();
                                        button.setChecked(streamSocket.isConnected());
                                    }
                                });

                            }catch (Exception ex){
                                Log.e("CONNECT",ex.getMessage());
                            }
                        }
                    }).start();

                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });

            alert.show();
        }else{
            try {
                streamSocket.close();
            }catch (Exception ex){

            }
            button.setChecked(streamSocket != null && streamSocket.isConnected());
        }
    }
    
    /** FLIR EXTENSION FUNCTIONALITY ___________________________________________________________ **/

    // Device Delegate methods

    // onTuningStateChanged():
    //If using RenderedImage.ImageType.ThermalRadiometricKelvinImage, you should not rely on
    //the accuracy if tuningState is not Device.TuningState.Tuned
    //@param tuningState
    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {

    }

    @Override
    public void onAutomaticTuningChanged(boolean deviceWillTuneAutomatically) {

    }
    
    // Called during device discovery, when a device is connected
    // During this callback, you should save a reference to device
    // You should also set the power update delegate for the device if you have one
    // Go ahead and start frame stream as soon as connected, in this use case
    // Finally we create a frame processor for rendering frames
    @Override
    public void onDeviceConnected(Device device){
        Log.i("ExampleApp", "Device connected!");

        flirOneDevice = device;
        flirOneDevice.setPowerUpdateDelegate(this);
        flirOneDevice.startFrameStream(this);

        if(flirOneDevice instanceof SimulatedDevice){
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.VISIBLE);
                }
            });
        }else{
            currentActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chargeCableButton.setChecked(chargeCableIsConnected);
                    chargeCableButton.setVisibility(View.INVISIBLE);
                    connectSimButton.setEnabled(false);
                }
            });
        }

        orientationEventListener.enable();
    }


    //Indicate to the user that the device has disconnected
    @Override
    public void onDeviceDisconnected(Device device) {

    }

    // Frame Processor Delegate method, will be called each time a rendered frame is produced
    @Override
    public void onFrameProcessed(final RenderedImage renderedImage){
        Log.v("ExampleApp", "Frame processed!");

        long startTime = System.nanoTime();
        if (renderedImage.imageType() == RenderedImage.ImageType.VisualJPEGImage){
            final Bitmap visBitmap = BitmapFactory.decodeByteArray(renderedImage.pixelData(), 0, renderedImage.pixelData().length);

            // we must rotate the raw visual JPEG to match phone/tablet screen
            android.graphics.Matrix mtx = new android.graphics.Matrix();
            mtx.postRotate(90);
            final Bitmap rotatedVisBitmap = Bitmap.createBitmap(visBitmap, 0, 0, visBitmap.getWidth(), visBitmap.getHeight(), mtx, true);
            updateThermalImageView(rotatedVisBitmap);
        }
        else {
            if (thermalBitmap == null || (renderedImage.width() != thermalBitmap.getWidth() || renderedImage.height() != thermalBitmap.getHeight())) {
                Log.d("THERMALBMP", "Creating thermalBitmap with dimensions: " + renderedImage.width() + "x" + renderedImage.height() );
                thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);

            }
            if (renderedImage.imageType() == RenderedImage.ImageType.ThermalRadiometricKelvinImage){
                /**
                 * Here is a simple example of showing color for 9 bands of tempuratures:
                 * Below 0 celceus is black
                 * 0-10C is dark blue
                 * 10-20C is light blue
                 * 20-36C is green
                 * 36-40C is dark red (human body)
                 * 40-50C is bright red
                 * 50-60C is orange
                 * 60-100C is yellow
                 * Above 100C is white
                 */
                short[] shortPixels = new short[renderedImage.pixelData().length / 2];
                byte[] argbPixels = new byte[renderedImage.width() * renderedImage.height() * 4];

                // Thermal data is little endian.
                ByteBuffer.wrap(renderedImage.pixelData()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPixels);
                final byte aPixValue = (byte)255;
                for (int p = 0; p < shortPixels.length; p++) {
                    int destP = p * 4;
                    int tempInC = (shortPixels[p]-27315)/100;
                    byte rPixValue;
                    byte gPixValue;
                    byte bPixValue;
                    if (tempInC < 0){
                        rPixValue = gPixValue = bPixValue = 0;
                    }else if (tempInC < 10){
                        rPixValue = gPixValue = 0;
                        bPixValue = 127;
                    }else if (tempInC < 20){
                        rPixValue = gPixValue = 0;
                        bPixValue = (byte)255;
                    }else if (tempInC < 36){
                        rPixValue = bPixValue = 0;
                        gPixValue = (byte)160;
                    }else if (tempInC < 40){
                        bPixValue = gPixValue = 0;
                        rPixValue = 127;
                    }else if (tempInC < 50){
                        bPixValue = gPixValue = 0;
                        rPixValue = (byte)255;
                    }else if (tempInC < 60){
                        rPixValue = (byte)255;
                        gPixValue = (byte)166;
                        bPixValue = 0;
                    }else if (tempInC < 100){
                        rPixValue = gPixValue = (byte)255;
                        bPixValue = 0;
                    }else{
                        bPixValue = rPixValue = gPixValue = (byte)255;
                    }
                    // alpha always high
                    argbPixels[destP + 3] = aPixValue;
                    // red pixel
                    argbPixels[destP] = rPixValue;
                    argbPixels[destP + 1] = gPixValue;
                    argbPixels[destP + 2] = bPixValue;
                }

                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbPixels));

            }

            else if(renderedImage.imageType() == RenderedImage.ImageType.ThermalLinearFlux14BitImage) {
                /**
                 * Here is an example of how to apply custom pseudocolor to a 14 bit greyscale image
                 * This example crates a 768-color Black->Green->Aqua->White by linearly mapping
                 * RGB values. Try experimenting with different color mapping approaches.
                 *
                 * This example normalizes the scene linearly. If you want to map colors to temperatures,
                 * use the Radiometic Kelvin image type and do not apply a scale as done below.
                 */

                short[] shortPixels = new short[renderedImage.pixelData().length / 2];
                byte[] argbPixels = new byte[renderedImage.width() * renderedImage.height() * 4];
                // Thermal data is little endian.
                ByteBuffer.wrap(renderedImage.pixelData()).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortPixels);
                int minValue = 65535;
                int maxValue = 0;
                for (int p = 0; p < shortPixels.length; p++) {
                    minValue = Math.min(minValue, shortPixels[p]);
                    maxValue = Math.max(maxValue, shortPixels[p]);
                }
                int range = (maxValue - minValue);
                float scale = ((float) 767 / (float) range);
                for (int p = 0; p < shortPixels.length; p++) {
                    int destP = p * 4;

                    short pixelValue = (short)((shortPixels[p] - minValue) * scale);
                    byte redValue = 0;
                    byte greenValue;
                    byte blueValue = 0;
                    if (pixelValue < 256){
                        greenValue = (byte)pixelValue;
                    }else if (pixelValue < 512){
                        greenValue = (byte)255;
                        blueValue = (byte)(pixelValue - 256);
                    }else{
                        greenValue = (byte)255;
                        blueValue = (byte)255;
                        redValue = (byte)(pixelValue -512);
                    }


                    // alpha always high
                    argbPixels[destP + 3] = (byte) 255;
                    // red pixel
                    argbPixels[destP] = redValue;
                    argbPixels[destP + 1] = greenValue;
                    argbPixels[destP + 2] = blueValue;
                }

                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(argbPixels));
            } else {
                Log.e("render", "width: "+renderedImage.width()+", height: "+renderedImage.height());

                thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));
            }

            updateThermalImageView(thermalBitmap);

            /*
                    Capture this image if requested.
                    */
            if (this.imageCaptureRequested) {
                imageCaptureRequested = false;
                final Context context = currentActivity;
                new Thread(new Runnable() {
                    public void run() {
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
                        String formatedDate = sdf.format(new Date());
                        String fileName = "FLIROne-" + formatedDate + ".jpg";
                        try{
                            lastSavedPath = path+ "/" + fileName;
                            renderedImage.getFrame().save(lastSavedPath, RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);

                            MediaScannerConnection.scanFile(context,
                                    new String[]{path + "/" + fileName}, null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                            Log.i("ExternalStorage", "Scanned " + path + ":");
                                            Log.i("ExternalStorage", "-> uri=" + uri);
                                        }

                                    });

                        }catch (Exception e){
                            e.printStackTrace();
                        }
                        currentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                thermalImageView.animate().setDuration(50).scaleY(0).withEndAction((new Runnable() {
                                    public void run() {
                                        thermalImageView.animate().setDuration(50).scaleY(1);
                                    }
                                }));
                            }
                        });
                    }
                }).start();
            }
            long endTime = System.nanoTime();
            long duration = (endTime - startTime);
            Log.d("ImageProcessingDuration", "Duration: "+(duration/1000000)+"ms");
            if (streamSocket != null && streamSocket.isConnected()){
                try {
                    // send PNG file over socket in another thread
                    final OutputStream outputStream = streamSocket.getOutputStream();
                    // make a output stream so we can get the size of the PNG
                    final ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();

                    thermalBitmap.compress(Bitmap.CompressFormat.WEBP, 100, bufferStream);
                    bufferStream.flush();
                    (new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                            /*
                             * Header is 6 bytes indicating the length of the image data and rotation
                             * of the device
                             * This could be expanded upon by adding bytes to have more metadata
                             * such as image format
                             */
                                byte[] headerBytes = ByteBuffer.allocate((Integer.SIZE + Short.SIZE) / 8).putInt(bufferStream.size()).putShort((short)deviceRotation).array();
                                synchronized (streamSocket) {
                                    outputStream.write(headerBytes);
                                    bufferStream.writeTo(outputStream);
                                    outputStream.flush();
                                }
                                bufferStream.close();


                            } catch (IOException ex) {
                                Log.e("STREAM", "Error sending frame: " + ex.toString());
                            }
                        }
                    }).start();
                } catch (Exception ex){
                    Log.e("STREAM", "Error creating PNG: "+ex.getMessage());

                }

            }

        }
    }

    @Override
    public void onBatteryChargingStateReceived(final Device.BatteryChargingState batteryChargingState) {
        Log.i("ExampleApp", "Battery charging state received!");

        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (originalChargingIndicatorColor == null) {
                    originalChargingIndicatorColor = chargingIndicator.getColorFilter();
                }
                switch (batteryChargingState) {
                    case FAULT:
                    case FAULT_HEAT:
                        chargingIndicator.setColorFilter(Color.RED);
                        chargingIndicator.setVisibility(View.VISIBLE);
                        break;
                    case FAULT_BAD_CHARGER:
                        chargingIndicator.setColorFilter(Color.DKGRAY);
                        chargingIndicator.setVisibility(View.VISIBLE);
                    case MANAGED_CHARGING:
                        chargingIndicator.setColorFilter(originalChargingIndicatorColor);
                        chargingIndicator.setVisibility(View.VISIBLE);
                        break;
                    case NO_CHARGING:
                    default:
                        chargingIndicator.setVisibility(View.GONE);
                        break;
                }
            }
        });
    }

    @Override
    public void onBatteryPercentageReceived(final byte percentage){
        Log.i("ExampleApp", "Battery percentage received!");


        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                levelTextView.setText(String.valueOf((int) percentage) + "%");
            }
        });


    }

    // StreamDelegate method
    public void onFrameReceived(Frame frame){
        Log.v("ExampleApp", "Frame received!");

        if (currentTuningState != Device.TuningState.InProgress){
            frameProcessor.processFrame(frame);
        }
    }
}