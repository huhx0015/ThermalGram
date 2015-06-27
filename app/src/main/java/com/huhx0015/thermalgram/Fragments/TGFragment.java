package com.huhx0015.thermalgram.Fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import com.huhx0015.thermalgram.UI.TGToast;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TGFragment extends Fragment implements Device.Delegate, FrameProcessor.Delegate {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // SAVE LOCATION
    Boolean isCaptureImage = false; // Used to determine if an image capture event is in progress.
    String saveLocationPath = "";

    // FILR VARIABLES
    Device flirDevice;
    private FrameProcessor frameProcessor;

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGFragment.class.getSimpleName();

    // SYSTEM VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.

    // VIEW INJECTION VARIABLES
    @InjectView(R.id.tg_capture_button) ImageButton captureButton;
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

                TGToast.toastyPopUp("Saving your thelfie...", currentActivity);
                Log.d(LOG_TAG, "setOnClickListener(): Capture button has been pressed.");
                isCaptureImage = true; // Indicates that the thermal frame should be saved.
            }

        });
    }

    /** IMAGE FUNCTIONALITY ____________________________________________________________________ **/

    private void saveImage(RenderedImage renderedImage) {

        //saveLocationPath = currentActivity.getFilesDir() + "/pictures/";
        saveLocationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ssZ", Locale.getDefault());
        String formatedDate = sdf.format(new Date());
        String fileName = "FLIROne-" + formatedDate + ".jpg";

        Log.d(LOG_TAG, "saveImage(): Save environment has been prepared.");


        try{

            String lastSavedPath = saveLocationPath + "/" + fileName;
            renderedImage.getFrame().save(lastSavedPath, RenderedImage.Palette.Iron, RenderedImage.ImageType.BlendedMSXRGBA8888Image);

            MediaScannerConnection.scanFile(currentActivity,
                    new String[] {saveLocationPath + "/" + fileName}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {

                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Log.d(LOG_TAG, "saveImage(): Thermal image save has been successful. File has been saved as: " + fileName);
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

        thermalBitmap.copyPixelsFromBuffer(ByteBuffer.wrap(renderedImage.pixelData()));

        // Runs on the UI thread.
        currentActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                // Sets the bitmap into the ImageView object.
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

            // Indicates that the concurrent thermal images should not be saved. This is to prevent
            // multiple images from saving,
            isCaptureImage = false;
        }
    }

}