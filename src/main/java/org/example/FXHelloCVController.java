package org.example;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.QRCodeDetector;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import static java.lang.Thread.sleep;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 *
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @author <a href="http://max-z.de">Maximilian Zuleger</a> (minor fixes)
 * @version 2.0 (2016-09-17)
 * @since 1.0 (2013-10-20)
 *
 */
public class FXHelloCVController implements Initializable
{
    // the FXML button
    @FXML
    private Button button;
    // the FXML image view
    @FXML
    private ImageView currentFrame;

    @FXML
    private ImageView processedFrame;

    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private Slider slider1;
    @FXML
    private Slider slider2;

    double val1 = 153;
    double val2 = 255;



    @Override
    public void initialize(URL location, ResourceBundle resources) {
        slider1.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                val1 = slider1.getValue();
                label1.setText(String.valueOf(val1));
            }
        });
        slider2.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                val2 = slider2.getValue();
                label2.setText(String.valueOf(val2));
            }
        });
    }



    // a timer for acquiring the video stream
    private ScheduledExecutorService timer;
    // the OpenCV object that realizes the video capture
    private final VideoCapture capture = new VideoCapture();
    // a flag to change the button behavior
    private boolean cameraActive = false;
    // the id of the camera to be used
    private static final int cameraId = 0;

    Mat image;
    Mat image2;

    QRCodeDetector qrDecoder;

    /**
     * The action triggered by pushing the button on the GUI
     *
     * @param event
     *            the push button event
     */
    @FXML
    protected void startCamera(ActionEvent event)
    {
        Imgcodecs img = new Imgcodecs();
        String path = "C:/Users/maxim/Pictures/green2.jpg";
        image = img.imread(path);
        image2 = img.imread(path);
        if (!this.cameraActive)
        {
            // start the video capture
            this.capture.open(cameraId);

            // is the video stream available?
            if (this.capture.isOpened())
            {
                this.cameraActive = true;
                qrDecoder = new QRCodeDetector();




                // grab a frame every 33 ms (30 frames/sec)
                Runnable frameGrabber = new Runnable() {

                    @Override
                    public void run()
                    {
                        // effectively grab and process a single frame
                        Mat frame = grabFrame();
                        // convert and show the frame
                        Image imageToShow = Utils.mat2Image(image);
                        updateImageView(currentFrame, imageToShow);
                        Mat frame2 = grabFrame2();
                        Image imageToShow2 = Utils.mat2Image(image2);
                        updateImageView(processedFrame, imageToShow2);
                    }
                };

                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

                // update the button content
                this.button.setText("Stop Camera");
            }
            else
            {
                // log the error
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else
        {
            // the camera is not active at this point
            this.cameraActive = false;
            // update again the button content
            this.button.setText("Start Camera");

            // stop the timer
            this.stopAcquisition();
        }
    }

    /**
     * Get a frame from the opened video stream (if any)
     *
     * @return the {@link Mat} to show
     */
    private Mat grabFrame()
    {
        // init everything
        Mat frame = new Mat();
        Mat points = new Mat();
        Mat toProcess  =frame;

        Imgproc.GaussianBlur(image, new Mat(), new Size(5,5), 1);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(image, image, val1, val2);

        // check if the capture is open
        if (this.capture.isOpened())
        {
            try
            {
                // read the current frame
                this.capture.read(frame);
               // Mat sub = frame.submat(new Rect(new Point(50,50), new Point(400,400)));
                Imgproc.rectangle(frame, new Point(50,50), new Point(400,400), new Scalar(0,0,255), 2);





                // if the frame is not empty, process it
                if (!frame.empty())
                {



                    
                }

            }
            catch (Exception e)
            {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return image;
    }
    private Mat grabFrame2()
    {
        // init everything
        Mat frame = new Mat();
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Point submatPoint1 = new Point(0,105);
        Point submatPoint2 = new Point(image.width(),335);
        Rect submatRect = new Rect(submatPoint1, submatPoint2);
        Mat initSubmat = image.submat(submatRect);
        Scalar submatColor = new Scalar(255,0,0);
        Imgproc.rectangle(image2, submatRect, submatColor, 2);
        Imgproc.findContours(image, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE);
        Scalar color = new Scalar(0, 255, 0);

        int peri;
        int numFound = 0;
        int colorAvgRed = 0;
        int colorAvgBlue = 0;
        Mat detectedSubmat;
        MatOfPoint2f approx = new MatOfPoint2f();
        for(MatOfPoint p : contours){
            peri = (int) Imgproc.arcLength(new MatOfPoint2f(p.toArray()), true);
            Imgproc.approxPolyDP(new MatOfPoint2f(p.toArray()), approx, 0.02 * peri, true);
            if(approx.toArray().length == 4){
                //System.out.println("Found a rectangle");
                //Imgproc.drawContours(image2, contours, -1, color, 2);
                Rect r = Imgproc.boundingRect(p);
                if(submatRect.contains(r.tl()) && submatRect.contains(r.br()) && r.area() > 3000 && r.area() < 10000){
                    Imgproc.rectangle(image2,r, new Scalar(0, 0, 255), 2);
                    detectedSubmat = image2.submat(r);
                   // Mat yCrCb = new Mat();
                   // Mat cB = new Mat();
                   // Mat cR = new Mat();
                    //Imgproc.cvtColor(submat, yCrCb, Imgproc.COLOR_BGR2YCrCb);
                    //Core.extractChannel(yCrCb, cB, 1);
                   // Core.extractChannel(yCrCb, cR, 2);
                   // int colorAvg1 = (int) Core.mean(cB).val[0];
                   // int colorAvg2 = (int) Core.mean(cR).val[0];
                    colorAvgRed = (int) Core.mean(detectedSubmat).val[2];
                    colorAvgBlue = (int) Core.mean(detectedSubmat).val[0];
                    //System.out.println(r.tl());
                    numFound++;
                    Imgproc.putText(image2, Integer.toString(colorAvgRed), r.tl(), Imgproc.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 255), 2);
                    Imgproc.putText(image2, Integer.toString(colorAvgBlue), r.br(), Imgproc.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 255), 2);
                    //Imgproc.putText(image2, Double.toString(r.area()), r.tl(), Imgproc.FONT_HERSHEY_COMPLEX, 1, new Scalar(0, 0, 255), 2);

                }

            }
        }


        // check if the capture is open
        if (this.capture.isOpened())
        {
            try
            {
                // read the current frame
                this.capture.read(frame);
                // Mat sub = frame.submat(new Rect(new Point(50,50), new Point(400,400)));
                Imgproc.rectangle(frame, new Point(50,50), new Point(400,400), new Scalar(0,0,255), 2);





                // if the frame is not empty, process it
                if (!frame.empty())
                {



                }

            }
            catch (Exception e)
            {
                // log the error
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        if (numFound == 1){
            if(colorAvgRed > 100){
                System.out.println("Red");
            }
            else if(colorAvgBlue > 80){
                System.out.println("Blue");
            }
            else{
                System.out.println("Green");
            }
        }
        else {
            System.out.println("Too many rectangles " + numFound);
        }

        return frame;
    }

    /**
     * Stop the acquisition from the camera and release all the resources
     */
    private void stopAcquisition()
    {
        if (this.timer!=null && !this.timer.isShutdown())
        {
            try
            {
                // stop the timer
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e)
            {
                // log any exception
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened())
        {
            // release the camera
            this.capture.release();
        }
    }

    /**
     * Update the {@link ImageView} in the JavaFX main thread
     *
     * @param view
     *            the {@link ImageView} to update
     * @param image
     *            the {@link Image} to show
     */
    private void updateImageView(ImageView view, Image image)
    {
        Utils.onFXThread(view.imageProperty(), image);
    }

    /**
     * On application close, stop the acquisition from the camera
     */
    protected void setClosed()
    {
        this.stopAcquisition();
    }


}