## CFN Stacker

This is a command-line tool to maintain [AWS CloudFormation](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html) (CFN) stacks. CFN stacks are created based on CFN templates, which are very verbose JSON documents executed by the CFN service in response to CFN api calls. This tool lets you organize your CFN template into multiple JSON fragments and use the [Velocity template language](http://velocity.apache.org/engine/devel/vtl-reference-guide.html) (VTL) to generate a single template before it is applied using the CFN api.

Major advantages of using **cfn-stacker** to maintain your stacks instead of using AWS console or the aws-cli are:
- Template can be broken up across multiple fragment files. This increases maintainability and readability.
- VTL can be used to script the generation of template fragments. This reduces verbosity and code duplication.
- Templates are uploaded to S3 before CFN api is called. This means that the templates can be much larger.
- The template, its parameters, and all the configuration about the CFN stack are contained in a configuration file. The means that the configuration itself can be maintained as code in version control. This also means, that one can re-use template fragments and run them with a slightly different configuration file to create a test and production stack, for example. 
 
### Requirements
- Active AWS account
- JDK 1.7+
- Apache Maven 3+

### Building
First clone or download the [project from GitHub](https://github.com/deploymentio/cfn-stacker) and then run:

```bash
    cd cfn-stacker
    mvn clean package
```
This will create a self-executing jar file in the `target` sub-directory named `cfn-stacker.jar`. 

### Running ###

- Make sure your AWS credentials are properly setup in the system according to [these instructions](http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/com/amazonaws/auth/DefaultAWSCredentialsProviderChain.html).
- In the `target` directory where the jar file was generated, run `java -jar cfn-stacker.jar` - which will produce the following:

```
usage: java -jar cfn-stacker.jar -c <file> -a <CREATE|CREATE_DRY_RUN|UPDATE|UPDATE_DRY_RUN|DELETE>

CFN Stacker is used to create/update/delete an AWS CloudFormation stack
   -a,--action <name>   Action to take
   -c,--config <file>   Stack configuration file
   -d,--debug           Print debug messages
   -t,--trace           Print trace messages
```

The two main things needed are the configuration file and the action to take. The actions are self-explanatory. The configuration file itself is a JSON document that describes the following about a stack:
- Stack name
- Any tags that can be assigned to the stack and all its resources
- SNS topic that receives events as the stack is being created, updated, or deleted
- S3 bucket and prefix where the assembled templates are uploaded
- Parameter values passed in when creating/updating a stack to the CFN api. These parameters can also be used during the creation of the template itself using VTL expressions in the template fragments.
- List of template fragments and their own parameters. Each template fragment has access to all the stack parameters as well as its own parameters.
- List of sub-stacks, their names, and a list of template fragments and parameters for each sub-stack.

### Sample stack configurations/templates

It is easier to explain the functionality by showing examples of what **cfn-stacker** can do:
1. Example of a stack with simple parameters and single JSON fagment ... _[comming soon]_
1. Example of a stack with more complex parameters and mulitple JSON fragments ... _[comming soon]_
1. Example of a stack with multiple sub-stacks ... _[comming soon]_
