package BrazilCenter.transfer.reUploadService;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.FileObj;
import BrazilCenter.models.TASKTYPE;
import BrazilCenter.models.Task;
 import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

/**
 * This Service is used to receive that repeat request from any receivers those
 * who wants to get the specified files again.
 * 
 * @author maful
 *
 */
public class ReUploadService {

	private ServerSocket s;
	private boolean connected = false;
	private Configuration conf;

	public ReUploadService(Configuration confr) {
		this.conf = confr;
		try {
			this.s = new ServerSocket(this.conf.getReupLoadServerPort());
			setConnected(true);
			LogUtils.logger.info("Reuploader Server Started....Port" + this.conf.getReupLoadServerPort());
		} catch (IOException e) {
			LogUtils.logger.error("TCP Server Started Failed! :" + e.getMessage());
		}
	}

	public void StartServer() {
		while (true) {
			try {
				Socket cs = this.s.accept();
				LogUtils.logger.info(cs.getInetAddress() + "��Online!");
				new ServerThread(cs, this.conf).start();
			} catch (IOException e) {
				LogUtils.logger.error(e.getMessage());
			}
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}
}

class ServerThread extends Thread {
	private Socket sock;
	private String addresses;

	public ServerThread(Socket s, Configuration conf) {
		sock = s;
		this.addresses = conf.getStoreRootDir();
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

	public void DirectoryScan(String scanAddress, String relativePath, List<FileObj> flist) {

		String address = scanAddress + File.separator + relativePath;
		File parentF = new File(address);
		if (!parentF.exists()) {
			LogUtils.logger.warn("Directory: " + address + " doesn't exist!");
		} else {
			String[] subFiles = parentF.list();
			for (int i = 0; i < subFiles.length; i++) {

				File tmpfile = new File(address + File.separator + subFiles[i]);
				long size = tmpfile.length();
				if (size != -1) {
					if (tmpfile.isFile()) {

						FileObj file = new FileObj();
						file.setName(subFiles[i]);
						file.setFilesize(size);
						file.setPath(address);
						flist.add(file);

					} else {
						String tmppath = null;
						if (relativePath.length() == 0) {
							tmppath = subFiles[i];
						} else {
							tmppath = relativePath + File.separator + subFiles[i];
						}
						DirectoryScan(scanAddress, tmppath, flist);
					}
				}
			}
		}
	}

	/**
 	 * 
	 * @param fileName
	 * @return
	 */
	public FileObj findFile(String fileName) {
		List<FileObj> tmplist = new LinkedList<FileObj>();
		String scanAddress = this.addresses;
		this.DirectoryScan(scanAddress, "", tmplist);
		for (FileObj fileobj : tmplist) {
			if (fileobj.getName().contains(fileName)) {
				return fileobj;
			}
		}
		tmplist.clear();

		return null;
	}

	public void run() {
		try {
			while (true) {
				InputStream in = sock.getInputStream();
				DataInputStream din = new DataInputStream(in);
				String msg = din.readUTF();
				if (msg.length() > 0) {
					LogUtils.logger.info(msg);
					ReUploadMsg reuploadMsgObj = XMLOperator.ParseReUploadMsgXML(msg);
					LogUtils.logger.info("CenterName: " + reuploadMsgObj.getCenterName() + " want to reupload file: "
							+ reuploadMsgObj.getFileName());

 					FileObj fileObj = this.findFile(reuploadMsgObj.getFileName());
					if (fileObj != null) {
						File tmpfile = new File(fileObj.getPath() + File.separator + fileObj.getName());
						if (this.isFileReady(tmpfile)) {

 							Task task = new Task();
							task.setTaskType(TASKTYPE.RemoteTask);
							task.setFilename(tmpfile.getName());
							task.setFilepath(tmpfile.getParent());
							task.setFilesize(tmpfile.length());
							task.setFailedCenterName(reuploadMsgObj.getCenterName());

 							Utils.transferTaskQueue.AddTask(task);
						}
					} else {
						LogUtils.logger.error("didn't find reupload file��" + reuploadMsgObj.getFileName());
					}
				}
			}
		} catch (IOException e) {
			try {
				this.sock.close();
				LogUtils.logger.info(sock.getInetAddress() + " closed! ��" + e.getMessage());
			} catch (IOException e1) {
				LogUtils.logger.error("closed unexcepted! ��" + e.getMessage());
			}
		}
	}
}
