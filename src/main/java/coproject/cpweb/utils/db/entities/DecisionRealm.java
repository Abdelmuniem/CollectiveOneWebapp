package coproject.cpweb.utils.db.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;

@Entity
@Table( name = "DECISION_REALMS" )
public class DecisionRealm {
	
	@Id
	@GeneratedValue(generator="increment")
	@GenericGenerator(name="increment", strategy = "increment")
	private int id;
	@OneToMany(cascade=CascadeType.ALL)
	private List<Voter> voters = new ArrayList<Voter>();
	@OneToOne
	private Project project;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public List<Voter> getVoters() {
		return voters;
	}
	public void setVoters(List<Voter> voters) {
		this.voters = voters;
	}
	public Project getProject() {
		return project;
	}
	public void setProject(Project project) {
		this.project = project;
	}
	
	
	public int size() {
		return this.getVoters().size();
	}
	
}

