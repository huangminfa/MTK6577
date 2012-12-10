package com.mediatek.media3d;

/**
 *  Help to handle and switch drag status: initial, start, dragging and finish.
 *  initial: prepare for dragging
 *  start: start dragging
 *  dragging: in the dragging
 *  finish: finish dragging
 */
public class PageDragHelper {
    private int mDragX;
    private int mDragY;
    private Direction mDrag2Fling = Direction.NONE;
    private static final int ON_FLING_THRESHOLD = 20;
    private static final int ON_DRAG_THRESHOLD = 2;

    public enum State {
        INITIAL, START, DRAGGING, FINISH
    }
    private State mState = State.INITIAL;

    public enum Direction {
        NONE, LEFT, RIGHT, DOWN, UP
    }

    public PageDragHelper() {
        // Do nothing now
    }

    public Direction handleActionUp(Page page, Media3DView m3d) {
        Direction dir = mDrag2Fling;
        updateState();
        if (mState == State.START || mState == State.DRAGGING) {
            setFinish();
            page.onDrag(State.FINISH, 0);
            m3d.onDrag(State.FINISH, 0);
        }
        mDrag2Fling = Direction.NONE;
        mDragX = 0;
        mDragY = 0;

        return dir;
    }

    public boolean onScroll(float disX, float disY, Page page, Media3DView m3d) {
        mDragX += disX;
        mDragY += disY;
        if (Math.abs(mDragY) > Math.abs(mDragX) && Math.abs(mDragY) > ON_DRAG_THRESHOLD) {
            updateState();
            page.onDrag(mState, mDragY);
            if (mDragY > 0) {
                mDrag2Fling = Direction.UP;
            } else {
                mDrag2Fling = Direction.DOWN;
            }
        } else if (Math.abs(mDragX) > ON_FLING_THRESHOLD) {
            updateState();
            m3d.onDrag(mState, mDragX);
            if (mDragX > 0) {
                mDrag2Fling = Direction.LEFT;
            } else if (mDragX < 0) {
                mDrag2Fling = Direction.RIGHT;
            }
        }

        return true;
    }

    private void updateState() {
        if (mState == State.FINISH) {
            mState = State.INITIAL;
        } else if (mState == State.INITIAL) {
            mState = State.START;
        } else {
            mState = State.DRAGGING;
        }
    }

    public void setFinish() {
        mState = State.FINISH;
    }
}
