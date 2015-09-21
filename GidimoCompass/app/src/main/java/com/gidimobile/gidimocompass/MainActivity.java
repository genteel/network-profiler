package com.gidimobile.gidimocompass;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.gidimobile.gidimocompass.constants.RequestCodes;
import com.gidimobile.gidimocompass.data.User;
import com.gidimobile.gidimocompass.data.UserConnectivity;
import com.gidimobile.gidimocompass.receivers.CronJob;
import com.gidimobile.gidimocompass.util.Connectivity;

import java.util.Map;

public class MainActivity extends AppCompatActivity{
    CallbackManager callbackManager;
    public final String TAG = MainActivity.class.getSimpleName();
    private Firebase firebaseRef;
    AlarmManager alarmManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        FacebookSdk.sdkInitialize(this, RequestCodes.FACEBOOK);
        callbackManager = CallbackManager.Factory.create();
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        firebaseRef = new Firebase("https://intense-torch-4637.firebaseio.com/");
        setContentView(R.layout.activity_main);
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e("MainActivity", "" + loginResult.getAccessToken().getToken());
                firebaseRef.authWithOAuthToken("facebook", loginResult.getAccessToken().getToken(),
                        new Firebase.AuthResultHandler() {
                            @Override
                            public void onAuthenticated(final AuthData authData) {
                                firebaseRef.child("users").child(authData.getUid()).addListenerForSingleValueEvent(
                                        new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if (!dataSnapshot.exists()) {
                                                    createUser(authData);
                                                    Intent intent = new Intent(MainActivity.this, CronJob.class);
                                                    intent.putExtra("uid",authData.getUid());
                                                    Long time = System.currentTimeMillis()+(60*1000);
                                                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, (60 * 1000),
                                                            PendingIntent.getBroadcast(MainActivity.this, 5, intent, 0));
                                                } else {
                                                    Log.e("TAGEEIR","gh");
                                                    Intent intent = new Intent(MainActivity.this, CronJob.class);
                                                    intent.putExtra("uid", authData.getUid());
                                                    Long time = System.currentTimeMillis()+(60*1000);
                                                    alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, time, (60 * 1000),
                                                            PendingIntent.getBroadcast(MainActivity.this, 5, intent, 0));
                                                }
                                            }

                                            @Override
                                            public void onCancelled(FirebaseError firebaseError) {

                                            }
                                        }
                                );
                                //user.set((String) data.get("email"));
                                Log.e("MainActivity", "authentication successful. UID: " + authData.getProviderData().toString());
                            }

                            @Override
                            public void onAuthenticationError(FirebaseError firebaseError) {
                                Log.e("MainActivity", "authentication failed!" + firebaseError.getMessage());
                            }
                        });
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {
                Log.e("MainActivity", "FBauthentication failed!" + e.getMessage());
            }
        });
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout)
                findViewById(R.id.collapsing_toolbar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        collapsingToolbarLayout.setTitle("Network Monitoring");
        collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }




    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RequestCodes.FACEBOOK:
                callbackManager.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                break;
        }
    }

    public void createUser(final AuthData authData){
        Map<String, Object> data = authData.getProviderData();
        User user = new User();
        user.setUserName((String) data.get("email"));
        user.setUid(authData.getUid());
        NetworkInfo networkInfo = Connectivity.getNetworkInfo(this);
        final UserConnectivity userConnectivity = new UserConnectivity();
        userConnectivity.setTime(System.currentTimeMillis());
        userConnectivity.setSpeed(Connectivity.getNetworkSpeed(networkInfo.getType(), networkInfo.getSubtype()));
        firebaseRef.child("users").child(authData.getUid()).setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if(firebaseError!=null)
                    Log.e(TAG,"Status : "+firebaseError.getMessage());
                else{
                    Log.e(TAG,"Status : Successful");
                    firebaseRef.child("users").child(authData.getUid()).child("userConnectivities").push().setValue(userConnectivity);
                }
            }
        });
    }


}
