package com.itoday.ivi.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.itoday.ivi.R;
import com.itoday.ivi.R.id;
import com.itoday.ivi.R.layout;
import com.itoday.ivi.platform.IVIApp;
import com.itoday.ivi.sdk.IVIManager;
import com.itoday.ivi.sdk.IVIManager.OnActiveListener;

public class RunModeActivity extends Activity implements OnClickListener{
	
	private IVIManager iviManager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_runmode);
		
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
		case R.id.btnA2dp:
			iviManager.setRunning(IVIApp.A2DP);
			break;
		case R.id.btnAtv:
			iviManager.setRunning(IVIApp.ATV);
			break;
		case R.id.btnAvin:
			iviManager.setRunning(IVIApp.AVIN);
			break;
		case R.id.btnAVM:
			iviManager.setRunning(IVIApp.AVM);
			break;
		case R.id.btnBack:
			iviManager.setRunning(IVIApp.BACK);
			break;
		case R.id.btnBackCarByKey:
			iviManager.setRunning(IVIApp.BACK_REVIEW);
			break;
		case R.id.btnBt:
			iviManager.setRunning(IVIApp.BT);
			break;
		case R.id.btnCarSwitch:
			iviManager.setRunning(IVIApp.CAR_SWITCH);
			break;
		case R.id.btnCmmb:
			iviManager.setRunning(IVIApp.CMMB);
			break;
		case R.id.btnDvd:
			iviManager.setRunning(IVIApp.DVD);
			break;
		case R.id.btnDvrMode:
			iviManager.setRunning(IVIApp.DVR);
			break;
		case R.id.btnEQ:
			iviManager.setRunning(IVIApp.EQ);
			break;
		case R.id.btnHDMI:
			iviManager.setRunning(IVIApp.HDMI);
			break;
		case R.id.btnHelp:
			iviManager.setRunning(IVIApp.HELP);
			break;
		case R.id.btnIE:
			iviManager.setRunning(IVIApp.IE);
			break;
		case R.id.btnIpod:
			iviManager.setRunning(IVIApp.IPOD);
			break;
		case R.id.btnKtv:
			iviManager.setRunning(IVIApp.KTV);
			break;
		case R.id.btnMain:
			iviManager.setRunning(IVIApp.MAIN);
			break;
		case R.id.btnMovie:
			iviManager.setRunning(IVIApp.MOVIE);
			break;
		case R.id.btnMusic:
			iviManager.setRunning(IVIApp.MUSIC);
			break;
		case R.id.btnNavi:
			iviManager.setRunning(IVIApp.NAVI);
			break;
		case R.id.btnOther:
			iviManager.setRunning(IVIApp.OTHER);
			break;
		case R.id.btnPhoto:
			iviManager.setRunning(IVIApp.PHOTO);
			break;
		case R.id.btnPlugInDvd:
			iviManager.setRunning(IVIApp.PLUG_DVD);
			break;
		case R.id.btnSDCrad:
			iviManager.setRunning(IVIApp.SDCARD);
			break;
		case R.id.btnTv:
			iviManager.setRunning(IVIApp.TV);
			break;
		case R.id.btnUSB:
			iviManager.setRunning(IVIApp.USB);
			break;
		case R.id.btnVDisc:
			iviManager.setRunning(IVIApp.VDISC);
			break;
		case R.id.btnWifiSetting:
			iviManager.setRunning(IVIApp.WIFI_SETTINGS);
			break;
		case R.id.btnRadio:
			iviManager.setRunning(IVIApp.RADIO);
			break;
		case R.id.btnFrontAvin:
			iviManager.setRunning(IVIApp.FRONT_AVIN);
			break;
		
		}
	}
}
