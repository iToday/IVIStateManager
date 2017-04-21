package com.itoday.ivi;

import com.itoday.ivi.vehicle.AppModeManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		
		if (action != null && action.equals(Intent.ACTION_BOOT_COMPLETED)){
			final Intent service = new Intent("android.intent.action.IVIMainService");
			service.setPackage("com.itoday.ivi");
			context.startService(service);
			
			AppModeManager apps = new AppModeManager(context);
			apps.recovery();
		}
	}

}
