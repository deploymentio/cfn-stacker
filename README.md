## CFN Stacker

This is a command-line tool to maintain [AWS CloudFormation](http://docs.aws.amazon.com/AWSCloudFormation/latest/UserGuide/Welcome.html) (CFN) stacks. CFN stacks are created based on CFN templates, which are very verbose JSON documents executed by the CFN service in response to CFN api calls. This tool lets you organize your CFN template into multiple JSON fragments and use the [Velocity template language](http://velocity.apache.org/engine/devel/vtl-reference-guide.html) (VTL) to generate a single template before it is applied using the CFN api.

Major advantages of using **cfn-stacker** to maintain your stacks instead of using AWS console or the aws-cli are:
- Template can be broken up across multiple fragment files. This increases maintainability and readability.
- VTL can be used to script the generation of template fragments. This reduces verbosity and code duplication.
- Templates are uploaded to S3 before CFN api is called. This means that the templates can be much larger.
- The template, its parameters, and all the configuration about the CFN stack are contained in a configuration file. The means that the configuration itself can be maintained as code in version control. This also means, that one can re-use template fragments and run them with a slightly different configuration file to create a test and production stack, for example. 

*Update*: CloudFormation has improved a lot and some of these capabilities might be not needed anymore ... thats how AWS rolls :-)
 
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

### Anatomy of a stack configuration

```json
{
	"name": "stackname",
	"s3Bucket": "my-s3-bucket",
	"s3Prefix": "some-prefix/",
	"snsTopic": "arn:aws:sns:us-east-1:1234567778901:cfn-events-topic",
	"tags": {
		"a-tag": "foo",
		"b-tag": "bar"
	},
	"parameters": {
		"environment": "test",
		"officeIps": {
			"office1": "10.0.1.0/24",
			"office2": "10.0.2.0/24"
		},
		"stooges": [
			"larry",
			"moe",
			"curly"
		],
		"aFlag": false
	},
	"fragments": [
		{
			"path": "security-groups.json"
		},
		{
			"path": "web-server.json",
			"parameters": {
				"anotherFlag": true
			}
		}
	],
	"subStacks": [
		{
			"name": "lambda",
			"fragments": [
				{
					"path": "lambda/roles.json",
					"parameters": {
						"anotherFlag": true
					}
				},
				{
					"path": "lambda/functions.json"
				}
			]
		}
	]
}
```

- The `name` parameter determines the name of the stack, it is a required parameter.
- The `s3Bucket` and `s3Prefix` parameters are also required. These parameters are used to upload the evaluated CFN templates before the CloudFormation api is invoked. The advantage of first uploading the template to S3 is that much larger templates are supported this way compared to sending in the template contents as part of the CloudFormation api calls.
- The `tags` are an optional key/value pair map. The stack created will contain these tags. These tags are then propagated to all resources created within the stack through CloudFormation. This propagation of tags is a CloudFormation feature - nothing special that cfn-stacker is doing.
- The `snsTopic` is an optional parameter which takes an ARN of an SNS topic. This topic will be sent events as stack resources are being created, updated, or deleted. Unless you want to take some automated action based on these events, there is no need to use this parameter.
- The `parameters` attribute is a map of arbitrary key/values that will be used with your template fragments. It can be omitted if you don't have any such parameters - but I suspect, you won't be using cfn-stacker if you didn't need this feature. In addition to being available in your VTL expressions, these parameters will also provide values to any parameters defined in the `Parameters` section of your template. This only works for the top-level stack.
- The `fragments` parameter is a required list of fragment objects where each fragment consists of a path to a template json file and optional parameters overrides for the template fragment. All the fragments are first evaluated using VTL rules and then merged into one big JSON template before CloudFormation api is used to create/update/delete the stack.
- The `subStack` parameter is an optional list of sub-stack objects. Each sub-stack object has its own name and a list of `fragments` just like the top level stack does. For each sub-stack defined in this way, cfn-stacker will evaluate all fragments of the sub-stack, merge them into one JSON template, and upload them to S3. It is then your responsibility to refer to that template in your top-level stack using a resource with type of `AWS::CloudFormation::Stack`. For more details on using sub-stacks, see the sub-stack example below. 

### Sample stack configurations/templates

It is easier to explain the functionality by showing examples of what **cfn-stacker** can do:

1. Example of a [stack with simple parameters and single JSON fagment](example1/)
1. Example of a stack with multiple JSON fragments ... _[comming soon]_
1. Example of a stack with a sub-stack ... _[comming soon]_

