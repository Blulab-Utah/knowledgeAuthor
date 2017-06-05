package edu.utah.blulab.knowledgeAuthor;

import edu.utah.blulab.domainontology.Anchor;
import edu.utah.blulab.domainontology.DomainOntology;
import org.semanticweb.owlapi.model.OWLClass;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by melissa on 3/3/17.
 */
public class creaticSemanticTypeClasses {

    public static void main(String[] args) {
        try{
            File domainFile = new File(args[0]);
            //File tuiMappingFile = new File();

            DomainOntology domain = new DomainOntology(domainFile.getAbsolutePath(), false);

            ArrayList<Anchor> anchorDictionary = domain.createAnchorDictionary();
            for(Anchor anchor : anchorDictionary){
                OWLClass anchorClass = domain.getClass(anchor.getURI());
                ArrayList<String> types = anchor.getSemanticType();

                for(String type : types){

                    OWLClass semanticTypeClass = domain.getClass(OntologyConstants.SEMANTIC_TYPE_URI);
                }
            }

        }catch(Exception e){
            System.err.print(e.toString());
            System.out.println("You must specify the local domain file to update.");

        }
    }
}
