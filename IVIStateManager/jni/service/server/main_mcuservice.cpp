#include "McuService.h"
using namespace android;


int main(int arg, char** argv)
{
	
	if (arg >= 2){
			McuService::setDevice(argv[1]);
	}
	
	McuService::publishAndJoinThreadPool();
	return 0;
}
