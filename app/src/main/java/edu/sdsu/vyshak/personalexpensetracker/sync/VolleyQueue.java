package edu.sdsu.vyshak.personalexpensetracker.sync;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * This class handles the HTTP requests.
 * Created by Vyshak on 4/25/2017.
 */

public class VolleyQueue {
    private static VolleyQueue mInstance;
    private RequestQueue
            mRequestQueue;
    private static Context mContext;

    private VolleyQueue(Context context) {
        mContext = context;
        mRequestQueue = queue();
    }

    public static synchronized VolleyQueue instance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyQueue(context);
        }
        return mInstance;
    }

    public RequestQueue queue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void add(Request<T> req) {
        queue().add(req);
    }
}
