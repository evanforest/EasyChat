package com.linzg.www.androidinternet;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;


/**
 * 正方形ImageView，边长为宽
 * Created by asus on 2017/9/15.
 */

public class SquareImageView extends android.support.v7.widget.AppCompatImageView {
    private static final String TAG = "SquareImageView";
    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }
}
