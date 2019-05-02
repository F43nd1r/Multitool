package com.faendir.lightning_launcher.multitool.gesture

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.util.BaseActivity
import com.faendir.lightning_launcher.multitool.util.IntentChooserFragment
import com.google.android.material.tabs.TabLayout

class IntentChooser : BaseActivity(R.layout.content_intent_chooser) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager>(R.id.viewpager)
        viewPager.adapter = object : FragmentPagerAdapter(supportFragmentManager) {
            override fun getItem(position: Int): Fragment {
                val intent: Intent
                val indirect: Boolean
                if (position == 0) {
                    intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    indirect = false
                } else {
                    intent = Intent(Intent.ACTION_CREATE_SHORTCUT)
                    indirect = true
                }
                return IntentChooserFragment.newInstance(intent, indirect)
            }

            override fun getCount(): Int = 2

            override fun getPageTitle(position: Int): CharSequence? = getString(if (position == 0) R.string.title_apps else R.string.title_shortcuts)
        }
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
