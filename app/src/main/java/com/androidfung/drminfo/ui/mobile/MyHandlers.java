package com.androidfung.drminfo.ui.mobile;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.androidfung.drminfo.R;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.widget.AppCompatTextView;

public class MyHandlers {
    private static final String TAG = MyHandlers.class.getSimpleName();

    public void copyToClipboard(View view, String label, String text) {


        Snackbar.make(view, text, Snackbar.LENGTH_INDEFINITE).setAction(R.string.label_copy, v -> {
            ClipboardManager clipboard = (ClipboardManager)
                    v.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

            ClipData clip = ClipData.newPlainText(label, text);
            clipboard.setPrimaryClip(clip);
//            Toast.makeText(v.getContext(), text, Toast.LENGTH_SHORT).show();
        }).show();

    }


    public void copyToClipboard(View v) {
        String label = String.valueOf(v.getId());
        String text = v.toString();

        if (v instanceof TextView){
            text = ((TextView) v).getText().toString();
            Log.d(TAG, "TextView: " + text);
        }else {
            Log.d(TAG, "View: " + text);
        }

        copyToClipboard(v, label, text);
    }

}
