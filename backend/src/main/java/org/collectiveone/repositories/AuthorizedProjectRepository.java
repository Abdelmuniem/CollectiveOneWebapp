package org.collectiveone.repositories;

import org.collectiveone.model.AuthorizedProject;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;


@Repository
public class AuthorizedProjectRepository extends BaseRepository {

	public AuthorizedProjectRepository() {
		super();
	}

	public AuthorizedProject get(String projectName) {
		Criteria query = sessionFactory.getCurrentSession().createCriteria(AuthorizedProject.class);
		query.add(Restrictions.eq("projectName", projectName));
		AuthorizedProject res = (AuthorizedProject) query.uniqueResult();
		return res;
	}
	
}
