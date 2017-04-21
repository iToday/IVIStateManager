#include "Netlink.h"

#define TAG "NetLink"

#include "../../include/ilog.h"

namespace android{

	Netlink::Netlink(NetlinkListener * listener){
		mListener = listener;
		mSocketFd = -1;
	}

	int Netlink::Open(int channel){

	    int state;
	    struct sockaddr_nl src_addr, dest_addr;
	    struct msghdr msg;

	    int retval;
	    int state_smg = 0;

	    // Create a socket
	    mSocketFd = socket(AF_NETLINK, SOCK_RAW, channel);
	    if(mSocketFd == -1){
	    	LOGI("error getting socket: %s", strerror(errno));
	        return -1;
	    }

	    // To prepare binding
	    memset(&msg,0,sizeof(msg));
	    memset(&src_addr, 0, sizeof(src_addr));
	    src_addr.nl_family = AF_NETLINK;
	    src_addr.nl_pid = getpid(); // self pid

	    src_addr.nl_groups = 1; // multi cast

	    retval = bind(mSocketFd, (struct sockaddr*)&src_addr, sizeof(src_addr));
	    if(retval < 0){
	    	LOGI("bind failed socket: %s", strerror(errno));
	        close(mSocketFd);
	        return -1;
	    }

	    if (pthread_create(&mReadThread, NULL, read_thread_func, (void *)this) != 0){
	    	LOGI("NetLink create read thread failed  \n");
	    }
	    return 0;
	}

	int Netlink::Send(unsigned char* buffer, int len){

		struct nlmsghdr *nlh = NULL;
		struct iovec iov;
		struct msghdr msg;
		struct sockaddr_nl src_addr, dest_addr;
		int state_smg = 0;

		nlh = (struct nlmsghdr *)malloc(NLMSG_SPACE(1024));
		if(!nlh){
			LOGI("malloc nlmsghdr error!\n");
			close(mSocketFd);
			return -1;
		}

		memset(&dest_addr,0,sizeof(dest_addr));
		dest_addr.nl_family = AF_NETLINK;
		dest_addr.nl_pid = 0;
		dest_addr.nl_groups = 0;

		nlh->nlmsg_len = NLMSG_SPACE(1024);
		nlh->nlmsg_pid = getpid();
		nlh->nlmsg_flags = 0;

		memcpy(NLMSG_DATA(nlh), buffer, len);

		iov.iov_base = (void *)nlh;
	    iov.iov_len = NLMSG_SPACE(1024);
		// iov.iov_len = nlh->nlmsg_len;

		memset(&msg, 0, sizeof(msg));

		msg.msg_name = (void *)&dest_addr;
		msg.msg_namelen = sizeof(dest_addr);
		msg.msg_iov = &iov;
		msg.msg_iovlen = 1;

		LOGI("state_smg\n");

		state_smg = sendmsg(mSocketFd,&msg,0);

		if(state_smg == -1)
		{
			LOGI("get error sendmsg = %s\n",strerror(errno));
		}

		memset(nlh,0,NLMSG_SPACE(1024));

		return state_smg;
	}

	int Netlink::Close(){

		if (mSocketFd >= 0)
			close(mSocketFd);

		return 0;
	}

	int Netlink::Recv(unsigned char* buffer, int len){

		struct nlmsghdr *nlh = NULL;
		struct iovec iov;
		struct msghdr msg;

		int state = 0;

		nlh = (struct nlmsghdr *)malloc(NLMSG_SPACE(1024));
		if(!nlh){
			LOGI("malloc nlmsghdr error!\n");
			close(mSocketFd);
			return -1;
		}

//		memset(&dest_addr,0,sizeof(dest_addr));
//		dest_addr.nl_family = AF_NETLINK;
//		dest_addr.nl_pid = 0;
//		dest_addr.nl_groups = 0;

//		nlh->nlmsg_len = NLMSG_SPACE(1024);
//		nlh->nlmsg_pid = getpid();
//		nlh->nlmsg_flags = 0;

		iov.iov_base = (void *)nlh;
		iov.iov_len = NLMSG_SPACE(1024);
		// iov.iov_len = nlh->nlmsg_len;

		memset(&msg, 0, sizeof(msg));

		//msg.msg_name = (void *)&dest_addr;
		//msg.msg_namelen = sizeof(dest_addr);
		msg.msg_iov = &iov;
		msg.msg_iovlen = 1;

		memset(nlh,0,NLMSG_SPACE(1024));

		state = recvmsg(mSocketFd, &msg, 0);
		if(state<0)
		{
			LOGI("state<1");
		}

		memcpy(buffer, NLMSG_DATA(nlh), state);

		return state;
	}

	void* Netlink::read_thread_func(void* args){

		Netlink *link = (Netlink*)args;

		LOGI("Netlink::read_thread_func");

		while (1){
			unsigned char buffer[1024] = {0};

			int len = link->Recv(buffer, 1024);

			if (len > 0 && link->mListener != NULL){
				link->mListener->onNewData(buffer, len);
			}
		}

		return NULL;
	}
}
