package BrazilCenter.transfer.model;

/**
  **/
public class FtpServerAddress {
	private String name;	//center's name, actually is the softwareID
	
	private String ip;
	private int port;
	private String destinationDirectory;
	private String username; 
	private String passwd; 

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getDestinationDirectory() {
		return destinationDirectory;
	}

	public void setDestinationDirectory(String destinationDirectory) {
		this.destinationDirectory = destinationDirectory;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPasswd() {
		return passwd;
	}

	public void setPasswd(String passwd) {
		this.passwd = passwd;
	}

	@Override
	public String toString() {
		return "FtpServerAddress [name=" + name + ", ip=" + ip + ", port=" + port + ", destinationDirectory="
				+ destinationDirectory + ", username=" + username + ", passwd=" + passwd + "]";
	}

}
