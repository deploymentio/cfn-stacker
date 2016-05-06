package com.deploymentio.cfnstacker;

import java.io.File;

import com.deploymentio.cfnstacker.config.StackConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Stacker {
	
	public static void main(String[] args) {

		Stacker stacker = new Stacker();
		try {
			stacker.run(args[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run(String stackConfigFile) throws Exception {
		
		ObjectMapper mapper = new ObjectMapper();
		StackConfig stackConfig = mapper.readValue(new File(stackConfigFile), StackConfig.class);
		
		
	}
	
}