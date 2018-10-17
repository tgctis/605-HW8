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
    private Scanner stdInput;
    private String[] matches;
    private String[] fileMatches;
    private String[] matchPerFile;

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
        this.matchPerFile = new String[numFiles];
        try{
            stdInput = new Scanner(System.in);
        }catch(IOError e){
            e.printStackTrace();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    private String[] matchEngine(boolean matchWords, String fileName){
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
            LineNumberReader reader = new LineNumberReader(new FileReader(fileName));
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
            System.err.println("File: " + fileName + " -- IO Error: " + e.getMessage());
            e.printStackTrace();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }

        for(int index = 0; index < fileMatches.length; index++){
            if(fileMatched && fileMatches[index] == null) {
                fileMatches[index] = fileName;
                matchPerFile[index] = matches.length + "";
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

    private static String usageMessage(){
        return "Usage: 'grep {-OPTIONS} PATTERN {FILE}'";
    }

    public static void main(String args[]){
        boolean hasPattern = false;
        boolean test = true;
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
                if(arg.charAt(1) == 'c')
                    doCount = true;
                if(arg.charAt(1) == 'w')
                    doWords = true;
                if(arg.charAt(1) == 'q')
                    doQuiet = true;
                if(arg.charAt(1) == 'l')
                    doFilenames = true;
            }else{
                if(hasPattern) {
                    fileName += arg + "|";
                }else{
                    hasPattern = true;
                    pattern = arg;
                }
            }
        }

        String[] files = fileName.split("\\|");


        Grep testGrep = new Grep(pattern, files.length);

        for(String file : files){
            testGrep.matchEngine(doWords, file.trim());
        }

        if(doQuiet){
            if(testGrep.count(false) > 0)
                System.exit(0);
            else
                System.exit(1);
        }

        if(doCount){
            if(files.length > 1){
                for(int index = 0; index < files.length; index++){
                    System.out.println(files[index].trim() + " : " + testGrep.matchPerFile[index]);
                }
            }else{
                System.out.println("\n" + testGrep.count(doFilenames) + " matches!");
            }

        }else{
            if(doFilenames){
                for(String fileMatch : testGrep.fileMatches){
                    if(fileMatch != null)
                        System.out.println(fileMatch);
                }
            }else {
                for (String match : testGrep.matches) {
                    System.out.println(match);
                }
            }
        }
    }

}
