package com.acme.drools.tooling;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import com.acme.module.builder.ModuleBuilder;
import com.acme.module.gitloader.GitRuleLoader;
import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.model.RuleArtefact;
import com.acme.module.properties.ModulePropertyLoader;

/**
 * 
 * 
 * @goal gene
 * @implementation com.acme.drools.tooling.BuildRuleArchiveMojo
 * @language java
 * @phase generate-test-resources
 * @requiresDependencyResolution compile
 * @description Building tool for Drools rule persistence
 * @parameters
 */
@Mojo(name = "generate")
public class BuildRuleArchiveMojo extends AbstractMojo {

	/**
	 * @parameter
	 */
	private String remoteUrl;
	
	/**
	 * @parameter default-value="${project.build.directory}/local_git_repository"
	 */
	private String localGitCopyDir;
	
	/**
	 * @parameter default-value=""
	 */
	private String login;
	
	/**
	 * @parameter default-value=""
	 */
	private String pwd;
	
	/**
	 * @parameter default-value="${project.build.directory}/generated"
	 */
	private String targetDirectory;   
	
	/**
	 * @parameter default-value="${basedir}/modules.properties"
	 */
	private File propertyFile;
	

	public void execute() throws MojoExecutionException, MojoFailureException {
		try {
			
			if(getLog().isInfoEnabled()){
				getLog().info("Starting BuildRuleArchiveMojo");
				getLog().info("remoteUrl  <"+remoteUrl+">");
				getLog().info("localGitCopyDir  <"+localGitCopyDir+">");
				getLog().info("login  <"+login+">");
				getLog().info("pwd  <"+pwd+">");
				getLog().info("targetDirectory  <"+targetDirectory+">");
				getLog().info("propertyFile  <"+propertyFile+">");
			}
			
			// Loading property file
			Properties props = new Properties();
			FileInputStream fis = new FileInputStream(propertyFile);
			props.load(fis);
			
			// parsing property file
			ModulePropertyLoader modulePropertyLoader = new ModulePropertyLoader(props);
			modulePropertyLoader.processProperties();
			
			// checking parsing outcome
			if(!modulePropertyLoader.getErrors().isEmpty()){
				StringBuilder sb = new StringBuilder("modules.properties is not correct \n");
				for(String error : modulePropertyLoader.getErrors()){
					sb.append(error+"\n");
				}
				MojoFailureException mfe = new MojoFailureException(sb.toString());
				throw mfe;
			}
			List<String> packages = new ArrayList<String>();
			Set<Module> modules = modulePropertyLoader.getModules();
			Module module = (Module)(modules.toArray())[0];

			Map<String, Base> bases = module.getBases();
			Set<String> keys = bases.keySet();
			for(String key : keys){
				Base base = bases.get(key);
				packages.addAll(base.getPackages());
			}
			
			// Make a local copy of remote repository
			GitRuleLoader gitLoader = new GitRuleLoader(localGitCopyDir,remoteUrl);
			gitLoader.cloneRemoteRepositoryOnLocal(login, pwd);
			
			// get rules for each package from local copy
			for(String rulePackage:packages){
				List<RuleArtefact> artefacts = gitLoader.fetchRulesArtefactsFromClonedRepository(rulePackage);
				module.getArtefacts().addAll(artefacts);
			}

			// compile rules and generate Archive 
			ModuleBuilder moduleBuilder = new ModuleBuilder();
			moduleBuilder.generateArchiveAndSaveToFile(module, new File(targetDirectory));
			
		} catch (Exception e) {
			e.printStackTrace();
			MojoFailureException mfe = new MojoFailureException("Failed to build archive");
			mfe.initCause(e);
			throw mfe;
		}
	}
}
