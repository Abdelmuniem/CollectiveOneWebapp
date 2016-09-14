package coproject.cpweb.utils.db.entities.dtos;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class GoalDto {
	
	private int id;
	private String projectName;
	private String creatorUsername;
	private Timestamp creationDate;
	private String goalTag;
	private List<String> parentGoalsTags = new ArrayList<String>();
	private List<String> subGoalsTags = new ArrayList<String>();
	private String description;
	private String state;
	private DecisionDto createDec;
	private DecisionDto deleteDec;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
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
	
	
}

