/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.ui2.main;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LevelListDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import com.andrew.apollo.utils.ThemeHelper;

import org.opensilk.common.util.ThemeUtils;
import org.opensilk.common.util.VersionUtils;
import org.opensilk.common.widget.FloatingActionButtonCheckable;
import org.opensilk.music.R;

import org.opensilk.common.widget.FloatingActionButton;
import org.opensilk.common.widget.FlingyFabLayout;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import mortar.Mortar;
import timber.log.Timber;

/**
 * Created by drew on 10/14/14.
 */
public class MainView extends FlingyFabLayout {

    @Inject MainScreen.Presenter presenter;

    @InjectView(R.id.floating_action_button) FloatingActionButtonCheckable fabPlay;
    @InjectView(R.id.floating_action_next) FloatingActionButton fabNext;
    @InjectView(R.id.floating_action_prev) FloatingActionButton fabPrev;
    @InjectView(R.id.floating_action_shuffle) FloatingActionButton fabShuffle;
    @InjectView(R.id.floating_action_repeat) FloatingActionButton fabRepeat;

    AnimatorSet expandFabs;
    AnimatorSet collapseFabs;

    boolean secondaryFabsShowing = true;
    boolean firstLayout = true;

    public MainView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            Mortar.inject(getContext(), this);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.inject(this);
        setupActionButton();
        if (!isInEditMode()) presenter.takeView(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!isInEditMode()) presenter.dropView(this);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Timber.v("onLayout firstLayout=%s", firstLayout);
        super.onLayout(changed, l, t, r, b);
        if (!firstLayout) return;
        firstLayout = false;
        // Now that everyone knows where they are supposed to be
        // move them into position for the expand animation
        fabNext.setY(fabPlay.getY());
        fabNext.setX(fabPlay.getX());
        fabNext.setVisibility(INVISIBLE);
        fabPrev.setY(fabPlay.getY());
        fabPrev.setX(fabPlay.getX());
        fabPrev.setVisibility(INVISIBLE);
        fabShuffle.setY(fabPlay.getY());
        fabShuffle.setX(fabPlay.getX());
        fabShuffle.setVisibility(INVISIBLE);
        fabRepeat.setY(fabPlay.getY());
        fabRepeat.setX(fabPlay.getX());
        fabRepeat.setVisibility(INVISIBLE);
        secondaryFabsShowing = false;
        setupFabAnimatiors();
    }

    @Override
    public boolean canCaptureView(View child, int pointerId) {
        if (child.getId() == fabPlay.getId()) {
            return true;
        } else if (fabNext.getVisibility() == VISIBLE && child.getId() == fabNext.getId()) {
            return true;
        } else if (fabPrev.getVisibility() == VISIBLE && child.getId() == fabPrev.getId()) {
            return true;
        } else if (fabRepeat.getVisibility() == VISIBLE && child.getId() == fabRepeat.getId()) {
            return true;
        } else if (fabShuffle.getVisibility() == VISIBLE && child.getId() == fabShuffle.getId()) {
            return true;
        }
        return false;
    }

    void setupActionButton() {
        fabPlay.setOnDoubleClickListener(new FloatingActionButton.OnDoubleClickListener() {
            @Override
            public void onDoubleClick(View view) {
                toggleSecondaryFabs();
            }
        });
        fabPlay.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleSecondaryFabs();
                return true;
            }
        });
        fabPlay.bringToFront();

        setupShuffleDrawable();
        setupRepeatDrawable();
    }

    void setupFabAnimatiors() {
        Interpolator overshootInterpolator = new OvershootInterpolator();
        Interpolator decelerateInterpolator = new DecelerateInterpolator();

        ObjectAnimator nextCY = ObjectAnimator.ofFloat(fabNext, "Y", fabPlay.getY());
        nextCY.setInterpolator(decelerateInterpolator);
        ObjectAnimator nextCX = ObjectAnimator.ofFloat(fabNext, "X", fabPlay.getX());
        nextCX.setInterpolator(decelerateInterpolator);
        ObjectAnimator prevCY = ObjectAnimator.ofFloat(fabPrev, "Y", fabPlay.getY());
        prevCY.setInterpolator(decelerateInterpolator);
        ObjectAnimator prevCX = ObjectAnimator.ofFloat(fabPrev, "X", fabPlay.getX());
        prevCX.setInterpolator(decelerateInterpolator);
        ObjectAnimator shuffleCY = ObjectAnimator.ofFloat(fabShuffle, "Y", fabPlay.getY());
        shuffleCY.setInterpolator(decelerateInterpolator);
        ObjectAnimator shuffleCX = ObjectAnimator.ofFloat(fabShuffle, "X", fabPlay.getX());
        shuffleCX.setInterpolator(decelerateInterpolator);
        ObjectAnimator repeatCY = ObjectAnimator.ofFloat(fabRepeat, "Y", fabPlay.getY());
        repeatCY.setInterpolator(decelerateInterpolator);
        ObjectAnimator repeatCX = ObjectAnimator.ofFloat(fabRepeat, "X", fabPlay.getX());
        repeatCX.setInterpolator(decelerateInterpolator);
        collapseFabs = new AnimatorSet();
        collapseFabs.playTogether(nextCY, nextCX, prevCY, prevCX, shuffleCY, shuffleCX, repeatCY, repeatCX);
        collapseFabs.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                fabNext.setVisibility(INVISIBLE);
                fabPrev.setVisibility(INVISIBLE);
                fabShuffle.setVisibility(INVISIBLE);
                fabRepeat.setVisibility(INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        ObjectAnimator nextEY = ObjectAnimator.ofFloat(fabNext, "translationY", 0);
        nextEY.setInterpolator(overshootInterpolator);
        ObjectAnimator nextEX = ObjectAnimator.ofFloat(fabNext, "translationX", 0);
        nextEX.setInterpolator(overshootInterpolator);
        ObjectAnimator prevEY = ObjectAnimator.ofFloat(fabPrev, "translationY", 0);
        prevEY.setInterpolator(overshootInterpolator);
        ObjectAnimator prevEX = ObjectAnimator.ofFloat(fabPrev, "translationX", 0);
        prevEX.setInterpolator(overshootInterpolator);
        ObjectAnimator shuffleEY = ObjectAnimator.ofFloat(fabShuffle, "translationY", 0);
        shuffleEY.setInterpolator(overshootInterpolator);
        ObjectAnimator shuffleEX = ObjectAnimator.ofFloat(fabShuffle, "translationX", 0);
        shuffleEX.setInterpolator(overshootInterpolator);
        ObjectAnimator repeatEY = ObjectAnimator.ofFloat(fabRepeat, "translationY", 0);
        repeatEY.setInterpolator(overshootInterpolator);
        ObjectAnimator repeatEX = ObjectAnimator.ofFloat(fabRepeat, "translationX", 0);
        repeatEX.setInterpolator(overshootInterpolator);
        expandFabs = new AnimatorSet();
        expandFabs.playTogether(nextEY, nextEX, prevEY, prevEX, shuffleEY, shuffleEX, repeatEY, repeatEX);
        expandFabs.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                fabNext.setVisibility(VISIBLE);
                fabPrev.setVisibility(VISIBLE);
                fabShuffle.setVisibility(VISIBLE);
                fabRepeat.setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    void toggleSecondaryFabs() {
        if (secondaryFabsShowing) {
            secondaryFabsShowing = false;
            expandFabs.cancel();
            collapseFabs.start();
        } else {
            secondaryFabsShowing = true;
            collapseFabs.cancel();
            expandFabs.start();
        }
    }

    void setupShuffleDrawable() {
        if (VersionUtils.hasLollipop()) return;
        LevelListDrawable d = (LevelListDrawable) fabShuffle.getDrawable();
//        Drawable d1 = getResources().getDrawable(R.drawable.ic_shuffle_black_36dp);
//        d1.setColorFilter(Themer.getColorAccent(getContext()), PorterDuff.Mode.MULTIPLY);
        Drawable d1 = ThemeHelper.themeDrawable(getContext(), R.drawable.ic_shuffle_white_24dp,
                ThemeUtils.getColorAccent(getContext()));
        d.addLevel(1, 2, d1);
    }

    void setupRepeatDrawable() {
        if (VersionUtils.hasLollipop()) return;
        LevelListDrawable d = (LevelListDrawable) fabRepeat.getDrawable();
        Drawable d1 = ThemeHelper.themeDrawable(getContext(), R.drawable.ic_repeat_one_white_24dp,
                ThemeUtils.getColorAccent(getContext()));
        d.addLevel(1, 1, d1);
        Drawable d2 = ThemeHelper.themeDrawable(getContext(), R.drawable.ic_repeat_white_24dp,
                ThemeUtils.getColorAccent(getContext()));
        d.addLevel(2, 2, d2);
    }

    void setupLayoutTransitions() {
        LayoutTransition lt = new LayoutTransition();
//        lt.disableTransitionType(LayoutTransition.CHANGE_APPEARING);
//        lt.disableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        Animator appearing = AnimatorInflater.loadAnimator(getContext(), R.animator.fab_slide_in_left);
        lt.setAnimator(LayoutTransition.APPEARING, appearing);
        Animator disappearing = AnimatorInflater.loadAnimator(getContext(), R.animator.fab_slide_out_left);
        lt.setAnimator(LayoutTransition.DISAPPEARING, disappearing);
        setLayoutTransition(lt);
    }

}
