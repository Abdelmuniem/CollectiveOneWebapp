function DecisionBox(container_id,decisionData, voter) {
	DecisionBase.call(this,container_id,decisionData, voter);
	this.argumentsExpanded = false;
};

DecisionBox.prototype = DecisionBase.prototype;

DecisionBox.prototype.draw = function() {
	this.container.load("../elements/DecisionBox/DecisionBox.html",this.decisionBoxLoaded.bind(this));
}

DecisionBox.prototype.decisionBoxLoaded = function() {
	
	$("#accept_div",this.container).click(this.voteAccept.bind(this));
	$("#reject_div",this.container).click(this.voteReject.bind(this));
	
	this.updateVoteStatus();
	
	$("#description",this.container).append("<a href=DecisionPage.action?decisionId="+ this.decision.id+">"+this.decision.description+"</a>");
	
	switch(this.decision.type) {
		case "GENERAL":
			$("#from_to_state",this.container).hide();
			break;
		case "CBTION":
			$("#from_to_state",this.container).append("<p>changes the state of contribution <a href=CbtionPage.action?cbtionId="+
			this.decision.cbtionId+">"+this.decision.cbtionTitle+"</a> from "+this.decision.fromState+" to "+this.decision.toState+"</p>");	
			break;
		case "BID":
			$("#from_to_state",this.container).append("<p>modifies bid to contribution <a href=CbtionPage.action?cbtionId="+
			this.decision.cbtionId+">"+this.decision.cbtionTitle+"</a> made by "+getUserPageLink(this.decision.bidCreatorUsername)+"</p>");	
			break;
		case "GOAL":
			$("#from_to_state",this.container).append("<p>modifies goal <a href=GoalListPage.action>"+this.decision.goalTag+"</a>");
			break;
	}
	
	var stateStr = '';
	
	switch(this.decision.state) {
		case "IDLE":
		case "OPEN":
			$("#right_div #state",this.container).css("color","DarkGreen");
			stateStr = this.decision.state;
			break;

		case "CLOSED_ACCEPTED":
		case "CLOSED_DENIED":
		case "CLOSED_EXTERNALLY":
			$("#right_div #state",this.container).css("color","DarkRed");
			$("#center_div",this.container).hide();
			stateStr = "CLOSED";
			break;
	}


	$("#project_div",this.container).append($("<p>In <a href=../views/ProjectPage.action?projectName="+this.decision.projectName+">"+this.decision.projectName+"</a></p>"));
	$("#state_div",this.container).append($("<p>Currenlty <span id=state>"+stateStr+"</span></p>"));

	if(this.decision.state != "IDLE") {

		$("#metrics",this.container).show();
		$("#verdictTime_div",this.container).show();
		$("#verdict_div",this.container).show();
		
		$("#clarity",this.container).append($("<p>clarity: "+floatToChar(this.decision.clarity*100,1)+"</p>"));
		$("#stability",this.container).append($("<p>stability: "+floatToChar(this.decision.stability*100,1)+"</p>"));
		$("#votedRatio",this.container).append($("<p>voted ratio: "+floatToChar(this.decision.ppsCum/this.decision.ppsTot*100,2)+"%</p>"));
		$("#acceptToRejectRatio",this.container).append($("<p>yes ratio: "+floatToChar(this.decision.log_l1l0,2)+"</p>"));
		
		var verdictStr = [];
		if(this.decision.verdict == 1) {
			verdictStr = "YES"; 
		} else {
			verdictStr = "NO";
		}
		
		var verdictPre = ''; 
		var verdictTime = '';
		var verdictTimePost = '';

		switch(this.decision.state) {
			case "IDLE":
			case "OPEN":
				verdictPre = "Temporary verdict is ";
				verdictTime = "to be closed in less than "
				+floatToChar((1-this.decision.elapsedFactor)*this.decision.verdictHours,1)+" hr";
				break;

			case "CLOSED_ACCEPTED":
			case "CLOSED_DENIED":
			case "CLOSED_EXTERNALLY":
				verdictPre = "Final verdict: ";
				verdictTime = "taken "+getTimeStrSince(this.decision.actualVerdictDate)+" ago";
				break;
		}
		
		$("#verdict_div",this.container).append($("<p>"+verdictPre+"<span id=verdict>"+verdictStr+"</span></p>"));
		$("#verdictTime_div",this.container).append("<p>"+verdictTime+"</p>");

		if(this.decision.verdict == 1) {
			$("#right_div #verdict",this.container).css("color","DarkGreen");
		} else {
			$("#right_div #verdict",this.container).css("color","DarkRed");
		}
	}
	
	// Arguments portion expansion

	$("#arguments_expand_div",this.container).append("<p>see arguments ("+this.decision.narguments+")</p>");
	
	$("#arguments_expand_div",this.container).click(this.argumentsExpandClick.bind(this));
	$("#arguments_no_new_btn",this.container).click(this.argumentNoExpand.bind(this));
	$("#arguments_yes_new_btn",this.container).click(this.argumentYesExpand.bind(this));

	$("#arg_no_new_save",this.container).click(this.argumentNoSave.bind(this));
	$("#arg_yes_new_save",this.container).click(this.argumentYesSave.bind(this));
}

DecisionBox.prototype.argumentsExpandClick = function() {

	if(this.argumentsExpanded) {
		$("#discussion_div",this.container).hide();
		this.argumentsExpanded = false;
	} else {
		this.updateArguments();
		this.argumentsExpanded = true;
	}
	
}

DecisionBox.prototype.updateArguments = function() {
	GLOBAL.serverComm.argumentsGetOfDecision(this.decision.id,this.argumentsReceivedCallback,this);
}

DecisionBox.prototype.argumentsReceivedCallback = function(data) {

	this.decision.argumentsNo = data.argumentNoDtos;
	this.decision.argumentsYes = data.argumentYesDtos;

	this.drawArguments();
}

DecisionBox.prototype.drawArguments = function() {

	$("#discussion_div",this.container).show();

	$("#args_no_div", this.container).empty();
	for ( var ix in this.decision.argumentsNo ) {
		$("#args_no_div", this.container).append("<div id=argument_no"+ix+" class=argument_container></div>");
		var argumentBox = new ArgumentBox($("#argument_no"+ix,this.container),this.decision.argumentsNo[ix]);
		argumentBox.draw();
	}
	
	$("#args_yes_div", this.container).empty();
	for ( var ix in this.decision.argumentsYes ) {
		$("#args_yes_div", this.container).append("<div id=argument_yes"+ix+" class=argument_container></div>");
		var argumentBox = new ArgumentBox($("#argument_yes"+ix,this.container),this.decision.argumentsYes[ix]);
		argumentBox.draw();
	}	
}

DecisionBox.prototype.argumentNoExpand = function() {
	if(GLOBAL.sessionData.userLogged != null) {
		$("#arg_no_new_form",this.container).toggle();	
	} else {
		showOutput("please login to add arguments","DarkRed")
	}
	
}

DecisionBox.prototype.argumentYesExpand = function() {
	if(GLOBAL.sessionData.userLogged != null) {
		$("#arg_yes_new_form",this.container).toggle();
	} else {
		showOutput("please login to add arguments","DarkRed")
	}
}

DecisionBox.prototype.argumentNoSave = function() {
	argDto = {
		creatorUsername : GLOBAL.sessionData.userLogged.username,
		description : $("#arg_no_new_description", this.container).val(),
		decisionId : this.decision.id,
		tendency: "FORNO"
	}

	$("#arg_no_new_form",this.container).hide();	
	GLOBAL.serverComm.argumentNew(argDto,this.argumentNewCallback,this);	
}

DecisionBox.prototype.argumentYesSave = function() {
	argDto = {
		creatorUsername : GLOBAL.sessionData.userLogged.username,
		description : $("#arg_yes_new_description", this.container).val(),
		decisionId : this.decision.id,
		tendency: "FORYES"
	}
	$("#arg_yes_new_description", this.container).val("");
	$("#arg_yes_new_form",this.container).hide();
	GLOBAL.serverComm.argumentNew(argDto,this.argumentNewCallback,this);		
}

DecisionBox.prototype.argumentNewCallback = function() {
	this.updateArguments();
}
