package org.collectiveone.web.dto;

public class ActiveProject {
	private String projectName;
	private boolean active;
	
	public ActiveProject() {
		
	}
	
	public ActiveProject(String _projectName, boolean _active) {
		projectName = _projectName;
		active = _active;
	}
	
	public String getProjectName() {
		return projectName;
	}
	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	public boolean getActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
}

