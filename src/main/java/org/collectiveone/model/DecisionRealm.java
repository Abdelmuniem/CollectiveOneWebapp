package org.collectiveone.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table( name = "DECISION_REALMS" )
public class DecisionRealm {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;
	@OneToMany(mappedBy="realm", cascade=CascadeType.ALL)
	private List<Voter> voters = new ArrayList<Voter>();
	private double weightTot; /* aggregation of weights to avoid recomputing it*/
	@OneToOne
	private Goal goal;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public List<Voter> getVoters() {
		return voters;
	}
	public void setVoters(List<Voter> voters) {
		this.voters = voters;
	}
	public Goal getGoal() {
		return goal;
	}
	public void setGoal(Goal goal) {
		this.goal = goal;
	}
	public double getWeightTot() {
		return weightTot;
	}
	public void setWeightTot(double weightTot) {
		this.weightTot = weightTot;
	}
	
	public int size() {
		return this.getVoters().size();
	}
	
}

