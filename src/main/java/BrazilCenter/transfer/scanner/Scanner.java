package BrazilCenter.transfer.scanner;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import BrazilCenter.DaoUtils.dao.Storager;
import BrazilCenter.transfer.model.FileObj;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestMsg;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestService;
import BrazilCenter.transfer.tasks.TASKTYPE;
import BrazilCenter.transfer.tasks.Task;
import BrazilCenter.transfer.utils.CacheScanFileList;
import BrazilCenter.transfer.utils.Configuration;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.MD5Util;
import BrazilCenter.transfer.utils.UploadReport;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

public class Scanner extends Thread {

	private Configuration conf;
	private Storager storer;

	public Scanner(Configuration config) {
		this.conf = config;
		this.storer = new Storager();
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
			LogUtils.logger.error("Error happened when Caculate MD5 value, File: " + filename + " doesn't exist! ");
			return false;
		}
	}

	/**
	 */
	private boolean isFileReady(File file) {
		if (file.renameTo(file)) {
			return true;
		} else {
			return false;
		}
	}

	private List<FileObj> DirectoryScan(String scanAddress) {

		List<FileObj> flist = new LinkedList<FileObj>();
		File parentF = new File(scanAddress);
		if (!parentF.exists()) {
			LogUtils.logger.warn(scanAddress + ", File or directory doesn't exist!");
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
							LogUtils.logger.info("Found new report file: " + file.getName());
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
							Utils.sharedata.AddTask(task);
						}
						
						/**No mattern in which mode, the task has to be processed and stored.*/
						BrazilCenter.DaoUtils.model.FileObj storeObj = new BrazilCenter.DaoUtils.model.FileObj();
						storeObj.setName(task.getFilename());
						storeObj.setPath(task.getFilepath());
						this.storer.Store(storeObj, this.conf.getStoreRootDir());

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
					reloadmsg.setType(0);
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