# all framework module add their path via MTK_FRAMEWORK_BASE += $(my-dir)

MTK_ALL_FRAMEWORK             := 
MTK_MPATHMAP_MK               := $(wildcard $(shell find mediatek/source/frameworks/*/ | grep "/mpathmap.mk$$"))
$(foreach mk,$(MTK_MPATHMAP_MK),$(eval include $(mk))$(eval MTK_ALL_FRAMEWORK += $(MTK_FRAMEWORK)))
MTK_ALL_FRAMEWORK             := $(addprefix ../../,$(MTK_ALL_FRAMEWORK))
MTK_ALL_FRAMEWORK             := $(addsuffix /java,$(MTK_ALL_FRAMEWORK))
MTK_FRAMEWORKS_BASE_JAVA_SRC_DIRS += $(MTK_ALL_FRAMEWORK)

