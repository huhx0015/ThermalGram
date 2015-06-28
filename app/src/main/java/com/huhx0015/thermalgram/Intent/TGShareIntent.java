package com.huhx0015.thermalgram.Intent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import java.io.ByteArrayOutputStream;

/** -----------------------------------------------------------------------------------------------
 *  [TGShareIntent] CLASS
 *  PROGRAMMER: Michael Yoon Huh
 *  DESCRIPTION: TGShareIntent class is used to provide functions to share data with external
 *  activities.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGShareIntent {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // LOGGING VARIABLES
    private static final String LOG_TAG = TGShareIntent.class.getSimpleName();

    /** INTENT FUNCTIONALITY ___________________________________________________________________ **/

    // shareThermalIntent(): Prepares an Intent to share thermal image data with external activities.
    public static void shareThermalIntent(String fileName, Context context) {

        // References the directory path where the image is stored.
        final String uploadFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() + "/";
        String fullFilePath = uploadFilePath + "" + fileName; // Sets the full file path.
        Bitmap thermalBitmap; // References the thermal bitmap.

        // Retrieves the bitmap data from the file
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            thermalBitmap = BitmapFactory.decodeFile(fullFilePath, options);
        }

        // Exception handler.
        catch (Exception e) {
            Log.e(LOG_TAG, "shareThermalIntent(): ERROR: File could not be found.");
            return;
        }

        // Checks to see if the thermalBitmap is null first.
        if (thermalBitmap != null) {

            // Prepares the thermal bitmap to be shared via an Intent.
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            thermalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                    thermalBitmap, "Thermalgram", null);
            Uri imageUri = Uri.parse(path);

            // Sets up an Intent to share the shortcut data with external activities.
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("image/jpg"); // Specifies that this is a image type.
            sendIntent.putExtra(Intent.EXTRA_TEXT, "THERMALGRAM: It's getting hot in here with my Thermalgram..!");
            sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
            sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(sendIntent, "Share my Thermalgram with:"));
        }
    }
}