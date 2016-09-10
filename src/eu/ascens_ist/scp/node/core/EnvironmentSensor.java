/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core;

import java.io.File;
import org.hyperic.sigar.CpuInfo;
import org.hyperic.sigar.CpuPerc;
import org.hyperic.sigar.Mem;
import org.hyperic.sigar.NetInterfaceConfig;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

/**
 * 
 * "Sensor" for the environment of the node: CPU, Memory, ...
 * 
 * @author A. Zeblin, A. Dittrich
 * 
 */
public class EnvironmentSensor {

	protected static Sigar sigar = new Sigar();

	public static int getCPUCores() {		
		try {
			CpuInfo cpuInfo= sigar.getCpuInfoList()[0];
			return cpuInfo.getTotalCores();
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}

	}

	public static int getCPUSpeed() {
		try {
			CpuInfo cpuInfo= sigar.getCpuInfoList()[0];
			return cpuInfo.getMhz();
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static String getCPUModel() {
		try {
			CpuInfo cpuInfo= sigar.getCpuInfoList()[0];
			return cpuInfo.getVendor() + " " + cpuInfo.getModel();
		} catch (SigarException e) {
			e.printStackTrace();
			return "Unknown Model";
		}
	}

	public static double getCPULoad() {
		try {
			CpuPerc cpuPerc= sigar.getCpuPerc();
			double load= cpuPerc.getCombined() * 100;
			return roundDouble(load);
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static long getMEMTotal() {
		try {
			Mem mem= sigar.getMem();
			return (mem.getTotal() / 1024 / 1024);
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static long getMEMUsed() {
		try {
			Mem mem= sigar.getMem();
			return (mem.getUsed() / 1024 / 1024);
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static long getMEMFree() {
		try {
			Mem mem= sigar.getMem();
			return (mem.getFree() / 1024 / 1024);
		} catch (SigarException e) {
			e.printStackTrace();
			return 0;
		}
	}

	public static long getTotalDiskSpace() {
		File file = new File("/");
		long total= (long) (file.getTotalSpace() / (1024.0 * 1024.0));
		return (total);
	}

	public static long getFreeDiskSpace() {
		File file = new File("/");
		long total= (long) (file.getFreeSpace() / (1024.0 * 1024.0));
		return (total);
	}

	public static boolean getVirtualized() {
		try {
			//TODO More reliable way to detect virtualization
			NetInterfaceConfig config = sigar.getNetInterfaceConfig(null);
			if (config.getHwaddr().contains("00:50:56:"))
				return true;
			else
				return false;
		} catch (SigarException e) {
			e.printStackTrace();
			return false;
		}
	}

	private static double roundDouble(double value) {
		return (Math.round(value * 100.0) / 100.0);
	}

}
