package com.ofalvai.bpinfo.api.subscription

import android.net.Uri
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.iid.FirebaseInstanceId
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import javax.inject.Inject

class SubscriptionClient @Inject constructor(private val requestQueue: RequestQueue) {

    interface Callback {
        fun onSubscriptionError(error: VolleyError)
        fun onPostSubscriptionResponse() // TODO: response object
        fun onGetSubscriptionResponse(routeIDList: List<String>)
    }

    companion object {
        const val BASE_URL = "https://bpinfo-backend.herokuapp.com/api/v1/"
    }

    private val token get() = FirebaseInstanceId.getInstance().token

    fun postSubscription(routeID: String, callback: Callback) {
        val url: String = Uri.parse(BASE_URL)
            .buildUpon()
            .appendPath("subscription")
            .toString()

        val body = JSONObject().apply {
            put("routeId", routeID)
            put("token", token)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, body,
            Response.Listener {
                Timber.d(it.toString())
                callback.onPostSubscriptionResponse()
            },
            Response.ErrorListener {
                Timber.e(it.toString())
                callback.onSubscriptionError(it)
            }
        )

        requestQueue.add(request)
    }

    fun getSubscriptions(callback: Callback) {
        val url = Uri.parse(BASE_URL)
            .buildUpon()
            .appendPath("subscription")
            .appendPath(token)
            .toString()

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            Response.Listener {
                callback.onGetSubscriptionResponse(parseSubscriptionList(it))
            },
            Response.ErrorListener {
                Timber.e(it.toString())
                callback.onSubscriptionError(it)
            }
        )
        requestQueue.add(request)
    }

    private fun parseSubscriptionList(array: JSONArray): List<String> {
        val routeIDList = mutableListOf<String>()

        if (array.length() == 0) return routeIDList

        for (i in 0 until array.length()) {
            routeIDList.add(array.getString(i))
        }
        return routeIDList
    }

}