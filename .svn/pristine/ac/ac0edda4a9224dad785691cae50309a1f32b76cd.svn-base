$(document).ready(function() {
		$('a[data-toggle="tab"]').on("shown.bs.tab", function (e) {
		var id = $(e.target).attr("href").substr(1);
		window.location.hash = id;
	});
	
	var hash = window.location.hash;
	$('#navTabs a[href="' + hash + '"]').tab('show');
	
	
	/**
	 * Network
	 */

	$('#startNodeButton').on('click', function (e) {
		var params = "port=" + $('#startNodePort').val();
		postRequest("startLocalNode", params)
		reload(100);
	});

	$('#createConnectionButton').on('click', function (e) {
		var params = "port=" + $('#connectionPortText').val() + "&ip=" + $('#connectionIPText').val();
		postRequest("connect", params)
		reload(100);
	});
	
	
	/**
	 * NodeInfo
	 */
	
	$('.nodeInfoButton').on('click', function (e) {
		window.location.href = 'node/' + $(this).attr('name');
	});
	
	
	/**
	 * Node Modification
	 */
	
	$('#resetNodeInfoButton').on('click', function (e) {	
		postRequest("resetNode", null)
		reload(100);
	});
	
	$('#setCPULoadButton').on('click', function (e) {
		var params = "cpuLoad=" + $('#cpuLoadText').val();
		postRequest("cpuLoad", params)
		reload(100);
	});
	
	$('#setFullCPULoadButton').on('click', function (e) {
		var params = "cpuLoad=100";
		postRequest("cpuLoad", params)
		reload(100);
	});
	
	$('#setLocationButton').on('click', function (e) {
		var params = "location=" + $('#locationSelect').val();
		postRequest("location", params)
		reload(100);
	});
	
	$('#setVirtualizedButton').on('click', function (e) {
		var params = "virtualized=" + $('#virtualizedCheckbox').prop('checked');
		postRequest("virtualized", params)
		reload(100);
	});
	
	$('#setDeploymentIdButton').on('click', function (e) {
		var params = "deploymentId=" + $('#deploymentIdText').val();
		postRequest("deploymentId", params)
		reload(100);
	});
	
	$('#shutdownNodeButton').on('click', function (e) {
		postRequest("shutdown", null)
		reload(1000);
	});
});

function postRequest(apiString, params) {
	var url = window.location.href.split("#")[0] + "api/"+apiString;
	var xmlHttp = null;
	xmlHttp = new XMLHttpRequest();
	xmlHttp.open("POST", url, false);
	xmlHttp.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
	xmlHttp.send(params);
}

function reload (millis) {
	setTimeout(function () {			
		window.location.reload();
	}, millis);
}