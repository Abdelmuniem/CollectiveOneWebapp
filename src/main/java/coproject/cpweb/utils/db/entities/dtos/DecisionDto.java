package coproject.cpweb.utils.db.entities.dtos;

import java.sql.Timestamp;

public class DecisionDto {
	
	protected int id;
	protected String description;
	protected Timestamp creationDate;
	protected Timestamp openDate;
	protected String fromState;
	protected String toState;
	protected int nVoters;
	protected double ppsTot;
	protected double verdictHours;
	protected int verdict;
	protected String state;
	protected int nVotesCasted;
	protected double ppsCum;
	protected double pest;
	protected double stability;
	protected double clarity;
	protected double log_l1l0;
	protected double elapsedFactor;
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Timestamp getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}
	public Timestamp getOpenDate() {
		return openDate;
	}
	public void setOpenDate(Timestamp openDate) {
		this.openDate = openDate;
	}
	public String getFromState() {
		return fromState;
	}
	public void setFromState(String fromState) {
		this.fromState = fromState;
	}
	public String getToState() {
		return toState;
	}
	public void setToState(String toState) {
		this.toState = toState;
	}
	public int getnVoters() {
		return nVoters;
	}
	public void setnVoters(int nVoters) {
		this.nVoters = nVoters;
	}
	public double getPpsTot() {
		return ppsTot;
	}
	public void setPpsTot(double ppsTot) {
		this.ppsTot = ppsTot;
	}
	public double getVerdictHours() {
		return verdictHours;
	}
	public void setVerdictHours(double verdictHours) {
		this.verdictHours = verdictHours;
	}
	public int getVerdict() {
		return verdict;
	}
	public void setVerdict(int verdict) {
		this.verdict = verdict;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public int getnVotesCasted() {
		return nVotesCasted;
	}
	public void setnVotesCasted(int nVotesCasted) {
		this.nVotesCasted = nVotesCasted;
	}
	public double getPpsCum() {
		return ppsCum;
	}
	public void setPpsCum(double ppsCum) {
		this.ppsCum = ppsCum;
	}
	public double getPest() {
		return pest;
	}
	public void setPest(double pest) {
		this.pest = pest;
	}
	public double getStability() {
		return stability;
	}
	public void setStability(double stability) {
		this.stability = stability;
	}
	public double getClarity() {
		return clarity;
	}
	public void setClarity(double clarity) {
		this.clarity = clarity;
	}
	public double getLog_l1l0() {
		return log_l1l0;
	}
	public void setLog_l1l0(double log_l1l0) {
		this.log_l1l0 = log_l1l0;
	}
	public double getElapsedFactor() {
		return elapsedFactor;
	}
	public void setElapsedFactor(double elapsedFactor) {
		this.elapsedFactor = elapsedFactor;
	}
	
	
}

