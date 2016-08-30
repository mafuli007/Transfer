package BrazilCenter.transfer.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import BrazilCenter.DaoUtils.Utils.LogUtils;
import BrazilCenter.transfer.core.TaskQueue;

/**
 */
public class Utils {

	public static String dateFormat24 = "yyyy-MM-dd HH:mm:ss";
	public static String dateFormat24Mis = "yyyy-MM-dd HH:mm:ss.SSS";
	public static String dateFormat12 = "yyyy-MM-dd hh:mm:ss";
	public static String dateDayFormat = "yyyyMMdd";

	public final static int SCAN_INTERVAL = 5; 
	public final static int FtpStatusCheckInterval = 10;	
	public static String REPORT_INVALID_MD5_DIR = "M5D_Failed"; // used to store the xml file that has invalid md5 value.
	public static String REPORT_INVALID_XML_DIR = "InvalidReport";	//used to store the xml file that failed to parse.
	public static String LOCAL_REPORT_DIR = "./report/";	
	public final static String ErrDataDir = "FailedRecords";	
	public final static int MAXTryCount = 2;	
	public final static int TRYCONNECTINTERVAL = 6000; 
	public static TaskQueue transferTaskQueue = new TaskQueue(); //used to store the transfer tasks.
	public static TaskQueue storeTaskQueue = new TaskQueue(); // used to keep the store tasks.
	
	/**
	 */
	public static boolean isFileUnlocked(File file) {
		try {
			FileInputStream in = new FileInputStream(file);
			if (in != null)
				in.close();
			return true;
		} catch (FileNotFoundException e) {
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	/**
	 * format the millisecond into *d*h*m*s . 
	 * @param ms
	 * @return
	 */
	public static String formatTime(Long ms) {
		Integer ss = 1000;
		Integer mi = ss * 60;
		Integer hh = mi * 60;
		Integer dd = hh * 24;

		Long day = ms / dd;
		Long hour = (ms - day * dd) / hh;
		Long minute = (ms - day * dd - hour * hh) / mi;
		Long second = (ms - day * dd - hour * hh - minute * mi) / ss;

		StringBuffer sb = new StringBuffer();
		if (day >= 0) {
			sb.append(day + "d ");
		}
		if (hour >= 0) {
			sb.append(hour + "h ");
		}
		if (minute >= 0) {
			sb.append(minute + "m ");
		}
		if (second >= 0) {
			sb.append(second + "s");
		}
		return sb.toString();
	}
 
	public static boolean delFile(File file) {
		return FileUtils.deleteQuietly(file);
	}

	public static boolean CopyFile(String oldPath, String newPath) {
		 try {
			FileUtils.copyFile(new File(oldPath), new File(newPath));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogUtils.logger.error("Failed to copy: " + oldPath + ", error: " +e.getMessage());
			return false;
		}
	}

	public static boolean MoveFile(String oldPath, String newPath) {
		try {
			FileUtils.moveFile(new File(oldPath), new File(newPath));
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LogUtils.logger.error("Failed to move file: " + oldPath + ", " + e.getMessage());
			return false;
		}
	}

	public static boolean CreateFile(String fileName, String data) {
		File tmpfile = new File(fileName);
		try {
			if (!tmpfile.exists()) {
				if (!tmpfile.getParentFile().exists()) {
					tmpfile.getParentFile().mkdirs();
				}
				tmpfile.createNewFile();
			}
			FileOutputStream fop = null;
			try {
				fop = new FileOutputStream(tmpfile);
				byte[] contentInBytes = data.getBytes();
				fop.write(contentInBytes);
				fop.flush();
				fop.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fop != null) {
						fop.close();
					}
				} catch (IOException e) {
					LogUtils.logger.error(e.getMessage());
				}
			}
		} catch (IOException e) {
			LogUtils.logger.error(e.getMessage());
		}
		return true;
	}
}
