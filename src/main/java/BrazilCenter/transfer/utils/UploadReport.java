package BrazilCenter.transfer.utils;

/**
 * ��˵���������ϴ��ͻ��˷��͵����֪ͨ
 *
 * @author phoenix
 * @Time 20150128
 *
 */
public class UploadReport {

	private String messageType;  
	private String softwareId;  
	private String sendTime;  
	private String sourceAddress; 
	private String destinationAddress; 
	private String filename; 
	private String md5value; 
	private boolean result; 
	private String failReason;	
	private long size;

	public UploadReport(){
		this.failReason = "";
	}

	public String getFailReason() {
		return failReason;
	}

	public void setFailReason(String failReason) {
		this.failReason = failReason;
	}

	public String getMessageType() {
		return messageType;
	}

	public String getSoftwareId() {
		return softwareId;
	}

	public void setSoftwareId(String softwareId) {
		this.softwareId = softwareId;
	}

	public String getSendTime() {
		return sendTime;
	}

	public void setSendTime(String sendTime) {
		this.sendTime = sendTime;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public String getDestinationAddress() {
		return destinationAddress;
	}

	public void setDestinationAddress(String destinationAddress) {
		this.destinationAddress = destinationAddress;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getMd5value() {
		return md5value;
	}

	public void setMd5value(String md5value) {
		this.md5value = md5value;
	}

	public boolean getResult() {
		return result;
	}

	public void setResult(boolean result) {
		this.result = result;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
