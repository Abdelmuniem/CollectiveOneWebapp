package org.collectiveone.repositories;

import org.collectiveone.model.Contributor;
import org.collectiveone.model.Project;
import org.collectiveone.model.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

@Repository
public class ContributorRepository extends BaseRepository {

	public ContributorRepository() {
		super();
	}

	public void updateContributor(Long projectId, Long contributorUserId, double pps) {
		
		Session session = sessionFactory.getCurrentSession();
		
		Project project = (Project) session.get(Project.class,projectId);
		Contributor contributor = getContributor(projectId,contributorUserId);
		
		if(contributor == null) {
			/* if voter is not in the realm, then add him */
			contributor = new Contributor();
			contributor.setPps(pps);
			contributor.setContributorUser(session.get(User.class,contributorUserId));
			contributor.setProject(project);
			project.getContributors().add(contributor);
		}
		
		contributor.setPps(pps);
		
		save(contributor);
		save(project);
	}
	
	public Contributor getContributor(Long projectId, Long contributorUserId) {
		Session session = sessionFactory.getCurrentSession();

		Query query = session.createQuery(
				"SELECT contrib "
						+ "FROM Project proj "
						+ "JOIN proj.contributors contrib "
						+ "WHERE proj.id = :pId "
						+ "AND contrib.contributorUser.id = :cuId "
				);
		
		query.setParameter("pId", projectId);
		query.setParameter("cuId", contributorUserId);
		
		return (Contributor)query.uniqueResult();
	}
}
