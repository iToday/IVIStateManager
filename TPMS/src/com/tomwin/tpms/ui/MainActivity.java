package com.tomwin.tpms.ui;

import java.text.DecimalFormat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tomwin.tpms.IVIDataManager;
import com.tomwin.tpms.IntObserver;
import com.tomwin.tpms.IntObserver.OnIntDataChange;
import com.tomwin.tpms.R;
import com.tomwin.tpms.Tyres;
import com.tomwin.tpms.ui.TpmsProxy.OnTpmsListener;

public class MainActivity extends Activity implements OnTpmsListener{
	
	private static final String TAG = "TActivity";

	private int[] topLeftIds = {R.id.tv_tar_info_pressure_left_top,
			R.id.tv_tar_info_t_left_top,
			R.id.tv_tar_warning_left_top,
			R.id.tv_tar_info_pressure_left_top_unit,
			R.id.tv_tar_info_t_left_top_unit,
			R.id.rl_top_left};
	
	private int[] topRightIds = {R.id.tv_tar_info_pressure_right_top,
			R.id.tv_tar_info_t_right_top,
			R.id.tv_tar_warning_right_top,
			R.id.tv_tar_info_pressure_right_top_unit,
			R.id.tv_tar_info_t_right_top_unit,
			R.id.rl_top_right};
	
	private int[] blowLeftIds = {R.id.tv_tar_info_pressure_left_bottom,
			R.id.tv_tar_info_t_left_bottom,
			R.id.tv_tar_warning_left_low,
			R.id.tv_tar_info_pressure_left_bottom_unit,
			R.id.tv_tar_info_t_left_bottom_unit,
			R.id.rl_bottom_left};
	
	private int[] blowRightIds = {R.id.tv_tar_info_pressure_right_bottom,
			R.id.tv_tar_info_t_right_bottom,
			R.id.tv_tar_warning_right_low,
			R.id.tv_tar_info_pressure_right_bottom_unit,
			R.id.tv_tar_info_t_right_bottom_unit,
			R.id.rl_bottom_right};
	
	private TpmsProxy mProxy;
	
	private int mPressureUnit = Tyres.UNIT_BAR;
	
	private int mTempUnit = Tyres.UNIT_C;
	
	private IntObserver mPressureObserver;
	
	private IntObserver mTempObserver;
	
	private IntObserver mHighPressureLevelObserver;
	
	private IntObserver mLowPressureLevelObserver;
	
	private IntObserver mHighTempLevelObserver;
	
	private DecimalFormat df = new DecimalFormat("#0.0");
	
	private int mHighPressureLevel;
	
	private int mLowPressureLevel;
	
	private int mHighTempLevel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		IVIDataManager.setup(getApplicationContext());
		
		mProxy = TpmsProxy.setUp(this);
		mProxy.registerListener(this);
		
		mPressureObserver = new IntObserver(IVIDataManager.PRESSURE_UNIT);
		mTempObserver = new IntObserver(IVIDataManager.TEMP_UNIT);
		
		mPressureObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mPressureUnit = newState;
				return 0;
			}
			
		});
		
		mTempObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mTempUnit = newState;
				return 0;
			}
			
		});
		
		mPressureUnit = mPressureObserver.getValue(mPressureUnit);
		mTempUnit = mTempObserver.getValue(mTempUnit);
		
		mHighPressureLevelObserver = new IntObserver(IVIDataManager.HIGH_PRESSURE);
		
		mHighPressureLevel = mHighPressureLevelObserver.getValue(IVIDataManager.HIGH_LIMIT);
		
		mHighPressureLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mHighPressureLevel = newState;
				return 0;
			}
			
		});
		
		mLowPressureLevelObserver = new IntObserver(IVIDataManager.LOW_PRESSURE);
		
		mLowPressureLevel = mLowPressureLevelObserver.getValue(IVIDataManager.LOW_LIMIT);
		
		mLowPressureLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mLowPressureLevel = newState;
				return 0;
			}
			
		});
		
		mHighTempLevelObserver = new IntObserver(IVIDataManager.HIGH_TEMP);
		
		mHighTempLevel = mHighTempLevelObserver.getValue(IVIDataManager.T_HIGH_LIMIT);
		
		mHighTempLevelObserver.registerDataChangeListener(new OnIntDataChange(){

			@Override
			public int onIntDataChange(int newState, int oldState) {
				mHighTempLevel = newState;
				return 0;
			}
			
		});
		
		mProxy.requestTyres();
	}
	
	@Override
	protected void onDestroy() {
		mProxy.unregisterListener(this);
		mProxy.release();
		mPressureObserver.release();
		mTempObserver.release();
		super.onDestroy();
	}

	public void onClick(View v){
		switch (v.getId()) {
		case R.id.tv_title_setting:
			startActivity(new Intent(this,SettingActivity.class));
			break;

		default:
			break;
		}
	}

	@Override
	public void onActive(boolean active) {
		
		if (active)
			mProxy.requestTyres();
	}

	@Override
	public void onStateChange(Tyres tyres) {
		
		Log.d(TAG, "onStateChange " + tyres);
		
		switch (tyres.getIndex()){
		case TpmsProxy.LEFT_FRONT_INDEX:
			updateView(topLeftIds, tyres);
			break;
		case TpmsProxy.LEFT_REAR_INDEX:
			updateView(blowLeftIds, tyres);
			break;
		case TpmsProxy.RIGHT_FRONT_INDEX:
			updateView(topRightIds, tyres);
			break;
		case TpmsProxy.RIGHT_REAR_INDEX:
			updateView(blowRightIds, tyres);
			break;
		}
	}

	private void updateView(int [] views, Tyres tyres) {
		
		((TextView)findViewById(views[0])).setText(df.format(tyres.getPressure(mPressureUnit)));
		((TextView)findViewById(views[3])).setText(Tyres.getPressureUnitDescriptor(mPressureUnit));
		((TextView)findViewById(views[1])).setText(df.format(tyres.getTemp(mTempUnit)));
		((TextView)findViewById(views[4])).setText(Tyres.getTempUnitDescriptor(mTempUnit));
		
		StringBuilder builder = new StringBuilder();
		builder.append(tyres.getLeak() == 1 ? getString(R.string.alarm_leak) + "   " : "");
		builder.append(tyres.getBattery() == 1 ? getString(R.string.alarm_low_battery) + "  " : "");
		builder.append(tyres.getSignal() == 1 ? getString(R.string.alarm_signal_error)	+ "  " : "");
		((TextView)findViewById(views[2])).setText(builder.toString());
		
		if (builder.toString().length() > 0
				|| tyres.isHighPressure(mHighPressureLevel)
				|| tyres.isLowPressure(mLowPressureLevel)
				|| tyres.isHighTemp(mHighTempLevel))
			((LinearLayout) findViewById(views[5])).getBackground().setLevel(1);
		else
			((LinearLayout) findViewById(views[5])).getBackground().setLevel(0);
	}

	@Override
	public void onId(Tyres tyres) {
		
	}

	@Override
	public void onBattery(Tyres tyres) {
		
	}

	@Override
	public void onPairState(int index, int state) {
		
	}

	@Override
	public void onSwitch(int index) {
		
	}
}
