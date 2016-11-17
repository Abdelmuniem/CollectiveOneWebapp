package org.collectiveone.web.dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GoalDto {
	
	private Long id;
	private String projectName;
	private String creatorUsername;
	private Timestamp creationDate;
	private String goalTag;
	private String parentGoalTag;
	private List<String> parentGoalsTags = new ArrayList<String>();
	private List<String> subGoalsTags = new ArrayList<String>();
	private String description;
	private String state;
	private DecisionDto createDec;
	private DecisionDto deleteDec;
	private String parentState;
	private String proposedParent;
	private DecisionDto proposeParent;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public String getCreatorUsername() {
		return creatorUsername;
	}
	public void setCreatorUsername(String creatorUsername) {
		this.creatorUsername = creatorUsername;
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public String getParentGoalTag() {
		return parentGoalTag;
	}
	public void setParentGoalTag(String parentGoalTag) {
		this.parentGoalTag = parentGoalTag;
	}
	public String getGoalTag() {
		return goalTag;
	}
	public void setGoalTag(String goalTag) {
		this.goalTag = goalTag;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public DecisionDto getCreateDec() {
		return createDec;
	}
	public void setCreateDec(DecisionDto createDec) {
		this.createDec = createDec;
	}
	public DecisionDto getDeleteDec() {
		return deleteDec;
	}
	public void setDeleteDec(DecisionDto deleteDec) {
		this.deleteDec = deleteDec;
	}
	public List<String> getParentGoalsTags() {
		return parentGoalsTags;
	}
	public void setParentGoalsTags(List<String> parentGoalsTags) {
		this.parentGoalsTags = parentGoalsTags;
	}
	public List<String> getSubGoalsTags() {
		return subGoalsTags;
	}
	public void setSubGoalsTags(List<String> subGoalsTags) {
		this.subGoalsTags = subGoalsTags;
	}
	public String getParentState() {
		return parentState;
	}
	public void setParentState(String parentState) {
		this.parentState = parentState;
	}
	public String getProposedParent() {
		return proposedParent;
	}
	public void setProposedParent(String proposedParent) {
		this.proposedParent = proposedParent;
	}
	public DecisionDto getProposeParent() {
		return proposeParent;
	}
	public void setProposeParent(DecisionDto proposeParent) {
		this.proposeParent = proposeParent;
	}
}

