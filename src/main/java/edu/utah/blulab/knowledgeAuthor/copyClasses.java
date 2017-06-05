package edu.utah.blulab.knowledgeAuthor;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.io.File;
import java.util.ArrayList;
import java.util.Set;

/**
 * Created by melissa on 11/8/16.
 */
public class copyClasses {

    public static void main(String[] args){
        try{
            File domainFile = new File(args[0]);
            File sourceFile = new File(args[1]);

            final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            final OWLDataFactory factory = manager.getOWLDataFactory();
            System.out.println("Loading domain ontology...");
            final OWLOntology domainOnt = manager.loadOntologyFromOntologyDocument(domainFile);
            System.out.println(domainOnt);
            System.out.println("Loading source ontology...");
            final OWLOntology sourceOnt = manager.loadOntologyFromOntologyDocument(sourceFile);
            System.out.println(sourceOnt);

            ArrayList<String> classList = new ArrayList<String>();

            for(int i = 2; i<args.length; i++){
                classList.add(args[i]);
                ArrayList<String> subList = new ArrayList<String>();
                getVariableList(factory.getOWLClass(IRI.create(args[i])), new ArrayList<OWLClass>(), subList,
                        manager, factory);
                classList.addAll(subList);

            }

            for(String cls : classList){
                System.out.println("Copying " + cls + " ...");
                copyClass(manager, factory, domainOnt, sourceOnt, cls);
            }


            manager.saveOntology(domainOnt);

            System.out.println("FINISHED!!");
        }catch(Exception e){
            System.err.print(e.toString());
            System.out.println("You must specify the local domain file to import classes to as well as the list of" +
                    " modifiers you would like to import.");

        }

    }

    public static void copyClass(OWLOntologyManager manager, OWLDataFactory factory,
                                 OWLOntology domain, OWLOntology source, String classURI){

        OWLClass sourceClass = factory.getOWLClass(IRI.create(classURI));
        String domainURI = domain.getOntologyID().getOntologyIRI().toString();
        String targetClassName = sourceClass.asOWLClass().getIRI().getShortForm();

        OWLClass targetClass = factory.getOWLClass(IRI.create(domainURI + "#" + targetClassName));
        //System.out.println(targetClass.getIRI().toString());

        //If there is superclass add axiom else add as subclass of OWLThing
        Set<OWLClassExpression> superclasses = sourceClass.getSuperClasses(source);
        if(superclasses.isEmpty()){
            OWLAxiom subOfThing = factory.getOWLSubClassOfAxiom(targetClass, factory.getOWLThing());
            manager.addAxiom(domain, subOfThing);
        }else{
            for(OWLClassExpression superClass : superclasses){
                if(superClass.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS)){
                    String parentName = superClass.asOWLClass().getIRI().getShortForm();
                    OWLClass targetParentClass = factory.getOWLClass(IRI.create(domainURI + "#"+ parentName));
                    OWLAxiom subOfParent = factory.getOWLSubClassOfAxiom(targetClass, targetParentClass);
                    manager.addAxiom(domain, subOfParent);
                }
            }
        }

        //copy all annotation properties
        Set<OWLAnnotation> annotationProperties = sourceClass.getAnnotations(source);
        for(OWLAnnotation annotation : annotationProperties){
            //System.out.println(annotation);
            OWLAnnotationProperty property = annotation.getProperty();
            OWLAnnotationValue sourceValue = annotation.getValue();
            OWLLiteral sourceLiteral = (OWLLiteral) sourceValue;
            OWLLiteral targetLiteral = factory.getOWLLiteral(sourceLiteral.getLiteral(), "en");
            OWLAnnotation targetAnnotation = factory.getOWLAnnotation(property, targetLiteral);
            OWLAnnotationAssertionAxiom annotationAxiom = factory.getOWLAnnotationAssertionAxiom(
                    targetClass.getIRI(), targetAnnotation);
            manager.addAxiom(domain, annotationAxiom);
        }


        //add seealso annotation property linking back to source ontology
        OWLLiteral seeLiteral = factory.getOWLLiteral(sourceClass.getIRI().toString());
        OWLAnnotation seeAnnotation = factory.getOWLAnnotation(factory.getRDFSSeeAlso(), seeLiteral);
        OWLAnnotationAssertionAxiom seeAnnotationAxiom = factory.getOWLAnnotationAssertionAxiom(
                targetClass.getIRI(), seeAnnotation);
        manager.addAxiom(domain, seeAnnotationAxiom);

        //copy




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
            //if(!subCls.asOWLClass().getIRI().getNamespace().equalsIgnoreCase(OntologyConstants.CONTEXT_BASE_URI+"#")){
            clsList.add(subCls.asOWLClass().getIRI().toString());
            //}

            getVariableList(subCls.asOWLClass(), allCls, clsList, manager, factory);
        }
    }

}
