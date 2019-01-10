package org.collectiveone.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.reflect.Type;
import java.util.UUID;

import org.collectiveone.AbstractTest;
import org.collectiveone.common.dto.GetResult;
import org.collectiveone.common.dto.PostResult;
import org.collectiveone.modules.contexts.dto.ContextMetadataDto;
import org.collectiveone.modules.contexts.dto.PerspectiveDto;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.auth0.client.auth.AuthAPI;
import com.auth0.exception.APIException;
import com.auth0.exception.Auth0Exception;
import com.auth0.json.auth.TokenHolder;
import com.auth0.net.AuthRequest;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;


@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class TestContextController extends AbstractTest {
	
	@Autowired
    private MockMvc mockMvc;
	
	@Value("${AUTH0_ISSUER}")
	private String auth0Domain;
	
	@Value("${AUTH0_AUDIENCE}")
	private String clientId;
	
	@Value("${AUTH0_SECRET}")
	private String clientSecret;
	
	@Value("${TEST_USER_EMAIL}")
	private String testEmail;
	
	@Value("${TEST_USER_PWD}")
	private String testPwd;
	
	private String authorizationToken;
    
	@Before
    public void setUp() throws Exception {
		
		AuthAPI auth = new AuthAPI(auth0Domain, clientId, clientSecret);
		
		AuthRequest request = auth.login(testEmail, testPwd)
		    .setScope("openid contacts");
		try {
		    TokenHolder holder = request.execute();
		    authorizationToken = holder.getIdToken();
		} catch (APIException exception) {
			System.out.println(exception);
		} catch (Auth0Exception exception) {
			System.out.println(exception);
		}
		
		MvcResult result = this.mockMvc
	    	.perform(get("/1/user/myProfile")
	        .header("Authorization", "Bearer " + authorizationToken))	    	
	    	.andReturn();
		
		assertEquals("error in http request: " + result.getResponse().getErrorMessage(),
        		200, result.getResponse().getStatus());
		
		System.out.println("Test user created:");
		System.out.println(result.getResponse().getContentAsString());
    }

    @After
    public void tearDown() {
        // clean up after each test method
    }
    
    @Test
    public void createContext() throws Exception {
    	
    	String title = "My Title";
    	String description = "My Description";
    	
    	ContextMetadataDto contextDto = new ContextMetadataDto(title, description);
    	
    	Gson gson = new Gson();
        String json = gson.toJson(contextDto);
        MvcResult result = null;
        
        result = this.mockMvc
	    	.perform(post("/1/ctx")
	    	.header("Authorization", "Bearer " + authorizationToken)
	    	.contentType(MediaType.APPLICATION_JSON)
	    	.content(json))
	    	.andReturn();
        
        assertEquals("error in http request: " + result.getResponse().getErrorMessage(),
        		200, result.getResponse().getStatus());
        
        PostResult postResult = gson.fromJson(result.getResponse().getContentAsString(), PostResult.class); 
        
        assertEquals("error creating context: " + postResult.getMessage(),
        		"success", postResult.getResult());
        
        String contextId = postResult.getElementId();
        
        assertNotNull("unexpected id",  UUID.fromString(contextId));
        
        result = this.mockMvc
    	    	.perform(get("/1/ctx/" + contextId)
    	    	.header("Authorization", "Bearer " + authorizationToken))
    	    	.andReturn();
        
        assertEquals("error in http request: " + result.getResponse().getErrorMessage(),
        		200, result.getResponse().getStatus());
        
        @SuppressWarnings("serial")
		Type resultType = new TypeToken<GetResult<PerspectiveDto>>(){}.getType();
        
        GetResult<PerspectiveDto> getResult = gson.fromJson(result.getResponse().getContentAsString(), resultType);
        
        assertEquals("error getting context: " + getResult.getMessage(),
        		"success", getResult.getResult());
        
        PerspectiveDto perspectiveDto = getResult.getData();
        
        assertEquals("unexpected title",
        		title, perspectiveDto.getMetadata().getTitle());
        
        assertEquals("unexpected description",
       		 	description, perspectiveDto.getMetadata().getDescription());
        
    }

    
}
