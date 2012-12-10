
package com.mediatek.ngin3d.debugtools.messagepackage;

public final class Constants {

    private Constants() {
        // Do nothing
    }

    // ******************Message******************
    public static final byte M_HANDSHAKE = 0x01;
    public static final byte M_REQUEST = 0x02;
    public static final byte M_RESPONSE = 0x03;
    public static final byte M_CONTROL = 0x04;
    public static final byte M_MODIFY = 0x05;
    public static final byte M_BYE = 0x06;

    // *****************Handshake******************
    public static final byte H_HANDSHAKE_SYNC = 0x11;

    // *****************Request******************
    public static final byte I_RQ_STAGE_INFO_ALL = 0x21;
    public static final byte I_RQ_STAGE_INFO_UNIT = 0x22;
    public static final byte I_RQ_SATRT_GET_DEVICE_PERFORMANCE_ALL = 0x23;
    public static final byte I_RQ_SATRT_GET_DEVICE_PERFORMANCE_CPU = 0x24;
    public static final byte I_RQ_SATRT_GET_DEVICE_PERFORMANCE_FPS = 0x25;
    public static final byte I_RQ_SATRT_GET_DEVICE_PERFORMANCE_MEM = 0x26;
    public static final byte I_RQ_STOP_GET_DEVICE_PERFORMANCE_ALL = 0x27;
    public static final byte I_RQ_STOP_GET_DEVICE_PERFORMANCE_CPU = 0x28;
    public static final byte I_RQ_STOP_GET_DEVICE_PERFORMANCE_FPS = 0x29;
    public static final byte I_RQ_STOP_GET_DEVICE_PERFORMANCE_MEM = 0x30;
    public static final byte I_RQ_SATRT_GET_FRAME_INTERVAL = 0x31;
    public static final byte I_RQ_STOP_GET_FRAME_INTERVAL = 0x32;
    public static final byte I_RQ_ANIMATION_INFO_UNIT = 0x33;
    public static final byte I_RQ_SET_DEVICE_PERFORMANCE_INTERVAL = 0x34;

    // *****************Response******************
    // get dump
    public static final byte I_RP_GET_STAGE_DUMP_INFO_STRING_OK = 0x41;
    public static final byte I_RP_GET_STAGE_DUMP_INFO_BYTEARRAY_OK = 0x42;
    public static final byte I_RP_GET_STAGE_DUMP_INFO_FAIL = 0x43;
    // bye
    public static final byte I_RP_BYE_OK = 0x44;
    public static final byte I_RP_SERVICE_ACTIVE_BYE = 0x45;
    // sync
    public static final byte I_RP_HANDSHAKE_OK = 0x46;
    // modify
    public static final byte I_RP_MODIFY_STAGE_OK = 0x47;
    public static final byte I_RP_MODIFY_STAGE_FAIL = 0x48;
    // performance
    public static final byte I_RP_SATRT_GET_DEVICE_PERFORMANCE_OK = 0x49;
    public static final byte I_RP_STOP_GET_DEVICE_PERFORMANCE_OK = 0x50;
    // control
    public static final byte I_RP_NGIN3D_PASUE_RENDERING_OK = 0x51;
    public static final byte I_RP_NGIN3D_RESUME_RENDERING_OK = 0x52;
    public static final byte I_RP_NGIN3D_TICKTIME_RENDERING_OK = 0x53;
    public static final byte I_RP_NGIN3D_START_GET_FRAME_INTERVAL_OK = 0x54;
    public static final byte I_RP_NGIN3D_STOP_GET_FRAME_INTERVAL_OK = 0x55;
    public static final byte I_RP_NGIN3D_REMOVE_ACTOR_BY_ID_OK = 0x56;
    public static final byte I_RP_GET_ANIMATION_DUMP_INFO_STRING_OK = 0x57;
    public static final byte I_RP_GET_ANIMATION_DUMP_INFO_BYTEARRAY_OK = 0x58;
    public static final byte I_RP_GET_ANIMATION_DUMP_INFO_FAIL = 0x59;

    // *****************Control******************
    public static final byte I_C_NGIN3D_PASUE_RENDERING = 0x61;
    public static final byte I_C_NGIN3D_RESUME_RENDERING = 0x62;
    public static final byte I_C_NGIN3D_TICKTIME_RENDERING = 0x63;
    public static final byte I_C_NGIN3D_REMOVE_ACTOR = 0x64;

    // *****************Modify******************
    public static final byte I_MD_MODIFY_STAGE_UNIT_BY_ID = 0x71;

    // *****************Bye******************
    public static final byte I_B_BYE = 0x72;

    // performance
    public static final byte I_RP_SATRT_GET_DEVICE_CPU_OK = 0x73;
    public static final byte I_RP_SATRT_GET_DEVICE_MEM_OK = 0x74;
    public static final byte I_RP_SATRT_GET_DEVICE_FPS_OK = 0x75;
    public static final byte I_RP_STOP_GET_DEVICE_CPU_OK = 0x76;
    public static final byte I_RP_STOP_GET_DEVICE_MEM_OK = 0x77;
    public static final byte I_RP_STOP_GET_DEVICE_FPS_OK = 0x78;

    // control
    public static final byte I_RP_SET_DEVICE_PERFORMANCE_INTERVAL_OK = 0x79;
    public static final int EXTEND_HEADER_SIZE = 5;
}
