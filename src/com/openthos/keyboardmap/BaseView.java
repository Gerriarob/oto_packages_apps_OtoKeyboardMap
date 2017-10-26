package com.openthos.keyboardmap;

import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseView extends View {
    private Paint paint;
    private final List<Integer> mFunctionKeys = new ArrayList<>();
    private int mPressedDirectionKeyCount;
    private Instrumentation mInstrumentation = new Instrumentation();
    private int mCircleCenterX, mCircleCenterY, mCurrentDirectionX, mCurrentDirectionY, mDistanceFromCircleToKey;
    public List<Integer> mDirectionKeys = new ArrayList<>();

    public BaseView(Context context) {
        super(context);
        paint = new Paint();
        mFunctionKeys.clear();
        mDirectionKeys.clear();
        for (ControlView2.DragView dragView : ViewManager.mDragViewList) {
            mFunctionKeys.add(dragView.keyCode);
        }
        mDirectionKeys = Arrays.asList(ViewManager.mDirectionKeyArr);
    }

    public void processKeyMapping(final Instrumentation in, final int eventType, final float x,
                                  final float y, final boolean needMove, final float downX,
                              final float downY, final float moveX, final float moveY) {
            new Thread() {
                @Override
                public void run() {
                    super.run();
                    long time = SystemClock.uptimeMillis();
                    if (needMove) {
                        in.sendPointerSync(MotionEvent.obtain(time, time, eventType, x, y, 0));
                        in.sendPointerSync(MotionEvent.obtain(time, time, MotionEvent.ACTION_DOWN, downX, downY, 0));
                        in.sendPointerSync(MotionEvent.obtain(time, time, MotionEvent.ACTION_MOVE, moveX, moveY, 0));
                    } else {
                        in.sendPointerSync(MotionEvent.obtain(time, time, eventType, x, y, 0));
                    }
                }
            }.start();
    }

    @Override
    public boolean dispatchKeyEvent(final KeyEvent event) {
        final int keyCode = event.getKeyCode();
        switch (keyCode) {
            case KeyEvent.KEYCODE_F1:
                MainActivity.mHandler.sendEmptyMessage(0);
                return true;
        }

        if (event.getAction() == KeyEvent.ACTION_DOWN
                && event.getRepeatCount() == 0) {
            if (mDirectionKeys.contains(keyCode)) {
                mPressedDirectionKeyCount++;
                if (mPressedDirectionKeyCount == 1) {
                    // todo down circle center
                    mCircleCenterX = mDirectionKeys.get(4);
                    mCircleCenterY = mDirectionKeys.get(5);
                    mDistanceFromCircleToKey = mDirectionKeys.get(6);
                    processKeyMapping(mInstrumentation, MotionEvent.ACTION_DOWN,
                            mCircleCenterX, mCircleCenterY, false, 0, 0 , 0, 0);
                    mCurrentDirectionX = mCircleCenterX;
                    mCurrentDirectionY = mCircleCenterY;
                }
                // todo move direction
                if (event.getKeyCode() == mDirectionKeys.get(0)) {
                    // direction left
                    mCurrentDirectionX = mCurrentDirectionX - mDistanceFromCircleToKey;
                } else  if (event.getKeyCode() == mDirectionKeys.get(1)) {
                    // direction up
                    mCurrentDirectionY = mCurrentDirectionY - mDistanceFromCircleToKey;

                } else  if (event.getKeyCode() == mDirectionKeys.get(2)) {
                    // direction right
                    mCurrentDirectionX = mCurrentDirectionX + mDistanceFromCircleToKey;

                } else  if (event.getKeyCode() == mDirectionKeys.get(3)) {
                    // direction down
                    mCurrentDirectionY = mCurrentDirectionY + mDistanceFromCircleToKey;
                }
                processKeyMapping(mInstrumentation, MotionEvent.ACTION_MOVE,
                        mCurrentDirectionX, mCurrentDirectionY, false, 0, 0 , 0, 0);
            } else if (mFunctionKeys.contains(event.getKeyCode())) {
                // todo down functionkey
                    int index = mFunctionKeys.indexOf(keyCode);
                //if (mPressedDirectionKeyCount == 0) {
                    processKeyMapping(mInstrumentation, MotionEvent.ACTION_DOWN,
                            ViewManager.mDragViewList.get(index).
                                    mWUpX + 45, ViewManager.mDragViewList.get(index).mWUpY + 45,
                                    false, 0, 0 , 0, 0);
                //} else {
                //    processKeyMapping(mInstrumentation, MotionEvent.ACTION_DOWN,
                //            ViewManager.mDragViewList.get(index). mWUpX + 45,
                //            ViewManager.mDragViewList.get(index).mWUpY + 45,
                //            true, mCircleCenterX, mCircleCenterY,
                //            mCurrentDirectionX, mCurrentDirectionY);
                //}

            }

        } else if (event.getAction() == KeyEvent.ACTION_UP) {
            if (mDirectionKeys.contains(event.getKeyCode())) {
                mPressedDirectionKeyCount--;
                if (mPressedDirectionKeyCount == 0) {
                    // todo up circle center
                    processKeyMapping(mInstrumentation, MotionEvent.ACTION_UP,
                            mCurrentDirectionX, mCurrentDirectionY, false, 0, 0 , 0, 0);
                    mCurrentDirectionX = 0;
                    mCurrentDirectionY = 0;
                    return true;
                }
                // todo move direction
                if (event.getKeyCode() == mDirectionKeys.get(0)) {
                    // direction left
                    mCurrentDirectionX = mCurrentDirectionX + mDistanceFromCircleToKey;
                } else  if (event.getKeyCode() == mDirectionKeys.get(1)) {
                    // direction up
                    mCurrentDirectionY = mCurrentDirectionY + mDistanceFromCircleToKey;

                } else  if (event.getKeyCode() == mDirectionKeys.get(2)) {
                    // direction right
                    mCurrentDirectionX = mCurrentDirectionX - mDistanceFromCircleToKey;

                } else  if (event.getKeyCode() == mDirectionKeys.get(3)) {
                    // direction down
                    mCurrentDirectionY = mCurrentDirectionY - mDistanceFromCircleToKey;
                }
                processKeyMapping(mInstrumentation, MotionEvent.ACTION_MOVE,
                        mCurrentDirectionX, mCurrentDirectionY, false, 0, 0 , 0, 0);
            } else if (mFunctionKeys.contains(event.getKeyCode())) {
                // todo up functionkey
                int index = mFunctionKeys.indexOf(keyCode);
                processKeyMapping(mInstrumentation, MotionEvent.ACTION_UP,
                        ViewManager.mDragViewList.get(index).
                                mWUpX + 45, ViewManager.mDragViewList.get(index).mWUpY + 45, false, 0, 0 , 0, 0);
            }
        }

        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        Log.i("wwww", event.getAction() + " motion");
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(MainActivity.screenWidth, MainActivity.screenHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0x00ffffff);
        canvas.drawRect(0, 0, MainActivity.screenWidth, MainActivity.screenHeight, paint);
    }
}