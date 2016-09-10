/*
 * This file belongs to the Science Cloud Platform (SCP) Implementation of the
 * Autonomic Cloud Case Study of the ASCENS EU project.
 *
 * For more information, see http://ascens-ist.eu/cloud.
 *
 */
package eu.ascens_ist.scp.node.info;

public enum NodeLocation {
	MUNICH, LUCCA;

	public String getLocationString() {
		switch (this) {
		case MUNICH:
			return "Munich";
		case LUCCA:
			return "Lucca";
		default:
			return "";
		}
	}

	public static NodeLocation getLocationFromString(String locationString) {
		switch (locationString) {
		case "Munich":
		case "Muenchen":
		case "MUNICH":
			return MUNICH;
		case "Lucca":
		case "LUCCA":
			return LUCCA;
		default:
			return MUNICH;
		}
	}
}
