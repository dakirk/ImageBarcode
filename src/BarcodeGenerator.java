/**
 * This class generates a barcode from an ArrayList of colors.
 * 
 * @author David Kirk
 * @version 1.3.1
 * @since 1.2
 */

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Map;

public class BarcodeGenerator {


	public BarcodeGenerator() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * This method generates a barcode image given an ArrayList of colors
	 * @param avgColorList the given color list
	 * @param sortOption the sorting order for the colors
	 * @param enhanceOption the enhancement setting for the colors
	 * @param barWidth the width of each bar in the barcode (in pixels)
	 * @param imgHeight the height of the barcode (in pixels)
	 * @return the finished barcode as a png file
	 */
	public static ArrayList<Map.Entry<BufferedImage, String>> generate(ArrayList<Map.Entry<Color, String>> avgColorList, String sortOption, String enhanceOption, int barWidth, int imgHeight) {
				
		//sort the arraylist if necessary
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
    	
    	//generate the image
    	ArrayList<Map.Entry<BufferedImage, String>> output = createBarcode(avgColorList, barWidth, imgHeight);
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
	 * @return ArrayList<Color> the adjusted arraylist
	 */
	public static ArrayList<Map.Entry<Color, String>> adjustHSB(ArrayList<Map.Entry<Color, String>> sortedColorList, Double hue, Double saturation, Double brightness) {
		
		double finalHue;
		double finalSaturation;
		double finalBrightness;

		for (int i = 0; i < sortedColorList.size(); i++) {
			
			Color currColor = sortedColorList.get(i).getKey();
			
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
			
			sortedColorList.set(i, new AbstractMap.SimpleEntry<Color, String>(Color.getHSBColor((float)finalHue, (float)finalSaturation, (float)finalBrightness), sortedColorList.get(i).getValue()));

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
	public static ArrayList<Map.Entry<BufferedImage, String>> createBarcode(ArrayList<Map.Entry<Color, String>> modifiedColorList, int stripeWidth, int imgHeight) {
		ArrayList<Map.Entry<BufferedImage, String>> imgList = new ArrayList<Map.Entry<BufferedImage, String>>();
		
		for (int i = 0; i < modifiedColorList.size(); i++) {
			BufferedImage stripe = new BufferedImage(stripeWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
			Graphics2D g2d = stripe.createGraphics();
			
			Color currColor = modifiedColorList.get(i).getKey();
			g2d.setColor(currColor);
			g2d.fillRect(0, 0, stripeWidth, imgHeight);
			
			imgList.add(new AbstractMap.SimpleEntry<BufferedImage, String>(stripe, modifiedColorList.get(i).getValue()));
			
			g2d.dispose();
		}
		
		return imgList;
		
	}

	//sorts color list by hue, saturation, or brightness
	/**
	 * This method sorts the avgColorList by hue, saturation, or brightness, in decreasing order.
	 * @param sortVar The type of sort to be executed. Currently supports "hue", "saturation", and "brightness"
	 * @return ArrayList<Color> the sorted arraylist
	 */
	public static ArrayList<Map.Entry<Color, String>> sortAvgColorList(ArrayList<Map.Entry<Color, String>> avgColorList, String sortVar) {
		
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
		
		avgColorList.sort(new Comparator<Map.Entry<Color, String>>() {
		    @Override
		    public int compare(Map.Entry<Color, String> o1, Map.Entry<Color, String> o2) {
		    	Color c1 = o1.getKey();
		    	Color c2 = o2.getKey();
		    	float[] o1HSB = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
		    	float[] o2HSB = Color.RGBtoHSB(c2.getRed(), c2.getGreen(), c2.getBlue(), null);

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
