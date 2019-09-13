package com.example.micua.licenseapp;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;

public class AnimationFactory {

    public AnimationFactory() {
    }

    public Animation generateFadeInAnimation(long duration, int offset
            , Interpolator interpolator, boolean fillAfter) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
        alphaAnimation.setInterpolator(interpolator);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setStartOffset(offset);
        alphaAnimation.setFillAfter(fillAfter);

        return alphaAnimation;
    }

    public Animation generateFadeOutAnimation(long duration, int offset
            , Interpolator interpolator, boolean fillAfter) {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setInterpolator(interpolator);
        alphaAnimation.setDuration(duration);
        alphaAnimation.setStartOffset(offset);
        alphaAnimation.setFillAfter(fillAfter);

        return alphaAnimation;
    }
}
