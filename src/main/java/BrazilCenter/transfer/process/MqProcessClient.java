package BrazilCenter.transfer.process;

import java.io.File;
import java.io.IOException;

import BrazilCenter.Process.mqClient.RabbitMqProcessClient;
import BrazilCenter.models.Configuration;
import BrazilCenter.models.Task;
import BrazilCenter.transfer.utils.CacheScanFileList;
import BrazilCenter.transfer.utils.Utils;

/**
 * send the messag to the Processing Service.
 * 
 * @author Fuli Ma
 *
 */
public class MqProcessClient extends RabbitMqProcessClient implements Runnable{

	public MqProcessClient(Configuration conf) throws IOException {
		super("BrazilStoreQueue", conf);
	}

	/**
	 * if the transfer function is closed. then process client is responsible
	 * for moving the report files.
	 */
	private boolean MoveInputReportToOkDir(Task task) {
		String inUploadFileNameWithPath = task.getInputReportpath() + File.separator + task.getInputReportName();
		String fileToDir = task.getInputReportpath() + File.separator + "ok" + File.separator
				+ task.getInputReportName();
		return Utils.MoveFile(inUploadFileNameWithPath, fileToDir);
	}

	public void run() {
		// TODO Auto-generated method stub
		Task task = null;
		while (true) {
			if ((task = Utils.storeTaskQueue.GetTask()) != null) {

				/** 1. send the task the processing service. */
				if (this.sendTaskToSever(task)) {

				} else { // failed to send the task to the server.
					Utils.storeTaskQueue.AddTask(task);
				}

				/** 2. try to delete the report file. */
				if (this.getConf().getTransferSwitch().compareTo("no") == 0) {
					// only used as a receiver, don't have to transfer files.
					this.MoveInputReportToOkDir(task);
					CacheScanFileList.RemoveFromCacheScanFileList(task.getInputReportName());
				}
			}
		}
	}
}
