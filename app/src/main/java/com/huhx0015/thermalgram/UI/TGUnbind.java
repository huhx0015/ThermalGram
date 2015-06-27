package com.huhx0015.thermalgram.UI;

import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

/** -----------------------------------------------------------------------------------------------
 *  [TGUnbind] CLASS
 *  DESCRIPTION: TGUnbind class is a class that contains methods which unbind View groups that are
 *  no longer needed by activities.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGUnbind {

    /** RECYCLE FUNCTIONALITY __________________________________________________________________ **/

    // unbindDrawables(): Unbinds all Drawable objects attached to the view layout by setting them
    // to null, freeing up memory resources and preventing Context-related memory leaks. This code
    // is borrowed from Roman Guy at www.curious-creature.org.
    public static void unbindDrawables(View view) {

        // If the View object's background is not null, a Callback is set to render them null.
        if (view.getBackground() != null) { view.getBackground().setCallback(null); }

        if (view instanceof ViewGroup && !(view instanceof AdapterView)) {

            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }

            ((ViewGroup) view).removeAllViews(); // Removes all View objects in the ViewGroup.
        }
    }
}
