/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.core;

import java.util.Map;

/**
 * 
 * This interface is used for communicating with an app. Each app must register a service which implements this interface to allow interaction.
 * 
 * 
 * @author A. Zeblin
 * 
 */
public interface IAppService {

	public String handleUI(ISCPNode node, String target, Map<String, String[]> properties);

	public void start(ISCPNode node);

	public void stop();

}
