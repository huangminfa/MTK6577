package com.mediatek.media3d;

import android.app.Instrumentation;
import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.media3d.photo.ImageDbItem;
import com.mediatek.media3d.photo.PhotoPage;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w -e class com.mediatek.media3d.PhotoPageTest com.mediatek.media3d.tests/android.test.InstrumentationTestRunner
 */
public class PhotoPageTest extends ActivityInstrumentationTestCase2<Main> {

    private Instrumentation mInstrumentation;
    private Main mActivity;
    private Media3DView mMedia3DView;

    public PhotoPageTest() {
        super("com.mediatek.media3d", Main.class);
    }

    public void validateNoNullMember() {
        if (null == mActivity || null == mMedia3DView || null == mInstrumentation) {
            throw new NullPointerException(
                    "There is at least one null-pointer data member.");
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        mInstrumentation = getInstrumentation();
        mActivity = getActivity();
        if (null != mActivity) {
            mMedia3DView = mActivity.getMedia3DView();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        mInstrumentation = null;
        mMedia3DView = null;
        mActivity = null;
        super.tearDown();
    }

    public void enterPhotoPageWaitForIdle() {
        validateNoNullMember();
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPortalPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        CommonTestUtil.waitLoadForIdleSync(mInstrumentation, mActivity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);

        CommonTestUtil.sendSingleTapConfirmedEventOnUiThread(mActivity, mMedia3DView, CommonTestUtil.PHOTO_ICON_IN_PORTALPAGE);
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);

        assertTrue(mMedia3DView.getCurrentPage() == mActivity.getPhotoPage());
    }
    // Testing Case #1
    public void testFastFlingUp() {
        enterPhotoPageWaitForIdle();

        final int FLING_COUNT = 10;
        for (int i = 0; i < FLING_COUNT; ++i) {
            CommonTestUtil.sendDragSync(mInstrumentation, mActivity, CommonTestUtil.DragDirection.UP);
            CommonTestUtil.sendDragSync(mInstrumentation, mActivity, CommonTestUtil.DragDirection.DOWN);
        }
        CommonTestUtil.wait(mInstrumentation, CommonTestUtil.WAIT_FOR_INNER_PAGE_SWITCH_TIME_IN_SECS);
        assertTrue(mMedia3DView.getCurrentPage() == mActivity.getPhotoPage());
    }

    // Testing Case #2
    public void testFolderOnMenuBar() {
        enterPhotoPageWaitForIdle();

        CommonTestUtil.sendSingleTapConfirmedEventOnUiThread(mActivity, mMedia3DView, CommonTestUtil.NON_ACTOR_POINT);
        CommonTestUtil.waitMenuBarForActionSync(mInstrumentation, mMedia3DView, CommonTestUtil.DEFAULT_MENU_BAR_TIMEOUT_IN_MS);

        // Click folder icon
        CommonTestUtil.sendTouchEventsOnUiThread(mActivity, mMedia3DView, CommonTestUtil.FOLDER_ICON_IN_TOP_MENU);
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);

        final int REQUEST_GET_PHOTO = 2; // refer to PhotoPage
        CommonTestUtil.finishLaunchedActivity(mActivity, REQUEST_GET_PHOTO);
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        assertTrue(mMedia3DView.getCurrentPage() == mActivity.getPhotoPage());
    }

    // Testing Case #3
    public void testPauseResume() {
        enterPhotoPageWaitForIdle();

        mInstrumentation.callActivityOnPause(mActivity);
        CommonTestUtil.wait(mInstrumentation, CommonTestUtil.WAIT_FOR_PAGE_SWITCH_TIME_IN_SECS);

        mInstrumentation.callActivityOnResume(mActivity);
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPhotoPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        assertTrue(mMedia3DView.getCurrentPage() == mActivity.getPhotoPage());
    }

    // Testing Case #4
    public void testBackOnMenuBar() {
        enterPhotoPageWaitForIdle();

        // trigger menu bar
        CommonTestUtil.sendSingleTapConfirmedEventOnUiThread(mActivity, mMedia3DView, CommonTestUtil.NON_ACTOR_POINT);
        CommonTestUtil.waitMenuBarForActionSync(mInstrumentation, mMedia3DView, CommonTestUtil.DEFAULT_MENU_BAR_TIMEOUT_IN_MS);

        // click back icon on menu bar
        CommonTestUtil.sendTouchEventsOnUiThread(mActivity, mMedia3DView, CommonTestUtil.BACK_ICON_IN_TOP_MENU);
        CommonTestUtil.waitPageForIdleSync(mInstrumentation, mMedia3DView, mActivity.getPortalPage(), CommonTestUtil.DEFAULT_PAGE_SWITCH_TIMEOUT_IN_MS);
        assertTrue(mMedia3DView.getCurrentPage() == mActivity.getPortalPage());
    }

    // Testing Case #5
    public void testOrphanFunctions() {
        // ImageDbItemSet
        final long id = 1;
        final ImageDbItem item = new ImageDbItem(mActivity.getContentResolver(), id, null, null, null);
        assertNotNull(item);
        if (item.getThumbnail() != null) {
            assertTrue(item.getDuration() == 0);
            assertNull(item.getPath());
        }
    }
}
