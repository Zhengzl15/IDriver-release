package project.idriver.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import project.idriver.R;

/**
 * Created by apple on 16-2-6.
 */
public class LocalView extends View {

    private Canvas lCanvas;
    private Bitmap bitmap;
    protected Paint paint;
    private Bitmap redCar;

    private float x;
    private float y;
    private int t;

    private String targetContent = null;
    private String targetType = null;
    private String line1 = null;
    private String line2 = null;

    public LocalView(Context context) {
        super(context);
        init(null, 0);
    }

    public LocalView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public LocalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        //初始化
        x = 0;
        y = (0-24);
        redCar = BitmapFactory.decodeResource(getResources(), R.drawable.red_car_48x48);
        setBitmap(redCar);
    }

    public void setTargetContent(String cont) {
        this.targetContent = cont;
    }

    public void setTargetType(String type) {
        this.targetType = type;
    }

    public void setLine1(String l) {
        this.line1 = l;
    }

    public void setLine2(String l) {
        this.line2 = l;
    }

    private void drawTarget() {
        if (targetContent != null && targetType != null) {
            char[] typeChars = targetType.toCharArray();
            String[] tp_strs = targetContent.split(";");
            for (int i = 0; i < tp_strs.length; i++) {
                String[] xy = tp_strs[i].split(",");
                float x = Float.parseFloat(xy[0]);
                float y = Float.parseFloat(xy[1]);
                int t = 0;
                if (typeChars[i] == '0') {
                    t = 0;
                } else if (typeChars[i] == '1') {
                    t = 1;
                } else { // typeChars[i] == '2'
                    t = 2;
                }
                setT(t);
                if (bitmap == null) {
                    return;
                }
                Rect srcRect = new Rect();
                srcRect.left = 0;
                srcRect.right = bitmap.getWidth();
                srcRect.top = 0;
                srcRect.bottom = bitmap.getHeight();
                float radio = (float) (srcRect.bottom - srcRect.top) / bitmap.getWidth();
                //dstRecF定义了要将绘制的Bitmap拉伸到哪里
                RectF dstRecF = new RectF();
                dstRecF.left = 125 - x;
                dstRecF.right = 175 - x;
                dstRecF.top = 400 - y - 24;
                float dstHeight = (dstRecF.right - dstRecF.left) * radio;
                dstRecF.bottom = dstRecF.top + dstHeight;
                lCanvas.drawBitmap(bitmap, srcRect, dstRecF, paint);
            }
        }
    }

    private void drawLine1() {
        if (line1 != null) {
            String[] line1_positions = line1.split(";");
            for (int i = 0; i < line1_positions.length; i++) {
                String[] xy = line1_positions[i].split(",");
                float x = Float.parseFloat(xy[0]);
                float y = Float.parseFloat(xy[1]);
                setT(3);
                if (bitmap == null) {
                    return;
                }
                Rect srcRect = new Rect();
                srcRect.left = 0;
                srcRect.right = bitmap.getWidth();
                srcRect.top = 0;
                srcRect.bottom = bitmap.getHeight();
                float radio = (float) (srcRect.bottom - srcRect.top) / bitmap.getWidth();
                //dstRecF定义了要将绘制的Bitmap拉伸到哪里
                RectF dstRecF = new RectF();
                dstRecF.left = 120 - x + 24;
                dstRecF.right = 130 - x + 24;
                dstRecF.top = 400 - y;
                float dstHeight = (dstRecF.right - dstRecF.left) * radio;
                dstRecF.bottom = dstRecF.top + dstHeight;
                lCanvas.drawBitmap(bitmap, srcRect, dstRecF, paint);
            }
        }
    }

    private void drawLine2() {
        if (line2 != null) {
            String[] line1_positions = line2.split(";");
            for (int i = 0; i < line1_positions.length; i++) {
                String[] xy = line1_positions[i].split(",");
                float x = Float.parseFloat(xy[0]);
                float y = Float.parseFloat(xy[1]);
                setT(4);
                if (bitmap == null) {
                    return;
                }
                Rect srcRect = new Rect();
                srcRect.left = 0;
                srcRect.right = bitmap.getWidth();
                srcRect.top = 0;
                srcRect.bottom = bitmap.getHeight();
                float radio = (float) (srcRect.bottom - srcRect.top) / bitmap.getWidth();
                //dstRecF定义了要将绘制的Bitmap拉伸到哪里
                RectF dstRecF = new RectF();
                dstRecF.left = 120 - x + 24;
                dstRecF.right = 130 - x + 24;
                dstRecF.top = 400 - y;
                float dstHeight = (dstRecF.right - dstRecF.left) * radio;
                dstRecF.bottom = dstRecF.top + dstHeight;
                lCanvas.drawBitmap(bitmap, srcRect, dstRecF, paint);
            }
        }
    }



    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.lCanvas = canvas;
        invalidate();
        drawInit();
        drawTarget();
        drawLine1();
        drawLine2();
//        Log.i("TAG", "ondaw");
        //drawBitmap(canvas);
    }

    public void drawInit(){
        //如果bitmap不存在，那么就不执行下面的绘制代码
        if(bitmap == null){
            return;
        }

        //invalidate();    //清除画布
        //绘制本车
        //redCar = BitmapFactory.decodeResource(getResources(), R.drawable.red_car_48x48);
        //canvas.drawBitmap(redCar, 125, 400, paint);
        redCar = BitmapFactory.decodeResource(getResources(), R.drawable.red_car_48x48);
        setBitmap(redCar);

        //绘制传入的target
        //绘制Bitmap的一部分，并对其拉伸
        //srcRect定义了要绘制Bitmap的哪一部分
        Rect srcRect = new Rect();
        srcRect.left = 0;
        srcRect.right = bitmap.getWidth();
        srcRect.top = 0;
        srcRect.bottom = bitmap.getHeight();
        float radio = (float)(srcRect.bottom - srcRect.top)  / bitmap.getWidth();
        //dstRecF定义了要将绘制的Bitmap拉伸到哪里
        RectF dstRecF = new RectF();
        dstRecF.left = 125-x;
        dstRecF.right = 175-x;
        dstRecF.top = 400-y - 24;
        float dstHeight = (dstRecF.right - dstRecF.left) * radio;
        dstRecF.bottom = dstRecF.top + dstHeight;
        lCanvas.drawBitmap(bitmap, srcRect, dstRecF, paint);
    }

    public void setBitmap(Bitmap bm){
        releaseBitmap();
        bitmap = bm;
    }

    private void releaseBitmap(){
        if(bitmap != null && !bitmap.isRecycled()){
            bitmap.recycle();
        }
        bitmap = null;
    }

    public void destroy(){
        releaseBitmap();
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setT(int t) {
        this.t = t;
        Bitmap targetIcon;
        if (t == 0) {
            targetIcon = BitmapFactory.decodeResource(getResources(), R.drawable.yellow_car_48x48);
        } else if (t == 1) {
            targetIcon = BitmapFactory.decodeResource(getResources(), R.drawable.walker_48x48);
        } else if (t == 2) {
            targetIcon = BitmapFactory.decodeResource(getResources(), R.drawable.roadblock_48x48);
        } else if (t == 3) {
            targetIcon = BitmapFactory.decodeResource(getResources(), R.drawable.little_circle_red_10x10);
        } else {    // if (t == 4) {
            targetIcon = BitmapFactory.decodeResource(getResources(), R.drawable.little_circle_green_10x10);
        }
        setBitmap(targetIcon);
    }

}
