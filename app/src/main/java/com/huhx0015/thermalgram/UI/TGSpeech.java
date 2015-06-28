package com.huhx0015.thermalgram.UI;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

/** -----------------------------------------------------------------------------------------------
 *  [TGSpeech] CLASS
 *  PROGRAMMER: Michael Yoon Huh (Huh X0015)
 *  DESCRIPTION: TGSpeech contains TextToSpeech related functionality.
 *  -----------------------------------------------------------------------------------------------
 */
public class TGSpeech {

    /** CLASS VARIABLES ________________________________________________________________________ **/

    // SPEECH VARIABLES
    private static TextToSpeech speechInstance; // Used to reference the TTS instance for the class.

    /** SPEECH FUNCTIONALITY ___________________________________________________________________ **/

    // startSpeech(): Activates voice functionality to say something.
    public static void startSpeech(final String script, final Context context) {

        // Creates a new timer thread for temporarily pausing the app before starting speech output.
        // Required for speech to begin immediately at activity launch.
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // Creates a new instance to begin TextToSpeech functionality.
                speechInstance = new TextToSpeech(context, new TextToSpeech.OnInitListener() {

                    @Override
                    public void onInit(int status) {

                        // The script is spoken by the TTS system.
                        speechInstance.speak(script, TextToSpeech.QUEUE_FLUSH, null);
                    }
                });
            }
        }, 50);
    }

    // getPhrase(): Returns a phrase based on the value passed.
    public static String getPhrase(double value) {

        String speechScript; // References the speech script.

        // RATING: 0 - 1.99:
        if ( (value >= 0) && (value < 2)) { speechScript = "Ice, ice, baby..."; }

        // RATING: 2 - 2.99:
        else if ( (value >= 2) && (value < 3)) { speechScript = "That's cold."; }

        // RATING: 3 - 3.99:
        else if ( (value >= 3) && (value < 4)) { speechScript = "Getting warmer..."; }

        // RATING: 4 - 4.99:
        else if ( (value >= 4) && (value < 5)) { speechScript = "I feel the heat..."; }

        // RATING: 5+:
        else if (value == 5) { speechScript = "It's gettin' hot in here..."; }

        // IMPOSSIBLE:
        else { speechScript = "It's over 9000!"; }

        return speechScript;
    }
}
