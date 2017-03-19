
package org.collectiveone.test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.collectiveone.model.Decision;
import org.collectiveone.services.DecisionServiceImp;
import org.collectiveone.services.PeriodicMethods;
import org.collectiveone.services.ProjectServiceImp;
import org.collectiveone.services.UserAuthServiceImp;
import org.collectiveone.web.controllers.BaseController;
import org.collectiveone.web.controllers.rest.DecisionsController;
import org.collectiveone.web.controllers.views.ViewsController;
import org.collectiveone.web.dto.DecisionDtoCreate;
import org.collectiveone.web.dto.ProjectNewDto;
import org.collectiveone.web.dto.ThesisDto;
import org.collectiveone.web.dto.UserNewDto;
import org.collectiveone.web.dto.UsernameAndPps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class CollectiveOneWebappApplicationTests {
	
	@Autowired
	BaseController baseController;
	
	@Autowired
	ViewsController viewsController;
	
	@Autowired
	DecisionsController decisionController;
	
	
	@Autowired
	UserAuthServiceImp userAuthService;
	
	@Autowired
	ProjectServiceImp projectService; 
	
	@Autowired
	DecisionServiceImp decisionService;
	
	@Autowired
	PeriodicMethods periodicMethods;
	
	
	private String projectName = "test-project";
	private String goalTag = "test-goal";
	
	
	@Before
	public void setup() throws IOException {
		/* Mockups */
		BindingResult result = mock(BindingResult.class);
	    when(result.hasErrors()).thenReturn(false);
	    
	    Model model = mock(Model.class);
	    HttpServletRequest request = mock(HttpServletRequest.class);
		
		/* Create platform user, used by the application as creator automatically created objects */
		UserNewDto platformUser = new UserNewDto();
		platformUser.setEmail("contact@collectiveone.org");
		platformUser.setPassword("12345678");
		platformUser.setPasswordConfirm("12345678");
		platformUser.setUsername("collectiveone");

		userAuthService.emailAuthorize(platformUser.getEmail());
		userAuthService.registerNewUserAccount(platformUser);
		
		/* create dummy objects */
		Locale locale = new Locale("EN-US");
		
		/* Create users */
		int nusers = 10;
		for(int ix=0; ix < nusers; ix++) {
			UserNewDto userNewDto = new UserNewDto();
	
			userNewDto.setPassword("userpassword"+ix);
			userNewDto.setPasswordConfirm("userpassword"+ix);
			userNewDto.setUsername("user"+ix);
			userNewDto.setEmail("temp"+ix+"@gmail.com");
	
			userAuthService.emailAuthorize(userNewDto.getEmail());
			baseController.signupSubmit(locale, userNewDto, result, model, request);
		}
		
		
		/* Create project */
		/* Login one user*/
		UserDetails userDetails = userAuthService.loadUserByUsername("user0");
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername (),userDetails.getPassword (),userDetails.getAuthorities ());
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		ProjectNewDto projectDto = new ProjectNewDto();
		
		projectDto.setName(projectName);
		projectDto.setCreatorUsername("user0");
		projectDto.setDescription("Dummy project created for test");
		projectDto.setGoalTag(goalTag);
		projectDto.setGoalDescription("Dummy goal created at project creation");
		
		List<UsernameAndPps> contributorNamesAndPps = new ArrayList<UsernameAndPps>();
		
		for(int ix=0; ix < nusers; ix++) {
			contributorNamesAndPps.add(new UsernameAndPps("user"+ix,100));
		}
		
		projectDto.setUsernamesAndPps(contributorNamesAndPps);
		
		projectService.authorize(projectDto.getName());
	    viewsController.projectNewSubmit(projectDto, result);
	}
	
	@Test
	public void testDecisionAlgorithm() throws IOException {
		
		/* Mockups */
		BindingResult result = mock(BindingResult.class);
	    when(result.hasErrors()).thenReturn(false);
	    Model model = mock(Model.class);
	    
		/* Create a new decision */
	    DecisionDtoCreate decisionDto = new DecisionDtoCreate();
	    decisionDto.setDescription("Test decision");
	    decisionDto.setGoalTag(goalTag);
	    decisionDto.setProjectName(projectName);
	    decisionDto.setVerdictHours(100);
	    
	    String returned = viewsController.decisionNewSubmit(decisionDto, result, model);
	    String[] parts = returned.split("/");
	    Long decisionId = Long.parseLong(parts[3]);
	    
	    /* Login one user*/
	    UserDetails userDetails = userAuthService.loadUserByUsername("user0");
		Authentication auth = new UsernamePasswordAuthenticationToken(userDetails.getUsername (),userDetails.getPassword (),userDetails.getAuthorities ());
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		ThesisDto thesisDto = new ThesisDto();
		thesisDto.setDecisionId(decisionId);
		thesisDto.setValue(1);
		decisionController.vote(thesisDto);
		
		
		/* Update state */
		periodicMethods.updateState();
		
		/* show state */
		Decision decision = decisionService.get(decisionId);
		
		System.out.println(decision.n);
		System.out.println(decision.pest);
		System.out.println(decision.weightTot);
		System.out.println(decision.weightCum);
		System.out.println(decision.elapsedFactor);
		System.out.println(decision.shrinkFactor);
		System.out.println(decision.pc_low);
		System.out.println(decision.pc_high);
		
		
	}
}


