package com.huhx0015.thermalgram.Intent;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.huhx0015.flirhotornot.R;

/** -----------------------------------------------------------------------------------------------
 *  [TGShareIntent] CLASS
 *  PROGRAMMER: Michael Yoon Huh
 *  DESCRIPTION: TGShareIntent class is used to provide functions to share data with external
 *  activities.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGShareIntent {

    /** INTENT FUNCTIONALITY ___________________________________________________________________ **/

    // shareThermalIntent(): Prepares an Intent to share thermal image data with external activities.
    public static void shareThermalIntent(String fileName, Context context) {



        //Bitmap b = BitmapFactory.decodeResource(context.getResources(), R.drawable.userimage);

        // Sets up an Intent to share the shortcut data with external activities.
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, "(THERMALGRAM):\n" + message);
        sendIntent.setType("image/jpeg");
        context.startActivity(Intent.createChooser(sendIntent, "Share my Thermalgram with:"));

        /*
        Bitmap b =BitmapFactory.decodeResource(getResources(),R.drawable.userimage);
            Intent share = new Intent(Intent.ACTION_SEND);
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(),
                    b, "Title", null);
            Uri imageUri =  Uri.parse(path);
            share.putExtra(Intent.EXTRA_STREAM, imageUri);
            startActivity(Intent.createChooser(share, "Select"));

         */
    }
}