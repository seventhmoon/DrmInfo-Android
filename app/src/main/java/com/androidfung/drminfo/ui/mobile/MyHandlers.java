package com.androidfung.drminfo.ui.mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.media.MediaDrm;
import android.os.Build;
import android.view.View;
import android.widget.TextView;

import com.androidfung.drminfo.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;

public class MyHandlers {
    private static final String TAG = MyHandlers.class.getSimpleName();

    private boolean copyToClipboard(View view, String label, String text) {

        Snackbar snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction(R.string.label_copy, v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);

        });
        snackbar.show();

        return snackbar.isShownOrQueued();
    }


    public boolean onLongClick(View v) {
        String label = String.valueOf(v.getId());
        String text = v.toString();

        if (v instanceof TextView){
            text = ((TextView) v).getText().toString();
        }

        return copyToClipboard(v, label, text);
    }


    @RequiresApi(api = Build.VERSION_CODES.P)
    private String toStringValue(@MediaDrm.HdcpLevel int maxHdcpLevel) {
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

//            case MediaDrm.HDCP_V2_3:
//                return "2.3";
//
            default:
                return null;
        }
    }
}
