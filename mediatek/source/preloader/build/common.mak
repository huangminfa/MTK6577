##############################################################
# Include MakeFile
##############################################################

include $(MTK_PATH_PLATFORM)/makefile.mak

##############################################################
# Initialize Variables
##############################################################

C_FILES   		:= $(filter %.c, $(MOD_SRC))
ASM_FILES 		:= $(filter %.s, $(MOD_SRC)) 
DA_VERIFY_FILES   	:= $(filter %.c, $(DA_VERIFY_SRC))

OBJS_FROM_C 		:= $(C_FILES:%.c=%.o)
OBJS_FROM_ASM 		:= $(ASM_FILES:%.s=%.o)
DA_VERIFY_OBJS_FROM_C 	:= $(DA_VERIFY_SRC:%.c=%.o)

All_OBJS 		+= OBJS_FROM_C
All_OBJS 		+= OBJS_FROM_ASM
All_OBJS 		+= DA_VERIFY_OBJS_FROM_C

OBJS_FROM_C 		:= $(addprefix $(MOD_OBJ)/, $(C_FILES:%.c=%.o))
OBJS_FROM_ASM 		:= $(addprefix $(MOD_OBJ)/, $(ASM_FILES:%.s=%.o))
DA_VERIFY_OBJS_FROM_C 	:= $(addprefix $(DA_VERIFY_OBJ)/, $(DA_VERIFY_FILES:%.c=%.o))

##############################################################
# Specify Builld Command
##############################################################

define COMPILE_C
			$(CC) $(C_OPTION) -o $@ $<
endef

define COMPILE_ASM
			$(CC) $(AFLAGS) -o $@ $<
endef

##############################################################
# Main Flow
##############################################################

all: show $(OBJS_FROM_C) $(OBJS_FROM_ASM) $(DA_VERIFY_OBJS_FROM_C)

show:
	@echo .......... Complete .........

$(OBJS_FROM_C) : $(MOD_OBJ)/%.o : %.c
			$(COMPILE_C)	

$(OBJS_FROM_ASM) : $(MOD_OBJ)/%.o : %.s
			$(COMPILE_ASM)

$(DA_VERIFY_OBJS_FROM_C) : $(DA_VERIFY_OBJ)/%.o : %.c
			$(COMPILE_C)
