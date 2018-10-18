import java.io.*;
import java.sql.SQLOutput;
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
public class PiEvenOddImprovement extends Thread{
    private String fileName;
    private InputStream byteStream;
    private boolean compressed;
    private int streamLength;
    private int even = 0;
    private int odd = 0;
    private byte[] streamArray;
    private boolean usingStdin = false;
    private int numThreads;
    private int threadNum;
    private PiEvenOddImprovement counter;
    public static final int MAX_STREAM_LENGTH = 65536;
//    public static final int MAX_STREAM_LENGTH = 30;

    public PiEvenOddImprovement(){
        //final counter
    }

    public PiEvenOddImprovement(byte[] streamArray, int threadNum, int numThreads, PiEvenOddImprovement counter){
        this.numThreads = numThreads;
        this.threadNum = threadNum;
        this.counter = counter;
        this.streamLength = streamArray.length;
        this.streamArray = streamArray;
        this.usingStdin = true;
//        System.out.println("Thread #" + threadNum);
    }

    public PiEvenOddImprovement(String fileName, boolean compressed, int threadNum, int numThreads, PiEvenOddImprovement counter){
        this.fileName = fileName;
        this.compressed = compressed;
        this.numThreads = numThreads;
        this.threadNum = threadNum;
        this.counter = counter;
        System.out.println("Thread #" + threadNum);
    }

    /**
     * Opens the file and does some testing
     * @return boolean of readability
     */
    private boolean openFile(){
        //assume Stdin is actually open...
        if(this.usingStdin){
            return this.streamLength > 0;
        }

        //Try if we're using a file stream
        try {
            FileInputStream readFile = new FileInputStream(this.fileName);
            if(!this.compressed)
                this.byteStream = new BufferedInputStream(readFile);
            else
                this.byteStream = new GZIPInputStream(readFile, MAX_STREAM_LENGTH);
//                byteStream = new GZIPInputStream(new BufferedInputStream(readFile), 5000);
//                byteStream = new BufferedInputStream(new GZIPInputStream(readFile));

            if(this.byteStream.available() == 0)
                throw new EmptyFileException("This is an empty file.");
            else
                return true;
        }catch(FileNotFoundException e) {
            System.out.println("File '" +this.fileName+ "' not found.");
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
    private void readAndUpdateCounts(){
        try{
            int loopNum = 0;
            int numChars = byteStream.available();
            int totalBytesRead = 0;

            if(compressed) //we don't know how large the file is...
                numChars = 1000000000;

            //how many threads do we have? and how many characters should each process?
            int charPerThread = numChars/numThreads;

            //set up the stream
            streamLength = MAX_STREAM_LENGTH;
            streamArray = new byte[streamLength];

            //Start reading after the last thread will be done
            if(threadNum > 0){
                byteStream.skip(threadNum*charPerThread);
            }

            //Tracking...
            System.out.println("I am thread #" + threadNum
                    + " I'm reading chars from " + (threadNum*charPerThread)
                    + " to " + ((threadNum+1) * charPerThread));

            //How do we know when to stop reading?
            int bytesRead = byteStream.read(streamArray, 0, streamLength);
            //read until there are no more bytes to read or the current thread should terminate
            while(bytesRead > 0 && totalBytesRead < charPerThread){
                for(int index = 0; index < bytesRead && (index + totalBytesRead < charPerThread); index++){
                    int myChar = streamArray[index];
                    //check that it is a number
                    if(myChar != 0 && myChar != '\n' && myChar != '\r') {
                        try{
                            count(myChar);
                        }catch(NoNumbersException e){
                            System.err.println("Improper Number Format.");
                        }
                    }
                }
                totalBytesRead += bytesRead;
                bytesRead = byteStream.read(streamArray, 0, streamLength);
                loopNum++;
            }

            //Tracking...
            System.out.println("Thread #" + threadNum
                    + " completed " + loopNum + " Loops and read "
                    + totalBytesRead + " Bytes!");
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    public void readArrayAndCount(){
//        System.out.println("Thread #" + this.threadNum + " is reading in " + this.streamArray.length + " bytes.");
        for(int myChar : this.streamArray){
            try{
                if((char)myChar != '.' && (char)myChar != 0){
                    count(myChar);
                }
            }catch(NoNumbersException e){
                System.err.println(e.getMessage());
            }
        }
//        System.out.println("Thread #" + this.threadNum + " has finished with even:" + this.counter.getEven() + " and odd: " + this.counter.getOdd());
    }

    /**
     * Counts the actual odd/even numbers
     * @param myChar the byte read in
     * @throws NoNumbersException If whatever is passed in is not a number
     */
    public void count(int myChar) throws NoNumbersException{
        try{
            int num = Integer.parseInt(String.valueOf((char)myChar));
            if(num%2 == 1)
                this.counter.addOdd();
            else
                this.counter.addEven();

        }catch(NumberFormatException e){
            System.err.println("Erroneous value = " + myChar);
            throw new NoNumbersException("Input was not a number");
        }
    }

    /**
     * Runs the thread
     */
    public void run(){
        if(openFile() && !this.usingStdin) {
            readAndUpdateCounts();
        }else if(openFile() && this.usingStdin){
            readArrayAndCount();
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
        int numThreads = 8;
        Thread[] allThreads;
        PiEvenOddImprovement[] allCounters;
        String fileName = "";
        boolean compressed = false;

        byte[] byteArray = new byte[PiEvenOddImprovement.MAX_STREAM_LENGTH];
        InputStreamReader byteStream;
        boolean threaded = false;

        //If there's a param, it should be a file name, otherwise use stdin
        if(args.length == 1){
            fileName = args[0];
        }

        allThreads = new Thread[numThreads];
        allCounters = new PiEvenOddImprovement[numThreads];

        try {
            //use stdin
            if(fileName.length() == 0){
                 byteStream = new InputStreamReader(System.in);

                 int totalBytes = 0;
                 int byteCount = 0;
                 int inputByte = 0;
                 //generate all the counters
                for(int thread = 0; thread < numThreads; thread++){
                    allCounters[thread] = new PiEvenOddImprovement();
                }

                 //while there's something to read...
                 while((inputByte = byteStream.read()) != -1){
                     if(byteCount < MAX_STREAM_LENGTH - 1){
                         //keep adding to our own "buffer"
                         byteArray[byteCount] = (byte)inputByte;
                         byteCount++;
                     }else if(byteCount == MAX_STREAM_LENGTH - 1){
//                         System.out.println("New Buffer");
//                         System.out.println("Total Bytes So Far: " + totalBytes);
                         byteArray[byteCount] = (byte)inputByte;
                         byteCount = 0;
//                         threaded = false;
                         //purge the buffer and send it to a thread
                         //what thread do we send it to?
                         //keep checking... all threads may be full and we'll have to wait
                         while(!threaded){
                             for(int thread = 0; thread < numThreads; thread++){
//                                 System.out.println("Trying thread #" + thread);
                                 //if a thread is null it has died and should be "renewed"
                                 if(allThreads[thread] == null || allThreads[thread].getState() == State.TERMINATED){
                                     allThreads[thread] =
                                             new Thread(
                                                     new PiEvenOddImprovement(byteArray
                                                             , thread, numThreads, allCounters[thread]));
                                     threaded = true;
                                     allThreads[thread].start();
//                                     System.out.println("State: " + allThreads[thread].getState());
                                     thread = numThreads;
                                 }
                                 else{
//                                     System.out.println("Thread #" + thread + " is busy, trying the next...");
                                     threaded = false;
                                 }
                             }
                         }
                         byteArray = new byte[PiEvenOddImprovement.MAX_STREAM_LENGTH];
                     }
                     totalBytes++;
                     threaded = false;
                 }

                //join all the threads to make sure they're all completed
                for(int thread = 0; thread < numThreads; thread++){
                    if(allThreads[thread] != null)
                        allThreads[thread].join();
                }

                 /**FINAL COUNTER**/
                allThreads[0] =
                        new Thread(
                                new PiEvenOddImprovement(byteArray
                                        , 0, numThreads, allCounters[0]));
                allThreads[0].start();
                allThreads[0].join();
                 /**END FINAL COUNTER**/
            }else{
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
            e.printStackTrace();
            System.err.println("Error");
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

