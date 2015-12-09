package com.acme.module.gitloader;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.acme.module.exception.ModuleException;
import com.acme.module.model.RuleArtefact;
import com.acme.module.model.RuleArtefactType;

/**
 * This class enables the cloning of a remote GIT repository<br>
 * and the look up of rule Artefacts in the locally cloned repository<br>
 *  
 *
 */
public class GitRuleLoader {
	
	static Logger logger = LoggerFactory.getLogger(GitRuleLoader.class);
	
	private final String localRepositoryDirectory, remoteRepositoryUri;

	/**
	 * GitRuleLoader Constructor<br>
	 * This class is initialized with two Strings :<br>
	 * - a string path to a local directory<br>
	 * - a string path URI to a remote GIT repository<br>
	 * @param localRepositoryDirectory : local directory path to be used to store the remote repository
	 * @param remoteRepositoryUri : the remote repository URI
	 */
	public GitRuleLoader(String localRepositoryDirectory, String remoteRepositoryUri){
		this.localRepositoryDirectory = localRepositoryDirectory;
		this.remoteRepositoryUri = remoteRepositoryUri;
	}

	/**
	 * Returns the name space for files that should be rules<br>
	 * The namespace or packagename, must start with "rules." <br> 
	 * @param fullPathName
	 * @param fileName
	 * @return
	 */
	private static String getNameSpaceFromFileNamePath( String fullPathName,  String fileName) {
		if(!"".equals(fileName)){
			int fileNameIndex = fullPathName.lastIndexOf(fileName);
			if((fileNameIndex!=-1)&&(fileNameIndex>0)){
				String pathToFileName = fullPathName.substring(0, fileNameIndex);
				int namespaceBeginIndex = pathToFileName.lastIndexOf("rules", pathToFileName.length()-1);
				if((namespaceBeginIndex != -1 )){
					String nameSpace = (pathToFileName.substring(namespaceBeginIndex, pathToFileName.length()-1)).replace('/', '.');
					return nameSpace;
				}
			}
		}
		return "";
	}

	/**
	 * Returns the file extension name from full file path name JGit repository style 
	 * @param fullPathName
	 * @return
	 */
	private static String getExtensionFromFileNamePath(String fullPathName) {
		// null or empty name
		if(null==fullPathName||"".equals(fullPathName))
			return "";
		
		// lookup period char last position in name
		int periodIndex = fullPathName.lastIndexOf('.');
		
		// if : no extension, no filename but extension, ends with a period
		if(periodIndex == -1||periodIndex == 0 || periodIndex == fullPathName.length()-1)
			return "";
		else
			// return extension
			return fullPathName.substring(periodIndex+1);
	}
	
	/**
	 * 
	 * @param extension
	 * @return
	 */
	private static boolean isDroolsSupportedRuleFileExtension(final String extension){
		boolean result  = false;
		if(extension!=null && !"".equals(extension)){
			if(RuleArtefactType.getSupportedRuleFileExtensions().contains(extension))
				result = true;
		}
		return result;
	}

	/**
	 * This method clones a remote GIT repository on a local directory<br>
	 * If the local directory is not empty, it will get emptied and deleted first prior to cloning. 
	 * The remote repository and local directory information is provided at Instantiation time <br>
	 * If the remote repository is private, then login and password strings may be set<br> 
	 * so that cloning a private repository is possible with adequate credentials<br>
	 * <br>
	 *  <strong>NOTE</strong>: This method may throw a {@link ModuleException}<br>
	 *  
	 * 
	 * @param login a non empty string if needed or null otherwise
	 * @param password a non empty string if needed or null otherwise
	 */
	public void cloneRemoteRepositoryOnLocal(String login, String password) {
		
		// in case null credential, just init as empty strings
		if(login==null)
			login="";
		if(password==null)
			password="";
		
		try {
			// cleanup local directory before cloning repository
			cleanUpAndDeleteRepository( localRepositoryDirectory);

			// clone remote repository as Bare repository (no need for other type)
			 CloneCommand cloneCommand = Git.cloneRepository();
			 Git clone = cloneCommand.setURI(remoteRepositoryUri)
			.setCloneAllBranches(true)
			.setCloneSubmodules(true)
			.setBare(true)
			.setDirectory(new File(localRepositoryDirectory))
			.setCredentialsProvider(new UsernamePasswordCredentialsProvider(login, password))
			.call();
			logger.info("Remote repository <"+remoteRepositoryUri+">sucessfuly cloned in directory <"+localRepositoryDirectory+">");
			
			// NOTE : be sure to call the following method,  
			// otherwise some repository files can not be deleted afterward
			// because they are locked on the file system...
			clone.getRepository().close();
			
		}
		catch (Exception e) {
			throw new ModuleException("unnable to clone<"+remoteRepositoryUri+"> in <"+localRepositoryDirectory+">",e);
		} 
	}
	
	/**
	 * This methods delete a directory Tree with all its content
	 * @param localRepositoryDirectory
	 */
	private static void cleanUpAndDeleteRepository(String localRepositoryDirectory){
		File localDirRepo = new File(localRepositoryDirectory);
		if(localDirRepo.exists()){
			emptyDirectoryTree(localDirRepo);
			localDirRepo.delete();
		}
	}

	/**
	 * This method cleans up a directory File
	 * @param directory
	 */
	private static void emptyDirectoryTree(File directory){
		if(directory.isDirectory()){
			try(DirectoryStream<Path> stream = Files.newDirectoryStream(directory.toPath())){
				for (Path file: stream){
					if(file.toFile().isDirectory()){
						emptyDirectoryTree(file.toFile());
						file.toFile().delete();
					}
					else{
						String name = file.toFile().getAbsolutePath();
						if(!file.toFile().delete()){
							throw new Exception("failed to delete file <"+name+">");
						}
					}
				}
			}
			catch (Exception e) {
				throw new ModuleException("Unable to empty directory <"+directory.toPath().toString()+">",e);
			}
		}
	}
	
	
	
	
	/**
	 * This methods walks the local repository() to look up for drools artefacts<br>
	 * A package name must be provided to search in the repository for a specific package<br>
	 *   
	 * @param lookUpRootDirectories
	 * @return The list of artefacts that was found in that package/namespace
	 */
	public List<RuleArtefact> fetchRulesArtefactsFromClonedRepository(String packageName){
		List<RuleArtefact> ruleArtefacts = new ArrayList<RuleArtefact>(); 
		
		// we work on local copy repository
		// we need a builder so that we can use it
		File localDirRepo = new File(localRepositoryDirectory);
		FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder();
		repositoryBuilder.setMustExist(true);
		repositoryBuilder.readEnvironment();
		repositoryBuilder.setGitDir(localDirRepo);

		Repository repository = null; 	// the repository to inspect
		RevWalk revWalk = null ;		// repository commit walker
		TreeWalk treeWalk = null;		// repository walker
		ObjectReader reader = null;		// repository object reader
		
		try {
			repository = repositoryBuilder.build();
			
			if (repository.getObjectDatabase().exists()) {
				logger.info("found local repository <"+localRepositoryDirectory+"> ");
				
				if (repository.getRef("HEAD") != null) {
					logger.info("Found local repository HEAD <"+localRepositoryDirectory+"> ");
					
					reader = repository.newObjectReader();	// we get a reader to read repository Objects
					revWalk = new RevWalk(repository);		// initiate repository walker for commits
					Ref head = repository.getRef("HEAD");	// get a reference on Head repository
					RevCommit commit = revWalk.parseCommit(head.getObjectId());// get HEAD commit from repository
					RevTree tree = commit.getTree();		// get commit tree from HEAD
					treeWalk = new TreeWalk(repository);	// get a tree walker for this repository
					treeWalk.addTree(tree);					// we'll walk the HEAD commit tree
					treeWalk.setRecursive(false);			// walk the tree not recursively, because we'll decide WHEN entering subtrees.
					
					
					while (treeWalk.next()) {
						if (treeWalk.isSubtree()) {
							treeWalk.enterSubtree();// if its a directory, then explore it
						} 
						else {
							String fullName = new String(treeWalk.getPathString().getBytes("UTF8"));
							String shortName = new String(treeWalk.getNameString().getBytes("UTF8"));
							String extension = getExtensionFromFileNamePath(fullName);
							String localPackage = getNameSpaceFromFileNamePath(fullName,shortName);
							
							// are we at the package level in the tree ?
							if(packageName!=null && !packageName.equals(localPackage))
								continue;// NOT!
							
							if (isDroolsSupportedRuleFileExtension(extension)) {
								logger.info("Found file: " + treeWalk.getPathString());
								RuleArtefact ra = new RuleArtefact();
								ra.setName(shortName);
								ra.setNameSpace(localPackage);
								ra.setType(RuleArtefactType.getRuleArtefactTypeFromExtension(extension));
								ra.setContent(reader.open(treeWalk.getObjectId(0)).getBytes());
								ruleArtefacts.add(ra);
							}
						}
					}
				} else{
					throw new Exception("local repository HEAD <"+localRepositoryDirectory+"> does not exists");
				}
			} else{
				throw new Exception("local repository <"+localRepositoryDirectory+"> does not exists");
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(e.getCause()==null)
				throw new ModuleException("Failed to build repository", e);
			else
				throw new ModuleException(e.getMessage(),e.getCause());
		}
		finally{
			// clean up to prevent either memory leaks or locked file system objects 
			if(revWalk != null) revWalk.release();
			if(treeWalk != null)treeWalk.release();
			if(reader != null)reader.release();
			if(repository != null)repository.close();
		}
		return ruleArtefacts;
	}
	
	/**
	 * This methods destroys local repository so that FileSystem is cleaned up<br>
	 * After this call no rules can be fetched from local repository<br>
	 * unless a successful call to <b>cloneRemoteRepositoryOnLocal</b> method is performed.<br>
	 */
	public void close(){
		cleanUpAndDeleteRepository( localRepositoryDirectory);
	}
}
