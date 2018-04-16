package org.collectiveone.modules.model.repositories;

import java.util.List;
import java.util.UUID;

import org.collectiveone.modules.model.ModelSection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface ModelSectionRepositoryIf extends CrudRepository<ModelSection, UUID> {

	public ModelSection findById(UUID sectionId);
	
	@Query("SELECT section.id from ModelSection section WHERE sec.id = ?1")
	public UUID getId(UUID sectionId);
	
	default Boolean sectionExists(UUID sectionId) {
		UUID res = getId(sectionId);
		return res != null;
	}
	
	@Query("SELECT section from ModelSection section JOIN section.subsections subsec WHERE subsec.id = ?1")
	public List<ModelSection> findParentSections(UUID sectionId);
	
	@Query("SELECT section.id from ModelSection section JOIN section.subsections subsec WHERE subsec.id = ?1")
	public List<UUID> findParentSectionsIds(UUID sectionId);
	
	@Query("SELECT subsec.id from ModelSection section JOIN section.subsections subsec WHERE sec.id = ?1")
	public List<UUID> findSubsectionsIds(UUID sectionId);
	
	@Query("SELECT sec from ModelSection sec " +
			"WHERE (LOWER(sec.title) LIKE ?1 OR LOWER(sec.description) LIKE ?1) " +
			"AND sec.initiative.id IN ?2)")
	public Page<ModelSection> searchBy(String query, List<UUID> initiativeId, Pageable page);

	@Query("SELECT subsec.id FROM ModelSection sec JOIN sec.subsections subsec WHERE sec.id = ?1")
	public List<UUID> getSubsectionsIds(UUID sectionId);
	
}
