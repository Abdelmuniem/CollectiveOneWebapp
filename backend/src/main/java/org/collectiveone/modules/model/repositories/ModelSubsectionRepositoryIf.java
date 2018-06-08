package org.collectiveone.modules.model.repositories;

import java.util.List;
import java.util.UUID;

import org.collectiveone.modules.model.ModelScope;
import org.collectiveone.modules.model.ModelSubsection;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ModelSubsectionRepositoryIf extends CrudRepository<ModelSubsection, UUID> {
	
	ModelSubsection findById(UUID subsectionId);
	
	@Query("SELECT subsection FROM ModelSubsection subsection "
			+ "JOIN subsection.section sec "
			+ "WHERE sec.id= ?2 "
			+ "AND subsection.inSection.id = ?1 "
			+ "AND (subsection.scope != 'PRIVATE' OR subsection.adder.c1Id = ?3) "
			+ "AND (subsection.status != 'DELETED' OR subsection.status IS NULL)")
	ModelSubsection findByParentSectionAndSectionVisibleToUser(UUID parentSectionId, UUID sectionId, UUID adderId);
	
	@Query("SELECT subsection FROM ModelSubsection subsection "
			+ "WHERE "
			+ "subsection.section.id = ?1 "
			+ "AND subsection.scope = ?2 "
			+ "AND (subsection.status != 'DELETED' OR subsection.status IS NULL) "
			+ "AND subsection.beforeElement IS NULL")
	List<ModelSubsection> findLastByParentSectionAndScope(UUID sectionId, ModelScope scope);	
	
	@Query("SELECT subsection FROM ModelSubsection subsection "
			+ "WHERE "
			+ "crdWrpAdd.section.id = ?1 "
			+ "AND subsection.adder.c1Id = ?2 "
			+ "AND subsection.scope = ?3 "
			+ "AND (subsection.status != 'DELETED' OR subsection.status IS NULL) "
			+ "AND subsection.beforeElement IS NULL")
	List<ModelSubsection> findLastByParentSectionAndAdderAndScope(UUID sectionId, UUID adderId, ModelScope scope);	
	
		
}
