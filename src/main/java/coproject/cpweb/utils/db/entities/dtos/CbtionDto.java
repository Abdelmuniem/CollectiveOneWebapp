package coproject.cpweb.utils.db.entities.dtos;

import java.sql.Timestamp;

import coproject.cpweb.utils.db.entities.Cbtion;
import coproject.cpweb.utils.db.entities.Project;
import coproject.cpweb.utils.db.entities.User;

public class CbtionDto {
	
	private int id;
	private String projectName;
	private String creatorUsername;
	private Timestamp creationDate;
	private String title;
	private String description;
	private String product;
	private Integer relevance;
	private String state;
	private int nBids;
	private String contributorUsername;
	private double assignedPpoints;	
	
	public Cbtion toCbtion(User creator, Project project) {
		Cbtion cbtion = new Cbtion();
		
		cbtion.setCreator(creator);
		cbtion.setProject(project);
		cbtion.setTitle(title);
		cbtion.setDescription(description);
		
		return cbtion;
	}
	
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getProduct() {
		return product;
	}
	public void setProduct(String product) {
		this.product = product;
	}
	public Integer getRelevance() {
		return relevance;
	}
	public void setRelevance(Integer relevance) {
		this.relevance = relevance;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getnBids() {
		return nBids;
	}
	public void setnBids(int nBids) {
		this.nBids = nBids;
	}
	public String getContributorUsername() {
		return contributorUsername;
	}
	public void setContributorUsername(String contributorUsername) {
		this.contributorUsername = contributorUsername;
	}
	public double getAssignedPpoints() {
		return assignedPpoints;
	}
	public void setAssignedPpoints(double assignedPpoints) {
		this.assignedPpoints = assignedPpoints;
	}
	
}
