# 设计初衷
车机中包含多种状态，在不同状态下，系统的功能表现不同；

如：

蓝牙电话时，需要打开显示屏和触摸屏，媒体静音，暂停播放，屏蔽部分功能按键等；

倒车时，需要打开显示屏，关闭触摸，媒体静音，暂停播放，屏蔽所有按键等；

待机时，需要打开显示屏，关闭触摸，屏蔽部分功能按键等；

需要设计一种机制统一管理状态的切换及行为，并且各状态下的行为可通过配置文件配置；

# 车载状态机
设计车载状态机，解决复杂的状态问题

下面是状态机核心部分的类图
![](https://github.com/iToday/Vehicle/blob/master/doc/Overview%20of%20ivi.jpg)

我们将车机中状态分原子状态和组合状态；原子状态不可分割，组合状态由原子状态组成；</br>
原子状态包括静音开关状态、触摸屏开关状态、显示屏开关状态；</br>
组合状态包括电话状态、倒车状态、声控状态、待机状态等；如电话状态时，显示屏打开、触摸打开、声音打开；倒车状态时，显示屏打开、触摸关闭、声音关闭；</br>
```Java
private void update() {
  AtomicState asMute;
  AtomicState asScreen;
  AtomicState asTouch;

  synchronized (cstates) {
    asMute = getTargetState(AtomicState.TYPE_MUTE);
    asTouch = getTargetState(AtomicState.TYPE_TOUCH);
    asScreen = getTargetState(AtomicState.TYPE_SCREEN);
  }

  if (DEBUG)
    Log.d(TAG, "thread run: mute :" + asMute + "   \n last mute: " + lastMute);

  if (asMute != null && !asMute.equalsState(lastMute)){
    //设置静音状态,所有声音静音，调用硬件静音
    mPlatform.setMute(asMute.getState() == AtomicState.ON);
    lastMute = asMute;
  }

  if (DEBUG)
    Log.d(TAG, "thread run: screen :" + asScreen + "   \n last screen: " + lastScreen);

  if (asScreen != null &&  !asScreen.equalsState(lastScreen)){

    //设置屏幕开关
    mPlatform.setBacklight(asScreen.getState());
    lastScreen = asScreen;
  }

  if (asTouch != null && !asTouch.equalsState(lastTouch)){

    if (DEBUG)
      Log.d(TAG, "thread run: touch :" + asTouch);

    //设置触摸状态
    mPlatform.setTouch(asTouch.getState());
    lastTouch = asTouch;
  }
}
