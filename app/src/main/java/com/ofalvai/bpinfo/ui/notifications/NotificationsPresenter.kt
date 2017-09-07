package com.ofalvai.bpinfo.ui.notifications

import com.ofalvai.bpinfo.BpInfoApplication
import com.ofalvai.bpinfo.api.bkkinfo.RouteListClient
import com.ofalvai.bpinfo.model.Route
import com.ofalvai.bpinfo.ui.base.BasePresenter
import timber.log.Timber
import javax.inject.Inject

class NotificationsPresenter : BasePresenter<NotificationsContract.View>(),
        NotificationsContract.Presenter, RouteListClient.RouteListListener {

    @Inject lateinit var routeListClient: RouteListClient

    init {
        BpInfoApplication.injector.inject(this)
    }

    override fun fetchRouteList() {
        routeListClient.fetchRouteList(this)
    }

    override fun onRouteListResponse(routeList: List<Route>) {
        routeList.forEach {
            Timber.d(it.toString())
        }
    }

    override fun onRouteListError(ex: Exception) {
        Timber.e(ex)
    }
}