$(document).ready(function() {

	GLOBAL = new Object();
	GLOBAL.activityListPage = new ActivityListPage("#content_pane");
	CopDocReadyCommon(GLOBAL.activityListPage.init,GLOBAL.activityListPage);

});

function ActivityListPage(container_id) {

	// Parent constructor
	Page.call(this,container_id);

	// Array with Activity objects
	this.activityItems = {};
	this.filter = {};
};

//Inheritance
ActivityListPage.prototype = Page.prototype;

ActivityListPage.prototype.init = function() {
	// Common after sessionUpdate code
	CopSessionReadyCommon();

	var filters = {
			projectNames : GLOBAL.sessionData.activeProjectsController.getActiveProjectsNames(),
			stateNames: [],
			creatorUsernames: [],
			keyw : '',
			sortBy: "CREATIONDATEDESC",
			page : 1,
			nperpage : 30
	};

	customElements = { 
			stateNames: [],
			sortBy: [ { text:"New first", value:"CREATIONDATEDESC" },
			          { text:"Old first", value:"CREATIONDATEASC" } ]
	};

	this.filter = new FilterElement("#filter_container", 
			GLOBAL.serverComm.activityListGet, 
			this.activityItemsReceivedCallback, 
			this, 
			customElements, 
			filters);

	this.filter.updateData();
}

ActivityListPage.prototype.activityItemsReceivedCallback = function(data) {
	this.activityItems = data.activityDtos;
	this.drawActivityItems();

	this.filter.resSet = data.resSet;
	this.filter.updateResSet();
}

ActivityListPage.prototype.drawActivityItems = function() {

	$("#list_of_elements", this.container).empty();

	for ( var ix in this.activityItems) {
		$("#list_of_elements", this.container).append("<div class=itembox_div id=itembox"+ix+"_div></div>");
		var activityBox = new ActivityBox($("#itembox"+ix+"_div"),this.activityItems[ix]);
		activityBox.draw();
	}
}

