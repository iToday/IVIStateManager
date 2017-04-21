package com.itoday.ivi.debug;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itoday.ivi.R;
import com.itoday.ivi.data.IVIDataManager;
import com.itoday.ivi.platform.IVIChannel;
import com.itoday.ivi.platform.IVITuner;
import com.itoday.ivi.sdk.IVITunerManager;
import com.itoday.ivi.sdk.IVITunerManager.OnTunerListener;

public class TunerActivity extends Activity implements OnClickListener{
	
	private IVITunerManager mTunerManager;
	
	private ListView mFavorsView;

	private int mStereo  = IVITuner.Stereo.CLOSE;
	
	private int mLoc = IVITuner.FmMode.LOC;
	
	private TextView mTvBand;
	private TextView mTvFreq;
	private TextView mTvArea;
	private TextView mTvStereo;
	private TextView mTvLoc;
	
	private ArrayAdapter<IVIChannel> mAdapter;
	
	private ArrayList<IVIChannel> mList = new ArrayList<IVIChannel>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_tuner);
		
		IVIDataManager.setup(this);
		
		mTunerManager = new IVITunerManager(this, new OnTunerListener(){

			@Override
			public void onActive(boolean active) {
				
				if (active){
					IVIChannel channel = mTunerManager.getFreq();
					
					mTvBand.setText(channel.getBand() + "");
					mTvFreq.setText(channel.getFreq() + "");
				}
			}

			@Override
			public void onSignal(int freq, int band, int level) {
				mTvBand.setText(freq + "");
				mTvFreq.setText(band + "");
			}

			@Override
			public void onState(int scanType, int newStatus, int oldStatus) {
				
			}

			@Override
			public void onFreq(int newFreq, int newBand, int oldFreq,int oldBand) {
				mTvBand.setText(newFreq + "");
				mTvFreq.setText(newBand + "");
			}

			@Override
			public void onRemoteCommand(int cmd) {
				
			}

			@Override
			public void onFavorList(int band, List<IVIChannel> favors, int index) {
				mList.clear();
				mList.addAll(favors);
				mAdapter.notifyDataSetChanged();
			}
			
		});
		
		mFavorsView = (ListView)findViewById(R.id.favors);
		mTvBand = (TextView)findViewById(R.id.tvBand);
		mTvFreq = (TextView)findViewById(R.id.tvFreq);
		mTvStereo = (TextView)findViewById(R.id.tvStereo);
		mTvLoc = (TextView)findViewById(R.id.tvLoc);
		mTvArea = (TextView)findViewById(R.id.tvArea);
		
		mAdapter = new ArrayAdapter<IVIChannel>(this, android.R.layout.simple_list_item_1, mList);
		mFavorsView.setAdapter(mAdapter);
		
		mFavorsView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				IVIChannel channel = mList.get(arg2);
				mTunerManager.setFreq(channel);
				
			}
			
		});
	}

	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mTunerManager.release();
	}



	@Override
	public void onClick(View v) {
		switch (v.getId()){
		case R.id.btnBand:
			mTunerManager.setFreq(new IVIChannel(87500, IVITuner.Band.FM, null));
			break;
		case R.id.btnSeekup:
			mTunerManager.seekUp();
			break;
		case R.id.btnScandown:
			mTunerManager.scanDown();
			break;
		case R.id.btnScanSave:
			mTunerManager.scanSave();
			break;
		case R.id.btnScanup:
			mTunerManager.scanUp();
			break;
		case R.id.btnSeekdown:
			mTunerManager.seekDown();
			break;
		case R.id.btnStereo:
			mTunerManager.setStereo(mStereo == 0 ? 1 : 0);
			mStereo = mStereo == 0 ? 1 : 0;
			break;
		case R.id.btnLoc:
			mTunerManager.setLoc(mLoc == 0 ? 1 : 0);
			
			mLoc = mLoc == 0 ? 1 : 0;
			break;
		case R.id.btnOuzhou:
			mTunerManager.setArea(IVITuner.Area.AREA_EUROPE);
			break;
		case R.id.btnLadingmeizhou:
			mTunerManager.setArea(IVITuner.Area.AREA_LATIN);
			break;
		case R.id.btnTaiguo:
			mTunerManager.setArea(IVITuner.Area.AREA_THAILAND);
			break;
		case R.id.btnRiben:
			mTunerManager.setArea(IVITuner.Area.AREA_JAPAN);
			break;
		case R.id.btnEluosi:
			mTunerManager.setArea(IVITuner.Area.AREA_RUSSIA);
			break;
		case R.id.btnBeimeizhou:
			mTunerManager.setArea(IVITuner.Area.AREA_NORTH);
			break;
		}
		
	}
}
