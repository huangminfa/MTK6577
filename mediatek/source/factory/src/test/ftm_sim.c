/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 * 
 * MediaTek Inc. (C) 2010. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */

#include <ctype.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>
#include <pthread.h>
#include <fcntl.h>
#include "common.h"
#include "miniui.h"
#include "ftm.h"
// #include "cust_sim.h"
#include "utils.h"


#ifdef FEATURE_FTM_SIM

#define TAG    "[SIM] "

#ifdef MTK_DT_SUPPORT
static void *sim_update_thread_for_dualtalk(void *priv);
#else
static void *sim_update_thread(void *priv);
#endif
int sim_entry(struct ftm_param *param, void *priv);
int sim_init(void);

static int sim_detect(const int fd);
static int wait_rsp_for_sim_detect(const int fd, const int timeout);

extern int send_at (const int fd, const char *pCMD);
extern int openDeviceWithDeviceName(char *deviceName);

#define ERROR_NONE           0
#define ERROR_INVALID_FD    -1
#define ERROR_AT_FAIL       -2

#define RET_ESIMS_NO        1
#define RET_ESIMS_YES       2

#define AT_CMD_BUFF_SIZE 128
#define HALT_INTERVAL 20000

#define AT_RSP_ESIMS    "+ESIMS: "
#define AT_RSP_OK       "OK"

enum {
#ifdef MTK_DT_SUPPORT
    ITEM_SIM_FOR_MODEM_75,
    ITEM_SIM_FOR_MODEM_52,
#else
  ITEM_SIM1,
  ITEM_SIM2,
#endif
  ITEM_PASS,
  ITEM_FAIL,
};

static item_t sim_items[] = {
#ifdef MTK_DT_SUPPORT
    item(ITEM_SIM_FOR_MODEM_75, "Modem 1(MT6575, SIM1)"),
    item(ITEM_SIM_FOR_MODEM_52, "Modem 2(MT6252, SIM2)"),
#else
  item(ITEM_SIM1, "Detect SIM1"),
#ifdef GEMINI
  item(ITEM_SIM2, "Detect SIM2"),
#endif
#endif
  item(ITEM_PASS, "Test Pass"),
  item(ITEM_FAIL, "Test Fail"),
  item(-1, NULL),
};

struct sim_factory {
  char info[1024];
  text_t title;
  text_t text;
  struct ftm_module *mod;
  struct itemview *iv;
  pthread_t update_thread;
  bool exit_thread;
  bool test_done;
  int sim_id;
};

#define mod_to_sim(p)  (struct sim_factory*)((char*)(p) + sizeof(struct ftm_module))

#ifdef MTK_DT_SUPPORT
    #define SIM_ID_FOR_MODEM_75  1
    #define SIM_ID_FOR_MODEM_52  2
#else
#define SIM_ID_1   1
#define SIM_ID_2   2
#endif


#ifdef MTK_DT_SUPPORT
static void *sim_update_thread_for_dualtalk(void *priv) {
    LOGD(TAG "%s: Start\n", __FUNCTION__);
    
    struct sim_factory *sim = (struct sim_factory*)priv;
    struct itemview *iv = sim->iv;
    int ret = RET_ESIMS_NO;
    
    int fd75 = -1;
    fd75 = openDeviceWithDeviceName(CCCI_MODEM_MT6575);
    if(fd75 < 0) {
        LOGD(TAG "fail to open %s", CCCI_MODEM_MT6575);
        return NULL;
    }
    
    int fd52 = -1;
    fd52 = openDeviceWithDeviceName(CCCI_MODEM_MT6252);
    if(fd52 < 0) {
        LOGD(TAG, "fail to open %s", CCCI_MODEM_MT6252);
        return NULL;
    }
    
    LOGD(TAG "dual device has been opened...\n");
    
    while(1) {
        usleep(200000);
        if(sim->exit_thread) {
            LOGD(TAG "exit thread");
            break;
        }
        
        if(!sim->test_done) {
            if(sim->sim_id == SIM_ID_FOR_MODEM_75) {
                ret = sim_detect(fd75);
            } else if(sim->sim_id == SIM_ID_FOR_MODEM_52) {
                ret = sim_detect(fd52);
            } else {
                LOGD(TAG "invalid test item: %d\n", sim->sim_id);
            }
            
            char *s = NULL;
            if(RET_ESIMS_YES == ret) {
                s = "Yes";
            } else if(RET_ESIMS_NO == ret) {
                s = "No";
            } else {
                s = "Fail";
            }
            sprintf(sim->info + strlen(sim->info), "Detect SIM%d: %s\n", sim->sim_id, s);
            iv->redraw(iv);
            sim->test_done = true;
        }
    }
    
    close(fd75);
    close(fd52);
    
    LOGD(TAG "%s: End\n", __FUNCTION__);
    return NULL;
}
#else
static void *sim_update_thread(void *priv)
{
  LOGD(TAG "%s: Start\n", __FUNCTION__);
  
  struct sim_factory *sim = (struct sim_factory*)priv;
  struct itemview *iv = sim->iv;

  int fd = -1;
  fd = open("/dev/ttyC0", O_RDWR);
  if(fd < 0) {
    LOGD(TAG "Fail to open ttyC0: %s\n", strerror(errno));
    //sprintf(sim->info, "%s", "ttyC0 open failed\n");
    //iv->redraw(iv);
    return NULL;
  }
  LOGD(TAG "ttyC0 has been opened...\n");
  
  const int rdTimes = 3;
  int rdCount = 0;

  while(1) {
    usleep(200000);
    if(sim->exit_thread) {
      LOGD(TAG "Exit thread\n");
      break;
    }

    memset(sim->info, 0, sizeof(sim->info) / sizeof(*(sim->info)));
    if(!sim->test_done) {
      bool ret = false;
      sim->test_done = true;

      const int BUF_SIZE = 256;
      char cmd_buf[BUF_SIZE];
      char rsp_buf[BUF_SIZE];
      const int HALT_TIME = 100000;

      memset(cmd_buf, 0, sizeof(cmd_buf));
      memset(rsp_buf, 0, sizeof(rsp_buf));

      if(SIM_ID_2 == sim->sim_id) { // detect SIM2
        // switch UART to SIM2
        strcpy(cmd_buf, "AT+ESUO=5\r\n");
        write(fd, cmd_buf, strlen(cmd_buf));
        usleep(HALT_TIME);
        LOGD(TAG "Send AT+ESUO=5\n");
        read(fd, cmd_buf, BUF_SIZE);
        LOGD(TAG "------AT+ESUO=5 start------\n");
        LOGD(TAG "%s\n", cmd_buf);
        LOGD(TAG "------AT+ESUO=5 end------\n");

        strcpy(cmd_buf, "AT+ESIMS\r\n");
        write(fd, cmd_buf, strlen(cmd_buf));
        LOGD(TAG "Send AT+ESIMS\n");
        usleep(HALT_TIME);
        read(fd, rsp_buf, BUF_SIZE);
        //usleep(HALT_TIME);
        LOGD(TAG "------AT+ESIMS(SIM2) start------\n");
        LOGD(TAG "%s\n", rsp_buf);
        LOGD(TAG "------AT+ESIMS(SIM2) end------\n");

        strcpy(cmd_buf, "AT+ESUO=4\r\n");
        write(fd, cmd_buf, strlen(cmd_buf));
        LOGD(TAG "Send AT+ESUO=4\n");
        usleep(HALT_TIME);
        read(fd, cmd_buf, BUF_SIZE);
        LOGD(TAG "------AT+ESUO=4 start------\n");
        LOGD(TAG "%s\n", cmd_buf);
        LOGD(TAG "------AT+ESUO=4 end------\n");
      } else 
      { // detect SIM1
        LOGD(TAG "\n\n");
        strcpy(cmd_buf, "AT+ESIMS\r\n");
        write(fd, cmd_buf, strlen(cmd_buf));
        LOGD(TAG "Send AT+ESIMS\n");
        usleep(HALT_TIME);
        read(fd, rsp_buf, BUF_SIZE);
        //usleep(HALT_TIME);
        LOGD(TAG "------AT+ESIMS(SIM1) start------\n");
        LOGD(TAG "%s\n", rsp_buf);
        LOGD(TAG "------AT+ESIMS(SIM1) end------\n");
      }


      const char *tok = "+ESIMS: ";
      const char *tok_eind = "+EIND";
      char *p = NULL;
      char *p_eind = NULL;
      p = strstr(rsp_buf, tok);
      p_eind = strstr(rsp_buf, tok_eind);
      if(p) {
        p += strlen(tok);
        if('1' == *p) {
          rdCount = 0;
          ret = true;
        }
      } else if(p_eind) {
      	LOGD(TAG, "detect +EIND, redo\n");
        sim->test_done = false;
        continue;
      } else {
        if(rdCount < rdTimes) {
          LOGD(TAG, "detect unknown response, redo\n");
          rdCount++;
          sim->test_done = false;
          continue;
        }
      }

      if(ret) {
        sprintf(sim->info + strlen(sim->info),
                "Detect SIM%d: Pass\n", sim->sim_id);
      } else {
        sprintf(sim->info + strlen(sim->info),
                "Detect SIM%d: Fail\n", sim->sim_id);
      }
      LOGD(TAG "redraw\n");
      iv->redraw(iv);
    } // end if(!sim->test_done)
  } // end while(1)

  close(fd);
  fd = -1;

  LOGD(TAG "%s: Exit\n", __FUNCTION__);

  return NULL;
}
#endif

int sim_entry(struct ftm_param *param, void *priv)
{
  bool exit = false;
  struct sim_factory *sim = (struct sim_factory*)priv;
  struct itemview *iv = NULL;

  LOGD(TAG "%s: Start\n", __FUNCTION__);

  init_text(&sim->title, param->name, COLOR_YELLOW);
  init_text(&sim->text, &sim->info[0], COLOR_YELLOW);

  if(NULL == sim->iv) {
    iv = ui_new_itemview();
    if(!iv) {
      LOGD(TAG "No memory for item view");
      return -1;
    }
    sim->iv = iv;
  }
  iv = sim->iv;
  iv->set_title(iv, &sim->title);
  iv->set_items(iv, sim_items, 0);
  iv->set_text(iv, &sim->text);

  sim->exit_thread = false;
#ifdef MTK_DT_SUPPORT
  pthread_create(&sim->update_thread, NULL, sim_update_thread_for_dualtalk, priv);
#else
  pthread_create(&sim->update_thread, NULL, sim_update_thread, priv);
#endif

  while(!exit) {
    int chosen = iv->run(iv, &exit);
    switch(chosen) {
#ifdef MTK_DT_SUPPORT
      case ITEM_SIM_FOR_MODEM_75:
        sim->sim_id = SIM_ID_FOR_MODEM_75;
        sim->test_done = false;
        exit = false;
        break;
        
      case ITEM_SIM_FOR_MODEM_52:
        sim->sim_id = SIM_ID_FOR_MODEM_52;
        sim->test_done = false;
        exit = false;
        break;
#else
      case ITEM_SIM1:
        sim->sim_id = SIM_ID_1;
        sim->test_done = false;
        exit = false;
        break;

      case ITEM_SIM2:
        sim->sim_id = SIM_ID_2;
        sim->test_done = false;
        exit = false;
        break;
#endif
      case ITEM_PASS:
      case ITEM_FAIL:
        if(ITEM_PASS == chosen) {
          sim->mod->test_result = FTM_TEST_PASS;
        } else {
          sim->mod->test_result = FTM_TEST_FAIL;
        }

        sim->exit_thread = true;
        sim->test_done = true;
        exit = true;
        break;

      default:
	    sim->exit_thread = true;
        sim->test_done = true;
	    exit = true;
	    LOGD(TAG "DEFAULT EXIT\n");
        break;
    } // end switch(chosen)
	  if(exit) {
        sim->exit_thread = true;
    }
  } // end while(!exit)
  pthread_join(sim->update_thread, NULL);

  LOGD(TAG "%s: End\n", __FUNCTION__);

  return 0;
}

int sim_init(void)
{
  int ret = 0;
  struct ftm_module *mod;
  struct sim_factory *sim;

  LOGD(TAG "%s: Start\n", __FUNCTION__);

  mod = ftm_alloc(ITEM_SIM, sizeof(struct sim_factory));
  if(!mod) {
    return -ENOMEM;
  }
  sim = mod_to_sim(mod);
  sim->mod = mod;
  sim->test_done = true;

  ret = ftm_register(mod, sim_entry, (void*)sim);
  if(ret) {
    LOGD(TAG "register sim_entry failed (%d)\n", ret);
  }

  return ret;
}

static int wait_rsp_for_sim_detect(const int fd, const int timeout) {
    LOGD(TAG "%s start\n", __FUNCTION__);
    
    if(fd < 0) {
        LOGD(TAG "invalid fd\n");
        return ERROR_INVALID_FD;
    }
    
    char buf[AT_CMD_BUFF_SIZE] = {0};
    char *p = NULL;
    int rdCount = 0;
    int rdTimes = timeout * 1000 / HALT_INTERVAL;
    int ret = 0;
    
    LOGD(TAG "wait for AT ack...");
    for(rdCount = 0; rdCount < rdTimes; ++rdCount) {
        ret = read(fd, buf, AT_CMD_BUFF_SIZE);
        LOGD(TAG "ACK: %s\n", buf);
        
        p = NULL;
        p = strstr(buf, AT_RSP_ESIMS);
        if(p) {
            p += strlen(AT_RSP_ESIMS);
            if('1' == *p) {
                ret = RET_ESIMS_YES;
            } else {
                ret = RET_ESIMS_NO;
            }
            break;
        }
    }
    
    LOGD(TAG "%s end\n", __FUNCTION__);
    return ret;
}

static int sim_detect(const int fd) {
    LOGD(TAG "%s start\n", __FUNCTION__);
    
    if(fd < 0) {
        LOGD(TAG "invalid fd\n");
        return ERROR_INVALID_FD;
    }
    
    int ret = 0;
    
    LOGD(TAG "[AT] detect sim status\n");
    if(send_at(fd, "AT+ESIMS\r\n")) return ERROR_AT_FAIL;
    ret = wait_rsp_for_sim_detect(fd, 3000);
    LOGD(TAG "%s end\n", __FUNCTION__);
    
    return ret;
}

#endif
