package org.collectiveone.web.dto;

public class UsernameAndData {
	private String username;
	private double pps;
	private double nCbtionsCreated;
	private double nCbtionsDone;
	private double nCbtionsDoneRecently;
	
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public double getPps() {
		return pps;
	}
	public void setPps(double pps) {
		this.pps = pps;
	}
	public double getnCbtionsCreated() { // NO_UCD (unused code)
		return nCbtionsCreated;
	}
	public void setnCbtionsCreated(double nCbtionsCreated) {
		this.nCbtionsCreated = nCbtionsCreated;
	}
	public double getnCbtionsDone() { // NO_UCD (unused code)
		return nCbtionsDone;
	}
	public void setnCbtionsDone(double nCbtionsDone) {
		this.nCbtionsDone = nCbtionsDone;
	}
	public double getnCbtionsDoneRecently() { // NO_UCD (unused code)
		return nCbtionsDoneRecently;
	}
	public void setnCbtionsDoneRecently(double nCbtionsDoneRecently) {
		this.nCbtionsDoneRecently = nCbtionsDoneRecently;
	}
	
}
