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

#include <stdio.h>   /* Standard input/output definitions */
#include <string.h>  /* String function definitions */
#include <unistd.h>  /* UNIX standard function definitions */
#include <fcntl.h>   /* File control definitions */
#include <errno.h>   /* Error number definitions */
#include <termios.h> /* POSIX terminal control definitions */
#include <time.h>
#include <pthread.h>
#include <stdlib.h>
#include <signal.h>
#include <netdb.h>
#include <sys/types.h>
#include <sys/wait.h>
#include <sys/socket.h>
#include <sys/epoll.h>
#include <netinet/in.h>
#include <arpa/inet.h>

#include <ctype.h>
#include <dirent.h>
#include <pthread.h>

#include "common.h"
#include "miniui.h"
#include "ftm.h"

#ifdef FEATURE_FTM_GPS

#define TAG             "[GPS]   "
#define INFO_SIZE       1024
#define NMEA_SIZE       10240

unsigned char nmea_buf[NMEA_SIZE];

#define NUM_CH  (20)
#define PSEUDO_CH (32)
#define Knot2Kmhr (1.8532)

typedef struct SVInfo
{
    int SVid;            // PRN
    int SNR;
    int elv;             // elevation angle : 0~90
    int azimuth;         // azimuth : 0~360
    unsigned char Fix;   // 0:None , 1:FixSV
} SVInfo;

typedef struct ChInfo
{
    int SVid;            // PRN
    int SNR;             // SNR
    unsigned char Status;// Status(0:Idle, 1:Search, 2:Tracking)
} ChInfo;

typedef struct GPSInfo
{
    int year;
    int mon;
    int day;
    int hour;
    int min;
    float sec;

    float Lat; // Position, +:E,N -:W,S
    float Lon;
    float Alt;
    unsigned char FixService;  // NoFix:0, SPS:1, DGPS:2, Estimate:6
    unsigned char FixType;     // None:0, 2D:1, 3D:2
    float Speed;  // km/hr
    float Track;  // 0~360
    float PDOP;   //DOP
    float HDOP;
    float VDOP;

    int SV_cnt;
    int fixSV[NUM_CH];
}GPSInfo;

GPSInfo g_gpsInfo;
SVInfo  g_svInfo[NUM_CH];
ChInfo  g_chInfo[PSEUDO_CH];

int ttff = 0;
int fixed = 0;
int httff = 0;
int cttff = 0;

enum {
    ITEM_PASS,
    ITEM_FAIL,
    ITEM_HTTFF,
    ITEM_CTTFF
};

static item_t gps_items[] = {
    item(ITEM_PASS,   "Test Pass"),
    item(ITEM_FAIL,   "Test Fail"),
    item(ITEM_HTTFF,  "Hot Restart"),
    item(ITEM_CTTFF,  "Cold Restart"),
    item(-1, NULL),
};

struct gps_desc {
    char         info[INFO_SIZE];
    char        *mntpnt;
    bool         exit_thd;

    text_t title;
    text_t text;
    
    pthread_t update_thd;
    struct ftm_module *mod;
    struct itemview *iv;
};

#define mod_to_gps(p)  (struct gps_desc*)((char*)(p) + sizeof(struct ftm_module))

#define C_INVALID_PID  (-1)   /*invalid process id*/
#define C_INVALID_TID  (-1)   /*invalid thread id*/
#define C_INVALID_FD   (-1)   /*invalid file handle*/
#define C_INVALID_SOCKET (-1) /*invalid socket id*/

pid_t mnl_pid = C_INVALID_PID;
int sockfd = C_INVALID_SOCKET;
pthread_t gps_meta_thread_handle = C_INVALID_TID;

static int mnl_write_attr(const char *name, unsigned char attr) 
{
    int err, fd = open(name, O_RDWR);
    char buf[] = {attr + '0'};
    
    if (fd == -1) {
        LOGD(TAG"open %s err = %s\n", name, strerror(errno));
        return -errno;
    }
    do { err = write(fd, buf, sizeof(buf) ); }
    while (err < 0 && errno == EINTR);
    
    if (err != sizeof(buf)) { 
        LOGD(TAG"write fails = %s\n", strerror(errno));
        err = -errno;
    } else {
        err = 0;    /*no error*/
    }
    if (close(fd) == -1) {
        LOGD(TAG"close fails = %s\n", strerror(errno));
        err = (err) ? (err) : (-errno);
    }
    LOGD(TAG"write '%d' to %s okay\n", attr, name);
    return err;
}

static int GPS_Open()
{
    int err;
    pid_t pid;
    int portno;
    struct sockaddr_in serv_addr;
    struct hostent *server;
    int mt3326_fd;
    struct termios termOptions;
    unsigned char query[11]     = {0x04, 0x24, 0x0b, 0x00, 0x08, 0xff, 0x19, 0x00, 0xe5, 0x0d, 0x0a};
    unsigned char response[11]  = {0x04, 0x24, 0x0b, 0x00, 0x1d, 0xff, 0x01, 0xaa, 0x42, 0x0d, 0x0a};
    unsigned char buf[20] = {0};
    int nRead = 0, nWrite = 0;
#if (defined(MTK_GPS_MT6620)||defined(MTK_GPS_MT6628))
#define DSP_DEV     "/dev/stpgps"
#else
#define DSP_DEV     "/dev/ttyMT1"
#endif

    LOGD(TAG"GPS_Open() 1\n");
    // power on GPS chip
#if !(defined(MTK_GPS_MT6620)||defined(MTK_GPS_MT6628))
    err = mnl_write_attr("/sys/class/gpsdrv/gps/pwrctl", 4);
    if(err != 0)
    {
        LOGD(TAG"GPS_Open: GPS power-on error: %d\n", err);
        return (-1);
    }
#endif
#if !(defined(MTK_GPS_MT6620)||defined(MTK_GPS_MT6628))
    // check whether GPS chip is alive or not
	mt3326_fd = open(DSP_DEV, O_RDWR | O_NOCTTY);
	if (mt3326_fd == -1) 
    {
		LOGD(TAG"GPS_Open: Unable to open - %s\n", DSP_DEV);
        /*the process should exit if fail to open UART device*/
        return (-7); 
	}
    else 
    {
		LOGD("GPS_Open: open - %s\n", DSP_DEV);
		fcntl(mt3326_fd, F_SETFL, 0);

		// Get the current options:
		tcgetattr(mt3326_fd, &termOptions);

		// Set 8bit data, No parity, stop 1 bit (8N1):
		termOptions.c_cflag &= ~PARENB;
		termOptions.c_cflag &= ~CSTOPB;
		termOptions.c_cflag &= ~CSIZE;
		termOptions.c_cflag |= CS8 | CLOCAL | CREAD;

		LOGD("GPS_Open: c_lflag=%x,c_iflag=%x,c_oflag=%x\n",termOptions.c_lflag,termOptions.c_iflag,
								termOptions.c_oflag);
		//termOptions.c_lflag

		// Raw mode
		termOptions.c_iflag &= ~(INLCR | ICRNL | IXON | IXOFF | IXANY);
		termOptions.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);  /*raw input*/
		termOptions.c_oflag &= ~OPOST;  /*raw output*/

		tcflush(mt3326_fd,TCIFLUSH);//clear input buffer
		termOptions.c_cc[VTIME] = 0; /* inter-character timer unused */
		termOptions.c_cc[VMIN] = 0; /* blocking read until 0 character arrives */

        // Set baudrate to 38400 bps
        cfsetispeed(&termOptions, B38400);
		cfsetospeed(&termOptions, B38400);

		/*
		* Set the new options for the port...
		*/
		tcsetattr(mt3326_fd, TCSANOW, &termOptions);
	}
    usleep(500000);
    nWrite = write(mt3326_fd, query, sizeof(query));
    LOGD(TAG"nWrite = %d\n", nWrite);
    usleep(500000);
    nRead = read(mt3326_fd, buf, sizeof(buf));
    LOGD(TAG"nRead = %d\n", nRead);
    if(nRead > 0)
    {
        int i;

        for(i = 0; i < nRead; i++)
        {
            LOGD("%02x ", buf[i]);
        }
        LOGD("\n");
    }
    close(mt3326_fd);
    if(nRead < 11) // response should be 11 bytes long
    {
        return (-8);
    }
    usleep(500000);
#endif
    // run gps driver (libmnlp)
    if ((pid = fork()) < 0) 
    {
        LOGD(TAG"GPS_Open: fork fails: %d (%s)\n", errno, strerror(errno));
        return (-2);
    } 
    else if (pid == 0)  /*child process*/
    {
        int err;

        LOGD(TAG"GPS_Open: execute: %s\n", "/system/xbin/libmnlp");
#if (defined(MTK_GPS_MT6620)||defined(MTK_GPS_MT6628))
        LOGD("check MTK_GPS_MT6620/MTK_GPS_MT6628\n");
        err = execl("/system/xbin/libmnlp", "libmnlp", "1Hz=y", NULL);
#else
        err = execl("/system/xbin/libmnlp", "libmnlp", NULL);
#endif
        if (err == -1)
        {
            LOGD(TAG"GPS_Open: execl error: %s\n", strerror(errno));
            return (-3);
        }
        return 0;
    } 
    else  /*parent process*/
    {
        mnl_pid = pid;
        LOGD(TAG"GPS_Open: mnl_pid = %d\n", pid);
    }

    // create socket connection to gps driver
    portno = 7000;
    /* Create a socket point */
    sockfd = socket(AF_INET, SOCK_STREAM, 0);
    if (sockfd < 0) 
    {
        LOGD(TAG"GPS_Open: ERROR opening socket");
        return (-4);
    }
    server = gethostbyname("127.0.0.1");
    if (server == NULL) {
        LOGD(TAG"GPS_Open: ERROR, no such host\n");
        return (-5);
    }

    bzero((char *) &serv_addr, sizeof(serv_addr));
    serv_addr.sin_family = AF_INET;
    bcopy((char *)server->h_addr, (char *)&serv_addr.sin_addr.s_addr, server->h_length);
    serv_addr.sin_port = htons(portno);

    sleep(3);  // sleep 5sec for libmnlp to finish initialization

    /* Now connect to the server */
    if (connect(sockfd, (struct sockaddr *)&serv_addr, sizeof(serv_addr)) < 0) 
    {
         LOGD(TAG"GPS_Open: ERROR connecting");
         return (-6);
    }	

    LOGD(TAG"GPS_Open() 2\n");

    return 0;
}

static void GPS_Close()
{    
    int err;
    
    LOGD(TAG"GPS_Close() 1\n");

    // disconnect to gps driver
    if(sockfd != C_INVALID_SOCKET)
    {
        LOGD(TAG"GPS_Close() 2\n");
        close(sockfd);
        LOGD(TAG"GPS_Close() 3\n");
        sockfd = C_INVALID_SOCKET;
    }
    LOGD(TAG"GPS_Close() 4\n");

    // kill gps driver (libmnlp)
    if(mnl_pid != C_INVALID_PID)
    {
        LOGD(TAG"GPS_Close() 5\n");
        kill(mnl_pid, SIGKILL);
        usleep(500000); //500ms
    }
    LOGD(TAG"GPS_Close() 6\n");
    
    // power off GPS chip
#if !(defined(MTK_GPS_MT6620)||defined(MTK_GPS_MT6628))
    err = mnl_write_attr("/sys/class/gpsdrv/gps/pwrctl", 0);
    if(err != 0)
    {
        LOGD(TAG"GPS power-off error: %d\n", err);
    }
    LOGD(TAG"GPS_Close() 6\n");
#endif

    return;
}

unsigned char CheckSum(char *buf, int size)
{
   int i;
   char chksum=0, chksum2=0;

   if(size < 5)
      return false;

   chksum = buf[1];
   for(i = 2; i < (size - 2); i++)
   {
      if(buf[i] != '*')
      {
        chksum ^= buf[i];
      }
      else
      {
        if(buf[i + 1] >= 'A')
        {
          chksum2 = (buf[i+1]-'A'+10)<<4;
        }
        else
        {
          chksum2 = (buf[i+1]-'0')<<4;
        }

        if(buf[i + 2] >= 'A')
        {
          chksum2 += buf[i+2]-'A'+10;
        }
        else
        {
          chksum2 += buf[i+2]-'0';
        }
        break;
      }
    }

   /* if not found character '*' */
   if(i == (size - 2))
   {
      return (false);
   }

   if(chksum == chksum2)
   {
     return (true);
   }
   else
   {
     return (false);
   }
}

bool FetchField(char *start, char *result)
{
   char *end;

   if(start == NULL)
      return false;

   end = strstr( start, ",");
   // the end of sentence
   if(end == NULL)
      end = strstr(start, "*");

   if(end-start>0)
   {
     strncpy( result, start, end-start);
     result[end-start]='\0';
   }
   else   // no data
   {
     result[0]='\0';
     return false;
   }

   return true;
}
void GLL_Parse( char *head)
{
   // $GPGLL,2446.367638,N,12101.356226,E,144437.000,A,A*56
   char *start, result[20], tmp[20], *point;
   int len=0;
   char FixService;

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // Position(Lat)
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField(start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lat = (float)(atof(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lat += (float)(atof(tmp)/60.0);
      }

      // N or S
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField(start, result))
      {
         if(*result=='S')
            g_gpsInfo.Lat = -g_gpsInfo.Lat;
      }

      // Position(Lon)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField(start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lon = (float)(atof(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lon += (float)(atof(tmp)/60.0);
      }

      // E or W
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         if(*result=='W')
            g_gpsInfo.Lon = -g_gpsInfo.Lon;
      }

      // UTC Time
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         // Hour
         strncpy( tmp, result, 2);
         tmp[2]='\0';
         g_gpsInfo.hour = atoi(tmp);
         // Min
         strncpy( tmp, result+2, 2);
         tmp[2]='\0';
         g_gpsInfo.min = atoi(tmp);
         // Sec
         strncpy( tmp, result+4, strlen(result)-4);
         tmp[strlen(result)-4]='\0';
         g_gpsInfo.sec = (float)(atof(tmp));
      }   

      // The positioning system Mode Indicator and Status fields shall not be null fields.
      // Data valid
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      if(!FetchField( start, result))
         return;
      
      if(*result=='A')
      {
         // Fix Type
         if(g_gpsInfo.FixType == 0)
            g_gpsInfo.FixType = 1;   // Assume 2D, if there's no other info.

         // Fix Service
         start = strstr( start, ",");
         if(start != NULL)
             start = start +1;
         else
            return;

         if(!FetchField( start, result))
            return;

         FixService = *result;

         switch(FixService)
         {
            case 'A':
            {
               g_gpsInfo.FixService = 1;
               break;
            }
            case 'D':
            {
               g_gpsInfo.FixService = 2;
               break;
            }
            case 'E':
            {
               g_gpsInfo.FixService = 6;
               break;
            }
         }
      }
      else // Data invalid
      {
         g_gpsInfo.FixType = 0;    // NoFix
         g_gpsInfo.FixService = 0; // NoFix
      }
   }
}
//---------------------------------------------------------------------------
void RMC_Parse( char *head)
{
   // $GPRMC,073446.000,A,2446.3752,N,12101.3708,E,0.002,22.08,121006,,,A*6C

   char *start, result[20], tmp[20], *point;
   int len=0;

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // UTC time : 161229.487
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, tmp))
      {
         // Hour
         strncpy( result, tmp, 2);
         result[2]='\0';
         g_gpsInfo.hour = atoi(result);
         // Min
         strncpy( result, tmp+2, 2);
         result[2]='\0';
         g_gpsInfo.min = atoi(result);
         // Sec
         strncpy( result, tmp+4, strlen(tmp)-4);
         result[strlen(tmp)-4]='\0';
         g_gpsInfo.sec = (float)(atof(result));
      }

      // valid
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;

      if(*result == 'A')
      {
         if(g_gpsInfo.FixType == 0)
            g_gpsInfo.FixType = 1;      // Assume 2D

         if(g_gpsInfo.FixService == 0)
            g_gpsInfo.FixService = 1;   // Assume SPS
      }
      else
      {
         g_gpsInfo.FixType = 0;    // NoFix
         g_gpsInfo.FixService = 0; // NoFix
      }

      // Position(Lat) : 3723.2475(N)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lat = (float)(atoi(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lat += (float)(atof(tmp)/60.0);
      }
	  else  //Can not fetch Lat field
	  {
	     g_gpsInfo.Lat = 0;
	  }

      // N or S
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result) && g_gpsInfo.Lat!=0)
      {
         if(*result=='S')
            g_gpsInfo.Lat = -g_gpsInfo.Lat;
      }

      // Position(Lon) : 12158.3416(W)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lon = (float)(atoi(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lon += (float)(atof(tmp)/60.0);
      }
	  else  //Can not fetch Lat field
	  {
	     g_gpsInfo.Lon = 0;
	  }

      // E or W
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result) && g_gpsInfo.Lat!=0)
      {
         if(*result=='W')
            g_gpsInfo.Lon = -g_gpsInfo.Lon;
      }

      // Speed : 0.13
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.Speed = (float)(atof(result) * Knot2Kmhr);
      }

      // Track : 309.62
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.Track = (float)(atof(result));
      }

      // Date : 120598
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         // Day
         strncpy(tmp, result, 2);
         tmp[2]='\0';
         g_gpsInfo.day=atoi(tmp);

         // Month
         strncpy(tmp, result+2, 2);
         tmp[2]='\0';
         g_gpsInfo.mon=atoi(tmp);

         // Year
         strncpy(tmp, result+4, 2);
         tmp[2]='\0';
         g_gpsInfo.year=atoi(tmp)+2000;
      }

      // skip Magnetic variation
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      // mode indicator
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      if(!FetchField( start, result))
         return;

      if(g_gpsInfo.FixType > 0)
      {
         switch(result[0])
         {
            case 'A':
            {
               g_gpsInfo.FixService = 1;
               break;
            }
            case 'D':
            {
               g_gpsInfo.FixService = 2;
               break;
            }
            case 'E':
            {
               g_gpsInfo.FixService = 6;
               break;
            }
         }
      }
   }
}
//---------------------------------------------------------------------------
void VTG_Parse( char *head)
{
   //$GPVTG,159.16,T,,M,0.013,N,0.023,K,A*34
   char *start, result[20];
   char FixService;

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // Track
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.Track = (float)(atof(result));
      }

      // ignore
      start = strstr( start, ",");     // T
      if(start != NULL)
         start = start +1;
      else
         return;

      start = strstr( start, ",");     // NULL
      if(start != NULL)
         start = start +1;
      else
         return;

      start = strstr( start, ",");     // M
      if(start != NULL)
         start = start +1;
      else
         return;

      // Speed
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.Speed = (float)(atof(result) * Knot2Kmhr);
      }

      // ignore
      start = strstr( start, ",");     // N
      if(start != NULL)
         start = start +1;
      else
         return;

      start = strstr( start, ",");     // 0.023
      if(start != NULL)
         start = start +1;
      else
         return;

      start = strstr( start, ",");     // K
      if(start != NULL)
         start = start +1;
      else
         return;

      // Fix Service
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      if(!FetchField( start, result))
         return;
      FixService = *result;

      if(FixService != 'N')
      {
         if(g_gpsInfo.FixType == 0)
            g_gpsInfo.FixType = 1;  //Assume 2D

         switch(FixService)
         {
            case 'A':
            {
               g_gpsInfo.FixService = 1;
               break;
            }
            case 'D':
            {
               g_gpsInfo.FixService = 2;
               break;
            }
            case 'E':
            {
               g_gpsInfo.FixService = 6;
               break;
            }
         }
      }
      else // NoFix
      {
         g_gpsInfo.FixType = 0;    // NoFix
         g_gpsInfo.FixService = 0; // NoFix
      }
   }
}
//---------------------------------------------------------------------------
void GSA_Parse( char *head)
{
   // $GPGSA,A,3,03,19,27,23,13,16,15,11,07,,,,1.63,0.95,1.32*03
   char *start, result[20];
   int sv_cnt=0, i;

   if(CheckSum(head, strlen(head)))
   {
      //Fix SV
      memset(&g_gpsInfo.fixSV, 0, sizeof(g_gpsInfo.fixSV));

      //Valid
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;

      if((*result == 'A') || (*result == 'M'))
      {
         // Fix Type
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;
         if(!FetchField( start, result))
            return;
         g_gpsInfo.FixType = atoi(result)-1;

         if(g_gpsInfo.FixType > 0)          // Fix
         {
            if(g_gpsInfo.FixService == 0)
               g_gpsInfo.FixService = 1;    //Assume SPS FixSerivce
         }
         else
         {
            g_gpsInfo.FixType = 0;    // NoFix
            g_gpsInfo.FixService = 0; // NoFix
         }
      }
      else
      {
         g_gpsInfo.FixType = 0;    // NoFix
         g_gpsInfo.FixService = 0; // NoFix
      }

      for(i=0 ; i<12 ; i++)
      {
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;

         FetchField( start, result);

         if(strlen(result)>0)
            g_gpsInfo.fixSV[sv_cnt++] = atoi(result);
      }

      //PDOP
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      g_gpsInfo.PDOP = (float)(atof(result));

      //HDOP
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      g_gpsInfo.HDOP = (float)(atof(result));

      //VDOP
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      g_gpsInfo.VDOP = (float)(atof(result));
   }
}
//---------------------------------------------------------------------------
void GSV_Parse( char *head)
{
   // $GPGSV,3,1,09,03,63,020,43,19,76,257,37,27,14,320,30,23,39,228,37*79
   // $GPGSV,3,2,09,13,38,274,38,16,31,058,37,15,16,055,34,11,16,192,32*76
   // $GPGSV,3,3,09,07,15,043,26*40

   char *start, result[20];
   int sv_cnt=0, base, i;

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // ignore
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      //first Message
      if(*(start+2)=='1')
      {
         memset( &g_svInfo, 0, sizeof(g_svInfo));
         //g_fgSVUpdate = false;
      }

      // Last Message
      //if(*start == *(start+2))
      //   g_fgSVUpdate = true;

      //base  //sentence number.
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      base = (atoi(result)-1)*4;

      //total
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      g_gpsInfo.SV_cnt = atoi(result);
      if(g_gpsInfo.SV_cnt == 0)
      {
         return;
      }

      for( i=0 ; i<4 ; i++)
      {
         //SVid
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;
         FetchField( start, result);
         if(strlen(result)>0)
            g_svInfo[base+sv_cnt].SVid = atoi(result);
         else
            g_svInfo[base+sv_cnt].SVid = 0;

         //elev
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;
         FetchField( start, result);
         if(strlen(result)>0)
            g_svInfo[base+sv_cnt].elv = atoi(result);
         else
            g_svInfo[base+sv_cnt].elv = 0;

         //azimuth
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;
         FetchField( start, result);
         if(strlen(result)>0)
            g_svInfo[base+sv_cnt].azimuth = atoi(result);
         else
            g_svInfo[base+sv_cnt].azimuth = 0;

         //SNR
         start = strstr( start, ",");
         if(start != NULL)
            start = start +1;
         else
            return;
         if(*start == '*')
            g_svInfo[base+sv_cnt].SNR = 0;
         else
         {
            FetchField( start, result);
            if(strlen(result)>0)
               g_svInfo[base+sv_cnt].SNR = atoi(result);
            else
               g_svInfo[base+sv_cnt].SNR = 0;
         }

         sv_cnt++;

         if(base+sv_cnt == g_gpsInfo.SV_cnt)
            break;
      }
   }
}
//---------------------------------------------------------------------------
void GGA_Parse( char *head)
{
   //$GPGGA,144437.000,2446.367638,N,12101.356226,E,1,9,0.95,155.696,M,15.057,M,,*58
   char *start, result[20], tmp[20], *point;
   int len=0;

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // UTC time : 144437.000
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      if(FetchField( start, result))
      {
         // Hour
         strncpy( tmp, result, 2);
         tmp[2]='\0';
         g_gpsInfo.hour = atoi(tmp);
         // Min
         strncpy( tmp, result+2, 2);
         tmp[2]='\0';
         g_gpsInfo.min = atoi(tmp);
         // Sec
         strncpy( tmp, result+4, strlen(result)-4);
         tmp[strlen(result)-4]='\0';
         g_gpsInfo.sec = (float)(atof(tmp));
      }

      // Position(Lat)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lat = (float)(atoi(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lat += (float)(atof(tmp)/60.0);
      }

      // N or S
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         if(*result=='S')
            g_gpsInfo.Lat = -g_gpsInfo.Lat;
      }

      // Position(Lon)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         point = strstr( result, ".");
         len = (point-2)-result;
         strncpy(tmp, result, len);
         tmp[len]='\0';
         g_gpsInfo.Lon = (float)(atoi(tmp));
         strncpy(tmp, result+len, strlen(result)-len);
         tmp[strlen(result)-len]='\0';
         g_gpsInfo.Lon += (float)(atof(tmp)/60.0);
      }

      // E or W
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         if(*result=='W')
            g_gpsInfo.Lon = -g_gpsInfo.Lon;
      }

      //GPS Fix Type and Service
      // 0: NoFix, 1:SPS, 2:DGPS, 6:Estimate
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(!FetchField( start, result))
         return;
      g_gpsInfo.FixService = atoi(result);

      // Fix
      if(g_gpsInfo.FixService > 0)
      {
         if(g_gpsInfo.FixType == 0)
            g_gpsInfo.FixType = 1; // Assume 2D
      }

      start = strstr( start, ",");   // Number of SV in use , ex :9
      if(start != NULL)
         start = start +1;
      else
         return;

      // HDOP
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.HDOP = (float)(atof(result));
      }

      //Altitude (mean sea level)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
      {
         g_gpsInfo.Alt = (float)(atof(result));
      }

      //Altitude unit (bypass)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      //Altitude (Geoidal separation)
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      
      if(FetchField( start, result))
      {
         g_gpsInfo.Alt += (float)(atof(result));
      }   
   }
}
//---------------------------------------------------------------------------
void ZDA_Parse( char *head)
{
   // $GPZDA,000007.123,06,01,2000,,*50
   char *start, result[20], tmp[20];

   // check checksum
   if(CheckSum(head, strlen(head)))
   {
      // UTC time : 000007.123
      start = strstr( head, ",");
      if(start != NULL)
         start = start +1;
      else
         return;

      if(FetchField( start, result))
      {
         // Hour
         strncpy( tmp, result, 2);
         tmp[2]='\0';
         g_gpsInfo.hour = atoi(tmp);

         // Min
         strncpy( tmp, result+2, 2);
         tmp[2]='\0';
         g_gpsInfo.min = atoi(tmp);

         // Sec
         strncpy( tmp, result+4, strlen(result)-4);
         tmp[strlen(result)-4]='\0';
         g_gpsInfo.sec = (float)(atof(tmp));
      }

      // Day
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
         g_gpsInfo.day = atoi(result);

      // Month
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
         g_gpsInfo.mon = atoi(result);

      // Year
      start = strstr( start, ",");
      if(start != NULL)
         start = start +1;
      else
         return;
      if(FetchField( start, result))
         g_gpsInfo.year = atoi(result);
   }
}

void NMEA_Parse(char *TempBuf)
{
    char *GP;
    char type[4];
    //char TempBuf[500];
    //strcpy(TempBuf,g_NMEAbuf);
    GP=strtok(TempBuf, "\r\n");

    memset(&g_gpsInfo, 0, sizeof(g_gpsInfo));
    memset(g_svInfo, 0, (sizeof(SVInfo)*NUM_CH));
    memset(g_chInfo, 0, (sizeof(ChInfo)*PSEUDO_CH));

    if(GP == NULL)
        return;

    do
    {
        // Channel Status
        if(strncmp(GP, "$PMTKCHN", 8) == 0)
        {
            //Channel_Parse(GP);
        }
        //Ack Parse
        else if(strncmp(GP, "$PMTK", 5) == 0)
        {
            //Ack_Parse(GP);
        }
        // NMEA Parse
        else if((strncmp(GP, "$GP", 3) == 0) && (strlen(GP) > 10))
        {
            // skip "$GP" char to fetch Message Type
            strncpy ( type, GP+3, 3);
            type[3]='\0';

            if(strcmp( type, "GLL")==0)
            {
                GLL_Parse( GP);
            }
            else if(strcmp( type, "RMC")==0)
            {
                RMC_Parse( GP);
            }
            else if(strcmp( type, "VTG")==0)
            {
                VTG_Parse( GP);
            }
            else if(strcmp( type, "GSA")==0)
            {
                GSA_Parse( GP);
            }
            else if(strcmp( type, "GSV")==0)
            {
                GSV_Parse( GP);
            }
            else if(strcmp( type, "GGA")==0)
            {
                GGA_Parse( GP);
            }
            else if(strcmp( type, "ZDA")==0)
            {
                ZDA_Parse( GP);
            }
        }
    }while( (GP = strtok( NULL, "\r\n")) != NULL );
}

static void gps_update_info(struct gps_desc *gps, char *info)
{
    char *ptr;
    int i = 0;
    int read_leng = 0;
    int total_leng = 0;

    memset(info, '\n', INFO_SIZE);
    info[INFO_SIZE-1] = 0x0;

    if(sockfd != C_INVALID_SOCKET)
    {
        memset(nmea_buf, 0, NMEA_SIZE);
        //do
        //{
            LOGD(TAG"read from sockfd 1\n");
            read_leng = read(sockfd, &nmea_buf[total_leng], (NMEA_SIZE - total_leng));
            total_leng += read_leng;
            LOGD(TAG"read_leng=%d, total_leng=%d\n", read_leng, total_leng);
        //}while((read_leng > 0) /*|| ((total_leng > 0) && (nmea_buf[total_leng-1] != '\n'))*/);
        
        if (read_leng <= 0) 
        {
            LOGD(TAG"ERROR reading from socket\n");
            sprintf(gps->info, "GPS failed!\n");
            gps->exit_thd = true;
        }
        else if(total_leng > 0)
        {
            NMEA_Parse((char*)&nmea_buf[0]);

            ptr  = info;            
            if(((g_gpsInfo.FixType != 0) && (ttff != 0)/*avoid prev second's NMEA*/) || (fixed == 1)) // 2D or 3D fixed
            {
                ptr += sprintf(ptr, "GPS TTFF %d sec, fixed!\n", ttff);
                fixed = 1;
            }
            else if((g_gpsInfo.FixType != 0) && (ttff == 0)) //skip prev second's NMEA, clear data
            {
                ptr += sprintf(ptr, "GPS TTFF %d sec\n", ttff++);
                memset(&g_gpsInfo, 0, sizeof(g_gpsInfo));
                memset(g_svInfo, 0, (sizeof(SVInfo)*NUM_CH));
                memset(g_chInfo, 0, (sizeof(ChInfo)*PSEUDO_CH));
            }
            else    // no fix
            {
                ptr += sprintf(ptr, "GPS TTFF %d sec\n", ttff++);
            }

            for(i = 0; i < g_gpsInfo.SV_cnt; i++)
            {
                ptr += sprintf(ptr, "SVid[%d] : %d\n", g_svInfo[i].SVid, g_svInfo[i].SNR);
            }
        }
    }

    return;
}

static void *gps_update_iv_thread(void *priv)
{
    struct gps_desc *gps = (struct gps_desc *)priv;
    struct itemview *iv = gps->iv;
    int count = 1, chkcnt = 10;
    int init_status;

    LOGD(TAG "%s: Start\n", __FUNCTION__);
    //init GPS driver
    memset(gps->info, '\n', INFO_SIZE);
    sprintf(gps->info, "GPS initializing...\n");
    iv->redraw(iv);
    sleep(1);
    init_status = GPS_Open();
    if(init_status != 0)    // GPS init fail
    {
        memset(gps->info, '\n', INFO_SIZE);
        sprintf(gps->info, "GPS failed! (%d)\n", init_status);
        iv->redraw(iv);
    }
    else
    {
        //init GPS driver done
        ttff = 0;
        fixed = 0;
        memset(gps->info, '\n', INFO_SIZE);
        iv->redraw(iv);
        
        while (1) {
            usleep(100000); // wake up every 0.1sec
            chkcnt--;

            if (gps->exit_thd)
            {
                LOGD(TAG "%s, gps->exit_thd = true\n", __FUNCTION__);
                break;
            }

            if(httff == 1)
            {
                httff = 0;
                write(sockfd, "$PMTK101*32\r\n", sizeof("$PMTK101*32\r\n"));
                ttff = 0;
                fixed = 0;
                memset(gps->info, '\n', INFO_SIZE);
                gps->info[INFO_SIZE-1] = 0x0;
                iv->redraw(iv);
            }

            if(cttff == 1)
            {
                cttff = 0;
                write(sockfd, "$PMTK103*30\r\n", sizeof("$PMTK103*30\r\n") );
                ttff = 0;
                fixed = 0;
                memset(gps->info, '\n', INFO_SIZE);
                gps->info[INFO_SIZE-1] = 0x0;
                iv->redraw(iv);
            }

            if (chkcnt > 0)
                continue;

            chkcnt = 10;

            gps_update_info(gps, gps->info);
            iv->redraw(iv);
        }
    }
    //close GPS driver
    GPS_Close();
    //close GPS driver done
    LOGD(TAG "%s: Exit\n", __FUNCTION__);
    pthread_exit(NULL);

	return NULL;
}

int gps_entry(struct ftm_param *param, void *priv)
{
    char *ptr;
    int chosen;
    bool exit = false;
    struct gps_desc *gps = (struct gps_desc *)priv;
    struct itemview *iv;

    LOGD(TAG "%s\n", __FUNCTION__);

    init_text(&gps->title, param->name, COLOR_YELLOW);
    init_text(&gps->text, &gps->info[0], COLOR_YELLOW);
    
    gps_update_info(gps, gps->info);
   
    gps->exit_thd = false;

    if (!gps->iv) {
        iv = ui_new_itemview();
        if (!iv) {
            LOGD(TAG "No memory");
            return -1;
        }
        gps->iv = iv;
    }
    
    iv = gps->iv;
    iv->set_title(iv, &gps->title);
    iv->set_items(iv, gps_items, 0);
    iv->set_text(iv, &gps->text);
    
    pthread_create(&gps->update_thd, NULL, gps_update_iv_thread, priv);
    do {
        chosen = iv->run(iv, &exit);
        LOGD(TAG "%s, chosen = %d\n", __FUNCTION__, chosen);
        switch (chosen) {
        case ITEM_HTTFF:
            httff = 1;
            break;
        case ITEM_CTTFF:
            cttff = 1;
            break;
        case ITEM_PASS:
        case ITEM_FAIL:
            if (chosen == ITEM_PASS) {
                gps->mod->test_result = FTM_TEST_PASS;
            } else if (chosen == ITEM_FAIL) {
                gps->mod->test_result = FTM_TEST_FAIL;
            }           
            exit = true;
            break;
        }
        
        if (exit) {
            gps->exit_thd = true;
            LOGD(TAG "%s, gps->exit_thd = true\n", __FUNCTION__);
            break;
        }        
    } while (1);
    pthread_join(gps->update_thd, NULL);

    return 0;
}

int gps_init(void)
{
    int ret = 0;
    struct ftm_module *mod;
    struct gps_desc *gps;

    LOGD(TAG "%s\n", __FUNCTION__);
    
    mod = ftm_alloc(ITEM_GPS, sizeof(struct gps_desc));
    gps  = mod_to_gps(mod);

    gps->mod      = mod;

    if (!mod)
    {
        return -ENOMEM;
    }

    ret = ftm_register(mod, gps_entry, (void*)gps);

    return ret;
}

#endif

