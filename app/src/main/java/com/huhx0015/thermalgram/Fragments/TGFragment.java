package com.huhx0015.thermalgram.Fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.flir.flironesdk.Device;
import com.flir.flironesdk.Frame;
import com.flir.flironesdk.FrameProcessor;
import com.flir.flironesdk.RenderedImage;
import com.huhx0015.flirhotornot.R;
import com.huhx0015.thermalgram.Interface.OnFlirViewListener;
import com.huhx0015.thermalgram.Server.TGServer;
import com.huhx0015.thermalgram.UI.TGToast;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class TGFragment extends Fragment implements Device.Delegate, FrameProcessor.Delegate, OnFlirViewListener {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // FLIR VARIABLES
    private Boolean isFlirOn = false; // Used to determine if the FLIR One device is connected or not.
    private Device flirDevice; // References the connected FLIR One device.
    private FrameProcessor frameProcessor; // Responsible for processing the frames from the FLIR One device.

    // IMAGE VARIABLES
    private Boolean isCaptureImage = false; // Used to determine if an image capture event is in progress.
    private Boolean isSavingDone = false; // Used to determine if the thermal frame has been saved.
    private int imageCounter = 0; // Used to count the number of images taken.
    private String currentImageFile = ""; // References the current image file name.
    private String saveLocationPath = ""; // References the save location path.

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGFragment.class.getSimpleName();

    // SYSTEM VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.

    // THREAD VARIABLES
    private Handler backgroundHandler = new Handler(); // Thread for handling background animation.

    // VIEW INJECTION VARIABLES
    @InjectView(R.id.tg_capture_button) ImageButton captureButton;
    @InjectView(R.id.tg_upload_button) ImageButton uploadButton;
    @InjectView(R.id.tg_thermal_image) ImageView thermalImage;

    /** FRAGMENT FUNCTIONALITY _________________________________________________________________ **/

    // onAttach(): The initial function that is called when the Fragment is run. The activity is
    // attached to the fragment.
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.currentActivity = activity; // Sets the currentActivity to attached activity object.
    }

    // onCreateView(): Creates and returns the view hierarchy associated with the fragment.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View tg_fragment_view = (ViewGroup) inflater.inflate(R.layout.tg_fragment, container, false);
        ButterKnife.inject(this, tg_fragment_view); // ButterKnife view injection initialization.

        // FLIR
        RenderedImage.ImageType blendedType = RenderedImage.ImageType.BlendedMSXRGBA8888Image;
        //RenderedImage.ImageType blendedType = RenderedImage.ImageType.VisualJPEGImage;
        frameProcessor = new FrameProcessor(currentActivity, this, EnumSet.of(blendedType));
        frameProcessor.setImagePalette(RenderedImage.Palette.Iron);

        setUpLayout(); // Sets up the layout for the fragment.

        return tg_fragment_view;
    }

    // onDestroyView(): This function runs when the screen is no longer visible and the view is
    // destroyed.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this); // Sets all injected views to null.
    }

    @Override
    public void onResume() {
        super.onResume();
        Device.startDiscovery(currentActivity, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Device.stopDiscovery();
    }

    /** LAYOUT FUNCTIONALITY ___________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the fragment.
    private void setUpLayout() {

        setUpButtons(); // Sets up the button listeners for the fragment.
    }

    // setUpButtons(): Sets up the button listeners for the fragment.
    private void setUpButtons() {

        // IMAGE CAPTURE Button: Defines the listener for the ImageButton object.
        captureButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(LOG_TAG, "setOnClickListener(): Capture button has been pressed.");

                // Checks to see if the FLIR One device has been connected or not.
                if (isFlirOn) {

                    TGToast.toastyPopUp("Saving your thelfie...", currentActivity);

                    // Indicates that the current thermal frame should be saved.
                    isCaptureImage = true;

                    imageCounter++; // Increments the image count.
                }

                // Informs the user to connect the FLIR One device.
                else { TGToast.toastyPopUp("Please connect the FLIR One device.", currentActivity); }
            }

        });

        // UPLOAD Button: Defines the listener for the ImageButton object.
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                Log.d(LOG_TAG, "setOnClickListener(): Upload button has been pressed.");

                // Checks to see if the FLIR One device has been connected or not.
                if (isFlirOn) {

                    TGToast.toastyPopUp("Uploading your thelfie to the server...", currentActivity);

                    // Uploads the saved image file to the web server in the background.
                    TGUploadImageTask uploadTask = new TGUploadImageTask();
                    uploadTask.execute();
                }

                // Informs the user to connect the FLIR One device.
                else { TGToast.toastyPopUp("Please connect the FLIR One device.", currentActivity);  }
            }

        });
    }

    /** IMAGE FUNCTIONALITY ____________________________________________________________________ **/

    // saveImage(): Saves the current rendered frame to the local storage in the device's default
    // Pictures folder.
    private void saveImage(RenderedImage renderedImage) {

        // Sets up the save location and the file formatting.
        saveLocationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        currentImageFile = "Thermalgram-" + formatedDate + "-" + imageCounter + ".jpg" ;

        Log.d(LOG_TAG, "saveImage(): Save environment has been prepared.");

        // Attempts to save the file to local storage.
        try {

            String lastSavedPath = saveLocationPath + "/" + currentImageFile;
            renderedImage.getFrame().save(lastSavedPath, RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);

            MediaScannerConnection.scanFile(currentActivity,
                    new String[] {saveLocationPath + "/" + currentImageFile}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        // onScanCompleted(): Runs when media scan has concluded.
                        @Override
                        public void onScanCompleted(String path, Uri uri) {

                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            Log.d(LOG_TAG, "saveImage(): Thermal image save has been successful. File has been saved as: " + currentImageFile);

                            isSavingDone = true; // Signals that the file has been saved.

                            // DEBUG:
                            // Uploads the saved image file to the web server in the background.
                            TGUploadImageTask uploadTask = new TGUploadImageTask();
                            uploadTask.execute();

                        }
                    });
        }

        // Exception handler.
        catch (Exception e){
            Log.d(LOG_TAG, "saveImage(): Thermal image save has failed.");
            e.printStackTrace();
        }
    }

    /** INTERFACE FUNCTIONALITY ________________________________________________________________ **/

    // disconnectFlirDevice(): Disconnects the FLIR One device.
    @Override
    public void disconnectFlirDevice() {
        Device.stopDiscovery();
    }

    /** FLIR EXTENSION FUNCTIONALITY ___________________________________________________________ **/

    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {

    }

    @Override
    public void onAutomaticTuningChanged(boolean b) {

    }

    // onDeviceConnected(): Called when the FLIR One device is connected.
    @Override
    public void onDeviceConnected(Device device) {

        flirDevice = device;
        isFlirOn = true;

        // Starts capturing frames from camera.
        device.startFrameStream(new Device.StreamDelegate() {

            @Override
            public void onFrameReceived(Frame frame) {
                frameProcessor.processFrame(frame);
            }
        });
    }

    // onDeviceDisconnected(): This method runs when the FLIR One device has been disconnected.
    @Override
    public void onDeviceDisconnected(Device device) {
        isFlirOn = false; // Indicates that the FLIR One device has been disconnected.
    }

    // onFrameProcessed(): Passes rendered images to delegate.
    @Override
    public void onFrameProcessed(RenderedImage renderedImage) {

        // NOTE: Using AR88 color profile may be what could be causing crashes on older phones!
        final Bitmap thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.ARGB_8888);
        //final Bitmap thermalBitmap = Bitmap.createBitmap(renderedImage.width(), renderedImage.height(), Bitmap.Config.RGB_565);

        thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));

        // Runs on the UI thread.
        currentActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                thermalImage.setImageBitmap(thermalBitmap);
            }
        });

        // If the user has initiated a capture image event, the current thermal image frame will
        // be saved.
        if (isCaptureImage) {

            //TGToast.toastyPopUp("onFrameProcessed(): Saving current thermal frame...", currentActivity);
            Log.d(LOG_TAG, "onFrameProcessed(): Saving current thermal frame...");

            // NOTE: Should be moved to onFrameReceived callback.
            saveImage(renderedImage); // Attempts to save the current thermal image frame.
        }
    }

    /** ASYNCTASK FUNCTIONALITY ________________________________________________________________ **/

    /**
     * --------------------------------------------------------------------------------------------
     * [TGUploadImageTask] CLASS
     * PROGRAMMER: Michael Yoon Huh (HUHX0015)
     * DESCRIPTION: This is an AsyncTask-based class that uploads the thermal image to the web
     * server in the background.
     * --------------------------------------------------------------------------------------------
     */

    public class TGUploadImageTask extends AsyncTask<String, Integer, String> {

        // onPostExecute(): This method runs after the AsyncTask has finished running.
        @Override
        protected void onPostExecute(String result) {
            Log.d(LOG_TAG, "Image upload task complete.");
        }

        // doInBackground(): This method constantly runs in the background while AsyncTask is
        // running.
        @Override
        protected String doInBackground(String... params) {

            // Uploads the image to the web server in the background.
            TGServer.imageUploadFile(currentImageFile);
            return null;
        }
    }
}