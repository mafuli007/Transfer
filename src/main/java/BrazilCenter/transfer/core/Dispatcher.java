package BrazilCenter.transfer.core;

import java.util.LinkedList;
import java.util.List;

import BrazilCenter.transfer.model.FtpServerAddress;
import BrazilCenter.transfer.tasks.TASKTYPE;
import BrazilCenter.transfer.tasks.Task;
import BrazilCenter.transfer.tcpService.TcpClient;
import BrazilCenter.transfer.utils.Configuration;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.Utils;

/**
 * assign the tasks to different transfer thread.
 * @author mafuli
 */
public class Dispatcher extends Thread {

	private List<FtpTransfer> transferList; // FTP

	/**
	 * Get the transfer thread through center's name.
	 * 
	 * @param centerName
	 * @return
	 */
	private FtpTransfer getFtpTransfer(String centerName) {
		for (FtpTransfer transfer : this.transferList) {
			if (transfer.getTargetName().compareTo(centerName) == 0) {
				return transfer;
			}
		}
		return null;
	}

	/***
	 * Started the transfer thread for each receive center.
	 * 
	 * @param confr
	 * @param monitorClient
	 */
	public Dispatcher(Configuration confr, TcpClient monitorClient) {

		this.transferList = new LinkedList<FtpTransfer>();
		List<FtpServerAddress> addressList = confr.getAddresslist();
		for (int i = 0; i < addressList.size(); i++) {
			FtpTransfer ftpTransfer = new FtpTransfer(confr, monitorClient, addressList.get(i));
			ftpTransfer.setName(addressList.get(i).getName());
			ftpTransfer.start();
			this.transferList.add(ftpTransfer);
		}
	}

	/**
	 * begin to get the date from the queue, and then assign the task to each
	 * transfer thread.
	 */
	public void run() {
		Task task = null;
		while (true) {
			if ((task = Utils.sharedata.GetTask()) != null) {
				if (task.getTaskType() == TASKTYPE.NewTask) {
					/** 1. assign how subtasks for this task. */
					task.setSubTaskNumber(this.transferList.size());

					/** 2. assign task to each tranfer thread. */
					for (int i = 0; i < this.transferList.size(); i++) {
						this.transferList.get(i).addTask(task);
					}
				} else if ((task.getTaskType() == TASKTYPE.LocalFailedTask)
						|| (task.getTaskType() == TASKTYPE.RemoteTask)) {
					task.setSubTaskNumber(1);

					/** 1. get the centre's name */
					String centerName = task.getFailedCenterName().get(0);

					/**
					 * 2. get the transfer thread and add task to the center.
					 */
					FtpTransfer transfer = this.getFtpTransfer(centerName);
					if (transfer != null) {
						transfer.addTask(task);
					} else {
						LogUtils.logger.error("Configuration ERROR, Target Name doesn't exist: " + centerName
								+ ", File failed to transfer : " + task.getFilename());
					}
				}  else { 
					LogUtils.logger.error("Task Type error: " + task.getTaskType());
				}
			} else {
				try {
					Thread.sleep(5 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
