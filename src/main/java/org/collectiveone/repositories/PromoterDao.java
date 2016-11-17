package org.collectiveone.repositories;

import java.util.List;

import org.collectiveone.model.Promoter;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class PromoterDao extends BaseDao {

	public PromoterDao() {
		super();
	}

	public Promoter get(Integer id) {
		return (Promoter) super.get(id,Promoter.class);
	}

	public List<Promoter> getAll(Integer max) {
		return (List<Promoter>) super.getAll(max,Promoter.class);
	}

	public Promoter getOfCbtion(Long cbtionId, Long userId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				" SELECT prom "
						+ "FROM Cbtion cbt "
						+ "JOIN cbt.promoters prom "
						+ "WHERE cbt.id = :cId "
						+ "AND prom.user.id = :uId"
				);

		query.setParameter("cId", cbtionId);
		query.setParameter("uId", userId);

		return (Promoter) query.uniqueResult();
	}
	
	public Promoter getOfComment(Long commentId, Long userId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				" SELECT prom "
						+ "FROM Comment com "
						+ "JOIN com.promoters prom "
						+ "WHERE com.id = :cId "
						+ "AND prom.user.id = :uId"
				);

		query.setParameter("cId", commentId);
		query.setParameter("uId", userId);

		return (Promoter) query.uniqueResult();
	}

}
