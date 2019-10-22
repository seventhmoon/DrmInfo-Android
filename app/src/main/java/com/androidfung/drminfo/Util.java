package com.androidfung.drminfo;

import android.media.MediaDrm;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class Util {

    @RequiresApi(api = Build.VERSION_CODES.P)
    public static String toHdcpLevelString(@MediaDrm.HdcpLevel int maxHdcpLevel) {
        switch (maxHdcpLevel) {
            case MediaDrm.HDCP_LEVEL_UNKNOWN:
                return "Unknown";

            case MediaDrm.HDCP_NONE:
                return "None";

            case MediaDrm.HDCP_NO_DIGITAL_OUTPUT:
                return "No Digital Output";

            case MediaDrm.HDCP_V1:
                return "1";

            case MediaDrm.HDCP_V2_1:
                return "2.1";

            case MediaDrm.HDCP_V2_2:
                return "2.2";

            case MediaDrm.HDCP_V2:
                return "2";

            case MediaDrm.HDCP_V2_3:
                return "2.3";

            default:
                return null;
        }
    }
}
