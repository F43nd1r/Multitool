package com.faendir.lightning_launcher.multitool.util

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.faendir.lightning_launcher.multitool.R

/**
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

abstract class BaseActivity(@param:LayoutRes private val layoutRes: Int) : AppCompatActivity() {
    protected lateinit var toolbar: Toolbar
        private set

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initLayout()
        toolbar = initToolbar()
    }

    private fun initLayout() {
        val inflater = LayoutInflater.from(this)
        val layout = inflater.inflate(R.layout.activity_base, findViewById<View>(android.R.id.content).rootView as ViewGroup, false)
        val mainFrame = layout.findViewById<ViewGroup>(R.id.main_frame)
        inflater.inflate(layoutRes, mainFrame, true)
        setContentView(layout)
    }

    private fun initToolbar(): Toolbar {
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        return toolbar
    }
}
