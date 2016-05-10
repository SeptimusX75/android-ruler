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
import android.view.View;
import android.view.WindowManager;

/**
 * Created by M. Silva on 5/9/16.
 */
public class RulerView extends View {
    public static final int WHOLE = 0;
    public static final double HALF = .5;
    public static final double QUARTER = .25;
    public static final double EIGHTH = .125;
    public static final double SIXTEENTH = .0625;
    public static final int TEXT_SIZE = 8;
    public static final double TICKS_IN_AN_INCH = 16.0;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private DisplayMetrics mDisplayMetrics;
    private TickMark[] mTickMarks;
    private boolean mDisplayAllMarks = false;

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

    public void setDisplayAllMarks(boolean displayAllMarks) {
        // Set only if value changes and when it does, update the UI
        if (displayAllMarks != mDisplayAllMarks) {
            mDisplayAllMarks = displayAllMarks;
            invalidate();
        }
    }

    private void init(Context context) {
        mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(convertSpToPx(TEXT_SIZE));
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // Find the max of the view
        Rect drawingRect = new Rect();
        getDrawingRect(drawingRect);

        // Copy into array
        mTickMarks = generateTickMarksFromMaxPosition(drawingRect.right);
    }

    @NonNull
    private TickMark[] generateTickMarksFromMaxPosition(float maxPosition) {
        float currentMarkPos = 0;
        TickMark[] tickMarks = new TickMark[(int) (maxPosition / TICKS_IN_AN_INCH)];

        for (int i = 0; i < tickMarks.length; i++) {
            TickMark tickMark = new TickMark();

            tickMark.rank = (int) (i % TICKS_IN_AN_INCH);
            tickMark.value = i / TICKS_IN_AN_INCH;
            tickMark.subdivision = getSubdivisionFromRank(tickMark.rank);

            tickMark.height = convertDpToPx(getTickHeightFromSubdivision(tickMark.subdivision));
            float labelPos = tickMark.height + convertDpToPx(12);

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

    private double getSubdivisionFromRank(int rank) {
        double value = rank / 16.0;
        if (rank == 0) {
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

    private String getTickLabel(TickMark tickMark) {
        int rank = tickMark.rank;

        if (rank == 0) {
            return String.valueOf(((int) tickMark.value));
        } else {
            double subdivision = tickMark.subdivision;
            // Return values are the rank divided by the GCD
            // i.e. 8/16 returns rank 8 divided by 8 over 2
            if (subdivision == HALF) {
                return rank / 8 + "/" + 2;
            } else if (subdivision == QUARTER) {
                return rank / 4 + "/" + 4;
            } else if (subdivision == EIGHTH) {
                return rank / 2 + "/" + 8;
            } else if (subdivision == SIXTEENTH) {
                return rank + "/" + 16;
            } else {
                return "";
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (TickMark tickMark : mTickMarks) {
            canvas.drawLines(tickMark.points, mLinePaint);
            mTextPaint.setTextSize(tickMark.textSize);

            boolean isDetailMark =
                    tickMark.subdivision == EIGHTH || tickMark.subdivision == SIXTEENTH;

            if (mDisplayAllMarks || !isDetailMark) {
                canvas.drawText(
                        tickMark.label,
                        tickMark.labelPosition.x,
                        tickMark.labelPosition.y,
                        mTextPaint
                );
            }
        }
    }

    private static class TickMark {
        float[] points = new float[4];
        int rank;
        double value;
        double subdivision;
        String label;
        Point labelPosition;
        float height;
        float textSize;
    }
}
