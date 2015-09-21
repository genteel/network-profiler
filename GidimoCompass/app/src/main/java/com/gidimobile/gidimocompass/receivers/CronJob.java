package com.gidimobile.gidimocompass.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.facebook.network.connectionclass.DeviceBandwidthSampler;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.gidimobile.gidimocompass.data.NetworkSpeed;
import com.gidimobile.gidimocompass.data.UserConnectivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Ocheja Patrick Ileanwa on 2015-09-17.
 */
public class CronJob extends BroadcastReceiver {

    public static final String TAG = CronJob.class.getSimpleName();
    private ConnectionClassManager mConnectionClassManager;
    private DeviceBandwidthSampler mDeviceBandwidthSampler;
    private ConnectionChangedListener mListener;
    private Firebase firebaseRef;
    private String uid;
    private Context context;
    private long lastSent =System.currentTimeMillis();

    private String mURL = "http://gidimo.com/wp-content/uploads/2014/03/gidilogoheader1.png";
    private int mTries = 0;
    private ConnectionQuality mConnectionClass = ConnectionQuality.UNKNOWN;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        Log.e("TAGEEIR","onRecieve");
        Firebase.setAndroidContext(context);
        firebaseRef = new Firebase("https://intense-torch-4637.firebaseio.com/");
        mConnectionClassManager = ConnectionClassManager.getInstance();
        mDeviceBandwidthSampler = DeviceBandwidthSampler.getInstance();
        if(mListener!=null){
            Log.e(TAG, "listener removed!");
            mConnectionClassManager.remove(mListener);
        }
        mListener = new ConnectionChangedListener();
        mConnectionClassManager.register(mListener);
        uid = intent.getExtras().getString("uid");
        Log.e(uid, uid);
        new DownloadImage().execute(mURL);
        Toast.makeText(context,"Alarm fired",Toast.LENGTH_LONG).show();
    }

    /**
     * Listener to update the UI upon connectionclass change.
     */
    private class ConnectionChangedListener
            implements ConnectionClassManager.ConnectionClassStateChangeListener {

        @Override
        public void onBandwidthStateChange(ConnectionQuality bandwidthState) {
            mConnectionClass = bandwidthState;
            Log.e(TAG, "Last sent: "+lastSent+" Current time:"+System.currentTimeMillis());
            if((System.currentTimeMillis()-lastSent)>(1000*50)){
                lastSent = System.currentTimeMillis();
                UserConnectivity userConnectivity = new UserConnectivity();
                userConnectivity.setTime(lastSent);
                switch (mConnectionClass.toString()){
                    case "EXCELLENT":
                        userConnectivity.setSpeed(NetworkSpeed.EXCELLENT);
                        break;
                    case "GOOD":
                        userConnectivity.setSpeed(NetworkSpeed.GOOD);
                        break;
                    case "MODERATE":
                        userConnectivity.setSpeed(NetworkSpeed.MODERATE);
                        break;
                    case "POOR":
                        userConnectivity.setSpeed(NetworkSpeed.POOR);
                        break;
                    default:
                        userConnectivity.setSpeed(NetworkSpeed.UNKNOWN);
                        break;
                }
                firebaseRef.child("users").child(uid).child("userConnectivities").push().setValue(userConnectivity, new Firebase.CompletionListener() {
                    @Override
                    public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                        Log.e("onRec", firebase.getKey());
                    }
                });

            }else{
                Log.e(TAG,"Already sent to server");
            }
        }
    }

    /**
     * AsyncTask for handling downloading and making calls to the timer.
     */
    private class DownloadImage extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            mDeviceBandwidthSampler.startSampling();
        }

        @Override
        protected Void doInBackground(String... url) {
            String imageURL = url[0];
            try {
                // Open a stream to download the image from our URL.
                URLConnection connection = new URL(imageURL).openConnection();
                connection.setUseCaches(false);
                connection.connect();
                InputStream input = connection.getInputStream();
                try {
                    byte[] buffer = new byte[1024];

                    // Do some busy waiting while the stream is open.
                    while (input.read(buffer) != -1) {
                    }
                } finally {
                    input.close();
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while downloading image.");
                    UserConnectivity userConnectivity = new UserConnectivity();
                    userConnectivity.setTime(System.currentTimeMillis());
                    userConnectivity.setSpeed(NetworkSpeed.NOT_AVAILABLE);
                    firebaseRef.child("users").child(uid).child("userConnectivities").push().setValue(userConnectivity, new Firebase.CompletionListener() {
                        @Override
                        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                            Log.e("onRec", firebase.getKey());
                        }
                    });
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            mDeviceBandwidthSampler.stopSampling();
            // Retry for up to 10 times until we find a ConnectionClass.
            if (mConnectionClass == ConnectionQuality.UNKNOWN && mTries < 10) {
                mTries++;
                new DownloadImage().execute(mURL);
            }
        }
    }
}
