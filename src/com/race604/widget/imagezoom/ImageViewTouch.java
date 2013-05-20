
package com.race604.widget.imagezoom;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

public class ImageViewTouch extends ImageViewTouchBase {

    static final float MIN_ZOOM = 0.9f;

    protected GestureDetector mGestureDetector;

    protected int mTouchSlop;

    protected float mCurrentScaleFactor;
    protected float mLastScaleFactor;

    protected float mScaleFactor;

    protected int mDoubleTapDirection;

    protected GestureListener mGestureListener;

    private boolean mSDKLever5;

    private float mOldDist;

    private int mTouchMode;

    private static final int NONE = 0;

    private static final int DRAG = 1;

    private static final int ZOOM = 2;

    private PointF mMidPoint;
    
    private OnClickListener mOnClickListener;

    public ImageViewTouch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        super.init();
        mTouchSlop = ViewConfiguration.getTouchSlop();
        mGestureListener = new GestureListener();

        mGestureDetector = new GestureDetector(getContext(), mGestureListener, null);
        mLastScaleFactor = mCurrentScaleFactor = 1f;
        mDoubleTapDirection = 1;

        mSDKLever5 = android.os.Build.VERSION.SDK_INT >= 5;
        if (mSDKLever5) {
            mMidPoint = new PointF();
        }
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        mOnClickListener = l;
    }

    @Override
    public void setImageRotateBitmapReset(RotateBitmap bitmap, boolean reset) {
        super.setImageRotateBitmapReset(bitmap, reset);
        mScaleFactor = getMaxZoom() / 3;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // mScaleDetector.onTouchEvent( event );
        mGestureDetector.onTouchEvent(event);

        int action = event.getAction();

        if (mSDKLever5) {
            action &= MotionEvent.ACTION_MASK;
        }

        if (action == MotionEvent.ACTION_UP) {
            if (getScale() < 1f) {
                zoomTo(1f, 50);
            }
            mTouchMode = NONE;
        } else if (mSDKLever5) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    mOldDist = spacing(event);
                    if (mOldDist > 10f) {
                        midPoint(mMidPoint, event);
                        mLastScaleFactor = mCurrentScaleFactor;
                        mTouchMode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchMode == DRAG) {
                        // ...
                    } else if (mTouchMode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float scale = newDist / mOldDist;
                            onScale(scale, mMidPoint);
                        }
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mTouchMode = NONE;
                    break;
                case MotionEvent.ACTION_DOWN:
                    mTouchMode = DRAG;
                    break;
            }
        }

        return true;
    }

    private float spacing(MotionEvent event) {
        float x = Reflect.getX(event, 0) - Reflect.getX(event, 1);
        float y = Reflect.getY(event, 0) - Reflect.getY(event, 1);
        return FloatMath.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = Reflect.getX(event, 0) + Reflect.getX(event, 1);
        float y = Reflect.getY(event, 0) + Reflect.getY(event, 1);
        point.set(x / 2, y / 2);
    }

    public boolean onScale(float scale, PointF mid) {
        float targetScale = mLastScaleFactor * scale;
        targetScale = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
        zoomTo(targetScale, mid.x, mid.y);
        mCurrentScaleFactor = Math.min(getMaxZoom(), Math.max(targetScale, MIN_ZOOM));
        mDoubleTapDirection = 1;
        invalidate();
        return true;
    }

    @Override
    protected void onZoom(float scale) {
        super.onZoom(scale);
        // if ( !mScaleDetector.isInProgress() ) mCurrentScaleFactor = scale;
        mCurrentScaleFactor = scale;
    }

    protected float onDoubleTapPost(float scale, float maxZoom) {
        if (mDoubleTapDirection == 1) {
            if ((scale + (mScaleFactor * 2)) <= maxZoom) {
                return scale + mScaleFactor;
            } else {
                mDoubleTapDirection = -1;
                return maxZoom;
            }
        } else {
            mDoubleTapDirection = 1;
            return 1f;
        }
    }

    class GestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDoubleTap(MotionEvent e) {
			if (mBitmapDisplayed.getBitmap() != null) {
				float scale = getScale();
				float targetScale = scale;
				targetScale = onDoubleTapPost(scale, getMaxZoom());
				targetScale = Math.min(getMaxZoom(),
						Math.max(targetScale, MIN_ZOOM));
				mCurrentScaleFactor = targetScale;
				zoomTo(targetScale, e.getX(), e.getY(), 200);
				invalidate();
			}
			return super.onDoubleTap(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e1 == null || e2 == null)
                return false;
            if (mSDKLever5) {
                if (Reflect.getPointerCount(e1) > 1 || Reflect.getPointerCount(e2) > 1)
                    return false;
            }
            // if ( mScaleDetector.isInProgress() ) return false;
            if (mBitmapDisplayed.getBitmap() == null || getScale() == 1f)
                return false;
            scrollBy(-distanceX, -distanceY);
            invalidate();
            return super.onScroll(e1, e2, distanceX, distanceY);
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mSDKLever5) {
                if (Reflect.getPointerCount(e1) > 1 || Reflect.getPointerCount(e2) > 1)
                    return false;
            }
            // if ( mScaleDetector.isInProgress() ) return false;
            if ( mBitmapDisplayed.getBitmap() == null ) return false;

            float diffX = e2.getX() - e1.getX();
            float diffY = e2.getY() - e1.getY();

            if (Math.abs(velocityX) > 800 || Math.abs(velocityY) > 800) {
                scrollBy(diffX / 2, diffY / 2, 300);
                invalidate();
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
			if (mOnClickListener != null) {
				mOnClickListener.onClick(ImageViewTouch.this);
			}
            return super.onSingleTapConfirmed(e);
        }
        
        
    }
    
   

    // class ScaleListener extends
    // ScaleGestureDetector.SimpleOnScaleGestureListener {
    //
    // @SuppressWarnings( "unused" )
    // @Override
    // public boolean onScale( ScaleGestureDetector detector )
    // {
    // float span = detector.getCurrentSpan() - detector.getPreviousSpan();
    // float targetScale = mCurrentScaleFactor * detector.getScaleFactor();
    // if ( true ) {
    // targetScale = Math.min( getMaxZoom(), Math.max( targetScale, MIN_ZOOM )
    // );
    // zoomTo( targetScale, detector.getFocusX(), detector.getFocusY() );
    // mCurrentScaleFactor = Math.min( getMaxZoom(), Math.max( targetScale,
    // MIN_ZOOM ) );
    // mDoubleTapDirection = 1;
    // invalidate();
    // return true;
    // }
    // return false;
    // }
    // }
}
