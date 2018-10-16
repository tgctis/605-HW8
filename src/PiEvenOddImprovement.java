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
public class PiEvenOddImprovement implements Runnable{
    private String fileName;
    private InputStream byteStream;
    private boolean compressed;
    private int streamLength;
    private int even = 0;
    private int odd = 0;
    private byte[] streamArray;
    private int numThreads;
    private int threadNum;
    private PiEvenOddImprovement counter;

    public PiEvenOddImprovement(){
        //final counter
    }

    public PiEvenOddImprovement(String fileName, boolean compressed, int threadNum, int numThreads, PiEvenOddImprovement counter){
        this.fileName = fileName;
        this.compressed = compressed;
        this.numThreads = numThreads;
        this.threadNum = threadNum;
        this.counter = counter;
        System.out.println("Thread #" + threadNum);
    }

    public static void printUsageError(){
        System.err.println("Wrong parameters.\nUsage: 'PiEvenOddImprovement FILENAME {NUM_THREADS}'");
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
                //byteStream = new GZIPInputStream(new BufferedInputStream(readFile));
                byteStream = new BufferedInputStream(new GZIPInputStream(readFile));

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
    private void readAndUpdateCounts() throws NoNumbersException{
        try{
            int loopNum = 0;
            int numChars = byteStream.available();
            if(compressed) //we don't know how large the file is...
                numChars = 1000000000;
            int charPerThread = numChars/numThreads;
            if(charPerThread > 5000)
                streamLength = 5000;
            else{
                streamLength = charPerThread;
            }
            streamArray = new byte[streamLength];
            if(threadNum == 0) {
                byteStream.skip(2);
                counter.addOdd();
            }else{
                byteStream.skip(2+(threadNum*charPerThread));
            }

            System.out.println("I am thread #" + threadNum + " I'm reading chars from " + (threadNum*charPerThread) + " to " + ((threadNum+1) * charPerThread));
            while(byteStream.read(streamArray, 0, streamLength) > 0 && loopNum < (charPerThread/streamLength)){
                for(byte myChar : streamArray){
                    if(myChar != 0 && myChar != '\n' && myChar != '\r') {
                        try{
                            int num = Integer.parseInt(String.valueOf((char)myChar));
                            if(num%2 == 1)
                                counter.addOdd();
                            else
                                counter.addEven();
                        }catch(NumberFormatException e){
                            throw new NoNumbersException("Input was not a number");
                        }
                    }
                }
                loopNum++;
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void run(){
        try{
            if(openFile()) {
                readAndUpdateCounts();
            }
        }catch(NoNumbersException e){
            System.out.println(e.getMessage());
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
     * Increments the even counter
     */
    private void addEven(){
        even++;
    }

    /**
     * Increments the odd counter
     */
    private void addOdd(){
        odd++;
    }


    /**
     * Main method
     * @param args [0] First argument is the filename to be read in
     */
    public static void main(String args[]){
        int numThreads = 20;
        Thread[] allThreads;
        PiEvenOddImprovement[] allCounters;
        String fileName = "\0";

        if(args.length < 1 || args[0].length() < 1){
            printUsageError();
            System.exit(1);
        }else{
            fileName = args[0];
        }

        if(args.length == 2){
            numThreads = Integer.parseInt(args[1]);
        }

//        fileName = "src/test_small_pi";
        allThreads = new Thread[numThreads];
        allCounters = new PiEvenOddImprovement[numThreads];

        try {
            //crude index
            boolean compressed = false;
            if(fileName.indexOf('.') > 0)
                compressed = true;

            /*Spin the threads*/
            for(int thread = 0; thread < numThreads; thread++){
                allCounters[thread] = new PiEvenOddImprovement();
                allThreads[thread] =
                        new Thread(
                                new PiEvenOddImprovement(fileName, compressed, thread, numThreads, allCounters[thread]));
                allThreads[thread].start();
            }

            /*join the threads*/
            for(int thread = 0; thread < numThreads; thread++){
                allThreads[thread].join();
            }

            int even= 0;
            int odd = 0;
            for(int thread = 0; thread < numThreads; thread++){
                even += allCounters[thread].getEven();
                odd += allCounters[thread].getOdd();
            }
            /*reap the rewards*/
            System.out.printf("Ratio of even:odd %d:%d = %.8f\n\n"
                    , even
                    , odd
                    , (even * 1.0) / (odd * 1.0)
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

