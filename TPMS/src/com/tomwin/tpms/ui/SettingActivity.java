package com.tomwin.tpms.ui;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.tomwin.tpms.IVIDataManager;
import com.tomwin.tpms.R;
import com.tomwin.tpms.Tyres;

public class SettingActivity extends Activity implements OnClickListener, android.widget.RadioGroup.OnCheckedChangeListener, OnCheckedChangeListener, OnSeekBarChangeListener {

	private int[] unitPIDs = {R.id.unit_pressure_kpa,R.id.unit_pressure_psi,R.id.unit_pressure_bar};
	private int[] unitTIDs = {R.id.unit_t_c,R.id.unit_t_f};
	
	private IVIDataManager mManager;
	
	private int mPressureUnit = Tyres.UNIT_BAR;
	
	private int mTempUnit = Tyres.UNIT_C;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);
		IVIDataManager.setup(getApplicationContext());
		mManager = IVIDataManager.instance();
		initView();
	}

	private void initView() {
		((SeekBar) findViewById(R.id.myseekbar_press_t_limit)).setMax(14);
		((SeekBar) findViewById(R.id.myseekbar_press_t_limit)).setProgress(mManager.getInt(IVIDataManager.HIGH_TEMP, IVIDataManager.T_HIGH_LIMIT));
		((RadioGroup) findViewById(R.id.unit_pressure)).setOnCheckedChangeListener(this);
		((RadioGroup) findViewById(R.id.unit_t)).setOnCheckedChangeListener(this);
		((CheckBox) findViewById(R.id.sb_alarm_trun)).setOnCheckedChangeListener(this);
		((SeekBar) findViewById(R.id.myseekbar_press_up_limit)).setOnSeekBarChangeListener(this);
		((SeekBar) findViewById(R.id.myseekbar_press_low_limit)).setOnSeekBarChangeListener(this);
		((SeekBar) findViewById(R.id.myseekbar_press_t_limit)).setOnSeekBarChangeListener(this);
		
		((CheckBox) findViewById(R.id.sb_alarm_trun)).setChecked(mManager.getInt(IVIDataManager.SOUND_ALARM, 1) == 1 ? true : false);
		((CheckBox) findViewById(R.id.sb_alarm_trun)).setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mManager.putInt(IVIDataManager.SOUND_ALARM, isChecked ? 1 : 0);
			}
		});
		
		((SeekBar) findViewById(R.id.myseekbar_press_up_limit)).setProgress(mManager.getInt(IVIDataManager.HIGH_PRESSURE, IVIDataManager.HIGH_LIMIT));
		((SeekBar) findViewById(R.id.myseekbar_press_low_limit)).setProgress(mManager.getInt(IVIDataManager.LOW_PRESSURE, IVIDataManager.LOW_LIMIT));
		((RadioGroup) findViewById(R.id.unit_pressure)).check(unitPIDs[mManager.getInt(IVIDataManager.PRESSURE_UNIT, Tyres.UNIT_BAR)]);
		((RadioGroup) findViewById(R.id.unit_t)).check(unitTIDs[mManager.getInt(IVIDataManager.TEMP_UNIT, 0)]);
	}
	
	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.tv_title_back:
			finish();
			break;
		case R.id.rl_emitter_matching:
			startActivity(new Intent(this,PairActivity.class));
			break;
		case R.id.btn_initsetting:
			mManager.putInt(IVIDataManager.HIGH_PRESSURE, IVIDataManager.HIGH_LIMIT);
			mManager.putInt(IVIDataManager.LOW_PRESSURE, IVIDataManager.LOW_LIMIT);
			mManager.putInt(IVIDataManager.HIGH_TEMP, IVIDataManager.T_HIGH_LIMIT);
			
			mManager.putInt(IVIDataManager.TEMP_UNIT,Tyres.UNIT_C);
			mTempUnit = Tyres.UNIT_C;
			
			mPressureUnit = Tyres.UNIT_BAR;
			mManager.putInt(IVIDataManager.PRESSURE_UNIT,Tyres.UNIT_BAR);
			
			initView();
			break;
		case R.id.rl_tyre_transposition:
			startActivity(new Intent(this,TransActivity.class));
			break;
		default:
			break;
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		
		if(group.getId() == R.id.unit_pressure){
			switch (checkedId) {
			case R.id.unit_pressure_kpa:
				mPressureUnit = Tyres.UNIT_KPA;
				mManager.putInt(IVIDataManager.PRESSURE_UNIT,Tyres.UNIT_KPA);
				break;
			case R.id.unit_pressure_psi:
				mPressureUnit = Tyres.UNIT_PSI;
				mManager.putInt(IVIDataManager.PRESSURE_UNIT,Tyres.UNIT_PSI);
				break;
			case R.id.unit_pressure_bar:
				mPressureUnit = Tyres.UNIT_BAR;
				mManager.putInt(IVIDataManager.PRESSURE_UNIT,Tyres.UNIT_BAR);
				break;

			default:
				break;
			}
		}else if(group.getId() == R.id.unit_t){
			switch (checkedId) {
			case R.id.unit_t_c:
				mManager.putInt(IVIDataManager.TEMP_UNIT,Tyres.UNIT_C);
				mTempUnit = Tyres.UNIT_C;
				break;
			case R.id.unit_t_f:
				mManager.putInt(IVIDataManager.TEMP_UNIT,Tyres.UNIT_F);
				mTempUnit = Tyres.UNIT_F;
				break;

			default:
				break;
			}
		}
		
		setInitData();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		mManager.putInt(IVIDataManager.SOUND_ALARM, isChecked ? 1 : 0);
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		switch (seekBar.getId()) {
		case R.id.myseekbar_press_up_limit:
			mManager.putInt(IVIDataManager.HIGH_PRESSURE, progress);
			returnUP(progress, ((TextView)findViewById(R.id.tv_pressure_up_limit_alarm)), mPressureUnit);
			break;
		case R.id.myseekbar_press_low_limit:
			mManager.putInt(IVIDataManager.LOW_PRESSURE, progress);
			returnDP(progress, ((TextView)findViewById(R.id.tv_pressure_low_limit_alarm)), mPressureUnit);
			break;
		case R.id.myseekbar_press_t_limit:
			mManager.putInt(IVIDataManager.HIGH_TEMP, progress);
			returnT(progress, ((TextView)findViewById(R.id.tv_pressure_t_limit_alarm)), mTempUnit);
			break;
		default:
			break;
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		
	}
	
	public static void returnUP(int progress, TextView t, int unit) {
		switch (unit) {
		case 0:
			t.setText((double) (250 + progress * 10) + "Kpa");
			break;
		case 1:
			t.setText(36 + (double) Math.round(progress * 14.5) / 10 + "Psi");
			break;
		case 2:
			t.setText((double) (Math.round((progress + 25)) / 10.0) + "Bar");
			break;

		default:
			break;
		}
	}

	public static void returnDP(int progress, TextView t, int unit) {
		switch (unit) {
		case 0:
			t.setText((double) (180 + progress * 10) + "Kpa");
			break;
		case 1:
			t.setText(26 + (double) Math.round(progress * 14.5) / 10 + "Psi");
			break;
		case 2:
			t.setText((double) (Math.round((progress + 18)) / 10.0) + "Bar");
			break;

		default:
			break;
		}
	}
	
	public static void returnT(int progress, TextView t, int unit) {
		switch (unit) {
		case 0:
			t.setText(progress * 5 + 50 + "℃");
			break;
		case 1:
			t.setText(((double) (Math.round((progress * 5 + 50 * 1.8 + 32) * 10)) / 10) + "℉");
			break;
		default:
			break;
		}
	}
	
	private void setInitData(){
		int hp = mManager.getInt(IVIDataManager.HIGH_PRESSURE, IVIDataManager.HIGH_LIMIT);
		int lp = mManager.getInt(IVIDataManager.LOW_PRESSURE, IVIDataManager.LOW_LIMIT);
		int ht = mManager.getInt(IVIDataManager.HIGH_TEMP, IVIDataManager.T_HIGH_LIMIT);
		returnUP(hp,((TextView)findViewById(R.id.tv_pressure_up_limit_alarm)), mPressureUnit);
		returnDP(lp,((TextView)findViewById(R.id.tv_pressure_low_limit_alarm)), mPressureUnit);
		returnT(ht,((TextView)findViewById(R.id.tv_pressure_t_limit_alarm)), mTempUnit);
		
	}
}
