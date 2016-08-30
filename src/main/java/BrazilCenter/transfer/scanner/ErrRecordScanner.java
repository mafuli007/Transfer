package BrazilCenter.transfer.scanner;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.FileObj;
import BrazilCenter.models.TASKTYPE;
import BrazilCenter.models.Task;
import BrazilCenter.transfer.model.ErrRecordObj;
import BrazilCenter.transfer.utils.CacheScanFileList;
 import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

/**
 * 
 * @author maful
 *
 */
public class ErrRecordScanner extends Thread {

	private Configuration conf;

	public ErrRecordScanner(Configuration config) {
		this.conf = config;
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
			LogUtils.logger.warn(scanAddress + " Directory doesn't exist!");
			parentF.mkdirs();
			LogUtils.logger.info(scanAddress + " Created!");
		} else {
			String[] subFiles = parentF.list();
			for (int i = 0; i < subFiles.length; i++) {

				File file = new File(scanAddress + File.separator + subFiles[i]);
				if (file.isFile()) {
					if (!CacheScanFileList.IfContainedInCacheScanFileList(file.getName())) {
						LogUtils.logger.info("Found new Error Record: " + file.getName());
						if (this.isFileReady(file)) {
							FileObj fileObj = new FileObj();
							fileObj.setName(file.getName());
							fileObj.setPath(file.getParent());
							fileObj.setFilesize(file.length());
							flist.add(fileObj);
							CacheScanFileList.AddToCacheScanFileList(file.getName());
						}
					}
				}
			}
		}
		return flist;
	}

	public void run() {

		String errDir = this.conf.getReportRootDir() + File.separator + Utils.ErrDataDir;
		while (true) {
			List<FileObj> tmplist = this.DirectoryScan(errDir);
			for (int i = 0; i < tmplist.size(); i++) {
				String errRecordName = tmplist.get(i).getName();
				String errRecordPath = tmplist.get(i).getPath();
				String errRecordNameWithPath = errRecordPath + File.separator + errRecordName;
				ErrRecordObj record = XMLOperator.ParseErrRecordXML(errRecordNameWithPath);
				if (record != null) {

					Task task = new Task();
					task.setInputReportName(errRecordName);
					task.setInputReportpath(errRecordPath);
					task.setTaskType(TASKTYPE.LocalFailedTask);
					task.setFilename(record.getFileName());
					task.setFilepath(record.getFilePath());
					File tmp = new File(task.getFilepath() + File.separator + task.getFilename());
					task.setFilesize(tmp.length());
					task.setFailedCenterName(record.getTargetCenterName());
					task.setFailedTimes(record.getFailedTimes());

					Utils.transferTaskQueue.AddTask(task);

					/** delete the error record */
					Utils.delFile(new File(errRecordNameWithPath));
				} else {
					LogUtils.logger.error("Failed to parse the error record. " + errRecordName);
				}
			}
			try {
				Thread.sleep(this.conf.getErrRecordScanInterval() * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}