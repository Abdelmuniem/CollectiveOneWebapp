package coproject.cpweb.utils.db.entities;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import coproject.cpweb.utils.db.entities.Project;
import coproject.cpweb.utils.db.entities.User;
import coproject.cpweb.utils.db.entities.dtos.CbtionDto;

@Entity
@Table( name = "CBTIONS" )
public class Cbtion {
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;
	@ManyToOne
	private Project project;
	@ManyToOne
	private User creator;
	private Timestamp creationDate;
	private String title;
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	private String description;
	private String product;
	private CbtionState state;	
	@OneToMany(cascade=CascadeType.ALL)
	private List<Bid> bids = new ArrayList<Bid>();
	@ManyToOne
	private User contributor;
	private double assignedPpoints;
	@ManyToOne
	private Goal goal;
	@OneToOne
	private Decision openDec;
	@OneToMany(cascade=CascadeType.ALL)
	@JoinTable(name = "CBTIONS_PROMOTERS")
	private List<Promoter> promoters = new ArrayList<Promoter>();
	private int relevance;
	
	@OneToMany(cascade=CascadeType.ALL)
	@JoinTable(name = "CBTIONS_COMMENTS")
	private List<Comment> comments = new ArrayList<Comment>();
	
	public CbtionDto toDto() {
		CbtionDto dto = new CbtionDto();
		
		dto.setTitle(title);
		dto.setProjectName(project.getName());
		dto.setId(id);
		dto.setDescription(description);
		dto.setProduct(product);
		dto.setCreatorUsername(creator.getUsername());
		dto.setCreationDate(creationDate.getTime());
		dto.setState(state.toString());
		if(bids != null) dto.setnBids(bids.size());
		if(contributor != null) dto.setContributorUsername(contributor.getUsername());
		if(goal != null) dto.setGoalTag(goal.getGoalTag());
		dto.setAssignedPpoints(assignedPpoints);
		if(openDec != null) dto.setOpenDec(openDec.toDto());
		dto.setRelevance(relevance);
		dto.setNcomments(comments.size());
		
		return dto;
	}
	
	public CbtionDto toDto(List<String> parentGoalsTags) {
		CbtionDto dto = this.toDto();
		dto.setParentGoalsTags(parentGoalsTags);
		
		return dto;
	}
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	public User getCreator() {
		return creator;
	}
	public void setCreator(User creator) {
		this.creator = creator;
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
	public CbtionState getState() {
		return state;
	}
	public void setState(CbtionState state) {
		this.state = state;
	}
	public User getContributor() {
		return contributor;
	}
	public void setContributor(User contributor) {
		this.contributor = contributor;
	}
	public List<Bid> getBids() {
		return bids;
	}
	public void setBids(List<Bid> bids) {
		this.bids = bids;
	}
	public double getAssignedPpoints() {
		return assignedPpoints;
	}
	public void setAssignedPpoints(double assignedPpoints) {
		this.assignedPpoints = assignedPpoints;
	}
	public Goal getGoal() {
		return goal;
	}
	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	public Decision getOpenDec() {
		return openDec;
	}
	public void setOpenDec(Decision openDec) {
		this.openDec = openDec;
	}
	public List<Promoter> getPromoters() {
		return promoters;
	}
	public void setPromoters(List<Promoter> promoters) {
		this.promoters = promoters;
	}
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	public List<Comment> getComments() {
		return comments;
	}
	public void setComments(List<Comment> comments) {
		this.comments = comments;
	}
	
}
