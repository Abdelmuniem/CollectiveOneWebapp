package coproject.cpweb.utils.db.daos;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import coproject.cpweb.utils.db.entities.Bid;
import coproject.cpweb.utils.db.entities.BidState;
import coproject.cpweb.utils.db.entities.Cbtion;
import coproject.cpweb.utils.db.entities.CbtionState;
import coproject.cpweb.utils.db.entities.Comment;
import coproject.cpweb.utils.db.entities.DecisionState;
import coproject.cpweb.utils.db.entities.Goal;
import coproject.cpweb.utils.db.services.Filters;
import coproject.cpweb.utils.db.services.ObjectListRes;

@Service
public class CbtionDao extends BaseDao {
	
	public Cbtion get(Integer id) {
		return (Cbtion) super.get(id,Cbtion.class);
	}
	
	public List<Cbtion> getAll(Integer max) {
		return (List<Cbtion>) super.getAll(max,Cbtion.class);
	}
	
	public List<Cbtion> get(Cbtion refCbtion) {
		return (List<Cbtion>) super.get(refCbtion,Cbtion.class);
	}
	
	public void remove(int id) {
		Cbtion cbtion = get(id);
		
		cbtion.setState(CbtionState.DELETED);
		cbtion.setDeleteDate(new Timestamp(System.currentTimeMillis()));
		
		if(cbtion.getOpenDec() != null)	cbtion.getOpenDec().setState(DecisionState.CLOSED_EXTERNALLY);
		
		BidDao bidDao = new BidDao();
		bidDao.setSessionFactory(sessionFactory);
		
		for(Bid bid : cbtion.getBids()) {
			bidDao.remove(bid.getId());
		}
		
		save(cbtion);
	}
	
	public List<Cbtion> getWithStates(List<CbtionState> states) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Cbtion.class);
		
		Disjunction stateDisj = Restrictions.disjunction();
		for(CbtionState state : states) {
			stateDisj.add(Restrictions.eq("state", state));	
		}
		
		query.add(stateDisj);
		
		@SuppressWarnings("unchecked")
		List<Cbtion> res = (List<Cbtion>) query.list();
		
		return res;
	}
	
	public ObjectListRes<Cbtion> get(Filters filters) {
		
		Criteria q = applyGeneralFilters(filters, Cbtion.class);
		
		/* State names are entity specific and I was not able to put these
		 * disjunction in a common function*/
		
		List<String> stateNames = filters.getStateNames();
		Disjunction stateDisj = Restrictions.disjunction();
		for(String stateName:stateNames) {	
			stateDisj.add( Restrictions.eq("state", CbtionState.valueOf(stateName)));
		}
		
		q.add(stateDisj);
		
		/* if contributorUsername requested */
		String contributorUsername = filters.getContributorUsername();
		if(contributorUsername != null) {
			if(!contributorUsername.equals("")) {
				q.createAlias("contributor","cont")
					.add(Restrictions.eq("cont.username",contributorUsername));
			}
		}
		
		/* if goalTag requested */
		if(filters.getProjectNames().size() == 1) {
			String projectName = filters.getProjectNames().get(0);
			String goalTag = filters.getGoalTag();
			if(goalTag != null) {
				if(!goalTag.equals("")) {
					
					GoalDao goalDao = new GoalDao();
					goalDao.setSessionFactory(this.getSessionFactory());
					Goal goal = goalDao.get(goalTag, projectName);
					
					q.createAlias("goal","go");
					
					Disjunction goalDisj = Restrictions.disjunction();
					goalDisj.add(Restrictions.eq("go.id",goal.getId()));
					
					if(filters.getGoalSubgoalsFlag()) {
						for(Goal subgoal : goalDao.getSubgoalsIteratively(goal.getId())) {
							goalDisj.add(Restrictions.eq("go.id",subgoal.getId()));
						}
					}
					
					q.add(goalDisj);
				}
			}
		}
		
		return getObjectsAndResSet(q, filters, Cbtion.class);
	}
	
	public List<Cbtion> getAcceptedInProject(int projectId) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Cbtion.class);
		
		query.add(Restrictions.eq("state", CbtionState.ACCEPTED))
			.add(Restrictions.eq("project.id", projectId));
		
		@SuppressWarnings("unchecked")
		List<Cbtion> res = (List<Cbtion>) query.list();
		
		return res;
	}
	
	public List<Cbtion> getAcceptedOfUserInProject(int contributorId, int projectId) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Cbtion.class);
		
		query.add(Restrictions.eq("state", CbtionState.ACCEPTED))
			.add(Restrictions.eq("project.id", projectId))
			.add(Restrictions.eq("contributor.id", contributorId));
		
		@SuppressWarnings("unchecked")
		List<Cbtion> res = (List<Cbtion>) query.list();
		
		return res;
	}
	
	public int countPromotersDiff(int cbtionId) {
		return countPromoters(cbtionId, true) - countPromoters(cbtionId, false); 
	}
	
	
	public int countPromoters(int cbtionId, boolean promoteUp) {
		
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				"SELECT COUNT(*) "
						+" FROM Cbtion cbt "
						+ "JOIN cbt.promoters prom "
						+ "WHERE cbt.id = :cId "
						+ "AND prom.promoteUp = :puId "
				);
		
		query.setParameter("cId", cbtionId);
		query.setParameter("puId", promoteUp);
		
		Long count = (Long) query.uniqueResult();
		
		return (int)(count + 0);
	}	
	
	public List<Comment> getCommentsSorted(int cbtionId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				"SELECT coms "
						+" FROM Cbtion cbt "
						+ "JOIN cbt.comments coms "
						+ "WHERE cbt.id = :cId "
						+ "ORDER BY coms.relevance DESC"
				);
		
		query.setParameter("cId", cbtionId);
				
		@SuppressWarnings("unchecked")
		List<Comment> res = (List<Comment>) query.list();
		
		return res;
	}
	
	public Bid getAcceptedBid(int cbtionId) {
		Session session = sessionFactory.getCurrentSession();

		Criteria q = session.createCriteria(Bid.class)
				.createAlias("cbtion", "cbt")
				.add(Restrictions.eq("state",BidState.ACCEPTED))
				.add(Restrictions.eq("cbt.id",cbtionId));
						
		Bid res = (Bid) q.uniqueResult();
		
		return res;
	}
	
}
