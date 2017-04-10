package BrazilCenter.transfer.model;

/**
  * 
 * @author mafuli
 *
 */
public class ErrRecordObj {
	private String fileName;
	private String filePath;
	private String targetCenterName;
	private int failedTimes;
	
	public ErrRecordObj(){
		this.failedTimes = 0;
		this.fileName = " ";
		this.targetCenterName = " ";
		this.filePath = " ";
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFailedTimes() {
		return failedTimes;
	}

	public void setFailedTimes(int failedTimes) {
		this.failedTimes = failedTimes;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getTargetCenterName() {
		return targetCenterName;
	}

	public void setTargetCenterName(String targetCenterName) {
		this.targetCenterName = targetCenterName;
	}
}
