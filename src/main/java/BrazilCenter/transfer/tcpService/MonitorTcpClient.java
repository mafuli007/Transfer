package BrazilCenter.transfer.tcpService;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;

import BrazilCenter.Tcp.Client.TcpClient;

/**
 * used to send heartbeat and realtaskinfo to the monitor service.
 * 
 * @author Fuli Ma
 */
public class MonitorTcpClient extends TcpClient implements Runnable {

	private Queue<String> realInfoList;

	public synchronized void SendRealInfo(String msg) {
		this.realInfoList.add(msg);
	}

	private synchronized String getRealInfo() {
		return this.realInfoList.poll();
	}

	public MonitorTcpClient(String server_ip, int server_port) {
		super(server_ip, server_port, new Timer());
		this.realInfoList = new LinkedList<String>();

	}

	public boolean SendHeartbeatMessage(String msg) {
		return this.send(msg);
	}

	private boolean SendMsg(String msg) {
		return this.send(msg);

	}

	public void run() {
		this.RunClient();
		
		// TODO Auto-generated method stub
		while (true) {
			String msg = null;
			if ((msg = this.getRealInfo()) != null) {
				if (!this.SendMsg(msg)) { // failed to send the realtaskinfo.
					this.SendRealInfo(msg);
					try {
						Thread.sleep(5 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
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
