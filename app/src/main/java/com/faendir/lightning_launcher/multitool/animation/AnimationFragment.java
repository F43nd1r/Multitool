package com.faendir.lightning_launcher.multitool.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.faendir.lightning_launcher.multitool.R;

/**
 * @author lukas
 * @since 18.07.18
 */
public class AnimationFragment extends Fragment {
    private boolean isReverse = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_animation, container, false);
        RadioGroup radioAnimations = view.findViewById(R.id.radioAnimations);
        for (Animation animation : Animation.values()) {
            RadioButton radioButton = new RadioButton(getActivity());
            radioButton.setText(animation.getLabel());
            radioButton.setId(animation.ordinal());
            radioAnimations.addView(radioButton);
        }
        AnimationDemo demo = view.findViewById(R.id.animationDemo);
        radioAnimations.setOnCheckedChangeListener((group, checkedId) -> demo.setAnimation(Animation.values()[checkedId]));
        radioAnimations.check(0);
        CheckBox shouldAnimate = view.findViewById(R.id.shouldAnimate);
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> demo.scrollTo((int) ((isReverse ? 1 - (float) animation.getAnimatedValue() : (float) animation.getAnimatedValue())
                                                                     * demo.getWidth()), 0));
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animator.setStartDelay(1000);
                isReverse = !isReverse;
                animator.start();
            }
        });
        animator.start();
        shouldAnimate.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (!isChecked && animator.isRunning()) {
                animator.pause();
            } else if (animator.isPaused()) {
                animator.resume();
            }
        });
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.animation, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                new AlertDialog.Builder(getActivity()).setTitle(R.string.title_help).setMessage(R.string.message_animationHelp).setPositiveButton(R.string.button_ok, null).show();
                break;
        }
        return true;
    }
}
