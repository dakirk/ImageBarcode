import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.awt.Graphics2D;
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
    private BufferedImage output;
	
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
		
	}
	
	
	/**
	 * This method allows this SwingWorker to return status text.
	 * @param chunks The data produced by the publish() method
	 */
	@Override
	protected void process(List<String> chunks) {
		progLabel.setText(chunks.get(chunks.size()-1));
		return;
	}
	/**
	 * This method runs when the doInBackground method finishes. Currently not used.
	 */
	@Override
	protected void done() {
		progLabel.setText("Done!");
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
    	
    	//sort if necessary
    	/*
    	if (!sortOption.equals("None") && !sortOption.equals("Chronological")) {
        	publish("Sorting images...");
    		//System.out.print("\nSorting... ");
    		sortAvgColorList(sortOption.toLowerCase());
        	//System.out.print("[DONE]");
    	}
    	
    	failIfInterrupted();
    	
    	//enhance one color attribute if requested
    	if (!enhanceOption.equals("None")) {
        	publish("Adjusting HSB values...");
    		//System.out.print("\nAdjusting HSB values... ");
    		switch(enhanceOption) {
    			case "Hue": 		adjustHSB(1.0, null, null);
    								break;
    			case "Saturation":	adjustHSB(null, 1.0, null);
    								break;
    			case "Brightness":	adjustHSB(null,  null,  1.0);
    								break;
    			default: 			break;
    		}
        	//System.out.print("[DONE]");
    	}
    	
    	failIfInterrupted();

    	//System.out.println("made it here");

    	publish("Generating and saving barcode...");
    	//System.out.print("\nGenerating and saving barcode... ");
    	//System.out.println(imgHeight);
    	BufferedImage output = createBarcode(savePath, barWidth, imgHeight);
    	//System.out.println("[DONE]");
    	
    	failIfInterrupted();
    	*/
    	return annotatedColorList; //successful completion
	}
	
	/**
	 * This method loads an unordered set of images from a folder and its subfolders. It
	 * accepts only JPGs and PNGs, and stores them in imgList.
	 * @param folderPath This is the path for the folder containing images to be loaded
	 */
	public void loadImagesFromFolder(String folderPath) {
    	File folder = new File(folderPath);
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

    		//if valid image, read and add to ArrayList
    		if (file.isFile() && (lastFourChars.equals(".jpg") || lastFourChars.equals(".png"))) {
    			try {

    				//System.out.print("\nLoading " + file.getName() + "... ");
    				InputStream tempStream = Files.newInputStream(Paths.get(file.getPath()));    				
    				BufferedImage tempImg = ImageIO.read(tempStream);
    				imgList.add(tempImg);
    				filenames.add(file.getName());

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
			
			//find file name for this image
			JSONObject imgData = jsonArr.getJSONObject(i);

			//determine hover text
			String imgName = imgData.getString("path");
			String imgCaption = imgData.getString("caption");
			if (!imgCaption.equals("")) imgCaption = "<br>Caption: " + imgCaption;
			String imgDate = imgData.getString("taken_at");
			String imgLocation = "";
			try {
				imgLocation = "<br>Taken at: " + imgData.getString("location");
			} catch (JSONException jex) {
				System.out.println("No location for this one");
			}
			String imgPath = folderPath + "/" + imgName;
			String hoverText = "<html>Filename: " + imgName + imgCaption + imgLocation + "<br>Timestamp: " + imgDate + "</html>";

    		//if valid image, read and add to ArrayList
			try {
				//System.out.print("\nLoading " + imgName + ", taken at " + imgData.getString("taken_at") + "... ");
       			imgList.add(ImageIO.read(Files.newInputStream(Paths.get(imgPath))));
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
    		
    		//System.out.print("\nAveraging image " + imgCount + " of " + imgList.size() + "... ");
    		avgColorList.add(averageColor(img));
    		//System.out.print("[DONE]");
    		i++;
    	}
	}
	
	//if any argument is null, leave as-is
	/**
	 * This method modifies the colors in avgColorList according to the HSB color model.
	 * All parameters are values between 0 and 1, and for now there is no input sanitization.
	 * @param hue The new hue for all colors--if null, hues are not adjusted
	 * @param saturation The new saturation for all colors--if null saturations are not adjusted
	 * @param brightness The new brightness for all color--if null, hues are not adjusted
	 */
	public void adjustHSB(Double hue, Double saturation, Double brightness) {
		
		double finalHue;
		double finalSaturation;
		double finalBrightness;

		for (int i = 0; i < avgColorList.size(); i++) {
			
			Color currColor = avgColorList.get(i);
			
			float[] avgHSB = Color.RGBtoHSB(currColor.getRed(), currColor.getGreen(), currColor.getBlue(), null);
			
			//fix hue if necessary
			if (hue == null) {
				finalHue = avgHSB[0];
			} else {
				finalHue = hue;
			}
			
			//fix saturation if necessary
			if (saturation == null) {
				finalSaturation = avgHSB[1];
			} else {
				finalSaturation = saturation;
			}
			
			//fix brightness if necessary
			if (brightness == null) {
				finalBrightness = avgHSB[2];
			} else {
				finalBrightness = brightness;
			}
			
			avgColorList.set(i, Color.getHSBColor((float)finalHue, (float)finalSaturation, (float)finalBrightness));

		}
		
		
	}

	/**
	 * This method generates and saves the barcode in PNG format using the colors currently saved in avgColorList.
	 * @param savePath The location for the saved barcode image
	 * @param stripeWidth The width of each bar in the barcode, in pixels
	 * @param imgHeight The height of the barcode, in pixels
	 * @return BufferedImage The completed barcode, in a renderable form
	 */
	public BufferedImage createBarcode(String savePath, int stripeWidth, int imgHeight) {

		int numStripes = avgColorList.size();

		//initialize new image
		output = new BufferedImage(numStripes*stripeWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = output.createGraphics();

		//draw each stripe
		for (int i = 0; i < numStripes; i++) {
			g2d.setColor(avgColorList.get(i));
			g2d.fillRect(i*stripeWidth, 0, ((i+1)*stripeWidth), imgHeight);
		}
		
		//save image
		try {
			File outputFile = new File(savePath);
			ImageIO.write(output, "png", outputFile);
		} catch (IOException e) {
			//System.out.print("\nSaving failed");
			publish("Saving failed");
			e.printStackTrace(System.out);

		}
		
		return output;
	}

	//sorts color list by hue, saturation, or brightness
	/**
	 * This method sorts the avgColorList by hue, saturation, or brightness, in decreasing order.
	 * @param sortVar The type of sort to be executed. Currently supports "hue", "saturation", and "brightness"
	 * @return boolean True if input was a valid sort parameter, or false otherwise
	 */
	public boolean sortAvgColorList(String sortVar) {
		
		int sortIndex;
		
		if (sortVar.equals("hue")) {
			sortIndex = 0;
		} else if (sortVar.equals("saturation")) {
			sortIndex = 1;
		} else if (sortVar.equals("brightness")) {
			sortIndex = 2;
		} else {
			return false;
		}
		
		avgColorList.sort(new Comparator<Color>() {
		    @Override
		    public int compare(Color o1, Color o2) {
		    	float[] o1HSB = Color.RGBtoHSB(o1.getRed(), o1.getGreen(), o1.getBlue(), null);
		    	float[] o2HSB = Color.RGBtoHSB(o2.getRed(), o2.getGreen(), o2.getBlue(), null);

		    	if (o1HSB[sortIndex] > o2HSB[sortIndex]) return -1;
		    	else if (o1HSB[sortIndex] == o2HSB[sortIndex]) return 0;
		    	else return 1;

		    	//System.out.println("first HSB: " + o1HSB[0]);
		    	//System.out.println("second HSB: " + o2HSB[0]);
		        //return (int)(10.0*(o1HSB[0] - o2HSB[0]));
    		}
		});
		
		return true;
	}
	
	
	

}
