package com.ofalvai.bpinfo.ui.notifications

import com.android.volley.VolleyError
import com.ofalvai.bpinfo.BpInfoApplication
import com.ofalvai.bpinfo.api.bkkinfo.RouteListClient
import com.ofalvai.bpinfo.api.subscription.SubscriptionClient
import com.ofalvai.bpinfo.model.Route
import com.ofalvai.bpinfo.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class NotificationsPresenter : BasePresenter<NotificationsContract.View>(),
        NotificationsContract.Presenter, RouteListClient.RouteListListener,
    SubscriptionClient.Callback {

    @Inject lateinit var routeListClient: RouteListClient
    @Inject lateinit var subscriptionClient: SubscriptionClient

    private var routeListResponse: List<Route>? = null
    private var subscribedRouteIDListResponse: List<String>? = null

    init {
        BpInfoApplication.injector.inject(this)
    }

    override fun fetchRouteList() {
        routeListClient.fetchRouteList(this)
    }

    override fun onRouteListResponse(routeList: List<Route>) {
        routeListResponse = routeList
        view?.displayRouteList(routeList)

        subscribedRouteIDListResponse?.let {
            displaySubscribedRoutes(it, routeList)
        }
    }

    override fun onRouteListError(ex: Exception) {
        Timber.e(ex) // TODO
    }

    override fun subscribeTo(routeID: String) {
        subscriptionClient.postSubscription(routeID, this)
    }

    override fun fetchSubscriptions() {
        subscriptionClient.getSubscriptions(this)
    }

    override fun onSubscriptionError(error: VolleyError) {
        Timber.e(error) // TODO
    }

    override fun onPostSubscriptionResponse() {
        fetchSubscriptions()
    }

    override fun onGetSubscriptionResponse(routeIDList: List<String>) {
        subscribedRouteIDListResponse = routeIDList

        routeListResponse?.let {
            displaySubscribedRoutes(routeIDList, it)
        }
    }

    /**
     * Calls the View with the full Route objects when both the subscribed route IDs and
     * the list of all Route objects are available
     */
    private fun displaySubscribedRoutes(routeIDList: List<String>, allRoutes: List<Route>) {
        val routes: List<Route> = allRoutes.filter { routeIDList.contains(it.id) }
        view?.displaySubscriptions(routes)
    }
}