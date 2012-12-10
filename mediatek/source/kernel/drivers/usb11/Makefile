obj-$(CONFIG_MTK_USBFSH) := musbfsh_host.o musbfsh_virthub.o

ifneq ($(CONFIG_MUSBFSH_PIO_ONLY),y)
obj-$(CONFIG_MTK_USBFSH) += musbfsh_hsdma.o
endif

ccflags-y += -I$(MTK_PATH_PLATFORM)/drivers/usb11/ -I$(MTK_PATH_SOURCE)/drivers/usb11/