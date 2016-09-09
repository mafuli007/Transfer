package BrazilCenter.transfer.heartbeat;

import BrazilCenter.DaoUtils.Utils.LogUtils;
import BrazilCenter.HeartBeat.Utils.HeartBeatUtils;
import BrazilCenter.models.Configuration;
import BrazilCenter.models.HeartBeatObj;
import BrazilCenter.transfer.tcpService.MonitorTcpClient;
import BrazilCenter.transfer.tcpService.RequestTcpClient;

public class HeartBeat extends Thread {

	private Configuration conf;
	private HeartBeatObj hbobj;
	private MonitorTcpClient monitor_client;

	public HeartBeat(Configuration conf, MonitorTcpClient client) {
 		this.conf = conf;
 		hbobj = new HeartBeatObj();
		hbobj.setSoftwareid(this.conf.getSoftwareId());
		this.monitor_client = client;
	}

	@Override
	public void run() {
		
		LogUtils.logger.info("HeartBeat thread started!");
		while (true) {
			/** update sending time and hardware status. */
			hbobj.update();

			 
			String msg = HeartBeatUtils.MakeXMLHeartbeat(hbobj);
			if(!monitor_client.SendHeartbeatMessage(msg)){
				LogUtils.logger.debug("failed to send heartbeat..");
			}
			
			/** sleep for interval seconds. */
			try {
				long interval = this.conf.getHeartbeatInterval() * 1000;
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
