
public class Controller {

	public static void main(String[] args) {
		//InstagramScraper scraper = new InstagramScraper("asher.schachter");

		//GENERATE BARCODE FROM GIVEN IMAGES
		//TODO: rig loadImages to take the URL list instead of a directory
		
        BarcodeMaker barcodeGen = new BarcodeMaker();

		//load all images to be used
    	barcodeGen.loadImagesChronologically("images/", "images/media.json");

    	barcodeGen.averageAll();

    	System.out.print("\nSorting... ");
    	barcodeGen.sortAvgColorList("saturation");
    	System.out.print("[DONE]");
    	
    	System.out.print("\nAdjusting HSB values... ");
    	barcodeGen.adjustHSB(1.0, 1.0, 1.0);
    	System.out.print("[DONE]");

    	//System.out.println("made it here");

    	System.out.print("\nGenerating and saving barcode... ");
    	barcodeGen.createBarcode("./barcode.png", 1);
    	System.out.println("[DONE]");

	}

}
