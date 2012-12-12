#include <fcntl.h>
#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <string.h>
#include <termios.h>
#include <time.h>
#include <linux/serial.h>
#include <pthread.h>

#define max_args (10)
#define WLAN_UART "/dev/ttyMT2"
#define COMBO_DEV "/dev/rdacombo"

#define RDA_BT_IOCTL_MAGIC 'u'
#define RDA_WIFI_POWER_SET_TEST_MODE_IOCTL    _IO(RDA_BT_IOCTL_MAGIC ,0x12)
#define RDA_WIFI_POWER_CANCEL_TEST_MODE_IOCTL    _IO(RDA_BT_IOCTL_MAGIC ,0x13)
#define RDA_WIFI_DEBUG_MODE_IOCTL _IO(RDA_BT_IOCTL_MAGIC ,0x14)

static int gFd = -1;
static int wlan_ts_quit = 0;
static pthread_t read_thread = NULL;
static unsigned char * pDev_name = NULL;

int send_cmd_to_combo_drv(int cmd, unsigned char shutdown)
{
    static int fd = -1;

    if(fd <  0)
    {
        if(pDev_name)
            fd = open(pDev_name, O_RDWR);
        else
            fd = open(COMBO_DEV, O_RDWR);
    }
    if (fd < 0) {
        printf("Can't open rdacombo device %s \n", pDev_name?pDev_name:COMBO_DEV);
        return -1;
    }

    if(ioctl(fd, cmd) == -1)
    {
        printf("rdabt_send_cmd_to_drv failed \n");
    }

    if(shutdown)
    {
        close(fd);
        fd = -1;
    }
    printf("%s cmd %x \n", __func__, cmd);
    return 0;
}


static void usage(void)
{
    printf("sk\r\n"
           "setdbga 0001\r\n"
           "utm 2 set transfer mode\n"
           "setr 1(1~54) set speed\n"
           "setch 1(1~13) set channel\n"
           "utttxlen 10~1000 set packet lenght \n");
}

static int uart_speed(int s)
{
	switch (s) {
	case 9600:
		return B9600;
	case 19200:
		return B19200;
	case 38400:
		return B38400;
	case 57600:
		return B57600;
	case 115200:
		return B115200;
	case 230400:
		return B230400;
	case 460800:
		return B460800;
	case 500000:
		return B500000;
	case 576000:
		return B576000;
	case 921600:
		return B921600;
	case 1000000:
		return B1000000;
	case 1152000:
		return B1152000;
	case 1500000:
		return B1500000;
	case 2000000:
		return B2000000;
#ifdef B2500000
	case 2500000:
		return B2500000;
#endif
#ifdef B3000000
	case 3000000:
		return B3000000;
#endif
#ifdef B3500000
	case 3500000:
		return B3500000;
#endif
#ifdef B4000000
	case 4000000:
		return B4000000;
#endif
	default:
		return B57600;
	}
}

int set_speed(int fd, struct termios *ti, int speed)
{
	cfsetospeed(ti, uart_speed(speed));
	cfsetispeed(ti, uart_speed(speed));
	return tcsetattr(fd, TCSANOW, ti);
}
static int init_uart(char *dev)
{
	struct termios ti;
	int fd, i;
	unsigned long flags = 0;

	fd = open(dev, O_RDWR | O_NOCTTY);
	if (fd < 0) {
		perror("Can't open serial port");
		return -1;
	}

	tcflush(fd, TCIOFLUSH);

	if (tcgetattr(fd, &ti) < 0) {
		perror("Can't get port settings");
		return -1;
	}

	cfmakeraw(&ti);

	ti.c_cflag |= CLOCAL;
	ti.c_cflag &= ~CRTSCTS;

	if (tcsetattr(fd, TCSANOW, &ti) < 0) {
		perror("Can't set port settings");
		return -1;
	}

	/* Set initial baudrate */
	if (set_speed(fd, &ti, 115200) < 0) {
		perror("Can't set initial baud rate");
		return -1;
	}

	tcflush(fd, TCIOFLUSH);

    printf("%s fd:%d \n", __func__,fd);
	return fd;
}


struct wl_test_cmd {
	const char *cmd;
	int (*handler)(int argc, char *argv[]);
};

void wlan_test_cmd_sk(int argc, char *argv[])
{
    unsigned char data_to_send[5];
    int len = 0;
    memcpy(data_to_send, "sk\r\n", 4);
    data_to_send[4] = '\0';
    
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));
    
     printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send);
}

void wlan_test_cmd_setdbga(int argc, char *argv[])
{
    unsigned char  data_to_send[35] = "setdbga 0001\r\n";
    unsigned char len = 0;
    
    len = strlen(argv[0]);
    len = len>4?4:len;

    memcpy(data_to_send + 8, argv[0], len);
    
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));

    printf("%x %x \n", data_to_send[12], data_to_send[13]);
    printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send);
}

void wlan_test_cmd_utm(int argc, char *argv[])
{
    unsigned char data_to_send[25] = "utm ";
    unsigned char len = 0;
    
    len = strlen(argv[0]);
    len = len>2?2:len;

    memcpy(data_to_send + 4, argv[0], len);
    memcpy(data_to_send + 4 + len, "\r\n", 2);
    data_to_send[4 + 2 + len] = '\0';
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));

    printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send); 
}

void wlan_test_cmd_setr(int argc, char *argv[])
{
    unsigned char  data_to_send[25] = "setr ";
    unsigned char len = 0;
    
    len = strlen(argv[0]);
    len = len>2?2:len;

    memcpy(data_to_send + 5, argv[0], len);
    memcpy(data_to_send + 5 + len, "\r\n", 2);
    data_to_send[5 + 2 + len] = '\0';
    
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));
    
    printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send); 
}

void wlan_test_cmd_setch(int argc, char *argv[])
{
    unsigned char  data_to_send[25] = "setch ";
    unsigned char len = 0;
    
    len = strlen(argv[0]);
    len = len>2?2:len;

    memcpy(data_to_send + 6, argv[0], len);
    memcpy(data_to_send + 6 + len, "\r\n", 2);
    data_to_send[6 + 2 + len] = '\0';
    
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));
    
    printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send);  
}

void wlan_test_cmd_utttxlen(int argc, char *argv[])
{
    unsigned char data_to_send[25] = "utttxlen ";
    unsigned char len = 0;
    
    len = strlen(argv[0]);
    len = len>4?4:len;

    memcpy(data_to_send + 9, argv[0], len);
    memcpy(data_to_send + 9 + len, "\r\n", 2);
    data_to_send[9 + 2 + len] = '\0';
    
    if(gFd > 0)
        len = write(gFd, data_to_send, strlen(data_to_send));
    
    printf("%s argv:%s len:%d send:%s \n", __func__, argv[0], len,data_to_send);    
}

void wlan_test_cmd_quit(int argc, char *argv[])
{
    wlan_ts_quit = 1;
    printf("%s \n", __func__);
}

void wlan_test_cmd_ftest(int argc, char *argv[])
{
    send_cmd_to_combo_drv(RDA_WIFI_POWER_SET_TEST_MODE_IOCTL, 0);    
    printf("%s \n", __func__);
}

void wlan_test_cmd_wl_debug(int argc, char *argv[])
{
    send_cmd_to_combo_drv(RDA_WIFI_DEBUG_MODE_IOCTL, 0);
    printf("%s \n", __func__);
}

static struct wl_test_cmd wlan_test_commands[] = {
	//{ "sk", wlan_test_cmd_sk},
    //{ "setdbga", wlan_test_cmd_setdbga},
    //{ "utm", wlan_test_cmd_utm},
    //{ "setr", wlan_test_cmd_setr},
    //{ "setch", wlan_test_cmd_setch},
    //{ "utttxlen", wlan_test_cmd_utttxlen},
    {"f_test", wlan_test_cmd_ftest},
    {"wl_debug", wlan_test_cmd_wl_debug},
    { "quit", wlan_test_cmd_quit},
};

static void *event_thread(void *param)
{
    int fd = *((int*)param), rLen = 0;
    unsigned char data_buffer[4096 + 1],print_buffer[4096 +1];
    data_buffer[4096] = '\0';
    print_buffer[4096] = '\0';

    if(fd < 0)
        goto out;
    printf("enter %s \n", __func__);

    while(1)
    {
        rLen = read(fd, data_buffer, 4096);
        data_buffer[rLen] = '\0';
        printf("recev: %s \n", data_buffer);
    }
    
out:
    printf("leave from %s \n", __func__);
    return NULL;
}

int main(int argC, char *argV[])
{
    int argc = 0, i = 0, err;
    char cmdbuf[256], *cmd, *argv[max_args], *pos;
    char uncmdbuf[256];

    usage();
    gFd = init_uart(WLAN_UART);
    
    if(gFd < 0)
    {
        printf("open device failed \n");
        goto out;
    }

    if(argC > 1)
    {
        pDev_name = argV[1];
    }

    err = pthread_create(&read_thread, 0, event_thread, &gFd);
    if(err < 0)
        goto out;

    do{
        printf("> ");
		cmd = fgets(cmdbuf, sizeof(cmdbuf), stdin);

        if (cmd == NULL)
			break;

        memcpy(uncmdbuf, cmdbuf, sizeof(cmdbuf));
		pos = cmd;
		while (*pos != '\0') {
			if (*pos == '\n') {
				*pos = '\0';
				break;
			}
			pos++;
		}
		argc = 0;
		pos = cmd;
		for (;;) {
			while (*pos == ' ')
				pos++;
			if (*pos == '\0')
				break;
			argv[argc] = pos;
			argc++;
			if (argc == max_args)
				break;
			if (*pos == '"') {
				char *pos2 = strrchr(pos, '"');
				if (pos2)
					pos = pos2 + 1;
			}
			while (*pos != '\0' && *pos != ' ')
				pos++;
			if (*pos == ' ')
				*pos++ = '\0';
		}

        if (argc)
        {
            unsigned char handled = 0;
            for(i = 0; i < sizeof(wlan_test_commands)/sizeof(struct wl_test_cmd); i ++ )
            {
                if(!memcmp(argv[0], wlan_test_commands[i].cmd, strlen(wlan_test_commands[i].cmd)))
                {
                    wlan_test_commands[i].handler(argc - 1, &argv[1]);
                    handled = 1;
                }
            }
            if(!handled)
            {
                uncmdbuf[pos - cmd] = '\r';
                uncmdbuf[pos - cmd + 1] = '\n';
                uncmdbuf[pos - cmd + 2] = '\0';
                printf("input unknow cmd %d: %s \n", pos - cmd, uncmdbuf);
                write(gFd, uncmdbuf, pos - cmd + 2);
                sleep(1);
                if(!memcmp(uncmdbuf, "utm 1", 5))
                {
                    write(gFd, "mw 50000080 400\r\n", 17);
                    sleep(1);   
                    write(gFd, "rfregw 36 2f4\r\n", 15);
                    sleep(1); 
                } 
                else if(!memcmp(uncmdbuf, "utm 2", 5))
                {
                	  write(gFd, "rfregw 36 6c02\r\n", 16);
                    sleep(1); 
                }
            }
            
        }
    }while(!wlan_ts_quit);

out:
    printf("wlan_test end\n");
    send_cmd_to_combo_drv(RDA_WIFI_POWER_CANCEL_TEST_MODE_IOCTL, 1);
    if(read_thread)
    {
        pthread_exit(read_thread);
        read_thread = 0;
    }
    if(gFd > 0)
    {
        close(gFd);
        gFd = -1;
    }
    
    return 0;
}




