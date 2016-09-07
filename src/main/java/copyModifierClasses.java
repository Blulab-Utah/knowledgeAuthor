import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by melissa on 9/7/16.
 */
public class copyModifierClasses {


    public static void main(String[] args){
        try{
            File domainFile = new File(args[0]);

            final IRI modIRI = IRI.create(OntologyConstants.MODIFIER_URI);
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLDataFactory factory = manager.getOWLDataFactory();
            final OWLOntology domainOnt = manager.loadOntologyFromOntologyDocument(domainFile);
            final OWLOntology modOnt = manager.loadOntology(modIRI);

            for(int i = 1; i<args.length; i++){
                String str = args[i];
                System.out.println("Copying " + str + " ...");
                copyModifier(manager, factory, domainOnt, modOnt, str);
            }
            manager.saveOntology(domainOnt);

            System.out.println("FINISHED!!");
        }catch(Exception e){
            System.err.print(e.toString());
            System.out.println("You must specify the local domain file to import classes to as well as the list of" +
                    " modifiers you would like to import.");

        }

    }

    public static void copyModifier(OWLOntologyManager manager, OWLDataFactory factory,
                                    OWLOntology domain, OWLOntology modifier, String modStr){
        IRI domainIRI = domain.getOntologyID().getOntologyIRI();
        //System.out.println(domainIRI);

        //OWLClass modCls = factory.getOWLClass(IRI.create(OntologyConstants.CONTEXT_BASE_URI+"#"+modStr));
        OWLClass modCls = factory.getOWLClass(IRI.create(modStr));

        //System.out.println("Copy all classes under " + modCls);
        ArrayList<OWLClass> subClasses = new ArrayList<OWLClass>();
        getVariableList(modCls, new ArrayList<OWLClass>(), subClasses, manager, factory);
        for(OWLClass sub : subClasses){
            //System.out.println(sub.toString());
            //Set superclass axiom
            String className = sub.getIRI().getShortForm();
            //System.out.println(className);
            OWLClass domainSubCls = factory.getOWLClass(IRI.create(domainIRI + "#"+ className));
            //System.out.println(domainSubCls.toString());
            OWLAxiom subAx = factory.getOWLSubClassOfAxiom(domainSubCls, modCls);
            manager.addAxiom(domain, subAx);

            //Get any annotation properties on modifier subclass
            Set<OWLAnnotation> annProps = sub.getAnnotations(modifier);
            for(OWLAnnotation ann : annProps){
                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(sub.getIRI(), ann);
                manager.addAxiom(domain, ax);
            }

            //TODO: Class hierarchy isn't being preserved in copy must debug...
            //Get sublcass axioms to copy over to domain class
            Set<OWLClassExpression> exps = sub.getSuperClasses(modifier);
            for(OWLClassExpression e : exps){
                //System.out.println(e.getClassExpressionType());
                if(e.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)){
                    //System.out.println(e);
                    OWLObjectSomeValuesFrom objprop = (OWLObjectSomeValuesFrom) e;
                    OWLClass modObj = objprop.getFiller().asOWLClass();
                    if(modObj.getIRI().getNamespace().equals(OntologyConstants.MODIFIER_URI+"#")){
                        String temp = modObj.getIRI().getShortForm();
                        OWLClass objDomainCls = factory.getOWLClass(IRI.create(domainIRI + "#" + temp));
                        //Get superclass of modObj
                        Set<OWLClassExpression> origSuperClasses = modObj.getSuperClasses(modifier);
                        for(OWLClassExpression origSuperClsExp : origSuperClasses){
                            if(!origSuperClsExp.isAnonymous()){
                                OWLClass origSuperCls = origSuperClsExp.asOWLClass();
                                OWLAxiom subClsAxiom = factory.getOWLSubClassOfAxiom(objDomainCls, origSuperCls);
                                manager.addAxiom(domain, subClsAxiom);
                            }

                        }
                        copyIndividuals(modObj, objDomainCls, manager, factory, modifier, domain, domainIRI);
                        OWLClassExpression domainObjProp = factory.getOWLObjectSomeValuesFrom(
                                objprop.getProperty(), objDomainCls);
                        //System.out.println("Changed the URI to this: " + domainObjProp);
                        OWLSubClassOfAxiom domainSuperClass = factory.getOWLSubClassOfAxiom(domainSubCls, domainObjProp);
                        manager.addAxiom(domain, domainSuperClass);
                    }else{
                        copyIndividuals(modObj, modObj, manager, factory, modifier, domain, domainIRI);
                        OWLSubClassOfAxiom domainAxiom = factory.getOWLSubClassOfAxiom(domainSubCls, objprop);
                        manager.addAxiom(domain, domainAxiom);
                    }
                    //OWLObjectPropertyExpression relation = objprop.getProperty();
                    //System.out.println(modObj.getIRI().getNamespace());
                }if(e.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                    //System.out.println("This is a possible superclass expression...");
                    String superClsName = e.asOWLClass().getIRI().getShortForm();
                    OWLClass superCls = factory.getOWLClass(IRI.create(domainIRI + "#" + superClsName));
                    OWLAxiom subAxiom = factory.getOWLSubClassOfAxiom(domainSubCls, superCls);
                    manager.addAxiom(domain, subAxiom);
                }

            }

            copyIndividuals(sub, domainSubCls, manager, factory, modifier, domain, domainIRI);


        }

    }

    private static void copyIndividuals(OWLClass origCls, OWLClass copyCls, OWLOntologyManager manager, OWLDataFactory factory,
                                        OWLOntology origOnt, OWLOntology copyOnt, IRI copyIRI){
        //Get individuals for modifier class and copy to domain
        Set<OWLIndividual> lexItems = origCls.getIndividuals(origOnt);
        for(OWLIndividual modItem : lexItems){
            String name = modItem.asOWLNamedIndividual().getIRI().getShortForm();
            //System.out.println(name);
            OWLIndividual domainInd = factory.getOWLNamedIndividual(IRI.create(copyIRI + "#" + name));
            //First, assert indiv to domain class
            OWLClassAssertionAxiom clsAx = factory.getOWLClassAssertionAxiom(copyCls, domainInd);
            manager.addAxiom(copyOnt, clsAx);

            //Copy annotation properties to domain ontology
            for (String propStr : OntologyConstants.ANNOTATION_PROPS){
                copyAnnotationProp(modItem.asOWLNamedIndividual(), domainInd.asOWLNamedIndividual(),
                        IRI.create(propStr), manager, factory, origOnt, copyOnt);
            }

            //Copy object properties
            Set<OWLAxiom> modAxioms = modItem.asOWLNamedIndividual().getReferencingAxioms(origOnt);
            for(OWLAxiom modAxiom : modAxioms){
                //System.out.println(modAxiom.getAxiomType());
                if(modAxiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)){
                    OWLObjectPropertyAssertionAxiom	 opaxiom = (OWLObjectPropertyAssertionAxiom) modAxiom;
                    OWLObjectProperty prop = (OWLObjectProperty) opaxiom.getProperty();
                    OWLIndividual obj = opaxiom.getObject();
                    String modInd = opaxiom.getSubject().asOWLNamedIndividual().getIRI().getShortForm();
                    OWLIndividual subj = factory.getOWLNamedIndividual(IRI.create(copyIRI + "#"+ modInd));

                    //System.out.println(subj.toString());

                    OWLObjectPropertyAssertionAxiom propertyAssertion = factory
                            .getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
                    manager.addAxiom(copyOnt, propertyAssertion);
                }


            }
        }
    }

    private static void copyAnnotationProp(OWLNamedIndividual origInd, OWLNamedIndividual copyInd, IRI iri,
                                           OWLOntologyManager manager, OWLDataFactory factory, OWLOntology origOnt, OWLOntology copyOnt){
        Set<OWLAnnotation> modLabels = origInd.asOWLNamedIndividual().getAnnotations(origOnt,
                factory.getOWLAnnotationProperty(iri));
        for(OWLAnnotation modLabel : modLabels){
            //System.out.println(modLabel.toString());
            OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(copyInd.asOWLNamedIndividual().getIRI(),
                    modLabel);
            manager.addAxiom(copyOnt, ax);
        }
    }

    private static void getVariableList(OWLClass cls, ArrayList<OWLClass> allCls, ArrayList<OWLClass> clsList,
                                        OWLOntologyManager manager, OWLDataFactory factory){
        //make sure class exists and hasn't already been visited
        OWLClass c = factory.getOWLClass(cls.getIRI());
        if(cls == null || allCls.contains(cls)){
            return;
        }

        Set<OWLClassExpression> subExp = cls.getSubClasses(manager.getOntologies());
        //System.out.println("Class " + cls.asOWLClass().getIRI());
        for(OWLClassExpression subCls : subExp){
            //System.out.println("Expression: " + subCls.asOWLClass().toString());
            if(!allCls.contains(cls.asOWLClass())){
                allCls.add(cls.asOWLClass());
            }
            if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CONTEXT_BASE_URI+"#")){
                clsList.add(subCls.asOWLClass());
            }

            getVariableList(subCls.asOWLClass(), allCls, clsList, manager, factory);
        }
}}
