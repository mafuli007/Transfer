package BrazilCenter.transfer.Transfer;

import org.apache.log4j.PropertyConfigurator;

import BrazilCenter.models.Configuration;
import BrazilCenter.transfer.core.Dispatcher;
import BrazilCenter.transfer.heartbeat.HeartBeat;
import BrazilCenter.transfer.processClient.MqProcessClient;
import BrazilCenter.transfer.reUploadService.ReUploadService;
import BrazilCenter.transfer.repeatRequestService.RepeatRequestService;
import BrazilCenter.transfer.scanner.ErrRecordScanner;
import BrazilCenter.transfer.scanner.Scanner;
import BrazilCenter.transfer.tcpService.MonitorTcpClient;
import BrazilCenter.transfer.tcpService.RequestTcpClient;
import BrazilCenter.transfer.utils.LogUtils;
import BrazilCenter.transfer.utils.XMLOperator;

public class Main {

	public static Configuration conf = null;

	public static void main(String args[]) throws Exception {

		XMLOperator xmloperator = new XMLOperator();
		if (!xmloperator.Initial()) {
			LogUtils.logger.error("parsing XML configuration failed!");
			return;
		}
		Main.conf = xmloperator.getConfiguration();
		PropertyConfigurator.configure("./log4j.properties");
		LogUtils.logger.info("parsing XML configuration successfully");

		/** Initial the monitor thread */
		MonitorTcpClient monitor_client = new MonitorTcpClient(conf.getMonitorServerIp(), conf.getMonitorServerPort());
		Thread monitorThread = new Thread(monitor_client);

		HeartBeat heatbeat = new HeartBeat(conf, monitor_client);
		heatbeat.start();
		monitorThread.start();

		LogUtils.logger.info("heartbeat started.");
		
		/** Initial scanning thread */
		Scanner server = new Scanner(Main.conf);
		Thread scanThread = new Thread(server);
		scanThread.start();
		LogUtils.logger.info("scanner started.");

		/** Initial reget file service */
		RepeatRequestService reGetFileService = new RepeatRequestService(conf);
		reGetFileService.start();

		/** Initial process client */
		MqProcessClient processClient = new MqProcessClient(conf);
		Thread processThread = new Thread(processClient);
		processThread.start();

		if (Main.conf.getTransferSwitch().compareTo("yes") == 0){
			ErrRecordScanner errDataScan = new ErrRecordScanner(Main.conf);
			Thread errScanThread = new Thread(errDataScan);
			errScanThread.start();

			Thread dispatchThread = new Thread(new Dispatcher(Main.conf, monitor_client));
			dispatchThread.start();

			/** Initial reupload service, ###this one should always be the last step. */
			LogUtils.logger.info("reupload service started.");
			ReUploadService reUploadService = new ReUploadService(conf);
			reUploadService.StartServer();
		}
	}
}
