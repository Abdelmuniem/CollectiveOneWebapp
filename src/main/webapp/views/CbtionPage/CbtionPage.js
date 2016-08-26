$(document).ready(function() {
	
	GLOBAL = new Object();
	GLOBAL.cbtionPage = new CbtionPage("#content_pane");
	CopDocReadyCommon(GLOBAL.cbtionPage.init,GLOBAL.cbtionPage);
});

function CbtionPage(container_id) {
	// Parent constructor
	Page.call(this,container_id);
	
	this.cbtion;
};

//Inheritance
CbtionPage.prototype = Page.prototype;

CbtionPage.prototype.init = function() {
	$("#new_bid_btn",this.container).click(function (){
		if(GLOBAL.sessionData.userLogged) {
			$("#new_bid_div",this.container).toggle();
			$("#newbid_username_div",this.container).html(("<p>"+GLOBAL.sessionData.userLogged.username+"</p>"));
		} else {
			$("#new_bid_div",this.container).hide();
			showOutput("plase login to bid to this contribution");
		}
	});
	
	$("#newbid_submit_div",this.container).click(this.bidNew.bind(this));
	
	GLOBAL.cbtionPage.updateCbtion(JSP_cbtionId);
}

CbtionPage.prototype.cbtionClose = function (){
	if(GLOBAL.sessionData.userLogged) {
		GLOBAL.serverComm.cbtionClose(this.cbtion.id,this.cbtionClosedCallback,this);
	} else {
		showOutput("plase login to propose to close this contribution");
	}
}

CbtionPage.prototype.cbtionClosedCallback = function (){
	
}

CbtionPage.prototype.bidNew = function (){
	var bidData = { 
			cbtionId:this.cbtion.id,
			ppoints:$("#newbid_ppoints_in",this.container).attr('value'),
			description:$("#newbid_description_in",this.container).attr('value'),
			creatorDto: GLOBAL.sessionData.userLogged
		}; 
	
	GLOBAL.serverComm.bidNew(bidData,this.newBidSavedCallback,this);
}

CbtionPage.prototype.newBidSavedCallback = function() {
	$("#new_bid_div").hide();
	this.updateBids();
}

CbtionPage.prototype.updateCbtion = function(cbtionId) {
	GLOBAL.serverComm.cbtionGet(cbtionId,this.CbtionReceivedCallback,this);
}

CbtionPage.prototype.CbtionReceivedCallback = function(cbtionDto) {
	this.cbtion = cbtionDto;
	this.drawCbtion();
}

CbtionPage.prototype.drawCbtion = function() {
	$("#project_div",this.container).append($("<p id=cbtion_project>"+this.cbtion.projectName+"</p>"));
	$("#project_div",this.container).click(function (){
		window.location = 'ProjectPage.action?projectName='+GLOBAL.cbtionPage.cbtion.projectName;
	});
	$("#title_div",this.container).append($("<p id=cbtion_title>"+this.cbtion.title+"</p>"));
	$("#creator_div",this.container).append($("<p id=creator_username>"+this.cbtion.creatorUsername+"</p>"));
	if(this.cbtion.state == "ACCEPTED") {
		$("#state_div",this.container).append($("<p id=cbtion_state>"+this.cbtion.state+
				" to "+this.cbtion.contributorUsername+" for "+floatToChar(this.cbtion.assignedPpoints,2)+" pp's</p>"));
	} else {
		$("#state_div",this.container).append($("<p id=cbtion_state>"+this.cbtion.state+"</p>"));
	}	
	
	$("#description_div",this.container).append($("<p id=cbtion_description>"+this.cbtion.description+"</p>"));
	$("#product_div",this.container).append($("<p id=cbtion_product>"+this.cbtion.product	+"</p>"));
	
	this.updateBids();
}

CbtionPage.prototype.updateBids = function(bidDtos) {
	GLOBAL.serverComm.bidsOfCbtionGet(this.cbtion.id,this.updateBidsCallback,this);
}

CbtionPage.prototype.updateBidsCallback = function(bidDtos) {
	this.cbtion.bids = bidDtos;
	this.drawBids();
}

CbtionPage.prototype.drawBids = function() {
	
	$("#bids_div",this.container).empty();
	var nb = this.cbtion.bids.length;
	
	for(var ix=0; ix<nb ; ix++ ) {
		
		$("#bids_div",this.container).append($("<div class=bid_div id=bid_"+ix+"_div></div>"));

		$("#bid_"+ix+"_div",this.container).append($("<div class=bid_creator_div id=bid"+ix+"_creator_div></div>"));
		$("#bid_"+ix+"_div",this.container).append($("<div class=bid_contents_div id=bid"+ix+"_contents_div></div>"));
		$("#bid_"+ix+"_div",this.container).append($("<div class=bid_decision_div id=bid"+ix+"_decision_div></div>"));
		
		var bid = this.cbtion.bids[ix];
		
		var userBox = new UserBox("#bid"+ix+"_creator_div",bid.creatorDto,this.cbtion.projectName);
		userBox.updateUser();
		
		var bidBox = new BidBox("#bid"+ix+"_contents_div",bid);
		bidBox.draw();
		
		var applicable_decision = null;
		
		switch(bid.state) {
			case "OFFERED":
				applicable_decision = bid.assignDec;
				break;
				
			case "ASSIGNED":
				applicable_decision = bid.acceptDec;
				break;

			case "DONE":
				applicable_decision = bid.acceptDec;
				break;
				
			case "NOT_ASSIGNED":
				applicable_decision = bid.assignDec;
				break;
				
			case "ACCEPTED":
				applicable_decision = bid.acceptDec;
				break;
				
			case "NOT_ACCEPTED":
				applicable_decision = bid.acceptDec;
				break;
				
			case "OVERSEED":
				applicable_decision = bid.assignDec;
				break;	
				
			default:
				console.log("Unexected bid state " + bid.state);
				
		}
		var decBox = new DecisionBoxSmall("#bid"+ix+"_decision_div",applicable_decision, GLOBAL.sessionData.userLogged);
		decBox.draw();
	}
}