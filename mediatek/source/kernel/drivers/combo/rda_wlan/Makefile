ccflags-y := -I$(src)/../common/include -I$(src)/../common/linux/include

obj-m := rda5890.o
rda5890-objs := \
	rda5890_if_sdio.o \
	rda5890_dev.o \
	rda5890_txrx.o \
	rda5890_wid.o \
	rda5890_wext.o \
	rda5890_debugfs.o\
	rda5890_sdio_patch.o\
	rda5890_nvram.o\
    rda5890_scan.o

