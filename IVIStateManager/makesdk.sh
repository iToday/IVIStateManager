#!/bin/sh

IVI_DIR=$(cd `dirname $0`; pwd)

echo "IVI_DIR is : $IVI_DIR"
echo "ANDROID is : $ANDROID_BUILD_TOP"

MANIFEST_FILE=$IVI_DIR/MANIFEST.MF
IVI_SRC_FILE=$ANDROID_BUILD_TOP/out/target/common/obj/JAVA_LIBRARIES/IVIMainSDK_intermediates/classes
SDK_PATH=$IVI_DIR/sdk

if [ -d $IVI_SRC_FILE -a -d $ANDROID_BUILD_TOP ]; then
	
	mkdir -p $SDK_PATH
	chmod a+x $SDK_PATH

	jar cvfm $SDK_PATH/IVIMainSDK.jar $MANIFEST_FILE -C $IVI_SRC_FILE .

	chmod a+x -R $SDK_PATH
	
else
	echo "IVIMainSDK or Android dir not set"
	echo "please run command : source ./selfenv before it"
	echo "e.g. source ./selfenv sd2"
fi
