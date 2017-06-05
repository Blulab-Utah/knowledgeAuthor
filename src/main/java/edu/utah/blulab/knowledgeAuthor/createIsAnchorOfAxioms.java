package edu.utah.blulab.knowledgeAuthor;

import java.io.File;

/**
 * Created by melissa on 3/3/17.
 */
public class createIsAnchorOfAxioms {

    public static void main(String[] args) {
        try{
            File domainFile = new File(args[0]);



        }catch(Exception e){
            System.err.print(e.toString());
            System.out.println("You must specify the local domain file to update.");

        }
    }
}
