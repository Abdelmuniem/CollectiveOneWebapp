package org.collectiveone.modules.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

@Entity
@Table(name = "model_sections")
public class ModelSection {
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator",
		parameters = { @Parameter( name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy") })
	@Column(name = "id", updatable = false, nullable = false)
	private UUID id;
	
	@Column(name = "title", length = 30)
	private String title;
	
	@Lob
	@Type(type = "org.hibernate.type.TextType")
	@Column(name = "description")
	private String description;
	
	@OneToMany
	@OrderColumn(name = "cards_order")
	private List<ModelCardWrapper> cards = new ArrayList<ModelCardWrapper>();
	
	@OneToMany
	@OrderColumn(name = "subsections_order")
	private List<ModelSection> subsections = new ArrayList<ModelSection>();
	
	
	public ModelSectionDto toDto(Integer level) {
		ModelSectionDto sectionDto = new ModelSectionDto();
		
		sectionDto.setId(id.toString());
		sectionDto.setTitle(title);
		sectionDto.setDescription(description);
		
		for (ModelCardWrapper cardWrapper : cards) {
			sectionDto.getCards().add(cardWrapper.getCard().toDto());
		}
		
		if (level >= 1) {
			for (ModelSection subsection : subsections) {
				sectionDto.getSubsections().add(subsection.toDto(level - 1));
			}
		}
		
		return sectionDto;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<ModelCardWrapper> getCards() {
		return cards;
	}

	public void setCards(List<ModelCardWrapper> cards) {
		this.cards = cards;
	}

	
}
