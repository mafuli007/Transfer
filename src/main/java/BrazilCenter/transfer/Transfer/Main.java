package BrazilCenter.transfer.Transfer;

import org.apache.log4j.PropertyConfigurator;

import BrazilCenter.transfer.core.Dispatcher;
import BrazilCenter.transfer.heartbeat.HeartBeat;
import BrazilCenter.transfer.reUploadService.ReUploadService;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestService;
import BrazilCenter.transfer.scanner.ErrRecordScanner;
import BrazilCenter.transfer.scanner.Scanner;
import BrazilCenter.transfer.tcpService.TcpClient;
import BrazilCenter.transfer.utils.Configuration;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.XMLOperator;

public class Main {
	
	public static Configuration conf = null;
	public static void main(String args[]) throws Exception {

 		XMLOperator xmloperator = new XMLOperator();
		if (!xmloperator.Initial()) {
			LogUtils.logger.error("Parsing XML configuration failed!");
			return;
		}
		Main.conf = xmloperator.getConfiguration();
		PropertyConfigurator.configure("./log4j.properties");
		LogUtils.logger.info("Parsing XML configuration!");

		/** Initial the monitor thread*/
		TcpClient monitor_client = new TcpClient(conf.getMonitorServerIp(), conf.getMonitorServerPort());
		monitor_client.start();
		if (monitor_client.isConnected()) {
			LogUtils.logger.info("Initialing monitor thread!");
		} else {
			LogUtils.logger.error("Initialing monitor thread failed!");
		}
		HeartBeat heatbeat = new HeartBeat(conf, monitor_client);
		heatbeat.start();

		/** Initial scanning thread */
		Scanner server = new Scanner(Main.conf);
		Thread scanThread = new Thread(server);
		scanThread.start();

		/** Initial reget file service */
		RepeatRequestService reGetFileService = new RepeatRequestService(conf);
		reGetFileService.start();

 		if (Main.conf.getTransferSwitch().compareTo("yes") == 0) {
			ErrRecordScanner errDataScan = new ErrRecordScanner(Main.conf);
			Thread errScanThread = new Thread(errDataScan);
			errScanThread.start();

 			Thread dispatchThread = new Thread(new Dispatcher(Main.conf, monitor_client));
			dispatchThread.start();
			
			/** Initial reupload service*/
			ReUploadService reUploadService = new ReUploadService(conf);
			reUploadService.StartServer();
		}
	}
}
