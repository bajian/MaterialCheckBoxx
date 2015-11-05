package com.example.bajian.materialcheckbox;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Created by hgx on 2015/11/5.
 */
public class MaterialCheckBox extends View {
    private final String TAG = "MaterialCheckBox";
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    /**
     * 填充内圆半径
     */
    private int radius;

    /**
     * 填充内圆四个边界的坐标
     */
    private RectF mRectF = new RectF();

    /**
     * 勾勾四个边界的坐标
     */
    private RectF mInnerRectF = new RectF();

    private Path mPath = new Path();
    private float mSweepAngle;
    private final double mSin27 = Math.sin(Math.toRadians(27));
    private final double mSin63 = Math.sin(Math.toRadians(63));
    private float mHookStartY;
    private float mBaseLeftHookOffset;
    private float mBaseRightHookOffset;
    private float mEndLeftHookOffset;
    private float mEndRightHookOffset;

    /**
     * 整个view的宽
     */
    private int size;
    private boolean mChecked = true;
    private float mHookOffset;
    private float mHookSize;


    /**
     * 圈内颜色（渐变）
     */
    private int mInnerCircleAlpha = 0XFF;

    /**
     * 勾和圆圈宽度
     */
    private int mStrokeWidth = 2;
    private final int mDuration = 500;

    /**
     * 勾和圆圈的颜色
     */
    private int mStrokeColor = Color.BLUE;

    /**
     * 内圆填充颜色
     */
    private int mCircleColor = Color.WHITE;
    private final int defaultSize = 40;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    public MaterialCheckBox(Context context) {
        this(context, null);
    }

    public MaterialCheckBox(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    private void init(AttributeSet attrs){
        if (attrs != null){
            TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.MaterialCheckBox);
            mStrokeWidth = (int)array.getDimension(R.styleable.MaterialCheckBox_stroke_width, dip(mStrokeWidth));
            mStrokeColor = array.getColor(R.styleable.MaterialCheckBox_stroke_color, mStrokeColor);
            mCircleColor = array.getColor(R.styleable.MaterialCheckBox_circle_color, mCircleColor);
            array.recycle();
        }else {
            mStrokeWidth = dip(mStrokeWidth);
        }

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mStrokeColor);

        //TODO styleable 增加Checked属性

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setChecked(!mChecked);
            }
        });
    }
    //onMeasure->onLayout->Draw
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG,"onMeasure widthMeasureSpec"+widthMeasureSpec+"heightMeasureSpec"+heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.AT_MOST &&
                MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST){
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)getLayoutParams();
            width = height =Math.min(dip(defaultSize) - params.leftMargin - params.rightMargin,
                    dip(defaultSize) - params.bottomMargin - params.topMargin);
        }
        int size = Math.min(width - getPaddingLeft() - getPaddingRight(),
                height - getPaddingBottom() - getPaddingTop());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout changed" + changed + "left" + left+"top"+top+"right"+right+"bottom"+bottom);
        super.onLayout(changed, left, top, right, bottom);
        size = getWidth();
        radius = (size - (2 * mStrokeWidth))/2;
        //float left, float top, float right, float bottom
        mRectF.set(mStrokeWidth, mStrokeWidth, size - mStrokeWidth, size - mStrokeWidth);
        mInnerRectF.set(mRectF);
        mInnerRectF.inset(mStrokeWidth / 2, mStrokeWidth / 2);

        mHookStartY = (float)(size/2 - (radius* mSin27 + (radius-radius*mSin63)));

        mBaseLeftHookOffset = (float)(radius*(1-mSin63)) + mStrokeWidth/2;
        mBaseRightHookOffset = 0f;
        mEndLeftHookOffset = mBaseLeftHookOffset + (2*size/3-mHookStartY)*0.33f;
        mEndRightHookOffset = mBaseRightHookOffset + (size/3 + mHookStartY)*0.38f;
        mHookSize = size - (mEndLeftHookOffset + mEndRightHookOffset);
        mHookOffset = mChecked?mHookSize + mEndLeftHookOffset - mBaseLeftHookOffset:0;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw");
        super.onDraw(canvas);
        drawCircle(canvas);
        drawHook(canvas);
    }

    private void drawCircle(Canvas canvas){
        //360->0 框-》勾勾
        if (mSweepAngle!=0){
            initDrawStrokeCirclePaint();
            canvas.drawArc(mRectF, 202, mSweepAngle, false, mPaint);
            initDrawAlphaStrokeCirclePaint();
            canvas.drawArc(mRectF, 202, mSweepAngle - 360, false, mPaint);
            initDrawInnerCirclePaint();
            canvas.drawArc(mInnerRectF, 0, 360, false, mPaint);
        }else{
            initDrawAlphaStrokeCirclePaint();
            canvas.drawRoundRect(mRectF, 5, 5, mPaint);
            initDrawInnerCirclePaint();
            canvas.drawRoundRect(mInnerRectF,5,5,mPaint);
        }
    }

    private void drawHook(Canvas canvas){
        if (mHookOffset == 0)
            return;
        initDrawHookPaint();
        mPath.reset();
        float offset;
        Log.d(TAG, "mHookOffset"+mHookOffset);
        Log.d(TAG, "2 * size/3 - mHookStartY - mBaseLeftHookOffset"+(2 * size/3 - mHookStartY - mBaseLeftHookOffset));
        Log.d(TAG, "mHookSize"+mHookSize);
        if (mHookOffset <= (2 * size/3 - mHookStartY - mBaseLeftHookOffset)){
            Log.d(TAG, "mHookOffset <= (2 * size/3 - mHookStartY - mBaseLeftHookOffset)");
            mPath.moveTo(mBaseLeftHookOffset, mBaseLeftHookOffset + mHookStartY);
            mPath.lineTo(mBaseLeftHookOffset + mHookOffset, mBaseLeftHookOffset + mHookStartY + mHookOffset);
        }else if (mHookOffset <= mHookSize){
            Log.d(TAG, "mHookOffset <= mHookSize");
            mPath.moveTo(mBaseLeftHookOffset, mBaseLeftHookOffset + mHookStartY);
            mPath.lineTo(2 * size / 3 - mHookStartY, 2 * size / 3);
            mPath.lineTo(mHookOffset + mBaseLeftHookOffset,
                    2 * size/3 - (mHookOffset - (2 * size/3 - mHookStartY - mBaseLeftHookOffset)));
        }else {
            Log.d(TAG, "else");
            offset = mHookOffset - mHookSize;
            mPath.moveTo(mBaseLeftHookOffset + offset, mBaseLeftHookOffset + mHookStartY + offset);
            mPath.lineTo(2 * size / 3 - mHookStartY, 2 * size / 3);
            mPath.lineTo(mHookSize + mBaseLeftHookOffset + offset,
                    2 * size/3 - (mHookSize - (2 * size/3 - mHookStartY - mBaseLeftHookOffset) + offset));
        }
        canvas.drawPath(mPath, mPaint);
    }

    private void initDrawHookPaint(){
        mPaint.setAlpha(0xFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mStrokeColor);
    }

    private void initDrawStrokeCirclePaint(){
        mPaint.setAlpha(0xFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setColor(mStrokeColor);
    }

    private void initDrawAlphaStrokeCirclePaint(){
        mPaint.setStrokeWidth(mStrokeWidth);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(mStrokeColor);
        mPaint.setAlpha(0x30);
    }

    private void initDrawInnerCirclePaint(){
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mCircleColor);
        mPaint.setAlpha(mInnerCircleAlpha);
    }

    private void startCheckedAnim(){
        ValueAnimator animator = new ValueAnimator();
        final float hookMaxValue = mHookSize + mEndLeftHookOffset - mBaseLeftHookOffset;
        final float circleMaxFraction = mHookSize / hookMaxValue;
        final float circleMaxValue = 360 / circleMaxFraction;
        animator.setFloatValues(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                mHookOffset = fraction * hookMaxValue;
                if (fraction <= circleMaxFraction) {
                    mSweepAngle = (int) ((circleMaxFraction - fraction) * circleMaxValue);
                } else {
                    mSweepAngle = 0;
                }
                Log.d(TAG,"mSweepAngle"+mSweepAngle);
                mInnerCircleAlpha = (int)(fraction*0xFF);
                invalidate();
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(mDuration).start();
    }

    private void startUnCheckedAnim(){
        ValueAnimator animator = new ValueAnimator();
        final float hookMaxValue = mHookSize + mEndLeftHookOffset - mBaseLeftHookOffset;
        final float circleMinFraction = (mEndLeftHookOffset - mBaseLeftHookOffset) / hookMaxValue;
        final float circleMaxValue = 360 / (1 - circleMinFraction);
        animator.setFloatValues(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float circleFraction = animation.getAnimatedFraction();
                float fraction = 1 - circleFraction;
                mHookOffset = fraction * hookMaxValue;
                if (circleFraction >= circleMinFraction) {
                    mSweepAngle = (int) ((circleFraction - circleMinFraction) * circleMaxValue);
                } else {
                    mSweepAngle = 0;
                }
                mInnerCircleAlpha = (int)(fraction*0xFF);
                invalidate();
            }
        });
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(mDuration).start();
    }

    private void startAnim(){
        clearAnimation();
        if (mChecked){
            startCheckedAnim();
        } else {
            startUnCheckedAnim();
        }
    }


    private int getAlphaColor(int color, int alpha){
        alpha = alpha<0? 0 : alpha;
        alpha = alpha>255? 255 : alpha;
        return (color & 0x00FFFFFF) | alpha << 24;
    }

    public boolean isChecked() {
        return mChecked;
    }


    /**
     * setChecked with Animation
     * @param checked true if checked, false if unchecked
     */
    public void setChecked(boolean checked) {
        setChecked(checked, true);
    }

    /**
     * @param checked  true if checked, false if unchecked
     * @param animation true with animation,false without animation
     */
    public void setChecked(boolean checked, boolean animation){
        if (checked == this.mChecked){
            return;
        }
        this.mChecked = checked;
        if (animation){
            startAnim();
        }else {
            if (mChecked){
                mInnerCircleAlpha = 0xFF;
                mSweepAngle = 0;
                mHookOffset = mHookSize + mEndLeftHookOffset - mBaseLeftHookOffset;
            }else {
                mInnerCircleAlpha = 0x00;
                mSweepAngle = 360;
                mHookOffset = 0;
            }
            invalidate();
        }
        if (mOnCheckedChangeListener != null){
            mOnCheckedChangeListener.onChange(mChecked);
        }
    }

    private int dip(int dip){
        return (int)getContext().getResources().getDisplayMetrics().density * dip;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return super.onSaveInstanceState();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
    }

    /**
     * setOnCheckedChangeListener
     * @param listener the OnCheckedChangeListener listener
     */
    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.mOnCheckedChangeListener = listener;
    }

    public interface OnCheckedChangeListener{
        void onChange(boolean checked);
    }
}
