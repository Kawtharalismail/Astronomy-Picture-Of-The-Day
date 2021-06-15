package com.barmej.apod.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class NetworkUtils {
    private String url="https://api.nasa.gov/planetary/apod?api_key=fSL8o6mGokdxfCwo0yYhHonqU9XzKoM5lrfa3HlW";
    private String dateEndPoint="";
    private static Context mContext;
    private static NetworkUtils sInstance;
    private RequestQueue mRequestQueue;

    public static synchronized NetworkUtils getInstance(Context context) {
        if(sInstance==null)
        { sInstance=new NetworkUtils(context);
        }
        return sInstance;
    }

    private NetworkUtils(Context context){
        mContext=context.getApplicationContext();
        mRequestQueue=getRequestQueue();
    }

    public RequestQueue getRequestQueue(){
        if(mRequestQueue==null)
            mRequestQueue= Volley.newRequestQueue(mContext);
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> request){
        getRequestQueue().add(request);
    }

    public void cancelRequests(String tags){
        getRequestQueue().cancelAll(tags);
    }

    public String getAstronomyUri()
    {
        return url;
    }

}
