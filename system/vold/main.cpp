/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#include <stdio.h>
#include <stdlib.h>
#include <errno.h>
#include <string.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <fcntl.h>
#include <dirent.h>
#include <cutils/properties.h>


#include "cutils/log.h"

#include "VolumeManager.h"
#include "CommandListener.h"
#include "NetlinkManager.h"
#include "DirectVolume.h"
#include "libnvram.h"
#include "CFG_OMADMUSB_File.h"
#include "cryptfs.h"

#define LOG_TAG "Vold"


extern int iFileOMADMUSBLID;

static int process_config(VolumeManager *vm);
static void coldboot(const char *path);

#ifdef MTK_EMMC_SUPPORT
	#ifdef __cplusplus
	extern "C"
	{
	#endif	
		extern int emmc_name_to_number(const char *name);	
	#ifdef __cplusplus
	}
	#endif
#endif

#define MAX_NVRAM_RESTRORE_READY_RETRY_NUM	(20)

static int is_meta_boot(void)
{
  int fd;
  size_t s;
  char boot_mode;
  
  fd = open("/sys/class/BOOT/BOOT/boot/boot_mode", O_RDWR);
  if (fd < 0) 
  {
    printf("fail to open: %s\n", "/sys/class/BOOT/BOOT/boot/boot_mode");
    return 0;
  }
  
  s = read(fd, (void *)&boot_mode, sizeof(boot_mode));
  close(fd);
  
  if(s <= 0)
  {
    return 0;
  }
  
  if ((boot_mode != '1'))
  {
	  printf("Current boot_mode is Not meta mode\n");
    return 0;
  }

  printf("META Mode Booting.....\n");
  return 1;  
}
int main() {

    VolumeManager *vm;
    CommandListener *cl;
    NetlinkManager *nm;

//M{
    int fd = 0;
    char value[20];
    int count = 0;
	int Ret = 0;
    int rec_size;
    int rec_num;
    int file_lid = iFileOMADMUSBLID;
    OMADMUSB_CFG_Struct mStGet;

	int nvram_restore_ready_retry=0;
	char nvram_init_val[32] = {0};
    memset(&mStGet, 0, sizeof(OMADMUSB_CFG_Struct));
	LOGD("Check whether nvram restore ready!\n");
	while(nvram_restore_ready_retry < MAX_NVRAM_RESTRORE_READY_RETRY_NUM){
		nvram_restore_ready_retry++;
		property_get("nvram_init", nvram_init_val, NULL);
		if(strcmp(nvram_init_val, "Ready") == 0){
			LOGD("nvram restore ready!\n");
			break;
		}else{
			usleep(500*1000);
		}
	}

	if(nvram_restore_ready_retry >= MAX_NVRAM_RESTRORE_READY_RETRY_NUM){
		LOGD("Get nvram restore ready fail!\n");
	}


	Ret = vm->NvramAccessForOMADM(&mStGet, true);
	SLOGD("OMADM NVRAM read  Ret=%d, IsEnable=%d, Usb=%d, Adb=%d, Rndis=%d", Ret, mStGet.iIsEnable, mStGet.iUsb, mStGet.iAdb, mStGet.iRndis);
	if (Ret < 0) {
		SLOGE("vold main read NVRAM failed!");
	} else {
		if (1 == mStGet.iIsEnable) {//usb enable
		//do nothing
		
		/*
			if ((fd = open("/sys/class/usb_composite/usb_mass_storage/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite enable");
				exit(1);
	    	}
	    	count = snprintf(value, sizeof(value), "%d\n", mStGet.iUsb);
	    	Ret = write(fd, value, count);
			SLOGE("OMADM USB write  Ret=%d  count=%d", Ret, count);
	    	close(fd);
	    
	    	if ((fd = open("/sys/class/usb_composite/adb/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite adb enable");
				exit(1);
	    	}
			count = snprintf(value, sizeof(value), "%d\n", mStGet.iAdb);
	    	Ret = write(fd, value, count);
			SLOGE("OMADM ADB write  Ret=%d  count=%d", Ret, count);
	    	close(fd);

	    	if ((fd = open("/sys/class/usb_composite/rndis/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite adb enable");
				exit(1);
	    	}
			count = snprintf(value, sizeof(value), "%d\n", mStGet.iRndis);
	    	Ret = write(fd, value, count);
			SLOGE("OMADM RNDIS write  Ret=%d  count=%d", Ret, count);
	    	close(fd);
		*/
		} else {//usb disable
	
#if 0
			count = snprintf(value, sizeof(value), "%d\n", 0);

			if ((fd = open("/sys/class/usb_composite/rndis/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite adb enable");
				//exit(1);
	    	}
			SLOGD("OMADM USB write  Ret=%d  count=%d", Ret, count);
	    	close(fd);
			
			if ((fd = open("/sys/class/usb_composite/usb_mass_storage/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite enable");
				//exit(1);
	    	}
			Ret = write(fd, value, count);
			SLOGD("OMADM USB write  Ret=%d  count=%d", Ret, count);
	    	close(fd);

			if ((fd = open("/sys/class/usb_composite/adb/enable", O_WRONLY)) < 0) {
				SLOGE("Unable to open usb_composite adb enable");
				//exit(1);
	    	}
			SLOGD("OMADM USB write  Ret=%d  count=%d", Ret, count);
	    	close(fd);
#else
    		if ((fd = open("/sys/devices/platform/mt_usb/cmode", O_WRONLY)) < 0) {
    			SLOGE("Unable to open /sys/devices/platform/mt_usb/cmode");
    			return -1;
    	    }

    		count = snprintf(value, sizeof(value), "%d\n", mStGet.iIsEnable);
    	    Ret = write(fd, value, count);
    	    close(fd);
    		if (Ret < 0) {
    			SLOGE("Unable to write /sys/devices/platform/mt_usb/cmode");
    			// return -1;
            }
#endif
		}
	}
    //int nvram_fd = NVM_GetFileDesc(file_lid, &rec_size, &rec_num, true);
    //SLOGE("OMADM NVRAM read USB  rec_size=%d, rec_num=%d", rec_size, rec_num);
    //read(nvram_fd, &mStGet, rec_size*rec_num);
	//SLOGE("OMADM NVRAM read USB  mStGet.iIsEnable=%d, mStGet.iUsb=%d, mStGet.iAdb=%d, mStGet.iRndis=%d", mStGet.iIsEnable, mStGet.iUsb, mStGet.iAdb, mStGet.iRndis);
    //NVM_CloseFileDesc(nvram_fd);	
//}M
    SLOGI("Vold 2.1 (the revenge) firing up");

    mkdir("/dev/block/vold", 0755);

    /* Create our singleton managers */
    if (!(vm = VolumeManager::Instance())) {
        SLOGE("Unable to create VolumeManager");
        exit(1);
    };

    if (!(nm = NetlinkManager::Instance())) {
        SLOGE("Unable to create NetlinkManager");
        exit(1);
    };


    cl = new CommandListener();
    vm->setBroadcaster((SocketListener *) cl);
    nm->setBroadcaster((SocketListener *) cl);

    if (vm->start()) {
        SLOGE("Unable to start VolumeManager (%s)", strerror(errno));
        exit(1);
    }

    if (process_config(vm)) {
        SLOGE("Error reading configuration (%s)... continuing anyways", strerror(errno));
    }

    if (nm->start()) {
        SLOGE("Unable to start NetlinkManager (%s)", strerror(errno));
        exit(1);
    }

    coldboot("/sys/block");
//    coldboot("/sys/class/switch");

#if defined(MTK_2SDCARD_SWAP) || defined(MTK_SHARED_SDCARD)
   SLOGE("Sleep 2s to make sure that coldboot() events are handled");
   sleep(2);
#endif

#ifdef MTK_2SDCARD_SWAP
	vm->swap2Sdcard();
#else
	property_set("internal_sd_path", "/mnt/sdcard");
	property_set("external_sd_path", "/mnt/sdcard2");
#endif

#ifdef MTK_SHARED_SDCARD
    vm->setSharedSdState(Volume::State_Mounted);
#endif
    if(is_meta_boot())
    {
    	SLOGD("meta_boot Sleep 2s to make sure that coldboot() events are handled");
   		sleep(2);
       vm->mountallVolumes();
    }  
    /*
     * Now that we're up, we can respond to commands
     */
    if (cl->startListener()) {
        SLOGE("Unable to start CommandListener (%s)", strerror(errno));
        exit(1);
    }

    // Eventually we'll become the monitoring thread
    while(1) {
        sleep(1000);
    }

    SLOGI("Vold exiting");
    exit(0);
}

static void do_coldboot(DIR *d, int lvl)
{
    struct dirent *de;
    int dfd, fd;

    dfd = dirfd(d);

    fd = openat(dfd, "uevent", O_WRONLY);
    if(fd >= 0) {
        write(fd, "add\n", 4);
        close(fd);
    }

    while((de = readdir(d))) {
        DIR *d2;

        if (de->d_name[0] == '.')
            continue;

        if (de->d_type != DT_DIR && lvl > 0)
            continue;

        fd = openat(dfd, de->d_name, O_RDONLY | O_DIRECTORY);
        if(fd < 0)
            continue;

        d2 = fdopendir(fd);
        if(d2 == 0)
            close(fd);
        else {
            do_coldboot(d2, lvl + 1);
            closedir(d2);
        }
    }
}

static void coldboot(const char *path)
{
    DIR *d = opendir(path);
    if(d) {
        do_coldboot(d, 0);
        closedir(d);
    }
}

static int parse_mount_flags(char *mount_flags)
{
    char *save_ptr;
    int flags = 0;

    if (strcasestr(mount_flags, "encryptable")) {
        flags |= VOL_ENCRYPTABLE;
    }

    if (strcasestr(mount_flags, "nonremovable")) {
        flags |= VOL_NONREMOVABLE;
    }

    return flags;
}

static int process_config(VolumeManager *vm) {
    FILE *fp;
    int n = 0;
    char line[255];

#ifdef MTK_EMMC_SUPPORT
    if (!(fp = fopen("/etc/vold.fstab", "r"))) {
		SLOGE("Open config file fail (vold.fstab)");
        return -1;
    }
#else
    if (!(fp = fopen("/etc/vold.fstab.nand", "r"))) {
		SLOGE("Open config file fail (vold.fstab.nand), Try to open (vold.fstab) instead");

		if (!(fp = fopen("/etc/vold.fstab", "r"))) {
			SLOGE("Open config file fail (vold.fstab)");		
			return -1;
		}
    }
#endif


    while(fgets(line, sizeof(line), fp)) {
        const char *delim = " \t";
        char *save_ptr;
        char *type, *label, *mount_point, *mount_flags, *sysfs_path;
        int flags;

        n++;
        line[strlen(line)-1] = '\0';

        if (line[0] == '#' || line[0] == '\0')
            continue;

        if (!(type = strtok_r(line, delim, &save_ptr))) {
            SLOGE("Error parsing type");
            goto out_syntax;
        }
        if (!(label = strtok_r(NULL, delim, &save_ptr))) {
            SLOGE("Error parsing label");
            goto out_syntax;
        }
        if (!(mount_point = strtok_r(NULL, delim, &save_ptr))) {
            SLOGE("Error parsing mount point");
            goto out_syntax;
        }

        if (!strcmp(type, "dev_mount")) {
            DirectVolume *dv = NULL;
            char *part;

            if (!(part = strtok_r(NULL, delim, &save_ptr))) {
                SLOGE("Error parsing partition");
                goto out_syntax;
            }
			
            if (strcmp(part, "auto") && atoi(part) == 0 && strncmp(part, "emmc@", 5) ) {
                SLOGE("Partition must be 'auto', 'emmc@xxx' or 1 based index instead of '%s'", part);
                goto out_syntax;
            }

            if (!strcmp(part, "auto")) {
                dv = new DirectVolume(vm, label, mount_point, -1);
            }
            #ifdef MTK_EMMC_SUPPORT
            else if (!strncmp(part, "emmc@", 5)) {
                   int n = emmc_name_to_number(part + 5);
                   if (n < 0) {
                        LOGD("eMMC: can NOT find FAT partition via name mapping, part=%s.\n", part);

                        #ifdef MTK_SHARED_SDCARD
                            n = -1;
                            LOGD("eMMC: MTK_SHARED_SDCARD is enabled. Even if the partition id is error, still continue to create DirectVolume.\n");
                        #else                         
                          goto out_syntax;
                        #endif
                    }
                    dv = new DirectVolume(vm, label, mount_point, n);

                   #ifdef MTK_SHARED_SDCARD
                     dv->setState(Volume::State_Idle);
                   #endif
            }
            #endif
			else {
                dv = new DirectVolume(vm, label, mount_point, atoi(part));
            }

            while ((sysfs_path = strtok_r(NULL, delim, &save_ptr))) {
                if (*sysfs_path != '/') {
                    /* If the first character is not a '/', it must be flags */
                    break;
                }
                if (dv->addPath(sysfs_path)) {
                    SLOGE("Failed to add devpath %s to volume %s", sysfs_path,
                         label);
                    goto out_fail;
                }
            }

            /* If sysfs_path is non-null at this point, then it contains
             * the optional flags for this volume
             */
            if (sysfs_path)
                flags = parse_mount_flags(sysfs_path);
            else
                flags = 0;
            dv->setFlags(flags);

            vm->addVolume(dv);
        } else if (!strcmp(type, "map_mount")) {
        } else {
            SLOGE("Unknown type '%s'", type);
            goto out_syntax;
        }
    }

    fclose(fp);
    return 0;

out_syntax:
    SLOGE("Syntax error on config line %d", n);
    errno = -EINVAL;
out_fail:
    fclose(fp);
    return -1;   
}
