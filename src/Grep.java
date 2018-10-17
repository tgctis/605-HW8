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
    private String fileName;
    private Scanner stdInput;
    private String[] matches;
    private boolean hasMatch;
    private boolean matchFile;

    /**
     * Generic constructor
     */
    public Grep(){
        //do nothing
    }

    /**
     * Constructor for using std.in
     * @param pattern String pattern to match
     */
    public Grep(String pattern){
        this.pattern = pattern;
        this.matchFile = false;
        try{
            stdInput = new Scanner(System.in);
        }catch(IOError e){
            e.printStackTrace();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Constructor that accepts a fileName
     * @param pattern String pattern to match
     * @param fileName String file name to open and read
     */
    public Grep(String pattern, String fileName){
        this.fileName = fileName;
        this.pattern = pattern;
    }

    private String[] matchEngine(boolean matchWords){
        //clear old matches
        this.matches = null;
        //creation of a new pattern if necessary
        String line_pattern;
        if(matchWords){
//            line_pattern = "\\b+" + pattern;
            line_pattern = "\\b+" + pattern;
        }else {
            line_pattern = ".*" + pattern + ".*";
        }
        System.out.println("Matching " + line_pattern);
        try{
            String line;
            LineNumberReader reader = new LineNumberReader(new FileReader(fileName));
            while((line = reader.readLine()) != null){
                System.out.println("Line#: " + reader.getLineNumber() + " Line: " + line);
                if(Pattern.matches(line_pattern, line)) {
                    if(matches == null){
                        System.out.println("First Match! Line#" + reader.getLineNumber() + " String: " + line);
                        matches = new String[1];
                        matches[0] = line;
                    }else {
                        System.out.println("New Match! String: " + line);
                        String[] newMatch = new String[matches.length + 1];
                        for (int i = 0; i < matches.length; i++) {
                            newMatch[i] = matches[i];
                        }
                        newMatch[newMatch.length-1] = line;
                        matches = newMatch;
                    }
                }
            }
        }catch(EOFException e){
            //do nothing, end of file
        }catch(IOException e){
            e.printStackTrace();
        }catch(Exception e){
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        return this.matches;
    }

    /**
     * Counts matching objects (default is lines)
     */
    private int count(){
        if(matches != null)
            return matches.length;
        else
            return 0;
    }

    /**
     * outputs the number of matching files' names
     */
    private String[] listFiles(){
        return matches;
    }

    /**
     * outputs only lines whose matches are whole words
     */
    private String[] matchWords(){
        return matches;
    }

    /**
     * Returns true if something is found, false if no match
     * @return true if found, false if not found
     */
    private boolean quietMatch(){
        return hasMatch;
    }

    private static String usageMessage(){
        return "Usage: 'grep {-OPTIONS} PATTERN {FILE}'";
    }

    public static void main(String args[]){
        boolean hasPattern = false;
        boolean test = true;
        boolean doCount = false;
        boolean doWords = false;
        String fileName = "\0";
        String pattern = "\0";

        if(test){
            args = new String[4];
            args[0] = "-w";
            args[1] = "-c";
            args[2] = "one";
            args[3] = "src/input.txt";
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
            }else{
                if(hasPattern) {
                    fileName = arg;
                }else{
                    hasPattern = true;
                    pattern = arg;
                }
            }
        }
        System.out.println("File: '" + fileName + "' & Pattern: '" + pattern + "'");
        Grep testGrep = new Grep(pattern, fileName);

        testGrep.matchEngine(doWords);

        if(doCount){
            System.out.println(testGrep.count());
        }
    }

}
