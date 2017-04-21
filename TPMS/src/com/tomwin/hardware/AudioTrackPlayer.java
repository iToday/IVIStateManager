package com.tomwin.hardware;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;

public class AudioTrackPlayer {

	private AudioTrack audioTrack;
	
	private byte[] audioData;
	
	private void releaseAudioTrack() {
		
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack.release();
			audioTrack = null;
		}
	}
	
	public AudioTrackPlayer(){
		
		releaseAudioTrack();
		
		int min = AudioTrack.getMinBufferSize(44100, AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT);
		audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 44100,
				AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_16BIT,
				min, AudioTrack.MODE_STATIC);

	}
	
	public boolean isPlaying(){
		return audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}
	
	public void load(InputStream in){
		
		try {
			try {
				
				ByteArrayOutputStream out = new ByteArrayOutputStream(264848); 
				
				for (int b; (b = in.read()) != -1;) {
					out.write(b);
				}
				
				audioData = out.toByteArray();
				
			} finally {
				in.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		audioTrack.write(audioData, 0, audioData.length);
	}
	
	public void start(){
		audioTrack.write(audioData, 0, audioData.length);
		audioTrack.play();
	}
	
	public void pause(){
		audioTrack.pause();
	}
}
