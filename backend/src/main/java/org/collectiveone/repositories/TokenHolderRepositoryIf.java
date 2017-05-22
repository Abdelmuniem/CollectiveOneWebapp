package org.collectiveone.repositories;

import java.util.List;
import java.util.UUID;

import org.collectiveone.model.basic.TokenHolder;
import org.collectiveone.model.basic.TokenType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TokenHolderRepositoryIf extends CrudRepository<TokenHolder, UUID> {
	
	TokenHolder findByTokenTypeIdAndHolderId(UUID tokenTypeId, UUID holderId);
	
	@Query("SELECT holder.tokenType FROM TokenHolder holder WHERE holder.holderId = ?1")
	List<TokenType> getTokenTypesHeldBy(UUID holderId);
	
	
	@Query("SELECT holder.tokenType FROM TokenHolder holder WHERE holder.holderId = ?1 AND holder.tokenType.id != ?2")
	List<TokenType> getTokenTypesHeldByOtherThan(UUID holderId, UUID tokenTypeId);
}
