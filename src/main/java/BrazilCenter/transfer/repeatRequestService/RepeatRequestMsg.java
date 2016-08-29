package BrazilCenter.transfer.repeatRequestService;

/**
 * Used to describe information that needed to get a file from Reupload Server. 
 * @author Fuli Ma
 *
 */
public class RepeatRequestMsg {

	/** softwareID of the data source. */
	private String sourceClientId;	
	private String fileName;
	private int type; 	// 0: send message to all the servers, 1: send the message to the specified server.
	private long lastTryDate;

	public RepeatRequestMsg(String clientid, String filename) {
		this.sourceClientId = clientid;
		this.fileName = filename;
		this.lastTryDate = 0;
	}

	public RepeatRequestMsg( String filename) {
		this.fileName = filename;
	}
	
	public String getClientId() {
		return sourceClientId;
	}

	public void setClientId(String clientId) {
		this.sourceClientId = clientId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getLastTryDate() {
		return lastTryDate;
	}

	public void setLastTryDate(long lastTryDate) {
		this.lastTryDate = lastTryDate;
	}
}
