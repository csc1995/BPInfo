package com.ofalvai.bpinfo.ui.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.NavUtils
import android.support.v4.content.ContextCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.ContentLoadingProgressBar
import android.support.v7.app.AlertDialog
import android.support.v7.view.ContextThemeWrapper
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.iid.FirebaseInstanceId
import com.ofalvai.bpinfo.R
import com.ofalvai.bpinfo.model.Route
import com.ofalvai.bpinfo.model.RouteType
import com.ofalvai.bpinfo.ui.base.BaseActivity
import com.ofalvai.bpinfo.ui.notifications.adapter.RouteListPagerAdapter
import com.ofalvai.bpinfo.ui.notifications.routelist.RouteListContract
import com.ofalvai.bpinfo.ui.settings.SettingsActivity
import com.ofalvai.bpinfo.util.getContentDescription
import com.ofalvai.bpinfo.util.hide
import com.ofalvai.bpinfo.util.show
import com.wefika.flowlayout.FlowLayout
import kotterknife.bindView
import timber.log.Timber

class NotificationsActivity : BaseActivity(), NotificationsContract.View {

    private lateinit var presenter: NotificationsContract.Presenter

    private val tabLayout: TabLayout by bindView(R.id.notifications__tabs)
    private val viewPager: ViewPager by bindView(R.id.notifications__viewpager)
    private val subscribedRoutesLayout: FlowLayout by bindView(R.id.notifications__subscribed_routes)
    private val subscribedEmptyView: TextView by bindView(R.id.notifications__subscribed_empty)
    private val progressBar: ContentLoadingProgressBar by bindView(R.id.notifications__progress_bar)

    private lateinit var pagerAdapter: RouteListPagerAdapter

    companion object {
        @JvmStatic
        fun newIntent(context: Context): Intent {
            return Intent(context, NotificationsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
            setTitle(R.string.title_activity_notifications)
        }

        presenter = NotificationsPresenter()
        presenter.attachView(this)

        presenter.fetchRouteList()
        presenter.fetchSubscriptions()

        Timber.d("FCM token: ${FirebaseInstanceId.getInstance().token}")

        setupViewPager()

        GoogleApiAvailability.getInstance().makeGooglePlayServicesAvailable(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_notifications, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_item_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            android.R.id.home -> NavUtils.navigateUpFromSameTask(this)
        }

        return true
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun displayRouteList(routeList: List<Route>) {
        val groupedRoutes: Map<RouteType, List<Route>> = routeList.groupBy { it.type }
        groupedRoutes.forEach {
            val view: RouteListContract.View? = pagerAdapter.getView(it.key)
            val routes = it.value.sortedBy { it.id }
            view?.displayRoutes(routes)
        }
    }

    override fun addSubscribedRoute(route: Route) {
        subscribedEmptyView.hide()

        addSubscribedRouteIcon(route)
    }

    override fun removeSubscribedRoute(route: Route) {
        removeSubscribedRouteIcon(route)

        if (subscribedRoutesLayout.childCount == 0) {
            subscribedEmptyView.show()
        }
    }

    override fun onRouteClicked(route: Route) {
        presenter.subscribeTo(route.id)
    }

    override fun displaySubscriptions(routeList: List<Route>) {
        subscribedRoutesLayout.removeAllViews()

        if (routeList.isEmpty()) {
            subscribedEmptyView.show()
        } else {
            subscribedEmptyView.hide()
        }

        for (route in routeList) {
            addSubscribedRouteIcon(route)
        }
    }

    override fun showProgress(show: Boolean) {
        if (show) {
            progressBar.show()
        } else {
            progressBar.hide()
        }
    }

    private fun setupViewPager() {
        pagerAdapter = RouteListPagerAdapter(supportFragmentManager, this)
        viewPager.adapter = pagerAdapter
        viewPager.offscreenPageLimit = RouteListPagerAdapter.OFFSCREEN_PAGE_LIMIT
        tabLayout.setupWithViewPager(viewPager, false)
    }

    @SuppressLint("RestrictedApi")
    private fun addSubscribedRouteIcon(route: Route) {
        val iconContextTheme = ContextThemeWrapper(this, R.style.RouteIcon_Big)
        val iconView = TextView(iconContextTheme)

        iconView.text = route.shortName
        iconView.setTextColor(route.textColor)
        iconView.contentDescription = route.getContentDescription(this)
        iconView.tag = route.id
        iconView.alpha = 0.0f
        subscribedRoutesLayout.addView(iconView)

        // Layout attributes defined in R.style.RouteIcon were ignored before attaching the view to
        // a parent, so we need to manually set them
        val params = iconView.layoutParams as ViewGroup.MarginLayoutParams
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT
        val margin = resources.getDimension(R.dimen.route_icon_margin).toInt()
        params.rightMargin = margin
        params.topMargin = margin

        // Setting a custom colored rounded background drawable as background
        val iconBackground = ContextCompat.getDrawable(this, R.drawable.rounded_corner_5dp)
        if (iconBackground != null) {
            val colorFilter = LightingColorFilter(Color.rgb(1, 1, 1), route.color)
            iconBackground.mutate().colorFilter = colorFilter
            iconView.background = iconBackground
        }

        iconView.setOnClickListener { showDeleteDialog(route) }

        iconView.animate().apply {
            alpha(1.0f)
            duration = 225
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun removeSubscribedRouteIcon(route: Route) {
        for (i in 0 until subscribedRoutesLayout.childCount) {
            val view: View? = subscribedRoutesLayout.getChildAt(i)
            if (view?.tag == route.id) {
                subscribedRoutesLayout.removeView(view)
            }
        }
    }

    private fun showDeleteDialog(route: Route) {
        AlertDialog.Builder(this)
            .setTitle(route.shortName) // TODO: localized long name
            .setMessage(R.string.notif_remove_dialog_message)
            .setPositiveButton(R.string.notif_remove_dialog_positive) { dialog, _ ->
                dialog.dismiss()
                presenter.removeSubscription(route.id)
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
