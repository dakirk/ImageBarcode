
public class Controller {

	public static void main(String[] args) {
		//InstagramScraper scraper = new InstagramScraper("asher.schachter");

		//GENERATE BARCODE FROM GIVEN IMAGES
		//TODO: rig loadImages to take the URL list instead of a directory
		
        BarcodeMaker barcodeGen = new BarcodeMaker();

		//load all images to be used
    	barcodeGen.loadImages("images/", "images/media.json");

    	//
    	barcodeGen.averageAll();

    	System.out.print("\nSorting... ");
    	barcodeGen.sortAvgColorList();
    	System.out.print("[DONE]");

    	//System.out.println("made it here");

    	System.out.print("\nGenerating and saving barcode... ");
    	barcodeGen.createBarcode("./barcode.png", 1);
    	System.out.println("[DONE]");

	}

}
