package com.acme.module.properties;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.properties.ModulePropertyLoader;

public class ModulePropertyTest {

	@Test(expected=InvalidParameterException.class)
	public void nullPropertiesTest() {
		ModulePropertyLoader mpl = new ModulePropertyLoader(null);
		mpl.processProperties();
	}

	@Test
	public void emptyPorpertiesTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		mpl.processProperties();
		
		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check rule module file presence"));
		Assert.assertTrue(mpl.getErrors().contains("No Rule Module key defined, check rule modules property file"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <all.modules> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getModules().isEmpty());
	}

	@Test
	public void onlyModuleKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", ",,   , , ,,module1,,,,, , module2 ,,  ,,,,,");
		mpl.processProperties();
		
		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("groupid property is not defined for module <module1>, therefore this module can't be loaded"));
		Assert.assertTrue(mpl.getErrors().contains("artefactid property is not defined for module <module1>, therefore this module can't be loaded"));
		Assert.assertTrue(mpl.getErrors().contains("versionid property is not defined for module <module1>, therefore this module can't be loaded"));
		Assert.assertTrue(mpl.getErrors().contains("groupid property is not defined for module <module2>, therefore this module can't be loaded"));
		Assert.assertTrue(mpl.getErrors().contains("artefactid property is not defined for module <module2>, therefore this module can't be loaded"));
		Assert.assertTrue(mpl.getErrors().contains("versionid property is not defined for module <module2>, therefore this module can't be loaded"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.groupid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.artefactid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.versionid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module2.groupid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module2.artefactid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module2.versionid> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getModules().isEmpty());
	}
	
	@Test
	public void nobaseKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", "module1");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		mpl.processProperties();
		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check rule property module file, no base defined for module <module1>"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.bases> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getModules().isEmpty());
	}

	@Test
	public void emptybaseKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", "module1");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		props.put("module1.bases", ",,   ,, ,, ");
		mpl.processProperties();
		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check rule property module file, no base defined for module <module1>"));
		Assert.assertTrue(mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getModules().isEmpty());
	}
	
	@Test
	public void noPackageKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", "module1");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		props.put("module1.bases", "toto1, toto2,,");
		mpl.processProperties();

		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check property file, no packages defined for module<module1> and base<toto1>"));
		Assert.assertTrue(mpl.getErrors().contains("Check rule module file, session is undefined for module <module1> and base <toto1>"));
		Assert.assertTrue(mpl.getErrors().contains("Check property file, no packages defined for module<module1> and base<toto2>"));
		Assert.assertTrue(mpl.getErrors().contains("Check rule module file, session is undefined for module <module1> and base <toto2>"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto1.packages> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto1.session> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto2.packages> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto2.session> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getModules().isEmpty());
	}
	
	@Test
	public void emptyPackageKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", "module1, , , , ,,,,,   ,");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		props.put("module1.bases", "toto1, , , ,,,, ");
		props.put("module1.toto1.packages", "");
		mpl.processProperties();

		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check property file, no packages defined for module<module1> and base<toto1>"));
		Assert.assertTrue(mpl.getErrors().contains("Check rule module file, session is undefined for module <module1> and base <toto1>"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto1.session> does not exists in rule module property file"));
		Assert.assertTrue(mpl.getModules().isEmpty());
	}
	
	@Test
	public void noSessionKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", "module1, , , , ,,,,,   ,");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		props.put("module1.bases", "toto1, , , ,,,, ");
		props.put("module1.toto1.packages", "   ,, ,,,,,  babar, ,,pollux,  , ,,zebulon,,,,   ,, , ,,, ");
		mpl.processProperties();

		Assert.assertTrue(!mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getErrors().contains("Check rule module file, session is undefined for module <module1> and base <toto1>"));
		Assert.assertTrue(!mpl.getWarnings().isEmpty());
		Assert.assertTrue(mpl.getWarnings().contains("Property <module1.toto1.session> does not exists in rule module property file"));

		for(Module module:mpl.getModules()){
			if(module.getBases().containsKey("toto1")){
				Base base = module.getBases().get("toto1");
				Assert.assertTrue("babar,pollux,zebulon".equals(base.getPackages()));
			}
			else
				Assert.fail("base toto1 does not exist");
		}		
		Assert.assertTrue(mpl.getModules().isEmpty());
	}

	@Test
	public void sessionKeysTest() {
		Properties props = new Properties();
		ModulePropertyLoader mpl = new ModulePropertyLoader(props);
		props.put("all.modules", ", ,module1, , , , ,,,,,   ,");
		props.put("module1.groupid","com.acme.dede");
		props.put("module1.artefactid","graou");
		props.put("module1.versionid", "1.0.0-SNAPSHOT");
		props.put("module1.bases", "toto1, , , ,,,, ");
		props.put("module1.toto1.packages", "   ,, ,,,,,  babar, pollux, zebulon,,,,   ,, , ,,, ");
		props.put("module1.toto1.session", "gandalf");
		props.put("module1.toto1.gandalf.type","Stateless");
		mpl.processProperties();
		
		List<String> refPacks = new ArrayList<String>();
		refPacks.add("babar");
		refPacks.add("pollux");
		refPacks.add("zebulon");
		for(Module module:mpl.getModules()){
			if(module.getBases().containsKey("toto1")){
				Base base = module.getBases().get("toto1");
				List<String> packs = base.getPackages();
				Assert.assertTrue(packs.containsAll(refPacks));
			}
			else
				Assert.fail("base toto1 does not exist");
		}
		Assert.assertFalse(mpl.getModules().isEmpty());
		Assert.assertTrue(mpl.getErrors().isEmpty());
		Assert.assertTrue(mpl.getWarnings().isEmpty());
	}
}
