import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;

public class Normalize {

	protected Map<Integer,Set<OWLAxiom>> v_Iterable_MapAxioms = new HashMap<>();
	protected static int v_Iterable_KeyForMap = 1;
    protected final Set<OWLAxiom> v_final_normalised = new HashSet<>();
	
	protected final OWLDataFactory v_factory;
	
	protected static int v_counter;
	
	//constructor
	public Normalize(OWLDataFactory factory) {
		v_factory=factory;
		v_counter = 0;
	}

	/**
	 * Call axiomVisitor Class , Normalize and return new set of normalized axioms.
	 * @throws IOException 
	 * @throws IncompatiblePredicateArityException 
	 * @throws EdbIdbSeparationException 
	 * @throws ReasonerStateException 
	 */
	public Set<OWLAxiom> getFromOntology(OWLOntology onto) throws OWLOntologyCreationException, ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException {
		Set<OWLAxiom> asi = new HashSet<>();
		
		onto.axioms().forEach(x -> asi.add(x));
		
		v_Iterable_MapAxioms.put(v_Iterable_KeyForMap, asi);
		
		AxiomVisitorForNormalisation axmVisitor = new AxiomVisitorForNormalisation(v_factory);
		
		if(v_Iterable_MapAxioms.isEmpty()) {
			System.out.println("No Axioms in the Ontology!!");
		} else {
			visitAxioms(v_Iterable_MapAxioms.get(v_Iterable_KeyForMap), axmVisitor);
		}
		
		for(OWLAxiom axiom: axmVisitor.getNormalisedAxiom()) {
			v_final_normalised.add(axiom);
		}
		
		return v_final_normalised;
	}

	/**
	 * Visit all Axioms in the initial Ontology and normalize the axioms
	 */
	public void visitAxioms(Collection<? extends OWLAxiom> axioms, AxiomVisitorForNormalisation axmVisitor) throws OWLOntologyCreationException {
		
		axmVisitor.setCounterOfFreshNumber(v_counter+1);
		
		for (OWLAxiom axiom : axioms) {
			axiom.accept(axmVisitor);
		}

		v_Iterable_KeyForMap = v_Iterable_KeyForMap + 1;

		v_Iterable_MapAxioms.put(v_Iterable_KeyForMap, axmVisitor.getAxiomsForFurtherNorm());
		
		axmVisitor.clear();
		axmVisitor.removeNull();
		
		if (axmVisitor.getAxiomsForFurtherNorm().isEmpty() ) {
			axmVisitor.clear();
			axmVisitor.removeNull();
			System.out.println("Normalisation Complete!! ");		
		} else {
			visitAxioms(v_Iterable_MapAxioms.get(v_Iterable_KeyForMap), axmVisitor);
		}
	}

	public void crunchCleanNormalisedAxiomFromMap(int keyA) {		
		v_Iterable_MapAxioms.remove(keyA);		
	}
	
/*	//NOt working as map is not initialised
	public void addAxiomsToMap(int number, Set<OWLAxiom> axioms) {
		if (v_Iterable_MapAxioms.containsKey(number)) {
			v_Iterable_MapAxioms.put(number, axioms);
		} else {
			System.out.println("Okay !!");
		}
	}*/

	public boolean isNonComplementOFNamedClass(OWLClassExpression ce) {
		if (ce instanceof OWLObjectComplementOf) {
			throw new IllegalStateException();
		}
		return ce.isClassExpressionLiteral();
	}

	public boolean isNotNamedClass(OWLClassExpression ce) {
		return !ce.isClassExpressionLiteral();
	}

	public OWLAxiom addAxiomOfConjunctSubClass(OWLClassExpression ce1, OWLClassExpression ce2, OWLClassExpression ce3) {
		//ce1 and ce2 subsumes ce3
		return (OWLAxiom)v_factory.getOWLSubClassOfAxiom((OWLClassExpression)v_factory.getOWLObjectIntersectionOf(ce1,ce2),ce3);
	}

	public OWLAxiom addSubClassAxiom(OWLClassExpression ce1, OWLClassExpression ce2) {
		//ce1 subsumes ce2
		return (OWLAxiom)v_factory.getOWLSubClassOfAxiom(ce1, ce2);
	}

	public OWLAxiom addSomevaluesFromAxiomLeft(OWLClassExpression ce1, OWLObject obj,OWLClassExpression ce2) {		
		return (OWLAxiom) v_factory.getOWLSubClassOfAxiom(v_factory.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression) obj, ce1), ce2);
	}
	
	public OWLAxiom addSomevaluesFromAxiomRight(OWLClassExpression ce1, OWLObject obj,OWLClassExpression ce2) {		
		return (OWLAxiom) v_factory.getOWLSubClassOfAxiom(ce1,v_factory.getOWLObjectSomeValuesFrom((OWLObjectPropertyExpression) obj, ce2));
	}
	
	/**
	 * Adds Fresh Class name to the existentially quantified Concept expression
	 */
	public OWLClassExpression addSomeValuesFromToFreshClassName(OWLObjectSomeValuesFrom expr, long conceptNumber) {
		return (OWLClassExpression)v_factory.getOWLObjectSomeValuesFrom(expr.getProperty(), addFreshClassName(conceptNumber));
	}
		
	/**
	 * Adds the fresh concept name
	 */
	public OWLClassExpression addFreshClassName(long conceptNumber) {

		return v_factory.getOWLClass(IRI.create("#FreshConcept" + conceptNumber));
	}
}
