import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.vlog4j.core.model.api.Atom;
import org.semanticweb.vlog4j.core.model.api.Constant;
import org.semanticweb.vlog4j.core.model.api.Predicate;
import org.semanticweb.vlog4j.core.model.api.Rule;
import org.semanticweb.vlog4j.core.model.api.Variable;
import org.semanticweb.vlog4j.core.model.implementation.Expressions;
import org.semanticweb.vlog4j.core.reasoner.Algorithm;
import org.semanticweb.vlog4j.core.reasoner.Reasoner;
import org.semanticweb.vlog4j.core.reasoner.exceptions.EdbIdbSeparationException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.IncompatiblePredicateArityException;
import org.semanticweb.vlog4j.core.reasoner.exceptions.ReasonerStateException;
import org.semanticweb.vlog4j.core.reasoner.implementation.QueryResultIterator;


public class DatalogTranslation {
	protected Set<OWLAxiom> v__s_normalisedAxioms = new HashSet<>();

	protected Set<Atom> v_s_Facts = new HashSet<>();
	protected List<Rule> v_l_Rules = new ArrayList<>();

	protected Set<Predicate> v_s_nomEDB = new HashSet<>();
	protected Set<Predicate> v_s_clsEDB = new HashSet<>();
	protected Set<Predicate> v_s_rolEDB = new HashSet<>();	
	protected Set<Predicate> v_s_subClassEDB = new HashSet<>();	
	protected Set<Predicate> v_s_topEDB = new HashSet<>();
	protected Set<Predicate> v_s_botEDB = new HashSet<>();
	protected Set<Predicate> v_s_subConjEDB = new HashSet<>();
	protected Set<Predicate> v_s_subExEDB = new HashSet<>();
	protected Set<Predicate> v_s_supExEDB = new HashSet<>();
	protected Set<Predicate> v_s_subSelfEDB = new HashSet<>();
	protected Set<Predicate> v_s_supSelfEDB = new HashSet<>();
	protected Set<Predicate> v_s_subRoleEDB = new HashSet<>();

	protected Predicate v_P_triple =Expressions.makePredicate("triple", 3);
	protected Predicate v_P_inst = Expressions.makePredicate("inst", 2);
	protected final Predicate v_P_self = Expressions.makePredicate("self", 2);

	protected Variable v = Expressions.makeVariable("v");
	protected Variable w = Expressions.makeVariable("w");
	protected Variable x = Expressions.makeVariable("x");
	protected Variable y = Expressions.makeVariable("y");
	protected Variable z = Expressions.makeVariable("z");


	public DatalogTranslation() {

	}

	public void visitNormalisedAxiomsHash(Collection<? extends OWLAxiom> normalizedAxioms) throws ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException {
		VisitNormalisedAxioms v_normalizedAxiomVisitor = new VisitNormalisedAxioms();
		for (OWLAxiom axiom: normalizedAxioms) {
			axiom.accept(v_normalizedAxiomVisitor);
		}
		
		DatalogRules dlogrules = new DatalogRules();
		dlogrules.makeRules();
		callReasoner();
	}
	
	//Facts
	public void addToDoubleConstantFacts(Predicate predicate, Constant c1, Constant c2) {
		Atom a = Expressions.makeAtom(predicate, c1, c2);
		//Add Atoms
		v_s_Facts.add(a);
	}
	
	public void addToFourConstantFacts(Predicate predicate, Constant c1, Constant c2, Constant c3, Constant c4) {
		Atom a = Expressions.makeAtom(predicate, c1, c2, c3, c4);
		//Add Atoms
		v_s_Facts.add(a);
	}
	
	public void addToThreeConstantFacts(Predicate predicate, Constant c1, Constant c2,Constant c3) {
		Atom a = Expressions.makeAtom(predicate, c1, c2, c3);
		//Add Atoms
		v_s_Facts.add(a);
	}
	
	//EDB Predicates
	public void addToSubClassEDB(Predicate predicate) {
		v_s_subClassEDB.add(predicate);
	}
	
	public void addToSubConjEDB(Predicate predicate) {
		v_s_subConjEDB.add(predicate);
	}
	
	public void addToSubExEDB(Predicate predicate) {
		v_s_subExEDB.add(predicate);
	}
	
	public void addToSupExEDB(Predicate predicate) {
		v_s_supExEDB.add(predicate);
	}
	
	public void addToSubSelfEDB(Predicate predicate) {
		v_s_subSelfEDB.add(predicate);
	}
	
	public void addToSupSelfEDB(Predicate predicate) {
		v_s_subSelfEDB.add(predicate);
	}
	
	public void addToSubRoleEDB(Predicate predicate) {
		v_s_subRoleEDB.add(predicate);
	}
	
	public void addTopEDB(String predicatename) {
		v_s_topEDB.add(Expressions.makePredicate(predicatename, 1));
	}
	
	public void addBotEDB(String predicatename) {
		v_s_botEDB.add(Expressions.makePredicate(predicatename, 1));
	}
	
	public void addrolesEDB(String predicatename) {
		v_s_rolEDB.add(Expressions.makePredicate(predicatename, 1));
	}
	
	public void addNominalsEDB(String predicatename) {
		v_s_nomEDB.add(Expressions.makePredicate(predicatename, 1));
	}
	
	public void addClassNamesEDB(String predicatename) {
		v_s_clsEDB.add(Expressions.makePredicate(predicatename, 1));
	}
	
	public void callReasoner() throws ReasonerStateException, EdbIdbSeparationException, IncompatiblePredicateArityException, IOException {
		Reasoner reasoner = Reasoner.getInstance();
		reasoner.addRules(v_l_Rules);
		reasoner.addFacts(v_s_Facts);

		reasoner.load();
		reasoner.setAlgorithm(Algorithm.SKOLEM_CHASE);
		reasoner.setReasoningTimeout(1);
		System.out.println("Starting Skolem Chase with 1 second timeout.");

		/* Indeed, the Skolem Chase did not terminate before timeout. */
		boolean skolemChaseFinished = reasoner.reason();
		System.out.println("Has Skolem Chase algorithm finished before 1 second timeout? " + skolemChaseFinished);
		System.out.println(
				"Answers to query " + Expressions.makeAtom(v_P_inst, x,y) + " after reasoning with the Skolem Chase for 1 second:");
		printOutQueryAnswers(Expressions.makeAtom(v_P_inst, x,y), reasoner);
		reasoner.close();

	}

	private void printOutQueryAnswers(Atom queryAtom, Reasoner reasoner) throws ReasonerStateException {
		// TODO Auto-generated method stub
		System.out.println();
		System.out.println("Answers to query " + queryAtom + " before materialisation:");
		try (QueryResultIterator answersBeforeMaterialisation = reasoner.answerQuery(queryAtom, true)) {
			while (answersBeforeMaterialisation.hasNext()) {
				System.out.println(" - " + answersBeforeMaterialisation.next());
			}
			System.out.println();
		}
	}
}