package com.nenoff.bluecard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

/**
 * TODO: document your custom view class.
 */
public class CarSteeringView extends View {
    private final static float MIN_POW = 0.20f; // min power (e.g 0.5V from 5V input)
    private final static float MOTOR_BOOST = 1.2f; // boost factor for the main motor control

    private CarController carController;

    private boolean isSteering = false;
    private float sTouchX;
    private float sTouchY;
    private float touchX;
    private float touchY;

    private byte lastLR;
    private byte lastFB;

    private Paint p = new Paint();
    private Rect rect;

    public CarSteeringView(Context context) {
        super(context);
        init();
    }

    public CarSteeringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CarSteeringView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public void setCarController(CarController carController) {
        this.carController = carController;
    }

    private void init() {
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                CarSteeringView.this.getViewTreeObserver().removeOnPreDrawListener(this);

                touchX = getWidth() / 2f;
                touchY = getHeight() / 2f;
                rect = new Rect(0, 0, getWidth(), getHeight());

                return true;
            }
        });
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        p.reset();

        // fill background
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFFC2C2C2);

        canvas.drawRect(rect, p);

        // draw square pattern
        p.setColor(0xFFECECEC);
        p.setStyle(Paint.Style.STROKE);
        float dw = getWidth()/10f;
        float dh = getHeight()/10f;
        for(int i=0; i<10; i++) {
            canvas.drawLine(0f, i*dw, getWidth(), i*dw, p);
            canvas.drawLine(i*dh, 0f, i*dh, getHeight(), p);
        }

        // draw steering circle
        p.setStrokeWidth(10f);
        float r = getWidth() / 7f;
        p.setColor(0xFF00FF00);
        canvas.drawCircle(this.touchX, this.touchY, r, p);
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

        return dr < getWidth() / 10f;
    }

    private void touchMove(float x, float y) {
        if(this.isSteering) {
            this.touchX = x;
            this.touchY = y;
            calcSteering();
        }
    }

    private long lsteer = System.currentTimeMillis();
    private void calcSteering() {
        float dx = normalizeInput((this.touchX - this.sTouchX) / (getWidth() / 2f));
        float dy = capPower(normalizeInput(MOTOR_BOOST * (this.sTouchY - this.touchY) / (getHeight() / 2f)));

        byte lr = convertToByteAngle(dx); // 90 is straight, > 90 is right, < 90 is left
        byte fb = convertToByte(dy); // 128 is no power, > 128 is forward, < 128 is backward

        if (lr != this.lastLR || fb != this.lastFB) {
            long nsteer = System.currentTimeMillis();
            long diff = nsteer - lsteer;
            if(diff > 50l) {
                lsteer = nsteer;

                updateSteering(lr, fb);
            }
        }
    }

    private void updateSteering(byte newLR, byte newFB) {
        this.lastLR = newLR;
        this.lastFB = newFB;

        this.carController.steer(newLR, newFB);
    }

    // cap to +/- 1
    private float normalizeInput(float input) {
        return Math.min(1f, Math.max (-1f, input));
    }

    // cut low power (e.g. can't go under 0.5V on 5V power supply)
    // min value +/- 0.2
    private float capPower(float input) {
        if(input < 0f && input > -MIN_POW)
            return 0f;

        if(input > 0f && input < MIN_POW)
            return 0f;

        return input;
    }

    private byte convertToByteAngle(float v) {
        if(Math.abs(v) < 0.1f) // drive straight if almost in the middle
            v = 0f;

        return (byte)(90 - Math.round(v * 70f)); // 20 to 160 degrees,
    }

    private byte convertToByte(float v) {
        return (byte)(128 + Math.round(v * 127f));
   }

    private void touchUp() {
        this.isSteering = false;
        this.touchX = getWidth() / 2f;
        this.touchY = getHeight() / 2f;
        this.sTouchX = this.touchX;
        this.sTouchY = this.touchY;
        calcSteering();
        createStopMotorAsyncTask().execute();
    }

    private AsyncTask<Void, Void, Void> createStopMotorAsyncTask() {
        return new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    Thread.sleep(50l);
                    calcSteering();
                }catch(InterruptedException ie) { ie.printStackTrace(); }
                for(int i=0; i<2; i++){
                    try {
                        Thread.sleep(100l);
                    }catch(InterruptedException ie) { ie.printStackTrace(); }
                    carController.steer(lastLR, lastFB);
                }

                return null;
            }
        };
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN :
                touchStart(x, y);
                break;
            case MotionEvent.ACTION_MOVE :
                touchMove(x, y);
                break;
            case MotionEvent.ACTION_UP :
            case MotionEvent.ACTION_CANCEL :
                touchUp();
                break;
        }
        invalidate();

        return true;
    }
}
