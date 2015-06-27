package com.huhx0015.thermalgram.UI;

import android.content.Context;
import android.widget.Toast;

/** -----------------------------------------------------------------------------------------------
 *  [TGToast] CLASS
 *  PROGRAMMER: Michael Yoon Huh (Huh X0015)
 *  DESCRIPTION: TGToast contains functions that utilize the Toast message functionality.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGToast {

    /** TOAST FUNCTIONALITY ____________________________________________________________________ **/

    // toastyPopUp(): Creates and displays a Toast popup.
    public static void toastyPopUp(String message, Context con) {
        Toast.makeText(con, message, Toast.LENGTH_SHORT).show();
    }
}
