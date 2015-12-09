package com.acme.module.builder;

import static org.junit.Assert.*;

import java.io.File;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Properties;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

import com.acme.module.builder.ModuleBuilder;
import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.model.RuleArtefact;
import com.acme.module.model.RuleArtefactType;
import com.acme.module.model.Session;
import com.acme.module.properties.ModulePropertyLoader;

public class ModuleBuilderTest {

	@Test( )
	public void nullModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			mb.generateArchiveAndSaveToFile(null, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("Module object is null, rule archive can not be built"));
		}
	}

	@Test
	public void emptyModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			mb.generateArchiveAndSaveToFile(new Module(null,null,null), new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==IllegalArgumentException.class);
			assertTrue(e.getMessage().equals("groupId, artefactId and versionId cannot be null or empty"));
		}
	}
	
	@Test
	public void noGroupIdModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module(null,"toto","toto");
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==IllegalArgumentException.class);
			assertTrue(e.getMessage().equals("groupId, artefactId and versionId cannot be null or empty"));
		}
	}

	@Test
	public void noArtefactIdModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("toto", null, "toto");
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==IllegalArgumentException.class);
			assertTrue(e.getMessage().equals("groupId, artefactId and versionId cannot be null or empty"));
		}
	}

	@Test
	public void noVersionIdModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("toto","toto",null);
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==IllegalArgumentException.class);
			assertTrue(e.getMessage().equals("groupId, artefactId and versionId cannot be null or empty"));
		}
	}
	
	@Test
	public void noRuleModuleTest() {
		try {
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
			ModuleBuilder mb = new ModuleBuilder();
			for(Module module: mpl.getModules()){
				mb.generateArchiveAndSaveToFile(module, new File("target"));
			}
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("No rule artefacts has been loaded from GIT, rule archive can not be built"));
		}
	}

	@Test
	public void noBaseModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("com.acme.drools","generated","1.0.0-SNAPSHOOT");
			module.getArtefacts().add(new RuleArtefact() {
				private static final long serialVersionUID = 434447611611059955L;
				public RuleArtefactType getType() {return RuleArtefactType.DRL;}
				public String getNameSpace() {return "foo.bar";}
				public String getName() {return "titi";}
				public byte[] getContent() {return ("package foo.bar \n "
						+"\n"
						+ "rule \"titi\" \n"
						+ "when\n"
						+ "then \n"
						+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
						+"end \n").getBytes();}	
				});
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("No Base have been defined for Module <com.acme.drools,generated,1.0.0-SNAPSHOOT>, rule archive can not be built"));
		}
	}

	@Test
	public void noPackageModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("com.acme.drools","generated","1.0.0-SNAPSHOOT");
			module.getArtefacts().add(new RuleArtefact() {
				private static final long serialVersionUID = 434447611611059955L;
				public RuleArtefactType getType() {return RuleArtefactType.DRL;}
				public String getNameSpace() {return "foo.bar";}
				public String getName() {return "titi";}
				public byte[] getContent() {return ("package foo.bar \n "
						+"\n"
						+ "rule \"titi\" \n"
						+ "when\n"
						+ "then \n"
						+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
						+"end \n").getBytes();}	
				});
			module.getBases().put("hello_base", new Base());
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("No packages have been defined for base<hello_base> for Module <com.acme.drools,generated,1.0.0-SNAPSHOOT>, rule archive can not be built"));
		}
	}

	@Test
	public void noSessionModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("com.acme.drools","generated","1.0.0-SNAPSHOOT");
			module.getArtefacts().add(new RuleArtefact() {
				private static final long serialVersionUID = 434447611611059955L;
				public RuleArtefactType getType() {return RuleArtefactType.DRL;}
				public String getNameSpace() {return "foo.bar";}
				public String getName() {return "titi";}
				public byte[] getContent() {return ("package foo.bar \n "
						+"\n"
						+ "rule \"titi\" \n"
						+ "when\n"
						+ "then \n"
						+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
						+"end \n").getBytes();}	
				});
			ArrayList<String> packs = new ArrayList<String>();
			packs.add("foo.bar");
			Base base = new Base();
			base.setPackages(packs);
			module.getBases().put("hello_base", base);
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("No session have been defined for base<hello_base> for Module <com.acme.drools,generated,1.0.0-SNAPSHOOT>, rule archive can not be built"));
		}
	}
	
	@Test
	public void noSessionNameModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("com.acme.drools","generated","1.0.0-SNAPSHOOT");
			module.getArtefacts().add(new RuleArtefact() {
				private static final long serialVersionUID = 434447611611059955L;
				public RuleArtefactType getType() {return RuleArtefactType.DRL;}
				public String getNameSpace() {return "foo.bar";}
				public String getName() {return "titi";}
				public byte[] getContent() {return ("package foo.bar \n "
						+"\n"
						+ "rule \"titi\" \n"
						+ "when\n"
						+ "then \n"
						+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
						+"end \n").getBytes();}	
				});
			ArrayList<String> packs = new ArrayList<String>();
			packs.add("foo.bar");
			Base base = new Base();
			base.setPackages(packs);
			Session session = new Session();
			base.setSession(session);
			module.getBases().put("hello_base", base);
			mb.generateArchiveAndSaveToFile(module, new File("target"));
		} catch (Exception e) {
			assertTrue(e.getClass()==InvalidParameterException.class);
			assertTrue(e.getMessage().equals("No session name have been defined for base<hello_base> for Module <com.acme.drools,generated,1.0.0-SNAPSHOOT>, rule archive can not be built"));
		}
	}
	
	@Test
	public void generateModuleTest() {
		try {
			ModuleBuilder mb = new ModuleBuilder();
			Module module = new Module("com.acme.drools","generated","1.0.0-SNAPSHOOT");
			module.getArtefacts().add(new RuleArtefact() {
				private static final long serialVersionUID = 434447611611059955L;
				public RuleArtefactType getType() {return RuleArtefactType.DRL;}
				public String getNameSpace() {return "foo.bar";}
				public String getName() {return "titi";}
				public byte[] getContent() {return ("package foo.bar \n "
						+"\n"
						+ "rule \"titi\" \n"
						+ "when\n"
						+ "then \n"
						+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
						+"end \n").getBytes();}	
				});
			ArrayList<String> packs = new ArrayList<String>();
			packs.add("foo.bar");
			Base base = new Base();
			base.setPackages(packs);
			Session session = new Session();
			session.setName("hello_session");
			base.setSession(session);
			module.getBases().put("hello_base", base);
			mb.generateArchiveAndSaveToFile(module, new File("target"));
			
			KieServices kieServices = KieServices.Factory.get();
			KieRepository kieRepository = kieServices.getRepository();
			KieModule kieModule2 = kieRepository.addKieModule(kieServices.getResources().newFileSystemResource("target/generated-1.0.0-SNAPSHOOT.jar"));
			KieContainer kieContainer = kieServices.newKieContainer(kieModule2.getReleaseId());
			StatelessKieSession kSession = kieContainer.newStatelessKieSession("hello_session");
			kSession.execute("dede");
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("An exception occured <"+e+"> but was not expected");
		}
	}
}
