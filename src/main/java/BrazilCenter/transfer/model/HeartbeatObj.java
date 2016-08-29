package BrazilCenter.transfer.model;

import java.util.Date;

import BrazilCenter.transfer.utils.Utils;

 
public class HeartbeatObj {
	
	private String softwareId;
	private String status;		 
	private Date currenttime;
	private String duration;
	
	private Date startTime;

	public HeartbeatObj() {
		startTime = new Date();
	}

	/** update lasting time */
	public void update(){
		//String date_str = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		long date_now = (new Date()).getTime();
		long date_start = this.startTime.getTime();
		long diff = date_now - date_start;  
		this.duration = Utils.formatTime(diff);
	}
	
	public String getSoftwareId() {
		return softwareId;
	}

	public void setSoftwareId(String softwareId) {
		this.softwareId = softwareId;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getCurrenttime() {
		return currenttime;
	}

	public void setCurrenttime(Date currenttime) {
		this.currenttime = currenttime;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}
	
}
