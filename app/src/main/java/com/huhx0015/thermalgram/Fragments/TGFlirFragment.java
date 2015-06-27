package com.huhx0015.thermalgram.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huhx0015.flirhotornot.R;
import butterknife.ButterKnife;

/** -----------------------------------------------------------------------------------------------
 *  [TGFlirFragment] CLASS
 *  DESCRIPTION: GTNTutorialFragment class is a Fragment class that is used for displaying the FLIR
 *  camera preview image.
 *  -----------------------------------------------------------------------------------------------
 */

public class TGFlirFragment extends Fragment {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // SYSTEM VARIABLES
    private Activity currentActivity; // Used to determine the activity class this fragment is currently attached to.

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

        View sb_tutorial_fragment_view = (ViewGroup) inflater.inflate(R.layout.tg_flir_fragment, container, false);
        ButterKnife.inject(this, sb_tutorial_fragment_view); // ButterKnife view injection initialization.

        setUpLayout(); // Sets up the layout for the fragment.

        return sb_tutorial_fragment_view;
    }

    // onDestroyView(): This function runs when the screen is no longer visible and the view is
    // destroyed.
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.reset(this); // Sets all injected views to null.
    }

    /** LAYOUT FUNCTIONALITY ___________________________________________________________________ **/

    // setUpLayout(): Sets up the layout for the fragment.
    private void setUpLayout() {
    }

}