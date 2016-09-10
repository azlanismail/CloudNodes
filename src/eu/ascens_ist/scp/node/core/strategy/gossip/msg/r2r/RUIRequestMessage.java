/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r;

import java.util.Map;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.info.AppInfo;

public class RUIRequestMessage extends RR2RMessage {

	private static final long serialVersionUID= 1L;

	private Map<String, String[]> properties;

	private String target;

	private AppInfo appInfo;

	private Id routingId;

	public RUIRequestMessage(RoleId fromRole, RoleId toRole, Id routingId, AppInfo appInfo, String target, Map<String, String[]> properties) {
		super(fromRole, toRole);
		this.routingId= routingId;
		this.appInfo= appInfo;
		this.target= target;
		this.properties= properties;
	}

	public Id getRoutingId() {
		return routingId;
	}

	public AppInfo getAppInfo() {
		return appInfo;
	}

	public String getTarget() {
		return target;
	}

	public Map<String, String[]> getProperties() {
		return properties;
	}

}
