package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;
import android.util.Log;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import androidx.databinding.ObservableArrayMap;

public class WidevineViewModel extends MediaDrmViewModel {

    private final static String TAG = WidevineViewModel.class.getSimpleName();

    public WidevineViewModel(Application application) {
        super(application);
        mMediaDrm = MediaDrmFactory.getMediaDrm(MediaDrmFactory.WIDEVINE);
    }

    @Override
    protected void prepareProperties(MediaDrm mediaDrm) {
        super.prepareProperties(mediaDrm);
        ObservableArrayMap<String, Object> map = mProperties.getValue();
        if (mMediaDrm != null) {
            try {
                String[] wvKeys = getApplication().getResources().getStringArray(R.array.widevine_string_property_keys);

                for (String wvKey : wvKeys) {
                    String value = mediaDrm.getPropertyString(wvKey);
                    map.put(wvKey, value);
                }
            } catch (IllegalStateException | IllegalArgumentException ignored) {
            }
        }
        mProperties.setValue(map);


//        myRef.setValue("Hello, World!");


        Map<String, Object> record = new HashMap<>(map);
//        ObservableMap<String, Object> record = widevineFragment.getProperties();
//        if (record != null) {
        record.put("brand", Build.BRAND);
        record.put("model", Build.MODEL);
        record.put("hardware", Build.HARDWARE);
        record.put("fingerprint", Build.FINGERPRINT);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("devices");

        auth.signInAnonymously().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                Log.d(TAG, "signInAnonymously:success");
//                FirebaseUser user = auth.getCurrentUser();
                myRef.child(auth.getUid()).setValue(record)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
                // Add a new document with a generated ID
                db.collection("devices").document(auth.getUid())
                        .set(record)
                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added"))
                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            } else {
                // If sign in fails, display a message to the user.
                Log.w(TAG, "signInAnonymously:failure", task.getException());

            }
        });


    }
//    }

}
