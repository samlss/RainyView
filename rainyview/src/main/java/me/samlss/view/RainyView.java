package me.samlss.view;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

/**
 * @author SamLeung
 * @e-mail samlssplus@gmail.com
 * @github https://github.com/samlss
 * @description A rainy rainy rainy view. ( ˘•灬•˘ )
 */
public class RainyView extends View {
    private final static int DEFAULT_SIZE = 300; //the default size if set "wrap_content"

    private final static int DEFAULT_DROP_MAX_NUMBER = 30; //Number of raindrops that can coexist at the same time
    private final static int DEFAULT_DROP_CREATION_INTERVAL = 50; //the default drop creation interval in millis
    private final static int DEFAULT_DROP_MAX_LENGTH = 50; //the default max length of drop
    private final static int DEFAULT_DROP_MIN_LENGTH = 10; //the default max length of drop
    private final static int DEFAULT_DROP_SIZE = 15; //the default drop size of drop
    private final static float DEFAULT_DROP_MAX_SPEECH = 5f; //the default max speech value
    private final static float DEFAULT_DROP_MIN_SPEECH = 1f; //the default max speech value
    private final static float DEFAULT_DROP_SLOPE = -3f; // the default drop slope

    private final static int DEFAULT_LEFT_CLOUD_COLOR = Color.parseColor("#B0B0B0");
    private final static int DEFAULT_RIGHT_CLOUD_COLOR = Color.parseColor("#DFDFDF");
    private final static int DEFAULT_RAIN_COLOR = Color.parseColor("#80B9C5");
    private final static float CLOUD_SCALE_RATIO = 0.85f;

    private Paint mLeftCloudPaint;
    private Paint mRightCloudPaint;
    private Paint mRainPaint;

    private int mLeftCloudColor = DEFAULT_LEFT_CLOUD_COLOR;
    private int mRightCloudColor = DEFAULT_RIGHT_CLOUD_COLOR;
    private int mRainColor = DEFAULT_RAIN_COLOR;

    //There are two clouds in this view, includes the left cloud & right cloud
    private Path mLeftCloudPath; //the left cloud's path
    private Path mRightCloudPath; //the right cloud's path

    private RectF mRainRect; //the rain rect
    private RectF mRainClipRect; //the rain clip rect

    private ValueAnimator mLeftCloudAnimator;
    private ValueAnimator mRightCloudAnimator;

    private long mLeftCloudAnimatorPlayTime;
    private long mRightCloudAnimatorPlayTime;

    private float mMaxTranslationX; //The max translation x when do animation.
    private float mLeftCloudAnimatorValue; //The left cloud animator value
    private float mRightCloudAnimatorValue; //The right cloud animator value

    private Path mComputePath = new Path(); //The path for computing
    private Matrix mComputeMatrix = new Matrix(); //The matrix for computing

    private List<RainDrop> mRainDrops; //all the rain drops
    private List<RainDrop> mRemovedRainDrops; //help to record the removed drops, avoid "java.util.ConcurrentModificationException"
    private Stack<RainDrop> mRecycler;

    private Random mOnlyRandom = new Random(); //the only random object
    private Handler mHandler = new Handler(); //help to update the raindrops state task

    private int mRainDropMaxNumber = DEFAULT_DROP_MAX_NUMBER;
    private int mRainDropCreationInterval = DEFAULT_DROP_CREATION_INTERVAL;
    private int mRainDropMinLength = DEFAULT_DROP_MIN_LENGTH;
    private int mRainDropMaxLength = DEFAULT_DROP_MAX_LENGTH;
    private int mRainDropSize = DEFAULT_DROP_SIZE;

    private float mRainDropMaxSpeed = DEFAULT_DROP_MAX_SPEECH;
    private float mRainDropMinSpeed = DEFAULT_DROP_MIN_SPEECH;
    private float mRainDropSlope = DEFAULT_DROP_SLOPE;

    private long mRainDropCreationTime;
    
    public RainyView(Context context) {
        this(context, null);
    }

    public RainyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RainyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        parseAttrs(attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RainyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        parseAttrs(attrs);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);

        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);
        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);

        int w = widthSpecSize;
        int h = heightSpecSize;

        if (widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            w = DEFAULT_SIZE;
            h = DEFAULT_SIZE;
        } else if (widthSpecMode == MeasureSpec.AT_MOST) {
            w = DEFAULT_SIZE;
            h = heightSpecSize;
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            w = widthSpecSize;
            h = DEFAULT_SIZE;
        }

        setMeasuredDimension(w, h);
    }

    private void parseAttrs(AttributeSet attrs){
        if (attrs == null){
            return;
        }
        
        TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.RainyView);
        mLeftCloudColor = typedArray.getColor(R.styleable.RainyView_left_cloud_color, DEFAULT_LEFT_CLOUD_COLOR);
        mRightCloudColor  = typedArray.getColor(R.styleable.RainyView_right_cloud_color, DEFAULT_RIGHT_CLOUD_COLOR);
        mRainColor = typedArray.getColor(R.styleable.RainyView_raindrop_color, DEFAULT_RAIN_COLOR);
        mRainDropMaxNumber  = typedArray.getInteger(R.styleable.RainyView_raindrop_max_number, DEFAULT_DROP_MAX_NUMBER);
        mRainDropMinLength  = typedArray.getInteger(R.styleable.RainyView_raindrop_min_length, DEFAULT_DROP_MIN_LENGTH);
        mRainDropMaxLength = typedArray.getInteger(R.styleable.RainyView_raindrop_max_length, DEFAULT_DROP_MAX_LENGTH);
        mRainDropMinSpeed  = typedArray.getFloat(R.styleable.RainyView_raindrop_min_speed, DEFAULT_DROP_MIN_SPEECH);
        mRainDropMaxSpeed  = typedArray.getFloat(R.styleable.RainyView_raindrop_max_speed, DEFAULT_DROP_MAX_SPEECH);
        mRainDropCreationInterval = typedArray.getInteger(R.styleable.RainyView_raindrop_creation_interval, DEFAULT_DROP_CREATION_INTERVAL);
        mRainDropSize = typedArray.getInteger(R.styleable.RainyView_raindrop_size, DEFAULT_DROP_SIZE);
        mRainDropSlope = typedArray.getFloat(R.styleable.RainyView_raindrop_slope, DEFAULT_DROP_SLOPE);

        typedArray.recycle();
        
        checkValue();
    }

    private void init(){
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        mLeftCloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLeftCloudPaint.setColor(mLeftCloudColor);
        mLeftCloudPaint.setStyle(Paint.Style.FILL);

        mRightCloudPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRightCloudPaint.setColor(mRightCloudColor);
        mRightCloudPaint.setStyle(Paint.Style.FILL);

        mRainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRainPaint.setStrokeCap(Paint.Cap.ROUND);
        mRainPaint.setColor(mRainColor);
        mRainPaint.setStyle(Paint.Style.STROKE);
        mRainPaint.setStrokeWidth(mRainDropSize);

        mLeftCloudPath  = new Path();
        mRightCloudPath = new Path();
        mRainRect = new RectF();
        mRainClipRect = new RectF();

        mRainDrops = new ArrayList<>(mRainDropMaxNumber);
        mRemovedRainDrops = new ArrayList<>(mRainDropMaxNumber);
        mRecycler = new Stack<>();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stop();

        mLeftCloudPath.reset();
        mRightCloudPath.reset();

        float centerX = w / 2; //view's center x coordinate
        float minSize = Math.min(w, h); //get the min size

        //************************compute left cloud**********************

        float leftCloudWidth = minSize / 2.5f; //the width of cloud
        float leftCloudBottomHeight = leftCloudWidth / 3f; //the bottom height of cloud
        float leftCloudBottomRoundRadius = leftCloudBottomHeight; //the bottom round radius of cloud

        float rightCloudTranslateX = leftCloudWidth * 2 / 3; //the distance of the cloud on the right
        float leftCloudEndX = (w - leftCloudWidth - leftCloudWidth * CLOUD_SCALE_RATIO / 2) / 2 + leftCloudWidth; //the left cloud end x coordinate
        float leftCloudEndY = h / 3; //clouds' end y coordinate

        //add the bottom round rect
        mLeftCloudPath.addRoundRect(new RectF(leftCloudEndX - leftCloudWidth, leftCloudEndY - leftCloudBottomHeight,
                leftCloudEndX, leftCloudEndY), leftCloudBottomHeight, leftCloudBottomRoundRadius, Path.Direction.CW);

        float leftCloudTopCenterY = leftCloudEndY - leftCloudBottomHeight;
        float leftCloudRightTopCenterX = leftCloudEndX - leftCloudBottomRoundRadius;
        float leftCloudLeftTopCenterX  = leftCloudEndX - leftCloudWidth + leftCloudBottomRoundRadius;

        mLeftCloudPath.addCircle(leftCloudRightTopCenterX, leftCloudTopCenterY, leftCloudBottomRoundRadius * 3 / 4, Path.Direction.CW);
        mLeftCloudPath.addCircle(leftCloudLeftTopCenterX, leftCloudTopCenterY, leftCloudBottomRoundRadius / 2, Path.Direction.CW);
        //*******************************Done*****************************

        //************************compute right cloud**********************
        //The cloud on the right is CLOUD_SCALE_RATIO size of the left
        float rightCloudCenterX = rightCloudTranslateX + centerX - leftCloudWidth / 2; //the right cloud center x

        RectF calculateRect = new RectF();
        mLeftCloudPath.computeBounds(calculateRect, false); //compute the left cloud's path bounds

        mComputeMatrix.reset();
        mComputeMatrix.preTranslate(rightCloudTranslateX, -calculateRect.height() * (1 - CLOUD_SCALE_RATIO) / 2);
        mComputeMatrix.postScale(CLOUD_SCALE_RATIO, CLOUD_SCALE_RATIO, rightCloudCenterX, leftCloudEndY);
        mLeftCloudPath.transform(mComputeMatrix, mRightCloudPath);

        float left = calculateRect.left + leftCloudBottomRoundRadius;
        mRightCloudPath.computeBounds(calculateRect, false); //compute the right cloud's path bounds

        float right = calculateRect.right;
        float top   = calculateRect.bottom;
        //************************compute right cloud**********************
        mRainRect.set(left, top, right, h * 3 / 4f); //compute the rect of rain...
        mRainClipRect.set(0, mRainRect.top, w, mRainRect.bottom);

        mMaxTranslationX = leftCloudBottomRoundRadius / 2;
        setupAnimator();
    }

    private void setupAnimator(){
        mLeftCloudAnimatorPlayTime = 0;
        mRightCloudAnimatorPlayTime = 0;

        mLeftCloudAnimator = ValueAnimator.ofFloat(0, 1);
        mLeftCloudAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mLeftCloudAnimator.setDuration(1000);
        mLeftCloudAnimator.setInterpolator(new LinearInterpolator());
        mLeftCloudAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mLeftCloudAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mLeftCloudAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mRightCloudAnimator = ValueAnimator.ofFloat(1, 0f);
        mRightCloudAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mRightCloudAnimator.setDuration(800);
        mRightCloudAnimator.setInterpolator(new LinearInterpolator());
        mRightCloudAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mRightCloudAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRightCloudAnimatorValue = (float) animation.getAnimatedValue();
                invalidate();
            }
        });

        mLeftCloudAnimator.start();
        mRightCloudAnimator.start();
        mHandler.post(mTask);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
//        canvas.drawRect(mRainRect, new Paint());
        canvas.clipRect(mRainClipRect);
        drawRainDrops(canvas);
        canvas.restore();

        mComputeMatrix.reset();
        mComputeMatrix.postTranslate((mMaxTranslationX / 2) * mRightCloudAnimatorValue, 0);
        mRightCloudPath.transform(mComputeMatrix, mComputePath);
        canvas.drawPath(mComputePath, mRightCloudPaint);

        mComputeMatrix.reset();
        mComputeMatrix.postTranslate(mMaxTranslationX * mLeftCloudAnimatorValue, 0);
        mLeftCloudPath.transform(mComputeMatrix, mComputePath);
        canvas.drawPath(mComputePath, mLeftCloudPaint);
    }

    /**
     * Start the animation.
     * */
    public void start(){
        if (mLeftCloudAnimator != null
                && !mLeftCloudAnimator.isRunning()){
            mLeftCloudAnimator.setCurrentPlayTime(mLeftCloudAnimatorPlayTime);
            mLeftCloudAnimator.start();
        }

        if (mRightCloudAnimator != null
                && !mRightCloudAnimator.isRunning()){
            mRightCloudAnimator.setCurrentPlayTime(mRightCloudAnimatorPlayTime);
            mRightCloudAnimator.start();
        }

        mHandler.removeCallbacks(mTask);
        mHandler.post(mTask);
    }

    /**
     * Stop the animation
     * */
    public void stop(){
        if (mLeftCloudAnimator != null && mLeftCloudAnimator.isRunning()){
            mLeftCloudAnimatorPlayTime = mLeftCloudAnimator.getCurrentPlayTime();
            mLeftCloudAnimator.cancel();
        }

        if (mRightCloudAnimator != null && mRightCloudAnimator.isRunning()){
            mRightCloudAnimatorPlayTime = mRightCloudAnimator.getCurrentPlayTime();
            mRightCloudAnimator.cancel();
        }

        mHandler.removeCallbacks(mTask);
    }

    /**
     * Release this view
     * */
    public void release(){
        stop();

        if (mLeftCloudAnimator != null){
            mLeftCloudAnimator.removeAllUpdateListeners();
        }

        if (mRightCloudAnimator != null){
            mRightCloudAnimator.removeAllUpdateListeners();
        }

        mRemovedRainDrops.clear();
        mRainDrops.clear();
        mRecycler.clear();
        mHandler = null;
    }


    /**
     * To optimize performance, use recycler {@link #mRecycler}
     * */
    private RainDrop obtainRainDrop(){
        if (mRecycler.isEmpty()){
            return new RainDrop();
        }

        return mRecycler.pop();
    }

    /**
     * Recycling the drop that are no longer in use
     * */
    private void recycle(RainDrop rainDrop){
        if (rainDrop == null){
            return;
        }

        if (mRecycler.size() >= mRainDropMaxNumber){
            mRecycler.pop();
        }

        mRecycler.push(rainDrop);
    }

    /**
     * The drop's handled task.
     * Call handler to schedule the task.
     * */
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {
            createRainDrop();
            updateRainDropState();

            mHandler.postDelayed(this, 20);
        }
    };

    /**
     * Now create a random raindrop.
     * */
    private void createRainDrop() {
        if (mRainDrops.size() >= mRainDropMaxNumber
                || mRainRect.isEmpty()) {
            return;
        }

        long current = System.currentTimeMillis();
        if ((current - mRainDropCreationTime) < mRainDropCreationInterval){
            return;
        }

        if (mRainDropMinLength > mRainDropMaxLength
                || mRainDropMinSpeed > mRainDropMaxSpeed){
            throw new IllegalArgumentException("The minimum value cannot be greater than the maximum value.");
        }

        mRainDropCreationTime = current;

        RainDrop rainDrop = obtainRainDrop();
        rainDrop.slope = mRainDropSlope;
        rainDrop.speedX = mRainDropMinSpeed + mOnlyRandom.nextFloat() * mRainDropMaxSpeed;
        rainDrop.speedY = rainDrop.speedX * Math.abs(rainDrop.slope);

        float rainDropLength = mRainDropMinLength + mOnlyRandom.nextInt(mRainDropMaxLength - mRainDropMinLength);
        double degree = Math.toDegrees(Math.atan(rainDrop.slope));

        rainDrop.xLength = (float) Math.abs(Math.cos(degree * Math.PI/180) * rainDropLength);
        rainDrop.yLength = (float) Math.abs(Math.sin(degree * Math.PI/180) * rainDropLength);

        rainDrop.x = mRainRect.left + mOnlyRandom.nextInt((int) mRainRect.width()); //random x coordinate
        rainDrop.y = mRainRect.top - rainDrop.yLength; //the fixed y coordinate

        mRainDrops.add(rainDrop);
    }


    /**
     * Update all the raindrops state
     * */
    private void updateRainDropState() {
        mRemovedRainDrops.clear();

        for (RainDrop rainDrop : mRainDrops) {
            if (rainDrop.y - rainDrop.yLength > mRainRect.bottom) {
                mRemovedRainDrops.add(rainDrop);
                recycle(rainDrop);
            } else {
                if (rainDrop.slope >= 0) {
                    rainDrop.x += rainDrop.speedX;
                }else{
                    rainDrop.x -= rainDrop.speedX;
                }
                rainDrop.y += rainDrop.speedY;
            }
        }

        if (!mRemovedRainDrops.isEmpty()) {
            mRainDrops.removeAll(mRemovedRainDrops);
        }

        if (!mRainDrops.isEmpty()){
            invalidate();
        }
    }

    private void drawRainDrops(Canvas canvas){
        for (RainDrop rainDrop : mRainDrops){
            canvas.drawLine(rainDrop.x, rainDrop.y,
                    rainDrop.slope > 0 ? rainDrop.x + rainDrop.xLength : rainDrop.x - rainDrop.xLength,
                    rainDrop.y + rainDrop.yLength,
                    mRainPaint);
        }
    }

    /**
     * The rain drop class
     * */
    private class RainDrop{
        float speedX;   //the drop's x coordinate speed
        float speedY;   //the drop's y coordinate speed
        float xLength; //the drop's x length
        float yLength; //the drop's y length
        float x;        //the drop's start x
        float y;        //the drop's start y
        float slope; //the drop's slope
    }

    private void checkValue(){
        checkRainDropCreationIntervalValue();
        checkRainDropLengthValue();
        checkRainDropManNumberValue();
        checkRainDropSizeValue();
        checkRainDropSpeedValue();
        checkRainDropSlopeValue();
    }

    private void checkRainDropManNumberValue(){
        if (mRainDropMaxNumber < 0){
            mRainDropMaxNumber = DEFAULT_DROP_MAX_NUMBER;
        }
    }

    private void checkRainDropSizeValue(){
        if (mRainDropSize < 0){
            mRainDropSize = DEFAULT_DROP_SIZE;
        }
    }

    private void checkRainDropCreationIntervalValue(){
        if (mRainDropCreationInterval < 0){
            mRainDropCreationInterval = DEFAULT_DROP_CREATION_INTERVAL;
        }
    }

    private void checkRainDropLengthValue(){
        if (mRainDropMinLength < 0
                || mRainDropMaxLength < 0){
            mRainDropMinLength = DEFAULT_DROP_MIN_LENGTH;
            mRainDropMaxLength = DEFAULT_DROP_MAX_LENGTH;
        }
    }

    private void checkRainDropSpeedValue(){
        if (mRainDropMinSpeed < 0
                || mRainDropMaxSpeed < 0){
            mRainDropMinSpeed = DEFAULT_DROP_MIN_SPEECH;
            mRainDropMaxSpeed = DEFAULT_DROP_MAX_SPEECH;
        }
    }

    private void checkRainDropSlopeValue(){
        if (mRainDropSlope < 0){
            mRainDropSlope = DEFAULT_DROP_SLOPE;
        }
    }


    /**
     * Set the color of the left cloud
     * */
    public void setLeftCloudColor(int leftCloudColor) {
        this.mLeftCloudColor = leftCloudColor;
        mLeftCloudPaint.setColor(mLeftCloudColor);
        postInvalidate();
    }

    /**
     * Get the color of the left cloud
     * */
    public int getLeftCloudColor() {
        return mLeftCloudColor;
    }


    /**
     * Set the color of the left cloud
     * */
    public void setRightCloudColor(int rightCloudColor) {
        this.mRightCloudColor = rightCloudColor;
        mRightCloudPaint.setColor(mRightCloudColor);
        postInvalidate();
    }

    /**
     * Get the color of the right cloud
     * */
    public int getRightCloudColor() {
        return mRightCloudColor;
    }

    /**
     * Set the color of all the raindrops
     * */
    public void setRainDropColor(int rainDropColor) {
        this.mRainColor = rainDropColor;
        mRainPaint.setColor(mRainColor);
        postInvalidate();
    }

    /**
     * Get the color of all the raindrops
     * */
    public int getRainDropColor() {
        return mRainColor;
    }

    /**
     * Get the max number of the {@link RainDrop}
     * */
    public int getRainDropMaxNumber() {
        return mRainDropMaxNumber;
    }

    /**
     * Set the max number of the {@link RainDrop}
     * */
    public void setRainDropMaxNumber(int rainDropMaxNumber) {
        this.mRainDropMaxNumber = rainDropMaxNumber;
        checkRainDropManNumberValue();
    }

    /**
     * Get the creation interval of the {@link RainDrop}
     * */
    public int getRainDropCreationInterval() {
        return mRainDropCreationInterval;
    }

    /**
     * Get the creation interval of the {@link RainDrop}
     * */
    public void setRainDropCreationInterval(int rainDropCreationInterval) {
        this.mRainDropCreationInterval = rainDropCreationInterval;
        checkRainDropCreationIntervalValue();
        postInvalidate();
    }

    /**
     * Get the min length of the {@link RainDrop}
     * */
    public int getRainDropMinLength() {
        return mRainDropMinLength;
    }

    /**
     * Set the min length of the {@link RainDrop}
     * */
    public void setRainDropMinLength(int rainDropMinLength) {
        this.mRainDropMinLength = rainDropMinLength;
        checkRainDropLengthValue();
    }

    /**
     * Get the max length of the {@link RainDrop}
     * */
    public int getRainDropMaxLength() {
        return mRainDropMaxLength;
    }

    /**
     * Set the max length of the {@link RainDrop}
     * */
    public void setRainDropMaxLength(int rainDropMaxLength) {
        this.mRainDropMaxLength = rainDropMaxLength;
        checkRainDropLengthValue();
    }

    /**
     * Get the size of the {@link RainDrop}
     * */
    public int getRainDropSize() {
        return mRainDropSize;
    }

    /**
     * Set the size of the {@link RainDrop}
     * */
    public void setRainDropSize(int rainDropSize) {
        this.mRainDropSize = rainDropSize;
        checkRainDropSizeValue();
    }

    /**
     * Get the max speed of the {@link RainDrop}
     * */
    public float getRainDropMaxSpeed() {
        return mRainDropMaxSpeed;
    }

    /**
     * Set the max speed of the {@link RainDrop}
     * */
    public void setRainDropMaxSpeed(float rainDropMaxSpeed) {
        this.mRainDropMaxSpeed = rainDropMaxSpeed;
        checkRainDropSpeedValue();
    }

    /**
     * Get the minimum speed of the {@link RainDrop}
     * */
    public float getRainDropMinSpeed() {
        return mRainDropMinSpeed;
    }

    /**
     * Set the minimum speed of the {@link RainDrop}
     * */
    public void setRainDropMinSpeed(float rainDropMinSpeed) {
        this.mRainDropMinSpeed = rainDropMinSpeed;
        checkRainDropSpeedValue();
    }

    /**
     * Get the slope of the {@link RainDrop}
     * */
    public float getRainDropSlope() {
        return mRainDropSlope;
    }

    /**
     * Set the slope of the {@link RainDrop}
     * */
    public void setRainDropSlope(float rainDropSlope) {
        this.mRainDropSlope = rainDropSlope;
        checkRainDropSlopeValue();
    }
}
