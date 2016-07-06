package com.hadisi.usbhosttool.myview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.EditText;

/**
 * Created by wugang on 2015/11/12.
 */
public class CustomEdiText extends EditText {
    public CustomEdiText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    public CustomEdiText(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
    }

    public CustomEdiText(Context context) {
        super(context);
        // TODO Auto-generated constructor stub
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        // TODO Auto-generated method stub

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 通知父控件不要干扰
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_MOVE:
                // 通知父控件不要干扰
                getParent().requestDisallowInterceptTouchEvent(true);
                break;
            case MotionEvent.ACTION_UP:
                break;

            default:
                break;
        }
        return super.onTouchEvent(e);
    }
}
