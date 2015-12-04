package it.jaschke.alexandria.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;

public class NetworkState extends BroadcastReceiver {
    public NetworkState() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        Intent messageIntent = new Intent("networkStateChanged");
        // You can also include some extra data.
        if (activeNetwork!=null){
            messageIntent.putExtra("isConnected", activeNetwork.isConnected());
        }else{
            messageIntent.putExtra("isConnected", false);
        }

        LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent);
    }

}
