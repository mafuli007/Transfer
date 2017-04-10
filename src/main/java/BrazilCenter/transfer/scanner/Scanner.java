package BrazilCenter.transfer.scanner;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.FileObj;
import BrazilCenter.models.TASKTYPE;
import BrazilCenter.models.Task;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestMsg;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestService;
import BrazilCenter.transfer.utils.CacheScanFileList;
 import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.MD5Util;
import BrazilCenter.transfer.utils.UploadReport;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

public class Scanner extends Thread {

	private Configuration conf;
	private List<FileObj> fileCacheList; // used to check if the file is ready.


	public Scanner(Configuration config) {
		this.conf = config;
		fileCacheList = new LinkedList<FileObj>();

	}

	/** check if the md5 values is equal to the value stored in report. */
	public boolean IsFileMd5Equeal(UploadReport report) {
		String filename = this.conf.getFtpMountDirectory() + "/DATA/" + report.getFilename();
		String md5value = MD5Util.getFileMD5(new File(filename));
		File file = new File(filename);
		if (file.exists()) {
			if (md5value.compareTo(report.getMd5value()) == 0) {
				return true;
			} else {
				LogUtils.logger.error("Error happened when Caculate MD5 value, Original value: " + report.getMd5value()
						+ ", New value : " + md5value);
				return false;
			}
		} else {
			LogUtils.logger.error("Error happened when Caculate MD5 value, File: " + filename + " does not exist! ");
			return false;
		}
	}

	/** get file size, usually when you copy the file to one directory, the following 
	 * function will throw exceptions*/
	private int getFileSize(File file) {
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			int size = fis.available();
			fis.close();
			return size;
		} catch (Exception e) {
			return -1;
		}
	}

	/**
	 */
	private boolean isFileReady(File file) {

		/**
		 * try to find if the the file already exist. if the file already exist,
		 * then check if the length changes, if not then copy the file else
		 * update the file's length.
		 */
		for (int i = 0; i < this.fileCacheList.size(); i++) {
			FileObj tmpObj = this.fileCacheList.get(i);
			if (file.getName().equals(tmpObj.getName())) {
				this.fileCacheList.remove(i);
				// file already exist.
				int currentLen = this.getFileSize(file);
				if (currentLen > 0) {
					if (currentLen == tmpObj.getFilesize()) { // same size
						return true;
					} else { // different size;
						tmpObj.setFilesize(this.getFileSize(file));
						this.fileCacheList.add(tmpObj);
						return false;
					}
				}
			}
		}
		/** new file */
		FileObj obj = new FileObj();
		obj.setName(file.getName());
		obj.setFilesize(this.getFileSize(file));
		this.fileCacheList.add(obj);

		return false;
	}

	private List<FileObj> DirectoryScan(String scanAddress) {

		List<FileObj> flist = new LinkedList<FileObj>();
		File parentF = new File(scanAddress);
		if (!parentF.exists()) {
			LogUtils.logger.warn(scanAddress + ", File or directory does not exist!");
			parentF.mkdirs();
			LogUtils.logger.info(scanAddress + " Created!");
		} else {
			String[] subFiles = parentF.list();
			for (int i = 0; i < subFiles.length; i++) {

				File file = new File(scanAddress + File.separator + subFiles[i]);
				if (file.isFile()) {
					if (!CacheScanFileList.IfContainedInCacheScanFileList(file.getName())) {
						if (this.isFileReady(file)) {
							FileObj fileObj = new FileObj();
							fileObj.setName(file.getName());
							fileObj.setPath(file.getParent());
							fileObj.setFilesize(file.length());
							flist.add(fileObj);
							CacheScanFileList.AddToCacheScanFileList(file.getName());
							LogUtils.logger.info("Find new report: " + file.getName());
						}
					}
				}
			}
		}
		return flist;
	}

	/**
	 */
	public void run() {

		while (true) {
			
			List<FileObj> tmplist = this.DirectoryScan(this.conf.getReportRootDir());
			for (int i = 0; i < tmplist.size(); i++) {

				String reportName = tmplist.get(i).getName();
				String reportPath = tmplist.get(i).getPath();
				String reportPathAndName = reportPath + File.separator + reportName;
				UploadReport report = XMLOperator.ParseUploadReportXML(reportPathAndName);
				if (report != null) {
					if (IsFileMd5Equeal(report)) {

						Task task = new Task();
						task.setInputReportName(reportName);
						task.setInputReportpath(reportPath);
						task.setTaskType(TASKTYPE.NewTask);
						task.setFilename(report.getFilename());
						task.setFilepath(this.conf.getFtpMountDirectory() + report.getDestinationAddress());
						File tmp = new File(task.getFilepath() + File.separator + task.getFilename());
						task.setFilesize(tmp.length());

						/** if the switch is "yes", then assign the task to transfer threads.*/
						if (this.conf.getTransferSwitch().equals("yes")) {
							Utils.transferTaskQueue.AddTask(task);
						}
						
						/**No matter in which mode, the task has to be processed and stored.*/
						Utils.storeTaskQueue.AddTask(task);
						
					} else {

						RepeatRequestMsg reloadmsg = new RepeatRequestMsg(report.getSoftwareId(), report.getFilename());
						reloadmsg.setType(1);
						RepeatRequestService.shareMsgList.AddMsg(reloadmsg);
						if(
						Utils.MoveFile(reportPathAndName, this.conf.getReportRootDir() + File.separator
								+ Utils.REPORT_INVALID_MD5_DIR + File.separator + reportName))
						{
							CacheScanFileList.RemoveFromCacheScanFileList(reportName);
						}
					}
				} else {

					String findName = reportName.substring(reportName.indexOf("_") + 1, reportName.indexOf("."));
					RepeatRequestMsg reloadmsg = new RepeatRequestMsg(findName);
					reloadmsg.setType(0);  // send the reloadmsg to all the reupload service. 
					RepeatRequestService.shareMsgList.AddMsg(reloadmsg);
					
					if(Utils.MoveFile(reportPathAndName, this.conf.getReportRootDir() + File.separator
							+ Utils.REPORT_INVALID_XML_DIR + File.separator + reportName)){
						CacheScanFileList.RemoveFromCacheScanFileList(reportName);
					}
				}
			}
			try {
				Thread.sleep(Utils.SCAN_INTERVAL * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}