include $(MTK_PATH_PLATFORM)/makefile.mak

C_FILES   := $(filter %.c, $(MOD_SRC))
ASM_FILES := $(filter %.s, $(MOD_SRC)) 

OBJS_FROM_C := $(C_FILES:%.c=%.o)
OBJS_FROM_ASM := $(ASM_FILES:%.s=%.o)

All_OBJS += OBJS_FROM_C
All_OBJS += OBJS_FROM_ASM

OBJS_FROM_C := $(addprefix $(MOD_OBJ)/, $(C_FILES:%.c=%.o))
OBJS_FROM_ASM := $(addprefix $(MOD_OBJ)/, $(ASM_FILES:%.s=%.o))


define COMPILE_C
	@echo [CC] $@
	@$(CC) $(C_OPTION_NO_OPTIMIZE) -o $@ $<
endef

define COMPILE_ASM
	@echo [AS] $@
	@$(CC) $(AFLAGS_NO_OPTIMIZE) -o $@ $<
endef

all: show $(OBJS_FROM_C) $(OBJS_FROM_ASM) 

show:
	@echo .......... Complete .........

$(OBJS_FROM_C) : $(MOD_OBJ)/%.o : %.c
	$(COMPILE_C)	

$(OBJS_FROM_ASM) : $(MOD_OBJ)/%.o : %.s
	$(COMPILE_ASM)
