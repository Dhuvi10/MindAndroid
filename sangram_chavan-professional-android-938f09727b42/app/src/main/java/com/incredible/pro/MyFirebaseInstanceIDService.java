

package com.incredible.pro;
/**
 * Created by VARUN on 01/01/19.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.incredible.pro.interfacess.Consts;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "MyPrefs" ;
    private static final String TAG = "MyFirebaseIIDService";

    @Override
    public void onTokenRefresh() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.e(TAG, "Refreshed token: " + refreshedToken);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        editor.putString(Consts.DEVICE_TOKEN, refreshedToken);
        editor.commit();
        sendRegistrationToServer(refreshedToken);
        SharedPreferences userDetails = MyFirebaseInstanceIDService.this.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        Log.e(TAG, "my token: " + userDetails.getString(Consts.DEVICE_TOKEN,""));


    }

    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.


    }
}
