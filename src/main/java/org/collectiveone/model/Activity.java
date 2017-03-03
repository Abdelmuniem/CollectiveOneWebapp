package org.collectiveone.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.collectiveone.web.dto.ActivityDto;

@Entity
@Table(name = "ACTIVITY")
public class Activity {
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	private Timestamp creationDate;
	private String event;
	@ManyToOne
	private Project project;
	
	private ActivityType type; 
	@ManyToOne
	private Cbtion cbtion;
	@ManyToOne
	private Bid bid;
	@ManyToOne
	private Goal goal;
	@ManyToOne
	private Decision decision;
	@ManyToOne
	private Argument argument;
	@ManyToOne
	private Comment comment;
	
	public Activity() {
	}
	
	public Activity(String event, Timestamp date, Project project) {
		this.event = event;
		this.creationDate = date;
		this.project = project;
	}
	
	public ActivityDto toDto() {
		ActivityDto dto = new ActivityDto();
		
		dto.setId(id);
		dto.setProjectName(project.getName());
		dto.setCreationDate(creationDate.getTime());
		dto.setEvent(event);
		dto.setEventPretty(this.getPrettyMessage(""));
		dto.setType(type.toString());
		if(cbtion != null) dto.setCbtionDto(cbtion.toDto());
		if(bid != null) dto.setBidDto(bid.toDto());
		if(goal != null) dto.setGoalDto(goal.toDto());
		if(decision != null) dto.setDecisionDto(decision.toDto());
		if(argument != null) dto.setArgumentDto(argument.toDto());
		if(comment != null)  dto.setCommentDto(comment.toDto());
		
		return dto;
	}
	
	public String getUserPageLink(String baseUrl, String username) {
		return "<a href="+baseUrl+"/v/u/"+username+">"+username+"</a>";
	}
	
	public String getGoalPageLink(String baseUrl, String goalTag, String projectName) {
		return "<a href="+baseUrl+"/v/goal?projectName="+projectName+"&goalTag="+goalTag+"><b>+</b>"+goalTag+"</a>";
	}
	
	public String getCbtionPageLink(String baseUrl, Long id, String title) {
		return "<a href="+baseUrl+"/v/cbtion/"+id+">"+title+"</a>";
	}
	
	public String limitStrSize(String strIn, int size) {
		if(strIn.length() > size) {
			return strIn.substring(0,size)+" ...";
		} else {
			return strIn;
		}
	}
	
	public String getPrettyMessage(String baseUrl) {
		String eventPretty = "";
		
		switch(this.type) {
			case CBTION:
				eventPretty = "contribution "+getCbtionPageLink(baseUrl,this.cbtion.getId(),this.cbtion.getTitle())+" was "+this.event;
				break;
	
			case BID:
				switch(this.event) {
					case "created":
						eventPretty = getUserPageLink(baseUrl,this.bid.getCreator().getUsername())+
							" made a new bid on "+getCbtionPageLink(baseUrl,this.bid.getCbtion().getId(),this.bid.getCbtion().getTitle());
						break;
	
					case "assigned":
					case "accepted":
					case "not accepted":
						
						eventPretty = "bid from "+getUserPageLink(baseUrl,this.bid.getCreator().getUsername())+
							" to "+getCbtionPageLink(baseUrl,this.bid.getCbtion().getId(),this.bid.getCbtion().getTitle())+" was "+this.event;
						break;
	
					case "marked done":
						eventPretty = getUserPageLink(baseUrl,this.bid.getCreator().getUsername())+
							" marked bid on "+getCbtionPageLink(baseUrl,this.bid.getCbtion().getId(),this.bid.getCbtion().getTitle())+" as done";
						break;
				}
				
				break;
	
			case GOAL:
				String goalLinkStr = getGoalPageLink(baseUrl,this.goal.getGoalTag(),this.goal.getProject().getName());
				switch(this.event) {
					case "proposed":
						eventPretty = "goal "+goalLinkStr+" was "+this.event+" by "+getUserPageLink(baseUrl,this.goal.getCreator().getUsername());
						break;
					default:
						eventPretty = "goal "+goalLinkStr+" - "+this.event;
						break;
				}
				break;
	
			case DECISION:
				switch(this.event) {
					case "created":	
						eventPretty = "decision <a href="+baseUrl+"/v/decision/"+this.decision.getId()+">"+limitStrSize(this.decision.getDescription(),70)+"</a> was "+this.event+" by "+getUserPageLink(baseUrl,this.decision.getCreator().getUsername());
						break;
	
					case "opened":
					case "accepted":
					case "rejected":
						eventPretty = "decision <a href="+baseUrl+"/v/decision/"+this.decision.getId()+">"+limitStrSize(this.decision.getDescription(),70)+"</a> was "+this.event;
						break;
				}
				break;
	
			case ARGUMENT:
				switch(this.event) {
					case "created":	
							eventPretty = getUserPageLink(baseUrl,this.argument.getCreator().getUsername())+" added an argument to decision '<a href="+baseUrl+"/v/decision/"+
							this.argument.getDecision().getId()+">"+limitStrSize(this.argument.getDecision().getDescription(),70)+"</a>'";
							break;
				}
				break;
				
			case COMMENT:
				switch(this.event) {
				case "new":	
						eventPretty = getUserPageLink(baseUrl,this.comment.getCreator().getUsername())+" added a new comment to contribution "+
						getCbtionPageLink(baseUrl,this.comment.getCbtion().getId(),this.comment.getCbtion().getTitle());
						break;
				}
				break;

		}
		
		return eventPretty;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public ActivityType getType() {
		return type;
	}
	public void setType(ActivityType type) {
		this.type = type;
	}
	public Cbtion getCbtion() {
		return cbtion;
	}
	public void setCbtion(Cbtion cbtion) {
		this.cbtion = cbtion;
	}
	public Bid getBid() {
		return bid;
	}
	public void setBid(Bid bid) {
		this.bid = bid;
	}
	public Goal getGoal() {
		return goal;
	}
	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	public Decision getDecision() {
		return decision;
	}
	public void setDecision(Decision decision) {
		this.decision = decision;
	}
	public Argument getArgument() {
		return argument;
	}
	public void setArgument(Argument argument) {
		this.argument = argument;
	}
	public Comment getComment() {
		return comment;
	}
	public void setComment(Comment comment) {
		this.comment = comment;
	}
	
	
}
