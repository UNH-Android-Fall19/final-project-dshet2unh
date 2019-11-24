package com.example.snapit.constant;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;

import java.util.Random;

public class AppConstant {

    public static boolean isInternetConnected(Context context) {
        ConnectivityManager connectivitymanager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected;
        assert connectivitymanager != null;
        Network activeNetwork = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            activeNetwork = connectivitymanager.getActiveNetwork();
        }

        isConnected = activeNetwork != null;

        return isConnected;
    }


    public static String getRandomId(){
        String randomChar = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder stringBuilder = new StringBuilder();

        Random random = new Random();
        while (stringBuilder.length() < 5) { // length of the random string.
            int index = (int) (random.nextFloat() * randomChar.length());
            stringBuilder.append(randomChar.charAt(index));
        }

        return stringBuilder.toString();
    }
}
