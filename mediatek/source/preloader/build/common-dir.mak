include $(MTK_PATH_PLATFORM)/makefile.mak

all: $(SUBDIRS)
.PHONY: $(SUBDIRS)

show_title:
	@echo  ======================================
	@echo       Src Directory : $(SUBDIRS)
	@echo  ======================================

$(SUBDIRS):
	@make -e -r -C $@ --no-print-directory
