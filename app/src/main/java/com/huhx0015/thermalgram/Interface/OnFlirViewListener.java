package com.huhx0015.thermalgram.Interface;

/**
 * -------------------------------------------------------------------------------------------------
 * [OnFlirViewListener] INTERFACE
 * DESCRIPTION: An interface class used primarily between TGMainActivity and TGFlirFragment classes.
 * -------------------------------------------------------------------------------------------------
 */
public interface OnFlirViewListener {

    // disconnectFlirDevice(): Used to signal the attached fragment to disconnect the FLIR ONE
    // device.
    void disconnectFlirDevice();
}
