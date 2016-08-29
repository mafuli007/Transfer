package BrazilCenter.transfer.model;


/***
 * Never be used. going to separate the function of monitoring system info.
 * @date 2016-8-26
 * @author maful
 *
 */
public class HardwareObj {

	private String hostname;
	private String localIp;
	
	private int cpuPercent;
	private int memoryPercent;
	private int diskPercent;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

	public String getLocalIp() {
		return localIp;
	}

	public void setLocalIp(String localIp) {
		this.localIp = localIp;
	}

	public int getCpuPercent() {
		return cpuPercent;
	}

	public void setCpuPercent(int cpuPercent) {
		this.cpuPercent = cpuPercent;
	}

	public int getMemoryPercent() {
		return memoryPercent;
	}

	public void setMemoryPercent(int memoryPercent) {
		this.memoryPercent = memoryPercent;
	}

	public int getDiskPercent() {
		return diskPercent;
	}

	public void setDiskPercent(int diskPercent) {
		this.diskPercent = diskPercent;
	}

	public HardwareObj() {
		this.hostname = "hostname";
		this.cpuPercent = 0;
		this.diskPercent = 0;
		this.localIp = "127.0.0.1";
		this.memoryPercent = 0;
	}

	public void update(){
		
		/** fetch use percent */
/*		InitialMemoryPercent();
		InitialCpuPercent();
		InitialFilePercent();*/
	}
	
/*	private void getProperty() {
		InetAddress addr = null;
		try {
			addr = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		this.localIp = addr.getHostAddress();
		this.hostname = addr.getHostName();*/
		
		/** fetch use percent */
/*		InitialMemoryPercent();
		InitialCpuPercent();
		InitialFilePercent();
	}
/*
	public void InitialMemoryPercent() {
		Sigar sigar = new Sigar();
		Mem mem = null;
		try {
			mem = sigar.getMem();
		} catch (Exception e) {
			e.printStackTrace();
		}

		double memory = mem.getUsedPercent();
		// System.out.println("memory = " + memory);
		this.memoryPercent =  (int) memory;
	}

	public void InitialCpuPercent() {
		Sigar sigar = new Sigar();
		CpuPerc cpuList[] = null;
		try {
			cpuList = sigar.getCpuPercList();
		} catch (SigarException e) {
			e.printStackTrace();
		}
		double combined = 0;
		for (int i = 0; i < cpuList.length; i++) {
			combined += cpuList[i].getCombined();
		}
		combined = combined / cpuList.length;

		this.cpuPercent = (int) (combined*100);
	}

	public void InitialFilePercent() {
		double usePercent = 0;
		Sigar sigar = new Sigar();
		try {

			FileSystem fslist[] = sigar.getFileSystemList();
			for (int i = 0; i < fslist.length; i++) {
				FileSystem fs = fslist[i];

				if (fs.getDirName().compareTo("D:\\") == 0) {
					FileSystemUsage usage = null;
					usage = sigar.getFileSystemUsage(fs.getDirName());
					switch (fs.getType()) {
					case 0: // TYPE_UNKNO
						break;
					case 1: // TYPE_NONE
						break;
					case 2: // TYPE_LOCAL_DISK 
						usePercent = usage.getUsePercent() * 100D;
						
						break;
					case 3:// 
						break;
					case 4:// 
						break;
					case 5://
						break;
					case 6:// 
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.diskPercent = (int) usePercent;
	}*/
}
