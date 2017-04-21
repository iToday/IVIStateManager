package com.tomwin.tpms.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.tomwin.tpms.R;
import com.tomwin.tpms.Tyres;
import com.tomwin.tpms.ui.TpmsProxy.OnTpmsListener;

public class TransActivity extends Activity implements OnTpmsListener{
	
	private ProgressDialog dialog;
	
	private List<Integer> list = new ArrayList<Integer>();
	
	private TpmsProxy mProxy;
	
	private Button mLF;
	private Button mRF;
	private Button mLR;
	private Button mRR;
	
	private int mSwitchIndex = 0;
	
	private Handler mHandle = new Handler();
	
	private Runnable mQueryIdRunnale = new Runnable(){

		@Override
		public void run() {
			mProxy.queryId();
			
			mHandle.removeCallbacks(mQueryIdRunnale);
		}
		
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tyre);
		
		mProxy = TpmsProxy.setUp(this);
		
		mLF = ((Button)findViewById(R.id.rl_top_left_transposition));
		mRF = ((Button)findViewById(R.id.rl_top_right_transposition));
		mLR = ((Button)findViewById(R.id.rl_low_left_transposition));
		mRR = ((Button)findViewById(R.id.rl_low_right_transposition));
		
		mProxy.registerListener(this);
		
		mProxy.queryId();
	}
	
	@Override
	protected void onDestroy() {
		mProxy.unregisterListener(this);
		mHandle.removeCallbacks(mQueryIdRunnale);
		super.onDestroy();
	}

	public void onClick(View v){
		switch (v.getId()) {
		case R.id.rl_top_left_transposition:
			addList(1,R.id.rl_top_left_transposition);
			break;
		case R.id.rl_top_right_transposition:
			addList(2,R.id.rl_top_right_transposition);
			break;
		case R.id.rl_low_left_transposition:
			addList(3,R.id.rl_low_left_transposition);
			break;
		case R.id.rl_low_right_transposition:
			addList(4,R.id.rl_low_right_transposition);
			break;
		case R.id.tv_transposition:
			getMessage();
			break;
		case R.id.tv_reset:
			creatList();
			break;

		default:
			break;
		}
	}
	
	private void addList(int var,int id){
		if(list.size() < 2){
			if(!list.contains(var)){
				list.add(var);
				((Button)findViewById(id)).getBackground().setLevel(1);
			}
		}
	}
	
	private void creatList(){
		list.clear();
		mLF.getBackground().setLevel(0);
		mRF.getBackground().setLevel(0);
		mLR.getBackground().setLevel(0);
		mRR.getBackground().setLevel(0);
	}
	
	private void getMessage(){
		if(list.size() == 2){
			
			mSwitchIndex = -1;
			
			 int a = list.get(0);
			 int b = list.get(1);
			 switch ((a+b)) {
			case 3:
				mSwitchIndex = TpmsProxy.LEFT_FRONT_RIGHT_FRONT;
				break;
			case 4:
				mSwitchIndex = TpmsProxy.LEFT_FRONT_LEFT_REAR;
				break;
			case 5:
				if(a == 1)
					mSwitchIndex = TpmsProxy.LEFT_FRONT_RIGHT_REAR;
				else
					mSwitchIndex = TpmsProxy.RIGHT_FRONT_LEFT_REAR;
				break;
			case 6:
				mSwitchIndex = TpmsProxy.RIGHT_FRONT_RIGHT_REAR;
				break;
			case 7:
				mSwitchIndex = TpmsProxy.LEFT_REAR_RIGHT_REAR;
				break;

			default:
				break;
			}
			 
			 if (mSwitchIndex != -1){
				 
				 mHandle.removeCallbacks(mQueryIdRunnale);
				 
				 mProxy.switchPos(mSwitchIndex);
				 sendMessage();
			 }
		}
	}
	
	public void sendMessage(){
		dialog = MAlertDialog.showProgress(this,getString(R.string.msg_switching),getString(R.string.wait));
	}
	
	private void closeProgress(final String str){
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				if(dialog != null){
					dialog.dismiss();
					android.widget.Toast.makeText(getApplicationContext(), str, android.widget.Toast.LENGTH_SHORT).show();
					creatList();
					finish();
				}
			}
		}, 2500);
	}

	@Override
	public void onActive(boolean active) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStateChange(Tyres tyres) {
		// TODO Auto-generated method stub
		
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
		
	}

	@Override
	public void onSwitch(int index) {
		if (mSwitchIndex == index)
			closeProgress(getString(R.string.msg_switch_success));
		else if (index == -1)
			closeProgress(getString(R.string.tyre_fail));
		
		mHandle.postDelayed(mQueryIdRunnale , 1000);
		
	}
	
	public static class MAlertDialog {

		
		public static AlertDialog.Builder showDiolg(Context context){
			LayoutInflater inflater =  LayoutInflater.from(context);
			View view = inflater.inflate(R.layout.alart_dialog, null);
			AlertDialog.Builder builder = new Builder(context);
			builder.setView(view);
			builder.create();
			return  builder;
		}
		
		public static ProgressDialog showProgress(Context context,String str1,String str2){
			return ProgressDialog.show(context, str1, str2, true, false);
		}
	}
}
