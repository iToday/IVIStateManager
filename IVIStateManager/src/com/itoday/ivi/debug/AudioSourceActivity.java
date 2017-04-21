package com.itoday.ivi.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.itoday.ivi.R;
import com.itoday.ivi.R.id;
import com.itoday.ivi.R.layout;
import com.itoday.ivi.platform.IVIApp;
import com.itoday.ivi.platform.IVIAudio;
import com.itoday.ivi.sdk.IVIManager;
import com.itoday.ivi.sdk.IVIManager.OnActiveListener;

public class AudioSourceActivity extends Activity implements OnClickListener{
	
	private IVIManager iviManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_audio_source);
		
		iviManager = new IVIManager(this, new OnActiveListener(){

			@Override
			public void onActive(boolean active) {
			}
		});		
	}

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		iviManager.release();
	}


	@Override
	public void onClick(View arg0) {
		switch (arg0.getId()){
		case R.id.btnAUXOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_AUX);
			break;
		case R.id.btnAUXClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_AUX);
			break;
		case R.id.btnMainOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_MAIN);
			break;
		case R.id.btnMainClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_MAIN);
			break;
		case R.id.btnRadioOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_FM);
			break;
		case R.id.btnRadioClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_FM);
			break;
		case R.id.btnDVDOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_TV);
			break;
		case R.id.btnDVDClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_TV);
			break;
		case R.id.btnIPodOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_IPOD);
			break;
		case R.id.btnIPodClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_IPOD);
			break;
		case R.id.btnBtOpen:
			iviManager.openAudioSource(IVIAudio.AUDIO_SOURCE_PHONE);
			break;
		case R.id.btnBtClose:
			iviManager.closeAudioSource(IVIAudio.AUDIO_SOURCE_PHONE);
			break;
		
		}
	}
}
