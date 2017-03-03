package org.collectiveone.repositories;

import java.util.List;

import org.collectiveone.model.Activity;
import org.collectiveone.web.dto.Filters;
import org.collectiveone.web.dto.ObjectListRes;
import org.hibernate.Criteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;


@Repository
public class ActivityRepository extends BaseRepository {

	@Autowired
	private ProjectRepository projectDao;
	
	public ActivityRepository() {
		super();
	}

	public Activity get(Integer id) {
		return (Activity) super.get(id,Activity.class);
	}

	public List<Activity> getAll(Integer max) {
		return (List<Activity>) super.getAll(max,Activity.class);
	}
	
	public ObjectListRes<Activity> get(Filters filters) {
		Criteria q = applyGeneralFilters(filters, projectDao.getNamesEnabled(), Activity.class);
		return getObjectsAndResSet(q, filters, Activity.class);
	}
	
}
