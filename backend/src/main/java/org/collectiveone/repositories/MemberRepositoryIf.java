package org.collectiveone.repositories;

import java.util.UUID;

import org.collectiveone.model.support.Member;
import org.springframework.data.repository.CrudRepository;

public interface MemberRepositoryIf extends CrudRepository<Member, UUID> {
	
}
