package com.androidfung.drminfo.ui.mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.androidfung.drminfo.R;
import com.google.android.material.snackbar.Snackbar;

public class MyHandlers {
    private static final String TAG = MyHandlers.class.getSimpleName();

    private boolean copyToClipboard(View view, String label, String text) {

        Snackbar.make(view, text, Snackbar.LENGTH_LONG).setAction(R.string.label_copy, v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);

        }).show();

        return true;
    }


    public boolean copyToClipboard(View v) {
        String label = String.valueOf(v.getId());
        String text = v.toString();

        if (v instanceof TextView){
            text = ((TextView) v).getText().toString();
        }

        return copyToClipboard(v, label, text);
    }

}
