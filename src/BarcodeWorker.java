import java.awt.image.BufferedImage;

import javax.swing.SwingWorker;

//based on: http://www.javacreed.com/swing-worker-example/
public class BarcodeWorker extends SwingWorker<BufferedImage, String> { 
	
	private static void failIfInterrupted() throws InterruptedException {
		if (Thread.currentThread().isInterrupted()) {
			throw new InterruptedException("Interrupted while generating barcode");
		}
	}
	
	private final String sortOption, enhanceOption; //drop-down menu choices
	private final int barWidth, imgHeight;
	private final boolean hasJSON;
	//private final Integer barWidth, imgHeight; //TODO: add these options
	
	public BarcodeWorker(final String sortOption, final String enhanceOption, final int barWidth, final int imgHeight, final boolean hasJSON) {
		this.sortOption = sortOption;
		this.enhanceOption = enhanceOption;
		this.barWidth = barWidth;
		this.imgHeight = imgHeight;
		this.hasJSON = hasJSON;
	}
	
	@Override
	protected BufferedImage doInBackground() throws Exception {
		
		BarcodeMaker barcodeGen = new BarcodeMaker();

		//load all images to be used
		publish("Loading images...");
		if (hasJSON) { //if JSON for chronological order is available
	    	barcodeGen.loadImagesChronologically("testImages/", "images/media.json");
		} else { //otherwise
			barcodeGen.loadImagesFromFolder("testImages/");
		}
		
		failIfInterrupted();

		//get average colors for each image
		publish("Averaging images...");
    	barcodeGen.averageAll();
    	
    	failIfInterrupted();
    	
    	//sort if necessary
    	
    	if (!sortOption.equals("None") && !sortOption.equals("Chronological")) {
        	publish("Sorting images...");
    		System.out.print("\nSorting... ");
    		barcodeGen.sortAvgColorList(sortOption.toLowerCase());
        	System.out.print("[DONE]");
    	}
    	
    	failIfInterrupted();
    	
    	//enhance one color attribute if requested
    	if (!enhanceOption.equals("None")) {
        	publish("Adjusting HSB values...");
    		System.out.print("\nAdjusting HSB values... ");
    		switch(enhanceOption) {
    			case "Hue": 		barcodeGen.adjustHSB(1.0, null, null);
    								break;
    			case "Saturation":	barcodeGen.adjustHSB(null, 1.0, null);
    								break;
    			case "Brightness":	barcodeGen.adjustHSB(null,  null,  1.0);
    								break;
    			default: 			break;
    		}
        	System.out.print("[DONE]");
    	}
    	
    	failIfInterrupted();

    	//System.out.println("made it here");

    	publish("Generating and saving barcode...");
    	System.out.print("\nGenerating and saving barcode... ");
    	//System.out.println(imgHeight);
    	BufferedImage output = barcodeGen.createBarcode("./barcode.png", barWidth, imgHeight);
    	System.out.println("[DONE]");
    	
    	failIfInterrupted();
    	
    	return output; //successful completion
	}
	
	
	

}
