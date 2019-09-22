package com.faendir.lightning_launcher.multitool.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.R

/**
 * @author lukas
 * @since 18.07.18
 */
class AnimationFragment : Fragment() {
    private var isReverse = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(R.layout.fragment_animation, container, false)
        val radioAnimations = view.findViewById<RadioGroup>(R.id.radioAnimations)
        for (animation in Animation.values()) {
            val radioButton = RadioButton(activity)
            radioButton.setText(animation.label)
            radioButton.id = animation.ordinal
            radioAnimations.addView(radioButton)
        }
        val demo = view.findViewById<AnimationDemo>(R.id.animationDemo)
        radioAnimations.setOnCheckedChangeListener { _, checkedId -> demo.setAnimation(Animation.values()[checkedId]) }
        radioAnimations.check(0)
        val shouldAnimate = view.findViewById<CheckBox>(R.id.shouldAnimate)
        val animator = ValueAnimator.ofFloat(0F, 1F)
        animator.duration = 2000
        animator.interpolator = AccelerateDecelerateInterpolator()
        animator.addUpdateListener { animation -> demo.scrollTo(((if (isReverse) 1 - animation.animatedValue as Float else animation.animatedValue as Float) * demo.width).toInt(), 0) }
        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                animator.startDelay = 1000
                isReverse = !isReverse
                animator.start()
            }
        })
        animator.start()
        shouldAnimate.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && animator.isRunning) {
                animator.pause()
            } else if (animator.isPaused) {
                animator.resume()
            }
        }
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.animation, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_help) {
            AlertDialog.Builder(activity).setTitle(R.string.title_help).setMessage(R.string.message_animationHelp).setPositiveButton(R.string.button_ok, null).show()
        }
        return true
    }
}
