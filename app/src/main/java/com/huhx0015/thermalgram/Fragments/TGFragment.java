package com.huhx0015.thermalgram.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import com.huhx0015.thermalgram.Interface.OnFlirUpdateListener;
import com.huhx0015.thermalgram.Server.HttpFileUpload;
import com.huhx0015.thermalgram.Server.TGServer;
import com.huhx0015.thermalgram.UI.TGToast;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TGFragment extends Fragment implements Device.Delegate, FrameProcessor.Delegate {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    private static final String POSTURL = "http://50.62.57.6/~ibrahimkabil7/thermalgram/endpoint.php"; // Server URL

    // THREAD VARIABLES
    private Handler backgroundHandler = new Handler(); // Thread for handling background animation.

    // SAVE LOCATION
    Boolean isCaptureImage = false; // Used to determine if an image capture event is in progress.
    Boolean isSavingDone = false;
    String saveLocationPath = "";
    String currentImageFile = "";

    // FILR VARIABLES
    Boolean isFlirOn = false;
    Device flirDevice;
    private FrameProcessor frameProcessor;

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGFragment.class.getSimpleName();

    // SYSTEM VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.

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
        startStopThreads(true);  // Starts all threads.
    }

    @Override
    public void onPause() {
        super.onPause();
        Device.stopDiscovery();
        startStopThreads(false);  // Stops all threads.
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

                TGToast.toastyPopUp("Saving your thelfie...", currentActivity);
                Log.d(LOG_TAG, "setOnClickListener(): Capture button has been pressed.");
                isCaptureImage = true; // Indicates that the thermal frame should be saved.
            }

        });

        // UPLOAD Button: Defines the listener for the ImageButton object.
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // Uploads image to the server.
                TGServer.imageUploadFile(currentImageFile, currentActivity);
            }

        });
    }



    /** IMAGE FUNCTIONALITY ____________________________________________________________________ **/

    // saveImage():
    private void saveImage(RenderedImage renderedImage) {

        // Sets up the save location and the file formatting.
        saveLocationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        currentImageFile = "FLIROne-" + formatedDate + ".jpg";

        Log.d(LOG_TAG, "saveImage(): Save environment has been prepared.");

        try{

            String lastSavedPath = saveLocationPath + "/" + currentImageFile;
            renderedImage.getFrame().save(lastSavedPath, RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);
            //renderedImage.getFrame().save(lastSavedPath, RenderedImage.Palette.Iron, RenderedImage.ImageType.VisualJPEGImage);

            MediaScannerConnection.scanFile(currentActivity,
                    new String[] {saveLocationPath + "/" + currentImageFile}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                            Log.d(LOG_TAG, "saveImage(): Thermal image save has been successful. File has been saved as: " + currentImageFile);

                            isSavingDone = true; // Signals that the file has been saved.
                        }
                    });
        }

        catch (Exception e){
            Log.d(LOG_TAG, "saveImage(): Thermal image save has failed.");
            e.printStackTrace();
        }

    }


    private void updateThermalImageView(final Bitmap frame){
        currentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                thermalImage.setImageBitmap(frame);
            }
        });
    }

    /** FLIR EXTENSION FUNCTIONALITY ___________________________________________________________ **/

    @Override
    public void onTuningStateChanged(Device.TuningState tuningState) {

    }

    @Override
    public void onAutomaticTuningChanged(boolean b) {

    }

    // Called when the FLIR One device is connected.
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

    @Override
    public void onDeviceDisconnected(Device device) {

    }

    // Passes rendered images to delegate.
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

    /** INTERFACE FUNCTIONALITY ________________________________________________________________ **/

    // updateServer(): Attempts to upload the image to the server.
    private void updateServer(String fileName) {
        try { ((OnFlirUpdateListener) currentActivity).updateServer(fileName); }
        catch (ClassCastException cce) { } // Catch for class cast exception errors.
    }

    /** THREAD FUNCTIONALITY ___________________________________________________________________ **/


    // backgroundThread():
    private Runnable backgroundThread = new Runnable() {

        public void run() {

            if (isSavingDone) {

                uploadButton.setVisibility(View.VISIBLE);
                captureButton.setVisibility(View.GONE);

                isCaptureImage = false; // Resets the capture image value.
                isSavingDone = false; // Resets the saving done value.
            }

            backgroundHandler.postDelayed(this, 1000); // Updates the thread per 1000 ms.
        }
    };

    // startStopThreads(): Resumes or stops all threads.
    private void startStopThreads(Boolean isStart) {

        // Starts all threads.
        if (isStart == true) { backgroundHandler.postDelayed(backgroundThread, 1000); }

        // Stops all threads.
        else { backgroundHandler.removeCallbacks(backgroundThread); }
    }

}