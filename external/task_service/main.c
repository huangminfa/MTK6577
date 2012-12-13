/*
 * Copyright (C) 2009 The Android Open Source Project
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
#include <stdarg.h>
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/poll.h>
#include <sys/wait.h>
#include <netdb.h>
#include <signal.h>
#include <unistd.h>
#include <fcntl.h>
#include <time.h>

#include <android/log.h>
#include <cutils/sockets.h>

#include "main.h"
#define LOG_TAG "task_service"

#define printf(...) \
__android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)

#define LINE_LEN 300

void runCmd(char* cmd, char *buffer, int control)
{
	char *p;
	FILE *fp;
	int read_len;
	int send_len;
	fp = popen(cmd,"r");
	printf("popened fp=%x,buffer=%x",fp,buffer);
	while(fgets(buffer,LINE_LEN,fp)>0)
	{
		printf("fgetsed");
		p=buffer;
		read_len=strlen(buffer);
		send(control,&read_len,4,0);
		printf("%d",read_len);
		printf("%s",buffer);
		if(control>0)
		{
			while(read_len>0)
			{
				send_len = send(control,p,read_len,0);
				printf("%d",send_len);
				read_len-=send_len;
				p+=send_len;
			}
		}
	}
	pclose(fp);
	printf("pclose");

}

int main(int argc, char **argv)
{
	static char *args;
	int control;
	int s,i;
	unsigned char length=0;
	char *buffer;
	int fork_res;
	buffer = malloc(LINE_LEN);

	if ((s = android_get_control_socket("task")) == -1) {
	printf("get socket error");
		return -1;
	}
	printf("Waiting for control socket");
	if (listen(s, 1) == -1 ) {
		printf("Cannot get control socket");
		exit(SYSTEM_ERROR);
	}
	while(1){
		control = accept(s,NULL,0);
		if(control > 0){
			printf("accept");
			for(i=0;i<256;i++){
				if (recv(control, &length, 1, 0) != 1) {
					printf("Cannot get argument length");
					break;
				}
				printf("length %d",length);
				if (length == 0xFF) {
					break;
				} else {
					int offset = 0;
					args = malloc(length + 1);
					while (offset < length) {
						int n = recv(control, &args[offset], length - offset, 0);
						if (n > 0) {
							offset += n;
						} else {
							printf("Cannot get argument value");
							exit(SYSTEM_ERROR);
						}
					}
					args[length] = 0;
					printf("cmd = %s",args);
					runCmd(args,buffer,control);
					free(args);
				}
			}
			close(control);
			printf("close socket");
		}
	}
	free(buffer);
	return 0;
}
