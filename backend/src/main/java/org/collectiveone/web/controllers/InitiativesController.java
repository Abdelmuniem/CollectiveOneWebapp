package org.collectiveone.web.controllers;

import java.util.List;
import java.util.UUID;

import org.collectiveone.model.basic.AppUser;
import org.collectiveone.model.enums.ContributorRole;
import org.collectiveone.services.AppUserService;
import org.collectiveone.services.InitiativeService;
import org.collectiveone.web.dto.AssignationDto;
import org.collectiveone.web.dto.ContributorDto;
import org.collectiveone.web.dto.GetResult;
import org.collectiveone.web.dto.InitiativeDto;
import org.collectiveone.web.dto.NewInitiativeDto;
import org.collectiveone.web.dto.PostResult;
import org.collectiveone.web.dto.TransferDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/1")
public class InitiativesController { 
	
	@Autowired
	InitiativeService initiativeService;
	
	@Autowired
	AppUserService appUserService;
	
	@RequestMapping(path = "/secured/initiative", method = RequestMethod.POST)
	public PostResult postInitiative(@RequestBody NewInitiativeDto initiativeDto) {
		return initiativeService.init(getLoggedUser().getC1Id(), initiativeDto);
	}
	
	@RequestMapping(path = "/secured/initiative/{id}", method = RequestMethod.GET)
	public GetResult<InitiativeDto> getInitiative(
			@PathVariable("id") String id, 
			@RequestParam(defaultValue = "false") boolean addAssets,
			@RequestParam(defaultValue = "false") boolean addSubinitiatives,
			@RequestParam(defaultValue = "false") boolean addContributors) {
		
		InitiativeDto initiativeDto = null;
		
		if(!addAssets) {
			initiativeDto = initiativeService.getLight(UUID.fromString(id));
		} else {
			initiativeDto = initiativeService.getWithOwnAssets(UUID.fromString(id));
		}
		
		if(addSubinitiatives) {
			initiativeDto.setSubInitiatives(initiativeService.getSubinitiativesTree(UUID.fromString(id)));
		}
		
		if(addContributors) {
			initiativeDto.setContributors(initiativeService.getContributors(UUID.fromString(id)));
		}
		
		return new GetResult<InitiativeDto>("success", "initiative retrieved", initiativeDto);
	}
	
	@RequestMapping(path = "/secured/initiatives/mines", method = RequestMethod.GET)
	public GetResult<List<InitiativeDto>> myInitiatives() {
		return initiativeService.getOfUser(getLoggedUser().getC1Id());
	}
	
	@RequestMapping(path = "/secured/initiatives/suggestions", method = RequestMethod.GET)
	public GetResult<List<InitiativeDto>> suggestions(@RequestParam("q") String query) {
		return initiativeService.searchBy(query);
	}
	
	@RequestMapping(path = "/secured/initiative/{id}/contributor", method = RequestMethod.POST) 
	public PostResult addContributor(@PathVariable("id") String id, @RequestBody ContributorDto contributorDto) {
		return initiativeService.postContributor(
				UUID.fromString(contributorDto.getInitiativeId()), 
				UUID.fromString(contributorDto.getUser().getC1Id()),
				ContributorRole.valueOf(contributorDto.getRole()));
	}
	
	@RequestMapping(path = "/secured/initiative/{initiativeId}/contributor/{contributorId}", method = RequestMethod.DELETE) 
	public PostResult deleteContributor(@PathVariable("initiativeId") String initiativeId, @PathVariable("contributorId") String contributorId) {
		return initiativeService.deleteContributor(getLoggedUser().getC1Id(), UUID.fromString(contributorId));
	}
	
	@RequestMapping(path = "/secured/initiative/{initiativeId}/assignation", method = RequestMethod.POST)
	public PostResult newAssignation(@PathVariable("initiativeId") String initiativeId, @RequestBody AssignationDto assignation) {
		
		switch(assignation.getType()) {
		case "direct":
			return initiativeService.makeDirectAssignation(UUID.fromString(initiativeId), assignation);
			
		case "peer-reviewed":
//			return initiativeService.createPeerReviewedAssignation(UUID.fromString(initiativeId), assignation);
		}
		
		return new PostResult("error", "error", "");
		
	} 
	
	private AppUser getLoggedUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return appUserService.getFromAuth0Id(auth.getName());
	}
	
	
}
