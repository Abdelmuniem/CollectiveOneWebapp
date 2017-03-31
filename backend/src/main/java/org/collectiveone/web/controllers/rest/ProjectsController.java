package org.collectiveone.web.controllers.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.collectiveone.model.Project;
import org.collectiveone.model.User;
import org.collectiveone.services.ProjectServiceImp;
import org.collectiveone.services.UserServiceImp;
import org.collectiveone.web.dto.ActiveProject;
import org.collectiveone.web.dto.Filters;
import org.collectiveone.web.dto.ProjectContributorsDto;
import org.collectiveone.web.dto.ProjectDto;
import org.collectiveone.web.dto.ProjectDtoListRes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttributes;


@RestController
@RequestMapping("/1")
@SessionAttributes("activeProjects")
public class ProjectsController { // NO_UCD (unused code)
	
	@Autowired
	ProjectServiceImp projectService;
	
	@Autowired
	UserServiceImp userService;
	
	@RequestMapping(value="/project/{projectName}", method = RequestMethod.GET)
	public @ResponseBody ProjectDto get(@PathVariable("projectName") String projectName) {
		ProjectDto projectDto = projectService.getDto(projectName);
		
		/* if user logged, check all projects and fill the starred and watched flags */
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(""); 
		if(logged != null) {
			projectDto.setIsStarred(userService.isProjectStarred(projectDto.getId(),logged.getId()));
			projectDto.setIsWatched(userService.isProjectWatched(projectDto.getId(),logged.getId()));
		}
		
		return projectDto;
	}
	
	@RequestMapping(value="/project/{projectName}/contributors", method = RequestMethod.GET)
	public @ResponseBody ProjectContributorsDto getContributors(@PathVariable("projectName") String projectName) {
			
		Project project = projectService.get(projectName);
		
		ProjectContributorsDto projectContributorsDto = new ProjectContributorsDto();
		projectContributorsDto.setUsernamesAndData(projectService.getContributorsAndData(project.getId()));
		projectContributorsDto.setPpsTot(project.getPpsTot());
		
		return projectContributorsDto;
	}
	
	@RequestMapping(value="/projects/getNamesEnabled", method = RequestMethod.GET)
	public Map<String,Object> getNamesEnabled() {
		
		List<String> projectList = projectService.getNamesEnabled();
		
		Map<String,Object> map = new HashMap<>();
		map.put("projectList", projectList);
		
		return map;
	}
	
	@RequestMapping(value="/projects", method = RequestMethod.POST)
	public @ResponseBody Map<String,Object> getList(@RequestBody Filters filters) {
		if(filters.getPage() == 0) filters.setPage(1);
		if(filters.getNperpage() == 0) filters.setNperpage(15);
		
		ProjectDtoListRes projectsDtosRes = projectService.getFilteredDto(filters);
		
		List<ProjectDto> projectDtos = projectsDtosRes.getProjectDtos();
		int[] resSet = projectsDtosRes.getResSet();
		
		/* if user logged, check all projects and fill the starred and watched flags */
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get(""); 
		
		if(logged != null) {
			for(ProjectDto projectDto : projectDtos) {
				projectDto.setIsStarred(userService.isProjectStarred(projectDto.getId(),logged.getId()));
				projectDto.setIsWatched(userService.isProjectWatched(projectDto.getId(),logged.getId()));
			}
		}
		
		Map<String,Object> map = new HashMap<>();
		
		map.put("projectDtos", projectDtos);
		map.put("resSet", resSet);
		
		return map;
	}
	
	// @Secured("ROLE_USER")
	@RequestMapping(value="/project/{projectId}/star", method = RequestMethod.PUT)
	public @ResponseBody Boolean star(	@PathVariable("projectId") Long projectId, 
										@ModelAttribute("activeProjects") ArrayList<ActiveProject> activeProjects) {
		
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get("");
		if(logged != null) {
			projectService.star(projectId, logged.getId());
			
			/* add to active projects list in session */
			if(activeProjects != null) activeProjects.add(new ActiveProject(projectService.get(projectId).getName(), false));
		}
		return true;
	}
	
	// @Secured("ROLE_USER")
	@RequestMapping(value="/project/{projectId}/unStar", method = RequestMethod.PUT)
	public @ResponseBody Boolean unStar(@PathVariable("projectId") Long projectId,
										@ModelAttribute("activeProjects") ArrayList<ActiveProject> activeProjects) {
		
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get("");
		if(logged != null) {
			projectService.unStar(projectId, logged.getId());
			
			/* remove from active projects list in session */
			if(activeProjects != null) {
				
				Project project = projectService.get(projectId);
				
				Iterator<ActiveProject> prIt = activeProjects.iterator();
				while(prIt.hasNext()){
					ActiveProject thisActiveProject = prIt.next();
					if(thisActiveProject.getProjectName().equals(project.getName())) {
			        	 prIt.remove();
			         }
				}
			} 
				
		}
		return true;
	}
	
	// @Secured("ROLE_USER")
	@RequestMapping(value="/project/{projectId}/watch", method = RequestMethod.PUT)
	public @ResponseBody Boolean watch(@PathVariable("projectId") Long projectId) {
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get("");
		if(logged != null) {
			projectService.watch(projectId, logged.getId());
		}
		return true;
	}
	
	// @Secured("ROLE_USER")
	@RequestMapping(value="/project/{projectId}/unWatch", method = RequestMethod.PUT)
	public @ResponseBody Boolean unWatch(@PathVariable("projectId") Long projectId) {
		// Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		User logged = userService.get("");
		if(logged != null) {
			projectService.unWatch(projectId, logged.getId());
		}
		return true;
	}
	
}
