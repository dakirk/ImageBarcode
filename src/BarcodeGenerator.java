import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;

import javax.imageio.ImageIO;

public class BarcodeGenerator {


	public BarcodeGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	public static BufferedImage generate(ArrayList<Color> avgColorList, String savePath, String sortOption, String enhanceOption, int barWidth, int imgHeight) {
				
		if (!sortOption.equals("None") && !sortOption.equals("Chronological")) {
    		//System.out.print("\nSorting... ");
    		sortAvgColorList(avgColorList, sortOption.toLowerCase());
        	//System.out.print("[DONE]");
    	}
    	    	
    	//enhance one color attribute if requested
    	if (!enhanceOption.equals("None")) {
    		//System.out.print("\nAdjusting HSB values... ");
    		switch(enhanceOption) {
    			case "Hue": 		adjustHSB(avgColorList, 1.0, null, null);
    								break;
    			case "Saturation":	adjustHSB(avgColorList, null, 1.0, null);
    								break;
    			case "Brightness":	adjustHSB(avgColorList, null,  null,  1.0);
    								break;
    			default: 			break;
    		}
        	//System.out.print("[DONE]");
    	}
    	
    	//System.out.println(imgHeight);
    	BufferedImage output = createBarcode(avgColorList, savePath, barWidth, imgHeight);
    	//System.out.println("[DONE]");
    	
    	return output;
	}
	
	//if any argument is null, leave as-is
	/**
	 * This method modifies the colors in avgColorList according to the HSB color model.
	 * All parameters are values between 0 and 1, and for now there is no input sanitization.
	 * @param hue The new hue for all colors--if null, hues are not adjusted
	 * @param saturation The new saturation for all colors--if null saturations are not adjusted
	 * @param brightness The new brightness for all color--if null, hues are not adjusted
	 */
	public static ArrayList<Color> adjustHSB(ArrayList<Color> sortedColorList, Double hue, Double saturation, Double brightness) {
		
		double finalHue;
		double finalSaturation;
		double finalBrightness;

		for (int i = 0; i < sortedColorList.size(); i++) {
			
			Color currColor = sortedColorList.get(i);
			
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
			
			sortedColorList.set(i, Color.getHSBColor((float)finalHue, (float)finalSaturation, (float)finalBrightness));

		}
		
		return sortedColorList;
		
		
	}

	/**
	 * This method generates and saves the barcode in PNG format using the colors currently saved in avgColorList.
	 * @param savePath The location for the saved barcode image
	 * @param stripeWidth The width of each bar in the barcode, in pixels
	 * @param imgHeight The height of the barcode, in pixels
	 * @return BufferedImage The completed barcode, in a renderable form
	 */
	public static BufferedImage createBarcode(ArrayList<Color> modifiedColorList, String savePath, int stripeWidth, int imgHeight) {

		int numStripes = modifiedColorList.size();

		//initialize new image
		BufferedImage output = new BufferedImage(numStripes*stripeWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2d = output.createGraphics();

		//draw each stripe
		for (int i = 0; i < numStripes; i++) {
			g2d.setColor(modifiedColorList.get(i));
			g2d.fillRect(i*stripeWidth, 0, ((i+1)*stripeWidth), imgHeight);
		}
		
		//save image
		try {
			File outputFile = new File(savePath);
			ImageIO.write(output, "png", outputFile);
		} catch (IOException e) {
			//System.out.print("\nSaving failed");
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
	public static ArrayList<Color> sortAvgColorList(ArrayList<Color> avgColorList, String sortVar) {
		
		int sortIndex;
		
		if (sortVar.equals("hue")) {
			sortIndex = 0;
		} else if (sortVar.equals("saturation")) {
			sortIndex = 1;
		} else if (sortVar.equals("brightness")) {
			sortIndex = 2;
		} else {
			return avgColorList; //if invalid option, return unmodified list
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
			
		return avgColorList;
	}

}
