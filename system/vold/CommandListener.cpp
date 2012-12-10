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

#include <stdlib.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <dirent.h>
#include <errno.h>
#include <fcntl.h>
#include <string.h>

#define LOG_TAG "VoldCmdListener"
#include <cutils/log.h>

#include <sysutils/SocketClient.h>
#include <private/android_filesystem_config.h>

#include "CommandListener.h"
#include "VolumeManager.h"
#include "Bicr.h"
#include "ResponseCode.h"
#include "Process.h"
#include "Xwarp.h"
#include "Loop.h"
#include "Devmapper.h"
#include "cryptfs.h"
#include <cutils/xlog.h>


CommandListener::CommandListener() :
                 FrameworkListener("vold") {
    registerCmd(new DumpCmd());
    registerCmd(new VolumeCmd());
    registerCmd(new AsecCmd());
    registerCmd(new ObbCmd());
    registerCmd(new StorageCmd());
    registerCmd(new XwarpCmd());
    registerCmd(new CryptfsCmd());
    //M{
    registerCmd(new USBCmd());
    registerCmd(new CDROMCmd());
    //}M
}

void CommandListener::dumpArgs(int argc, char **argv, int argObscure) {
    char buffer[4096];
    char *p = buffer;

    memset(buffer, 0, sizeof(buffer));
    int i;
    for (i = 0; i < argc; i++) {
        unsigned int len = strlen(argv[i]) + 1; // Account for space
        if (i == argObscure) {
            len += 2; // Account for {}
        }
        if (((p - buffer) + len) < (sizeof(buffer)-1)) {
            if (i == argObscure) {
                *p++ = '{';
                *p++ = '}';
                *p++ = ' ';
                continue;
            }
            strcpy(p, argv[i]);
            p+= strlen(argv[i]);
            if (i != (argc -1)) {
                *p++ = ' ';
            }
        }
    }
    SLOGD("%s", buffer);
}

CommandListener::DumpCmd::DumpCmd() :
                 VoldCommand("dump") {
}

int CommandListener::DumpCmd::runCommand(SocketClient *cli,
                                         int argc, char **argv) {
    cli->sendMsg(0, "Dumping loop status", false);
    if (Loop::dumpState(cli)) {
        cli->sendMsg(ResponseCode::CommandOkay, "Loop dump failed", true);
    }
    cli->sendMsg(0, "Dumping DM status", false);
    if (Devmapper::dumpState(cli)) {
        cli->sendMsg(ResponseCode::CommandOkay, "Devmapper dump failed", true);
    }
    cli->sendMsg(0, "Dumping mounted filesystems", false);
    FILE *fp = fopen("/proc/mounts", "r");
    if (fp) {
        char line[1024];
        while (fgets(line, sizeof(line), fp)) {
            line[strlen(line)-1] = '\0';
            cli->sendMsg(0, line, false);;
        }
        fclose(fp);
    }

    cli->sendMsg(ResponseCode::CommandOkay, "dump complete", false);
    return 0;
}

//M{
CommandListener::USBCmd::USBCmd() :
				 VoldCommand("USB") {

}

int CommandListener::USBCmd::runCommand(SocketClient *cli, int argc, char **argv) {
	if (argc < 2) {
	        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
	        return 0;
	}

	VolumeManager *vm = VolumeManager::Instance();
	int  rc = 0;
	if(!strcmp(argv[1],"enable")) {
		rc = vm->USBEnable(true);
	} else if(!strcmp(argv[1],"disable")) {
		rc = vm->USBEnable(false);
	} else {
		cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown USB cmd", false);
		return -1;
	}
	if(!rc) {
		cli->sendMsg(ResponseCode::CommandOkay, "USB operation succeeded", false);
	} else {
		rc = ResponseCode::convertFromErrno();
		cli->sendMsg(rc, "USB operation failed", true);
	}
	return 0;
}

// command for Build-in CD-ROM 
CommandListener::CDROMCmd::CDROMCmd() :
				 VoldCommand("cd-rom") {
				 	
}

int CommandListener::CDROMCmd::runCommand(SocketClient *cli, int argc, char **argv) {
	if (argc < 2) {
		SLOGD("CDROMcmd: argc<2 argc=%d", argc);
		cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
		return 0;
	}

	Bicr *bicr = Bicr::Instance();
	int  rc = 0;
	
	if(!strcmp(argv[1],"share")) {
		SLOGD("CDROMcmd: before cd-rom share");
		rc = bicr->shareCdRom();
		SLOGD("CDROMcmd: finish cd-rom share: rc=%d", rc);
	} else if(!strcmp(argv[1],"unshare")) {
		SLOGD("CDROMcmd: before cd-rom unshare");
		rc = bicr->unShareCdRom();
		SLOGD("CDROMcmd: after cd-rom unshare: rc=%d", rc);
	} else if(!strcmp(argv[1],"status")) {
		SLOGD("CDROMcmd: before cd-rom status");
		cli->sendMsg(ResponseCode::CdromStatusResult, bicr->getStatus(), false);	
		SLOGD("CDROMcmd: finish cd-rom status: %s", bicr->getStatus());	
		return 0;	
	} else {
		SLOGD("CDROMcmd: unknown cd-rom cmd: argc=%d, argv[0]=%s, argv[1]=%s", argc, argv[0], argv[1]);
		cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown cd-rom cmd", false);
		return -1;
	}
	
	if(!rc) {
		SLOGD("CDROMcmd: successed");
		cli->sendMsg(ResponseCode::CommandOkay, "cd-rom operation succeeded", false);
	} else {
		SLOGD("CDROMcmd: failed");
		rc = ResponseCode::convertFromErrno();
		cli->sendMsg(rc, "cd-rom operation failed", true);
	}
	
	return 0;
}
//}M
CommandListener::VolumeCmd::VolumeCmd() :
                 VoldCommand("volume") {
}

int CommandListener::VolumeCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    dumpArgs(argc, argv, -1);

    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    VolumeManager *vm = VolumeManager::Instance();
    int rc = 0;

    if (!strcmp(argv[1], "list")) {
        return vm->listVolumes(cli);
    } 
#ifdef MTK_SD_REINIT_SUPPORT
    else if (!strcmp(argv[1], "init_ext_sd")) {
        vm->reinitExternalSD();
    } 
#endif
    else if (!strcmp(argv[1], "ipo")) {
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume ipo shutdown/startup", false);
            return 0;
        }
        if(!strcmp(argv[2], "startup")){
            vm->setIpoState(VolumeManager::State_Ipo_Start);
#ifdef MTK_SD_REINIT_SUPPORT
            vm->reinitExternalSD();
#endif
        }
        else if(!strcmp(argv[2], "shutdown")){
            vm->setIpoState(VolumeManager::State_Ipo_Shutdown);
        }
        else{
            errno = -EINVAL;
            rc = 1;
        }
    }
#ifdef MTK_2SDCARD_SWAP
    else if (!strcmp(argv[1], "is_2sd_swapped")) {
        char *buffer;
		SLOGE("is2SdcardSwapped = %s", (vm->is2SdcardSwapped())? "True" : "False");
        asprintf(&buffer, "%d", vm->is2SdcardSwapped());
        cli->sendMsg(ResponseCode::CommandOkay, buffer, false);
        free(buffer);
                return 0;
    }
#endif	
	else if (!strcmp(argv[1], "debug")) {
        if (argc != 3 || (argc == 3 && (strcmp(argv[2], "off") && strcmp(argv[2], "on")))) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume debug <off/on>", false);
            return 0;
        }
        vm->setDebug(!strcmp(argv[2], "on") ? true : false);
    } else if (!strcmp(argv[1], "mount")) {
#ifndef MTK_2SDCARD_SWAP    
    	if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume mount <path>", false);
            return 0;
        }
	rc = vm->mountVolume(argv[2]);
#elif defined(MTK_SWAP_STATIC_MODE)
	if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume mount <path>", false);
            return 0;
        }
	rc = vm->mountVolume(argv[2]);
#else		
        if (argc < 3 || argc > 4) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume mount <path> [swap]", false);
            return 0;
        }
		SLOGE("[JAM] CommandListener::VolumeCmd::runCommand --> mount");		
		if(argc == 3)
        	rc = vm->mountVolume(argv[2]);
		else {
			if(!strcmp(argv[3], "swap")) {
				vm->setNeedSwapAfterMount(true) ;
				rc = vm->mountVolume(argv[2]);
			}
			else {
				cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume mount <path> [swap]", false);
	            return 0;
			}
		}
#endif		
    } else if (!strcmp(argv[1], "unmount")) {
#ifndef MTK_2SDCARD_SWAP    
        if (argc < 3 || argc > 4 ||
           ((argc == 4 && strcmp(argv[3], "force")) &&
            (argc == 4 && strcmp(argv[3], "force_and_revert")))) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume unmount <path> [force|force_and_revert]", false);
            return 0;
        }

        bool force = false;
        bool revert = false;
        if (argc >= 4 && !strcmp(argv[3], "force")) {
            force = true;
        } else if (argc >= 4 && !strcmp(argv[3], "force_and_revert")) {
            force = true;
            revert = true;
        }
        rc = vm->unmountVolume(argv[2], force, revert);
#elif defined(MTK_SWAP_STATIC_MODE)
	if (argc < 3 || argc > 4 ||
           ((argc == 4 && strcmp(argv[3], "force")) &&
            (argc == 4 && strcmp(argv[3], "force_and_revert")))) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume unmount <path> [force|force_and_revert]", false);
            return 0;
        }

        bool force = false;
        bool revert = false;
        if (argc >= 4 && !strcmp(argv[3], "force")) {
            force = true;
        } else if (argc >= 4 && !strcmp(argv[3], "force_and_revert")) {
            force = true;
            revert = true;
        }
        rc = vm->unmountVolume(argv[2], force, revert);
#else
		SLOGE("[JAM] CommandListener::VolumeCmd::runCommand --> unmount");

        if (argc < 3 || argc > 5 ||
           ((argc >= 4 && strcmp(argv[3], "force")) &&
            (argc >= 4 && strcmp(argv[3], "force_and_revert")) &&
            (argc >= 4 && strcmp(argv[3], "swap")) && 
            (argc >= 4 && strcmp(argv[3], "force_and_swap")) &&
            (argc >= 4 && strcmp(argv[3], "force_and_revert_and_swap"))
           )
        ) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume unmount <path> [force|force_and_revert|swap|force_and_swap|force_and_revert_and_swap]", false);
            return 0;
        }

        bool force = false;
        bool revert = false;
		vm->setNeedSwapAfterUnmount(false) ;
        if (argc >= 4 && !strcmp(argv[3], "force")) {
            force = true;	
        } else if (argc >= 4 && !strcmp(argv[3], "force_and_revert")) {
            force = true;
            revert = true;
        } else if (argc >= 4 && !strcmp(argv[3], "swap")) {
            vm->setNeedSwapAfterUnmount(true) ;
        } else if (argc >= 4 && !strcmp(argv[3], "force_and_swap")) {
            force = true;
            vm->setNeedSwapAfterUnmount(true) ;
        } else if (argc >= 4 && !strcmp(argv[3], "force_and_revert_and_swap")) {
            force = true;
            revert = true;
			vm->setNeedSwapAfterUnmount(true) ;
        }		

        rc = vm->unmountVolume(argv[2], force, revert);
#endif
    } else if (!strcmp(argv[1], "format")) {
    	SLOGE("[JAM] CommandListener::VolumeCmd::runCommand --> format");
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: volume format <path>", false);
            return 0;
        }
        rc = vm->formatVolume(argv[2]);
    } else if (!strcmp(argv[1], "share")) {
    	SLOGE("[JAM] CommandListener::VolumeCmd::runCommand --> share");
        if (argc != 4) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: volume share <path> <method>", false);
            return 0;
        }
        rc = vm->shareVolume(argv[2], argv[3]);
    } else if (!strcmp(argv[1], "unshare")) {
	    SLOGE("[JAM] Command Listener::VolumeCmd::runCommand --> unshare");
        if (argc != 4) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: volume unshare <path> <method>", false);
            return 0;
        }
        rc = vm->unshareVolume(argv[2], argv[3]);
    } else if (!strcmp(argv[1], "shared")) {
   		SLOGE("[JAM] CommandListener::VolumeCmd::runCommand --> shared");
        bool enabled = false;
        if (argc != 4) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: volume shared <path> <method>", false);
            return 0;
        }

        if (vm->shareEnabled(argv[2], argv[3], &enabled)) {
            cli->sendMsg(
                    ResponseCode::OperationFailed, "Failed to determine share enable state", true);
        } else {
            cli->sendMsg(ResponseCode::ShareEnabledResult,
                    (enabled ? "Share enabled" : "Share disabled"), false);
        }
        return 0;
    } else {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown volume cmd", false);
    }

    if (!rc) {
        cli->sendMsg(ResponseCode::CommandOkay, "volume operation succeeded", false);
    } else {
        int erno = errno;
        rc = ResponseCode::convertFromErrno();
        cli->sendMsg(rc, "volume operation failed", true);
    }

    return 0;
}

CommandListener::StorageCmd::StorageCmd() :
                 VoldCommand("storage") {
}

int CommandListener::StorageCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    dumpArgs(argc, argv, -1);

    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    if (!strcmp(argv[1], "users")) {
        DIR *dir;
        struct dirent *de;

        if (!(dir = opendir("/proc"))) {
            cli->sendMsg(ResponseCode::OperationFailed, "Failed to open /proc", true);
            return 0;
        }

        while ((de = readdir(dir))) {
            int pid = Process::getPid(de->d_name);

            if (pid < 0) {
                continue;
            }

            char processName[255];
            Process::getProcessName(pid, processName, sizeof(processName));

            char openfile[PATH_MAX];

		char path[PATH_MAX];
            bool isAccess = true;
            strcpy(path, argv[2]);

             if (Process::checkFileDescriptorSymLinks(pid, path, openfile, sizeof(openfile))) {
                SLOGE("Process %s (%d) has open file %s", processName, pid, openfile);
            } else if (Process::checkFileMaps(pid, path, openfile, sizeof(openfile))) {
                SLOGE("Process %s (%d) has open filemap for %s", processName, pid, openfile);
            } else if (Process::checkSymLink(pid, path, "cwd")) {
                SLOGE("Process %s (%d) has cwd within %s", processName, pid, path);
            } else if (Process::checkSymLink(pid, path, "root")) {
                SLOGE("Process %s (%d) has chroot within %s", processName, pid, path);
            } else if (Process::checkSymLink(pid, path, "exe")) {
                SLOGE("Process %s (%d) has executable path within %s", processName, pid, path);
            } else {
                isAccess = false;
            }

            if (isAccess) {
            
                char msg[1024];
                snprintf(msg, sizeof(msg), "%d %s", pid, processName);
                cli->sendMsg(ResponseCode::StorageUsersListResult, msg, false);
            }
        }
        closedir(dir);
        cli->sendMsg(ResponseCode::CommandOkay, "Storage user list complete", false);
    } else {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown storage cmd", false);
    }
    return 0;
}

CommandListener::AsecCmd::AsecCmd() :
                 VoldCommand("asec") {
}

int CommandListener::AsecCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    VolumeManager *vm = VolumeManager::Instance();
    int rc = 0;

    if (!strcmp(argv[1], "list")) {
        dumpArgs(argc, argv, -1);

        if (vm->mRetrunEmptyContainersForOnce) {
            SLOGD("asec list: skip this request once. Return an empty Containter list"); 
            vm->mRetrunEmptyContainersForOnce = false;
        }
        else if(!vm->listBackupAsec(cli)){
           SLOGD("asec list: use backup Asec list");
        }
        else {
            DIR *d = opendir(Volume::SEC_ASECDIR);

            if (!d) {
                cli->sendMsg(ResponseCode::OperationFailed, "Failed to open asec dir", true);
                return 0;
            }

            struct dirent *dent;
            while ((dent = readdir(d))) {
                if (dent->d_name[0] == '.')
                    continue;
                if (!strcmp(&dent->d_name[strlen(dent->d_name)-5], ".asec")) {
                    char id[255];
                    memset(id, 0, sizeof(id));
                    strncpy(id, dent->d_name, strlen(dent->d_name) -5);
                    cli->sendMsg(ResponseCode::AsecListResult, id, false);
                }
            }
            closedir(d);
        }
    } else if (!strcmp(argv[1], "create")) {
        dumpArgs(argc, argv, 5);
        if (argc != 7) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: asec create <container-id> <size_mb> <fstype> <key> <ownerUid>", false);
            return 0;
        }

        unsigned int numSectors = (atoi(argv[3]) * (1024 * 1024)) / 512;
        rc = vm->createAsec(argv[2], numSectors, argv[4], argv[5], atoi(argv[6]));
    } else if (!strcmp(argv[1], "finalize")) {
        dumpArgs(argc, argv, -1);
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: asec finalize <container-id>", false);
            return 0;
        }
        rc = vm->finalizeAsec(argv[2]);
    } else if (!strcmp(argv[1], "destroy")) {
        dumpArgs(argc, argv, -1);
        if (argc < 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: asec destroy <container-id> [force]", false);
            return 0;
        }
        bool force = false;
        if (argc > 3 && !strcmp(argv[3], "force")) {
            force = true;
        }
        rc = vm->destroyAsec(argv[2], force);
    } else if (!strcmp(argv[1], "mount")) {
        dumpArgs(argc, argv, 3);
        if (argc != 5) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: asec mount <namespace-id> <key> <ownerUid>", false);
            return 0;
        }
        rc = vm->mountAsec(argv[2], argv[3], atoi(argv[4]));
    } else if (!strcmp(argv[1], "unmount")) {
        dumpArgs(argc, argv, -1);
        if (argc < 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: asec unmount <container-id> [force]", false);
            return 0;
        }
        bool force = false;
        if (argc > 3 && !strcmp(argv[3], "force")) {
            force = true;
        }
        rc = vm->unmountAsec(argv[2], force);
    } else if (!strcmp(argv[1], "rename")) {
        dumpArgs(argc, argv, -1);
        if (argc != 4) {
            cli->sendMsg(ResponseCode::CommandSyntaxError,
                    "Usage: asec rename <old_id> <new_id>", false);
            return 0;
        }
        rc = vm->renameAsec(argv[2], argv[3]);
    } else if (!strcmp(argv[1], "path")) {
        dumpArgs(argc, argv, -1);
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: asec path <container-id>", false);
            return 0;
        }
        char path[255];

        if (!(rc = vm->getAsecMountPath(argv[2], path, sizeof(path)))) {
            cli->sendMsg(ResponseCode::AsecPathResult, path, false);
            return 0;
        }
    } else if (!strcmp(argv[1], "fspath")) {
        dumpArgs(argc, argv, -1);
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: asec fspath <container-id>", false);
            return 0;
        }
        char path[255];

        if (!(rc = vm->getAsecFilesystemPath(argv[2], path, sizeof(path)))) {
            cli->sendMsg(ResponseCode::AsecPathResult, path, false);
            return 0;
        }
    } else {
        dumpArgs(argc, argv, -1);
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown asec cmd", false);
    }

    if (!rc) {
        cli->sendMsg(ResponseCode::CommandOkay, "asec operation succeeded", false);
    } else {
        rc = ResponseCode::convertFromErrno();
        cli->sendMsg(rc, "asec operation failed", true);
    }

    return 0;
}

CommandListener::ObbCmd::ObbCmd() :
                 VoldCommand("obb") {
}

int CommandListener::ObbCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    VolumeManager *vm = VolumeManager::Instance();
    int rc = 0;

    if (!strcmp(argv[1], "list")) {
        dumpArgs(argc, argv, -1);

        rc = vm->listMountedObbs(cli);
    } else if (!strcmp(argv[1], "mount")) {
            dumpArgs(argc, argv, 3);
            if (argc != 5) {
                cli->sendMsg(ResponseCode::CommandSyntaxError,
                        "Usage: obb mount <filename> <key> <ownerUid>", false);
                return 0;
            }
            rc = vm->mountObb(argv[2], argv[3], atoi(argv[4]));
    } else if (!strcmp(argv[1], "unmount")) {
        dumpArgs(argc, argv, -1);
        if (argc < 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: obb unmount <source file> [force]", false);
            return 0;
        }
        bool force = false;
        if (argc > 3 && !strcmp(argv[3], "force")) {
            force = true;
        }
        rc = vm->unmountObb(argv[2], force);
    } else if (!strcmp(argv[1], "path")) {
        dumpArgs(argc, argv, -1);
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: obb path <source file>", false);
            return 0;
        }
        char path[255];

        if (!(rc = vm->getObbMountPath(argv[2], path, sizeof(path)))) {
            cli->sendMsg(ResponseCode::AsecPathResult, path, false);
            return 0;
        }
    } else {
        dumpArgs(argc, argv, -1);
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown obb cmd", false);
    }

    if (!rc) {
        cli->sendMsg(ResponseCode::CommandOkay, "obb operation succeeded", false);
    } else {
        rc = ResponseCode::convertFromErrno();
        cli->sendMsg(rc, "obb operation failed", true);
    }

    return 0;
}

CommandListener::XwarpCmd::XwarpCmd() :
                 VoldCommand("xwarp") {
}

int CommandListener::XwarpCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    if (!strcmp(argv[1], "enable")) {
        if (Xwarp::enable()) {
            cli->sendMsg(ResponseCode::OperationFailed, "Failed to enable xwarp", true);
            return 0;
        }

        cli->sendMsg(ResponseCode::CommandOkay, "Xwarp mirroring started", false);
    } else if (!strcmp(argv[1], "disable")) {
        if (Xwarp::disable()) {
            cli->sendMsg(ResponseCode::OperationFailed, "Failed to disable xwarp", true);
            return 0;
        }

        cli->sendMsg(ResponseCode::CommandOkay, "Xwarp disabled", false);
    } else if (!strcmp(argv[1], "status")) {
        char msg[255];
        bool r;
        unsigned mirrorPos, maxSize;

        if (Xwarp::status(&r, &mirrorPos, &maxSize)) {
            cli->sendMsg(ResponseCode::OperationFailed, "Failed to get xwarp status", true);
            return 0;
        }
        snprintf(msg, sizeof(msg), "%s %u %u", (r ? "ready" : "not-ready"), mirrorPos, maxSize);
        cli->sendMsg(ResponseCode::XwarpStatusResult, msg, false);
    } else {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown storage cmd", false);
    }

    return 0;
}

CommandListener::CryptfsCmd::CryptfsCmd() :
                 VoldCommand("cryptfs") {
}

int CommandListener::CryptfsCmd::runCommand(SocketClient *cli,
                                                      int argc, char **argv) {
    if ((cli->getUid() != 0) && (cli->getUid() != AID_SYSTEM)) {
        cli->sendMsg(ResponseCode::CommandNoPermission, "No permission to run cryptfs commands", false);
        return 0;
    }

    if (argc < 2) {
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Missing Argument", false);
        return 0;
    }

    int rc = 0;

    if (!strcmp(argv[1], "checkpw")) {
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs checkpw <passwd>", false);
            return 0;
        }
        dumpArgs(argc, argv, 2);
        rc = cryptfs_check_passwd(argv[2]);
    } 
    else if (!strcmp(argv[1], "ipo_reboot")) {
        dumpArgs(argc, argv, -1);
        rc = cryptfs_restart(true, atoi(argv[2]));
    }
    else if (!strcmp(argv[1], "restart")) {
        if (argc != 2) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs restart", false);
            return 0;
        }
        dumpArgs(argc, argv, -1);
        rc = cryptfs_restart();
    } else if (!strcmp(argv[1], "cryptocomplete")) {
        if (argc != 2) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs cryptocomplete", false);
            return 0;
        }
        dumpArgs(argc, argv, -1);
        rc = cryptfs_crypto_complete();
    } else if (!strcmp(argv[1], "enablecrypto")) {
        if ( (argc != 4) || (strcmp(argv[2], "wipe") && strcmp(argv[2], "inplace")) ) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs enablecrypto <wipe|inplace> <passwd>", false);
            return 0;
        }
        dumpArgs(argc, argv, 3);
        rc = cryptfs_enable(argv[2], argv[3]);
    } else if (!strcmp(argv[1], "changepw")) {
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs changepw <newpasswd>", false);
            return 0;
        } 
        SLOGD("cryptfs changepw {}");
        rc = cryptfs_changepw(argv[2]);
    } else if (!strcmp(argv[1], "verifypw")) {
        if (argc != 3) {
            cli->sendMsg(ResponseCode::CommandSyntaxError, "Usage: cryptfs verifypw <passwd>", false);
            return 0;
        }
        SLOGD("cryptfs verifypw {}");
        rc = cryptfs_verify_passwd(argv[2]);
    } else {
        dumpArgs(argc, argv, -1);
        cli->sendMsg(ResponseCode::CommandSyntaxError, "Unknown cryptfs cmd", false);
    }

    // Always report that the command succeeded and return the error code.
    // The caller will check the return value to see what the error was.
    char msg[255];
    snprintf(msg, sizeof(msg), "%d", rc);
    cli->sendMsg(ResponseCode::CommandOkay, msg, false);

    return 0;
}

