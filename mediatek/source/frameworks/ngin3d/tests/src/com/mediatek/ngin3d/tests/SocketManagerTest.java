
package com.mediatek.ngin3d.tests;

import android.test.ActivityInstrumentationTestCase2;
import com.mediatek.ngin3d.Point;
import com.mediatek.ngin3d.Stage;
import com.mediatek.ngin3d.Image;
import com.mediatek.ngin3d.presentation.PresentationEngine;
import com.mediatek.ngin3d.utils.Ngin3dDebugUtils;
import com.mediatek.ngin3d.Rotation;
import com.mediatek.ngin3d.animation.BasicAnimation;
import com.mediatek.ngin3d.animation.Mode;
import com.mediatek.ngin3d.animation.PropertyAnimation;
import com.mediatek.ngin3d.debugtools.android.serveragent.SocketManager;
import com.mediatek.ngin3d.debugtools.messagepackage.Constants;

public class SocketManagerTest extends ActivityInstrumentationTestCase2<PresentationStubActivity> {

    protected Stage testStage;
    protected PresentationEngine testPresentationEngine;
    protected Image testImage;
    protected BasicAnimation move;
    protected BasicAnimation rotate;
    protected SocketManager socketManager;

    public SocketManagerTest() {
        super("com.mediatek.ngin3d.tests", PresentationStubActivity.class);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("setUp");
        testStage = new Stage();
        Ngin3dDebugUtils ngin3dDt = new Ngin3dDebugUtils(
                getActivity().getStageView().getPresentationEngine(),
                testStage);
        /*Ngin3dDebugUtils ngin3dDt = new Ngin3dDebugUtils(
                (JirrPresentationEngine) getActivity().getStageView().getPresentationEngine(),
                testStage);*/
        socketManager = new SocketManager(31286, ngin3dDt);
        socketManager.startListen();

        // testPresentationEngine = Ngin3d.createPresentationEngine(testStage);
        // testPresentationEngine.initialize(480, 800);

        testImage = Image.createFromResource(getInstrumentation().getContext().getResources(),
                R.drawable.icon);
        testStage.add(testImage);

        move = new PropertyAnimation(testImage, "position", new Point(0, 0), new Point(480, 800))
                .setMode(Mode.EASE_IN_OUT_CUBIC)
                .setLoop(true)
                .setAutoReverse(true);

        rotate = new PropertyAnimation(testImage, "rotation", new Rotation(0, 0, 0), new Rotation(
                0, 0, 360))
                .setMode(Mode.EASE_IN_SINE)
                .setLoop(true)
                .setAutoReverse(true);

        move.start();
        rotate.start();

    }

    @Override
    protected void tearDown() throws Exception {
        /*
         * testPresentationEngine.uninitialize(); testStage = null;
         * testPresentationEngine = null; testImage = null;
         */
        System.out.println("tear down");
        if (socketManager != null) {
            socketManager.stopListen();
            socketManager = null;
        }

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        super.tearDown();
    }

    SocketThread mClientThread;
    private static final String COMMANDER = "mediatek";



    public void testPCConnection1() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("sync");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_HANDSHAKE_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }

    public void testGet01() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("get");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_GET_STAGE_DUMP_INFO_BYTEARRAY_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testGet02() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("get 52546");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_GET_STAGE_DUMP_INFO_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }

    public void testGet03() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("get animation 52546");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_GET_ANIMATION_DUMP_INFO_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify01() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 position 0,1,2");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify02() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 anchor 0,1,2");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify03() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 scale 0,1,2");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify04() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 ratation 0,1,2");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify05() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 color 123,125,254,255");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify06() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 visible 1");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify07() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 flag 1");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify08() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 zorderontop 1");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testModify09() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("modi 52546 name testname");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_MODIFY_STAGE_FAIL, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testControl1() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("pause ngin");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_NGIN3D_PASUE_RENDERING_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testControl2() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("resume ngin");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_NGIN3D_RESUME_RENDERING_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testControl3() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("tick");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_NGIN3D_TICKTIME_RENDERING_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testControl4() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("tick 500");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_NGIN3D_TICKTIME_RENDERING_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testControl5() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("set 100");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_SET_DEVICE_PERFORMANCE_INTERVAL_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    

    public void testPerf01() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf all");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_SATRT_GET_DEVICE_PERFORMANCE_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testPerf02() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf all");
        mClientThread.waitForEventReceived();
        mClientThread.mConstantsState = -1;
        mClientThread.sendmsg("stop perf all");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_STOP_GET_DEVICE_PERFORMANCE_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();
    }

    
    public void testPerf03() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf cpu");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_SATRT_GET_DEVICE_CPU_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testPerf04() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf cpu");
        mClientThread.waitForEventReceived();
        mClientThread.mConstantsState = -1;
        mClientThread.sendmsg("stop perf cpu");
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(Constants.I_RP_STOP_GET_DEVICE_CPU_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();
    }
    
    
    public void testPerf05() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf mem");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_SATRT_GET_DEVICE_MEM_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testPerf06() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf mem");
        mClientThread.waitForEventReceived();
        mClientThread.mConstantsState = -1;
        mClientThread.sendmsg("stop perf mem");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_STOP_GET_DEVICE_MEM_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();
    }
    
    
    public void testPerf07() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf fps");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_SATRT_GET_DEVICE_FPS_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testPerf08() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start perf fps");
        mClientThread.waitForEventReceived();
        mClientThread.mConstantsState = -1;
        mClientThread.sendmsg("stop perf fps");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_STOP_GET_DEVICE_FPS_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();
    }
    
    public void testPerf09() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start frame");
        mClientThread.waitForEventReceived();

//        assertEquals(Constants.I_RP_NGIN3D_START_GET_FRAME_INTERVAL_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
    
    public void testPerf10() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("start frame");
        mClientThread.waitForEventReceived();
        mClientThread.mConstantsState = -1;
        mClientThread.sendmsg("stop frame");
        mClientThread.waitForEventReceived();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertEquals(Constants.I_RP_NGIN3D_STOP_GET_FRAME_INTERVAL_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();
    }
    
    public void testRemove() {
        mClientThread = new SocketThread(COMMANDER);
        mClientThread.start();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mClientThread.sendmsg("remove 52681");
        mClientThread.waitForEventReceived();

        assertEquals(Constants.I_RP_NGIN3D_REMOVE_ACTOR_BY_ID_OK, mClientThread.mConstantsState);
        mClientThread.closeSocket();

    }
 

}
