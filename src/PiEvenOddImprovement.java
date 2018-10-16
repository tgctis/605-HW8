import java.io.*;
import java.util.zip.GZIPInputStream;

/**
 * PiEvenOddImprovement.java
 *
 * Version:
 * $Id$
 *
 * Revisions:
 * $Log$
 *
 * Count the number of even/odd numbers in pi (or any file).
 *
 *
 * @author  Timothy Chisholm
 * @author  Jake Groszewski
 *
 *
 */
public class PiEvenOddImprovement {
    private String fileName;
    private BufferedInputStream byteStream;
    private boolean compressed;
    private boolean hasReadDecimal = false;
    private int streamPosition = 0;
    private int streamLength = 4096;
    private int even = 0;
    private int odd = 1;
    private byte[] streamArray = new byte[streamLength];

    public PiEvenOddImprovement(String fileName, boolean compressed){
        this.fileName = fileName;
        this.compressed = compressed;
    }

    public PiEvenOddImprovement(String fileName, boolean compressed, int length){
        this.fileName = fileName;
        this.compressed = compressed;
        this.streamLength = length;
        streamArray = new byte[length];
    }

    public PiEvenOddImprovement(String fileName, boolean compressed, int position, int length){
        this.fileName = fileName;
        this.compressed = compressed;
        this.streamPosition = position;
        this.streamLength = length;
        streamArray = new byte[length];
    }

    public static void printUsageError(){
        System.err.println("Wrong parameters.\nUsage: 'PiEvenOddImprovement FILENAME'");
    }

    /**
     * Opens the file and does some testing
     * @return boolean of readability
     */
    private boolean openFile(){
        try {
            FileInputStream readFile = new FileInputStream(fileName);
            if(!compressed)
                byteStream = new BufferedInputStream(readFile);
            else
                byteStream = new BufferedInputStream(new GZIPInputStream(readFile));

            /*Skips first 2 digits since they are known*/
            byteStream.skip(2);

            if(byteStream.available() == 0)
                throw new EmptyFileException("This is an empty file.");
            else
                return true;
        }catch(FileNotFoundException e) {
            System.out.println("File '" +fileName+ "' not found.");
        }catch(IOError e){
            System.out.println("IO Error: " + e.getMessage());
        }catch(EmptyFileException e){
            System.err.println(e.getMessage());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        return false;
    }

    /**
     * While there is more to read, update the counts and adjust the buffer
     * @return boolean of reading
     */
    private boolean readAndUpdateCounts() throws NoNumbersException{
        try{
            if(byteStream.read(streamArray, streamPosition, streamLength) > 0){
                for(byte myChar : streamArray){
                    if(myChar != 0 && myChar != '\n' && myChar != '\r') {
                        try{
                            int num = Integer.parseInt(String.valueOf((char)myChar));
                            if(num%2 == 1)
                                odd++;
                            else
                                even++;
                        }catch(NumberFormatException e){
                            throw new NoNumbersException("Input was not a number");
                        }
                    }else{
                        return false;
                    }
                }
                return true;
            }else{
                return false;
            }
        }catch(IOException e){
            return false;
        }
    }

    /**
     * Gets the amount of even numbers
     * @return amount of even numbers
     */
    private int getEven(){
        return even;
    }

    /**
     * gets the amount of odd numbers
     * @return the amount of odd numbers
     */
    private int getOdd(){
        return odd;
    }

    /**
     * Gets the ratio of even:odd
     * @return ratio of even:odd numbers
     */
    private double getRatio(){
        if(odd < 1){
            return 1.0;
        }else{
            return (even * 1.0) / (odd * 1.0);
        }
    }

    /**
     * Main method
     * @param args [0] First argument is the filename to be read in
     */
    public static void main(String args[]){
        PiEvenOddImprovement countPi;
        String fileName = "\0";

        if(args.length < 1 || args[0].length() < 1){
            printUsageError();
            System.exit(1);
        }else{
            fileName = args[0];
        }

        try {
            if(fileName.indexOf('.') > 0)
                countPi = new PiEvenOddImprovement(fileName, true);
            else
                countPi = new PiEvenOddImprovement(fileName, false);
            if(!countPi.openFile())
                return;
            do{
                countPi.readAndUpdateCounts();
            }while(countPi.readAndUpdateCounts());

            System.out.printf("Ratio of even:odd %d:%d = %.2f\n\n"
                    , countPi.getEven()
                    , countPi.getOdd()
                    , countPi.getRatio()
            );
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }

    /**
     * Inner exception class, handles if a file is empty
     */
    private class EmptyFileException extends Exception{
        EmptyFileException(String error){
            super(error);
        }
    }

    /**
     * Inner exception class, handles if a file contains anything but numbers
     */
    private class NoNumbersException extends Exception{
        NoNumbersException(String error){
            super(error);
        }
    }
}
