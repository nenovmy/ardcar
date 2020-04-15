package com.nenoff.ardbttest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class CarSteeringView extends View {
    private final static float MIN_POW = 0.1f; // min power (e.g 0.5V from 5V input)

    private String mExampleString; // TODO: use a default from R.string...
    private int mExampleColor = Color.RED; // TODO: use a default from R.color...
    private float mExampleDimension = 0; // TODO: use a default from R.dimen...
    private Drawable mExampleDrawable;

    private TextPaint mTextPaint;
    private float mTextWidth;
    private float mTextHeight;

    private CarController carController;

    private boolean isSteering = false;
    private float sTouchX;
    private float sTouchY;
    private float touchX;
    private float touchY;

    private byte lastLR;
    private byte lastFB;

    public CarSteeringView(Context context) {
        super(context);
        init(null, 0);
    }

    public void setCarController(CarController carController) {
        this.carController = carController;
    }

    public CarSteeringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public CarSteeringView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CarSteeringView, defStyle, 0);

        mExampleString = a.getString(
                R.styleable.CarSteeringView_exampleString);
        mExampleColor = a.getColor(
                R.styleable.CarSteeringView_exampleColor,
                mExampleColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mExampleDimension = a.getDimension(
                R.styleable.CarSteeringView_exampleDimension,
                mExampleDimension);

        if (a.hasValue(R.styleable.CarSteeringView_exampleDrawable)) {
            mExampleDrawable = a.getDrawable(
                    R.styleable.CarSteeringView_exampleDrawable);
            mExampleDrawable.setCallback(this);
        }

        a.recycle();

        // Set up a default TextPaint object
        mTextPaint = new TextPaint();
        mTextPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextAlign(Paint.Align.LEFT);

        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {
        mTextPaint.setTextSize(mExampleDimension);
        mTextPaint.setColor(mExampleColor);
        mTextWidth = mTextPaint.measureText(mExampleString);

        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        mTextHeight = fontMetrics.bottom;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Paint p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFC2C2C2);
        Rect rect = new Rect(0, 0, getWidth(), getHeight());
        canvas.drawRect(rect, p);

        p.setColor(0xFFECECEC);
        p.setStyle(Paint.Style.STROKE);
        float dw = getWidth()/10f;
        float dh = getHeight()/10f;
        for(int i=0; i<10; i++) {
            canvas.drawLine(0f, i*dw, getWidth(), i*dw, p);
            canvas.drawLine(i*dh, 0f, i*dh, getHeight(), p);
        }

        p.setStrokeWidth(10f);
        float r = getWidth() / 10f;
        p.setColor(0xFF00FF00);
        if(this.isSteering)
            canvas.drawCircle(this.touchX, this.touchY, r, p);
        else
            canvas.drawCircle(this.getWidth()/2f,this.getHeight()/2f, r, p);
    }

    private void touchStart(float x, float y) {
        if(canStartSteeringAt(x, y)) {
            isSteering = true;
            this.touchX = x;
            this.touchY = y;
            this.sTouchX = x;
            this.sTouchY = y;
        }
    }

    private boolean canStartSteeringAt(float x, float y) {
        float dx = x - getWidth() / 2f;
        float dy = y - getHeight() / 2f;

        double dr = Math.sqrt(dx*dx + dy*dy);

        return dr < getWidth() / 20f;
    }

    private void touchMove(float x, float y) {
        if(this.isSteering) {
            this.touchX = x;
            this.touchY = y;
            calcSteering();
        }
    }

    private void calcSteering() {
        float dx = normalizeInput((this.touchX - this.sTouchX) / (getWidth() / 2));
        float dy = capPower(normalizeInput((this.sTouchY - this.touchY) / (getHeight() / 2)));

        byte lr = convertToByte(dx); // 128 is straight, > 128 is right, < 128 is left
        byte fb = convertToByte(dy); // 128 is no power, > 128 is forward, < 128 is backward

        if(lr != this.lastLR || fb != this.lastFB) {
            updateSteering(lr, fb);
        }
        Log.i("[BLE]", "steering: lr: " + lr + " fb: " + fb);
    }

    private void updateSteering(byte newLR, byte newFB) {
        this.lastLR = newLR;
        this.lastFB = newFB;

        this.carController.steer(newLR, newFB);
    }

    // cap to +/- 1
    // min value +/- 0.2
    private float normalizeInput(float input) {
        return Math.min(1f, Math.max (-1f, input));
    }

    // cut low power (e.g. can't go under 0.5V on 5V power supply)
    private float capPower(float input) {
        if(input < 0f && input > -MIN_POW)
            return 0f;

        if(input > 0f && input < MIN_POW)
            return 0f;

        return input;
    }

    private byte convertToByte(float v) {
        return (byte)(128 + (int)Math.round(v * 127f));
   }

    private void touchUp() {
        this.isSteering = false;
        this.touchX = getWidth() / 2f;
        this.touchY = getHeight() / 2f;
        this.sTouchX = this.touchX;
        this.sTouchY = this.touchY;
        calcSteering();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                Log.i("[BLE]", "on touch event action down");
                touchStart(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE :
                Log.i("[BLE]", "on touch event action move");
                touchMove(x, y);
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                Log.i("[BLE]", "on touch event action up");
                touchUp();
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL :
                Log.i("[BLE]", "on touch event action cancel");
                touchUp();
                invalidate();
                break;
        }

        return true;
    }
}
