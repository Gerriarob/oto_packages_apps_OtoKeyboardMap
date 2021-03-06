package org.openthos.keyboardmap;

import android.app.ActivityManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by asd on 1/1/2017.
 */

public class ViewManager {
    BaseView mBaseView;
    ControlView mControlView;
    WindowManager windowManager;
    public static ViewManager manager;
    Context context;
    private WindowManager.LayoutParams mBaseViewParams;
    private WindowManager.LayoutParams mControlViewParams;
    public static List<TextView> mDragViewList = new ArrayList<>();
    public static Integer[] mDirectionKeyArr = new Integer[]{-1, -1, -1, -1, -1, -1, -1};

    private ViewManager(Context context) {
        this.context = context;
    }

    public static ViewManager getInstance(Context context) {
        if (manager == null) {
            manager = new ViewManager(context);
        }
        return manager;
    }

    public void showBase() {
        if (mBaseView != null) {
            hideView();
            return;
        }
        hideControl();
        mBaseView = new BaseView(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mBaseViewParams == null) {
            mBaseViewParams = new WindowManager.LayoutParams();
            mBaseViewParams.width = KeymapService.screenWidth;
            mBaseViewParams.height = KeymapService.screenHeight;
            mBaseViewParams.gravity = Gravity.TOP | Gravity.LEFT;
            mBaseViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            mBaseViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
            mBaseViewParams.format = PixelFormat.RGBA_8888;
        }
        windowManager.addView(mBaseView, mBaseViewParams);
    }

    void hideView() {
        if (mBaseView != null) {
            windowManager.removeView(mBaseView);
            mBaseView = null;
        }
    }

    void hideControl() {
        if (mControlView != null) {
            windowManager.removeView(mControlView);
            mControlView = null;
        }
    }

    public void exit() {
        hideView();
        hideControl();
    }

    public void showControl() {
        if (mControlView != null) {
            hideControl();
            return;
        }
        hideView();
        mControlView  = new ControlView(context);
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (mControlViewParams == null) {
            mControlViewParams = new WindowManager.LayoutParams();
            mControlViewParams.width = KeymapService.screenWidth;
            mControlViewParams.height = KeymapService.screenHeight;
            mControlViewParams.gravity = Gravity.TOP | Gravity.LEFT;
            mControlViewParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
            mControlViewParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
            mControlViewParams.format = PixelFormat.RGBA_8888;
        }
        windowManager.addView(mControlView, mControlViewParams);
        loadMappingConfiguration();
    }

        // loading mapping configuration
    public void loadMappingConfiguration() {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(Integer.MAX_VALUE);
        String packageName = am.getRunningTasks(Integer.MAX_VALUE).get(0)
                .topActivity.getPackageName();
        MappingSQLiteOpenHelper mOpenHelper = new MappingSQLiteOpenHelper(context);
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Cursor directionCursor = db.rawQuery("select * from " + mOpenHelper.mDirectionKeyTableName
                + " where packageName = ?", new String[]{packageName});
        KeyEvent event = new KeyEvent(0, 0);
        String key = "";

        if (directionCursor != null && directionCursor.getCount() != 0) {
            while (directionCursor.moveToNext()) {
                int leftKeyCode = directionCursor.getInt(
                        directionCursor.getColumnIndex("leftKeyCode"));
                int topKeyCode = directionCursor.getInt(
                        directionCursor.getColumnIndex("topKeyCode"));
                int rightKeyCode = directionCursor.getInt(
                        directionCursor.getColumnIndex("rightKeyCode"));
                int bottomKeyCode = directionCursor.getInt(
                        directionCursor.getColumnIndex("bottomKeyCode"));
                int circleCenterX = directionCursor.getInt(
                        directionCursor.getColumnIndex("circleCenterX"));
                int circleCenterY = directionCursor.getInt(
                        directionCursor.getColumnIndex("circleCenterY"));
                int distance = directionCursor.getInt(
                        directionCursor.getColumnIndex("distance"));
                float scale = directionCursor.getFloat(directionCursor.getColumnIndex("scale"));
                scale = (scale == 0) ? 1.0f : scale;

                if (leftKeyCode != -1) {
                    mControlView.createVirtualWhell(circleCenterX, circleCenterY, true,
                            convertKeyCodeToKey(event, leftKeyCode),
                            convertKeyCodeToKey(event, topKeyCode),
                            convertKeyCodeToKey(event, rightKeyCode),
                            convertKeyCodeToKey(event, bottomKeyCode), scale, distance);
                    mDirectionKeyArr[0] = leftKeyCode;
                    mDirectionKeyArr[1] = topKeyCode;
                    mDirectionKeyArr[2] = rightKeyCode;
                    mDirectionKeyArr[3] = bottomKeyCode;
                    mDirectionKeyArr[4] = circleCenterX;
                    mDirectionKeyArr[5] = circleCenterY;
                    mDirectionKeyArr[6] = distance;
                }
            }
        }
        directionCursor.close();

        Cursor functionCursor = db.rawQuery("select * from " + mOpenHelper.mFunctionKeyTableName +
                " where packageName = ?", new String[] {packageName});
        ViewManager.mDragViewList.clear();
        TextView textView = null;
        List<Integer> textViewTag = null;
        if (functionCursor != null && functionCursor.getCount() != 0) {
            while (functionCursor.moveToNext()) {
                int keyCode = functionCursor.getInt(functionCursor.getColumnIndex("keyCode"));
                int valueX = functionCursor.getInt(functionCursor.getColumnIndex("valueX"));
                int valueY = functionCursor.getInt(functionCursor.getColumnIndex("valueY"));
                textView = mControlView.createDragTextView(convertKeyCodeToKey(event, keyCode),
                                                           valueX, valueY);
                textViewTag = (List<Integer>) textView.getTag();
                textViewTag.set(0, keyCode);
                textViewTag.set(1, valueX);
                textViewTag.set(2, valueY);
                textView.setTag(textViewTag);
                mDragViewList.add(textView);
            }
        }
        functionCursor.close();
        db.close();
    }

    public String convertKeyCodeToKey(KeyEvent event, int keyCode) {
        String key = KeymapService.mKeyMap.get(keyCode);
        if (key == null) {
            boolean isPrintingKey = event.getKeyCharacterMap().isPrintingKey(keyCode);
            if (isPrintingKey) {
                key = event.getKeyCharacterMap().getDisplayLabel(keyCode) + "";
            } else {
                key = keyCode + "";
            }
        }
        return key;
    }
}
