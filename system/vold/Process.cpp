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
#include <unistd.h>
#include <errno.h>
#include <string.h>
#include <fcntl.h>
#include <dirent.h>
#include <ctype.h>
#include <pwd.h>
#include <stdlib.h>
#include <poll.h>
#include <sys/stat.h>
#include <signal.h>

#define LOG_TAG "ProcessKiller"
#include <cutils/log.h>
#include <cutils/xlog.h>

#include "Process.h"

int Process::readSymLink(const char *path, char *link, size_t max) {
    struct stat s;
    int length;

    if (lstat(path, &s) < 0)
        return 0;
    if ((s.st_mode & S_IFMT) != S_IFLNK)
        return 0;
   
    // we have a symlink    
    length = readlink(path, link, max- 1);
    if (length <= 0) 
        return 0;
    link[length] = 0;
    return 1;
}

int Process::pathMatchesMountPoint(const char* path, const char* mountPoint) {
    int length = strlen(mountPoint);
    if (length > 1 && strncmp(path, mountPoint, length) == 0) {
        // we need to do extra checking if mountPoint does not end in a '/'
        if (mountPoint[length - 1] == '/')
            return 1;
        // if mountPoint does not have a trailing slash, we need to make sure
        // there is one in the path to avoid partial matches.
        return (path[length] == 0 || path[length] == '/');
    }
    
    return 0;
}

void Process::getProcessName(int pid, char *buffer, size_t max) {
    int fd;
    snprintf(buffer, max, "/proc/%d/cmdline", pid);
    fd = open(buffer, O_RDONLY);
    if (fd < 0) {
        strcpy(buffer, "???");
    } else {
        int length = read(fd, buffer, max - 1);
        buffer[length] = 0;
        close(fd);
    }
}

int Process::checkFileDescriptorSymLinks(int pid, const char *mountPoint) {
    return checkFileDescriptorSymLinks(pid, mountPoint, NULL, 0);
}

int Process::checkFileDescriptorSymLinks(int pid, const char *mountPoint, char *openFilename, size_t max) {


    // compute path to process's directory of open files
    char    path[PATH_MAX];
    sprintf(path, "/proc/%d/fd", pid);
    DIR *dir = opendir(path);
    if (!dir)
        return 0;

    // remember length of the path
    int parent_length = strlen(path);
    // append a trailing '/'
    path[parent_length++] = '/';

    struct dirent* de;
    while ((de = readdir(dir))) {
        if (!strcmp(de->d_name, ".") || !strcmp(de->d_name, "..")
                || strlen(de->d_name) + parent_length + 1 >= PATH_MAX)
            continue;
        
        // append the file name, after truncating to parent directory
        path[parent_length] = 0;
        strcat(path, de->d_name);

        char link[PATH_MAX];

        if (readSymLink(path, link, sizeof(link)) && pathMatchesMountPoint(link, mountPoint)) {
            if (openFilename) {
                memset(openFilename, 0, max);
                strncpy(openFilename, link, max-1);
            }
            closedir(dir);
            return 1;
        }
    }

    closedir(dir);
    return 0;
}

int Process::checkFileMaps(int pid, const char *mountPoint) {
    return checkFileMaps(pid, mountPoint, NULL, 0);
}

int Process::checkFileMaps(int pid, const char *mountPoint, char *openFilename, size_t max) {
    FILE *file;
    char buffer[PATH_MAX + 100];

    sprintf(buffer, "/proc/%d/maps", pid);
    file = fopen(buffer, "r");
    if (!file)
        return 0;
    
    while (fgets(buffer, sizeof(buffer), file)) {
        // skip to the path
        const char* path = strchr(buffer, '/');
        if (path && pathMatchesMountPoint(path, mountPoint)) {
            if (openFilename) {
                memset(openFilename, 0, max);
                strncpy(openFilename, path, max-1);
            }
            fclose(file);
            return 1;
        }
    }
    
    fclose(file);
    return 0;
}

int Process::checkSymLink(int pid, const char *mountPoint, const char *name) {
    char    path[PATH_MAX];
    char    link[PATH_MAX];

    sprintf(path, "/proc/%d/%s", pid, name);
    if (readSymLink(path, link, sizeof(link)) && pathMatchesMountPoint(link, mountPoint)) 
        return 1;
    return 0;
}

int Process::getPid(const char *s) {
    int result = 0;
    while (*s) {
        if (!isdigit(*s)) return -1;
        result = 10 * result + (*s++ - '0');
    }
    return result;
}

void CheckChildThread(int pid,const char *path)
{
	char thread_path[PATH_MAX];
	sprintf(thread_path, "/proc/%d/task", pid);
	DIR *thread_dir = opendir(thread_path);
	FILE *thread_file;
	char buffer_1[PATH_MAX];
	char buffer_2[PATH_MAX];
	int i_found=0;
	int i_break=0;
	struct dirent* thread_de;
	char openfile[PATH_MAX];
		
	if (!thread_dir)
		return;

	while ((thread_de = readdir(thread_dir))) 
		{
			if (atoi(thread_de->d_name))
				{		
 					if (pid==atoi(thread_de->d_name)) //parent
 							continue;
					i_found = 0;
					
					if (Process::checkFileDescriptorSymLinks(atoi(thread_de->d_name), path, openfile, sizeof(openfile))) 
						{
							i_found = 1;
							SLOGE("Thread (%d) has open file: %s", atoi(thread_de->d_name), openfile);
						}
					else if (Process::checkFileMaps(atoi(thread_de->d_name), path, openfile, sizeof(openfile))) 
						{
							i_found = 1;
							SLOGE("Thread (%d) has filemap of %s ", atoi(thread_de->d_name), openfile);
						} 
					else if (Process::checkSymLink(atoi(thread_de->d_name), path, "cwd")) 
						{
							i_found = 1;
							SLOGE("Thread (%d) has cwd within %s", atoi(thread_de->d_name), path);
						} 
					else if (Process::checkSymLink(atoi(thread_de->d_name), path, "root")) 
						{
							i_found = 1;
							SLOGE("Thread (%d) has chroot within %s", atoi(thread_de->d_name), path);
						} 
					else if (Process::checkSymLink(atoi(thread_de->d_name), path, "exe")) 
						{
							i_found = 1;
							SLOGE("Thread (%d) has executable path within %s", atoi(thread_de->d_name), path);
						} 
					
					if (i_found)
						{
							//get process name & status
							sprintf(buffer_1, "/proc/%d/status", atoi(thread_de->d_name));
							thread_file = fopen(buffer_1, "r");

							if (!thread_file)
								continue;
	
							fgets(buffer_2, sizeof(buffer_2), thread_file);
							fgets(buffer_1, sizeof(buffer_1), thread_file);
	
							fclose(thread_file);
									
							SLOGE("%s;	%s",strtok(buffer_2,"\n"),strtok(buffer_1,"\n"));

							//print backtrace
							sprintf(buffer_1, "/proc/%d/stack", atoi(thread_de->d_name));
							thread_file = fopen(buffer_1, "r");

							if (!thread_file)
								continue;

							i_break = 0;
							while(!feof(thread_file))
								{
									i_break = i_break + 1;
									if (i_break>100)
										break;
									fgets(buffer_2, sizeof(buffer_2), thread_file);
									if (!(strcmp("[<ffffffff>]",strtok(buffer_2," "))))
										break;
									else
										SLOGE("%s",buffer_2);
									
								}
							fclose(thread_file);
							
						}
				}
		}		
	closedir(thread_dir);
}

int IsProcessInZombieState(int pid)
{
	char buffer_file[PATH_MAX];
  FILE *process_file;
  sprintf(buffer_file, "/proc/%d/status", pid);
	process_file = fopen(buffer_file, "r");
						  
	if (!process_file)
		return 0;
								
	fgets(buffer_file, sizeof(buffer_file), process_file);
	fgets(buffer_file, sizeof(buffer_file), process_file);
	
	fclose(process_file);
	
	if (strrchr(strtok(buffer_file,"\n"),'Z')) //Z (zombie)
	{
		SLOGE("Process:%d   %s  ",pid,buffer_file);	
		return 1;
	}
	else
		return 0;	
}
/*
 * Hunt down processes that have files open at the given mount point.
 * action = 0 to just warn,
 * action = 1 to SIGHUP,
 * action = 2 to SIGKILL
 */
// hunt down and kill processes that have files open on the given mount point

#define USB_DEVICE_PATH "/sys/class/android_usb/android0/enable"

void Process::killProcessesWithOpenFiles(const char *path, int action) {
    DIR*    dir;
    struct dirent* de;
    static int i_found_parent=0;

    if (!(dir = opendir("/proc"))) {
        SLOGE("opendir failed (%s)", strerror(errno));
        return;
    }
		if (action==1)
			i_found_parent = 0;

    while ((de = readdir(dir))) {
        int killed = 0;
        int pid = getPid(de->d_name);
        char name[PATH_MAX];

        if (pid == -1)
            continue;
        getProcessName(pid, name, sizeof(name));

        char openfile[PATH_MAX];

		
        if (checkFileDescriptorSymLinks(pid, path, openfile, sizeof(openfile))) {
            SLOGE("Process %s (%d) has open file %s", name, pid, openfile);
        } else if (checkFileMaps(pid, path, openfile, sizeof(openfile))) {
            SLOGE("Process %s (%d) has open filemap for %s", name, pid, openfile);
        } else if (checkSymLink(pid, path, "cwd")) {
            SLOGE("Process %s (%d) has cwd within %s", name, pid, path);
        } else if (checkSymLink(pid, path, "root")) {
            SLOGE("Process %s (%d) has chroot within %s", name, pid, path);
        } else if (checkSymLink(pid, path, "exe")) {
            SLOGE("Process %s (%d) has executable path within %s", name, pid, path);
        } else {
        		if ((action == 2) && (!i_found_parent)) 
        		{
        				if(IsProcessInZombieState(pid))
								{
										CheckChildThread(pid,path);
								}
        	  }
            continue;
        }

        if(!strcmp(name, "/sbin/adbd"))     {
            int fd;
            size_t s;

            fd = open(USB_DEVICE_PATH, O_WRONLY);
            if (fd >= 0) {
              xlog_printf(ANDROID_LOG_INFO, LOG_TAG, "enable=0");
              write(fd, "0", 1);
              close(fd);
            } else {
              xlog_printf(ANDROID_LOG_INFO, LOG_TAG, "Fail to open %s", USB_DEVICE_PATH);
            }
        }

        if (action == 1) {
            SLOGW("Sending SIGTERM to process %d", pid);
            CheckChildThread(pid,path);
            kill(pid, SIGTERM);
            i_found_parent = 1;
        } else if (action == 2) {
            SLOGE("Sending SIGKILL to process %d", pid);
            kill(pid, SIGKILL);
        }

        if(!strcmp(name, "/sbin/adbd"))     {
            int fd;
            size_t s;

            fd = open(USB_DEVICE_PATH, O_WRONLY);
            if (fd >= 0) {
              xlog_printf(ANDROID_LOG_INFO, LOG_TAG, "enable=1");
              write(fd, "1", 1);
              close(fd);
            } else {
              xlog_printf(ANDROID_LOG_INFO, LOG_TAG, "Fail to open %s", USB_DEVICE_PATH);
            }
        }
    }
    closedir(dir);
}
