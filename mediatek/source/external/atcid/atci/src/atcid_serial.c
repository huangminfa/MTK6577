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

#include "atcid.h"
#include "atcid_serial.h"
#include "atcid_cmd_dispatch.h"
#include "atcid_util.h"
#include <string.h>
#include <sys/socket.h>
#include <cutils/sockets.h>
#include <netinet/in.h>
#include <termios.h>
#include "anaPowerKey.h"


#define MAX_FAILURE_RETRY 3

#define MAX(a,b) ((a)>(b)?(a):(b))

int android_log_lvl_convert[8]= {ANDROID_LOG_SILENT,    /*8*/
                                 ANDROID_LOG_SILENT,     /*7*/
                                 ANDROID_LOG_FATAL,      /*6*/
                                 ANDROID_LOG_ERROR,      /*5*/
                                 ANDROID_LOG_WARN,       /*4*/
                                 ANDROID_LOG_INFO,       /*3*/
                                 ANDROID_LOG_INFO,       /*2*/
                                 ANDROID_LOG_DEBUG
                                };     /*1*/

/*misc global vars */
extern Serial serial;

int s_fdRild_command = INVALIDE_SOCKET_FD;
int s_fdService_command = INVALIDE_SOCKET_FD;
int s_fdAudio_command = INVALIDE_SOCKET_FD;

/**
 * Returns a pointer to the end of the next line
 *
 * returns NULL if there is no complete line
 */
static char * findNextEOL(char *cur)
{

    // Find next newline
    while (*cur != '\0' && *cur != '\r' && *cur != '\n') cur++;

    return *cur == '\0' ? NULL : cur;
}

int sendDataToRild(char* line) {
    int sendLen = 0, len = 0;
    LOGATCI(LOG_DEBUG,"Enter");

    len = strlen(line);
    sendLen = send(s_fdRild_command, line, len, 0);
    if (sendLen != len) {
        LOGATCI(LOG_ERR, "lose data when send to ril. errno = %d", errno);
    }

    return 0;
}

void writeDataToserialByResponseType(ATRESPONSE_t response_type) {
    char response[MAX_AT_RESPONSE];
    int i = 0;

    memset(response, 0, sizeof(response));

    response[i++] = AT_CR;
    response[i++] = AT_LF;

    switch(response_type) {
    case AT_OK:
        memcpy(response+i, AT_OK_STRING, strlen(AT_OK_STRING));
        i+=strlen(AT_OK_STRING);
        break;
    case AT_CONNECT:
        break;
    case AT_ERROR:
        memcpy(response+i, AT_ERROR_STRING, strlen(AT_ERROR_STRING));
        i+=strlen(AT_ERROR_STRING);
        break;
    case AT_NOT_IMPL:
        memcpy(response+i, AT_NOT_IMPL_STRING, strlen(AT_NOT_IMPL_STRING));
        i+=strlen(AT_NOT_IMPL_STRING);
        break;
    default:
        memcpy(response+i, AT_UNKNOWN_STRING, strlen(AT_UNKNOWN_STRING));
        i+=strlen(AT_UNKNOWN_STRING);
        break;
    }
    response[i++] = AT_CR;
    response[i++] = AT_LF;

    writeDataToSerial(response, strlen(response));
}

void writeDataToSerial(char* input, int length) {
    //int len = 0, c = 0;

    //len = strlen(input);
    
    //if (strncmp(input, "MM+EAPS=0", 9)==0 || strncmp(input, "MM+EADP=0", 9)==0) {
    //    len = MAX_DATA_SIZE;
    //    LOGATCI(LOG_WARNING, "The data should be sent is large  length = %d", len);
    //}

    //LOGATCI(LOG_DEBUG,"Enter with data:%s:%d", input, len);
    //c = write(serial.fd[serial.currDevice], input, len);
    //if (len != c) {
    //    LOGATCI(LOG_WARNING, "Couldn't write all data to the serial port. Wrote only %d bytes with %d", c, errno);
    //}
    int c = 0;
    LOGATCI(LOG_DEBUG,"Enter with data:%s:%d", input, length);
    c = write(serial.fd[serial.currDevice], input, length);
    if (length != c) {
        LOGATCI(LOG_WARNING, "Couldn't write all data to the serial port. Wrote only %d bytes with %d", c, errno);
    }

    return;
}

int sendDataToGenericService(char* line) {
    int sendLen = 0, len = 0;
    LOGATCI(LOG_DEBUG,"Enter");

    len = strlen(line);
    sendLen = send(s_fdService_command, line, len, 0);
    if (sendLen != len) {
        LOGATCI(LOG_ERR, "lose data when send to generic service. errno = %d", errno);
    }

    return 0;
}

int sendDataToAudio(char* line, int len) {
	int sendLen = 0;
    LOGATCI(LOG_DEBUG,"Enter");

    //len = strlen(line);
    LOGATCI(LOG_WARNING, "sendDataToAudio - the data size of send to audio is: %d", len);
    sendLen = send(s_fdAudio_command, line, len, 0);
    if (sendLen != len) {
        LOGATCI(LOG_ERR, "lose data when send to audio. errno = %d", errno);
    }

    return 0;
}

void readDataFromTargetWithResult(ATCI_DataType dataType, int* s_fd_listen, char* buffer){
	int recvLen = 0;
	int strLen = 0;
	int retLen = 0;
	int len;
	char* data;
	int response = -1;

	LOGATCI(LOG_DEBUG,"Enter");
	LOGATCI(LOG_INFO,"Read data from target:%d with fd:%d", dataType, *s_fd_listen);

	memset(buffer, 0, sizeof(buffer));

	do {
		recvLen = recv(*s_fd_listen, buffer, MAX_DATA_SIZE, 0);
		if (recvLen == -1) {
			if(errno == EAGAIN || errno == EINTR) {
				continue;
			}
			LOGATCI(LOG_ERR, "fail to receive data from target socket. errno = %d", errno);
			close(*s_fd_listen);
			*s_fd_listen = INVALIDE_SOCKET_FD;
			return;
		} else if(recvLen == 0) {
			LOGATCI(LOG_ERR, "The peer has performed an orderly shutdown");
			close(*s_fd_listen);
			*s_fd_listen = INVALIDE_SOCKET_FD;
			return;
		}
	} while(recvLen <= 0);

	LOGATCI(LOG_INFO, "data receive from %d is %s, data length is %d", dataType, buffer, recvLen);
    writeDataToSerial(buffer, recvLen);
}

void readDataFromTarget(ATCI_DataType dataType, int* s_fd_listen)
{
    int recvLen = 0;
    int strLen = 0;
    int retLen = 0;
    int len;
    char buffer[MAX_DATA_SIZE];
    char* data;
    int response = -1;

    LOGATCI(LOG_DEBUG,"Enter");
    LOGATCI(LOG_INFO,"Read data from target:%d with fd:%d", dataType, *s_fd_listen);

    memset(buffer, 0, sizeof(buffer));

    do {
        recvLen = recv(*s_fd_listen, buffer, MAX_DATA_SIZE, 0);
        if (recvLen == -1) {
            if(errno == EAGAIN || errno == EINTR) {
                continue;
            }
            LOGATCI(LOG_ERR, "fail to receive data from target socket. errno = %d", errno);
            close(*s_fd_listen);
            *s_fd_listen = INVALIDE_SOCKET_FD;
            return;
        } else if(recvLen == 0) {
            LOGATCI(LOG_ERR, "The peer has performed an orderly shutdown");
            close(*s_fd_listen);
            *s_fd_listen = INVALIDE_SOCKET_FD;
            return;
        }
    } while(recvLen <= 0);

    LOGATCI(LOG_INFO, "data receive from %d is %s, data length is %d", dataType, buffer, recvLen);
    writeDataToSerial(buffer, recvLen);
}

void accessSpecialAtCommand(ATCI_DataType dataType, int* s_fd_listen)
{
    int recvLen = 0;
    int strLen = 0;
    int retLen = 0;
    int len;
    char buffer[MAX_DATA_SIZE];
    char* data;
    int response = -1;

    LOGATCI(LOG_DEBUG,"Enter");
    LOGATCI(LOG_INFO,"Read data from target:%d with fd:%d", dataType, *s_fd_listen);

    memset(buffer, 0, sizeof(buffer));

    do {
        recvLen = recv(*s_fd_listen, buffer, MAX_DATA_SIZE, 0);
        if (recvLen == -1) {
            if(errno == EAGAIN || errno == EINTR) {
                continue;
            }
            LOGATCI(LOG_ERR, "fail to receive data from target socket. errno = %d", errno);
            close(*s_fd_listen);
            *s_fd_listen = INVALIDE_SOCKET_FD;
            return;
        } else if(recvLen == 0) {
            LOGATCI(LOG_ERR, "The peer has performed an orderly shutdown");
            close(*s_fd_listen);
            *s_fd_listen = INVALIDE_SOCKET_FD;
            return;
        }
    } while(recvLen <= 0);

    LOGATCI(LOG_INFO, "data receive from %d is %s, data length is %d", dataType, buffer, recvLen);

    if(regcomp(buffer, "*OK*", 0) == 0) {
        memset(buffer, 0, sizeof(buffer));
        response = pressPowerKey();
        sprintf(buffer, "%d\r\nOK\r\n", response);
    }
    else {
        memset(buffer, 0, sizeof(buffer));
        sprintf(buffer, "ERROR\r\n");
    }
    writeDataToSerial(buffer, strlen(buffer));
}


void connectTarget(ATCI_DataType dataType)
{
    LOGATCI(LOG_DEBUG,"Enter");

    if (RIL_TYPE == dataType) {
        s_fdRild_command = socket_local_client(SOCKET_NAME_RILD,
                                               ANDROID_SOCKET_NAMESPACE_RESERVED,
                                               SOCK_STREAM);

        if (s_fdRild_command < 0) {
            LOGE("fail to open atci-ril socket. errno:%d", errno);
            close(s_fdRild_command);
            return;
        }
    } else if (GENERIC_TYPE == dataType) {
        s_fdService_command = socket_local_client(SOCKET_NAME_GENERIC,
                              ANDROID_SOCKET_NAMESPACE_RESERVED,
                              SOCK_STREAM);

        if (s_fdService_command < 0) {
            LOGE("fail to open atci generic socket. errno:%d", errno);
            close(s_fdService_command);
            return;
        }
    } else if (AUDIO_TYPE == dataType) {
        s_fdAudio_command = socket_local_client(SOCKET_NAME_AUDIO,
                              ANDROID_SOCKET_NAMESPACE_RESERVED,
                              SOCK_STREAM);

        if (s_fdAudio_command < 0) {
            LOGE("fail to open atci audio socket. errno:%d", errno);
            close(s_fdAudio_command);
            return;
        }
    }
}

int serviceReaderLoopWithResult(char* line){
	int ret;
	struct timeval tv;
	fd_set rfds;
	char buffer[MAX_DATA_SIZE];

	LOGATCI(LOG_DEBUG,"Enter");

	if(s_fdService_command != INVALIDE_SOCKET_FD) {

		/*set timeout value to detach abnormal socket event */
		//tv.tv_sec = 5;
		//tv.tv_usec = 0;

		FD_ZERO(&rfds);
		FD_SET(s_fdService_command, &rfds);

		ret = select(s_fdService_command+1, &rfds, NULL, NULL, NULL);
		if (ret < 0) {
			if(errno == EINTR || errno == EAGAIN) {
				return -1;
			}
			LOGATCI(LOG_ERR, "Fail to select in serviceReaderLoop. error: %d", errno);
			close(s_fdService_command);
			s_fdService_command = INVALIDE_SOCKET_FD;
			return -1;
		} else if(ret == 0) {
			LOGATCI(LOG_DEBUG, "timeout for select in serviceReaderLoop");
			close(s_fdService_command);
			s_fdService_command = INVALIDE_SOCKET_FD;			 
			return -1;
		}
		
		if (FD_ISSET(s_fdService_command, &rfds)) {
			LOGATCI(LOG_DEBUG, "Prepare read the data from generic service");
			readDataFromTargetWithResult(GENERIC_TYPE, &s_fdService_command, buffer);
			if(regcomp(buffer, "*OK*", 0) == 0) {
				return 1;
			}
			else {
				return -1;
			}
		}
		return -1;
	}
	return -1;
}

void serviceReaderLoop(char* line) {
    int ret;
    struct timeval tv;
    fd_set rfds;

    LOGATCI(LOG_DEBUG,"Enter");

    if(s_fdService_command != INVALIDE_SOCKET_FD) {

        /*set timeout value to detach abnormal socket event */
        //tv.tv_sec = 5;
        //tv.tv_usec = 0;

        FD_ZERO(&rfds);
        FD_SET(s_fdService_command, &rfds);

        ret = select(s_fdService_command+1, &rfds, NULL, NULL, NULL);
        if (ret < 0) {
            if(errno == EINTR || errno == EAGAIN) {
                return;
            }
            LOGATCI(LOG_ERR, "Fail to select in serviceReaderLoop. error: %d", errno);
            close(s_fdService_command);
            s_fdService_command = INVALIDE_SOCKET_FD;
        } else if(ret == 0) {
            LOGATCI(LOG_DEBUG, "timeout for select in serviceReaderLoop");
            close(s_fdService_command);
            s_fdService_command = INVALIDE_SOCKET_FD;            
            return;
        }
        
        if (FD_ISSET(s_fdService_command, &rfds)) {
            LOGATCI(LOG_DEBUG, "Prepare read the data from generic service");
			if(strcmp(line, "AT+POWERKEY") == 0)
            	accessSpecialAtCommand(GENERIC_TYPE, &s_fdService_command);
			else
				readDataFromTarget(GENERIC_TYPE, &s_fdService_command);
        }                
    }
}

void rildReaderLoop() {
    int ret;
    struct timeval tv;
    fd_set rfds;

    LOGATCI(LOG_DEBUG,"Enter");

    if(s_fdRild_command != INVALIDE_SOCKET_FD) {

        /*set timeout value to detach abnormal socket event */
        //tv.tv_sec = 5;
        //tv.tv_usec = 0;

        FD_ZERO(&rfds);
        FD_SET(s_fdRild_command, &rfds);

        ret = select(s_fdRild_command+1, &rfds, NULL, NULL, NULL);
        if (ret < 0) {
            if(errno == EINTR || errno == EAGAIN) {
                return;
            }
            LOGATCI(LOG_ERR, "Fail to select in rildReaderLoop. error: %d", errno);
            close(s_fdRild_command);
            s_fdRild_command = INVALIDE_SOCKET_FD;
        } else if(ret == 0) {
            LOGATCI(LOG_DEBUG, "timeout for select in rildREaderLoop");
            close(s_fdRild_command);
            s_fdRild_command = INVALIDE_SOCKET_FD;            
            return;
        }
        
        if (FD_ISSET(s_fdRild_command, &rfds)) {
            LOGATCI(LOG_DEBUG, "Prepare read the data from rild driver");
            readDataFromTarget(RIL_TYPE, &s_fdRild_command);
        }                
    }
}

void audioReaderLoop() {
	int ret;
	struct timeval tv;
	fd_set rfds;

	LOGATCI(LOG_DEBUG,"Enter");

	if(s_fdAudio_command != INVALIDE_SOCKET_FD) {

		/*set timeout value to detach abnormal socket event */
		//tv.tv_sec = 5;
		//tv.tv_usec = 0;

		FD_ZERO(&rfds);
		FD_SET(s_fdAudio_command, &rfds);

		ret = select(s_fdAudio_command+1, &rfds, NULL, NULL, NULL);
		if (ret < 0) {
			if(errno == EINTR || errno == EAGAIN) {
				return;
			}
			LOGATCI(LOG_ERR, "Fail to select in rildReaderLoop. error: %d", errno);
			close(s_fdAudio_command);
			s_fdAudio_command = INVALIDE_SOCKET_FD;
		} else if(ret == 0) {
			LOGATCI(LOG_DEBUG, "timeout for select in rildREaderLoop");
			close(s_fdAudio_command);
			s_fdAudio_command = INVALIDE_SOCKET_FD;			  
			return;
		}
		
		if (FD_ISSET(s_fdAudio_command, &rfds)) {
			LOGATCI(LOG_DEBUG, "Prepare read the data from rild driver");
			readDataFromTarget(AUDIO_TYPE, &s_fdAudio_command);
		}				 
	}

}
/**
 * Reads a line from the AT channel, returns NULL on timeout.
 * Assumes it has exclusive read access to the FD
 *
 * This line is valid only until the next call to readline
 *
 * This function exists because as of writing, android libc does not
 * have buffered stdio.
 */

static const char *readline(Serial *serial)
{
    fd_set rfds;
    char *p_read = NULL;
    int ret = 0;
    char buffer[MAX_DATA_SIZE];
    int readCurr = 0, readCount = 0;
    int err = 0, i = 0;
    int fd = INVALIDE_SOCKET_FD, currDevice = 0;
    int headerSize = 0;

    LOGATCI(LOG_DEBUG,"Enter");


    //Start to read the command data from USB devices
    for(;;) {

        //Initial the default value for each local paramters
        //Set the buffer data to zero
        memset(serial->ATBuffer, 0, sizeof(serial->ATBuffer));
        memset(buffer, 0, sizeof(buffer));
        serial->at_state = FIND_A;      //Inital state to find 'A' character in command line
        serial->totalSize = 0;
        readCurr = 0;
        FD_ZERO(&rfds);

        for(i = 0; i < MAX_DEVICE_NUM; i ++) {
            FD_SET(serial->fd[i], &rfds);
        }

        LOGATCI(LOG_DEBUG, "Wait for select data from USB");
        ret = select(MAX(serial->fd[0], serial->fd[1]) + 1, &rfds, NULL, NULL, NULL);

        if (ret == -1) {
            if(errno == EINTR || errno == EAGAIN) {
                continue;
            }
            LOGATCI(LOG_ERR, "Fail to select in readline. error: %d", errno);
            for(i = 0; i < MAX_DEVICE_NUM; i ++) {
                close(serial->fd[i]);
                serial->fd[i] = INVALIDE_SOCKET_FD;
            }
            return NULL;
        } else if(ret == 0) {
            LOGATCI(LOG_DEBUG, "ERROR:No data from USB devices");
            continue;
        }

        for(i = 0; i < MAX_DEVICE_NUM; i++) {
            if (FD_ISSET(serial->fd[i], &rfds)) {
                LOGATCI(LOG_DEBUG, "Read data from USB:%d:%d", i, serial->fd[i]);
                fd = serial->fd[i];
                currDevice = i;
                serial->currDevice = i;
                break;
            }
        }

        do {
            LOGATCI(LOG_DEBUG, "Wait for read data from USB :%d", fd);
            readCount = read(fd, buffer, sizeof(buffer));  //Read data from VCOM driver

            LOGATCI(LOG_DEBUG, "the readCount is %d with %s:%d", readCount, buffer, buffer[0]);

            if(readCount <= 0) {
                err = errno;
                if(err != EINTR && err != EAGAIN) {
                    LOGATCI(LOG_ERR, "FATAL Error in serail read:%d", err);
                    close(fd);

                    //Reopen this serial port
                    for(i = 0; i < MAX_FAILURE_RETRY; i++) {
                        serial->fd[currDevice] = open_serial_device(serial, serial->devicename[currDevice]);
                        if(serial->fd[currDevice] != INVALIDE_SOCKET_FD) {
                            LOGATCI(LOG_DEBUG, "The FD is reopen successfully");
                            return NULL;
                        }
                    }
                    LOGATCI(LOG_ERR, "The process is terminated due to fatal error");
                    exit(1);
                }
                continue;
            } else if(readCount == 0) {
                LOGATCI(LOG_ERR, "Can't get the data from USB-%d:%d", fd, errno);
                return NULL;
            } else {
                break;
            }
        } while(1);


        LOGATCI(LOG_DEBUG, "state %d with %d:%d", serial->at_state, readCurr, UCASE(buffer[readCurr]));

        readCurr = audio_command_hdlr(buffer);
        if(readCurr <= 0) {
            readCurr = 0;
            headerSize = 2;
            if(serial->at_state == FIND_A && readCount > 2){
                if((UCASE(buffer[0]) == AT_A) && (UCASE(buffer[1]) == AT_T)){
                    	p_read = &buffer[0];
                    	serial->at_state = FIND_DONE;
                    	readCurr = 2;
                }	
            }
        } else {
            serial->at_state = FIND_DONE;
            headerSize = readCurr;
            p_read = &buffer[0];
        }

        //There is no AT string in this input; discard this input and get the input again.
        if(serial->at_state == FIND_DONE) {
            serial->totalSize = readCount - readCurr + headerSize; //Add the length of "AT"
            LOGATCI(LOG_DEBUG, "the readCurr is %d & the total size is %d", readCurr, serial->totalSize);
            memcpy(serial->ATBuffer, p_read, serial->totalSize);
            serial->ATBuffer[serial->totalSize-1] = '\0';  //Skip the "\r" character
            break;
        }
    }

    return &serial->ATBuffer[0];
}

void *readerLoop(void *arg)
{
    Serial *p_serial = (Serial *)arg;
    ATCI_DataType dataType = UNKNOWN_TYPE;

    LOGATCI(LOG_DEBUG,"Enter");

    for (;;) {
        char* line = NULL;

        line = (char*) readline(p_serial);

        if (line == NULL) {
            //Try to reopen the USB virutail COM port
            //open_serial_device(&serial);
            continue;
        }

        convertToUpperCase(line);
        trim_string(&line);
        LOGATCI(LOG_INFO, "Command:%s",line);

        dataType = process_cmd_line(line);

        LOGATCI(LOG_INFO, "The command type is belonged to :%d",dataType);

        if(dataType == RIL_TYPE) {
            if (s_fdRild_command < 0) {
                connectTarget(RIL_TYPE);
            }
            sendDataToRild(line);
//            rildReaderLoop();
        } else if(dataType == ATCI_TYPE) {
            LOGATCI(LOG_INFO, "ATCI COMMAND");
        } else if(dataType == GENERIC_TYPE) {
            if (s_fdService_command < 0) {
                connectTarget(GENERIC_TYPE);
            }
            sendDataToGenericService(line);
            serviceReaderLoop(line);
        } else if(dataType == PLATFORM_TYPE) {
            LOGATCI(LOG_INFO, "PLATFORM COMMAND");
        } else if(dataType == AUDIO_TYPE) {
        	if(s_fdAudio_command < 0) {
                connectTarget(AUDIO_TYPE);
        	}
			sendDataToAudio(line, p_serial->totalSize);
			audioReaderLoop();
        }
    }

    LOGATCI(LOG_INFO, "ReaderLoop thread Closed");
    //onReaderClosed(p_channel);

    return NULL;
}

/*
* Purpose:  Initial the default value of serial device.
* Input:      serial - the serial struct
* Return:    void
*/

void initSerialDevice(Serial *serial) {

    LOGATCI(LOG_DEBUG,"Enter");
    memset(serial,0,sizeof(Serial));
    serial->fd[0] = INVALIDE_SOCKET_FD;
    serial->fd[1] = INVALIDE_SOCKET_FD;
    serial->echo[0] = 1;
    serial->echo[1] = 1;
    serial->totalSize = 0;
}


/*
* Purpose:  Creates a detached thread. also checks for errors on exit.
* Input:      thread_id - pointer to pthread_t id
*                thread_function - void pointer to thread function
*                thread_function_arg - void pointer to thread function args
* Return:    0 if success, -1 if fail
*/
/*
int create_thread(
    pthread_t * thread_id,
    void * thread_function,
    void * thread_function_arg
)
{
    pthread_attr_t thread_attr;

    LOGATCI(LOG_DEBUG,"Enter");
    pthread_attr_init(&thread_attr);
    pthread_attr_setdetachstate(&thread_attr, PTHREAD_CREATE_DETACHED);

    if(pthread_create(thread_id, &thread_attr, thread_function, thread_function_arg)!=0) {
        switch (errno) {
        case EAGAIN:
            LOGATCI(LOG_ERR,"Interrupt signal EAGAIN caught");
            break;
        case EINVAL:
            LOGATCI(LOG_ERR,"Interrupt signal EINVAL caught");
            break;
        default:
            LOGATCI(LOG_ERR,"Unknown interrupt signal caught");
        }
        LOGATCI(LOG_ERR,"Could not create thread");
        return -1;
    }
    pthread_attr_destroy(&thread_attr); 
    return 0; //thread created successfully
}
*/

void setEchoOption(int flag){
    struct termios deviceOptions;
    int fd = serial.fd[serial.currDevice];
    
    LOGATCI(LOG_INFO, "set echo option:%d for device:%d", flag, serial.currDevice);
    
    // get the parameters  
    tcgetattr(fd,&deviceOptions);
    if(flag == 1){
        deviceOptions.c_lflag = ICANON | ECHO;
    }else{
        deviceOptions.c_lflag = ICANON;
    }

    serial.echo[serial.currDevice] = flag;
    
    tcflush(fd, TCIFLUSH);
    tcsetattr(fd,TCSANOW,&deviceOptions);
        
}

/*
* Purpose:  Open and initialize the serial device used.
* Input:      serial - the serial struct
* Return:    0 if port successfully opened, else -1.
*/
int open_serial_device(Serial* serial, char* devicename) {
    LOGATCI(LOG_DEBUG, "Enter");
    unsigned int i;
    int fdflags;
    struct termios deviceOptions;
    int fd = INVALIDE_SOCKET_FD;
    int is_echo = 1;


    LOGATCI(LOG_INFO, "Opened serial port:%s", devicename);

    do {
        fd = open(devicename, O_RDWR | O_NOCTTY | O_NONBLOCK);
    } while (fd < 0 && errno == EINTR);

    if(fd < 0) {
        LOGATCI(LOG_ERR, "Fail to open serial device(%s) with error code:%d", devicename, errno);
        return INVALIDE_SOCKET_FD;
    }

    if(strcmp(devicename, TTY_GS0) == 0){
        is_echo = serial->echo[0];
    }else if(strcmp(devicename, TTY_GS0) == 0){
        is_echo = serial->echo[1];
    }

    LOGATCI(LOG_DEBUG, "serial->fd is %d ", fd);

    //Set FD to blocking IO
    fdflags = fcntl(fd, F_GETFL);
    fcntl(fd, F_SETFL, fdflags & ~O_NONBLOCK);

    // get the parameters
    tcgetattr(fd,&deviceOptions);

    // set raw input
    //deviceOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
    if(is_echo == 1){
        deviceOptions.c_lflag = ICANON | ECHO; //http://www.faqs.org/docs/Linux-HOWTO/Serial-Programming-HOWTO.html#AEN92 2.3.1. Canonical Input Processing
    }else{
        deviceOptions.c_lflag = ICANON; //http://www.faqs.org/docs/Linux-HOWTO/Serial-Programming-HOWTO.html#AEN92 2.3.1. Canonical Input Processing
    }
    

    // set raw output
    deviceOptions.c_oflag &= ~OPOST;
    deviceOptions.c_oflag &= ~OLCUC;
    deviceOptions.c_oflag &= ~ONLRET;
    deviceOptions.c_oflag &= ~ONOCR;
    deviceOptions.c_oflag &= ~OCRNL;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd,TCSANOW,&deviceOptions);

    return fd;
}

void* AtcidConnectUsb (void *param){
    int ret = 0 ;
    fd_set rfds;
    FD_ZERO(&rfds);
	
    LOGD("atcid start to wait for atci_serv data !!!");
    for (;;) {
        if (s_fdRild_command < 0) {                
            s_fdRild_command = socket_local_client(SOCKET_NAME_RILD,
                                               ANDROID_SOCKET_NAMESPACE_RESERVED,
                                               SOCK_STREAM);
            
            if (s_fdRild_command < 0) {
                LOGE("fail to open atci-ril socket. errno:%d", errno);
                close(s_fdRild_command);
                usleep(100*1000);
                continue;
            }
        }

        rildReaderLoop();
    }   
}     