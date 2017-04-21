package com.itoday.ivi.tuner;

import java.util.ArrayList;

public class RdsData {
	
	private int pi;
	
	private int pty;
	
	private String ps;
	
	private String rt;
	
	private ArrayList<Integer> altFreqs = new ArrayList<Integer>();

	public int getPi() {
		return pi;
	}

	public void setPi(int pi) {
		this.pi = pi;
	}

	public int getPty() {
		return pty;
	}

	public void setPty(int pty) {
		this.pty = pty;
	}

	public String getPs() {
		return ps;
	}

	public void setPs(String ps) {
		this.ps = ps;
	}

	public String getRt() {
		return rt;
	}

	public void setRt(String rt) {
		this.rt = rt;
	}

	public ArrayList<Integer> getAltFreqs() {
		return altFreqs;
	}

	public void setAltFreqs(int[] freqs) {
		
		altFreqs.clear();
		
		for (int freq : freqs)
			altFreqs.add(freq);
	}

	@Override
	public String toString() {
		return "RdsData [pi=" + pi + ", pty=" + pty + ", ps=" + ps + ", rt="
				+ rt + ", altFreqs=" + altFreqs + "]";
	}
}
