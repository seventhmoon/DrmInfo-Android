package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.media.UnsupportedSchemeException;
import androidx.annotation.IntDef;
import androidx.annotation.Keep;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

/**
 * Created by fung on 11/13/2015.
 */
@Keep
public class MediaDrmFactory {
    private static final UUID WIDEVINE_UUID = new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL);
    private static final UUID PLAYREADY_UUID = new UUID(0x9A04F07998404286L, 0xAB92E65BE0885F95L);
    private static final UUID MARLIN_UUID = new UUID(0x5E629AF538DA4063L, 0x897797FFBD9902D4L);
    private static final UUID VERIMATRIX_UUID = new UUID(0x9A27DD82FDE24725L, 0x8CBC4234AA06EC09L);
    private static final UUID CLEARKEY_UUID = new UUID(0x1077EFECC0B24D02L, 0xACE33C1E52E2FB4BL);
    private static final UUID PRIMETIME_UUID = new UUID(0xF239E769EFA34850L, 0x9C16A903C6932EFBL);
    private static final UUID CISCO_UUID = new UUID(0xA68129D3575B4F1AL, 0x9CBA3223846CF7C3L);
    private static final UUID DIVX_UUID = new UUID(0x35BF197B530E42D7L, 0x8B651B4BF415070FL);
    private static final UUID FAIRPLAY_UUID = new UUID(0x94CE86FB07FF4F43L, 0xADB893D2FA968CA2L);

    //Define the list of accepted constants
    @IntDef({PLAYREADY, WIDEVINE, MARLIN, VERIMATRIX, CLEARKEY, PRIMETIME})

    //Tell the compiler not to store annotation data in the .class file
    @Retention(RetentionPolicy.SOURCE)

    //Declare the NavigationMode annotation
    public @interface Provider {
    }

    //Declare the constants
    public static final int PLAYREADY = 0;
    public static final int WIDEVINE = 1;
    public static final int MARLIN = 2;
    public static final int VERIMATRIX = 3;
    public static final int CLEARKEY = 4;
    public static final int PRIMETIME = 5;

    public static MediaDrm getMediaDrm(@Provider int provider) {
        try {
            switch (provider) {
                case PLAYREADY:
                    return new MediaDrm(PLAYREADY_UUID);
                case WIDEVINE:
                    return new MediaDrm(WIDEVINE_UUID);
                case MARLIN:
                    return new MediaDrm(MARLIN_UUID);
                case VERIMATRIX:
                    return new MediaDrm(VERIMATRIX_UUID);
                case CLEARKEY:
                    return new MediaDrm(CLEARKEY_UUID);
                case PRIMETIME:
                    return new MediaDrm(PRIMETIME_UUID);
            }
        } catch (UnsupportedSchemeException e) {
//            e.printStackTrace();
        }
        return null;
    }
}
