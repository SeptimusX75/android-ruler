package corp.measure.abc.abcruler;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
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

        Rect drawingRect = new Rect();
        getDrawingRect(drawingRect);
        float lineStartY = drawingRect.bottom;
        float hashMarkHeight;
        ArrayList<Float> floats = new ArrayList<>();

        while (lineStartY > 0) {
            for (int i = 0; i < 16; i++) {

                double division = i / 16.0;
                if (i == 0)
                    hashMarkHeight = 50;
                else if (division % .5 == 0)
                    hashMarkHeight = 40;
                else if (division % .25 == 0)
                    hashMarkHeight = 30;
                else if (division % .125 == 0)
                    hashMarkHeight = 20;
                else if (division % .0625 == 0)
                    hashMarkHeight = 10;
                else
                    hashMarkHeight = 0;

                floats.add((float) 0);
                floats.add(lineStartY);
                floats.add(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, hashMarkHeight, mDisplayMetrics));
                floats.add(lineStartY);

                lineStartY -= mDisplayMetrics.ydpi / 16;
            }
        }
        mLines = Floats.toArray(floats);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLines(mLines, mLinePaint);
    }
}
