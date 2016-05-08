package com.deploymentio.cfnstacker;

public enum Action {
	
	CREATE(Status.CREATABLE),
	CREATE_DRY_RUN(Status.CREATABLE),
	UPDATE(Status.MODIFIABLE),
	UPDATE_DRY_RUN(Status.MODIFIABLE),
	DELETE(Status.MODIFIABLE);
	
	private Status[] allowed;
	private Action(Status... allowed) {
		this.allowed = allowed;
	}
	
	public boolean isAllowed(Status currentStackStatus) {
		for(Status status: allowed) {
			if (status == currentStackStatus) {
				return true;
			}
		}
		return false;
	}
}



