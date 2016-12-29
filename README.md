# rule-persist

Drools "rules" persistence on DB as is without KieWorkbench

## Goals
- Avoid drools maven scheme dependencies at runtime
- Avoid KieWorkbench component
- Allow hot deployement without runtime automated update
- Use versioning information for sets of rules
- Avoid changing rule execution API schemes


## Context
A Drools 6.xx Rule project is made of :
- a Pom.property file, containig GAV(groupid,artefactid, versionid ) information.
- a KieModule file :  A  description of components and their configuration that will be used at runtime.
- Rule artefact files : rules, decision tables, rule process flows, etc...
- dependencies to java models referenced in rules(no files)

Common development usage of Drools is to produce an archive containing those files so that this archive can be used at runtime to execute rules contained in it.

Drools uses Maven in its core for building and deployement scheme of rule archives:  
Whenever a new ruleset archive is produced, it may be deployed on the local Maven repository of an Application server, in order to be used and eventually update application behavior.   
Risk of a human error or misusage has been raised, and implies that the use of maven on a runtime server should be avoided and that a more secure and non automated solution unlike maven should be used instead, in order to update rules.  
Although, the usage of drools for developement, deployement, usage and update should remain simple.


## Drools  mechanisms
Drools since version 6.x uses a new set of APIs.  
Maven has been placed in the core of the Rule Engine for its lifecycle(build, runtime, update) 

Maven style archives are :
- produced by the build process.
- used at runtime to load rule sets
- used ar runtime to update rule sets

It is necessary to present core concepts, in order to show how they may be used without maven.

Runtime behaviour of rule execution and acess is described through an XML file : kiemodule.xml.   
In this xml file, some drools components are defined such as :
- KieContainer
- KieBase
- KieSession

## 1 : KieContainer
To model a set of rules, a KieContainer is used.
at runtime and is configured with the help of kiemodule.xml file.

Rules are not declared upfront in the file, they are declared throug groups

A KieContainer contains a set of rules : any kind of rule artifacts, whether there are contained in one or many files.  
A KieContainer supports versioning information : It holds versionning informations that are set like maven style.  
A KieContainer can be updated at runtime with new rules though a special set of drools components.  

A KieContainer can be updated from a JAR file, and the content of this JAR file must follow some contraints :   
- a kmodule.xml, describing the grouping of rules into packages, the access of rule groups through knowledge bases and sessions.
- Rules artifacts are contained into this file.

## 2 : KieBase
The scope of a KieBase is declared within a KieContainer scope and multiple KieBases can be defined in a single KieContainer scope.   
A kieBase defines one or more groups of rule artefacts using packages property. This is the only way to define rule selection for a KieBase using package names because rule artifact have to define a package name they belong to.  

A KieBase may reference one or more Kiebase as well, thus inheritating its/their rules grouping.   
In order to distinguish a KieBase from another one, it is necessary to provide a unique name to a KieBase.  

## 3 : KieSessions
A KieSession is defined within the scope of a KieBase. Generally only one session is defined.  
Mutiple properties may be defined within the KieSession scope such as:
- a name  : unique within the KieBase scope and preferably among the KieContainer scope.
- Session type : Stateless or Statefull
- ...

## 4 : Rule Artifacts
Rule artifacts may be of many kinds :
- Ruleflow process file : format = xml.
- Decision table : format = xls, csv
- Technical Rule : drl.

These file have always a package information. Rules are grouped logically per package information, that are used in KieBase definitions.   

Other file may be used but are primarely used on KieWorkbench(web interface : BRMS)




## Steps that needs to be performed in any case

### 1. Describing archives :
Archives need to be described in order to know how to build versionned rule archives 
- Description is performed by kieModule.xml file: KieContainer, KieBase, KieSession.   
-- packages and rule artefacts referencing these packages   
- Versionning information is done with the help of pom.properties   

### 2. Building archives : 
Rule archives are usually built with maven, but they can also be built using Java Drools API.
The result is a JAR file containing a kiemdule.xml


### 3. Deploying archives : 
Rule archives must be in the classpath so that they can be found. 
When rules are deployed with the help of Maven on a local Maven repository, they can be used and found, by underlying maven framework layer used by Drools.

### 4. Using archives : running rules
At startup time, Drools looks up for JAR files containing a kiemodule.xml file, parses them.
Versionned Rule archives must be available to Runtime platform so that rule engine may use them.
A special mechanism may be used with the help of Maven driven by drools to enable hot deployment: 
- Whenever a new archive is deployed on the local maven repository   
- maven-metadata-local.xml is updated with new deployed version   
** A kieScanner is used to monitor modifications of this file on a frequency based period , so that it updates the KieContainer with newly deployed version according to maven updates set properties and the period.   
** Its is possible to use a JMX console to update any instantiated KieContainer, with the latest Rule Archive deployed with maven. But this action needs to be performed on every KieContainer...

# Solution

Drools customization needs to be performed :   
- Building : In order to build archives, a different mechanism has to be used so that maven is not in the way
- Building will not be performed using maven plugins, but with Java code.
- Drools Java API will provide this buidling, but kiemodule.xml and pom.properties have to be generated.
- Rules have to be discovered where they are hosted(GIT).
- Rule Archives will be stored on DB instead of local maven repo.
- JMX will be used to perform any updates : security based actions, on demand.
- JMX will allow to read DB and update in memory Rule archive repository
- JMX will allow to udpate KieContainer(s) with new Rule archives
- Rule services will need to register themselves to JMX service manager so that they can be updated when needed.

## Property based description of KieContainer : a Module.
In order to describe rule projects, a property file based will provide a simple way to perform this task :   
modules.properties   
A special parsing is done to allow comma separated values properties and dynamic naming.

This file describes all modules that are to be built, their GAV information, and their bases and sessions.

Following is a sample of a modules.properties file,  describing a single module with a single base and a single session.
```Ini
# list of modules to generate
all.modules = module1

# module generated jar version
module1.versionid=1.0.0-SNAPSHOT
module1.groupid=com.acme.drools.tools
module1.artefactid=myartefact

# module1 description
module1.bases=hello_base 
module1.hello_base.packages=hello_rules 
module1.hello_base.session=hello_session 
module1.hello_base.hello_session.type=Stateless 

```

Java class : ModulePropertyLoader allows to parse this propety file in order to generate a composition of modules , bases and sessions.

The result of modules correctly parsed are available as well as errors during parsing process.

## Git artefact loading :    
Now that modules are described , it is possible to get Rules artefact files from SCM : i.e. GIT   
For that purpose, a GitRuleLoader Java class has been done for that purpose.

This class is able to retreive a remote repository GIT file structure as a Bare repository, make a local repository copy and extract rules associated with package, obtained through navigation withing module.base hierarchy.

## Archive building :    
As rule artefacts are available, it is time to build rule archive.    
One requirement is to have runtime model classes used in rule available so that rule compiling is possible.

MduleBuilder java class is ment for that purpose and allows to generate all required files : kieModule.xml, pom properties, and java artefacts in a JAR file, into a specific folder.

## Archive deployment :    
 Deployment is achieved through a set of java classes, allowing persistance using a persistence service, a DAO pattern and a persistence model.
 

## Runtime rule availability :
 Application has the responsability to load rules, that is to say instaciate KieContainer with proper GAV information.   
 Later on it can use those KieContainer and create named KieSessions.
 Although KieBase could be used it is not necessary to instantiate Object from this class.
 
 In order to load rules, a JMX Component is used and registered for the application VM : JmxModuleManager
 Through this MXBean, the application can retreive JARed modules and load them in Drools repository so that KieContainer.
 Upon request, the JmxModuleManager will download from database, all new JARed module newly deployed.
 
 A rule service using a KieContainer has to register itself to another MXBean with JMX : ServiceManager   
 This class upon request through JMX will get all loaded modules and update all registered RuleServices KieContext with latest JARed module related to this servcice.
 
 To Sum up : 
 To update an application with new Rules : 
 - deploy a new Archive to DB
 - Through JMX, reload all DB persisted modules.
 - Through JMX, update all Ruleservices.
 
 All thoses steps can be done via API, or even Oracle JConsole as JMX management tool.


# Implementation

Overall there is 4 projects:
- One model Project : ModuleModel
- One rule archive building project : ModuleArchiver
- One Maven plugin building project  : ModuleArchiverMavenPlugin
- One Runtime project : ModuleRuntime


## ModuleModel
This project holds the model used almmost in every project.

## ModuleArchiver
This project is responsible for 
- Parsing a property file with module, bases, packages and session descriptions
- Cloning remotes GIT respositories, and fecthing on cloned repository rulefiles
- Compiling rules modules and generating rule archives

## ModuleArchiverMavenPlugin
This project is responsible for regrouping ModuleArchiver project under a unique humbrella in order to automate with Maven the building of Rule Archives

Please note that when building rule archive, dependent model used in rules must be available at rule build time. If not, rule compilation will not succeed.

As a side effect, from Maven lookup mechanism deeply embeded in rule compliation, some stack trace will be generated in maven build screen though build result is SUCCESS.

Lasty, running rule tests must be performed after this generated rule archive have been performed so that rule are available.


### Building with maven example
```Ini
	<build>
		<plugins>
			<plugin>
				<groupId>com.acme.drools.tooling</groupId>
				<artifactId>persist-rules-maven-plugin</artifactId>
				<version>1.0.0</version>
				<configuration>
					<remoteUrl>https://github.com/fr9ncisco/rule-persist</remoteUrl>
					<targetDirectory>${project.build.directory}</targetDirectory>
					<propertyFile>${basedir}/modules.properties</propertyFile>
				</configuration>
			</plugin>
		</plugins>
	</build>
```


Properties : 
- ``` <remoteUrl>[GIT repository projects where rules are located]</remoteUrl> ```
- ``` <targetDirectory>[Directory where built rule archive will be generated]</targetDirectory> ```
- ``` <propertyFile>[local build folder path to look for modules description property file]/modules.properties</propertyFile> ```


This can be invoked using <b>mvn persist-rules:gene</b>

## ModuleRuntime
This project is responsible for defining the use of rule archives :
How to store on DB a rule Archive
How to retreive a rule archive
How to deploy a rule archive

As well it offers services to enable the use of Rule archives at runtime for any ruleservice that would be implemented though the use of JMX, in order to protect deployment(hot deployement) and decorelates deployment from building.

 
 
 









