package BrazilCenter.transfer.heartbeat;

import BrazilCenter.transfer.model.HardwareObj;
import BrazilCenter.transfer.model.HeartbeatObj;
import BrazilCenter.transfer.tcpService.TcpClient;
import BrazilCenter.transfer.utils.Configuration;
import BrazilCenter.transfer.utils.XMLOperator;

public class HeartBeat extends Thread {

	private Configuration conf;
	private HardwareObj hardwareobj;
	private HeartbeatObj hbobj;
	private TcpClient monitor_client;

	public HeartBeat(Configuration conf, TcpClient client) {
 		this.conf = conf;
 		hardwareobj = new HardwareObj();
 		hbobj = new HeartbeatObj();
		hbobj.setSoftwareId(this.conf.getSoftwareId());
		this.monitor_client = client;
	}

	@Override
	public void run() {
		
		while (true) {
			/** update sending time and hardware status. */
			hbobj.update();
			hardwareobj.update();

			 
			String msg = XMLOperator.MakeXMLHeartbeat(hbobj, hardwareobj);
			monitor_client.SendMessage(msg);
			
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
