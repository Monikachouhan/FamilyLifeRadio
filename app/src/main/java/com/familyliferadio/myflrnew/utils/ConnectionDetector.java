package com.familyliferadio.myflrnew.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class ConnectionDetector {
    private Context _context;
    private static ConnectionDetector connectionDetector = null;

    private ConnectionDetector(Context context) {
        this._context = context;
    }

    public static ConnectionDetector getInstance(Context context) {
        if (connectionDetector == null)
            connectionDetector = new ConnectionDetector(context);
        return connectionDetector;
    }

    public boolean isConnectingToInternet() {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (NetworkInfo anInfo : info)
                    if (anInfo.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }
}
