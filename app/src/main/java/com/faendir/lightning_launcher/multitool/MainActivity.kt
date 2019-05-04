package com.faendir.lightning_launcher.multitool

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import com.faendir.lightning_launcher.multitool.billing.BillingManager
import com.faendir.lightning_launcher.multitool.event.ClickEvent
import com.faendir.lightning_launcher.multitool.util.BaseActivity
import com.faendir.lightning_launcher.multitool.util.FragmentManager
import com.faendir.lightning_launcher.multitool.util.Fragments
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import org.greenrobot.eventbus.EventBus

class MainActivity : BaseActivity(R.layout.content_main) {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var drawer: Drawer
    private lateinit var billingManager: BillingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        billingManager = BillingManager(this)
        val builder = DrawerBuilder(this).withToolbar(toolbar)
        Fragments.values().forEach { it.addTo(builder) }
        drawer = builder.addDrawerItems(DividerDrawerItem(),
                SecondaryDrawerItem().withName(R.string.play_store)
                        .withIdentifier(R.string.play_store.toLong())
                        .withSelectable(false)
                        .withOnDrawerItemClickListener { _, _, _ ->
                            try {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
                            } catch (e: ActivityNotFoundException) {
                                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                            }
                            true
                        },/*
                SecondaryDrawerItem().withName(R.string.google_community)
                        .withIdentifier(R.string.google_community.toLong())
                        .withSelectable(false)
                        .withOnDrawerItemClickListener { _, _, _ ->
                            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_googlePlus))))
                            true
                        },*/
                SecondaryDrawerItem().withName(R.string.email)
                        .withIdentifier(R.string.email.toLong())
                        .withSelectable(false)
                        .withOnDrawerItemClickListener { _, _, _ ->
                            val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.fromParts(getString(R.string.link_email_scheme), getString(R.string.link_email_adress), null))
                            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.link_email_subject))
                            startActivity(emailIntent)
                            true
                        }).withSelectedItem(-1).withCloseOnClick(true).build()
        fragmentManager = FragmentManager(this, billingManager, drawer)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(fragmentManager)
        if (!fragmentManager.loadLastFragment() && !drawer.isDrawerOpen) {
            drawer.openDrawer()
        }
    }

    override fun onStop() {
        EventBus.getDefault().unregister(fragmentManager)
        super.onStop()
    }

    override fun onDestroy() {
        billingManager.release()
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen) {
            drawer.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!billingManager.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    fun onButtonClick(v: View) {
        EventBus.getDefault().post(ClickEvent(v.id))
    }
}
