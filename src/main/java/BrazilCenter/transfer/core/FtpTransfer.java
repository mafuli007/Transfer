package BrazilCenter.transfer.core;

import java.io.File;
import java.util.LinkedList;
import java.util.Queue;

import BrazilCenter.transfer.model.ErrRecordObj;
import BrazilCenter.transfer.model.FtpServerAddress;
import BrazilCenter.transfer.tasks.TASKTYPE;
import BrazilCenter.transfer.tasks.Task;
import BrazilCenter.transfer.tcpService.TcpClient;
import BrazilCenter.transfer.utils.CacheScanFileList;
import BrazilCenter.transfer.utils.Configuration;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.TransferReport;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

/**
  */
public class FtpTransfer extends Thread {

	private String targetName;
	private TcpClient monitor_client;
	private FtpClient ftpclient;
	private Queue<Task> taskList;
	private Configuration conf;

	public FtpTransfer(Configuration confr, TcpClient monitorClient, FtpServerAddress serverAddress) {
		this.monitor_client = monitorClient;
		this.conf = confr;
		this.taskList = new LinkedList<Task>();
		this.ftpclient = new FtpClient(confr, serverAddress);
		this.targetName = serverAddress.getName();
		if (!ftpclient.ConnectServer()) {
			LogUtils.logger.error(this.targetName + " FTP disconnected, Try to connect......");
			ftpclient.ReConnect();
		} else {
			LogUtils.logger.info("Connect to " + this.targetName + " FTP!");
		}
	}

	public String getTargetName() {
		return targetName;
	}

	public void addTask(Task task) {
		synchronized (this) {
			this.taskList.add(task);
		}
	}

	private Task GetTask() {
		synchronized (this) {
			return this.taskList.poll();
		}
	}

	/** move the input report to OK directory. */
	private boolean MoveInputReportToOkDir(Task task) {
		String inUploadFileNameWithPath = task.getInputReportpath() + File.separator + task.getInputReportName();
		String fileToDir = task.getInputReportpath() + File.separator + "ok" + File.separator
				+ task.getInputReportName();
		return Utils.MoveFile(inUploadFileNameWithPath, fileToDir);
	}

	/** move the input report to error directory. */
	private boolean MoveInputReportToErrorDir(Task task) {
		String inUploadFileNameWithPath = task.getInputReportpath() + File.separator + task.getInputReportName();
		String fileToDir = task.getInputReportpath() + File.separator + "error" + File.separator + this.targetName
				+ File.separator + task.getInputReportName();
		return Utils.MoveFile(inUploadFileNameWithPath, fileToDir);
	}

	private boolean DeleteInputReportFile(Task task) {
		String inUploadFileNameWithPath = task.getInputReportpath() + File.separator + task.getInputReportName();
		return Utils.delFile(new File(inUploadFileNameWithPath));
	}

	/**
	 * handle the successful task.
	 */
	private void handleSuccessedTask(Task task) {

		task.finished();
		if ((task.getTaskType() == TASKTYPE.NewTask) || (task.getTaskType() == TASKTYPE.LocalFailedTask)) {
			if (task.getsubTaskNumber() == 0) {
				if (this.MoveInputReportToOkDir(task)) {
					CacheScanFileList.RemoveFromCacheScanFileList(task.getInputReportName());
				}
			}
		} else if (task.getTaskType() == TASKTYPE.RemoteTask) {
			// do nothing.
		} else if (task.getTaskType() == TASKTYPE.SubFailedTask) {
			/**
			 * if one of sub tasks failed, then the input report will be moved
			 * to error directory.
			 */
			if (task.getsubTaskNumber() == 0) {
				if (this.MoveInputReportToErrorDir(task)) {
					CacheScanFileList.RemoveFromCacheScanFileList(task.getInputReportName());
				}
			}
		}
	}

	private void handleFailedUploadReport(TransferReport report, Task task) {
		// delete the file from the ./report/ok directory.
		String reportName = report.getOutReportName();
		Utils.delFile(new File(reportName));

		this.handelFaildTask(task);
	}

	/**
	 * handle the failed task.
	 * 
	 * @param task
	 */
	private void handelFaildTask(Task task) {
		int tryCount = task.getTryCount();
		/** try to execute the task again, till to get the maximum times. */
		if (tryCount < Utils.MAXTryCount) {
			task.addTryCount();
			Utils.sharedata.AddTask(task);
		} else {
			task.finished();
			if ((task.getTaskType() == TASKTYPE.NewTask) || (task.getTaskType() == TASKTYPE.SubFailedTask)) {
				if (task.getsubTaskNumber() == 0) {
					// all the sub tasks are finished. the final result of this
					// task is failed.
					if (this.MoveInputReportToErrorDir(task)) {
						CacheScanFileList.RemoveFromCacheScanFileList(task.getInputReportName());
					}
					this.addFailedRecord(task);
				} else {
					// other sub tasks are not finished, but this sub task is
					// failed.
					task.setTaskType(TASKTYPE.SubFailedTask);
				}
			} else if (task.getTaskType() == TASKTYPE.LocalFailedTask) {
				/** delete the input file */
				this.DeleteInputReportFile(task);
				CacheScanFileList.RemoveFromCacheScanFileList(task.getInputReportName());

				/** add new error record. */
				this.addFailedRecord(task);
			} else if (task.getTaskType() == TASKTYPE.RemoteTask) {
				this.addFailedRecord(task);
			}
		}
	}

	/**
	 * Create a failed record in the Utils.ErrDataDir directory.
	 * 
	 * @param task
	 */
	public void addFailedRecord(Task task) {

		ErrRecordObj errObj = new ErrRecordObj();
		errObj.setFileName(task.getFilename());
		errObj.setFilePath(task.getFilepath());
		errObj.setTargetCenterName(this.targetName);

		if (task.getTaskType() == TASKTYPE.LocalFailedTask) {
			errObj.setFailedTimes(task.getFailedTimes() + 1);
		} else {
			errObj.setFailedTimes(1);
		}

		String recordString = XMLOperator.MakeErrRecordXML(errObj);
		String errRecordFileName = null;
		errRecordFileName = this.conf.getReportRootDir() + File.separator + Utils.ErrDataDir + File.separator
				+ task.getFilename() + ".xml";

		if (Utils.CreateFile(errRecordFileName, recordString)) {
		} else {
			LogUtils.logger.error("Create error record " + task.getFilename() + " Failed.");
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		LogUtils.logger.info(Thread.currentThread().getName() + " Transfer thread started!");
		while (true) {
			Task task = null;
			if ((task = this.GetTask()) != null) {
				/** 1. get one task, then transfer the file to the server. */
				TransferReport report = ftpclient.FtpUploadFile(task);
				if (report != null) {
					/**
					 * 2. generate report, if the file is successfully
					 * transfered, it'll also upload report file to receive
					 * center.
					 */
					boolean UpLResult = ftpclient.FtpUploadReport(report);
					if (report.getResult()) { // transfer successfully.
						if (UpLResult == true) { // It means the task
													// successful.
							/** 3. handle the successful task. */
							this.handleSuccessedTask(task);
						} else { // It means the task failed.
							/** 3. handle the failed. task. */
							this.handleFailedUploadReport(report, task);
						}
					} else { // transfer failed.
						this.handelFaildTask(task);
					}

					/** Send the real time information to Monitor Server. */
					report.setTargetCentername(this.targetName);
					monitor_client.SendMessage(XMLOperator.MakeXMLTransferTaskInfo(report));
				} else {// transfer failed.
					this.handelFaildTask(task);
				}
			} else {
				this.ftpclient.CheckStatus();
			}
		}
	}
}
