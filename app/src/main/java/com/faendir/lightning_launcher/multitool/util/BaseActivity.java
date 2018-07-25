package com.faendir.lightning_launcher.multitool.util;

import android.os.Bundle;
import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.faendir.lightning_launcher.multitool.R;

/**
 * Created on 12.07.2016.
 *
 * @author F43nd1r
 */

public abstract class BaseActivity extends AppCompatActivity {
    private final int layoutRes;
    private Toolbar toolbar;

    public BaseActivity(@LayoutRes int layoutRes) {
        this.layoutRes = layoutRes;
    }

    @CallSuper
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLayout();
        toolbar = initToolbar();
    }

    private void initLayout() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.activity_base, (ViewGroup) findViewById(android.R.id.content).getRootView(), false);
        ViewGroup mainFrame = layout.findViewById(R.id.main_frame);
        inflater.inflate(layoutRes, mainFrame, true);
        setContentView(layout);
    }

    private Toolbar initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        return toolbar;
    }

    protected Toolbar getToolbar() {
        return toolbar;
    }
}
