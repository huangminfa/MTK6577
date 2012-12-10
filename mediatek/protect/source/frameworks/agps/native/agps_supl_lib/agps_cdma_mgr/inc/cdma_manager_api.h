#ifndef __CDMA_MANAGER_API_H__
#define __CDMA_MANAGER_API_H__

#ifdef __cplusplus
extern "C" {
#endif

#include "typedef.h"
#include "vagci-api.h"
#include "cdma_op1_pmtk_interface.h"
#include "mtk_agps_def.h"

void cdma_mgr_init();
void cdma_mgr_deinit();
void cdma_mgr_cbp_init();
void cdma_mgr_cbp_deinit();
void cdma_mgr_ap_init();
void cdma_mgr_ap_deinit();
void cdma_mgr_set_cb( t_viarpc_Callbacks* des_callback,  response_callback  resp_cb, err_callback err_cb);
void cdma_mgr_errno_cb(int id, int err);
void cdma_mgr_response_cb(int id, void *data, int datalen);
void mtk_agps_request_cdma_up_si(agps_supl_mode_enum mode);
void cm_update_cdma_profile( int mcp_enable, gps_mcp_cfg mcp_cfg, int pde_enable,pde_config_info pde_cfg );
void cdma_mgr_gps_agps_parser(kal_uint16 cmd, const kal_char *buffer, kal_uint32 length);
void cdma_mgr_cbp_rsp_start_ack_hdlr(kal_uint16 req_id);
void cdma_mgr_molr_stop_gps();
void cdma_mgr_reset_global_variable();
void cdma_mgr_reset_ongoing_session();
int cdma_mgr_dlopen_firmware();

//DEBUG
void test1();
void test_si_mb();
void test_pmtk_codec();
void test_agps_si_mb();
void test_agps_si_ma();
void test_agps_ni_mb();
void test_agps_ni_ma();
void test_agps_ni_reset();
void test_agps_ni_mss();
#ifdef __cplusplus
}
#endif

#endif
