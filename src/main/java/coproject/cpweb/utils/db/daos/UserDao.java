package coproject.cpweb.utils.db.daos;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Service;

import coproject.cpweb.utils.db.entities.Project;
import coproject.cpweb.utils.db.entities.User;

@Service
public class UserDao extends BaseDao {
	
	public User get(Integer id) {
		return (User) super.get(id,User.class);
	}
	
	public List<User> getAll(Integer max) {
		return (List<User>) super.getAll(max,User.class);
	}
	
	public List<User> getFromRef(User refUser) {
		return (List<User>) super.get(refUser,User.class);
	}
	
	public int getN() {
		return super.getN(User.class);
	}
	
	public User get(String username) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(User.class);
		query.add(Restrictions.eq("username", username));

		return (User) query.uniqueResult();
	}
	
	public Project getProjectContributed(int userId, int projectId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				"SELECT project "
						+ "FROM User user "
						+ "JOIN user.projectsContributed project "
						+ "WHERE user.id = :uId "
						+ "AND project.id = :pId"
				);
		
		query.setParameter("uId", userId);
		query.setParameter("pId", projectId);
		
		return (Project) query.uniqueResult();
	}
	
	public void addProjectContributed(int userId, int projectId) {
		Session session = sessionFactory.getCurrentSession();
		
		if(getProjectContributed(userId,projectId) == null) {
			User user = get(userId);
			user.getProjectsContributed().add(session.get(Project.class,projectId));
			save(user);
		}
	}
	
	public List<String> getSuggestions(String query) {
		Session session = sessionFactory.getCurrentSession();
		
		@SuppressWarnings("unchecked")
		List<String> res = (List<String>) session.createCriteria(User.class)
				.add(Restrictions.ilike("username", query, MatchMode.ANYWHERE))
				.setProjection(Projections.property("username"))
				.list();
		
		return res;
	}
}
