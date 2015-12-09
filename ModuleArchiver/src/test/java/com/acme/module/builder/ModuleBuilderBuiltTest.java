package com.acme.module.builder;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.drools.core.BeliefSystemType;
import org.drools.core.ClockType;
import org.drools.core.TimerJobFactoryType;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.KieRepository;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.api.runtime.conf.BeliefSystemTypeOption;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.api.runtime.conf.QueryListenerOption;
import org.kie.api.runtime.conf.TimedRuleExectionOption;
import org.kie.api.runtime.conf.TimerJobFactoryOption;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.runtime.conf.ForceEagerActivationOption;

import com.acme.module.builder.ModuleBuilder;
import com.acme.module.model.Base;
import com.acme.module.model.Module;
import com.acme.module.model.RuleArtefact;
import com.acme.module.model.RuleArtefactType;
import com.acme.module.model.Session;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ModuleBuilderBuiltTest {

	// GENERATE JAR MAVEN STYLE IN TARGET DIRECTORY
	private static final String ARTEFACT_ID = "generated";
	private static final String GROUP_ID = "com.acme.drools";
	private static final String VERSION_ID = "1.0.0-SNAPSHOOT";
	private static final String TARGET_DIRECTORY = "target";
	private static final String GENERATED_JAR_LOOK_UP_PATH_NAME = TARGET_DIRECTORY +"/"+ARTEFACT_ID+"-"+VERSION_ID+".jar";
	
	// RULE INFORMATION
	private static final String NAMESPACE = "foo.bar";
	private static final String RULENAME = "titi";
	private static final String RULE_SESSION_NAME = "helloo_session";
	private static final String RULE_BASE_NAME ="helloo_base";
	
	
	/**
	 * Generate a rule package using <br>
	 * - a module description property file<br>
	 * - a  simulated loaded rule<br> 
	 * - a moduleBuilder<br>
	 * 
	 * Once generated, this module will serve to check that regular Drools API<br> 
	 * - can load the module<br>
	 * - can execute the module with appropriate API calls(named sessions)<br>
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		ModuleBuilder mb = new ModuleBuilder();
		Module module = new Module(GROUP_ID,ARTEFACT_ID,VERSION_ID);
		module.getArtefacts().add(new RuleArtefact() {
			private static final long serialVersionUID = 5563713467125565632L;
			public RuleArtefactType getType() {return RuleArtefactType.DRL;}
			public String getNameSpace() {return NAMESPACE;}
			public String getName() {return RULENAME;}
			public byte[] getContent() {return ("package "+NAMESPACE+" \n "
					+"\n"
					+ "rule \""+RULENAME+"\" \n"
					+ "when\n"
					+ "then \n"
					+ "System.out.println(drools.getRule().getName() +\" fooo baaaaar\"); \n"
					+"end \n").getBytes();}	
			});
		ArrayList<String> packs = new ArrayList<String>();
		packs.add(NAMESPACE);
		Base base = new Base();
		base.setPackages(packs);
		Session session = new Session();
		session.setName(RULE_SESSION_NAME);
		base.setSession(session);
		module.getBases().put(RULE_BASE_NAME, base);
		mb.generateArchiveAndSaveToFile(module, new File(TARGET_DIRECTORY));
		
		File file = new File(TARGET_DIRECTORY+"/"+module.getArtefactId()+"-"+module.getVersionId()+".jar");
		assertTrue(file.exists());
	}

	/**
	 * Regular way to use API
	 */
	@Test
	public void A_testGeneratedArchive() {
		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		KieModule kieModule2 = kieRepository.addKieModule(kieServices.getResources().newFileSystemResource(GENERATED_JAR_LOOK_UP_PATH_NAME));
		KieContainer kieContainer = kieServices.newKieContainer(kieModule2.getReleaseId());
		StatelessKieSession kSession = kieContainer.newStatelessKieSession(RULE_SESSION_NAME);
		kSession.execute("");
	}
	
	/**
	 * Non working way to use Drools API
	 */
	@Test
	public void B_testGeneratedArchiveWithNoSessionName() {
		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		KieModule kieModule1 = kieRepository.addKieModule(kieServices.getResources().newFileSystemResource(GENERATED_JAR_LOOK_UP_PATH_NAME));
		KieContainer kieContainer = kieServices.newKieContainer(kieModule1.getReleaseId());

		// no named session will fail
		try {
			kieContainer.newStatelessKieSession();
		} catch (Exception e) {
			assertTrue(e.getClass()==RuntimeException.class);
			assertTrue(e.getMessage().equals("Cannot find a default StatelessKieSession"));
		}
	}

	/**
	 * Non working way to use Drools API
	 */
	@Test
	public void C_testGeneratedArchiveWithNoVersionContainer() {
		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		kieRepository.addKieModule(kieServices.getResources().newFileSystemResource(GENERATED_JAR_LOOK_UP_PATH_NAME));

		KieContainer kieContainer = kieServices.newKieClasspathContainer();
		
		// with this container instantiation, no explicitly loaded module in container and none on the class path
		// so no base are available in container
		assertTrue(kieContainer.getKieBaseNames().isEmpty());
		
		// even more, there is no sessions declared...
		try {
			kieContainer.newStatelessKieSession();
		} catch (Exception e) {
			assertTrue(e.getClass()==RuntimeException.class);
			assertTrue(e.getMessage().equals("Cannot find a default StatelessKieSession"));
		}
	}
	

	@Test
	public void D_testGeneratedArchiveWithNoNamedSessionFromNameBase() {

		KieServices kieServices = KieServices.Factory.get();
		KieRepository kieRepository = kieServices.getRepository();
		KieModule kieModule1 =  kieRepository.addKieModule(kieServices.getResources().newFileSystemResource(GENERATED_JAR_LOOK_UP_PATH_NAME));

		KieContainer kieContainer = kieServices.newKieContainer(kieModule1.getReleaseId());
		assertTrue(!kieContainer.getKieBaseNames().isEmpty());
		
		// get a named kiebase (this must work)
		KieBase base = kieContainer.getKieBase(RULE_BASE_NAME);
		assertTrue(base!=null);
		// verify generated rule is in that package
		assertTrue(base.getRule(NAMESPACE,RULENAME)!=null);

		KieSessionConfiguration conf = kieServices.newKieSessionConfiguration();
		conf.setProperty(ForceEagerActivationOption.PROPERTY_NAME,"false");
		conf.setProperty(TimedRuleExectionOption.PROPERTY_NAME,"false");
		conf.setProperty(BeliefSystemTypeOption.PROPERTY_NAME,BeliefSystemType.SIMPLE.getId());
		conf.setProperty(ClockTypeOption.PROPERTY_NAME,ClockType.REALTIME_CLOCK.getId());
		conf.setProperty(QueryListenerOption.PROPERTY_NAME,QueryListenerOption.STANDARD.getAsString());
		conf.setProperty(TimerJobFactoryOption.PROPERTY_NAME,TimerJobFactoryType.DEFUALT.getId());
		
		// only named session can be created
		StatelessKieSession kSession1 = base.newStatelessKieSession(conf );
		assertTrue(kSession1.getKieBase().getRule(NAMESPACE, RULENAME).getName().equalsIgnoreCase(RULENAME)); 
		assertTrue(kSession1 != null);

		List<BatchExecutionCommand> cmds = new ArrayList<BatchExecutionCommand>();
		ExecutionResults results = kSession1.execute( CommandFactory.newBatchExecution( cmds ) );
		assertTrue(results.getIdentifiers().isEmpty());

		StatelessKieSession kSession2 = base.newStatelessKieSession();
		assertTrue(kSession2.getKieBase().getRule(NAMESPACE, RULENAME).getName().equalsIgnoreCase(RULENAME)); 
		assertTrue(kSession2 != null);

		results = kSession2.execute( CommandFactory.newBatchExecution( cmds ) );
		assertTrue(results.getIdentifiers().isEmpty());
	}
}
