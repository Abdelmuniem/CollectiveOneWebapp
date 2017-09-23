package org.collectiveone.modules.activity.repositories;

import java.util.UUID;

import org.collectiveone.modules.activity.Activity;
import org.collectiveone.modules.activity.enums.ActivityType;
import org.springframework.data.repository.CrudRepository;

public interface ActivityRepositoryIf extends CrudRepository<Activity, UUID> {
	
	Activity findTop1ByAssignation_IdAndTypeOrderByTimestampDesc(UUID assignationId, ActivityType type);
	
}