/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.IStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.roles.HelenaRole;

/**
 * Represents an ensemble on one node, which has an ID (based on app name) and some roles.
 * 
 * @author P. Mayer
 * 
 */
public class Ensemble {

	private Id ensembleId;

	private Map<RoleId, HelenaRole> roles;

	private IStrategy strategy;

	public Ensemble(Id ensembleId, IStrategy strategy) {
		this.ensembleId= ensembleId;
		this.strategy= strategy;
		this.roles= new ConcurrentHashMap<>();
	}

	public Id getEnsembleId() {
		return ensembleId;
	}

	/**
	 * Creates a new role. Requires the three-argument constructor {@link HelenaRole#HelenaRole(Id, Id, GossipHelenaBasedStrategy)}.
	 * 
	 * @param roleType
	 * @return
	 * @throws RRoleCreationException
	 */
	public RoleId startNewRole(Class<?> roleType) throws RRoleCreationException {

		try {
			Constructor<?> constructor= roleType.getConstructor(Id.class, Id.class, GossipHelenaBasedStrategy.class);
			HelenaRole newRole= (HelenaRole) constructor.newInstance(ensembleId, strategy.getSCPNode().getId(), strategy);
			roles.put(newRole.getRoleId(), newRole);

			new Thread(newRole).start();
			return newRole.getRoleId();

		} catch (Exception e) {
			throw new RRoleCreationException("Could not create role " + roleType, e);
		}
	}

	public List<HelenaRole> getRoles(Class<? extends HelenaRole> roleType) {

		List<HelenaRole> returner= new ArrayList<>();

		Collection<HelenaRole> roles= this.roles.values();
		for (HelenaRole helenaRole : roles) {

			if (roleType.isAssignableFrom(helenaRole.getClass()))
				returner.add(helenaRole);
		}

		return returner;
	}

	public HelenaRole getSpecificRole(RoleId roleId) {
		return roles.get(roleId);
	}

	/**
	 * Stops all roles and removes them from the ensemble. This depends on the correct implementation of {@link HelenaRole#stop()}.
	 */
	public void stopAll() {

		Collection<HelenaRole> roles= this.roles.values();
		for (HelenaRole helenaRole : roles) {
			helenaRole.stop();
		}

		roles.clear();

	}

	public void removeRole(HelenaRole helenaRole) {
		roles.remove(helenaRole.getRoleId());
	}


}
