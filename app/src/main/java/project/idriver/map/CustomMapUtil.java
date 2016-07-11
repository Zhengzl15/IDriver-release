package project.idriver.map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import project.idriver.R;
import project.idriver.beans.AtosBean;
import project.idriver.beans.GlobalmapBean;
import project.idriver.beans.GsonUtil;
import project.idriver.beans.LineBean;
import project.idriver.beans.MessageBean;
import project.idriver.nets.NetConfig;
import project.idriver.nets.ZmqService;
import project.idriver.ui.UiConfig;
import project.idriver.ui.WheelView;

/**
 * Created by ryan_wu on 16/1/27.
 */
public class CustomMapUtil extends View implements View.OnClickListener{
    private Canvas wCanvas;
    private ArrayList<Coordinate> coords = new ArrayList<Coordinate>();
    private ArrayList<String> options = new ArrayList<String>();           //起终点选项
    private Coordinate carGPS;
    private Paint mapPaint;
    private Paint carPaint;
    private Paint wordPaint;
    private WheelView startWheel;
    private WheelView endWheel;
    private double maxX = Double.MIN_VALUE;
    private double minX = Double.MAX_VALUE;
    private double maxY = Double.MIN_VALUE;
    private double minY = Double.MAX_VALUE;

    private final int MAP_RADIUS = 3;
    private final int CAR_RADIUS = 10;
    private final int MAP_PADDING = 15;
    private final int TEXT_SIZE = 20;
    private final int CANVAS_BACKGROUND_COLOR = Color.WHITE;
    private final String NO_MAP_DATA = "定制地图数据文件不存在!\n请稍后点击更新地图刷新数据";
    private final String MAP_DATA_FORMAT_ERR = "定制地图数据文件格式错误!";
    private final String SEND_MSG_ERR = "发送定制起终点失败,请检查连接";
    private final String RCV_MSG_ERR = "接受GPS数据格式错误";
    private final String canvasDrawLog = "canvas draw";

    // handle message
    private ZmqService mZmqService;
    private final Handler mZmqHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case NetConfig.ZMQ_ID:
                    String content = (String) msg.obj;
                    MessageBean message = GsonUtil.fromJson(content, MessageBean.class);
                    if(message != null && message.getStoa() != null
                            && message.getStoa().getGlobalmap() != null && message.getStoa().getGlobalmap().getGps() != null) {
                        String[] gps = message.getStoa().getGlobalmap().getGps().split(",");
                        Log.i("f", "gps: " + gps[0] + "  " + gps[1]);
                        try {
                            carGPS = new Coordinate(Double.parseDouble(gps[0].trim()), Double.parseDouble(gps[1].trim()), "gps");
                        } catch (Exception e) {
                            Toast.makeText(getContext(), RCV_MSG_ERR, Toast.LENGTH_LONG).show();
                        }
                    }
            }
        }
    };

    public CustomMapUtil(Context context) {
        super(context);
        loadMapData();
        setPaint();

        mZmqService = ZmqService.getInstance(mZmqHandler);
    }

    public void setWheel(WheelView startWheel, WheelView endWheel) {
        this.startWheel = startWheel;
        this.endWheel = endWheel;
        this.startWheel.setOffset(1);
        this.endWheel.setOffset(1);
        if (options.size() == 0) {
            options.add("N");
        }
        setWheelOption();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.wCanvas = canvas;
        if(coords != null && coords.size() > 0) {
            drawDrive();
        }
    }

    public void drawDrive() {
        invalidate();    //清除画布
        wCanvas.drawColor(CANVAS_BACKGROUND_COLOR);  //设置背景颜色
        drawCustomMap();
        drawCar();
    }

    private void drawCustomMap() {
        for(Coordinate coord : coords) {
            double coordx = transCoordX(coord.getCoordX());
            double coordy = transCoordY(coord.getCoordY());
            wCanvas.drawCircle((float) coordx, (float) coordy, MAP_RADIUS, mapPaint);
            if (coord.getCoordType() == null || coord.getCoordType().toLowerCase().equals("null")) {
                continue;
            }
            double padding = 2*MAP_RADIUS+TEXT_SIZE;
            double wordx = (coordx+padding > wCanvas.getWidth()) ? (coordx-padding) : (coordx+padding);
            double wordy = (coordy+padding > wCanvas.getHeight()) ? (coordy-padding) : (coordy+padding);
            wCanvas.drawText(coord.getCoordType().toUpperCase(), (float)wordx, (float)wordy, wordPaint);
        }
    }

    private void drawCar() {
        if(carGPS != null) {
            Log.i("car", "gps: " + (float)transCoordX(carGPS.getCoordX()) + "   " + (float)transCoordY(carGPS.getCoordY()));
            wCanvas.drawCircle((float)transCoordX(carGPS.getCoordX()), (float)transCoordY(carGPS.getCoordY()), CAR_RADIUS, carPaint);
        }
    }

    private void loadMapData() {
        // init
        coords.clear();
        options.clear();
        maxX = Double.MIN_VALUE;
        minX = Double.MAX_VALUE;
        maxY = Double.MIN_VALUE;
        minY = Double.MAX_VALUE;

        String sdPath = "";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            sdPath = Environment.getExternalStorageDirectory().toString();
        }
        File mapFile = new File(sdPath + UiConfig.DATA_PATH + UiConfig.MAP_DATA);
        if (!mapFile.exists()) {
            Toast.makeText(getContext(), NO_MAP_DATA, Toast.LENGTH_LONG).show();
        }else {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(mapFile));
                String line = "";
                while((line = reader.readLine()) != null) {
                    String[] nodes = line.split(",");
                    double coordx = Double.parseDouble(nodes[0].trim());
                    double coordy = Double.parseDouble(nodes[1].trim());
                    maxX = max(coordx, maxX);
                    minX = min(coordx, minX);
                    maxY = max(coordy, maxY);
                    minY = min(coordy, minY);
                    coords.add(new Coordinate(coordx, coordy, nodes[2]));
                    if (!nodes[2].trim().toLowerCase().equals("null"))
                        options.add(nodes[2].trim().toUpperCase());
                }
            } catch (FileNotFoundException e) {
                Toast.makeText(getContext(), NO_MAP_DATA, Toast.LENGTH_LONG).show();
                coords.clear();
            } catch (IOException e) {
                coords.clear();
            } catch (Exception e) {
                Toast.makeText(getContext(), MAP_DATA_FORMAT_ERR, Toast.LENGTH_LONG).show();
                coords.clear();
            }
        }
    }

    private void setPaint() {
        setMapPaint();
        setCarPaint();
        setWordPaint();
    }

    private void setMapPaint() {
        mapPaint = new Paint();
        mapPaint.setStyle(Paint.Style.FILL);
        mapPaint.setAntiAlias(true);
        mapPaint.setColor(Color.RED);
        mapPaint.setStrokeWidth(2);
    }

    private void setCarPaint() {
        carPaint = new Paint();
        carPaint.setStyle(Paint.Style.FILL);
        carPaint.setAntiAlias(true);
        carPaint.setColor(Color.BLUE);
        carPaint.setStrokeWidth(2);
    }

    private void setWordPaint() {
        wordPaint = new Paint();
        wordPaint.setStrokeWidth(2);//设置画笔宽度
        wordPaint.setAntiAlias(true); //指定是否使用抗锯齿功能，如果使用，会使绘图速度变慢
        wordPaint.setStyle(Paint.Style.FILL);//绘图样式，对于设文字和几何图形都有效
        wordPaint.setTextAlign(Paint.Align.CENTER);//设置文字对齐方式，取值：align.CENTER、align.LEFT或align.RIGHT
        wordPaint.setTextSize(TEXT_SIZE);//设置文字大小

        //样式设置
        wordPaint.setFakeBoldText(true);//设置是否为粗体文字
        wordPaint.setTextSkewX((float) -0.25);//设置字体水平倾斜度，普通斜体字是-0.25
    }

    private double transCoordX(double coordx) {
        int width = wCanvas.getWidth() - 2 * MAP_PADDING;
        if (width <= 2 * MAP_PADDING) {
            return wCanvas.getWidth() * (coordx - minX) / (maxX - minX);
        }
        return MAP_PADDING + width * (coordx - minX) / (maxX - minX);
    }

    private double transCoordY(double coordy) {
        int height = wCanvas.getHeight() - 2 * MAP_PADDING;
        if (height <= 2 * MAP_PADDING) {
            return wCanvas.getHeight() * (1 - (coordy - minY) / (maxY - minY));
        }
        return MAP_PADDING + height * (1 - (coordy - minY) / (maxY - minY));
    }

    private double max(double d1, double d2) {
        return (((d1) > (d2)) ? (d1) : (d2));
    }

    private double min(double d1, double d2) {
        return (((d1) < (d2)) ? (d1) : (d2));
    }

    private void setWheelOption(){
        this.startWheel.setItems(options);
        this.endWheel.setItems(options);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.id_custom_map_setline_button:
                LineBean line = new LineBean();
                line.setStart(startWheel.getSeletedItem());
                line.setEnd(endWheel.getSeletedItem());
                GlobalmapBean globalmap = new GlobalmapBean();
                globalmap.setLine(line);
                AtosBean atos = new AtosBean();
                atos.setGlobalmap(globalmap);
                MessageBean message = new MessageBean();
                message.setAtos(atos);
                Log.i(canvasDrawLog, GsonUtil.toJson(message));
                try {
                    mZmqService.publishMsg(GsonUtil.toJson(message));
                }catch (Exception e) {
                    Toast.makeText(getContext(), SEND_MSG_ERR, Toast.LENGTH_LONG).show();
                }
                break;
        }
    }
}

class Coordinate {
    private double coordX;
    private double coordY;
    private String coordType;

    public Coordinate(double coordX, double coordY, String coordType) {
        this.coordX = coordX;
        this.coordY = coordY;
        this.coordType = coordType.trim();
    }

    public double getCoordX() {
        return this.coordX;
    }

    public double getCoordY() {
        return this.coordY;
    }

    public String getCoordType() {
        return this.coordType;
    }
}
