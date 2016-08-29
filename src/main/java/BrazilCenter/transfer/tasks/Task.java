package BrazilCenter.transfer.tasks;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import BrazilCenter.transfer.model.FileObj;

/**
 * 
  */
public class Task {

	/** times for reexecute the task, has a maximum number :Utils.MAXTryCount */
	private int tryCount;
	/** assign how many subtasks divided from this task */
	private int subtaskNumber;
	/** report file info */
	private FileObj inputReport;
	/** source file, that going to be handled. */
	private FileObj inputFile;
	private TASKTYPE taskType;

	// record how many times has this file been tried to be transfered.
	private int failedTimes;

	/** each center has its own destination */
	private Map<String, String> destinationAddress;
	/** failed to send files to the following centers. */
	private List<String> FailedCenterName;

	public Task(int branchNumber) {
		this.tryCount = 1;
		this.subtaskNumber = branchNumber;
		this.inputReport = new FileObj();
		this.inputFile = new FileObj();
		this.destinationAddress = new HashMap<String, String>();
		this.FailedCenterName = new LinkedList<String>();
	}

	public Task() {
		this.tryCount = 1;
		this.subtaskNumber = 0;
		this.inputReport = new FileObj();
		this.inputFile = new FileObj();
		this.destinationAddress = new HashMap<String, String>();
		this.FailedCenterName = new LinkedList<String>();
	}

	public synchronized void addTryCount() {
		this.tryCount++;
	}

	public synchronized void setDestinationAddress(String centerName, String destinationAddress) {
		this.destinationAddress.put(centerName, destinationAddress);
	}

	public synchronized void setFailedCenterName(String centername) {
		this.FailedCenterName.add(centername);
	}

	public synchronized void finished() {
		this.subtaskNumber--;
	}

	public void setSubTaskNumber(int branchNumber) {
		this.subtaskNumber = branchNumber;
	}

	public String getInputReportName() {
		return this.inputReport.getName();
	}

	public void setInputReportName(String inputReportName) {
		this.inputReport.setName(inputReportName);
	}

	public String getInputReportpath() {
		return this.inputReport.getPath();
	}

	public void setInputReportpath(String inputReportpath) {
		this.inputReport.setPath(inputReportpath);
	}

	public long getFilesize() {
		return this.inputFile.getFilesize();
	}

	public void setFilesize(long filesize) {
		this.inputFile.setFilesize(filesize);
	}

	public String getFilename() {
		return this.inputFile.getName();
	}

	public void setFilename(String filename) {
		this.inputFile.setName(filename);
	}

	public String getFilepath() {
		return this.inputFile.getPath();
	}

	public void setFilepath(String filepath) {
		this.inputFile.setPath(filepath);
	}

	public String getDestinationAddress(String centerName) {
		return this.destinationAddress.get(centerName);
	}

	public TASKTYPE getTaskType() {
		return taskType;
	}

	public synchronized void setTaskType(TASKTYPE taskType) {
		this.taskType = taskType;
	}

	public int getTryCount() {
		return tryCount;
	}

	public synchronized int getsubTaskNumber() {
		return this.subtaskNumber;
	}

	public List<String> getFailedCenterName() {
		return this.FailedCenterName;
	}

	public int getFailedTimes() {
		return failedTimes;
	}

	public void setFailedTimes(int failedTimes) {
		this.failedTimes = failedTimes;
	}
	
}
