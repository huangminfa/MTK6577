#ifndef __CDMA_MANAGER_H__
#define __CDMA_MANAGER_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "typedef.h"
#include "vagci-api.h"
#include "cdma_op1_pmtk_interface.h"
#include "agps_struct.h"

#define BLOCKING_SEND_ASSIST_REQ 0
#define MAX_CDMA_SI_REQ_NUM    1
#define GPS_ASSIST_DATA_N_SATE_MEAS     16 /* max allowed = 16, but typical = 4 to 12 */
#define DFLT_QOS_PERFORMANCE         100//change from 16 to 100 for SIMA test
#define CDMA_AGPS_QUERY_TIMER_INTERVAL 1000

typedef enum
{
    MMI_CDMA_AGPS_STATE_IDLE,
    MMI_CDMA_AGPS_STATE_SI_BEGIN,
    MMI_CDMA_AGPS_STATE_WAIT_AIDING,
    MMI_CDMA_AGPS_STATE_WAIT_RSLT,
    //C.K. add for VIA request; STOP report PRM after get MA rslt
    MMI_CDMA_AGPS_STATE_MA_DONE,
    MMI_CDMA_AGPS_STATE_NI_START,
    MMI_CDMA_AGPS_STATE_NI_WAIT_CNF,
    MMI_CDMA_AGPS_STATE_NI_ACTIVATE,
    MMI_CDMA_AGPS_STATE_NI_BEGIN,
} mmi_cdma_agps_state_enum;

typedef enum
{
    GPS_ASSIST_IDLE,
    GPS_ASSIST_WAIT_TIME,
    GPS_ASSIST_WAIT_OTHER,
} gps_cdma_assist_state_enum;

typedef enum
{
    CDMA_ASSIST_BIT_CLEAR,
    CDMA_ASSIST_BIT_NEED,         /* Need to request CBP */
    CDMA_ASSIST_BIT_DATA_INVALID, /* Wait data from CBP */
    CDMA_ASSIST_BIT_DATA_VALID,   /* Need to send to MNL */
} gps_cdma_assist_bit_val_enum;

typedef enum
{
    CDMA_GPS_START_MODE_NONE,
    CDMA_GPS_START_MODE_SI,
    CDMA_GPS_START_MODE_NI
} mmi_gps_start_mode_enum;

typedef struct
{
    gps_cdma_assist_bit_val_enum        ref_time_data_need;
    precise_time_info                   ref_time_data;
    gps_cdma_assist_bit_val_enum        acquisition_data_need;
    acqassist_resp                      acquisition_data;
    gps_cdma_assist_bit_val_enum        ref_location_data_need;
    aflt_refloc_data                    ref_location_data;
    gps_cdma_assist_bit_val_enum        ionosphere_data_need;
    ion_data                            ionosphere_data;
    gps_cdma_assist_bit_val_enum        almanac_data_need;
    alm_resp                            almanac_data;
    gps_cdma_assist_bit_val_enum        eph_data_need;
    gps_eph_prn_resp                    eph_data;
} gps_cdma_assist_data_cache_struct;

typedef struct
{
    gps_power_on_type   gps_power_on_req_info;
    U16 req_id;
    MMI_BOOL    is_used;
    mmi_cdma_agps_state_enum state;
    mmi_gps_start_mode_enum start_mode;

    //For periodic report result to CBP
    uint32 gps_session_cnt;         /*the number of GPS fixes attempted*/
    uint32 num_of_pos;              /*the number of positions*/
}mmi_cdma_mgr_req_struct;

typedef struct
{
    /* public */
    U16 cur_id;
    /* molr */
    mmi_cdma_mgr_req_struct req_list[MAX_CDMA_SI_REQ_NUM];

    //server config
    pde_config_info pde_config;
    gps_mcp_cfg gps_mcp_config;

    //gps data
    gps_assist_bitmap_struct    assist_bitmap;
    gps_cdma_assist_state_enum       assist_state;
    gps_cdma_assist_data_cache_struct assist_data;
    pmtk_array      meas; //only use GPS_ASSIST_DATA_N_SATE_MEAS instead of 32
}mmi_cdma_mgr_context_struct;

void cdma_agps_assist_data_parser(const kal_char *buffer, gps_assist_bitmap_struct* assist_data);
int cdma_send_request2cbp(int id, void *param, int datalen);
void cdma_mgr_send_cache_assist_data_to_gps();
void cdma_mgr_send_cache_assist_data_to_gps_time();
void cdma_mgr_send_cache_assist_data_to_gps_other();
kal_bool cdma_mgr_check_and_send_mnl_query( uint8 query_mode ); // 0:pos/PMTK485 1:measurement/PMTK486
U8 cdma_mgr_gps_ctx_idx_from_id( U32 id );
void cdma_mgr_abort_free_req(U8 index);
void cdma_mgr_handle_req2cbp_err(int rslt);
int get_pmtk_int_value_at(char* pmtk, int at);
void cdma_mgr_send_loc_error_rsp(UINT8 fix_mode ,uint8 error, uint32 gps_session_count, uint32 num_of_position);
void cdma_mgr_cbp_rsp_parsed_posi_report_hdlr( posi_data_resp* posi_data_rslt );
void cdma_process_meas_data( const kal_char* buffer, int seq );
void cdma_mgr_cbp_rsp_reset_assist_data_hdlr();
void cdma_mgr_cbp_rsp_gps_poweron_req_hdlr( gps_power_on_type* power_on_cfg );
void mtk_agps_request_cdma_up_ni( gps_power_on_type* power_on_cfg );
void cdma_mgr_cbp_rsp_stop_hdlr(gps_rpc_fix_id* stop_req_info);
void cdma_mgr_ni_open_gps_from_native();
void cdma_mgr_cbp_error_session_hdlr(gps_event_resp* error_info);
void cdma_mgr_send_gps_cmd_meas_query();
void cdma_mgr_send_gps_cmd_pos_query();

const char* enum2str_via_msg(int t);
const char* enum2str_cdma_agps_state(mmi_cdma_agps_state_enum t);

extern void gps_cp_start_timer(kal_uint8 timer_id, kal_uint32 period, kal_timer_func_ptr timer_expiry, void *arg);
extern void gps_cp_stop_timer(kal_uint8 timer_id);
#ifdef __cplusplus
}
#endif

#endif
