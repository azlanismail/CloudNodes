/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core.strategy.gossip.roles;

import java.util.Collection;

import rice.p2p.commonapi.Id;
import eu.ascens_ist.scp.node.core.strategy.gossip.GossipHelenaBasedStrategy;
import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.REnsembleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleCreationException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RRoleNotFoundException;
import eu.ascens_ist.scp.node.core.strategy.gossip.exc.RTimeoutException;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RCreateRoleInstanceMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RCreateRoleInstanceResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RGetRoleInstanceMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.framework.RGetRoleInstanceResultMessage;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r.RRouteRoleMessageResult;

public abstract class HelenaRole implements Runnable {

	public static final int INFINITY = Integer.MAX_VALUE;

	private GossipHelenaBasedStrategy strategy;

	private RoleId roleId;

	public HelenaRole(Id ensembleId, Id nodeId, GossipHelenaBasedStrategy strategy) {
		this.roleId = new RoleId(ensembleId, nodeId, strategy.getSCPNode().getEnvironment().createArbitraryId());
		this.strategy = strategy;
	}

	public Id getNodeId() {
		return this.roleId.getNodeId();
	}

	public RoleId getRoleId() {
		return this.roleId;
	}

	protected Id getEnsembleId() {
		return this.roleId.getEnsembleId();
	}

	protected Id createIdfromString(String string) {
		return getStrategy().createIdFromString(string);
	}

	public GossipHelenaBasedStrategy getStrategy() {
		return this.strategy;
	}

	protected <E extends RR2RMessage> E waitForIncomingMessage(int timeoutinms, Class<E> clazz) throws RTimeoutException {
		return getStrategy().waitForIncomingMessage(timeoutinms, clazz);
	}

	protected RR2RMessage waitForIncomingMessages(int infinity, Collection<Class<? extends RR2RMessage>> msgs) throws RTimeoutException {
		return getStrategy().waitForIncomingMessages(infinity, msgs);
	}

	protected RoleId createLocalRoleInstance(Class<? extends HelenaRole> roleType) throws RRoleCreationException {
		return getStrategy().createLocalRoleInstance(getRoleId().getEnsembleId(), roleType);
	}

	protected RoleId createRoleInstance(Id targetNode, Class<? extends HelenaRole> roleType) throws RRoleCreationException, RTimeoutException {

		RCreateRoleInstanceMessage m = new RCreateRoleInstanceMessage(getRoleId(), targetNode, roleType);
		RCreateRoleInstanceResultMessage result = (RCreateRoleInstanceResultMessage) getStrategy().routeFrameworkMessage(m);

		RRoleCreationException e = result.getRemoteRoleCreationException();
		if (e != null)
			throw e;

		return result.getRoleInstance();
	}

	protected RoleId getRoleInstance(Id targetNode, Class<? extends HelenaRole> roleType) throws RRoleNotFoundException, RTimeoutException, REnsembleNotFoundException {

		RGetRoleInstanceMessage m = new RGetRoleInstanceMessage(getRoleId(), targetNode, roleType);
		RGetRoleInstanceResultMessage result = (RGetRoleInstanceResultMessage) getStrategy().routeFrameworkMessage(m);

		RException e = result.getOriginException();
		if (e instanceof RRoleNotFoundException)
			throw (RRoleNotFoundException) e;
		if (e instanceof REnsembleNotFoundException)
			throw (REnsembleNotFoundException) e;

		return result.getRoleInstance();
	}

	protected void sendMessage(RR2RMessage message) throws RRoleNotFoundException, RTimeoutException, REnsembleNotFoundException {

		// throws RTimeoutException
		RRouteRoleMessageResult result = getStrategy().routeRoleMessage(message);

		RException e = result.getRemoteException();
		if (e instanceof REnsembleNotFoundException)
			throw ((REnsembleNotFoundException) e);
		if (e instanceof RRoleNotFoundException)
			throw ((RRoleNotFoundException) e);
		else
			;// all is fine.
	}

	protected void sleep(int timeinms) {
		try {
			Thread.sleep(timeinms);
		} catch (InterruptedException e) {
			// ignore this
		}
	}

	public void stop() {
		// do nothing by default
	}

}
