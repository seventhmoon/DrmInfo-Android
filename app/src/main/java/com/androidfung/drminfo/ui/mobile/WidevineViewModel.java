package com.androidfung.drminfo.ui.mobile;

import android.app.Application;
import android.media.MediaDrm;
import android.os.Build;
import android.util.Log;

import com.androidfung.drminfo.MediaDrmFactory;
import com.androidfung.drminfo.R;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInAnonymously().addOnCompleteListener(task -> writeIntoToFirebase(task, map));

    }
//    }
    private void writeIntoToFirebase(Task<AuthResult> task, Map<String, Object> record){
        if (task.isSuccessful()) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("devices");
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
            // Sign in success, update UI with the signed-in user's information
            Log.d(TAG, "signInAnonymously:success");
//                FirebaseUser user = auth.getCurrentUser();
            myRef.child(Build.BRAND).child(Build.HARDWARE).child(String.valueOf(Build.VERSION.SDK_INT)).child("widevine").setValue(record)
                    .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added"))
                    .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));
            // Add a new document with a generated ID
//                db.collection("devices").document(auth.getUid())
//                        .set(record)
//                        .addOnSuccessListener(documentReference -> Log.d(TAG, "DocumentSnapshot added"))
//                        .addOnFailureListener(e -> Log.w(TAG, "Error adding document", e));

        } else {
            // If sign in fails, display a message to the user.
            Log.w(TAG, "signInAnonymously:failure", task.getException());

        }
    }

}
