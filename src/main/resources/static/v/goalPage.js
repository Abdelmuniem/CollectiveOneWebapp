$(document).ready(function() {
	GLOBAL.goalPage = new GoalPage();
	docReadyCommon(GLOBAL.goalPage.init,GLOBAL.goalPage,false);
});

function GoalPage(container_id) {
};


GoalPage.prototype.init = function() {
	this.updateGoal(GLOBAL.REQ_goalTag, GLOBAL.REQ_projectName);
}

GoalPage.prototype.updateGoal = function(goalTag, projectName) {
	GLOBAL.serverComm.goalGet(goalTag,projectName,this.goalReceivedCallback,this);
}

GoalPage.prototype.goalReceivedCallback = function(goalDto) {
	this.goal = goalDto;
	var goalBox = new GoalBox($("#goal_container"),this.goal);
	goalBox.draw();
	
	var goalWeightsBox = new GoalWeightsBox($("#goal_weights_container"),{ goalId: this.goal.id });
	goalWeightsBox.update();
	
	this.updateCbtions();
}

GoalPage.prototype.updateCbtions = function(goalDto) {
	this.cbtionList = new CbtionList($("#cbtions_container",this.container),{ 
			goalTag: this.goal.goalTag,
			projectNames: [this.goal.projectName],
			maxheight: "600px",
			showFilterBtn: true,
			showNewBtn: true,
			newBtnLink: "/v/cbtionNew?goalTag="+this.goal.goalTag+"&projectName="+this.goal.projectName
		});

	this.cbtionList.init();
}