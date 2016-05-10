package corp.measure.abc.abcruler;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.Locale;

/**
 * Created by M. Silva on 5/9/16.
 * Custom {@linkplain View} that renders a fairly accurate ruler within the designated bounds
 */
public class RulerView extends View {
    public static final int WHOLE = 0;
    public static final double HALF = .5;
    public static final double QUARTER = .25;
    public static final double EIGHTH = .125;
    public static final double SIXTEENTH = .0625;

    // TODO: 5/10/16 make these configurable
    public static final int TEXT_SIZE = 8;
    public static final double TICKS_IN_AN_INCH = 16.0;
    public static final int CENTER_TEXT_SIZE = 40;
    public static final int TICK_TEXT_PADDING = 12;
    private boolean mDisplayAllLabels = false;

    private float mTouchLinePos = 0;
    private Paint mLinePaint;
    private Paint mTouchLinePaint;
    private Paint mTextPaint;
    private Paint mCenterTextPaint;
    private DisplayMetrics mDisplayMetrics;
    private Rect mDrawingRect;
    private TickMark[] mTickMarks;

    public RulerView(Context context) {
        super(context);
        init(context);
    }

    public RulerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RulerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public RulerView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    /**
     * Sets and renders more granular labels on marks
     *
     * @param displayAllLabels true to render all labels down to 1/16th of 1"
     */
    public void setDisplayAllLabels(boolean displayAllLabels) {
        // Set only if value changes and when it does, update the UI
        if (displayAllLabels != mDisplayAllLabels) {
            mDisplayAllLabels = displayAllLabels;
            invalidate();
        }
    }

    private void init(Context context) {
        // Retrieve the phone metrics
        mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTouchLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTouchLinePaint.setColor(Color.RED);
        mTouchLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(convertSpToPx(TEXT_SIZE));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mCenterTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterTextPaint.setColor(Color.BLACK);
        mCenterTextPaint.setTextSize(convertSpToPx(CENTER_TEXT_SIZE));
        mCenterTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Find the max of the view
        mDrawingRect = new Rect();
        getDrawingRect(mDrawingRect);

        // Copy into array
        mTickMarks = generateTickMarksFromMaxPosition(mDrawingRect.right);
    }

    /**
     * Generates tick mark objects containing information to render them in the designated bounds
     *
     * @param maxPosition the maximum x boundary of the view
     * @return all tick marks that can be rendered on the screen
     */
    @NonNull
    private TickMark[] generateTickMarksFromMaxPosition(float maxPosition) {
        float currentMarkPos = 0;
        TickMark[] tickMarks = new TickMark[(int) (maxPosition / TICKS_IN_AN_INCH)];

        for (int i = 0; i < tickMarks.length; i++) {
            TickMark tickMark = new TickMark();

            tickMark.numerator = (int) (i % TICKS_IN_AN_INCH);
            tickMark.value = i / TICKS_IN_AN_INCH;
            tickMark.subdivision = getSubdivisionFromNumerator(tickMark.numerator);

            tickMark.height = convertDpToPx(getTickHeightFromSubdivision(tickMark.subdivision));
            float labelPos = tickMark.height + convertDpToPx(TICK_TEXT_PADDING);

            tickMark.points[0] = currentMarkPos;
            tickMark.points[1] = 0;
            tickMark.points[2] = currentMarkPos;
            tickMark.points[3] = tickMark.height;

            tickMark.label = getTickLabel(tickMark);
            tickMark.labelPosition = new Point((int) currentMarkPos, (int) labelPos);
            tickMark.textSize = convertSpToPx(getTextSizeFromSubdivision(tickMark.subdivision));

            tickMarks[i] = tickMark;
            currentMarkPos += mDisplayMetrics.xdpi / TICKS_IN_AN_INCH;
        }

        return tickMarks;
    }

    private float convertDpToPx(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mDisplayMetrics);
    }

    private float convertSpToPx(float sp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, mDisplayMetrics);
    }

    /**
     * The fractional classification for the numerator of a tick mark.
     *
     * @param numerator of the tick within the inch
     * @return the fractional classification
     */
    private double getSubdivisionFromNumerator(int numerator) {
        double value = numerator / TICKS_IN_AN_INCH;
        if (numerator == 0) {
            return WHOLE;
        } else if (value % HALF == 0) {
            return HALF;
        } else if (value % QUARTER == 0) {
            return QUARTER;
        } else if (value % EIGHTH == 0) {
            return EIGHTH;
        } else if (value % SIXTEENTH == 0) {
            return SIXTEENTH;
        } else {
            return WHOLE;
        }
    }

    /**
     * Gets the height of a tick in DP based on the fractional classification
     *
     * @param subdivision the fractional classification
     * @return the tick height in DP
     */
    private float getTickHeightFromSubdivision(double subdivision) {
        if (subdivision == WHOLE) {
            return 50;
        } else if (subdivision == HALF) {
            return 40;
        } else if (subdivision == QUARTER) {
            return 30;
        } else if (subdivision == EIGHTH) {
            return 20;
        } else if (subdivision == SIXTEENTH) {
            return 10;
        } else {
            return 0;
        }
    }

    /**
     * These are hard coded and could possibly be configurable later
     *
     * @param subdivision the fractional classification
     * @return text size in SP
     */
    private float getTextSizeFromSubdivision(double subdivision) {
        if (subdivision == WHOLE) {
            return 12;
        } else if (subdivision == HALF) {
            return 10;
        } else if (subdivision == QUARTER) {
            return 8;
        } else if (subdivision == EIGHTH) {
            return 6;
        } else if (subdivision == SIXTEENTH) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * Gets the tick label using the tick numerator
     *
     * @param tickMark The tickmark requiring a textual label
     * @return The appropriate label for the tick's position
     */
    private String getTickLabel(TickMark tickMark) {
        int numerator = tickMark.numerator;

        if (numerator == 0) {
            return String.valueOf(((int) tickMark.value));
        } else {
            double subdivision = tickMark.subdivision;
            // Return values are the numerator divided by the GCD
            // i.e. 8/16 returns numerator 8 divided by 8 over 2
            if (subdivision == HALF) {
                return numerator / 8 + "/" + 2;
            } else if (subdivision == QUARTER) {
                return numerator / 4 + "/" + 4;
            } else if (subdivision == EIGHTH) {
                return numerator / 2 + "/" + 8;
            } else if (subdivision == SIXTEENTH) {
                return numerator + "/" + 16;
            } else {
                return "";
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the vertical measurement line for touch feedback
        canvas.drawLine(mTouchLinePos, 0, mTouchLinePos, mDrawingRect.bottom, mTouchLinePaint);

        // Render the measurement value rounded to 2 decimal places
        canvas.drawText(
                String.format(Locale.getDefault(), "%.2f", (mTouchLinePos / mDisplayMetrics.xdpi)) + " in.",
                mDrawingRect.right / 2,
                mDrawingRect.bottom / 2,
                mCenterTextPaint
        );

        // Render the tick marks
        for (TickMark tickMark : mTickMarks) {
            canvas.drawLines(tickMark.points, mLinePaint);
            //Use the same paint object for all ticks but re-size on a per-tick basis
            mTextPaint.setTextSize(tickMark.textSize);

            boolean isDetailMark =
                    tickMark.subdivision == EIGHTH || tickMark.subdivision == SIXTEENTH;

            if (mDisplayAllLabels || !isDetailMark) {
                canvas.drawText(
                        tickMark.label,
                        tickMark.labelPosition.x,
                        tickMark.labelPosition.y,
                        mTextPaint
                );
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int eventAction = event.getAction();

        float x = event.getX();

        switch (eventAction) {
            case MotionEvent.ACTION_DOWN:
                mTouchLinePos = x;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchLinePos = x;
                break;
        }

        invalidate();

        return true;
    }

    /**
     * Convenience class for storing tick mark state
     */
    private static class TickMark {
        float[] points = new float[4];
        int numerator;
        double value;
        double subdivision;
        String label;
        Point labelPosition;
        float height;
        float textSize;
    }
}
