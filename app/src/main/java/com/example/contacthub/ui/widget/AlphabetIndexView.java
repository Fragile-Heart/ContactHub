package com.example.contacthub.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class AlphabetIndexView extends View {
    // 字母索引数组
    private String[] letters = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                               "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"};
    // 画笔对象
    private Paint paint = new Paint();
    // 当前选中的字母索引
    private int selectedIndex = -1;
    // 字母选中监听器
    private OnLetterSelectedListener listener;

    // 字母选中监听器接口
    public interface OnLetterSelectedListener {
        void onLetterSelected(String letter);
    }

    // 构造函数1
    public AlphabetIndexView(Context context) {
        super(context);
        init();
    }

    // 构造函数2
    public AlphabetIndexView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    // 构造函数3
    public AlphabetIndexView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // 初始化画笔
    private void init() {
        paint.setAntiAlias(true);
        paint.setTextSize(36);
        paint.setTextAlign(Paint.Align.CENTER);
    }

    // 绘制字母索引
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float height = getHeight();
        float width = getWidth();
        float singleHeight = height / letters.length;

        for (int i = 0; i < letters.length; i++) {
            // 设置选中和未选中状态的颜色和字体
            paint.setColor(i == selectedIndex ? Color.parseColor("#3F51B5") : Color.GRAY);
            paint.setTypeface(i == selectedIndex ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);

            float xPos = width / 2;
            float yPos = singleHeight * i + singleHeight / 2 + paint.getTextSize() / 2;
            canvas.drawText(letters[i], xPos, yPos, paint);
        }
    }

    // 处理触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float y = event.getY();
                int index = (int) (y / getHeight() * letters.length);
                if (index >= 0 && index < letters.length) {
                    if (index != selectedIndex) {
                        selectedIndex = index;
                        if (listener != null) {
                            listener.onLetterSelected(letters[index]);
                        }
                        invalidate();
                    }
                }
                return true;
            case MotionEvent.ACTION_UP:
                selectedIndex = -1;
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    // 设置字母选中监听器
    public void setOnLetterSelectedListener(OnLetterSelectedListener listener) {
        this.listener = listener;
    }
}