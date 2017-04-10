package BrazilCenter.transfer.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.FtpServerAddress;
import BrazilCenter.models.Task;
  import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.MD5Util;
import BrazilCenter.transfer.utils.TransferReport;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

/**
 * ftp client.
 * 
 * @author maful
 *
 */
public class FtpClient {
	private FTPClient _ftpclient = null;
	private Configuration conf;
	private FtpServerAddress centerAddress;
	private boolean isConnected = false;

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public FtpServerAddress getCenterAddress() {
		return centerAddress;
	}

	public void setCenterAddress(FtpServerAddress centerAddress) {
		this.centerAddress = centerAddress;
	}

	public FtpClient(Configuration confr, FtpServerAddress address) {
		this.conf = confr;
		this.centerAddress = address;
		_ftpclient = new FTPClient();
	}

	public boolean ReConnect() {
		synchronized (this) {
			if (this.isConnected == true) {
				return true;
			} else {
				while (true) {
					try {
						this._ftpclient.connect(this.centerAddress.getIp(), this.centerAddress.getPort());
						this._ftpclient.setControlEncoding("UTF-8");

						int reply = this._ftpclient.getReplyCode();
						if (!FTPReply.isPositiveCompletion(reply)) {
							this._ftpclient.disconnect();
							LogUtils.logger.error(Thread.currentThread().getName() + " FTP connect failed!");
						} else {
							if (!this._ftpclient.login(this.centerAddress.getUsername(),
									this.centerAddress.getPasswd())) {
								LogUtils.logger.error("FTP login failed!");
							} else {
								LogUtils.logger.info(Thread.currentThread().getName() + " FTP relogin succcessfully!");
								this._ftpclient.enterLocalPassiveMode();
								this.isConnected = true;
								return true;
							}
						}
					} catch (UnknownHostException e) {
						LogUtils.logger.error(
								Thread.currentThread().getName() + " FTP connect failed! : UnknowHostExcpetion ");
					} catch (Exception e) {
						LogUtils.logger.error(Thread.currentThread().getName() + " FTP connect error: " + e.toString());
					}
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {

					}
				}
			}
		}
	}

	public boolean ConnectServer() {
		try {
			this._ftpclient.connect(this.centerAddress.getIp(), this.centerAddress.getPort());
			this._ftpclient.setControlEncoding("UTF-8");

			int reply = this._ftpclient.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				this._ftpclient.disconnect();
				return false;
			} else {
				if (!this._ftpclient.login(this.centerAddress.getUsername(), this.centerAddress.getPasswd())) {
					return false;
				} else {
					this.isConnected = true;
				}
			}
		} catch (UnknownHostException e) {
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP connect failed : UnknowHostExcpetion ");
			return false;
		} catch (Exception e) {
			LogUtils.logger.error(Thread.currentThread().getName() + " FTP connect failed!" + e.getMessage());
			return false;
		}
		_ftpclient.enterLocalPassiveMode();
		//ftpclient.setDataTimeout(0);
		return true;
	}

	/** Try to change the working place */
	public boolean setWorkingPlace(String workingDir) {
		try {
			while (!(this._ftpclient.changeWorkingDirectory(workingDir))) {
				if (!_ftpclient.makeDirectory(workingDir)) {
					LogUtils.logger.error("Create directory " + workingDir + " failed at FTP Server!");
					return false;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Generate report file's name based on the result of upload task.
	 * 
	 * @param report
	 * @return
	 */
	private String GenerateReportFileName(TransferReport report) {

		String date_str = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		String fileName = null;

		if (false == report.getResult()) { // if failed to upload the file,
			fileName = Utils.LOCAL_REPORT_DIR + "error/" + date_str + "_" + report.getFilename() + ".xml";
		} else {
			fileName = Utils.LOCAL_REPORT_DIR + "ok/" + date_str + "_" + report.getFilename() + ".xml";
		}

		return fileName;
	}

	/**
	 * if the result of transfer task is true, it'll generate the report and
	 * upload the report, else it'll only create a report in error directory.
	 * 
	 * @param report
	 * @param result
	 * @return
	 */
	public boolean FtpUploadReport(TransferReport report) {
		if (this.isConnected == false) {
			this.ReConnect();
		}

		boolean uploadresult = false;
		String fileName = this.GenerateReportFileName(report);
		report.setOutReportName(fileName);

		/** get the string data in XML format from the report object. */
		String data = XMLOperator.MakeXMLTransferReport(report);

		File tmpfile = null;
		if (Utils.CreateFile(fileName, data)) {
			tmpfile = new File(fileName);
		} else {
			LogUtils.logger.error("Create " + fileName + " failed");
			return uploadresult;
		}

		/**
		 * if the result if false, it means we only have to create a report file
		 * in error directory. no one cares about the result in this condition.
		 */
		if (false == report.getResult()) {
			uploadresult = true;
			return uploadresult;
		}

		/** begin to upload the report file. */
		FileInputStream fis = null;
		String workingDir = this.centerAddress.getDestinationDirectory() + "CTL/";
		try {
			fis = new FileInputStream(tmpfile);
			while (!(this._ftpclient.changeWorkingDirectory(workingDir))) {
				if (!_ftpclient.makeDirectory(workingDir)) {
					LogUtils.logger.error("Make Directory failed at FTP Server!");
					return uploadresult;
				}
			}
			this._ftpclient.setBufferSize(1024);
			this._ftpclient.setControlEncoding("UTF-8");
			this._ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);
			if (this._ftpclient.storeFile(new String(tmpfile.getName().getBytes("UTF-8"), "iso-8859-1"), fis)) {
				uploadresult = true;
			} else {
				LogUtils.logger.error("Transfer report: " + tmpfile.getName() + " failed!");
				return uploadresult;
			}
		} catch (FileNotFoundException e) {
			LogUtils.logger.error("Report File not exist!: " + tmpfile.getName());
			return uploadresult;
		} catch (SocketException e) {
			LogUtils.logger.error("Network Error: " + e.getMessage());
			try {
				this._ftpclient.disconnect();
				LogUtils.logger.error("FTP disconnected! Try to connnect.....");
				if (this.ReConnect() == true) {
					return this.FtpUploadReport(report);
				}

			} catch (Exception ea) {
				ea.printStackTrace();
			}
		} catch (IOException e) {
			LogUtils.logger.error("Failed to transfer the report file: " + tmpfile.getName() + " " + e.getMessage());
			if (!this._ftpclient.isConnected()) {
				this.isConnected = false;
			}
			return uploadresult;
		} catch (Exception e) {
			LogUtils.logger.error("Failed to transfer the report file:" + tmpfile.getName() + " " + e.getMessage());
			return uploadresult;
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
				if (uploadresult == false) {
					Utils.delFile(new File(fileName));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	public TransferReport FtpUploadFile(Task task) {
		if (this.isConnected == false) {
			this.ReConnect();
		}
		boolean uploadresult = false;
		TransferReport report = new TransferReport();

		/***
		 * Try fo find if the connection is usable!
		 */
		int reply = this._ftpclient.getReplyCode();
		if (!FTPReply.isPositiveCompletion(reply)) {
			try {
				this._ftpclient.disconnect();
				this.isConnected = false;
				if (this.ReConnect() == true) {
					report = this.FtpUploadFile(task);
					return report;
				}
			} catch (Exception ea) {
				ea.printStackTrace();
			}
		}
		
		File srcFile = new File(task.getFilepath() + "/" + task.getFilename());
		if (srcFile.exists()) {
			String destination = this.centerAddress.getDestinationDirectory() + "DATA/";
			if(!this.setWorkingPlace(destination)){
				this.Close();
				this.ConnectServer();
			}
			task.setDestinationAddress(this.centerAddress.getName(), destination);
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(srcFile);
				if (!this._ftpclient.changeWorkingDirectory(destination)) {
					LogUtils.logger.error("ChangeWorkingDirectory " + destination + " Failed!");
					report.setFailReason("Failed to Change the working direcotry in FTP server");
				} else {
					this._ftpclient.setBufferSize(1024);
					this._ftpclient.setControlEncoding("UTF-8");
					this._ftpclient.setFileType(FTPClient.BINARY_FILE_TYPE);
					SimpleDateFormat startFormat = new SimpleDateFormat(Utils.dateFormat24Mis);
					report.setStartSendTime(startFormat.format(new Date()));
					if (this._ftpclient.storeFile(new String(task.getFilename().getBytes("UTF-8"), "iso-8859-1"), fis)) {
						LogUtils.logger.info(this.conf.getSoftwareId() + " To "
								+ this.centerAddress.getName() + ", " + task.getFilename() + " Successfully!");
						uploadresult = true;
						SimpleDateFormat endFormat = new SimpleDateFormat(Utils.dateFormat24Mis);
						report.setEndSendTime(endFormat.format(new Date()));
					} else {
						LogUtils.logger.info("Transfer failed : " + task.getFilename() + " Destination: "
								+ this.centerAddress.getDestinationDirectory());
						uploadresult = false;
					}
				}
			} catch (SocketException e) {
				LogUtils.logger.error("Network error： " + e.getMessage());
				try {
					this._ftpclient.disconnect();
					LogUtils.logger
							.error(Thread.currentThread().getName() + " FTP disconnected, Try to connect again.....");
					if (this.ReConnect() == true) {
						report = this.FtpUploadFile(task);
						return report;
					}
				} catch (Exception ea) {
					LogUtils.logger.error("FTP server disconnect failed!, " + this.centerAddress.getIp());
					report.setFailReason("FTP server disconnect failed!, " + this.centerAddress.getIp());
				}
			} catch (IOException e) {
				LogUtils.logger.error(Thread.currentThread().getName() + " transfer failed: try " + task.getTryCount()
						+ " times, file name: " + srcFile.getName() + ", error: " + e.getMessage());
				report.setFailReason("IO error" + e.getMessage());
				if (!this._ftpclient.isConnected()) {
					this.isConnected = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
				} catch (Exception e) {
					LogUtils.logger.error(e.getMessage());
				}
			}
			report.setSize(srcFile.length());
			String value = MD5Util.getFileMD5(srcFile);
			report.setDestinationAddress(task.getDestinationAddress(this.centerAddress.getName()));
			if (value == null) {
				report.setMd5value("No Value");
			} else {
				report.setMd5value(value);
			}
		} else {
			LogUtils.logger.error("File does not exist:  " + srcFile.getName());
			report.setSize(0);
			report.setMd5value("No Value");
		}

		report.setResult(uploadresult);
		report.setFilename(task.getFilename());
		report.setSoftwareId(this.conf.getSoftwareId());
		report.setSourceAddress(task.getFilepath());
		report.setTargetCentername(this.centerAddress.getName());

		return report;
	}

	public void Close() {
		try {
			_ftpclient.logout();
			if (_ftpclient.isConnected()) {
				_ftpclient.disconnect();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void CheckStatus() {
		// TODO Auto-generated method stub
		try {
			if (this._ftpclient.sendNoOp() == false) {
				this.setConnected(false);
				this.ReConnect();
			}
		} catch (IOException e1) {
			this.setConnected(false);
			this.ReConnect();
		}
	}
}
