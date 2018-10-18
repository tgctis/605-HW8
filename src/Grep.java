import java.io.*;
import java.util.regex.*;
import java.util.Scanner;

/**
 * Grep.java
 *
 * Version:
 * $Id$
 *
 * Revisions:
 * $Log$
 *
 * Re-creates some functions of the unix grep()
 * specific tags [-c, -l, -w, -q]
 * Will accept multiple arguments
 *
 *
 * @author  Timothy Chisholm
 * @author  Jake Groszewski
 *
 *
 */
public class Grep {
    private String pattern;
    private String[] matches;
    private String[] fileMatches;
    private int[] matchPerFile;
    private String[] matchPerFileString;

    /**
     * Generic constructor
     */
    public Grep(){
        //do nothing
    }

    /**
     * Constructor for using std.in
     * @param pattern String pattern to match
     * @param numFiles number of files passed in
     */
    public Grep(String pattern, int numFiles){
        this.pattern = pattern;
        this.fileMatches = new String[numFiles];
        this.matchPerFile = new int[numFiles];
        this.matchPerFileString = new String[numFiles];
    }

    private String[] matchEngine(boolean matchWords, BufferedReader reader, String fileName){
        //clear old matches
        this.matches = null;
        boolean fileMatched = false;
        //creation of a new pattern if necessary
        String line_pattern;
        if(matchWords){
            line_pattern = ".*\\b+?" + pattern + "\\b+?.*";
        }else {
            line_pattern = ".*" + pattern + ".*";
        }
        try{
            String line;
            //Read in the lines
//            LineNumberReader reader = new LineNumberReader(new FileReader(fileName));
            while((line = reader.readLine()) != null){
//                System.out.print("\nLine#: " + reader.getLineNumber() + " Line: " + line);
                //match the line to whatever it needs to be, words or any substring
                if(Pattern.matches(line_pattern, line)) {
                    //set flag that this file has been matched
                    fileMatched = true;
                    //If your matched array is empty...
                    if(matches == null){
//                        System.out.print("\tFirst Match! Line#" + reader.getLineNumber());
                        matches = new String[1];
                        matches[0] = line;
                    }else { //your matched array has something in it, update it
//                        System.out.print("\tNew Match! Line #: " + reader.getLineNumber());
                        String[] newMatch = new String[matches.length + 1];
                        for (int i = 0; i < matches.length; i++) {
                            newMatch[i] = matches[i];
                        }
                        newMatch[newMatch.length-1] = line;
                        //update this objects matched array for use later
                        matches = newMatch;
                    }
                }
            }
            reader.close();
        }catch(EOFException e){
            //do nothing, end of file
        }catch(IOException e){
            System.err.println("IO Error: " + e.getMessage());
            e.printStackTrace();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        for(int index = 0; index < fileMatches.length; index++){
            if(fileMatched && fileMatches[index] == null) {
                fileMatches[index] = fileName;
                matchPerFile[index] = matches.length;
                matchPerFileString[index] = generateMatchString(fileName);
                fileMatched = false;
            }
        }

        return this.matches;
    }

    /**
     * Counts matching objects (default is lines)
     */
    private int count(boolean countFiles){
        if(!countFiles && matches != null)
            return matches.length;
        else if(countFiles && fileMatches.length > 0)
            return fileMatches.length;
        else
            return 0;
    }

    /**
     * Generates a string representation of all the matches to date
     * @param fileName the file name that the match is for
     * @return string representation of matched lines
     */
    private String generateMatchString(String fileName){
        String returnString = "\0";
        for(String match: matches){
            returnString += fileName + " : " + match + "\n";
        }
        return returnString.trim();
    }

    private static String usageMessage(){
        return "Usage: 'grep {-OPTIONS} PATTERN {FILE}'";
    }

    public static void main(String args[]){
        boolean hasPattern = false;
        boolean test = false;
        boolean doCount = false;
        boolean doWords = false;
        boolean doQuiet = false;
        boolean doFilenames = false;
        String fileName = "\0";
        String pattern = "\0";

        if(test){
            args = new String[6];
            args[0] = "-c";
            args[1] = "-l";
            args[2] = "one";
            args[3] = "src/input.txt";
            args[4] = "src/one.txt";
            args[5] = "src/two.txt";
        }

        if(args.length < 1){
            System.out.println(usageMessage());
            return;
        }

        //parse the arguments
        for(String arg : args){
            //catch the flags
            if(arg.charAt(0) == '-'){
                for(int index = 1; index < arg.length(); index++){
                    char argChar = arg.charAt(index);
                    if (argChar == 'c')
                        doCount = true;
                    if (argChar == 'w')
                        doWords = true;
                    if (argChar == 'q')
                        doQuiet = true;
                    if (argChar == 'l')
                        doFilenames = true;
                }
            }else{//arguments are done, onto pattern/file
                if(hasPattern) { //pattern comes before file, anything after are files, or if using stdin, nothing
                    fileName += arg + "|";
                }else{ //pattern comes before file...
                    hasPattern = true;
                    pattern = arg;
                }
            }
        }

        //get all the files
        String[] files = fileName.split("\\|");

        //get a grep
        Grep testGrep = new Grep(pattern, files.length);

        /*If we're using stdin, we can skip all of the file handling*/
        if(files[0].matches("\\W+")){
            try{
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                testGrep.matchEngine(doWords, reader, "");
            }catch(Exception e){
                e.printStackTrace();
            }
        }else {
            //run the engine for each file
            for (String file : files) {
                try{
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    testGrep.matchEngine(doWords, reader, file.trim());
                }catch(FileNotFoundException e){
                    System.err.println(file.trim() + " - not found.");
                }catch(Exception e){
                    e.printStackTrace();
                }

            }
        }
        /*
            Precedence of flags....
            0. -q | quiet
            1. -l | filenames
            2. -c | count
             */

        //exit if any match comes back positive
        if (doQuiet) {
            if (testGrep.count(false) > 0)
                System.exit(0);
            else
                System.exit(1);
        }

        //Priority 1 - filenames
        if (doFilenames) {
            for (int index = 0; index < files.length; index++) {
                if (testGrep.matchPerFile[index] > 0)
                    System.out.println(files[index].trim());
            }
        } else {//not doing filenames...
            //Priority 2 - counting
            if (doCount) {//count files
                if (files.length > 1) {//multi-file representation
                    for (int index = 0; index < files.length; index++) {
                        System.out.println(files[index].trim() + " : " + testGrep.matchPerFile[index]);
                    }
                } else {//single file representation
                    System.out.println("\n" + testGrep.count(true) + "");
                }
            } else {//not counting... want the actual matched lines
                if (files.length > 1) {//multi-file
                    for (int index = 0; index < files.length; index++) {
                        if (testGrep.matchPerFileString[index] != null)
                            System.out.println(testGrep.matchPerFileString[index]);
                    }
                } else {//single-file
                    for (String match : testGrep.matches) {
                        System.out.println(match);
                    }
                }
            }
        }
    }
}
