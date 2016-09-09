package BrazilCenter.transfer.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import BrazilCenter.models.Configuration;
import BrazilCenter.models.FtpServerAddress;
import BrazilCenter.models.TcpServerObj;
import BrazilCenter.transfer.model.ErrRecordObj;
import BrazilCenter.transfer.reUploadService.ReUploadMsg;

/**
 */
public class XMLOperator {

	private String filePath = "TransferConfig.xml";
	private Configuration conf;

	public Configuration getConfiguration() {
		return this.conf;
	}

	public XMLOperator() {
		this.conf = new Configuration();
	}

	/**
	 */
	public boolean Initial() {

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(filePath);

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("SoftwareId") == 0) {
						this.conf.setSoftwareId(strvalue);
					} else if (tmp.getNodeName().compareTo("FtpMountDirectory") == 0) {
						this.conf.setFtpMountDirectory(strvalue);
					} else if (tmp.getNodeName().compareTo("ReportAddress") == 0) {
						this.conf.setReportRootDir(strvalue);
					} else if (tmp.getNodeName().compareTo("ReupLoadServerPort") == 0) {
						this.conf.setReupLoadServerPort(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("StoreRootDir") == 0) {
						this.conf.setStoreRootDir(strvalue);
					} else if (tmp.getNodeName().compareTo("HeartbeatInterval") == 0) {
						this.conf.setHeartbeatInterval(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("ErrRecordScanInterval") == 0) {
						this.conf.setErrRecordScanInterval(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("TransferSwitch") == 0) {
						this.conf.setTransferSwitch(strvalue);
					} else if (tmp.getNodeName().compareTo("MonitorServerInfo") == 0) {
						NodeList innernodelist = tmp.getChildNodes();
						for (int m = 0; m < innernodelist.getLength(); m++) {
							Node innernode = innernodelist.item(m);
							String inerstrvalue = innernode.getTextContent();
							if (innernode.getNodeName().compareTo("MonitorServerIP") == 0) {
								this.conf.setMonitorServerIp(inerstrvalue);
							} else if (innernode.getNodeName().compareTo("MonitorServerPort") == 0) {
								this.conf.setMonitorServerPort(Integer.parseInt(inerstrvalue));
							} else {
							}
						}
					} else if (tmp.getNodeName().compareTo("ReUploadServers") == 0) {
						NodeList centerlist = tmp.getChildNodes();
						for (int c_i = 0; c_i < centerlist.getLength(); c_i++) {
							if (centerlist.item(c_i).getNodeName().compareTo("ReUploadServerInfo") == 0) {
								NodeList innernodelist = centerlist.item(c_i).getChildNodes();
								TcpServerObj serverObj = new TcpServerObj();
								for (int m = 0; m < innernodelist.getLength(); m++) {
									Node innernode = innernodelist.item(m);
									String inerstrvalue = innernode.getTextContent();
									if (innernode.getNodeName().compareTo("softwareId") == 0) {
										serverObj.setSoftwareId(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("ReUploadServerIp") == 0) {
										serverObj.setIp(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("ReUploadServerPort") == 0) {
										serverObj.setPort(Integer.parseInt(inerstrvalue));
									} else {
									}
								}
								this.conf.addReuploadServer(serverObj);
							}
						}
					} else if (tmp.getNodeName().compareTo("ReceiveCenters") == 0) {
						NodeList centerlist = tmp.getChildNodes();
						for (int c_i = 0; c_i < centerlist.getLength(); c_i++) {
							if (centerlist.item(c_i).getNodeName().compareTo("ReceiverFTPinfo") == 0) {
								NodeList innernodelist = centerlist.item(c_i).getChildNodes();
								FtpServerAddress address = new FtpServerAddress();
								for (int m = 0; m < innernodelist.getLength(); m++) {
									Node innernode = innernodelist.item(m);
									String inerstrvalue = innernode.getTextContent();
									if (innernode.getNodeName().compareTo("FTPIp") == 0) {
										address.setIp(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("CenterName") == 0) {
										address.setName(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("FTPPort") == 0) {
										address.setPort(Integer.parseInt(inerstrvalue));
									} else if (innernode.getNodeName().compareTo("FTPUserName") == 0) {
										address.setUsername(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("FTPPasswd") == 0) {
										address.setPasswd(inerstrvalue);
									} else if (innernode.getNodeName().compareTo("FTPDirectory") == 0) {
										address.setDestinationDirectory(inerstrvalue);
									} else {
									}
								}
								this.conf.AddAddress(address);
							}
						}
					} else {

					}
				}
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (ParserConfigurationException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (SAXException e) {
			System.out.println(e.getMessage());
			return false;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return false;
		} finally {
		}
		return true;
	}

	/***
	 * 
	 * @param obj
	 * @return String
	 */
	public static String MakeErrRecordXML(ErrRecordObj obj) {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return null;
		}

		String transferReportstr = null;

		Element root = document.createElement("info");
		document.appendChild(root);

		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode("FailedInfo"));
		root.appendChild(messagetype);

		Element centreName = document.createElement("TargetName");
		centreName.appendChild(document.createTextNode(obj.getTargetCenterName()));
		root.appendChild(centreName);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Element sendtime = document.createElement("SendTime");
		sendtime.appendChild(document.createTextNode(format.format(new Date())));
		root.appendChild(sendtime);

		Element sourceaddress = document.createElement("FilePath");
		sourceaddress.appendChild(document.createTextNode(obj.getFilePath()));
		root.appendChild(sourceaddress);

		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(obj.getFileName()));
		root.appendChild(filename);

		Element failedTimes = document.createElement("FailedTimes");
		failedTimes.appendChild(document.createTextNode(String.valueOf(obj.getFailedTimes())));
		root.appendChild(failedTimes);

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			transferReportstr = bos.toString();

		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}

		return transferReportstr;
	}

	/**
	 * 
	 * @param fileName
	 * @return ErrRecordObj
	 */
	public static ErrRecordObj ParseErrRecordXML(String fileName) {
		ErrRecordObj errObj = new ErrRecordObj();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fileName);

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("MessageType") == 0) {

					} else if (tmp.getNodeName().compareTo("FilePath") == 0) {
						errObj.setFilePath(strvalue);
					} else if (tmp.getNodeName().compareTo("FileName") == 0) {
						errObj.setFileName(strvalue);
					} else if (tmp.getNodeName().compareTo("FailedTimes") == 0) {
						errObj.setFailedTimes(Integer.parseInt(strvalue));
					} else if (tmp.getNodeName().compareTo("TargetName") == 0) {
						errObj.setTargetCenterName(strvalue);
					} else {

					}
				}
			}
		} catch (Exception e) {
			LogUtils.logger.error("parse error record failed. " + fileName);
			return null;
		}
		return errObj;
	}

	/**
	 */
	public static UploadReport ParseUploadReportXML(String fileName) {
		UploadReport report = new UploadReport();
		FileInputStream fis = null;
		try {
			File f = new File(fileName);
			fis = new FileInputStream(f);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(fis);

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("MessageType") == 0) {

					} else if (tmp.getNodeName().compareTo("SoftwareId") == 0) {
						report.setSoftwareId(strvalue);
					} else if (tmp.getNodeName().compareTo("SendTime") == 0) {
						report.setSendTime(strvalue);
					} else if (tmp.getNodeName().compareTo("SourceAddress") == 0) {
						report.setSourceAddress(strvalue);
					} else if (tmp.getNodeName().compareTo("DestinationAddress") == 0) {
						report.setDestinationAddress(strvalue);
					} else if (tmp.getNodeName().compareTo("FileName") == 0) {
						report.setFilename(strvalue);
					} else if (tmp.getNodeName().compareTo("Md5Value") == 0) {
						report.setMd5value(strvalue);
					} else if (tmp.getNodeName().compareTo("Result") == 0) {
						if (strvalue.compareTo("success") == 0) {
							report.setResult(true);
						} else {
							report.setResult(false);
						}
					} else {

					}
				}
			}
		} catch (Exception e) {
			LogUtils.logger.error("Failed to parse report: " + fileName);
			return null;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					LogUtils.logger.error(e.getMessage());
				}
			}
		}

		return report;
	}

	/**
	 * parse msg into Reupload object.
	 */
	public static ReUploadMsg ParseReUploadMsgXML(String msg) {
		ReUploadMsg msgObj = new ReUploadMsg();
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(msg)));

			NodeList items = document.getChildNodes();
			for (int i = 0; i < items.getLength(); i++) {
				Node value = items.item(i);
				NodeList values = value.getChildNodes();
				for (int j = 0; j < values.getLength(); j++) {
					Node tmp = values.item(j);
					String strvalue = tmp.getTextContent();
					if (tmp.getNodeName().compareTo("MessageType") == 0) {

					} else if (tmp.getNodeName().compareTo("CenterName") == 0) {
						msgObj.setCenterName(strvalue);
					} else if (tmp.getNodeName().compareTo("FileName") == 0) {
						msgObj.setFileName(strvalue);
					} else {

					}
				}
			}
		} catch (Exception e) {
			LogUtils.logger.error("Parse reupload message error.");
			return null;
		}

		return msgObj;
	}

	/**
	 * 
	 * @param report
	 * @return
	 */
	public static String MakeXMLTransferReport(TransferReport report) {
		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return null;
		}

		String transferReportstr = null;

		Element root = document.createElement("info");
		document.appendChild(root);

		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode(report.getMessageType()));
		root.appendChild(messagetype);

		Element centreName = document.createElement("TargetName");
		centreName.appendChild(document.createTextNode(report.getTargetCentername()));
		root.appendChild(centreName);

		Element softwareid = document.createElement("SoftwareId");
		softwareid.appendChild(document.createTextNode(report.getSoftwareId()));
		root.appendChild(softwareid);

		Element sendtime = document.createElement("SendTime");
		sendtime.appendChild(document.createTextNode(report.getEndSendTime()));
		root.appendChild(sendtime);

		Element sourceaddress = document.createElement("SourceAddress");
		sourceaddress.appendChild(document.createTextNode(report.getSourceAddress()));
		root.appendChild(sourceaddress);

		Element destinationaddress = document.createElement("DestinationAddress");
		destinationaddress.appendChild(document.createTextNode(report.getDestinationAddress()));
		root.appendChild(destinationaddress);

		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(report.getFilename()));
		root.appendChild(filename);

		Element md5value = document.createElement("Md5Value");
		md5value.appendChild(document.createTextNode(report.getMd5value()));
		root.appendChild(md5value);

		Element result = document.createElement("Result");
		String resultstr = null;
		if (report.getResult()) {
			resultstr = "success";
		} else {
			resultstr = "fail";
		}
		result.appendChild(document.createTextNode(resultstr));
		root.appendChild(result);

		Element failReason = document.createElement("FailReason");
		failReason.appendChild(document.createTextNode(report.getFailReason()));
		root.appendChild(failReason);

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			transferReportstr = bos.toString();

		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}

		return transferReportstr;
	}

	/**
	 */
	public static String MakeXMLUploadReport(UploadReport report) {

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return null;
		}

		String uploadreportstr = null;
		Element root = document.createElement("info");
		document.appendChild(root);

		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode(report.getMessageType()));
		root.appendChild(messagetype);

		Element softwareid = document.createElement("SoftwareId");
		softwareid.appendChild(document.createTextNode(report.getSoftwareId()));
		root.appendChild(softwareid);

		Element sendtime = document.createElement("SendTime");
		sendtime.appendChild(document.createTextNode(report.getSendTime()));
		root.appendChild(sendtime);

		Element sourceaddress = document.createElement("SourceAddress");
		sourceaddress.appendChild(document.createTextNode(report.getSourceAddress()));
		root.appendChild(sourceaddress);

		Element destinationaddress = document.createElement("DestinationAddress");
		destinationaddress.appendChild(document.createTextNode(report.getDestinationAddress()));
		root.appendChild(destinationaddress);

		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(report.getFilename()));
		root.appendChild(filename);

		Element md5value = document.createElement("Md5Value");
		md5value.appendChild(document.createTextNode(report.getMd5value()));
		root.appendChild(md5value);

		Element result = document.createElement("Result");
		String resultstr = null;
		if (report.getResult()) {
			resultstr = "success";
		} else {
			resultstr = "fail";
		}
		result.appendChild(document.createTextNode(resultstr));
		root.appendChild(result);

		Element failReason = document.createElement("FailReason");
		failReason.appendChild(document.createTextNode(report.getFailReason()));
		root.appendChild(failReason);

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			uploadreportstr = bos.toString();

		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}

		return uploadreportstr;
	}

	/**
	
	 */
	public static String MakeXMLTransferTaskInfo(TransferReport report) {

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return null;
		}

		String uploadreportstr = null;
		Element root = document.createElement("info");
		document.appendChild(root);

		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode("TaskInfo"));
		root.appendChild(messagetype);

		Element softwareid = document.createElement("SoftwareId");
		softwareid.appendChild(document.createTextNode(report.getSoftwareId()));
		root.appendChild(softwareid);

		Element startSendtime = document.createElement("StartTime");
		startSendtime.appendChild(document.createTextNode(report.getStartSendTime()));
		root.appendChild(startSendtime);

		Element endSendtime = document.createElement("EndTime");
		endSendtime.appendChild(document.createTextNode(report.getStartSendTime()));
		root.appendChild(endSendtime);

		Element targetCenterName = document.createElement("TargetSoftwareId");
		targetCenterName.appendChild(document.createTextNode(report.getTargetCentername()));
		root.appendChild(targetCenterName);

		Element filename = document.createElement("FileName");
		filename.appendChild(document.createTextNode(report.getFilename()));
		root.appendChild(filename);

		Element filesize = document.createElement("FileSize");
		filesize.appendChild(document.createTextNode(String.valueOf(report.getSize())));
		root.appendChild(filesize);

		Element sourceaddress = document.createElement("SourceAddress");
		sourceaddress.appendChild(document.createTextNode(report.getSourceAddress()));
		root.appendChild(sourceaddress);

		Element result = document.createElement("Result");
		String resultstr = null;
		if (report.getResult()) {
			resultstr = "success";
		} else {
			resultstr = "fail";
		}
		result.appendChild(document.createTextNode(resultstr));
		root.appendChild(result);

		Element failReason = document.createElement("FailReason");
		failReason.appendChild(document.createTextNode(report.getFailReason()));
		root.appendChild(failReason);

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			uploadreportstr = bos.toString();

		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}
		return uploadreportstr;
	}

	public static String MakeXMLReUploadMsg(String filename, String centerName) {

		Document document = null;
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			document = builder.newDocument();
		} catch (ParserConfigurationException e) {
			LogUtils.logger.error(e.getMessage());
			return null;
		}

		String uploadreportstr = null;
		Element root = document.createElement("info");
		document.appendChild(root);

		Element messagetype = document.createElement("MessageType");
		messagetype.appendChild(document.createTextNode("ReUploadMessage"));
		root.appendChild(messagetype);

		Element CenterName = document.createElement("CenterName");
		CenterName.appendChild(document.createTextNode(centerName));
		root.appendChild(CenterName);

		Element startSendtime = document.createElement("FileName");
		startSendtime.appendChild(document.createTextNode(filename));
		root.appendChild(startSendtime);

		TransformerFactory tf = TransformerFactory.newInstance();
		ByteArrayOutputStream bos = null;
		try {
			Transformer t = tf.newTransformer();
			t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			bos = new ByteArrayOutputStream();
			t.transform(new DOMSource(document), new StreamResult(bos));
			uploadreportstr = bos.toString();

		} catch (Exception e) {
			LogUtils.logger.error(e.getMessage());
		}
		return uploadreportstr;
	}
}
