#!/bin/bash
# ##########################################################

# Add MediaTek developed Python libraries path into PYTHONPATH
if [ -z "$PYTHONPATH" ]; then
  PYTHONPATH=$PWD/mediatek/build/tools
else
  PYTHONPATH=$PWD/mediatek/build/tools:$PYTHONPATH
fi
export PYTHONPATH

ANDROID_EABI_TOOLCHAIN=$PWD/prebuilt/linux-x86/toolchain/arm-linux-androideabi-4.4.x/bin
ARM_EABI_TOOLCHAIN=$PWD/prebuilt/linux-x86/toolchain/arm-eabi-4.4.3/bin

export ARM_EABI_TOOLCHAIN
export ANDROID_EABI_TOOLCHAIN

PATH=$ARM_EABI_TOOLCHAIN:$ANDROID_EABI_TOOLCHAIN:$PATH
export PATH

#alias arm-linux-androideabi-gcc='$ANDROID_EABI_TOOLCHAIN/arm-linux-androideabi-gcc'
