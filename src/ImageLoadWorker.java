import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * This class imports images from a given directory. It inherits from SwingWorker in order to run
 * in the background.
 * <p>
 * SwingWorker elements based on: http://www.javacreed.com/swing-worker-example/
 * 
 * @author David Kirk
 * @version 1.2
 * @since 1.0
 */
public class ImageLoadWorker extends SwingWorker<ArrayList<Map.Entry<Color, String>>, String> { 
	
	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while generating barcode");
		}
	}
	
	private final String imgPath, jsonPath; //drop-down menu choices
	private final boolean hasJSON;
	private final JProgressBar progBar;
	private final JLabel progLabel;
	
	private ArrayList<String> filenames;
	private ArrayList<BufferedImage> imgList;
    private ArrayList<Color> avgColorList;
    private int loadProgress;
    private int avgProgress;
    
	
	public ImageLoadWorker(final String imgPath,
						   final String jsonPath,
						   final boolean hasJSON,
						   final JProgressBar progBar,
						   final JLabel progLabel) {
		this.imgPath = imgPath;
		this.jsonPath = jsonPath;
		this.hasJSON = hasJSON;

		this.progBar = progBar;
		this.progLabel = progLabel;
		
		imgList = new ArrayList<BufferedImage>();
		filenames = new ArrayList<String>();
    	avgColorList = new ArrayList<Color>();
    	loadProgress = 0;
    	avgProgress = 0;
		
	}
	
	
	/**
	 * This method allows this SwingWorker to return status text.
	 * @param chunks The data produced by the publish() method
	 */
	@Override
	protected void process(List<String> chunks) {
		progBar.setEnabled(true);
		progBar.setString(chunks.get(chunks.size()-1));
		progBar.setValue(loadProgress + avgProgress);
		return;
	}
	/**
	 * This method runs when the doInBackground method finishes. Currently not used.
	 */
	@Override
	protected void done() {
		progBar.setString("Done!");
		progBar.setValue(0);
		progBar.setEnabled(false);
	}
	
	/**
	 * This is the method that runs all of the barcode generation code. It will run in a separate thread
	 * from the Swing UI and therefore will not cause it to freeze.
	 * @return BufferedImage The completed barcode
	 */
	@Override
	protected ArrayList<Map.Entry<Color, String>> doInBackground() throws Exception {
		
		//BarcodeMaker barcodeGen = new BarcodeMaker();

		//load all images to be used
		publish("Loading images...");
		if (hasJSON) { //if JSON for chronological order is available
	    	loadImagesChronologically(imgPath, jsonPath);
		} else { //otherwise
			loadImagesFromFolder(imgPath);
		}
		
		failIfInterrupted();

		//get average colors for each image
		publish("Averaging images...");
    	averageAll();

    	//package colors and info together using Map.Entry as pairs
    	ArrayList<Map.Entry<Color, String>> annotatedColorList = new ArrayList<Map.Entry<Color, String>>();
    	for (int i = 0; i < avgColorList.size(); i++) {
    		Color currColor = avgColorList.get(i);
    		String currAnnotation = filenames.get(i);
    		
    		annotatedColorList.add(new AbstractMap.SimpleEntry<Color, String>(currColor, currAnnotation));
    	}
    	
    	
    	failIfInterrupted();

    	return annotatedColorList; //successful completion
	}
	
	/**
	 * This method loads an unordered set of images from a folder and its subfolders. It
	 * accepts only JPGs and PNGs, and stores them in imgList.
	 * @param folderPath This is the path for the folder containing images to be loaded
	 */
	public void loadImagesFromFolder(String folderPath) {
    	//File[] fileList = folder.listFiles();
    	
    	//get all files in folder and subfolders recursively
    	Collection<File> fileCollection = (FileUtils.listFiles(new File(folderPath), null, true));
    	File[] fileList = fileCollection.toArray(new File[fileCollection.size()]);
    	ImageIO.setUseCache(false);
    	//load all images
    	int i = 0;
    	for (File file : fileList) {
    		int lastFourIndex = file.getName().length()-4;
    		String lastFourChars = file.getName().substring(lastFourIndex);
    		publish("Reading files (" + i + "/" + fileList.length + ")");
    		loadProgress = (int)(50 * ((double)i / (double)fileList.length));

    		//if valid image, read and add to ArrayList
    		if (file.isFile() && (lastFourChars.equals(".jpg") || lastFourChars.equals(".png"))) {
    			try {

    				//System.out.print("\nLoading " + file.getName() + "... ");
    				InputStream tempStream = Files.newInputStream(Paths.get(file.getPath()));    				
    				BufferedImage tempImg = ImageIO.read(tempStream);
    				imgList.add(tempImg);
    				
    				String hoverText = "<html>" + file.getName() + "<br><img src=\"file:" + file.getAbsolutePath() + "\" width=" + tempImg.getWidth()/8 + " height=" + tempImg.getHeight()/8 + "></html>";
    				filenames.add(hoverText);

    				//System.out.println(file.getName());
           			//System.out.print("[DONE]");
       			} catch (IOException e) {
       				//System.out.print("[FAILED]");
       				e.printStackTrace(System.out);
       			}
    		} else {
    			//System.out.print("\nNot an image: " + file.getName());
    		}
    		
    		i++;
    	}
    }
    
	/**
	 * This method loads an ordered set of images as described by the media.json file provided by Instagram image downloads.
	 * It assumes that the relative paths provided by that file are correct, so it will only work when folderPath is the root
	 * folder for the download (usually named "username_dateDownloaded")
	 * @param folderPath The root folder of the Instagram data download
	 * @param jsonPath The path to the media.json file
	 */
    public void loadImagesChronologically(String folderPath, String jsonPath) {

    	ImageIO.setUseCache(false);
    	
    	//read JSON file to get load order
    	BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(jsonPath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
		
    	String jsonText = "";
		String st; 
		try {
			while ((st = br.readLine()) != null) {
				jsonText += st;
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} 
    	
		JSONTokener jsonReader = new JSONTokener(jsonText);
		JSONObject jsonObj = new JSONObject(jsonReader);
		JSONArray jsonArr = jsonObj.getJSONArray("photos");
		
		
		
		for (int i = 0; i < jsonArr.length(); i++) {
			
    		publish("Reading files (" + i + "/" + jsonArr.length() + ")");
    		loadProgress = (int)(50 * ((double)i / (double)jsonArr.length()));

			
			//find file name for this image
			JSONObject imgData = jsonArr.getJSONObject(i);

			//determine hover text
			String imgName = imgData.getString("path");
			String imgCaption = imgData.getString("caption");
			if (!imgCaption.equals("")) imgCaption = "<br>Caption: " + imgCaption;
			String imgDate = imgData.getString("taken_at");
			String imgLocation = "";
			try {
				imgLocation = "<br>Location: " + imgData.getString("location");
			} catch (JSONException jex) {
				//System.out.println("No location for this one");
			}
			String imgPath = folderPath + "/" + imgName;
			
    		//if valid image, read and add to ArrayList
			try {
				//System.out.print("\nLoading " + imgName + ", taken at " + imgData.getString("taken_at") + "... ");
     
				BufferedImage image = ImageIO.read(Files.newInputStream(Paths.get(imgPath)));
				imgList.add(image);
    			String hoverText = "<html>Filename: " + imgName + imgCaption + imgLocation + "<br>Timestamp: " + imgDate + "<br><img src=\"file:" + imgPath + "\"width=" + image.getWidth()/8 + " height=" + image.getHeight()/8 + "></html>";
       			filenames.add(hoverText);
       			//System.out.print("[DONE]");
   			} catch (IOException e) {
   				//System.out.print("[FAILED]");
   				e.printStackTrace(System.out);
   			}

		}

    }

    /**
     * This method averages the colors in a single image for later use in the barcode.
     * Obtained from Stack Overflow here: https://stackoverflow.com/questions/28162488/get-average-color-on-bufferedimage-and-bufferedimage-portion-as-fast-as-possible
     * @param img This is the image whose colors will be averaged
     * @return Color The average color of img
     */
	private Color averageColor(BufferedImage img) {

		int width = img.getWidth();
		int height = img.getHeight();
	    long sumRed = 0, sumGreen = 0, sumBlue = 0;
	    for (int x = 0; x < width; x++) {
	        for (int y = 0; y < height; y++) {
	            Color pixel = new Color(img.getRGB(x, y));
	            sumRed += pixel.getRed();
	            sumGreen += pixel.getGreen();
	            sumBlue += pixel.getBlue();
	        }
	    }
	    int numPixels = width * height;
	    int avgRed = (int)(sumRed/numPixels);
	    int avgGreen = (int)(sumGreen/numPixels);
	    int avgBlue = (int)(sumBlue/numPixels);

	    return new Color(avgRed, avgGreen, avgBlue);
	    
	}

	/**
	 * This method runs the averageColor function on all images in imgList and adds them to an internal
	 * variable avgColorList.
	 */
	public void averageAll() {

    	//average each image and make a list of the averages
		int i = 0;
    	for (BufferedImage img : imgList) {
    		
    		publish("Averaging files (" + i + "/" + imgList.size() + ")");
    		avgProgress = (int)(50 * ((double)i / (double)imgList.size()));

    		
    		//System.out.print("\nAveraging image " + imgCount + " of " + imgList.size() + "... ");
    		avgColorList.add(averageColor(img));
    		//System.out.print("[DONE]");
    		i++;
    	}
	}	
}
