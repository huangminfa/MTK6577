#define LOG_TAG "losetup"

#include <sys/types.h>

#include <sys/stat.h>

#include <fcntl.h>

#include <unistd.h>

#include <string.h>

#include <stdio.h>

#include <errno.h>

#include <stdlib.h>

#include <linux/loop.h>

 

typedef struct loop_info64 bb_loop_info;

#define BB_LOOP_SET_STATUS LOOP_SET_STATUS64

#define BB_LOOP_GET_STATUS LOOP_GET_STATUS64

 

static int setloop(char *device, const char *file, unsigned long long offset)
{

    bb_loop_info loopinfo;

    struct stat statbuf;

    int dfd, ffd, mode, rc = -1;

    /* Open the file. */

    mode = O_RDWR;

    ffd = open(file, mode);

    if (ffd < 0)

        return -errno;

    /* Ran out of block devices, return failure.  */

    if (stat(device, &statbuf) || !S_ISBLK(statbuf.st_mode)) {

        return -errno;

    }

    /* Open the sucker and check its loopiness.  */

    dfd = open(device, mode);

    if (dfd < 0)

        return -errno;

    rc = ioctl(dfd, BB_LOOP_GET_STATUS, &loopinfo);

    /* If device is free, claim it.  */

    if (rc && errno == ENXIO) {

        memset(&loopinfo, 0, sizeof(loopinfo));

        strncpy((char *)loopinfo.lo_file_name, file, LO_NAME_SIZE);

        loopinfo.lo_offset = offset;

        /* Associate free loop device with file.  */

        if (!ioctl(dfd, LOOP_SET_FD, ffd)) {

            if (!ioctl(dfd, BB_LOOP_SET_STATUS, &loopinfo))

                rc = 0;

            else

                ioctl(dfd, LOOP_CLR_FD, 0);

        }

    }

    else

        return -errno;

    close(dfd);

    close(ffd);

    return rc;

}

 

 

int main(int nargs, char **args) {

    /* max 2 args,  no option*/

    if (nargs != 3)

        return -1;

    if (setloop(args[1], args[2], 0) < 0)

        return -2;

    return 0;

}
