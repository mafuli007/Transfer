package BrazilCenter.transfer.tcpService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import BrazilCenter.transfer.utils.LogUtils;

/**
  * 
 * @author phoenix
 */
public class TcpClient extends Thread {
	private Socket s;
	private String serverIp;
	private int serverPort;
	private OutputStream out;
	private DataOutputStream dout;
	private boolean connected = false; 

	public TcpClient(String server_ip, int server_port) {
		try {
			this.serverIp = server_ip;
			this.serverPort = server_port;
			this.s = new Socket(server_ip, server_port);
			setConnected(true);
		} catch (IOException e) {
			LogUtils.logger.error("TCP connecting failed! IP:" + server_ip + "Port:" + server_port);
		}
	}

	public void Reconnect() {
		try {
			this.s = new Socket(this.serverIp, this.serverPort);
			setConnected(true);
			LogUtils.logger.info("TCP Connected!");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogUtils.logger.error("TCP connect failed! IP:" + this.serverIp + " Port:" + this.serverPort + ", Reason: " + e.getMessage());
		}
	}

	public boolean SendMessage(String msg) {
		if (false == this.connected) {
			return false;
		}
		try {

			this.out = this.s.getOutputStream();
			this.dout = new DataOutputStream(this.out);
			this.dout.writeUTF(msg);

		} catch (IOException e) {
			LogUtils.logger.error("TCP send: " + e.getMessage() + " failed!");
			this.connected = false;
			this.Close();
			return false;
		}
		return true;
	}

	public String RecvMessage() {
		if (false == this.connected) {
			return "";
		}
		String recvmsg = null;
		try {
			InputStream in = this.s.getInputStream();
			DataInputStream din = new DataInputStream(in);
			recvmsg = din.readUTF();
		} catch (IOException e) {
			LogUtils.logger.error("TCP receive message failed!: " + e.getMessage());
		}
		return recvmsg;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void Close() {
		try {
			if (this.s != null) {
				this.s.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			if (this.connected == false) {
				this.Reconnect();
			}
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}