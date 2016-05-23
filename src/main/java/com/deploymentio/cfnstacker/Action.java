/*
 * Copyright 2016 - Deployment IO
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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



