package com.soumayaguenaguen.gas_subscriber_app;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View {
    private Paint circlePaint;
    private Paint needlePaint;
    private Paint textPaint;
    private float value; // Current value to be displayed on the gauge

    public GaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(20);
        circlePaint.setAntiAlias(true);
        circlePaint.setColor(Color.GRAY); // Set dark blue color

        needlePaint = new Paint();
        needlePaint.setStyle(Paint.Style.STROKE);
        needlePaint.setStrokeWidth(10);
        needlePaint.setAntiAlias(true);
        needlePaint.setColor(Color.RED); // Set red color

        textPaint = new Paint();
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(40);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    public void setValue(float value) {
        this.value = value;
        invalidate(); // Redraw the view
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = Math.min(centerX, centerY) - 30;

        // Draw the outer circle
        canvas.drawCircle(centerX, centerY, radius, circlePaint);

        // Draw the scale lines
        drawScaleLines(canvas, centerX, centerY, radius);

        // Draw the needle or indicator
        float angle = calculateAngle(value);
        float needleLength = radius - 40;
        float needleX = (float) (centerX + needleLength * Math.cos(angle));
        float needleY = (float) (centerY + needleLength * Math.sin(angle));
        canvas.drawLine(centerX, centerY, needleX, needleY, needlePaint);

        // Draw the text value
        canvas.drawText(String.valueOf((int) value), centerX, centerY + 20, textPaint);
    }

    private float calculateAngle(float value) {
        // Adjust this calculation based on your specific gauge scale
        // This is a simple example assuming a scale from 0 to 100
        float scale = 180f / 100f; // Total angle divided by the scale
        return (value * scale) - 90f; // Offset by -90 degrees to start from the top
    }

    private void drawScaleLines(Canvas canvas, float centerX, float centerY, float radius) {
        Paint scaleLinePaint = new Paint();
        scaleLinePaint.setStyle(Paint.Style.STROKE);
        scaleLinePaint.setStrokeWidth(5);
        scaleLinePaint.setAntiAlias(true);
        scaleLinePaint.setColor(Color.BLACK);

        float totalLines = 12;
        float angleIncrement = 180 / totalLines;

        for (int i = 0; i <= totalLines; i++) {
            float angle = -90 + i * angleIncrement;
            float startX = (float) (centerX + (radius - 10) * Math.cos(Math.toRadians(angle)));
            float startY = (float) (centerY + (radius - 10) * Math.sin(Math.toRadians(angle)));
            float endX = (float) (centerX + radius * Math.cos(Math.toRadians(angle)));
            float endY = (float) (centerY + radius * Math.sin(Math.toRadians(angle)));
            canvas.drawLine(startX, startY, endX, endY, scaleLinePaint);
        }
    }
}

