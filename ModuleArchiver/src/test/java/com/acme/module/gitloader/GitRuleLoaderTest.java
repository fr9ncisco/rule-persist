package com.acme.module.gitloader;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.acme.module.gitloader.GitRuleLoader;
import com.acme.module.model.RuleArtefact;

public class GitRuleLoaderTest {

	public static final String OK_REMOTE_REPO = "https://github.com/fr9ncisco/rule-persist/";
	public static final String NOT_OK_REMOTE_REPO = "https://magithub.com/fr9ncisco/rule-persist/";
	public static final String OK_LOCAL_REPO = "/Users/Francois/Desktop/temp";
	public static final String NOT_OK_LOCAL_REPO = "/Users/titi/Desktop/temp";
	public final String PACKAGE_1 = "rules.mytest";
	public final String PACKAGE_2 = "rules.mytest.subpack";
    public final String LOGIN ="fr9ncisco";
	public final String PASSWORD ="Alpha!g45B";

	@Test
	public void checkTwoPackageRuleLoadingTest() {
		GitRuleLoader grl = new GitRuleLoader(OK_LOCAL_REPO, OK_REMOTE_REPO);
		try {
			grl.cloneRemoteRepositoryOnLocal(LOGIN, PASSWORD);
		} catch (Exception e) {
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
		}

		List<RuleArtefact> artefacts  = grl.fetchRulesArtefactsFromClonedRepository(PACKAGE_1);
		assertTrue(!artefacts.isEmpty());
		for(RuleArtefact artefact:artefacts){
			assertTrue(artefact.getNameSpace().equals(PACKAGE_1));
		}

		artefacts  = grl.fetchRulesArtefactsFromClonedRepository(PACKAGE_2);
		assertTrue(!artefacts.isEmpty());
		for(RuleArtefact artefact:artefacts){
			assertTrue(artefact.getNameSpace().equals(PACKAGE_2));
		}
		grl.close();
	}
	
	@Test
	public void checkNokLocalRepositoryTest() {
		GitRuleLoader grl = new GitRuleLoader(NOT_OK_LOCAL_REPO, OK_REMOTE_REPO);
		try{
			grl.cloneRemoteRepositoryOnLocal(LOGIN, PASSWORD);
		}catch(Exception e){
			assertTrue(e.getMessage().equals("unnable to clone<" + OK_REMOTE_REPO + "> in <" + NOT_OK_LOCAL_REPO + ">; nested exception is Creating directories for " + NOT_OK_LOCAL_REPO + " failed"));
		}
		grl.close();
	}
	
	@Test
	public void checkNokRemoteRepositoryTest() {
		GitRuleLoader grl = new GitRuleLoader(OK_LOCAL_REPO, NOT_OK_REMOTE_REPO);
		try{
			grl.cloneRemoteRepositoryOnLocal(LOGIN,PASSWORD);
		}catch (Exception e) {
			assertTrue(e.getMessage().equals("unnable to clone<"+NOT_OK_REMOTE_REPO+"> in <"+OK_LOCAL_REPO+">; nested exception is "+NOT_OK_REMOTE_REPO+": cannot open git-upload-pack"));		
		}
		grl.close();
	}
}
