package org.collectiveone.repositories;

import java.util.ArrayList;
import java.util.List;

import org.collectiveone.model.Goal;
import org.collectiveone.model.GoalState;
import org.collectiveone.services.Filters;
import org.collectiveone.services.ObjectListRes;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

@Repository
public class GoalDao extends BaseDao {
	
	public GoalDao() {
		super();
	}

	public Goal get(Long id) {
		return (Goal) super.get(id,Goal.class);
	}
	
	public Goal get(String goalTag, String projectName) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Goal.class);
		query.add(Restrictions.eq("goalTag", goalTag))
			.createAlias("project", "prj")
			.add(Restrictions.eq("prj.name", projectName));
		
		return (Goal) query.uniqueResult();
	}
	
	public List<Goal> getAll(Integer max) {
		return (List<Goal>) super.getAll(max,Goal.class);
	}
	
	public List<Goal> getNotDeleted() {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Goal.class);
		query.add(Restrictions.or(
				Restrictions.eq("state", GoalState.PROPOSED),
				Restrictions.eq("state", GoalState.ACCEPTED)));
		
		@SuppressWarnings("unchecked")
		List<Goal> res = (List<Goal>) query.list();
		
		return res;
	}
	
	public List<String> getSuggestions(String query, List<String>  projectNames) {
		Session session = sessionFactory.getCurrentSession();
		
		Criteria q = session.createCriteria(Goal.class,"go")
				.add(Restrictions.eq("state", GoalState.ACCEPTED))
				.add(Restrictions.ilike("goalTag", query, MatchMode.ANYWHERE));
			
		if(projectNames.size() > 0) {
			q.createAlias("go.project", "pr");
			
			Disjunction prDisj = Restrictions.disjunction();
			for(String projectName : projectNames) {
				prDisj.add( Restrictions.eq("pr.name", projectName));
			}
			q.add(prDisj);
		}
		
		@SuppressWarnings("unchecked")
		List<String> res = q.setProjection(Projections.property("goalTag"))
				.list();
		
		return res;
	}
	
	public ObjectListRes<Goal> get(Filters filters) {
		
		Criteria q = applyGeneralFilters(filters, Goal.class);
		
		/* State names are entity specific and I was not able to put these
		 * disjunction in a common function */
		
		if(filters.getStateNames() != null) {
			List<String> stateNames = filters.getStateNames();
			Disjunction stateDisj = Restrictions.disjunction();
			for(String stateName:stateNames) {	
				stateDisj.add( Restrictions.eq("state", GoalState.valueOf(stateName)));
			}
			
			q.add(stateDisj);
		}
		
		if(filters.getOnlyParents()) {
			q.add(Restrictions.isNull("parent"));
		}
	
		return getObjectsAndResSet(q, filters, Goal.class);
		
	}
	
	public List<Goal> getSubgoalsIteratively(Long goalId) {
		
		List<Goal> subgoals = getSubgoals(goalId);
		int nsubgoals = subgoals.size();
		
		for(int ix = 0; ix < nsubgoals ; ix++) {
			Goal subgoal = subgoals.get(ix);
			
			/* reentrance */ 
			List<Goal> subsubgoals = getSubgoalsIteratively(subgoal.getId());
			for(Goal subsubgoal : subsubgoals) {
				subgoals.add(subsubgoal);
			}
		}
		
		return subgoals;
	}
	
	public List<Goal> getSubgoals(Long goalId) {
		
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Goal.class,"go");
		
		query.createAlias("go.parent","pa")
			.add(Restrictions.eq("pa.id", goalId));
		
		@SuppressWarnings("unchecked")
		List<Goal> res = (List<Goal>) query.list();
		
		return res;
	}
	
	public List<Goal> getSubgoals(Long goalId, List<GoalState> states) {
		
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Goal.class,"go");
		
		if(states != null) {
			Disjunction stateDisj = Restrictions.disjunction();
			for(GoalState state:states) {	
				stateDisj.add( Restrictions.eq("state", state));
			}	
			query.add(stateDisj);
		}
		
		query.createAlias("go.parent","pa")
			.add(Restrictions.eq("pa.id", goalId));
		
		@SuppressWarnings("unchecked")
		List<Goal> res = (List<Goal>) query.list();
		
		return res;
	}
	
	public List<Goal> getAllParents(Long goalId) {
		
		List<Goal> parents = new ArrayList<Goal>();
		
		Goal goal = get(goalId);
		Goal parent = goal.getParent();
		int count = 0;
		
		while((parent != null) && (count < 20)) {
			parents.add(parent);
			parent = parent.getParent();
			count++;
		}
		return parents;
	}
	
}
