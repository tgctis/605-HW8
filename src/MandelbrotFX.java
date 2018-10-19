import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class MandelbrotFX extends Application {
    
    WritableImage mandelBrotSetImage;
    final int IMG_WIDTH 	= 800;
    final int IMG_HEIGHT 	= 800;
    long milliSeconds;
    
    public void init()  {
        milliSeconds = System.currentTimeMillis();
    }
    public void end(String s)   {
	System.err.println(s + ":       " + ( System.currentTimeMillis() - milliSeconds) + "ms" );
	System.err.println(" # of cores" +   ":       " +
	Runtime.getRuntime().availableProcessors());
    System.err.println(" # of threads" +   ":       " +
            Runtime.getRuntime().availableProcessors() * 2);
    }
    
    public void start(Stage theStage) {
        int numThreads = Runtime.getRuntime().availableProcessors() * 2;
        MandelbrotSet aMandelbrotSet = new MandelbrotSet(IMG_WIDTH, IMG_HEIGHT, numThreads);

        /*
        init();
                mandelBrotSetImage = aMandelbrotSet.createImage();
        end("Single Thread MandelbrotSet Test");
        */

        init();

            mandelBrotSetImage = aMandelbrotSet.createImage();

        end("Multiple Thread MandelbrotSet Test");

        ImageView aImage = new ImageView();        
        aImage.setImage(mandelBrotSetImage);
        
        StackPane root = new StackPane();
        root.getChildren().add(aImage);
        
        Scene scene = new Scene(root, IMG_WIDTH, IMG_HEIGHT);
        
        theStage.setTitle("Mandelbrot Set");
	theStage.setResizable(false);
        theStage.setScene(scene);
        theStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}


class MandelbrotSet extends Thread {
 
    private static final int    MAX_COLORS 	= 256;
    private static final double BOUNDERY = 1000;
    private static int  width;
    private static int  height;
    private int maxH;
    private int maxW;
    private static int numThreads;
    private static WritableImage mandelBrotSetImage;
    private static PixelWriter aPixelWriter;
    private static final Color[] colors = new Color[MAX_COLORS];
    private static double minR  = -2.4;
    private static double maxR  = 0.9;
    private static double minI  = -1.3;
    private static double maxI  = 1.28;

    static {
        for (int index = 0; index < colors.length; index++) {
            colors[index] = Color.RED.interpolate(Color.BLUE, (( 1.0 / colors.length) * index) );
        }
    }

    public MandelbrotSet(PixelWriter px, int maxW, int maxH) {
        this.aPixelWriter = px;
        this.maxH = maxH;
        this.maxW = maxW;
    }
    public MandelbrotSet(int width,int height, int numThreads) {
        this.width = width;
        this.height = height;
        this.numThreads = numThreads;
        mandelBrotSetImage = new WritableImage(width, height);
    }
    private Color getColor(int count) {
	    return count >= colors.length ?  Color.BLACK : colors[count];
    }
    private int calc(double re, double img ) {
        int    counter = 0;
        double length;
        double aComplexNumberRe = 0;
        double aComplexNumberImg = 0;
        double real = 0;
        double imaginary = 0;
        do {
            real       =  aComplexNumberRe * aComplexNumberRe -
             aComplexNumberImg * aComplexNumberImg;
            imaginary  = aComplexNumberRe *  aComplexNumberImg +
             aComplexNumberImg *  aComplexNumberRe;
            aComplexNumberRe   = real;
            aComplexNumberImg  = imaginary;
            aComplexNumberRe   += re;
            aComplexNumberImg  += img;
            length = aComplexNumberImg * aComplexNumberImg +
             aComplexNumberRe * aComplexNumberRe;
            counter++;
        } while (counter < MAX_COLORS && ( length < BOUNDERY ) );
        return counter;
    }
    public Color determineColor(int x, int y)	{
	    double re = (minR * (width - x) + x * maxR) / width;
        double img = (minI * (height - y) + y * maxI) / height;
	    return getColor(calc(re, img));
    }
    public WritableImage createImage()	{
	    mandelBrotSetImage = new WritableImage(width, height);
        aPixelWriter = mandelBrotSetImage.getPixelWriter();

        Thread[] allThreads = new Thread[numThreads];
        int perThreadHeight = height/numThreads;

        for(int thread = 0; thread < numThreads; thread++){
            allThreads[thread] = new Thread(new MandelbrotSet(aPixelWriter, width, perThreadHeight * (thread+1)));
            allThreads[thread].start();
        }
        try{
            for(int thread = 0; thread < numThreads; thread++) {
                allThreads[thread].join();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
	    return mandelBrotSetImage;
    }

    public void run(){
        /*For my machine this does 50x50 squares... not exactly what I'm looking for here*/
        for (int x = 0; x < maxW; x++) {
            for (int y = (maxH - (height/numThreads)); y < maxH; y++) {
                aPixelWriter.setColor(x, y, determineColor(x, y));
            }
        }
    }
}
 
