import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.rules.TemporaryFolder;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.VLog4jException;
//Main class upload ontology here
public class InferenceForOWLELMain {

	protected static Set<OWLAxiom> v_normalisedAxioms = new HashSet<>();
	protected final String[] v_arrargsList;

	public TemporaryFolder temporaryFolder = new TemporaryFolder();
	
	public InferenceForOWLELMain(String[] args) {
		this.v_arrargsList = args;
	}

	/**
	 * 
	 * @param fileadd
	 * @throws OWLOntologyCreationException
	 * @throws VLog4jException
	 * @throws VLog4jException
	 * @throws VLog4jException
	 * @throws IOException
	 * @throws OWLOntologyStorageException 
	 */
	public void loadOntology(String fileadd) throws OWLOntologyCreationException, VLog4jException, VLog4jException, VLog4jException, IOException, OWLOntologyStorageException {

		OWLOntologyManager man = OWLManager.createOWLOntologyManager();

		OWLOntology onto = man.loadOntologyFromOntologyDocument(new File(fileadd));
		OWLDataFactory factory = man.getOWLDataFactory();		

		Normalize norm = new Normalize(factory);			
		v_normalisedAxioms = norm.getFromOntology(onto);
		
		StopWatch timer = new StopWatch();
		timer.start("Save Normalised Ontology...");
		createNormalisedOntology();
		timer.stop("Saved Normalised Ontology !!");
	}
	
	public void createNormalisedOntology() throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
		  	OWLOntologyManager m = OWLManager.createOWLOntologyManager();
	        OWLOntology o = m.createOntology(v_normalisedAxioms);
	        
	        //File output = temporaryFolder.newFile("normalisedOnto.owl");
	        // Output will be deleted on exit; to keep temporary file replace
	        // previous line with the following
	        File output = new File("/home/raj/normalisedOnto.owl");
	        //StringDocumentTarget target = new StringDocumentTarget(); 
	        //target = output.getAbsolutePath();
	        
	        m.saveOntology(o, IRI.create(output));
	       
	        //System.out.println(target);
	        // Remove the ontology from the manager
	        m.removeOntology(o);
	}

	/**
	 * 
	 * @param arg1
	 * @throws ReasonerStateException
	 * @throws EdbIdbSeparationException
	 * @throws IncompatiblePredicateArityException
	 * @throws IOException
	 */
	public void applyDatalogRules() throws ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException {
		DatalogTranslation dlog = new DatalogTranslation(v_normalisedAxioms);
		dlog.visitNormalisedAxiomsHash();
	}
	
	/**
	 * 
	 * @param args
	 * @throws IOException 
	 * @throws VLog4jException 
	 * @throws OWLOntologyStorageException 
	 */
	public static void main(String []args) throws VLog4jException, IOException, OWLOntologyStorageException{
		
		StopWatch timer = new StopWatch();
		InferenceForOWLELMain inferMain = new InferenceForOWLELMain(args);		

		String file = args[0];

		try {
			
			timer.start("Load File! ");
			inferMain.loadOntology(file);
			timer.stop();
			
			timer.start();
			inferMain.applyDatalogRules();
			timer.stop("Done!");
		
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}		
	}
}
