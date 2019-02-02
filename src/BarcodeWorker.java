import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
import org.json.JSONObject;
import org.json.JSONTokener;

//based on: http://www.javacreed.com/swing-worker-example/
public class BarcodeWorker extends SwingWorker<BufferedImage, String> { 
	
	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while generating barcode");
		}
	}
	
	private final String sortOption, enhanceOption, imgPath, jsonPath, savePath; //drop-down menu choices
	private final int barWidth, imgHeight;
	private final boolean hasJSON;
	private final JProgressBar progBar;
	private final JLabel progLabel;
	
	private ArrayList<BufferedImage> imgList;
    private ArrayList<Color> avgColorList;
    private BufferedImage output;
	//private final Integer barWidth, imgHeight; //TODO: add these options
	
	public BarcodeWorker(final String sortOption,
						 final String enhanceOption,
						 final int barWidth,
						 final int imgHeight,
						 final String imgPath,
						 final boolean hasJSON,
						 final String jsonPath,
						 final String savePath,
						 final JProgressBar progBar,
						 final JLabel progLabel) {
		this.sortOption = sortOption;
		this.enhanceOption = enhanceOption;
		this.barWidth = barWidth;
		this.imgHeight = imgHeight;
		this.hasJSON = hasJSON;
		this.imgPath = imgPath;
		this.jsonPath = jsonPath;
		this.savePath = savePath;
		this.progBar = progBar;
		this.progLabel = progLabel;
		
		imgList = new ArrayList<BufferedImage>();
    	avgColorList = new ArrayList<Color>();
		
	}
	
	@Override
	protected void process(List<String> chunks) {
		progLabel.setText(chunks.get(chunks.size()-1));
		return;
	}
	
	@Override
	protected void done() {
		progLabel.setText("Done!");
	}
	
	@Override
	protected BufferedImage doInBackground() throws Exception {
		
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
    	
    	failIfInterrupted();
    	
    	//sort if necessary
    	
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
    	
    	return output; //successful completion
	}
	
	
	public void loadImagesFromFolder(String folderPath) {
    	File folder = new File(folderPath);
    	//File[] fileList = folder.listFiles();
    	
    	//get all files in folder and subfolders recursively
    	Collection<File> fileCollection = (FileUtils.listFiles(new File(folderPath), null, true));
    	File[] fileList = fileCollection.toArray(new File[fileCollection.size()]);
    	
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
    
    //loads images given JSON for dates--assumes all images match the JSON file and are in the "images" folder
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
			//String imgName = imgData.getString("path").split("/")[2]; //get 3rd item
			String imgName = imgData.getString("path");
			String imgPath = folderPath + "/" + imgName;

    		//if valid image, read and add to ArrayList
			try {
				//System.out.print("\nLoading " + imgName + ", taken at " + imgData.getString("taken_at") + "... ");
       			imgList.add(ImageIO.read(Files.newInputStream(Paths.get(imgPath))));
       			//System.out.print("[DONE]");
   			} catch (IOException e) {
   				//System.out.print("[FAILED]");
   				e.printStackTrace(System.out);
   			}

		}

    }

    /* Obtained from Stack Overflow here: https://stackoverflow.com/questions/28162488/get-average-color-on-bufferedimage-and-bufferedimage-portion-as-fast-as-possible
	 * Where bi is your image, (x0,y0) is your upper left coordinate, and (w,h)
	 * are your width and height respectively
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
	    
	    //convert to HSB to manipulate hue, saturation, or brightness for neat effects
	    //float[] avgHSB = Color.RGBtoHSB(avgRed, avgGreen, avgBlue, null);
	    //return Color.getHSBColor(avgHSB[0], avgHSB[1], 1);

	    //System.out.println("Red avg: " + avgRed);
	    //System.out.println("Green avg: " + avgGreen);
	    //System.out.println("Blue avg: " + avgBlue);

	    //return new Color(avgRed, avgGreen, avgBlue);
	}

	//finds average of all images in imgList
	public void averageAll() {

		int imgCount = 1;

    	//average each image and make a list of the averages
		int i = 0;
    	for (BufferedImage img : imgList) {
    		
    		publish("Averaging files (" + i + "/" + imgList.size() + ")");
    		
    		//System.out.print("\nAveraging image " + imgCount + " of " + imgList.size() + "... ");
    		avgColorList.add(averageColor(img));
    		//System.out.print("[DONE]");
    		imgCount++;
    		//System.out.println("made an avg");
    		i++;
    	}
	}
	
	//if any argument is null, leave as-is
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

	//must only be run after the images are loaded
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
