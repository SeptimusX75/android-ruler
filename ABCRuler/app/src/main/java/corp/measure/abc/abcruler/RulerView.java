package corp.measure.abc.abcruler;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

import com.google.common.primitives.Floats;

import java.util.ArrayList;

/**
 * Created by M. Silva on 5/9/16.
 */
public class RulerView extends View {
    public static final double HALF = .5;
    public static final double QUARTER = .25;
    public static final double EIGHT = .125;
    public static final double SIXTEENTH = .0625;
    public static final int WHOLE = 0;
    private Paint mLinePaint;
    private Paint mTextPaint;
    private DisplayMetrics mDisplayMetrics;
    private float[] mLines;

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

    private void init(Context context) {
        mDisplayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(mDisplayMetrics);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setColor(Color.BLACK);
        mLinePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setColor(Color.BLACK);
        mTextPaint.setTextSize(12);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // Find the max of the view
        Rect drawingRect = new Rect();
        getDrawingRect(drawingRect);
        float markPositionY = drawingRect.bottom;

        // Copy into array
        mLines = generateMarksFromMaxPosition(markPositionY);
    }

    @NonNull
    private float[] generateMarksFromMaxPosition(float markPosition) {

        ArrayList<Float> floats = new ArrayList<>();
        float markHeight;

        // Iterate while marks can still be rendered within view
        while (markPosition > 0) {

            // Subdivide marks into 1/16" increments
            for (int i = 0; i < 16; i++) {

                double subdivision = i / 16.0; // Current mark for the current inch
                markHeight = getMarkHeightDp(subdivision);

                floats.add((float) 0); // X0
                floats.add(markPosition); // Y0
                floats.add(convertDpToSp(markHeight)); // X1, Height of marks
                floats.add(markPosition); // Y1

                // decrement size of 1/16" from the total available rendering space for each mark
                markPosition -= mDisplayMetrics.ydpi / 16;
            }
        }
        return Floats.toArray(floats);
    }

    private float convertDpToSp(float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, mDisplayMetrics);
    }

    private float getMarkHeightDp(double subdivision) {
        if (subdivision == WHOLE) {
            return 50;
        } else if (subdivision % HALF == 0) {
            return 40;
        } else if (subdivision % QUARTER == 0) {
            return 30;
        } else if (subdivision % EIGHT == 0) {
            return 20;
        } else if (subdivision % SIXTEENTH == 0) {
            return 10;
        } else {
            return 0;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLines(mLines, mLinePaint);
    }
}
