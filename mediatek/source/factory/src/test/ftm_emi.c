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
#include <sys/time.h>
#include <pthread.h>
#include <string.h>
#include <sys/stat.h> 
#include <fcntl.h>
//#include <cutils/pmem.h>
#include <common.h>
#include <miniui.h>
#include <ftm.h>
#include <dlfcn.h>
//#include "MediaHal.h"

#if defined(FEATURE_FTM_EMI)

#define TAG "[FTM_EMI] "
#define PAGE_SIZE 4096
enum { ITEM_PASS, ITEM_FAIL };

static item_t emi_items[] = 
{
    item(ITEM_PASS,   "Test Pass"),
    item(ITEM_FAIL,   "Test Fail"),
    item(-1, NULL),
};

/******** EMI CLK **********/
#define MHz_169    0
#define MHz_182    1
#define MHz_195    2
#define MHz_201    3
#define MHz_208    4
#define MHz_214    5
#define MHz_221    6
#define MHz_227    7
#define MHz_234    8
#define MHz_240    9
#define MHz_247    10
#define MHz_253    11
#define MHz_260    12

#define EMI_CLK   MHz_214
/**************************/

/******** CPU CLK **********/
#define MHz_676    12
#define MHz_689    13
#define MHz_702    14
#define MHz_715    15
#define MHz_728    16
#define MHz_741    17
#define MHz_754    18
#define MHz_767    19
#define MHz_780    20
#define MHz_793    21
#define MHz_806    22

#define CPU_CLK   MHz_702
/**************************/

static unsigned int clk_range[] = {169, 182, 195, 201, 208, 214, 221, 227, 234, 240, 247, 253, 260};

struct emi_info 
{
    char    info[1024];
    bool    stress_result;
    bool    exit_thd;
    bool    exit_clr_thd;
    text_t    title;
    text_t    text;
    pthread_t update_thd;
    pthread_t march_thd;
    struct ftm_module *mod;
    struct textview tv;
    struct itemview *iv;
};

#define mod_to_emi(p)     (struct emi_info*)((char*)(p) + sizeof(struct ftm_module))

static void emi_update_info(struct emi_info *emi, char *info)
{
    char *ptr;
    int rc;   

    /* preare text view info */
    ptr = info;
    ptr += sprintf(ptr, "Stress tests result: %s\n", emi->stress_result ? "PASS" : "FAIL");
    return;
}

/*
 * emi_update_thread: status-update thread function.
 * @priv:
 */
static void *emi_update_thread(void *priv)
{
    struct emi_info *emi = (struct emi_info*)priv;
    struct itemview *iv = emi->iv;
    int count = 1, chkcnt = 5;  

    LOGD(TAG "%s: Start\n", __FUNCTION__);
    
    while (1) {
        usleep(200000);
        chkcnt--;

        if (emi->exit_thd)
            break;

        if (chkcnt > 0)
            continue;        

        /* Prepare the info data to display texts on screen */
        //emi_update_info(emi, emi->info);
        
        iv->set_text(iv, &emi->text);
        iv->redraw(iv);
        chkcnt = 5;
    }
    pthread_exit(NULL);
    
    return NULL;
}

/*
 * update_screen_thread: screen-update thread function.
 * @priv:
 */
static bool update_screen_exit = false;
static void *update_screen_thread(void *priv)
{
    LOGD(TAG "enter update_screen_thread\n");
    while (!update_screen_exit){
        ui_flip();
    }
    LOGD(TAG "exit update_screen_thread\n");
    pthread_exit(NULL);
    return NULL;
}

/*
 * MBIST_March_MTK_Test: MTK March test.
 * @start: start of the test region.
 * @len: length of the test region.
 * Return error code.
 */
#define TEST_PASS 0
#define TEST_FAIL 1
static bool march_exit = false;
int MBIST_March_MTK_Test(unsigned int start, unsigned int len)
{
    unsigned char pattern8;
    volatile unsigned char *MEM8_BASE = (volatile unsigned char *)start;
    int i;

    //Write background to 0
    for (i = 0; i < (int)len; i++) //W0
    {
        MEM8_BASE[i] = 0;
    }
    //Marh Algorithm
    for (i = 0; i <= (int)(len - 1); i++) //R0, W1, R1
    {
        if (MEM8_BASE[i] == 0) MEM8_BASE[i]=0xFF;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0xFF) return TEST_FAIL;       
    }
    for (i = 0; i <= (int)(len - 1); i++) //R1, W0, R0
    {
        if (MEM8_BASE[i] == 0xFF) MEM8_BASE[i]=0x0;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0) return TEST_FAIL;       
    }
    for (i = (len-1); i >= 0; i--) //R0, W1, R1
    {
        if (MEM8_BASE[i] == 0) MEM8_BASE[i]=0xFF;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0xFF) return TEST_FAIL;      
    }
    for (i = (len-1); i >= 0; i--) //R1, W0, R0
    {
        if (MEM8_BASE[i] == 0xFF) MEM8_BASE[i]=0x0;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0) return TEST_FAIL;     
    }
    for (i = (len - 1); i >= 0; i--) //R0
    {
        if (MEM8_BASE[i] != 0) return TEST_FAIL;
    }

    //Write background to 1
    for (i = 0; i < (int)len; i++) //W1
    {
        MEM8_BASE[i] = 0xFF;
    }
    //Marh Algorithm
    for (i = 0; i <= (int)(len - 1); i++) //R1, W0, R0
    {
        if (MEM8_BASE[i] == 0xFF) MEM8_BASE[i]=0x0;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0) return TEST_FAIL;     
    }
    for (i = 0; i <= (int)(len - 1); i++) //R0, W1, R1
    {
        if (MEM8_BASE[i] == 0) MEM8_BASE[i]=0xFF;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0xFF) return TEST_FAIL;     
    }
    for (i = (len - 1); i >= 0; i--) //R1, W0, R0
    {
        if (MEM8_BASE[i] == 0xFF) MEM8_BASE[i]=0;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0) return TEST_FAIL;      
    }
    for (i = (len - 1); i >= 0; i--) //R0, W1, R1
    {
        if (MEM8_BASE[i] == 0) MEM8_BASE[i]=0xFF;
        else return TEST_FAIL;
        if (MEM8_BASE[i]!=0xFF) return TEST_FAIL;     
    }
    for(i = (len-1); i >= 0; i--) //R1
    {
        if (MEM8_BASE[i] != 0xFF) return TEST_FAIL;
    }

    //dbg_print("MTK MBIST March Test - PASS!\n\r");
    return TEST_PASS;
}

/*
 * mtk_march_thread: MTK March test thread function.
 * @priv:
 */
static void *mtk_march_thread(void *priv)
{
    struct emi_info *emi = (struct emi_info*)priv;
    unsigned int *pmem_va;
    int fd;
    
    LOGD(TAG "%s: Start\n", __FUNCTION__);
    
    while (1) {
//        pmem_va = pmem_alloc_sync(0x200000, &fd);
        pmem_va = malloc (0x200000);
        LOGE("EMI PMEM addr = %x\n", (unsigned int)pmem_va);
        if (pmem_va == NULL) {
            LOGD(TAG "allocate PMEM failed\n");
        } else {
            MBIST_March_MTK_Test((unsigned int)pmem_va, 0x200000);
//            pmem_free(pmem_va, 0x200000, fd);
            free (pmem_va);
        }
        if (march_exit) {
            break;
        }
    }

    pthread_exit(NULL);

    return NULL;
}

/*
 * emi_entry: factory mode entry function.
 * @param:
 * @priv:
 * Return error code.
 */
static int emi_entry(struct ftm_param *param, void *priv)
{
    char *ptr;
    int chosen;
    bool exit = false;
    struct emi_info *emi = (struct emi_info *)priv;
    struct itemview *iv;
    pthread_t update_screen;
    update_screen_exit = false;
    const char *dram_overclock = "/sys/bus/platform/drivers/emi_clk_test/emi_clk_test";  
    const char *cpu_overclock = "/sys/bus/platform/drivers/arm_pwr_test/arm_pwr_test_gui";  
    int i = 0;
    int fd = 0;
    ssize_t s;
#define STR_BUF_LEN 10
    char freq[STR_BUF_LEN], result[STR_BUF_LEN];
    
    emi->stress_result = false;
    LOGD(TAG "%s\n", __FUNCTION__);

    init_text(&emi->title, param->name, COLOR_YELLOW);
    init_text(&emi->text, &emi->info[0], COLOR_YELLOW);

    //emi_update_info(emi, emi->info);
  
    emi->exit_thd = false;  

    /* Create a itemview */
    if (!emi->iv) {
        iv = ui_new_itemview();
        if (!iv) {
            LOGD(TAG "No memory");
            return -1;
        }
        emi->iv = iv;
    }
    
    iv = emi->iv;
    iv->set_title(iv, &emi->title);
    iv->set_items(iv, emi_items, 0);
    iv->set_text(iv, &emi->text);
    
    /* create a thread for the test pattern: frame buffer update update */
    pthread_create(&emi->update_thd, NULL, emi_update_thread, priv); 
    
    /* create a thread for the test pattern: MTK March test */
    pthread_create(&emi->march_thd, NULL, mtk_march_thread, priv);

    /* create a thread for screen update */
    if (pthread_create(&update_screen, NULL, update_screen_thread, NULL)) {
        LOGD(TAG "create update_screen_thread failed\n");
    }

    ptr = emi->info;

    /* do CPU overclocking */
    fd = open(cpu_overclock, O_RDWR);
    if (fd < 0) {
        printf("Fail to open: %s. Ignore.\n", cpu_overclock);
    } else { 
        sprintf(freq, "%d", CPU_CLK);
        write(fd, freq, strlen(freq)); // start test
        fsync(fd);

        lseek(fd, 0, SEEK_SET);

        s = read(fd, (void *)result, STR_BUF_LEN); // read back result
        if (s <= 0) {
            printf("Fail to read %s. Ignore.\n", cpu_overclock);
        } else {
            printf("%s\n", result); //output to screen here...
            ptr += sprintf(ptr, "%s\n", result);
            usleep(100000);
        }

        close(fd);
    }
    
    /* do DRAM overclocking */
    fd = open(dram_overclock, O_RDWR);
    if  (fd < 0) {
        printf("Fail to open: %s. Terminate.\n", dram_overclock);
        goto emi_entry_exit;
    } else { 

        s = read(fd, (void*)result, STR_BUF_LEN);
        if (s <= 0)
        {
            printf("Fail to read %s. Terminate\n", dram_overclock);
            goto emi_entry_exit;
        }

        if (result[0] == '2')
        {
            /* for DDR2 clock */
            clk_range[EMI_CLK] = 267;
        }
        lseek(fd, 0 , SEEK_SET);
        sprintf(freq, "%d", clk_range[EMI_CLK]);
        write(fd, freq, strlen(freq)); // Change clk
        fsync(fd);

        lseek(fd, 0, SEEK_SET);

        s = read(fd, (void *)result, STR_BUF_LEN); // read back result
        if (s <= 0) {
            printf("Fail to read %s. Terminate\n", dram_overclock);
            goto emi_entry_exit;
        } else {
            printf("%s\n", result); //output to screen here...
            ptr += sprintf(ptr, "%s\n", result);
            usleep(100000);
        }

        close(fd); 
    }

    /* run multimedia test patterns */
    LOGD(TAG "start to run multimedia test patterns\n");
    emi->stress_result = (MHAL_NO_ERROR == mHalIoCtrl(MHAL_IOCTL_FACTORY , NULL , 0 , NULL , 0 , NULL) ? true : false);
    LOGD(TAG "complete to run multimedia test patterns\n");
    emi->stress_result = true;
    usleep(200000);
    printf("DRAM: clk range = %d\n", clk_range[EMI_CLK]);
    printf("DRAM: stress result = %d\n", emi->stress_result);
    printf("DRAM test all done\n"); //output to screen here...
    ptr += sprintf(ptr, "--> System stability tests result: %s\n", emi->stress_result ? "PASS" : "FAIL");      

emi_entry_exit:

    /* stop the test pattern: frame buffer update */
    update_screen_exit = true;
    /* stop the test pattern: MTK March test */
    march_exit = true;

    if (fd >= 0) {
        close(fd);
    }

    do {
        chosen = iv->run(iv, &exit);
        switch (chosen) {
        case ITEM_PASS:
        case ITEM_FAIL:
            /* report test results */
            if (chosen == ITEM_PASS) {
                emi->mod->test_result = FTM_TEST_PASS;
            } else if (chosen == ITEM_FAIL) {
                emi->mod->test_result = FTM_TEST_FAIL;
            }           
            exit = true;
            break;
        default:
            break;
        }
        
        if (exit) {
            /* stop the screen-update thread */
            emi->exit_thd = true;
            break;
        }
    } while (1);

    pthread_join(emi->update_thd, NULL); 
    pthread_join(emi->march_thd, NULL);
    //update_screen_exit = true;
    pthread_join(update_screen, NULL);

    return 0;
}

/*
 * emi_init: factory mode initialization function.
 * Return error code.
 */
int emi_init(void)
{
    int index;
    int ret = 0;
    struct ftm_module *mod;
    struct emi_info *emi;
    //pid_t p_id;

    LOGD(TAG "%s\n", __FUNCTION__);

    /* Alloc memory and register the test module */
    mod = ftm_alloc(ITEM_EMI, sizeof(struct emi_info));
    if (!mod)
        return -ENOMEM;

    emi = mod_to_emi(mod);
    emi->mod = mod;
  
    /* register the entry function to ftm_module */
    ret = ftm_register(mod, emi_entry, (void*)emi);

    return ret;
}

#endif  /* FEATURE_FTM_EMI */
