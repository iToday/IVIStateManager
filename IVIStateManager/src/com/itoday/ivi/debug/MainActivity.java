package com.itoday.ivi.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.autonavi.audio.tester.NaviAudioTest;
import com.itoday.ivi.R;
import com.itoday.ivi.R.id;
import com.itoday.ivi.R.layout;
import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.data.IntObserver;
import com.itoday.ivi.data.IntObserver.OnIntDataChange;
import com.itoday.ivi.platform.IVIAudio;
import com.itoday.ivi.platform.IVIDevice;
import com.itoday.ivi.sdk.IVIManager;
import com.itoday.ivi.sdk.IVIManager.OnActiveListener;

public class MainActivity extends Activity implements OnClickListener{
	
	private IVIManager iviManager ;

	private boolean isActive = false;
	
	private boolean mMute = false;
	
	private int mMainVolume = 20; /*0 ~ 40*/
	
	private int mInputVolume = 0; /**-7 ~ +7*/
	
	private int mFrSpeakerGain = 10;
	
	private int mFlSpeakerGain = 10;
	
	private int mRrSpeakerGain = 10;
	
	private int mRlSpeakerGain = 10;
	
	private int mSubwooferGain = 0;
	
	private int mMixGain = 0;
	
	private int mBassQ = 0;
	
	private int mMidQ = 0;
	
	private int mTrebleQ = 0;
	
	private int mBassGain = 0;
	
	private int mMidGain = 0;
	
	private int mTrebleGain = 0;
	
	private int mLoudness = 0;
	
	private int mLightColor = 0;
	
	private static final String action = "android.intent.action.IVIMainService";
	
	private IVIDataManager mDataManager;
	
	private TextView mTvLoudness;
	
	private TextView mTvMid;
	
	private TextView mTvMidQ;
	
	private TextView mTvTrebleQ;
	
	private TextView mMixVolume;
	
	private TextView mSubwVolume;
	
	private TextView mTvMainVolume;
	
	private TextView mFRVolume;
	
	private TextView mFlVolume;
	
	private TextView mRRVolume;
	
	private TextView mRLVolume;
	
	private TextView mTvBassQ;
	
	private TextView mTvBass;
	
	private TextView mTvTreble;
	
	private TextView mTvInputGain;
	
	private TextView mTvLightColor;
	
	private TextView mTvLamp;
	
	private TextView mTvAcc;
	
	private TextView mTvBack;
	
	private String[] COLOR = {
			"NONE","RED","GREEN","DodgerBlue","BLUE","Purple","cyan","Colourful"
	};
	
	private IntObserver mAcc, mLamp, mRevering;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		
		IVIDataManager.setup(this);
		
		mDataManager = IVIDataManager.instance();
	
		iviManager = new IVIManager(this, new OnActiveListener(){

			@Override
			public void onActive(boolean active) {
				isActive = active;
				
				init();
			}
			
		});
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		iviManager.release();
	}



	public void init(){
		
		TextView tvMcuVersion = (TextView)findViewById(R.id.tvMcuVersion);
		tvMcuVersion.setText(mDataManager.getString(IVIDevice.DeviceID.MCU + 0));
		
		mMute = mDataManager.getInt(IVIAudio.MUTE, mMute ? 1 : 0) == 1 ? true : false;
		((Button)findViewById(R.id.btnMute)).setText(mMute? "mute" : "unmute");
				
		mMainVolume = mDataManager.getInt(IVIAudio.MAIN_VOLUME, mMainVolume);
		
		mTvMainVolume = (TextView)this.findViewById(R.id.tvMainGain);
		mTvMainVolume.setText("" + mMainVolume);
				
		mFrSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_FR_GAIN, mFrSpeakerGain);
		
		mFRVolume = (TextView)this.findViewById(R.id.tvFRGain);
		mFRVolume.setText("" + mFrSpeakerGain);
		
		mFlSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_FL_GAIN, mFlSpeakerGain);
		
		mFlVolume = (TextView)this.findViewById(R.id.tvFLGain);
		mFlVolume.setText("" + mFlSpeakerGain);
		
		mRrSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_RR_GAIN, mRrSpeakerGain);
		
		mRRVolume = (TextView)this.findViewById(R.id.tvRRGain);
		mRRVolume.setText("" + mRrSpeakerGain);
		
		mRlSpeakerGain = mDataManager.getInt(IVIAudio.SPEAKER_RL_GAIN, mRlSpeakerGain);
		
		mRLVolume = (TextView)this.findViewById(R.id.tvRLGain);
		mRLVolume.setText("" + mRlSpeakerGain);
		
		mSubwooferGain = mDataManager.getInt(IVIAudio.SUBWOOFER_GAIN, mSubwooferGain);
		
		mSubwVolume = (TextView)this.findViewById(R.id.tvSubwGain);
		mSubwVolume.setText("" + mSubwooferGain);
				
		mMixGain = mDataManager.getInt(IVIAudio.MIX_GAIN, mMixGain);
		
		mMixVolume = (TextView)this.findViewById(R.id.tvMixGain);
		mMixVolume.setText("" + mMixGain);
		
		mBassQ = mDataManager.getInt(IVIAudio.BASS_Q, mBassQ);
		
		mTvBassQ = (TextView)this.findViewById(R.id.tvBassQGain);
		mTvBassQ.setText("" + mBassQ);
		
		mMidQ = mDataManager.getInt(IVIAudio.MID_Q, mMidQ);
		
		mTvMidQ = (TextView)this.findViewById(R.id.tvMidQGain);
		mTvMidQ.setText("" + mMidQ);
		
		mTrebleQ = mDataManager.getInt(IVIAudio.TREBLE_Q, mTrebleQ);
		
		mTvTrebleQ = (TextView)this.findViewById(R.id.tvTrebQGain);
		mTvTrebleQ.setText("" + mTrebleQ);
		
		mBassGain = mDataManager.getInt(IVIAudio.BASS_GAIN, mBassGain);
		
		mTvBass = (TextView)this.findViewById(R.id.tvBassGain);
		mTvBass.setText("" + mBassGain);
		
		mMidGain = mDataManager.getInt(IVIAudio.MID_GAIN, mMidGain);
		
		mTvMid = (TextView)this.findViewById(R.id.tvMidGain);
		mTvMid.setText("" + mMidGain);
				
		mTrebleGain = mDataManager.getInt(IVIAudio.TREBLE_GAIN, mTrebleGain);
		
		mTvTreble = (TextView)this.findViewById(R.id.tvTrebGain);
		mTvTreble.setText("" + mTrebleGain);
				
		mLoudness = mDataManager.getInt(IVIAudio.LOUDNESS, mLoudness);
		
		mTvLoudness = (TextView)this.findViewById(R.id.tvLoudnessGain);
		mTvLoudness.setText("" + mLoudness);
		
		mInputVolume = mDataManager.getInt(IVIAudio.MAIN_INPUT_GAIN, mInputVolume);
		
		mTvInputGain = (TextView)this.findViewById(R.id.tvInputGain);
		mTvInputGain.setText("" + mInputVolume);
		
		mLightColor =  mDataManager.getInt(IVIAudio.LIGHT_COLOR,mLightColor);
		
		mTvLightColor = (TextView)findViewById(R.id.tvLightColor);
		
		if (mLightColor < COLOR.length && mLightColor >= 0)
			mTvLightColor.setText(COLOR[mLightColor]);
		
		mTvLamp = (TextView)findViewById(R.id.tvLAMP);
		
		mTvAcc = (TextView)findViewById(R.id.tvACC);
		
		mTvBack = (TextView)findViewById(R.id.tvResver);
		
		mAcc = new IntObserver(IVIDevice.DeviceID.ACC);
		mLamp = new IntObserver(IVIDevice.DeviceID.LAMP);
		mRevering = new IntObserver(IVIDevice.DeviceID.REVERING);
		
		mAcc.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mTvAcc.setText("ACC:" + newState);
				return 0;
			}
			
		});
		
		mLamp.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mTvLamp.setText("LMAP:" + newState);
				return 0;
			}
			
		});
		
		mRevering.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mTvBack.setText("Back:" + newState);
				return 0;
			}
			
		});
		
	}
	
	@Override
	public void onClick(View v) {

		switch (v.getId()){
		case R.id.btnMute:
			iviManager.setMute(!mMute);
			mMute = !mMute;
			((Button)v).setText(mMute? "mute" : "unmute");
			break;
		case R.id.btnFLGainsub:
			mFlSpeakerGain --;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mFlVolume.setText("" + mFlSpeakerGain);
			break;
		case R.id.btnFLnGainAdd:
			mFlSpeakerGain ++;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mFlVolume.setText("" + mFlSpeakerGain);
			break;
		case R.id.btnFRGainsub:
			mFrSpeakerGain --;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mFRVolume.setText("" + mFrSpeakerGain);
			break;
		case R.id.btnFRnGainAdd:
			mFrSpeakerGain ++;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mFRVolume.setText("" + mFrSpeakerGain);
			break;
		case R.id.btnInputGainAdd:
			mInputVolume ++;
			iviManager.setInputVolume(0, mInputVolume);
			mTvInputGain.setText("" + mInputVolume);
			break;
		case R.id.btnInputGainsub:
			mInputVolume --;
			iviManager.setInputVolume(0, mInputVolume);
			mTvInputGain.setText("" + mInputVolume);
			break;
		case R.id.btnMainGainAdd:
			mMainVolume ++;
			iviManager.setMainVolume(mMainVolume);
			mTvMainVolume.setText("" + mMainVolume);
			break;
		case R.id.btnMainGainsub:
			mMainVolume --;
			iviManager.setMainVolume(mMainVolume);
			mTvMainVolume.setText("" + mMainVolume);
			break;
		case R.id.btnRLGainsub:
			mRlSpeakerGain --;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mRLVolume.setText("" + mRlSpeakerGain);
			break;
		case R.id.btnRLnGainAdd:
			mRlSpeakerGain ++;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mRLVolume.setText("" + mRlSpeakerGain);
			break;
		case R.id.btnRRGainsub:
			mRrSpeakerGain --;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mRRVolume.setText("" + mRrSpeakerGain);
			break;
		case R.id.btnRRnGainAdd:
			mRrSpeakerGain ++;
			iviManager.setSpeakerVolume(mFrSpeakerGain, mFlSpeakerGain, mRrSpeakerGain, mRlSpeakerGain);
			mRRVolume.setText("" + mRrSpeakerGain);
			break;
		case R.id.btnAudioSource:
			Intent intent = new Intent(this, AudioSourceActivity.class);
			startActivity(intent);
			break;
		case R.id.btnRunMode:
			Intent intent1 = new Intent(this, RunModeActivity.class);
			startActivity(intent1);
			
			break;
		case R.id.btnBassGainAdd:
			mBassGain ++;
			//iviManager.setBassGain(mBassGain);
			mTvBass.setText("" + mBassGain);
			break;
		case R.id.btnBassGainsub:
			mBassGain --;
			//iviManager.setBassGain(mBassGain);
			mTvBass.setText("" + mBassGain);
			break;
		case R.id.btnBassQGainAdd:
			mBassQ ++;
			//iviManager.setBassQ(mBassQ);
			mTvBassQ.setText("" + mBassQ);
			break;
		case R.id.btnBassQGainsub:
			mBassQ --;
			//iviManager.setBassQ(mBassQ);
			mTvBassQ.setText("" + mBassQ);
			break;
		case R.id.btnLightLeft:
			mLightColor --;
			iviManager.setLightColor(mLightColor);
			if (mLightColor < COLOR.length && mLightColor >= 0)
				mTvLightColor.setText(COLOR[mLightColor]);
			break;
		case R.id.btnLightRight:
			mLightColor ++;
			iviManager.setLightColor(mLightColor);
			
			if (mLightColor < COLOR.length && mLightColor >= 0)
				mTvLightColor.setText(COLOR[mLightColor]);
			break;
		case R.id.btnLoudnessGainAdd:
			mLoudness ++;
			iviManager.setLoudness(mLoudness);
			mTvLoudness.setText("" + mLoudness);
			break;
		case R.id.btnLoudnessGainsub:
			mLoudness --;
			iviManager.setLoudness(mLoudness);
			mTvLoudness.setText("" + mLoudness);
			break;
		case R.id.btnMidGainAdd:
			mMidGain ++;
			//iviManager.setMidGain(mMidGain);
			mTvMid.setText("" + mMidGain);
			break;
		case R.id.btnMidGainsub:
			mMidGain --;
			//iviManager.setMidGain(mMidGain);
			mTvMid.setText("" + mMidGain);
			break;
		case R.id.btnMixGainAdd:
			mMixGain ++;
			iviManager.setMixGain(mMixGain);
			mMixVolume.setText("" + mMixGain);
			break;
		case R.id.btnMixGainsub:
			mMixGain --;
			iviManager.setMixGain(mMixGain);
			mMixVolume.setText("" + mMixGain);
			break;
		case R.id.btnSubwGainAdd:
			mSubwooferGain ++;
			iviManager.setSubwooferGain(mSubwooferGain);
			mSubwVolume.setText("" + mSubwooferGain);
			break;
		case R.id.btnSubwGainsub:
			mSubwooferGain --;
			iviManager.setSubwooferGain(mSubwooferGain);
			mSubwVolume.setText("" + mSubwooferGain);
			break;
		case R.id.btnTrebQGainAdd:
			mTrebleQ ++;
			//iviManager.setTrebleQ(mTrebleQ);
			mTvTreble.setText("" + mTrebleQ);
			break;
		case R.id.btnTrebGainAdd:			
			mTrebleGain ++;			
			//iviManager.setTrebleGain(mTrebleGain);			
			mTvTreble.setText("" + mTrebleGain);
			break;
		case R.id.btnTrebGainsub:
			mTrebleGain --;			
			//iviManager.setTrebleGain(mTrebleGain);			
			mTvTreble.setText("" + mTrebleGain);
			break;
		case R.id.btnTrebQGainsub:
			mTrebleQ --;
			//iviManager.setTrebleQ(mTrebleQ);
			mTvTreble.setText("" + mTrebleQ);
			break;
		case R.id.btnTunerDebug:
			Intent intent2 = new Intent(this, TunerActivity.class);
			startActivity(intent2);
			break;
		case R.id.btnAudioState:
			Intent intent3 = new Intent(this, NaviAudioTest.class);
			startActivity(intent3);
			break;
		default:
			break;
		}
		
	}

}
