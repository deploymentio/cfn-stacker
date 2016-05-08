package com.deploymentio.cfnstacker;

import com.amazonaws.services.cloudformation.model.Stack;

public enum Status {

	MODIFIABLE,
	CREATABLE,
	BUSY;
	
	public static Status valueOf(Stack stack) {
		if (stack == null) {
			return Status.CREATABLE;
		}
		return stack.getStackStatus().endsWith("_IN_PROGRESS") ? Status.BUSY : Status.MODIFIABLE;
	}
}