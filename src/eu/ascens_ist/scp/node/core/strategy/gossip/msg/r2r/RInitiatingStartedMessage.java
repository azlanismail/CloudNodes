package eu.ascens_ist.scp.node.core.strategy.gossip.msg.r2r;

import eu.ascens_ist.scp.node.core.strategy.gossip.RoleId;
import eu.ascens_ist.scp.node.core.strategy.gossip.msg.RR2RMessage;

public class RInitiatingStartedMessage extends RR2RMessage {
	private static final long serialVersionUID = 1L;

	public RInitiatingStartedMessage(RoleId fromRole, RoleId toRole) {
		super(fromRole, toRole);
	}
}
