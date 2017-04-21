#ifndef ANDROID_LISTENER_H
#define ANDROID_LISTENER_H

#include "../listener/IListener.h"

namespace android {
	
	
	template<typename NAME>
	class DeathObserver: public IBinder::DeathRecipient {

	private:

		sp<NAME>& listener;

	public:

		DeathObserver(sp<NAME>& lis):listener(lis) { }


		virtual void binderDied(const wp<IBinder>& who) {
			  //ALOGW("listener died [%p]", who.unsafe_get());
			  listener = NULL;
		}
	};

	/**
	 *
	 */
	class Listener{

	public:

		Listener *next;

		sp<IListener> mListener;

		sp<DeathObserver<IListener> > mDeathObserver;

		int mType;

	public:

		Listener(const sp<IListener>& listener,int type){

			memset(this, 0, sizeof(*this));

			mListener = listener;

			mDeathObserver = new DeathObserver<IListener>(mListener);

			mType = type;
		 }

	};

	/**
	 *
	 */
	class ListenerList {

	private:

		Listener *mList;

		int mCount;

	public:

		ListenerList(): mList(NULL), mCount(0) {

		}

		~ListenerList()  {

			free();
		}

		Listener *insert(Listener *pListener)  {

			pListener->next = mList;

			mList = pListener;

			mCount++;

			return pListener;
		}

		void free(){

			Listener *temp = NULL;

			for (temp = mList; temp != NULL; temp = mList){

				mList = mList->next;

				delete temp;
	
				mCount--;
			}
		}

		Listener *find(int type) {

			Listener *temp = NULL;

			for (temp = mList; temp != NULL; temp = temp->next){

				if (temp->mType == type){
					break;
				}
			}

			return temp;
		}

	};

}

#endif
