/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.info;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.Configuration;
import eu.ascens_ist.scp.node.core.EnvironmentSensor;
import eu.ascens_ist.scp.node.core.strategy.IStrategy;

/**
 * 
 * Contains all the information about one node in the network (i.e. not only the
 * own node, possibly also other nodes)
 * 
 * @author A. Zeblin, A. Dittrich
 * 
 */
public class NodeInfo implements Serializable, Comparable<NodeInfo> {

	private transient boolean updateCpuCores;
	private transient boolean updateCpuSpeed;
	private transient boolean updateCpuModel;
	private transient boolean updateCpuLoad;
	private transient boolean updateMemTotal;
	private transient boolean updateMemUsed;
	private transient boolean updateMemFree;
	private transient boolean updateDiskTotal;
	private transient boolean updateDiskFree;
	private transient boolean updateLocation;
	private transient boolean updateVirtualized;
	private transient boolean updateDeploymentId;
	private transient boolean updateIsSingleNode;

	private transient IStrategy strategy;

	private static final long serialVersionUID = 1L;
	private final InetAddress address;
	private final int port;
	private final Id id;
	private final String fullId;
	private long timestamp;

	private int cpuCores = 0;
	private int cpuSpeed = 0; // in MHz
	private String cpuModel = "";
	private double cpuLoad = 0;
	private long memTotal = 0; // in MB
	private long memUsed = 0; // in MB
	private long memFree = 0; // in MB
	private long diskTotal = 0; // in MB
	private long diskFree = 0; // in MB
	private boolean virtualized = false;
	private int deploymentId = 0;
	private boolean singleNode = false;

	private NodeLocation location = NodeLocation.MUNICH;

	private Map<AppInfo, Map<String, String>> appRoles;

	public NodeInfo(Id id, InetAddress address, int port, IStrategy strategy) {
		this.id = id;
		this.fullId = id.toStringFull();
		this.address = address;
		this.port = port;
		this.strategy = strategy;
		this.appRoles = new HashMap<>();

		this.updateCpuCores = false;
		this.updateCpuSpeed = false;
		this.updateCpuModel = false;
		this.updateCpuLoad = false;
		this.updateMemTotal = false;
		this.updateMemUsed = false;
		this.updateMemFree = false;
		this.updateDiskTotal = false;
		this.updateDiskFree = false;
		this.updateLocation = false;
		this.updateVirtualized = false;
		this.updateDeploymentId = false;
		this.updateIsSingleNode = false;
		updateSystemInformation();

		markUpdated();
	}

	public synchronized void initialize() {
		this.updateCpuCores = true;
		this.updateCpuSpeed = true;
		this.updateCpuModel = true;
		this.updateCpuLoad = true;
		this.updateMemTotal = true;
		this.updateMemUsed = true;
		this.updateMemFree = true;
		this.updateDiskTotal = true;
		this.updateDiskFree = true;
		this.updateLocation = true;
		this.updateVirtualized = true;
		this.updateDeploymentId = true;
		this.updateIsSingleNode = true;
	}

	public synchronized void updateSystemInformation() {
		if (updateCpuCores)
			cpuCores = EnvironmentSensor.getCPUCores();
		if (updateCpuSpeed)
			cpuSpeed = EnvironmentSensor.getCPUSpeed();
		if (updateCpuModel)
			cpuModel = EnvironmentSensor.getCPUModel();
		if (updateCpuLoad)
			cpuLoad = EnvironmentSensor.getCPULoad();
		if (updateMemTotal)
			memTotal = EnvironmentSensor.getMEMTotal();
		if (updateMemUsed)
			memUsed = EnvironmentSensor.getMEMUsed();
		if (updateMemFree)
			memFree = EnvironmentSensor.getMEMFree();
		if (updateDiskTotal)
			diskTotal = EnvironmentSensor.getTotalDiskSpace();
		if (updateDiskFree)
			diskFree = EnvironmentSensor.getFreeDiskSpace();
		if (updateLocation) {
			// can't be updated automatically
		}
		if (updateDeploymentId) {
			// can't be updated automatically
		}
		if (updateVirtualized)
			virtualized = EnvironmentSensor.getVirtualized();
		if (updateIsSingleNode)
			singleNode = this.strategy.getSCPNode().getPastryNode().getLeafSet().getUniqueCount() == 1;

		markUpdated();
	}

	public InetAddress getAddress() {
		return address;
	}

	public Id getId() {
		return id;
	}

	public String getIdString() {
		return getId().toString();
	}

	public String getFullIdString() {
		return getId().toStringFull();
	}

	public String getAddressString() {
		return address.toString();
	}

	public NodeLocation getLocation() {
		return this.location;
	}

	public String getLocationString() {
		return this.location.getLocationString();
	}

	public int getPort() {
		return port;
	}

	public InetSocketAddress getSocketAddress() {
		return new InetSocketAddress(address, port);
	}

	public int getCpuCores() {
		return cpuCores;
	}

	public int getCpuSpeed() {
		return cpuSpeed;
	}

	public String getCpuModel() {
		return cpuModel;
	}

	public double getCpuLoad() {
		return cpuLoad;
	}

	public long getMemTotal() {
		return memTotal;
	}

	public long getMemUsed() {
		return memUsed;
	}

	public long getMemFree() {
		return memFree;
	}

	public long getDiskTotal() {
		return diskTotal;
	}

	public long getDiskFree() {
		return diskFree;
	}

	public boolean isVirtualized() {
		return virtualized;
	}

	public int getDeploymentId() {
		return deploymentId;
	}

	public boolean isSingleNode() {
		return singleNode;
	}

	public synchronized void setCpuCores(int cpuCores) {
		updateCpuCores = false;
		this.cpuCores = cpuCores;
		markUpdated();
	}

	public synchronized void setCpuSpeed(int cpuSpeed) {
		updateCpuSpeed = false;
		this.cpuSpeed = cpuSpeed;
		markUpdated();
	}

	public synchronized void setCpuModel(String cpuModel) {
		updateCpuModel = false;
		this.cpuModel = cpuModel;
		markUpdated();
	}

	public synchronized void setCpuLoad(double cpuLoad) {
		updateCpuLoad = false;
		this.cpuLoad = cpuLoad;
		markUpdated();
	}

	public synchronized void setMemTotal(long memTotal) {
		updateMemTotal = false;
		this.memTotal = memTotal;
		markUpdated();
	}

	public synchronized void setMemUsed(long memUsed) {
		updateMemUsed = false;
		this.memUsed = memUsed;
		markUpdated();
	}

	public synchronized void setMemFree(long memFree) {
		updateMemFree = false;
		this.memFree = memFree;
		markUpdated();
	}

	public synchronized void setDiskTotal(long diskTotal) {
		updateDiskTotal = false;
		this.diskTotal = diskTotal;
		markUpdated();
	}

	public synchronized void setDiskFree(long diskFree) {
		updateDiskFree = false;
		this.diskFree = diskFree;
		markUpdated();
	}

	public synchronized void setLocation(NodeLocation location) {
		updateLocation = false;
		this.location = location;
		markUpdated();
	}

	public synchronized void setVirtualized(boolean virtualized) {
		updateVirtualized = false;
		this.virtualized = virtualized;
		markUpdated();
	}

	public synchronized void setDeploymentId(int id) {
		updateDeploymentId = false;
		this.deploymentId = id;
		markUpdated();
	}

	public boolean canExecute(Requirements appReqs) {
		if (appReqs.getCPUCores() > this.getCpuCores())
			return false;

		if (appReqs.getCPUSpeed() > this.getCpuSpeed())
			return false;

		if (appReqs.getCPULoad() > (100.0 - this.getCpuLoad()))
			return false;

		if (appReqs.getTotalMemory() > this.getMemTotal())
			return false;

		if (appReqs.getFreeMemory() > this.getMemFree())
			return false;

		if (!appReqs.getLocations().contains(this.location))
			return false;

		if (appReqs.getNoVirtualization() && this.virtualized)
			return false;

		return true;
	}

	private void markUpdated() {
		timestamp = new Date().getTime();
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isTooOld() {
		return (new Date().getTime() - timestamp) > Configuration.NODE_INFO_EXPIRATION_INTERVAL;
	}

	public void reset() {
		this.updateCpuCores = true;
		this.updateCpuSpeed = true;
		this.updateCpuModel = true;
		this.updateCpuLoad = true;
		this.updateMemTotal = true;
		this.updateMemUsed = true;
		this.updateMemFree = true;
		this.updateDiskTotal = true;
		this.updateDiskFree = true;
		this.updateLocation = true;
		this.updateVirtualized = true;
		this.updateDeploymentId = true;
		this.updateIsSingleNode = true;
		updateSystemInformation();
	}

	@Override
	public String toString() {
		String result = address + ":" + port;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		NodeInfo info = null;

		if (obj instanceof NodeInfo) {
			info = (NodeInfo) obj;
		} else {
			return false;
		}

		if (compareTo(info) == 0) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int compareTo(NodeInfo info) {
		return id.compareTo(info.getId());
	}

	public String getFullId() {
		return fullId;
	}

	public Map<AppInfo, Map<String, String>> getAppRoles() {
		return appRoles;
	}

	public void addRoleForApp(AppInfo appInfo, String roleName, String roleStatus) {
		Map<String, String> roleMap = appRoles.get(appInfo);
		if (roleMap == null) {
			roleMap = new HashMap<>();
			appRoles.put(appInfo, roleMap);
		}
		roleMap.put(roleName, roleStatus);
		markUpdated();
	}

	public void removeRoleForApp(AppInfo appInfo, String roleName) {
		Map<String, String> roleMap = appRoles.get(appInfo);
		if (roleMap != null)
			roleMap.remove(roleName);

		if (roleMap == null || roleMap.isEmpty())
			appRoles.remove(appInfo);

		markUpdated();
	}

	public boolean hasRoleForApp(AppInfo appInfo, String roleName) {
		Map<String, String> roleMap = appRoles.get(appInfo);
		if (roleMap == null)
			return false;
		else
			return roleMap.containsKey(roleName);
	}
}
