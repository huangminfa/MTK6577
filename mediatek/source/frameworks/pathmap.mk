# all framework module add their path via MTK_FRAMEWORK_BASE += $(my-dir)

MTK_ALL_FRAMEWORK_BASE        := 
MTK_PATHMAP_MK                := $(wildcard $(shell find mediatek/source/frameworks/*/ | grep "/pathmap.mk$$"))
$(foreach mk,$(MTK_PATHMAP_MK),$(eval include $(mk))$(eval MTK_ALL_FRAMEWORK_BASE += $(MTK_FRAMEWORK_BASE)))
MTK_ALL_FRAMEWORK_BASE        := $(addprefix mediatek/source/frameworks/,$(MTK_ALL_FRAMEWORK_BASE))
MTK_ALL_FRAMEWORK_BASE        := $(addsuffix /java,$(wildcard $(MTK_ALL_FRAMEWORK_BASE)))
FRAMEWORKS_BASE_SUBDIRS       += $(addprefix ../../, $(MTK_ALL_FRAMEWORK_BASE))
FRAMEWORKS_BASE_JAVA_SRC_DIRS += $(MTK_ALL_FRAMEWORK_BASE)

#For Code Partition
MTK_ALL_FRAMEWORK_BASE        := 
MTK_PATHMAP_MK                := $(wildcard $(shell find mediatek/protect/source/frameworks/*/ | grep "/pathmap.mk$$"))
$(foreach mk,$(MTK_PATHMAP_MK),$(eval include $(mk))$(eval MTK_ALL_FRAMEWORK_BASE += $(MTK_FRAMEWORK_BASE)))
MTK_ALL_FRAMEWORK_BASE        := $(addprefix mediatek/protect/source/frameworks/,$(MTK_ALL_FRAMEWORK_BASE))
MTK_ALL_FRAMEWORK_BASE        := $(addsuffix /java,$(wildcard $(MTK_ALL_FRAMEWORK_BASE)))
FRAMEWORKS_BASE_SUBDIRS       += $(addprefix ../../, $(MTK_ALL_FRAMEWORK_BASE))
FRAMEWORKS_BASE_JAVA_SRC_DIRS += $(MTK_ALL_FRAMEWORK_BASE)
