package com.ofalvai.bpinfo.api;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.crashlytics.android.Crashlytics;
import com.ofalvai.bpinfo.BpInfoApplication;
import com.ofalvai.bpinfo.Config;
import com.ofalvai.bpinfo.R;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;

import static com.ofalvai.bpinfo.util.LogUtils.LOGE;

public class NoticeClient implements Response.ErrorListener {

    public interface NoticeListener {
        /**
         * Called only when there's at least 1 notice to display
         * @param noticeBody HTML string of 1 or more notices appended
         */
        void onNoticeResponse(String noticeBody);
    }

    private static final String TAG = "NoticeClient";

    private RequestQueue mRequestQueue;

    @Inject Context mContext;

    @Inject SharedPreferences mSharedPreferences;

    public NoticeClient(RequestQueue requestQueue) {
        mRequestQueue = requestQueue;

        BpInfoApplication.injector.inject(this);
    }

    public void fetchNotice(@NonNull final NoticeListener noticeListener) {
        final Uri url = Uri.parse(Config.BACKEND_URL).buildUpon()
                .appendEncodedPath(Config.BACKEND_NOTICE_PATH)
                .build();

        final JsonArrayRequest request = new JsonArrayRequest(
                url.toString(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        onResponseCallback(response, noticeListener);
                    }
                },
                this
        );

        // Invalidating Volley's cache for this URL to always get the latest notice
        mRequestQueue.getCache().remove(url.toString());

        mRequestQueue.add(request);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        // We don't display anything on the UI because this feature is meant to be silent
        LOGE(TAG, error.toString());
        Crashlytics.logException(error);

    }

    public void onResponseCallback(JSONArray response, NoticeListener listener) {
        try {
            StringBuilder noticeBuilder = new StringBuilder();

            // The response contains an array of notices, we display the ones marked as enabled
            for (int i = 0; i < response.length(); i++) {
                final JSONObject notice = response.getJSONObject(i);
                boolean enabled = notice.getBoolean("enabled");
                boolean debugEnabled = notice.getBoolean("debugEnabled");

                // Only display notice if it's marked as enabled OR marked as enabled for debug mode
                // and debug mode is actually turned on:
                if (enabled || (debugEnabled && isDebugActivated())) {
                    String noticeText = notice.getString("text");
                    noticeBuilder.append(noticeText);
                    noticeBuilder.append("<br />");
                }
            }

            if (noticeBuilder.length() > 0) {
                listener.onNoticeResponse(noticeBuilder.toString());
            }

        } catch (Exception ex) {
            // We don't display anything on the UI because this feature is meant to be silent
            LOGE(TAG, ex.toString());
            Crashlytics.logException(ex);
        }
    }

    private boolean isDebugActivated() {
        return mSharedPreferences.getBoolean(
                mContext.getString(R.string.pref_key_debug_mode), false);
    }
}
