package com.faendir.lightning_launcher.multitool.util;

import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.faendir.lightning_launcher.multitool.R;

/**
 * Created by Lukas on 12.07.2016.
 */

public class BaseActivity extends AppCompatActivity {
    private final int layoutRes;
    private final int menuRes;
    private DrawerLayout layout;
    private LayoutInflater inflater;

    public BaseActivity(@LayoutRes int layoutRes) {
        this(layoutRes, 0);
    }

    public BaseActivity(@LayoutRes int layoutRes, @MenuRes int menuRes) {
        this.layoutRes = layoutRes;
        this.menuRes = menuRes;
    }

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inflater = LayoutInflater.from(this);
        initLayout();
        Toolbar toolbar = initToolbar();
        initDrawer(toolbar);
    }

    private void initLayout() {
        layout = (DrawerLayout) inflater.inflate(R.layout.activity_base, (ViewGroup) findViewById(android.R.id.content).getRootView(), false);
        ViewGroup mainFrame = (ViewGroup) layout.findViewById(R.id.main_frame);
        inflater.inflate(layoutRes, mainFrame, true);
        setContentView(layout);
    }

    private void initDrawer(Toolbar toolbar) {
        if (menuRes != 0) {
            NavigationView view = (NavigationView) inflater.inflate(R.layout.navigationview, layout, false);
            view.inflateMenu(menuRes);
            layout.addView(view);

            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            assert drawer != null;
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            assert navigationView != null;
            initNavView(navigationView);
        }
    }

    protected void initNavView(NavigationView navigationView){
    }

    private Toolbar initToolbar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    protected DrawerLayout getLayout(){
        return layout;
    }
}
