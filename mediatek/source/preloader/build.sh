#!/bin/bash
##############################################################
# Program:
# Program to create ALPS preloader binary
#

function build_preloader () {

    if [ "$1" != "" ]; then export TARGET_PRODUCT=$1; fi

    source ../../../mediatek/build/shell.sh ../../../ preloader
    CUR_DIR=`pwd`

    ##############################################################
    # Variable Initialization
    #
    
    PL_IMAGE=bin/preloader_${MTK_PROJECT}.bin
    PL_ELF_IMAGE=bin/preloader_${MTK_PROJECT}.elf

    ##############################################################
    # Binary Generation
    #

    make

    if [ ! -f "${PL_IMAGE}" ]; then echo "BUILD FAIL."; exit 1; fi

    PL_FUN_MAP=function.map
    
   source ${MTK_PATH_PLATFORM}/check_size.sh
}

function ns_chip () {

    ##############################################################
    # Only Support Non-Secure Chip
    #        

    echo ""
    echo "[ Only for Non-Secure Chip ]"
    echo "============================================"
    
    GFH_PATH=${CHIP_CONFIG_PATH}/ns/  
    
    ##############################################################
    # INITIALIZE GFH
    #
    
    if [ "${MTK_EMMC_SUPPORT}" == "yes" ]; then
        GFH_INFO=${GFH_PATH}/GFH_INFO_EMMC.txt
    else
        GFH_INFO=${GFH_PATH}/GFH_INFO.txt
    fi
    GFH_HASH=${GFH_PATH}/GFH_HASH.txt

    ##############################################################
    # ATTACH GFH
    #

    echo ""      
    echo "[ Attach ${MTK_PLATFORM} GFH ]"
    echo "============================================"
    echo " : GFH_INFO             - ${GFH_INFO}"
    echo " : GFH_HASH             - ${GFH_HASH}"

    chmod u+w ${PL_IMAGE}
    mv -f ${PL_IMAGE} ${PL_IMAGE/%.bin/_NO_GFH.bin}
    cp -f ${GFH_INFO} ${PL_IMAGE}	    
    chmod u+w ${PL_IMAGE} ${PL_IMAGE/%.bin/_NO_GFH.bin}
    cat ${PL_IMAGE/%.bin/_NO_GFH.bin} >> ${PL_IMAGE}
    cat ${GFH_HASH} >> ${PL_IMAGE}

    ##############################################################
    # PROCESS BOOT LOADER
    #

    chmod 777 ${PBP_TOOL}/PBP.exe
    
    WINEPATH=`which wine`

    if [ -n "$WINEPATH" ]; then	
	wine ${PBP_TOOL}/PBP.exe ${PL_IMAGE}
        if [ $? -eq 0 ] ; then
            echo "${PBP_TOOL}/PBP.exe pass !!!!"
        else
            echo "===BUILD FAIL. ${PBP_TOOL}/PBP.exe return fail==="
            exit 1;
        fi	
    else
    	echo "===BUILD FAIL. need wine to execution PBP.exe==="
	echo "===Please check your environment variable!===" 
	exit 1;
    fi 
}

function s_chip_support () {

    ##############################################################
    # Can Support Secure Chip
    #

    echo ""
    echo "[ Enable Secure Chip Support ]"
    echo "============================================"

    GFH_PATH=${CHIP_CONFIG_PATH}/s/gfh    
    CONFIG_PATH=${CHIP_CONFIG_PATH}/s/cfg
    KEY_PATH=${CHIP_CONFIG_PATH}/s/key
    
    ##############################################################
    # INITIALIZE CONFIG and KEY
    #
    
    CHIP_CONFIG=${CONFIG_PATH}/CHIP_CONFIG.ini
    CHIP_KEY=${KEY_PATH}/CHIP_TEST_KEY.ini
    
    ##############################################################
    # INITIALIZE GFH
    #
    
    if [ "${MTK_EMMC_SUPPORT}" == "yes" ]; then
        GFH_INFO=${GFH_PATH}/GFH_INFO_EMMC.txt
    else
    	GFH_INFO=${GFH_PATH}/GFH_INFO.txt
    fi
    GFH_SEC_KEY=${GFH_PATH}/GFH_SEC_KEY.txt
    GFH_ANTI_CLONE=${GFH_PATH}/GFH_ANTI_CLONE.txt
    GFH_HASH_SIGNATURE=${GFH_PATH}/GFH_HASH_AND_SIG.txt
    GFH_PADDING=${GFH_PATH}/GFH_PADDING.txt

    source ${CONFIG_PATH}/SECURE_JTAG_CONFIG.ini
    if [ "${SECURE_JTAG_ENABLE}" == "TRUE" ]; then
        SECURE_JTAG_GFH=${GFH_PATH}/GFH_SEC_CFG_JTAG_ON.txt
        echo " : SECURE_JTAG_ENABLE - TRUE"
    elif [ "${SECURE_JTAG_ENABLE}" == "FALSE" ]; then
        SECURE_JTAG_GFH=${GFH_PATH}/GFH_SEC_CFG_JTAG_OFF.txt
        echo " : SECURE_JTAG_ENABLE - FALSE"
    else
        echo "BUILD FAIL. SECURE_JTAG_ENABLE not defined in ${CONFIG_PATH}/SECURE_JTAG_CONFIG.ini"
        exit 1;
    fi

    ##############################################################
    # ATTACH GFH
    #

    echo ""      
    echo "[ Attach ${MTK_PLATFORM} GFH ]"
    echo "============================================"
    echo " : GFH_INFO             - ${GFH_INFO}"
    echo " : GFH_SEC_KEY          - ${GFH_SEC_KEY}"
    echo " : GFH_ANTI_CLONE       - ${GFH_ANTI_CLONE}"
    echo " : GFH_JTAG_CFG         - ${SECURE_JTAG_GFH}"
    echo " : GFH_PADDING          - ${GFH_PADDING}"
    echo " : GFH_HASH_SIGNATURE   - ${GFH_HASH_SIGNATURE}"

    chmod u+w ${PL_IMAGE}
    mv -f ${PL_IMAGE} ${PL_IMAGE/%.bin/_NO_GFH.bin}
    cp -f ${GFH_INFO} ${PL_IMAGE}	    
    chmod 777 ${PL_IMAGE}
    cat ${GFH_SEC_KEY} >> ${PL_IMAGE}
    cat ${GFH_ANTI_CLONE} >> ${PL_IMAGE}
    cat ${SECURE_JTAG_GFH} >> ${PL_IMAGE}
    cat ${GFH_PADDING} >> ${PL_IMAGE}
    chmod u+w ${PL_IMAGE} ${PL_IMAGE/%.bin/_NO_GFH.bin}
    cat ${PL_IMAGE/%.bin/_NO_GFH.bin} >> ${PL_IMAGE}
    cat ${GFH_HASH_SIGNATURE} >> ${PL_IMAGE}

    echo ""
    echo "[ Load Configuration ]"
    echo "============================================"
    echo " : CONFIG               - ${CHIP_CONFIG}"
    echo " : RSA KEY              - ${CHIP_KEY}"	
    echo " : AC_K                 - ${CHIP_KEY}"

    ##############################################################
    # PROCESS BOOT LOADER
    #

    chmod 777 ${PBP_TOOL}/PBP.exe

    WINEPATH=`which wine` 
    if [ -n "$WINEPATH" ]; then
        wine ${PBP_TOOL}/PBP.exe -m ${CHIP_CONFIG} -i ${CHIP_KEY} ${PL_IMAGE}
        if [ $? -eq 0 ] ; then
            echo "${PBP_TOOL}/PBP.exe pass !!!!"
        else
            echo "===BUILD FAIL. ${PBP_TOOL}/PBP.exe return fail==="
            exit 1;
        fi
    else            
    	echo "===BUILD FAIL. need wine to execution PBP.exe==="
	echo "===Please check your environment variable!===" 
	exit 1;
    fi
} 

function key_encode () {

    ##############################################################
    # Encode Key
    #

    KEY_ENCODE_TOOL=tools/ke/KeyEncode
    chmod 777 ${KEY_ENCODE_TOOL}
    if [ -e ${KEY_ENCODE_TOOL} ]; then    

        ./${KEY_ENCODE_TOOL} ${PL_IMAGE} KEY_ENCODED_PL
        
        if [ $? -eq 0 ] ; then
            echo "${KEY_ENCODE_TOOL} pass !!!!"
        else
            echo "===BUILD FAIL. ${KEY_ENCODE_TOOL} return fail==="
            exit 1;
        fi 
        
        if [ -e KEY_ENCODED_PL ]; then    
            rm ${PL_IMAGE}
            mv KEY_ENCODED_PL ${PL_IMAGE}
        fi
    fi
}

function post_process () {

    ##############################################################
    # Binary Secure Postprocessing
    #        

    PBP_TOOL=tools/pbp
    CUSTOM_PATH=${MTK_ROOT_CUSTOM}/${MTK_PROJECT}/security
    CHIP_CONFIG_PATH=${CUSTOM_PATH}/chip_config
    WINEPATH=`which wine`
    if [ -e ${PBP_TOOL}/PBP.exe ]; then

        echo ""
        echo "[ Pre-loader Post Processing ]"
        echo "============================================"
        if [ -z "$WINEPATH" ]; then
            echo "===BUILD FAIL. need wine tool to sign pre-loader binary==="
	    echo "===Please check your environment variable!===" 
            exit 1;
        fi

        ##############################################################
        # ENCODE KEY FIRST
        #        
        key_encode;

        ##############################################################
        # CHECK CHIP TYPE
        #        

        if [ -e ${CHIP_CONFIG_PATH}/s/gfh/GFH_INFO.txt ] || [ -e ${CHIP_CONFIG_PATH}/s/gfh/GFH_INFO_EMMC.txt ]; then
            
            echo ""
            echo "[ Load Chip Config. ]"
            echo "============================================"                        
            echo " : MTK_SEC_CHIP_SUPPORT - ${MTK_SEC_CHIP_SUPPORT}"                 

            if [ "${MTK_SEC_CHIP_SUPPORT}" == "no" ]; then

                ##############################################################
                # ONLY SUPPORT NON-SECURE CHIP
                #

                CHIP_CONFIG_PATH=${MTK_PATH_PLATFORM}/gfh/default
                ns_chip;
                
            elif [ "${MTK_SEC_CHIP_SUPPORT}" == "yes" ]; then

                ##############################################################
                # CAN SUPPORT SECURE CHIP
                #
                
                CHIP_CONFIG_PATH=${CUSTOM_PATH}/chip_config
                s_chip_support;
                
            else
   
                echo "BUILD FAIL. MTK_SEC_CHIP_SUPPORT not defined in ProjectConfig.mk"
                exit 1;
            fi
            
        else

            ##############################################################
            # NO CONFIGURATION IS FOUND. APPLY DEFAULT SETTING
            #
            
            echo "${CHIP_CONFIG_PATH}/s/gfh/GFH_INFO.txt not found."
            echo "Suppose it is non-secure chip and apply default config."
            CHIP_CONFIG_PATH=${MTK_PATH_PLATFORM}/gfh/default
            ns_chip;
        fi            
    fi
}

function dump_build_info () {

    ##############################################################
    # Dump Message
    #

    echo ""          
    echo "============================================"
    echo "${MTK_PROJECT} preloader load"
    echo "${PL_IMAGE} built at"
    echo "time : $(date)"
    echo "img size : $(stat -c%s "${PL_IMAGE}")" byte
    echo "bss size : 0x$(readelf -SW "${PL_ELF_IMAGE}"|grep "bss" | awk '{print $6}')" byte
    echo "============================================"

    PL_ELF_IMAGE=bin/preloader_${MTK_PROJECT}.elf

    chmod a+w ${PL_IMAGE}
    cp -f ${PL_IMAGE} .
}

function copy_binary () {

    ##############################################################
    # Copy Binary to Output Direcory
    #

    copy_to_legacy_download_flash_folder   ${PL_IMAGE}
}


##############################################################
# Main Flow
#
build_preloader;
post_process;
dump_build_info;
copy_binary;
