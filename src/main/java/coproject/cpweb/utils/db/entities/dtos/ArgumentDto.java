package coproject.cpweb.utils.db.entities.dtos;

public class ArgumentDto {
	private int id;
	private String creatorUsername;
	private long creationDate;
	private String description;
	private int DecisionId;
	private String tendency;
	private int nbackers;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCreatorUsername() {
		return creatorUsername;
	}
	public void setCreatorUsername(String creatorUsername) {
		this.creatorUsername = creatorUsername;
	}
	public long getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(long creationDate) {
		this.creationDate = creationDate;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getDecisionId() {
		return DecisionId;
	}
	public void setDecisionId(int decisionId) {
		DecisionId = decisionId;
	}
	public String getTendency() {
		return tendency;
	}
	public void setTendency(String tendency) {
		this.tendency = tendency;
	}
	public int getNbackers() {
		return nbackers;
	}
	public void setNbackers(int nbackers) {
		this.nbackers = nbackers;
	}
	
}
