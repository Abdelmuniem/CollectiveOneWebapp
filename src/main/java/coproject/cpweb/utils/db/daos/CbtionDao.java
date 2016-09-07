package coproject.cpweb.utils.db.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import coproject.cpweb.utils.db.entities.Cbtion;
import coproject.cpweb.utils.db.entities.CbtionState;
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
	
	public List<Cbtion> getWithState(CbtionState state) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(Cbtion.class);
		query.add(Restrictions.eq("state", state));
		
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
		
		return getObjectsAndResSet(q, filters.getPage(), filters.getNperpage(), Cbtion.class);
	}
	
	public List<Cbtion> getAcceptedOfUserInProject(int userId, int projectId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				"SELECT cbtion "
						+" FROM User user "
						+ "JOIN user.cbtionsAccepted cbtion "
						+ "WHERE user.id = :uId "
						+ "AND cbtion.project.id = :pId "
				);
		
		query.setParameter("uId", userId);
		query.setParameter("pId", projectId);
		
		@SuppressWarnings("unchecked")
		List<Cbtion> res = (List<Cbtion>) query.list();
		
		return res;
	}

}
