package com.tomwin.tpms.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;

import com.tomwin.tpms.R;
import com.tomwin.tpms.Tyres;
import com.tomwin.tpms.ui.TpmsProxy.OnTpmsListener;

public class PairActivity extends Activity implements OnTpmsListener{
	
	private static final int MSG_PAIR_TIMEOUT = 0X01;
	
	private static final int MSG_QUERY_ID = 0X02;
	
	private Button mLF;
	private Button mRF;
	private Button mLR;
	private Button mRR;
	
	private TpmsProxy mProxy;
	
	public ProgressDialog dialog;
	
	private byte position = (byte) 0xFF;
	
	private Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if(msg.what == MSG_PAIR_TIMEOUT){
				closeProgress(getString(R.string.matching_fail));
			}else if (msg.what == MSG_QUERY_ID){
				mProxy.queryId();
				
				removeMessages(MSG_QUERY_ID);
				sendEmptyMessageDelayed(MSG_QUERY_ID, 2000);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_study);
		
		mProxy = TpmsProxy.setUp(getApplicationContext());
		mProxy.registerListener(this);
		
		mLF = ((Button)findViewById(R.id.rl_top_left_matching));
		mRF = ((Button)findViewById(R.id.rl_top_right_matching));
		mLR = ((Button)findViewById(R.id.rl_low_left_matching));
		mRR = ((Button)findViewById(R.id.rl_low_right_matching));
		
		mProxy.queryId();
	}
	
	@Override
	protected void onDestroy() {
		mProxy.unregisterListener(this);
		mHandler.removeMessages(MSG_PAIR_TIMEOUT);
		mHandler.removeMessages(MSG_QUERY_ID);
		super.onDestroy();
	}

	private void resetBackgroundColor() {
		mLF.getBackground().setLevel(0);
		mRF.getBackground().setLevel(0);
		mLR.getBackground().setLevel(0);
		mRR.getBackground().setLevel(0);
	}
	
	public void onClick(View v){
		resetBackgroundColor();
		switch (v.getId()) {
		case R.id.rl_top_left_matching:
			position = TpmsProxy.LEFT_FRONT_INDEX;
			v.getBackground().setLevel(1);
			break;
		case R.id.rl_top_right_matching:
			position = TpmsProxy.RIGHT_FRONT_INDEX;
			v.getBackground().setLevel(1);
			break;
		case R.id.rl_low_left_matching:
			position = TpmsProxy.LEFT_REAR_INDEX;
			v.getBackground().setLevel(1);
			break;
		case R.id.rl_low_right_matching:
			position = TpmsProxy.RIGHT_REAR_INDEX;
			v.getBackground().setLevel(1);
			break;
		case R.id.tv_matching:
			sendMatch(position);
			break;
		default:
			break;
		}
	}
	
	private void sendMatch(byte key){
		if(key != (byte)0xFF){
			Message msg = mHandler.obtainMessage(MSG_PAIR_TIMEOUT);
		    mHandler.sendMessageDelayed(msg, 1000*60*2);
			dialog = new ProgressDialog(this);
			dialog.setTitle(getString(R.string.msg_studying));
			dialog.setMessage(getString(R.string.wait));
			dialog.setButton( DialogInterface.BUTTON_NEGATIVE, getString(R.string.comm_cancel), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					mProxy.stopPair();
					mHandler.removeMessages(MSG_PAIR_TIMEOUT);
					dialog.cancel();
				}
			});
			dialog.show();
			mProxy.pair(key);
		}
		
	}
	
	private void closeProgress(final String str){
		mProxy.stopPair();
		mProxy.queryId();
		mHandler.removeMessages(MSG_PAIR_TIMEOUT);
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if(dialog != null){
					dialog.dismiss();
					android.widget.Toast.makeText(getApplicationContext(), str, android.widget.Toast.LENGTH_SHORT).show();
					position = (byte) 0xFF;
				}
			}
		}, 2500);
	}

	@Override
	public void onActive(boolean active) {
		
	}

	@Override
	public void onStateChange(Tyres tyres) {
		
	}

	@Override
	public void onId(Tyres tyres) {
		
		if (tyres.getIndex() == TpmsProxy.LEFT_FRONT_INDEX)
			mLF.setText(tyres.getId() + "");
		else if (tyres.getIndex() == TpmsProxy.LEFT_REAR_INDEX)
			mLR.setText(tyres.getId() + "");
		else if (tyres.getIndex() == TpmsProxy.RIGHT_FRONT_INDEX)
			mRF.setText(tyres.getId() + "");
		else if (tyres.getIndex() == TpmsProxy.RIGHT_REAR_INDEX)
			mRR.setText(tyres.getId() + "");
		
	}

	@Override
	public void onBattery(Tyres tyres) {
		
	}

	@Override
	public void onPairState(int index, int state) {
		if (state == Tyres.PAIRED){
			closeProgress(getString(R.string.msg_study_success));
			mHandler.removeMessages(MSG_QUERY_ID);
			mHandler.sendEmptyMessageDelayed(MSG_QUERY_ID, 1000);
		}
	}

	@Override
	public void onSwitch(int index) {
		
	}
}
