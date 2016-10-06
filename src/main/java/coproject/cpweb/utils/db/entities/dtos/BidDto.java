package coproject.cpweb.utils.db.entities.dtos;

import java.sql.Timestamp;

import coproject.cpweb.utils.db.entities.Bid;
import coproject.cpweb.utils.db.entities.Cbtion;
import coproject.cpweb.utils.db.entities.User;

public class BidDto {
	private int id;
	private int cbtionId;
	private String cbtionTitle;
	private UserDto creatorDto;
	private double ppoints;
	private long creationDate;
	private String description;
	private long deliveryDate;
	private String state;
	private DecisionDto assignDec;
	private DecisionDto acceptDec;
	private String doneState;
	private long doneDate;
	private String doneDescription;
	
	
	
	public Bid toBid(User creator, Cbtion cbtion) {
		Bid bid = new Bid();
		
		bid.setId(id);
		bid.setCbtion(cbtion);
		bid.setCreator(creator);
		bid.setPpoints(ppoints);
		bid.setCreationDate(new Timestamp(creationDate));
		bid.setDescription(description);
		bid.setDeliveryDate(new Timestamp(deliveryDate));
		
		return bid;
	}

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getCbtionId() {
		return cbtionId;
	}
	public void setCbtionId(int cbtionId) {
		this.cbtionId = cbtionId;
	}
	public String getCbtionTitle() {
		return cbtionTitle;
	}
	public void setCbtionTitle(String cbtionTitle) {
		this.cbtionTitle = cbtionTitle;
	}
	public UserDto getCreatorDto() {
		return creatorDto;
	}
	public void setCreatorDto(UserDto creatorDto) {
		this.creatorDto = creatorDto;
	}
	public double getPpoints() {
		return ppoints;
	}
	public void setPpoints(double ppoints) {
		this.ppoints = ppoints;
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
	public long getDeliveryDate() {
		return deliveryDate;
	}
	public void setDeliveryDate(long deliveryDate) {
		this.deliveryDate = deliveryDate;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public DecisionDto getAssignDec() {
		return assignDec;
	}
	public void setAssignDec(DecisionDto assignDec) {
		this.assignDec = assignDec;
	}
	public DecisionDto getAcceptDec() {
		return acceptDec;
	}
	public void setAcceptDec(DecisionDto acceptDec) {
		this.acceptDec = acceptDec;
	}
	public String getDoneState() {
		return doneState;
	}
	public void setDoneState(String doneState) {
		this.doneState = doneState;
	}
	public long getDoneDate() {
		return doneDate;
	}
	public void setDoneDate(long doneDate) {
		this.doneDate = doneDate;
	}
	public String getDoneDescription() {
		return doneDescription;
	}
	public void setDoneDescription(String doneDescription) {
		this.doneDescription = doneDescription;
	}
	

}
