package BrazilCenter.transfer.repeatRequestService;

import java.util.Date;
import java.util.List;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.TcpServerObj;
import BrazilCenter.transfer.tcpService.TcpClient;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.Utils;
import BrazilCenter.transfer.utils.XMLOperator;

/**
 * This Service is used to send message to reUploadServer when there's any need
 * to get files again.
 * 
 * @author mafuli
 *
 */
public class RepeatRequestService extends Thread {

	public static RepeatRequestMsgList shareMsgList = new RepeatRequestMsgList();
	private List<TcpServerObj> serverList;
	private Configuration conf;

	public RepeatRequestService(Configuration confrr) {
		this.serverList = confrr.getReUploadServers();
		this.conf = confrr;
	}

	private TcpServerObj getTcpServerObj(String clientId) {
		for (TcpServerObj obj : this.serverList) {
			if (obj.getSoftwareId().equals(clientId)) {
				return obj;
			}
		}
		return null;
	}

	/**
	 * send the message to the reupload server.
	 */
	private void SendMsgToReuploadServer(TcpServerObj serverObj, RepeatRequestMsg message) {

		/** 1. connnect to the reupload server */
		TcpClient client = new TcpClient(serverObj.getIp(), serverObj.getPort());

		if (client.isConnected() == true) {
			/** 2. create reuploadMsg */
			String sendingMsg = XMLOperator.MakeXMLReUploadMsg(message.getFileName(), this.conf.getSoftwareId());

			/** 3. send the message */
			if (client.SendMessage(sendingMsg) == false) {
				Date now = new Date();
				message.setLastTryDate(now.getTime());
				message.setType(1);
				message.setClientId(serverObj.getSoftwareId());
				RepeatRequestService.shareMsgList.AddMsg(message);
			} else {
				LogUtils.logger.info("Sending Message: " + sendingMsg);
			}
			client.Close();
		} else {
			message.setType(1);
			message.setClientId(serverObj.getSoftwareId());
			RepeatRequestService.shareMsgList.AddMsg(message);
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void run() {
		TcpServerObj serverObj = null;
		RepeatRequestMsg msg = null;
		LogUtils.logger.info("RepeatRequest Service Start.....");
		while (true) {
			if ((msg = RepeatRequestService.shareMsgList.GetMsg()) != null) {
				long date = msg.getLastTryDate();
				Date tmpnow = new Date();
				long diff = tmpnow.getTime() - date;
				if ((date == 0) || (diff > Utils.TRYCONNECTINTERVAL)) {
					if (msg.getType() == 0) {
						/** should send the request to every server. */
						for (TcpServerObj obj : this.serverList) {
							this.SendMsgToReuploadServer(obj, msg);
						}

					} else { // send the message to only one
						/**
						 * if the type is 1, try to get the file from the
						 * specified server.
						 */
						serverObj = this.getTcpServerObj(msg.getClientId());
						if (serverObj == null) {
							LogUtils.logger.error("RepeatRequest server doesn't exist: " + msg.getClientId());
						}else{
							this.SendMsgToReuploadServer(serverObj, msg);
						}
					}
				} else {
					RepeatRequestService.shareMsgList.AddMsg(msg);
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			} else {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
