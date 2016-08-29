package BrazilCenter.transfer.utils;

import java.util.LinkedList;
import java.util.List;


/***
 * cache all the scan files, if failed or successed, then deleted from the list; 
 * @author Fuli Ma
 *
 */
public class CacheScanFileList {

	private static List<String> scanFileList = new LinkedList<String>();	
	
	public static synchronized boolean AddToCacheScanFileList(String filename){
		return CacheScanFileList.scanFileList.add(filename);
	}
	
	public static synchronized  boolean RemoveFromCacheScanFileList(String filename){
		return CacheScanFileList.scanFileList.remove(filename);
	}
	
	public static synchronized boolean IfContainedInCacheScanFileList(String filename){
		return CacheScanFileList.scanFileList.contains(filename);
	}
	
	public static synchronized List<String> getScanFileList(){
		return CacheScanFileList.scanFileList;
	}
}
