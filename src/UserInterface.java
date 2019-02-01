import java.awt.event.*;
import java.util.concurrent.TimeUnit;
import java.awt.*;
import javax.swing.*;
public class UserInterface extends JFrame implements ActionListener {
	
	
	String[] sortOptionsNoJSON = {"Hue", "Saturation", "Brightness", "None"};
	String[] sortOptionsJSON = {"Hue", "Saturation", "Brightness", "Chronological"};
	String[] enhanceOptions = {"Hue", "Saturation", "Brightness", "None"};
	
	static String sortOption;
	static String enhanceOption;
	
	static boolean hasJSON;
	
	static JLabel descriptionLabelSort, descriptionLabelEnhance;
	static JFrame f;
	static JComboBox sortOptionBox, enhanceOptionBox;
	static JButton startButton;
	
	UserInterface() {
		f = new JFrame();
		hasJSON = false;
		
		//sorting drop-down
		descriptionLabelSort = new JLabel("Select sorting method: ");
		if (hasJSON) { 
			sortOptionBox = new JComboBox(sortOptionsJSON);
			sortOptionBox.setSelectedItem("Chronological"); //set default
			sortOption = "Chronological";
		} else {
			sortOptionBox = new JComboBox(sortOptionsNoJSON);
			sortOptionBox.setSelectedItem("Hue"); //set default
			sortOption = "Hue";
		}
		//sortOptionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		sortOptionBox.addActionListener(this);
		JPanel panel1 = new JPanel();
		panel1.add(descriptionLabelSort);
		panel1.add(sortOptionBox);
		
		//enhance drop-down
		descriptionLabelEnhance = new JLabel("Select a value to enhance: ");		
		enhanceOptionBox = new JComboBox(enhanceOptions); //set default
		//enhanceOptionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		enhanceOptionBox.addActionListener(this);
		enhanceOptionBox.setSelectedItem("None");
		enhanceOption = "None";
		JPanel panel2 = new JPanel();
		panel2.add(descriptionLabelEnhance);
		panel2.add(enhanceOptionBox);
		
		startButton = new JButton("Create Barcode");
		startButton.setBounds(130, 100, 100, 40);
		startButton.addActionListener(this);
		JPanel panel3 = new JPanel();
		panel3.add(startButton);
		
		
		
		f.add(panel1);
		f.add(panel2);
		f.add(panel3);
		
		f.setSize(400,  500);
		f.setLayout(new FlowLayout());
		f.setVisible(true);
	}
	
	public void actionPerformed (ActionEvent e) {
		
		//if sort method changes
		if (e.getSource() == sortOptionBox) {
			sortOption = (String)sortOptionBox.getSelectedItem();
			System.out.println(sortOption);
		}
		
		//if enhance method changes
		if (e.getSource() == enhanceOptionBox) {
			enhanceOption = (String)enhanceOptionBox.getSelectedItem();
			System.out.println(enhanceOption);
		}
		
		//start button generates bar code
		if (e.getSource() == startButton) {
			startButton.setText("Loading...");
			startButton.setEnabled(false);
			
			/*
			//create new runnable for barcode generation
			class BarcodeRunnable implements Runnable {
				String internalSortOption;
				String internalEnhanceOption;
				
				BarcodeRunnable(String sort, String enhance) {
					internalSortOption = sort;
					internalEnhanceOption = enhance;
				}
				
				public void run() {
					createBarcode(internalSortOption, internalEnhanceOption);
				}
			}
			
			
			//start thread
			Thread barcodeThread = new Thread(new BarcodeRunnable(sortOption, enhanceOption));
			barcodeThread.start();
			*/
			
			
			createBarcode();
			
			startButton.setText("Create Barcode");
			startButton.setEnabled(true);
			
		}
	}
	
	public void createBarcode() {
		
		BarcodeWorker bworker = new BarcodeWorker(sortOption, enhanceOption, hasJSON);
		
		
		
		
		bworker.execute();
		
		/*
		BarcodeMaker barcodeGen = new BarcodeMaker();

		//load all images to be used
		if (hasJSON) { //if JSON for chronological order is available
	    	barcodeGen.loadImagesChronologically("images/", "images/media.json");
		} else { //otherwise
			barcodeGen.loadImagesFromFolder("testImages/");
		}

		//get average colors for each image
    	barcodeGen.averageAll();

    	System.out.println(sortOpt);
    	
    	//sort if necessary
    	if (!sortOpt.equals("None") && !sortOpt.equals("Chronological")) {
        	System.out.print("\nSorting... ");
    		barcodeGen.sortAvgColorList(sortOpt.toLowerCase());
        	System.out.print("[DONE]");
    	}
    	
    	//enhance one color attribute if requested
    	if (!enhanceOpt.equals("None")) {
        	
    		System.out.print("\nAdjusting HSB values... ");
    		switch(enhanceOpt) {
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

    	//System.out.println("made it here");

    	System.out.print("\nGenerating and saving barcode... ");
    	barcodeGen.createBarcode("./barcode.png", 1);
    	System.out.println("[DONE]");
    	*/
	}
}
