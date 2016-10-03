package coproject.cpweb.actions.json;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.convention.annotation.Results;

/**
 * @author pepo
 *
 */
@Action("GoalGetSuggestions")
@ParentPackage("json-data")
@Results({
    @Result(name="success", type="json", params={"ignoreHierarchy","false","includeProperties","^suggestions.*,^fieldErrors.*"}),
    @Result(name="input", type="json", params={"ignoreHierarchy","false","includeProperties","^fieldErrors.*"})
})
public class GoalGetSuggestions extends CpAction {
	
	private static final long serialVersionUID = 1L;
	
	/* Input parameters  */
	private String query;
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	
	private String projectName;
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	/* Output parameters  */
	private List<String> suggestions = new ArrayList<String>();
	public List<String> getSuggestions() {
		return suggestions;
	}
	public void setSuggestions(List<String> suggestions) {
		this.suggestions = suggestions;
	}

	/* Execute */
	public String execute() throws Exception  {
    	
		List<String> projectNames = new ArrayList<String>();
		if(projectName != null) {
			if(!projectName.equals("")) {
				projectNames.add(projectName);
			}
		}
		
		suggestions = dbServices.goalGetSuggestions(query, projectNames);
		
		return SUCCESS;
    }
	

}
