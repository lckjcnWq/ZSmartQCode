package com.theswitchbot.common.widget;

/**
 * Created by wohand on 2018/01/22.
 */

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;

import com.theswitchbot.common.R;


/**
 * Created by Mikhael LOPEZ on 16/10/2015.
 */
public class CircularProgressBar extends View {

    private static final String TAG = "CircularProgressBar";
    // Properties
    private float progress = 0;
    private float strokeWidth = getResources().getDimension(R.dimen.default_stroke_width);
    private float backgroundStrokeWidth = getResources().getDimension(R.dimen.default_background_stroke_width);
    private int color = Color.BLACK;
    private int backgroundColor = Color.GRAY;

    // Object used to draw
    private int startAngle = -90;
    private RectF rectF;
    private Paint backgroundPaint;
    private Paint foregroundPaint;
    private ObjectAnimator mObjectAnimator;
    private AnimatorListener mListener;

    //region Constructor & Init Method
    public CircularProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        rectF = new RectF();
        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.CircularProgressBar, 0, 0);
        //Reading values from the XML layout
        try {
            // Value
            progress = typedArray.getFloat(R.styleable.CircularProgressBar_cpb_progress, progress);
            // StrokeWidth
            strokeWidth = typedArray.getDimension(R.styleable.CircularProgressBar_cpb_progressbar_width, strokeWidth);
            backgroundStrokeWidth = typedArray.getDimension(R.styleable.CircularProgressBar_cpb_background_progressbar_width, backgroundStrokeWidth);
            // Color
            color = typedArray.getInt(R.styleable.CircularProgressBar_cpb_progressbar_color, color);
            backgroundColor = typedArray.getInt(R.styleable.CircularProgressBar_cpb_background_progressbar_color, backgroundColor);
        } finally {
            typedArray.recycle();
        }

        // Init Background
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeWidth(backgroundStrokeWidth);

        // Init Foreground
        foregroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        foregroundPaint.setColor(color);
        foregroundPaint.setStyle(Paint.Style.STROKE);
        foregroundPaint.setStrokeWidth(strokeWidth);
    }
    //endregion

    //region Draw Method
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawOval(rectF, backgroundPaint);
        float angle = 360 * progress / 100;
        canvas.drawArc(rectF, startAngle, angle, false, foregroundPaint);
    }
    //endregion

    //region Mesure Method
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        final int width = getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int min = Math.min(width, height);
        setMeasuredDimension(min, min);
        float highStroke = (strokeWidth > backgroundStrokeWidth) ? strokeWidth : backgroundStrokeWidth;
        rectF.set(0 + highStroke / 2, 0 + highStroke / 2, min - highStroke / 2, min - highStroke / 2);
    }
    //endregion

    //region Method Get/Set
    public float getProgress() {
        return progress;
    }

    public void setProgress(float progress) {
        this.progress = (progress <= 100) ? progress : 100;
        invalidate();
    }

    public float getProgressBarWidth() {
        return strokeWidth;
    }

    public void setProgressBarWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
        foregroundPaint.setStrokeWidth(strokeWidth);
        requestLayout();//Because it should recalculate its bounds
        invalidate();
    }

    public float getBackgroundProgressBarWidth() {
        return backgroundStrokeWidth;
    }

    public void setBackgroundProgressBarWidth(float backgroundStrokeWidth) {
        this.backgroundStrokeWidth = backgroundStrokeWidth;
        backgroundPaint.setStrokeWidth(backgroundStrokeWidth);
        requestLayout();//Because it should recalculate its bounds
        invalidate();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
        foregroundPaint.setColor(color);
        invalidate();
        requestLayout();
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
        backgroundPaint.setColor(backgroundColor);
        invalidate();
        requestLayout();
    }
    //endregion

    //region Other Method

    /**
     * Set the progress with an animation.
     * Note that the {@link ObjectAnimator} Class automatically set the progress
     *
     * @param progress The progress it should animate to it.
     */
    public void setProgressWithAnimation(float progress) {
        setProgressWithAnimation(progress, 20000);
    }

    /**
     * Set the progress with an animation.
     * Note that the {@link ObjectAnimator} Class automatically set the progress
     *
     * @param progress The progress it should animate to it.
     * @param duration The length of the animation, in milliseconds.
     */
    public void setProgressWithAnimation(float progress, long duration) {
        if (mObjectAnimator != null) {
            mObjectAnimator.removeAllListeners();
            mObjectAnimator.cancel();
        }
        mObjectAnimator = ObjectAnimator.ofFloat(this, "progress", 0, progress);
        mObjectAnimator.setDuration(duration);
        mObjectAnimator.setInterpolator(new LinearOutSlowInInterpolator());
        mObjectAnimator.addUpdateListener(animation -> {

            float animatedFraction = animation.getAnimatedFraction();
            if (mListener != null) {
                mListener.animatorUpdate((Integer) animation.getAnimatedValue());
            }
        });
        mObjectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation, boolean isReverse) {
                setVisibility(VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);

                if (mObjectAnimator != null) {
                    mObjectAnimator.removeAllListeners();
                }
                if (mListener != null) {
                    postDelayed(() -> mListener.animatorEnd(), 100);
                }
            }
        });
        mObjectAnimator.start();
    }

    public void cancelAnimator() {
        setVisibility(INVISIBLE);
        if (mObjectAnimator != null) {
            mObjectAnimator.removeAllListeners();
            mObjectAnimator.cancel();
            mObjectAnimator = null;
        }
    }

    public void pauseAnimator(){
        setVisibility(INVISIBLE);
        if (mObjectAnimator != null) {
            mObjectAnimator.pause();
        }
    }

    public void resumeAnimator(){
        if (mObjectAnimator!=null&&mObjectAnimator.isPaused()){
            setVisibility(VISIBLE);
            mObjectAnimator.resume();
        }
    }

    public void changeDuration(float progress, long duration) {
        if (mObjectAnimator == null) {
            return;
        }
        mObjectAnimator.setDuration(duration);
//        mObjectAnimator.setFloatValues(progress + 100);

    }

    public long getDuration() {
        if (mObjectAnimator != null) {
            return mObjectAnimator.getDuration();
        }
        return 0;
    }

    public void setAnimatorLisenter(AnimatorListener lisenter) {
        mListener = lisenter;
    }

    //点击事件接口
    public interface AnimatorListener {
        void animatorStart();

        void animatorEnd();

        void animatorUpdate(int value);
    }

    public void endAnimation() {
        if (mObjectAnimator != null) {
            long currentPlayTime = mObjectAnimator.getCurrentPlayTime();
            mObjectAnimator.setCurrentPlayTime(currentPlayTime - 100);
            mObjectAnimator.setDuration(currentPlayTime + 250);
        }
    }
}

