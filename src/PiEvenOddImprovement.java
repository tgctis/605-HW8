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
    private final int MAX_STREAM_LENGTH = 65536;

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
                byteStream = new GZIPInputStream(readFile, MAX_STREAM_LENGTH);
//                byteStream = new GZIPInputStream(new BufferedInputStream(readFile), 5000);
//                byteStream = new BufferedInputStream(new GZIPInputStream(readFile));

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
            int totalBytesRead = 0;

            if(compressed) //we don't know how large the file is...
                numChars = 1000000000;

            //how many threads do we have? and how many characters should each process?
            int charPerThread = numChars/numThreads;

//            if(charPerThread > MAX_STREAM_LENGTH)
//                streamLength = MAX_STREAM_LENGTH;
//            else{
//                streamLength = charPerThread;
//            }
            streamLength = MAX_STREAM_LENGTH;
            streamArray = new byte[streamLength];

            if(threadNum == 0) {
                byteStream.skip(2);
                counter.addOdd();
            }else{
                byteStream.skip(2+(threadNum*charPerThread));
            }

            System.out.println("I am thread #" + threadNum
                    + " I'm reading chars from " + (threadNum*charPerThread)
                    + " to " + ((threadNum+1) * charPerThread));
            int bytesRead = byteStream.read(streamArray, 0, streamLength);
            while(bytesRead > 0 && totalBytesRead < charPerThread){
                for(int index = 0; index < bytesRead && (index + totalBytesRead < charPerThread); index++){
                    int myChar = streamArray[index];
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
                totalBytesRead += bytesRead;
                bytesRead = byteStream.read(streamArray, 0, streamLength);
                loopNum++;
            }
            System.out.println("Thread #" + threadNum
                    + " completed " + loopNum + " Loops and read "
                    + totalBytesRead + " Bytes!");
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
            if(fileName.matches(".*\\.gz"))
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

