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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <fcntl.h>
#include <unistd.h>

#ifdef __cplusplus
extern "C"
{
#endif	
	int emmc_name_to_number(const char *name);	
#ifdef __cplusplus
}
#endif


#define MAX_EMMC_PARTITIONS 16

static struct {
    char name[16];
    int number;
} emmc_part_map[MAX_EMMC_PARTITIONS];

static int emmc_part_count = -1;

static void find_emmc_partitions(void)
{
    int fd;
    char buf[1024];
    char *pemmcbufp;
    ssize_t pemmcsize;
    int r;

	printf("%s: emmc_part_count=%d \n", __func__, emmc_part_count);

    fd = open("/proc/emmc", O_RDONLY);
    if (fd < 0)
        return;

    buf[sizeof(buf) - 1] = '\0';
    pemmcsize = read(fd, buf, sizeof(buf) - 1);
    pemmcbufp = buf;
    while (pemmcsize > 0) {
        int partno, start_sect, nr_sects;
        char partition_name[16];
        partition_name[0] = '\0';
        partno = -1;
        r = sscanf(pemmcbufp, "emmc_p%d: %x %x %15s",
                   &partno, &start_sect, &nr_sects, partition_name);
        if ((r == 4) && (partition_name[0] == '"')) {
            char *x = strchr(partition_name + 1, '"');
            if (x) {
                *x = 0;
            }
            printf("emmc partition %d, %s\n", partno, partition_name + 1);
            if (emmc_part_count < MAX_EMMC_PARTITIONS) {
                strcpy(emmc_part_map[emmc_part_count].name, partition_name + 1);
                emmc_part_map[emmc_part_count].number = partno;
                emmc_part_count++;
            } else {
                printf("too many emmc partitions\n");
            }
        }
        while (pemmcsize > 0 && *pemmcbufp != '\n') {
            pemmcbufp++;
            pemmcsize--;
        }
        if (pemmcsize > 0) {
            pemmcbufp++;
            pemmcsize--;
        }
    }
    close(fd);
}

int emmc_name_to_number(const char *name)
{
    int n;
    if (emmc_part_count < 0) {
        emmc_part_count = 0;
        find_emmc_partitions();
    }
    for (n = 0; n < emmc_part_count; n++) {
        if (!strcmp(name, emmc_part_map[n].name)) {
            return emmc_part_map[n].number;
        }
    }
    return -1;
}

