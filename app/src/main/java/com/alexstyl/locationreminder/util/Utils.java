package com.alexstyl.locationreminder.util;

import android.content.Context;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * <p>Created by alexstyl on 28/02/15.</p>
 */
public class Utils {
    /**
     * Checks if the device is currently connected to the webz!
     *
     * @param context The context to use
     * @return Whether the device is online or not... duh
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        if (ni == null) {
            // There are no active networks.
            return false;
        }
        return ni.isConnectedOrConnecting();
    }

    /**
     * @param context The context to use
     * @param enableSound True will enable sound, false will set to vibrate
     */
    public static void muteDevice(Context context, boolean enableSound) {
        if (enableSound) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING);

            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, AudioManager.FLAG_SHOW_UI);
        } else {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);

        }
    }
}

