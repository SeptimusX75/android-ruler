package corp.measure.abc.abcruler;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by M. Silva on 5/9/16.
 */
public class RulerView extends View {
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


        float lineStartY = 0;
        float lines = mDisplayMetrics.heightPixels / mDisplayMetrics.ydpi;
        mLines = new float[(int) lines * 4];
        for (int i = 0; (i + 4) <= mLines.length; i += 4) {
            mLines[i] = 0;
            mLines[i + 2] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50, mDisplayMetrics);
            mLines[i + 1] = lineStartY;
            mLines[i + 3] = lineStartY;

            lineStartY += mDisplayMetrics.ydpi;
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLines(mLines, mLinePaint);
    }
}
