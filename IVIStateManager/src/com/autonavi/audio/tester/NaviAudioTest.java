package com.autonavi.audio.tester;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.itoday.ivi.R;
import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVINavi;
import com.itoday.ivi.platform.IVIPhone;

public class NaviAudioTest extends Activity implements OnClickListener {
	
	private AudioManager mAudioManager;
	
	private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener(){

		@Override
		public void onAudioFocusChange(int focusChange) {
			
		}
		
	};
	
	private IVIDataManager mDataManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_navi_audio);
		
		mAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
		mDataManager = IVIDataManager.instance();
	}

	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()){
		case R.id.btnNaviStart:
			mAudioManager.requestAudioFocus(mAudioFocusListener, AudioManager.STREAM_NOTIFICATION, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
			mDataManager.putInt(IVINavi.NAVI_STATE, 1);
			break;
		case R.id.btnNaviEnd:
			mAudioManager.abandonAudioFocus(mAudioFocusListener);
			mDataManager.putInt(IVINavi.NAVI_STATE, 0);
			break;
		case R.id.btnPhoneStart:
			mDataManager.putInt(IVIPhone.PHONE_STATE, IVIPhone.TALKING);
			break;
		case R.id.btnPhoneEnd:
			mDataManager.putInt(IVIPhone.PHONE_STATE, IVIPhone.IDLE);
			break;
		case R.id.btnReversingStart:
			break;
		case R.id.btnReversingEnd:
			break;
		case R.id.btnVoiceStart:
			break;
		case R.id.btnVoiceEnd:
			break;
		default:
			break;
		}
		
	}
}
