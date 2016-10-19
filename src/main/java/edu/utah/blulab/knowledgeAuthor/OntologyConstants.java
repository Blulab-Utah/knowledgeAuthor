package edu.utah.blulab.knowledgeAuthor;

/**
 * Created by melissa on 9/7/16.
 */
public class OntologyConstants {

        public static final String SCHEMA_URI = "http://blulab.chpc.utah.edu/ontologies/v2/Schema.owl";
        public static final String MODIFIER_URI = "http://blulab.chpc.utah.edu/ontologies/v2/Modifier.owl";
        public static final String CONTEXT_BASE_URI = "http://blulab.chpc.utah.edu/ontologies/v2/ConText.owl";
        public static final String TERM_MAP_URI = "http://blulab.chpc.utah.edu/ontologies/TermMapping.owl";

        public static final String PROP_LABEL = "http://www.w3.org/2000/01/rdf-schema#label";

        public static final String PREF_TERM = OntologyConstants.TERM_MAP_URI + "#preferredTerm";
        public static final String SYNONYM = OntologyConstants.TERM_MAP_URI + "#synonym";
        public static final String MISSPELLING = OntologyConstants.TERM_MAP_URI + "#misspelling";
        public static final String REGEX = OntologyConstants.TERM_MAP_URI + "#regex";
        public static final String ABBREV = OntologyConstants.TERM_MAP_URI + "#abbreviation";
        public static final String ALT_CODE = OntologyConstants.TERM_MAP_URI + "#alternateCode";
        public static final String CODE = OntologyConstants.TERM_MAP_URI + "#code";
        public static final String DEFINITION = OntologyConstants.TERM_MAP_URI + "#definition";
        public static final String SEMANTIC_TYPE = OntologyConstants.TERM_MAP_URI + "#semanticType";
        public static final String SUBJ_EXP = OntologyConstants.TERM_MAP_URI + "#subjectiveExpression";
        public static final String WINDOW = OntologyConstants.CONTEXT_BASE_URI + "#windowSize";

        public static final String[] ANNOTATION_PROPS = {PROP_LABEL, PREF_TERM, SYNONYM, MISSPELLING, REGEX, ABBREV, ALT_CODE,
                CODE, DEFINITION, SEMANTIC_TYPE, SUBJ_EXP, WINDOW};
}