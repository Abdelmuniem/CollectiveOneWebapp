package org.collectiveone.web.controllers;

import java.util.List;
import java.util.UUID;

import org.collectiveone.model.AppUser;
import org.collectiveone.services.AppUserService;
import org.collectiveone.services.InitiativeService;
import org.collectiveone.web.dto.GetResult;
import org.collectiveone.web.dto.InitiativeDto;
import org.collectiveone.web.dto.NewInitiativeDto;
import org.collectiveone.web.dto.PostResult;
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
	
	/* level: 0, light, 1 ownAssets, 2 ownAssets and subinitiatives assets*/
	@RequestMapping(path = "/secured/initiative/{id}", method = RequestMethod.GET)
	public GetResult<InitiativeDto> getInitiative(
			@PathVariable("id") String id, 
			@RequestParam(defaultValue = "light") String level) {
		
		InitiativeDto initiativeDto = null;
		switch (level) {
		case "light": 
			initiativeDto = initiativeService.getLight(UUID.fromString(id));
			break;
		
		case "withAssets": 
			initiativeDto = initiativeService.getWithOwnAssets(UUID.fromString(id));
			break;
			
		case "withSubinitiativesAssets": 
			initiativeDto = initiativeService.getWithSubInitiativesAssets(UUID.fromString(id));
			break;
		}
		
		return new GetResult<InitiativeDto>("success", "initiative retrieved", initiativeDto);
	}
	
	@RequestMapping(path = "/secured/initiatives/mines", method = RequestMethod.GET)
	public GetResult<List<InitiativeDto>> myInitiatives() {
		return initiativeService.getOfUser(getLoggedUser().getC1Id());
	}
	
	@RequestMapping(path = "/secured/initiatives/search", method = RequestMethod.GET)
	public GetResult<List<InitiativeDto>> search(@RequestParam("q") String query) {
		return initiativeService.searchBy(query);
	}
	
	private AppUser getLoggedUser() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return appUserService.getFromAuth0Id(auth.getName());
	}
	
	
}
