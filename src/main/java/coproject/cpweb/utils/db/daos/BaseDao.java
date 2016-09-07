package coproject.cpweb.utils.db.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Example;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import coproject.cpweb.utils.db.services.Filters;
import coproject.cpweb.utils.db.services.ObjectListRes;

@Service
public class BaseDao {
	
	@Autowired
	SessionFactory sessionFactory;
	
	public int save(Object object) {
		Session session = sessionFactory.getCurrentSession();
		int id = (Integer) session.save(object);
		return id;
	}
	
	public void delete(Object object) {
		Session session = sessionFactory.getCurrentSession();
		session.delete(object);
	}
	
	public <T> int getN(Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		
		Long count = (Long) session.createCriteria(clazz)
				.setProjection(Projections.rowCount()).uniqueResult();
		
		return (int)(count + 0);
	}
	
	public <T> Object get(Integer id, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		T object = session.get(clazz,id);
		return object;
	}
	
	public <T> List<T> getAll(Integer max, Class<T> clazz) {
		Session session = sessionFactory.getCurrentSession();
		
		@SuppressWarnings("unchecked")
		List<T> res = (List<T>) session.createCriteria(clazz)
				.list();
		
		return res;
	}
	
	public <T> List<T> get(Object refObject, Class<T> clazz) {
		
		Session session = sessionFactory.getCurrentSession();
		@SuppressWarnings("unchecked")
		List<T> res = (List<T>) session.createCriteria(clazz)
							.add(Example.create(refObject))
							.list();
		
		return res;
	}
	
	public <T> Criteria applyGeneralFilters(Filters filters, Class<T> clazz) {
		
		Session session = sessionFactory.getCurrentSession();
		Criteria q = session.createCriteria(clazz);
		
		q.createAlias("project", "proj");
		List<String> projectNames = filters.getProjectNames();
		Disjunction projDisj = Restrictions.disjunction();
		for(String projectName:projectNames) {
			projDisj.add( Restrictions.eq("proj.name", projectName));
		}
		q.add(projDisj);
		
		q.createAlias("creator", "crea");
		List<String> creatorUsernames = filters.getCreatorUsernames();
		Disjunction userDisj = Restrictions.disjunction();
		for(String creatorUsername:creatorUsernames) {
			if(creatorUsername.length() > 0) {
				userDisj.add( Restrictions.eq("crea.username", creatorUsername));
			}
		}
		q.add(userDisj);
		
		String keyw = filters.getKeyw();
		if(keyw.length() > 0) {
			Criterion keywInDescRestr = Restrictions.ilike("description",keyw, MatchMode.ANYWHERE);
			
			Criterion keyWordCrit = null; 
			
			switch(clazz.getSimpleName()) {
			
				case "Cbtion":
					keyWordCrit = Restrictions.or(keywInDescRestr,Restrictions.ilike("title",keyw, MatchMode.ANYWHERE));
					break;
					
				case "Decision":
					keyWordCrit = keywInDescRestr;
					break;
					
				case "Goal":
					keyWordCrit = Restrictions.or(keywInDescRestr,Restrictions.ilike("goalTag",keyw, MatchMode.ANYWHERE));
					break;
			}
			
			q.add(keyWordCrit);
		}
		
		return q;
		
	}
	
	public <T> ObjectListRes<T> getObjectsAndResSet(Criteria q, int page, int nPerPage, Class<T> clazz) {
		
		Long count = (Long) q.setProjection(Projections.rowCount()).uniqueResult();
		
		// Remove the projection
		q.setProjection(null);
		q.setResultTransformer(Criteria.ROOT_ENTITY);
		
		@SuppressWarnings("unchecked")
		List<T> objects = (List<T>) q.setFirstResult((page-1)*nPerPage).setMaxResults(nPerPage).list();
		
		int lastRes = page*nPerPage;
		if(count < page*nPerPage) lastRes = (int) (count + 0);
		
		ObjectListRes<T> res = new ObjectListRes<T>();
		res.setObjects(objects);
		
		int[] resSet = {(page-1)*nPerPage+1,lastRes,(int) (count + 0)};
		res.setResSet(resSet);
		
		return res;
	}

}
