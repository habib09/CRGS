package com.mpower.clientcollection.utilities;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by ratna on 6/19/16.
 */

public class GsonUtils {
    /**
     * Converting HashMap to Gson string
     */
    public static String hashMapToGson(HashMap<String, String> map){
        return new Gson().toJson(map);
    }

    /**
     * Converting Object to Gson string
     */
    public static String objectToGson(Object object){
        return new Gson().toJson(object);
    }
}
