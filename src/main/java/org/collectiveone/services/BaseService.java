package org.collectiveone.services;

import org.collectiveone.repositories.ActivityRepository;
import org.collectiveone.repositories.ArgumentRepository;
import org.collectiveone.repositories.AuthorizedEmailRepository;
import org.collectiveone.repositories.AuthorizedProjectRepository;
import org.collectiveone.repositories.BidRepository;
import org.collectiveone.repositories.CbtionRepository;
import org.collectiveone.repositories.CommentRepository;
import org.collectiveone.repositories.ContributorRepository;
import org.collectiveone.repositories.DecisionRealmDao;
import org.collectiveone.repositories.DecisionRepository;
import org.collectiveone.repositories.GoalRepository;
import org.collectiveone.repositories.MailSubscriptionRepository;
import org.collectiveone.repositories.ProjectRepository;
import org.collectiveone.repositories.PromoterRepository;
import org.collectiveone.repositories.ReviewRepository;
import org.collectiveone.repositories.ThesisRepository;
import org.collectiveone.repositories.UserRepository;
import org.collectiveone.repositories.VoterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.env.Environment;

public class BaseService {
	
	/* REPOSITORIES */
	
	@Autowired
	protected GoalRepository goalDao;

	@Autowired
	protected ProjectRepository projectDao;
	
	@Autowired
	protected ThesisRepository thesisDao;

	@Autowired 
	protected VoterRepository voterDao;
	
	@Autowired 
	protected DecisionRealmDao decisionRealmDao;
	
	@Autowired
	protected ActivityRepository activityDao;
	
	@Autowired 
	protected ArgumentRepository argumentDao;
	
	@Autowired
	protected UserRepository userDao;
	
	@Autowired
	protected DecisionRepository decisionDao;
	
	@Autowired
	protected CbtionRepository cbtionDao;
	
	@Autowired
	protected BidRepository bidDao;
	
	
	
	
	@Autowired
	protected MailSubscriptionRepository mailSubscriptionRepository;
	
	@Autowired
	protected AuthorizedEmailRepository authorizedEmailRepository;
	
	@Autowired 
	protected PromoterRepository promoterDao;
	
	@Autowired 
	protected CommentRepository commentDao;
	
	@Autowired 
	protected ContributorRepository contributorDao;
	
	@Autowired
	protected AuthorizedProjectRepository authorizedProjectDao;
	
	@Autowired 
	protected ReviewRepository reviewDao;
	
	
	/* SERVICES */
	
	@Autowired
	protected ActivityServiceImp activityService;
		
	@Autowired
	protected AppMailServiceHeroku mailService;
	
	@Autowired
	protected SlackServiceImp slackService;
	
	@Autowired
	protected ContributorServiceImp contributorService;
	
	@Autowired
	protected ProjectServiceImp projectService;
	
	@Autowired
	protected VoterServiceImp voterService;
	
	@Autowired
	protected GoalServiceImp goalService;
	
	@Autowired
	protected BidServiceImp bidService;
	
	@Autowired
	protected UserServiceImp userService;
	
	@Autowired
	protected DecisionRealmServiceImp decisionRealmService;
	
	@Autowired
	protected TimeServiceImp timeService;
	
	
	
	
	/* SYSTEM */
	
	@Autowired
	protected Environment env;
	
	@Autowired
	protected ApplicationEventPublisher eventPublisher;
	

}
