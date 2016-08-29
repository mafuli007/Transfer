package BrazilCenter.transfer.utils;

/**
 *
 * @author phoenix
 *
 */
public class TransferReport {

	private String outReportName; // name of the report file;
	private String outReportPath; // path of the report file;

	private String targetCentername;
	private String messageType;
	private String softwareId;
	private String startSendTime;
	private String endSendTime;
	private String sourceAddress;
	private String destinationAddress;
	private String filename; // the file's name that has been transfered.
	private String md5value;
	private boolean result;
	private String failReason;
	private long size;

	public TransferReport() {
		this.messageType = "TransferReport";
		this.failReason = " ";
		this.outReportName = " ";
		this.outReportPath = " ";
		this.targetCentername = " ";
		this.softwareId = " ";
		this.startSendTime = " ";
		this.endSendTime = " ";
		this.sourceAddress = " ";
		this.destinationAddress = " ";
		this.filename = " ";
		this.md5value = " ";
		this.result = false;
		this.size = 0;
	}

	public String getTargetCentername() {
		return targetCentername;
	}

	public void setTargetCentername(String targetCentername) {
		this.targetCentername = targetCentername;
	}

	public String getOutReportName() {
		return outReportName;
	}

	public void setOutReportName(String outReportName) {
		this.outReportName = outReportName;
	}

	public String getOutReportPath() {
		return outReportPath;
	}

	public void setOutReportPath(String outReportPath) {
		this.outReportPath = outReportPath;
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

	public String getStartSendTime() {
		return startSendTime;
	}

	public void setStartSendTime(String startSendTime) {
		this.startSendTime = startSendTime;
	}

	public String getEndSendTime() {
		return endSendTime;
	}

	public void setEndSendTime(String endSendTime) {
		this.endSendTime = endSendTime;
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
		if (destinationAddress != null) {
			this.destinationAddress = destinationAddress;
		}
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

	@Override
	public String toString() {
		return "TransferReport [outReportName=" + outReportName + ", outReportPath=" + outReportPath
				+ ", targetCentername=" + targetCentername + ", messageType=" + messageType + ", softwareId="
				+ softwareId + ", startSendTime=" + startSendTime + ", endSendTime=" + endSendTime + ", sourceAddress="
				+ sourceAddress + ", destinationAddress=" + destinationAddress + ", filename=" + filename
				+ ", md5value=" + md5value + ", result=" + result + ", failReason=" + failReason + ", size=" + size
				+ "]";
	}

}
