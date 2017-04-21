
package com.tomwin.hardware;

public class SerialPort {

    private static final String TAG = "SerialPort";

    private String mName;

    public SerialPort(String name) {
        mName = name;
    }

    public void open(int speed) {
        native_open(mName, speed);
    }

    public void close()  {
        native_close();
    }

    public String getName() {
        return mName;
    }

    public void write(byte[] buffer, int length){
            native_write(buffer, length);
    }
    
    public void setListener(Listener listener){
    	native_set_listener(listener);
    }

    public interface Listener{
    	void onNewData(byte[] buffer);
    }

    private native int native_open(String name, int speed);
    private native int native_close();
    private native int native_write(byte[] buffer, int length);
    private native void native_set_listener(Listener listener);
    
    static {
		System.loadLibrary("SerialPort");
	}
}
