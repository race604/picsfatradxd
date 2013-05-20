package com.race604.widget.imagezoom;

import android.view.MotionEvent;

public class Reflect {
    /*private static Method mGetXMethod;
    private static Method mGetYMethod;
    private static Method mGetPointerCountMethod;*/
    
    static {
        initCompatibility();
    };

    private static void initCompatibility() {
        /*try {
            mGetXMethod = MotionEvent.class.getMethod("getX", new Class[] {int.class} );
            mGetYMethod = MotionEvent.class.getMethod("getY", new Class[] {int.class} );
            mGetPointerCountMethod = MotionEvent.class.getMethod("getPointerCount");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }*/
    }
    
    
    public static float getX(MotionEvent event, int index) {
        /*if (mGetXMethod != null) {
            try {
                return (Float)mGetXMethod.invoke(event, index);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return Float.MIN_VALUE;
        }
        return Float.MIN_VALUE;
        */
        return (float) event.getX(index);
        
    }
    
    public static float getY(MotionEvent event, int index) {
        /*if (mGetYMethod != null) {
            try {
                return (Float)mGetYMethod.invoke(event, index);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return Float.MIN_VALUE;
        }
        
        return Float.MIN_VALUE;*/
        return (float) event.getY(index);
        
    }
    
    public static int getPointerCount(MotionEvent event) {
        /*if (mGetPointerCountMethod != null) {
            try {                
                return (Integer)mGetPointerCountMethod.invoke(event);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return -1;
        }
        return -1;*/
        return (int)event.getPointerCount();
    }
    
}
