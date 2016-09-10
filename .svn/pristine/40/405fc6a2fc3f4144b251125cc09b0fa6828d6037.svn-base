/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip;

import java.io.Serializable;

import rice.p2p.commonapi.Id;

/**
 * A role id is a triple of ids (nodeID, ensembleID, roleID). In theory the roleID is sufficient but we cannot extract the ensembleID and nodeID from it, so we
 * store all three.
 * 
 * @author P. Mayer
 * 
 */
public class RoleId implements Serializable {

	private static final long serialVersionUID= 1L;

	private Id ensembleId;

	private Id nodeId;

	private Id roleId;

	public RoleId(Id ensembleId, Id nodeId, Id createArbitraryId) {
		this.ensembleId= ensembleId;
		this.nodeId= nodeId;
		this.roleId= createArbitraryId;
	}

	public Id getEnsembleId() {
		return ensembleId;
	}

	public Id getNodeId() {
		return nodeId;
	}

	public Id getRoleId() {
		return roleId;
	}

	@Override
	public int hashCode() {
		final int prime= 31;
		int result= 1;
		result= prime * result + ( (ensembleId == null) ? 0 : ensembleId.hashCode());
		result= prime * result + ( (nodeId == null) ? 0 : nodeId.hashCode());
		result= prime * result + ( (roleId == null) ? 0 : roleId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RoleId other= (RoleId) obj;
		if (ensembleId == null) {
			if (other.ensembleId != null)
				return false;
		} else if (!ensembleId.equals(other.ensembleId))
			return false;
		if (nodeId == null) {
			if (other.nodeId != null)
				return false;
		} else if (!nodeId.equals(other.nodeId))
			return false;
		if (roleId == null) {
			if (other.roleId != null)
				return false;
		} else if (!roleId.equals(other.roleId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RoleId " + roleId.toStringFull() + " NodeId " + nodeId.toStringFull() + " EnsembleId " + ensembleId.toStringFull();
	}


}
