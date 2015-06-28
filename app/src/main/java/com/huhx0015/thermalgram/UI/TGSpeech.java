package com.huhx0015.thermalgram.UI;

import android.content.Context;
import android.os.Handler;
import android.speech.tts.TextToSpeech;

/**
 * Created by Michael Yoon Huh on 6/28/2015.
 */
public class TGSpeech {

    // SPEECH VARIABLES
    private static TextToSpeech speechInstance; // Used to reference the TTS instance for the class.

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
}
