if [ -f "${PL_IMG}" ]; then
  PL_SIZE=$(stat -c%s "${PL_IMG}")
  if [ ${PL_SIZE} -gt 75000 ]; then
    echo "===================== building 'fail' ========================"
    echo "---------------------------------------------------------------"
    echo "image size : ${PL_SIZE} cannot be greater than 74000 bytes !!"
    echo "please reduce your code size first then compile again !!"
    echo "==============================================================="
    size `ls out/*.o` > pl-code-size-report.txt
    echo "---------------------------------------------------------------"
    echo "                      CODE SIZE REPORT                         "
    echo "---------------------------------------------------------------"
    echo "size{bytes}     file  { size > 2500 bytes }"
    echo "---------------------------------------------------------------"
    awk '$prj ~ /^[0-9]/ { if {$4>2500} print $4 "\t\t" $6}' < pl-code-size-report.txt | sort -rn
    rm ${PL_IMG}
    echo "BUILD FAIL !!!!!!!!!!!!!!!!"
    echo "BUILD FAIL !!!!!!!!!!!!!!!!"
    exit 1;
  fi
  if [ -f "${MTK_PATH_PLATFORM}/gfh_info.txt" ]; then
    echo ""
    echo "Attach GFH ... "
    echo "----------------------"
    chmod 777 bin/${PL_IMG_NAME}.bin
    mv bin/${PL_IMG_NAME}.bin bin/${PL_IMG_NAME}_NO_GFH.bin
    chmod 777 ${MTK_PATH_PLATFORM}/gfh_info.txt
    cp ${MTK_PATH_PLATFORM}/gfh_info.txt bin/${PL_IMG_NAME}.bin
    cat bin/${PL_IMG_NAME}_NO_GFH.bin >> bin/${PL_IMG_NAME}.bin
    cat ${MTK_PATH_PLATFORM}/gfh_hash.txt >> bin/${PL_IMG_NAME}.bin
    echo ""
    echo "Sign Pre-loader ... "
    echo "----------------------"
    /usr/local/wine-1.1.33-i686/bin/wine tools/PBP/PBP.exe bin/${PL_IMG_NAME}.bin
  fi
  if [ ! -d ${IMG_DIR} ];   then mkdir ${IMG_DIR};   fi
  if [ ! -d ${FLASH_DIR} ]; then mkdir ${FLASH_DIR}; fi
  echo ""
  echo ============================================
  echo "${_BOARD} preloader load"
  echo "'bin/${PL_IMG_NAME}.bin' built at"
  echo "time : ${date}"
  echo ============================================
  chmod 777 bin/${PL_IMG_NAME}.bin
  chmod 777 ${PL_IMG_NAME}.bin
  cp bin/${PL_IMG_NAME}.bin ${PL_IMG_NAME}.bin
  rm -rf ${PL_DOWNLOAD_IMG}
  chmod 777 ${PL_IMG_NAME}.bin
  cp ${PL_IMG_NAME}.bin ${PL_DOWNLOAD_IMG}
fi

