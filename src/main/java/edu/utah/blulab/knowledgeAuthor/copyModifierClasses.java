package edu.utah.blulab.knowledgeAuthor;

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
            final boolean limitToEng = true;

            final IRI modIRI = IRI.create(OntologyConstants.MODIFIER_URI);
            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLDataFactory factory = manager.getOWLDataFactory();
            System.out.println("Loading domain ontology...");
            final OWLOntology domainOnt = manager.loadOntologyFromOntologyDocument(domainFile);
            System.out.println("Loading modifier ontology...");
            final OWLOntology modOnt = manager.loadOntology(modIRI);

            ArrayList<String> classList = new ArrayList<String>();
            for(int i = 1; i<args.length; i++){
                String str = args[i];
                //classList.add(str);
                ArrayList<String> ancestry = new ArrayList<String>();
                getVariableList(factory.getOWLClass(IRI.create(str)), new ArrayList<OWLClass>(), ancestry,
                        manager, factory);
                classList.addAll(ancestry);

            }

            for(String clsName : classList){
                System.out.println("Copying " + clsName + " ...");
                copyModifier(manager, factory, domainOnt, modOnt, clsName, limitToEng);
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
                                    OWLOntology domain, OWLOntology modifier, String modStr, boolean limitToEng){

        OWLClass sourceClass = factory.getOWLClass(IRI.create(modStr));
        String domainURI = domain.getOntologyID().getOntologyIRI().toString();
        String targetClassName = sourceClass.asOWLClass().getIRI().getShortForm();
        OWLClass targetClass = factory.getOWLClass(IRI.create(domainURI + "#" + targetClassName));
        //System.out.println(targetClass.getIRI().toString());

        //If there is superclass add axiom else add as subclass of OWLThing
        Set<OWLClassExpression> superclasses = sourceClass.getSuperClasses(modifier);
        if(superclasses.isEmpty()){
            OWLAxiom subOfThing = factory.getOWLSubClassOfAxiom(targetClass, factory.getOWLThing());
            manager.addAxiom(domain, subOfThing);
        }else{
            for(OWLClassExpression superClass : superclasses){
                if(superClass.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                    if(superClass.asOWLClass().getIRI().getNamespace().equals(OntologyConstants.CONTEXT_BASE_URI+"#")){
                        OWLAxiom subOfCTParent = factory.getOWLSubClassOfAxiom(targetClass, superClass.asOWLClass());
                        manager.addAxiom(domain, subOfCTParent);
                    }else{
                        String parentName = superClass.asOWLClass().getIRI().getShortForm();
                        OWLClass targetParentClass = factory.getOWLClass(IRI.create(domainURI + "#"+ parentName));
                        OWLAxiom subOfParent = factory.getOWLSubClassOfAxiom(targetClass, targetParentClass);
                        manager.addAxiom(domain, subOfParent);
                    }

                }
                if(superClass.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)){
                    OWLObjectSomeValuesFrom expression = (OWLObjectSomeValuesFrom) superClass;
                    if(expression.getProperty().getNamedProperty().equals(factory.getOWLObjectProperty(
                            IRI.create(OntologyConstants.HAS_PSEUDO)))){
                        OWLClass sourcePseudo = expression.getFiller().asOWLClass();
                        OWLClass targetPseudo = copyPseudoModifier(sourcePseudo, manager, factory, modifier,
                                domain, domainURI, limitToEng);
                        OWLObjectSomeValuesFrom hasPseudo = factory.getOWLObjectSomeValuesFrom(expression.getProperty(),
                                targetPseudo);
                        OWLAxiom pseudoAxiom = factory.getOWLSubClassOfAxiom(targetClass, hasPseudo);
                        manager.addAxiom(domain, pseudoAxiom);

                    }else if(expression.getProperty().getNamedProperty().equals(factory.getOWLObjectProperty(
                            IRI.create(OntologyConstants.HAS_TERMINATION)))){
                        OWLClass sourceClosure = expression.getFiller().asOWLClass();
                        copyIndividuals(sourceClosure, sourceClosure, manager, factory, modifier, domain,
                                domainURI, limitToEng);
                    }
                }
                if(superClass.getClassExpressionType().equals(ClassExpressionType.DATA_SOME_VALUES_FROM)){
                    OWLDataSomeValuesFrom expression = (OWLDataSomeValuesFrom) superClass;
                    if(expression.getProperty().asOWLDataProperty().equals(OntologyConstants.IS_DEFAULT)){
                        OWLAxiom defaultAxiom = factory.getOWLSubClassOfAxiom(targetClass, expression);
                        manager.addAxiom(domain, defaultAxiom);
                    }

                }
            }
        }

        //copy individuals
        copyIndividuals(sourceClass, targetClass, manager, factory, modifier, domain, domainURI, limitToEng);



    }

    private static OWLClass copyPseudoModifier(OWLClass sourceClass, OWLOntologyManager manager, OWLDataFactory factory,
                                           OWLOntology sourceOnt, OWLOntology targetOnt,
                                           String domainURI, boolean limitToEng){
        String targetPseudoClassName = sourceClass.getIRI().getShortForm();
        OWLClass targetPseudoClass = factory.getOWLClass(IRI.create(domainURI + "#" + targetPseudoClassName));
        OWLAxiom pseudoSubclassAxiom = factory.getOWLSubClassOfAxiom(targetPseudoClass,
                factory.getOWLClass(IRI.create(OntologyConstants.PSEUDOMODIFIER)));
        manager.addAxiom(targetOnt, pseudoSubclassAxiom);

        //copy individuals
        copyIndividuals(sourceClass, targetPseudoClass, manager, factory, sourceOnt, targetOnt, domainURI, limitToEng);

        return targetPseudoClass;
    }


    private static void copyIndividuals(OWLClass origCls, OWLClass copyCls, OWLOntologyManager manager, OWLDataFactory factory,
                                        OWLOntology origOnt, OWLOntology copyOnt, String domainIRI, boolean limitToEng){
        //Get individuals for modifier class and copy to domain
        Set<OWLIndividual> lexItems = origCls.getIndividuals(origOnt);
        for(OWLIndividual modItem : lexItems){
            String name = modItem.asOWLNamedIndividual().getIRI().getShortForm();
            //System.out.println(name);
            OWLIndividual domainInd = factory.getOWLNamedIndividual(IRI.create(domainIRI + "#" + name));
            //First, assert indiv to domain class
            OWLClassAssertionAxiom clsAx = factory.getOWLClassAssertionAxiom(copyCls, domainInd);
            manager.addAxiom(copyOnt, clsAx);

            //Copy annotation properties to domain ontology
            for (String propStr : OntologyConstants.ANNOTATION_PROPS){
                copyAnnotationProp(modItem.asOWLNamedIndividual(), domainInd.asOWLNamedIndividual(),
                        IRI.create(propStr), manager, factory, origOnt, copyOnt, limitToEng);
            }

            //Copy object properties
            Set<OWLAxiom> modAxioms = modItem.asOWLNamedIndividual().getReferencingAxioms(origOnt);
            for(OWLAxiom modAxiom : modAxioms){
                //System.out.println(modAxiom.getAxiomType());
                if(modAxiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)){
                    OWLObjectPropertyAssertionAxiom	 opaxiom = (OWLObjectPropertyAssertionAxiom) modAxiom;
                    OWLObjectProperty prop = (OWLObjectProperty) opaxiom.getProperty();

                    if(limitToEng){
                        if(prop.equals(factory.getOWLObjectProperty(IRI.create(OntologyConstants.ENGLISH_ACTION)))){
                            OWLIndividual obj = opaxiom.getObject();
                            String modInd = opaxiom.getSubject().asOWLNamedIndividual().getIRI().getShortForm();
                            OWLIndividual subj = factory.getOWLNamedIndividual(IRI.create(domainIRI + "#"+ modInd));

                            OWLObjectPropertyAssertionAxiom propertyAssertion = factory
                                    .getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
                            manager.addAxiom(copyOnt, propertyAssertion);
                        }
                    }else{
                        OWLIndividual obj = opaxiom.getObject();
                        String modInd = opaxiom.getSubject().asOWLNamedIndividual().getIRI().getShortForm();
                        OWLIndividual subj = factory.getOWLNamedIndividual(IRI.create(domainIRI + "#"+ modInd));

                        //System.out.println(subj.toString());

                        OWLObjectPropertyAssertionAxiom propertyAssertion = factory
                                .getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
                        manager.addAxiom(copyOnt, propertyAssertion);
                    }



                }


            }
        }
    }

    private static void copyAnnotationProp(OWLNamedIndividual origInd, OWLNamedIndividual copyInd, IRI iri,
                                           OWLOntologyManager manager, OWLDataFactory factory, OWLOntology origOnt,
                                           OWLOntology copyOnt, boolean limitToEnglish){
        Set<OWLAnnotation> modLabels = origInd.asOWLNamedIndividual().getAnnotations(origOnt,
                factory.getOWLAnnotationProperty(iri));
        for(OWLAnnotation modLabel : modLabels){
            //System.out.println(modLabel.toString());
            if(limitToEnglish){
                OWLLiteral annLiteral = (OWLLiteral) modLabel.getValue();
                if(annLiteral.hasLang()){
                    if(annLiteral.hasLang("en")){
                        OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(copyInd.asOWLNamedIndividual().getIRI(),
                                modLabel);
                        manager.addAxiom(copyOnt, ax);
                    }
                }else{
                    OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(copyInd.asOWLNamedIndividual().getIRI(),
                            modLabel);
                    manager.addAxiom(copyOnt, ax);
                }
            }else{
                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(copyInd.asOWLNamedIndividual().getIRI(),
                        modLabel);
                manager.addAxiom(copyOnt, ax);
            }


        }
    }

    private static void getVariableList(OWLClass cls, ArrayList<OWLClass> allCls, ArrayList<String> clsList,
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
                clsList.add(subCls.asOWLClass().getIRI().toString());
            }

            getVariableList(subCls.asOWLClass(), allCls, clsList, manager, factory);
        }
    }}

=======
-package edu.utah.blulab.knowledgeAuthor;
 -
 -import org.semanticweb.owlapi.apibinding.OWLManager;
 -import org.semanticweb.owlapi.model.*;
 -
 -import java.io.File;
 -import java.util.ArrayList;
 -import java.util.Set;
 -
 -/**
 - * Created by melissa on 9/7/16.
 - */
 -public class copyModifierClasses {
 -
 -
 -    public static void main(String[] args){
 -        try{
 -            File domainFile = new File(args[0]);
 -
 -            final IRI modIRI = IRI.create(OntologyConstants.MODIFIER_URI);
 -            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
 -            final OWLDataFactory factory = manager.getOWLDataFactory();
 -            final OWLOntology domainOnt = manager.loadOntologyFromOntologyDocument(domainFile);
 -            final OWLOntology modOnt = manager.loadOntology(modIRI);
 -
 -            for(int i = 1; i<args.length; i++){
 -                String str = args[i];
 -                System.out.println("Copying " + str + " ...");
 -                copyModifier(manager, factory, domainOnt, modOnt, str);
 -            }
 -            manager.saveOntology(domainOnt);
 -
 -            System.out.println("FINISHED!!");
 -        }catch(Exception e){
 -            System.err.print(e.toString());
 -            System.out.println("You must specify the local domain file to import classes to as well as the list of" +
 -                    " modifiers you would like to import.");
 -
 -        }
 -
 -    }
 -
 -    public static void copyModifier(OWLOntologyManager manager, OWLDataFactory factory,
 -                                    OWLOntology domain, OWLOntology modifier, String modStr){
 -        IRI domainIRI = domain.getOntologyID().getOntologyIRI();
 -        //System.out.println(domainIRI);
 -
 -        //OWLClass modCls = factory.getOWLClass(IRI.create(edu.utah.blulab.knowledgeAuthor.OntologyConstants.CONTEXT_BASE_URI+"#"+modStr));
 -        OWLClass modCls = factory.getOWLClass(IRI.create(modStr));
 -
 -        //System.out.println("Copy all classes under " + modCls);
 -        ArrayList<OWLClass> subClasses = new ArrayList<OWLClass>();
 -        getVariableList(modCls, new ArrayList<OWLClass>(), subClasses, manager, factory);
 -        for(OWLClass sub : subClasses){
 -            //System.out.println(sub.toString());
 -            //Set superclass axiom
 -            String className = sub.getIRI().getShortForm();
 -            //System.out.println(className);
 -            OWLClass domainSubCls = factory.getOWLClass(IRI.create(domainIRI + "#"+ className));
 -            //System.out.println(domainSubCls.toString());
 -
 -            if(modCls.getIRI().getNamespace().equals(OntologyConstants.CONTEXT_BASE_URI+"#")){
 -                OWLAxiom subAx = factory.getOWLSubClassOfAxiom(domainSubCls, modCls);
 -                manager.addAxiom(domain, subAx);
 -            }else{
 -                String modName = modCls.getIRI().getShortForm();
 -                OWLClass modDomainCls = factory.getOWLClass(IRI.create(domainIRI + "#" + modName));
 -                OWLAxiom subAx = factory.getOWLSubClassOfAxiom(domainSubCls, modDomainCls);
 -                manager.addAxiom(domain, subAx);
 -
 -                Set<OWLClassExpression> parentClasses = modCls.getSuperClasses(modifier);
 -                for(OWLClassExpression exp: parentClasses){
 -                    if(exp.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
 -                        if(exp.asOWLClass().getIRI().getNamespace().equals(OntologyConstants.CONTEXT_BASE_URI + "#")){
 -                            OWLAxiom parentSubAxiom = factory.getOWLSubClassOfAxiom(modDomainCls, exp.asOWLClass());
 -                            manager.addAxiom(domain, parentSubAxiom);
 -                        }
 -                    }
 -                }
 -            }
 -
 -
 -
 -            //Get any annotation properties on modifier subclass
 -            Set<OWLAnnotation> annProps = sub.getAnnotations(modifier);
 -            for(OWLAnnotation ann : annProps){
 -                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(sub.getIRI(), ann);
 -                manager.addAxiom(domain, ax);
 -            }
 -
 -            //Get sublcass axioms to copy over to domain class
 -            Set<OWLClassExpression> exps = sub.getSuperClasses(modifier);
 -            for(OWLClassExpression e : exps){
 -                //System.out.println(e.getClassExpressionType());
 -                if(e.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)){
 -                    //System.out.println(e);
 -                    OWLObjectSomeValuesFrom objprop = (OWLObjectSomeValuesFrom) e;
 -                    OWLClass modObj = objprop.getFiller().asOWLClass();
 -                    if(modObj.getIRI().getNamespace().equals(OntologyConstants.MODIFIER_URI+"#")){
 -                        String temp = modObj.getIRI().getShortForm();
 -                        OWLClass objDomainCls = factory.getOWLClass(IRI.create(domainIRI + "#" + temp));
 -                        //Get superclass of modObj
 -                        Set<OWLClassExpression> origSuperClasses = modObj.getSuperClasses(modifier);
 -                        for(OWLClassExpression origSuperClsExp : origSuperClasses){
 -                            if(!origSuperClsExp.isAnonymous()){
 -                                OWLClass origSuperCls = origSuperClsExp.asOWLClass();
 -                                OWLAxiom subClsAxiom = factory.getOWLSubClassOfAxiom(objDomainCls, origSuperCls);
 -                                manager.addAxiom(domain, subClsAxiom);
 -                            }
 -
 -                        }
 -                        copyIndividuals(modObj, objDomainCls, manager, factory, modifier, domain, domainIRI);
 -                        OWLClassExpression domainObjProp = factory.getOWLObjectSomeValuesFrom(
 -                                objprop.getProperty(), objDomainCls);
 -                        //System.out.println("Changed the URI to this: " + domainObjProp);
 -                        OWLSubClassOfAxiom domainSuperClass = factory.getOWLSubClassOfAxiom(domainSubCls, domainObjProp);
 -                        manager.addAxiom(domain, domainSuperClass);
 -                    }else{
 -                        copyIndividuals(modObj, modObj, manager, factory, modifier, domain, domainIRI);
 -                        OWLSubClassOfAxiom domainAxiom = factory.getOWLSubClassOfAxiom(domainSubCls, objprop);
 -                        manager.addAxiom(domain, domainAxiom);
 -                    }
 -                    //OWLObjectPropertyExpression relation = objprop.getProperty();
 -                    //System.out.println(modObj.getIRI().getNamespace());
 -                }if(e.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
 -                    //System.out.println(e.asOWLClass().getIRI().getNamespace());
 -                    OWLClass superCls = null;
 -                    if(e.asOWLClass().getIRI().getNamespace().equals(OntologyConstants.CONTEXT_BASE_URI+"#")){
 -                        superCls = e.asOWLClass();
 -                    }else{
 -                        String superClsName = e.asOWLClass().getIRI().getShortForm();
 -                        superCls = factory.getOWLClass(IRI.create(domainIRI + "#" + superClsName));
 -                    }
 -
 -                    OWLAxiom subAxiom = factory.getOWLSubClassOfAxiom(domainSubCls, superCls);
 -                    manager.addAxiom(domain, subAxiom);
 -                }
 -
 -            }
 -
 -            copyIndividuals(sub, domainSubCls, manager, factory, modifier, domain, domainIRI);
 -
 -
 -        }
 -
 -    }
 -
 -    private static void copyIndividuals(OWLClass origCls, OWLClass copyCls, OWLOntologyManager manager, OWLDataFactory factory,
 -                                        OWLOntology origOnt, OWLOntology copyOnt, IRI copyIRI){
 -        //Get individuals for modifier class and copy to domain
 -        Set<OWLIndividual> lexItems = origCls.getIndividuals(origOnt);
 -        for(OWLIndividual modItem : lexItems){
 -            String name = modItem.asOWLNamedIndividual().getIRI().getShortForm();
 -            //System.out.println(name);
 -            OWLIndividual domainInd = factory.getOWLNamedIndividual(IRI.create(copyIRI + "#" + name));
 -            //First, assert indiv to domain class
 -            OWLClassAssertionAxiom clsAx = factory.getOWLClassAssertionAxiom(copyCls, domainInd);
 -            manager.addAxiom(copyOnt, clsAx);
 -
 -            //Copy annotation properties to domain ontology
 -            for (String propStr : OntologyConstants.ANNOTATION_PROPS){
 -                copyAnnotationProp(modItem.asOWLNamedIndividual(), domainInd.asOWLNamedIndividual(),
 -                        IRI.create(propStr), manager, factory, origOnt, copyOnt);
 -            }
 -
 -            //Copy object properties
 -            Set<OWLAxiom> modAxioms = modItem.asOWLNamedIndividual().getReferencingAxioms(origOnt);
 -            for(OWLAxiom modAxiom : modAxioms){
 -                //System.out.println(modAxiom.getAxiomType());
 -                if(modAxiom.isOfType(AxiomType.OBJECT_PROPERTY_ASSERTION)){
 -                    OWLObjectPropertyAssertionAxiom	 opaxiom = (OWLObjectPropertyAssertionAxiom) modAxiom;
 -                    OWLObjectProperty prop = (OWLObjectProperty) opaxiom.getProperty();
 -                    OWLIndividual obj = opaxiom.getObject();
 -                    String modInd = opaxiom.getSubject().asOWLNamedIndividual().getIRI().getShortForm();
 -                    OWLIndividual subj = factory.getOWLNamedIndividual(IRI.create(copyIRI + "#"+ modInd));
 -
 -                    //System.out.println(subj.toString());
 -
 -                    OWLObjectPropertyAssertionAxiom propertyAssertion = factory
 -                            .getOWLObjectPropertyAssertionAxiom(prop, subj, obj);
 -                    manager.addAxiom(copyOnt, propertyAssertion);
 -                }
 -
 -
 -            }
 -        }
 -    }
 -
 -    private static void copyAnnotationProp(OWLNamedIndividual origInd, OWLNamedIndividual copyInd, IRI iri,
 -                                           OWLOntologyManager manager, OWLDataFactory factory, OWLOntology origOnt, OWLOntology copyOnt){
 -        Set<OWLAnnotation> modLabels = origInd.asOWLNamedIndividual().getAnnotations(origOnt,
 -                factory.getOWLAnnotationProperty(iri));
 -        for(OWLAnnotation modLabel : modLabels){
 -            //System.out.println(modLabel.toString());
 -            OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(copyInd.asOWLNamedIndividual().getIRI(),
 -                    modLabel);
 -            manager.addAxiom(copyOnt, ax);
 -        }
 -    }
 -
 -    private static void getVariableList(OWLClass cls, ArrayList<OWLClass> allCls, ArrayList<OWLClass> clsList,
 -                                        OWLOntologyManager manager, OWLDataFactory factory){
 -        //make sure class exists and hasn't already been visited
 -        OWLClass c = factory.getOWLClass(cls.getIRI());
 -        if(cls == null || allCls.contains(cls)){
 -            return;
 -        }
 -
 -        Set<OWLClassExpression> subExp = cls.getSubClasses(manager.getOntologies());
 -        //System.out.println("Class " + cls.asOWLClass().getIRI());
 -        for(OWLClassExpression subCls : subExp){
 -            //System.out.println("Expression: " + subCls.asOWLClass().toString());
 -            if(!allCls.contains(cls.asOWLClass())){
 -                allCls.add(cls.asOWLClass());
 -            }
 -            if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CONTEXT_BASE_URI+"#")){
 -                clsList.add(subCls.asOWLClass());
 -            }
 -
 -            getVariableList(subCls.asOWLClass(), allCls, clsList, manager, factory);
 -        }
 -}}
