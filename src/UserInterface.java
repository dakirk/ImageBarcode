/**
 * This class renders the user interface of this program using Java's built-in Swing elements.
 * 
 * @author David Kirk
 * @version 1.2
 * @since 1.0
 */

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserInterface extends JFrame implements ActionListener {
	
	final int panelWidth = 400; //preferred panel width
	final int panelHeight = 60; //preferred panel height
	final int numPanelRows = 7; //number of rows of panels
	
	String[] sortOptionsNoJSON = {"Hue", "Saturation", "Brightness", "None"};
	String[] sortOptionsJSON = {"Hue", "Saturation", "Brightness", "Chronological"};
	String[] enhanceOptions = {"Hue", "Saturation", "Brightness", "None"};
	
	String sortOption, enhanceOption;
	int barWidth, imgHeight;
	String imgPath, jsonPath, savePath;
	boolean hasJSON;
	
	JLabel descriptionLabelSort, descriptionLabelEnhance, barWidthBoxLabel, imgHeightBoxLabel, imgPathText, jsonPathText, savePathText, progLabel;
	JFrame f;
	JComboBox sortOptionBox, enhanceOptionBox;
	JTextField barWidthBox, imgHeightBox;
	JButton loadButton, genButton, imgPathChooseButton, jsonPathChooseButton, jsonPathClearButton, savePathChooseButton;
	JFileChooser imgPathChooser, jsonPathChooser, savePathChooser;
	JPanel imgPanel;
	JProgressBar progBar;
	
	ArrayList<Map.Entry<Color, String>> colorList;
	ArrayList<Map.Entry<BufferedImage, String>> barList;
	BufferedImage barcode;
	 
	UserInterface() {
		f = new JFrame();
		hasJSON = false;
		f.setLayout(new FlowLayout());

		Border lineBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		
		
		
		//drop-downs
		
		JPanel panel1 = new JPanel(new GridLayout(2, 2, 0, 0));
		descriptionLabelSort = new JLabel(" Select sorting method: ");
		descriptionLabelSort.setForeground(Color.gray);

		descriptionLabelSort.setOpaque(false);
		panel1.add(descriptionLabelSort);
		
		descriptionLabelEnhance = new JLabel(" Select a value to enhance: ");	
		descriptionLabelEnhance.setOpaque(false);
		descriptionLabelEnhance.setForeground(Color.gray);
		panel1.add(descriptionLabelEnhance);
		
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
		sortOptionBox.setEnabled(false);	

		
		panel1.add(sortOptionBox);
		
		//enhance drop-down
		enhanceOptionBox = new JComboBox(enhanceOptions); //set default
		//enhanceOptionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		enhanceOptionBox.addActionListener(this);
		enhanceOptionBox.setSelectedItem("None");
		enhanceOption = "None";
		enhanceOptionBox.setEnabled(false);
		
		panel1.add(enhanceOptionBox);
		panel1.setBorder(lineBorder);
		panel1.setPreferredSize(new Dimension(400, panelHeight));


		
		//text boxes
		JPanel panel2 = new JPanel();
		barWidthBoxLabel = new JLabel(" Bar width: ");
		barWidthBoxLabel.setForeground(Color.gray);
		imgHeightBoxLabel = new JLabel(" Image Height: ");
		imgHeightBoxLabel.setForeground(Color.gray);
		
		barWidthBox = new JTextField("1");
		barWidthBox.setEnabled(false);
		barWidth = 1;
		imgHeightBox = new JTextField("100");
		imgHeightBox.setEnabled(false);
		imgHeight = 100;
		
		panel2.add(barWidthBoxLabel);
		panel2.add(imgHeightBoxLabel);
		panel2.add(barWidthBox);
		panel2.add(imgHeightBox);
		panel2.setLayout(new GridLayout(2, 2, 0, 0));
		panel2.setBorder(lineBorder);
		panel2.setPreferredSize(new Dimension(panelWidth, panelHeight));

		
		//used in upcoming panels
		File workingDirectory = new File(System.getProperty("user.dir"));
		
		//panel for selecting file paths
		JPanel panel3 = new JPanel();
		JLabel imgPathTextLabel = new JLabel(" Location of images: ");
		
		//sub-panel with text box and file select button
		JPanel panel3a = new JPanel();
		imgPath = "images/";
		imgPathText = new JLabel(" images/");
		imgPathChooseButton = new JButton("Change");
		imgPathChooseButton.addActionListener(this);
		imgPathChooseButton.setOpaque(false);
		imgPathChooser = new JFileChooser(workingDirectory);
		imgPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		panel3a.add(imgPathText);
		panel3a.add(imgPathChooseButton);
		panel3a.setLayout(new GridLayout(1, 2, 0, 0));
		
		panel3.add(imgPathTextLabel);
		panel3.add(panel3a);
		panel3.setLayout(new GridLayout(2, 1, 0, 0));
		panel3.setBorder(lineBorder);
		panel3.setPreferredSize(new Dimension(panelWidth, panelHeight));

		
		//sub-panel with text box and file select button
		JPanel panel4 = new JPanel();
		JLabel jsonPathTextLabel = new JLabel(" Location of the JSON file: ");

		JPanel panel4a = new JPanel();

		JPanel panel4b = new JPanel();
		
		jsonPath = "";
		jsonPathText = new JLabel(" None selected");
		jsonPathText.setForeground(Color.gray);
		jsonPathChooseButton = new JButton("Change");
		jsonPathChooseButton.addActionListener(this);
		jsonPathClearButton = new JButton("Clear");
		jsonPathClearButton.addActionListener(this);
		jsonPathChooser = new JFileChooser(workingDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON FILES", "json");
		jsonPathChooser.setFileFilter(filter);
		jsonPathChooser.setAcceptAllFileFilterUsed(false);
		
		panel4b.add(jsonPathChooseButton);
		panel4b.add(jsonPathClearButton);
		panel4b.setLayout(new GridLayout(1, 2, 0, 0));
		
		panel4a.add(jsonPathText);
		panel4a.add(panel4b);
		panel4a.setLayout(new GridLayout(1, 2, 0, 0));
		
		panel4.add(jsonPathTextLabel);
		panel4.add(panel4a);
		panel4.setLayout(new GridLayout(2, 1, 0, 0));
		panel4.setBorder(lineBorder);
		panel4.setPreferredSize(new Dimension(panelWidth, panelHeight));

		
		//panel for load button
		loadButton = new JButton("Load Images");
		loadButton.addActionListener(this);
		progLabel = new JLabel("Ready", SwingConstants.CENTER);
		JPanel panel6 = new JPanel();
		panel6.setLayout(new GridLayout(2, 1, 0, 0));
		panel6.add(loadButton);
		panel6.add(progLabel);
		panel6.setBorder(lineBorder);
		panel6.setPreferredSize(new Dimension(panelWidth, panelHeight));
		
		//panel for generate button
		genButton = new JButton("Generate Barcode");
		genButton.addActionListener(this);
		genButton.setEnabled(false);
		JLabel genLabel = new JLabel("Ready", SwingConstants.CENTER);
		JPanel panel7 = new JPanel();
		
		savePath = System.getProperty("user.dir") + "barcode.png";
		savePathChooseButton = new JButton("Save Barcode");
		savePathChooseButton.addActionListener(this);
		savePathChooseButton.setEnabled(false);
		savePathChooser = new JFileChooser(workingDirectory);
		savePathChooser.setSelectedFile(new File("barcode.png"));
		
		panel7.setLayout(new GridLayout(2, 1, 0, 0));
		panel7.add(genButton);
		panel7.add(savePathChooseButton);
		panel7.setBorder(lineBorder);
		panel7.setPreferredSize(new Dimension(panelWidth, panelHeight));

		
		//panel for images
		imgPanel = new JPanel();
		imgPanel.setBorder(lineBorder);
		imgPanel.setLayout(new GridBagLayout());

		//scrollbar for large images
		JScrollPane imgScrollPane = new JScrollPane(imgPanel);
		imgScrollPane.setPreferredSize(new Dimension(panelWidth, 200));
		
		
		//left panel is for labels for the various steps
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		
		//populate left panel--for now, 7 parts so i = 7, but may need to change later
		for (int i = 1; i <= numPanelRows; i++) {
			JPanel textPanel = new JPanel();
			textPanel.setLayout(new GridBagLayout());
			JLabel stepLabel;
			
			if (i == numPanelRows) { //for imgPanel
				textPanel.setPreferredSize(new Dimension(panelHeight, 200));
				stepLabel = new JLabel(" Output: ");
			} else {
				textPanel.setPreferredSize(new Dimension(panelHeight, panelHeight));
				stepLabel = new JLabel(" Step " + i + ": ");
			}
			
			stepLabel.setFont(new Font("Dialog", Font.BOLD, 13));
			textPanel.add(stepLabel);
			leftPanel.add(textPanel);
		}
		
		f.add(leftPanel);
		
		
		//right panel is all the controls for the program
		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		
		rightPanel.add(panel3);
		rightPanel.add(panel4);
		rightPanel.add(panel6);
		rightPanel.add(panel1);
		rightPanel.add(panel2);
		rightPanel.add(panel7);
		rightPanel.add(imgScrollPane);
		
		f.add(rightPanel);
		
		f.setSize(500, 600);
		f.setMinimumSize(new Dimension(500, 600));
		//f.setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setVisible(true);
	}
	
	/**
	 * This method is required by the ActionListener interface. It runs every time a UI action occurs.
	 * @param e This is the action that has been performed
	 */
	public void actionPerformed (ActionEvent e) {
		
		//if sort method changes
		if (e.getSource() == sortOptionBox) {
			sortOption = (String)sortOptionBox.getSelectedItem();
			//System.out.println(sortOption);
		}
		
		//if enhance method changes
		if (e.getSource() == enhanceOptionBox) {
			enhanceOption = (String)enhanceOptionBox.getSelectedItem();
			//System.out.println(enhanceOption);
		}
		
		//if the image path's "choose" button is pressed
		if (e.getSource() == imgPathChooseButton) {
			int returnVal = imgPathChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				imgPath = imgPathChooser.getSelectedFile().getPath();
				imgPathText.setText(" " + imgPath);
			}
		}
		
		//if the json path's "choose" button is pressed
		if (e.getSource() == jsonPathChooseButton) {
			int returnVal = jsonPathChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				//if not already marked as having JSON
				sortOptionBox.addItem("Chronological");
				sortOptionBox.removeItem("None");
				hasJSON = true;
				jsonPath = jsonPathChooser.getSelectedFile().getPath();
				jsonPathText.setText(" " + jsonPath);
				jsonPathText.setForeground(Color.black);
			}
		}
		
		//if the json path's "clear" button is pressed
		if (e.getSource() == jsonPathClearButton) {
			sortOptionBox.removeItem("Chronological");
			sortOptionBox.addItem("None");
			hasJSON = false;
			jsonPathText.setText(" None selected");
			jsonPathText.setForeground(Color.gray);
			jsonPath = "";
		}
		
		//if the save path's "choose" button is pressed
		if (e.getSource() == savePathChooseButton) {
			int returnVal = savePathChooser.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				
				//taken from http://jens-na.github.io/2013/11/06/java-how-to-concat-buffered-images/
		        barcode = new BufferedImage(barList.size()*barWidth, imgHeight, BufferedImage.TYPE_INT_RGB);
		        Graphics g = barcode.getGraphics();
		        int x = 0;
		        for(Map.Entry<BufferedImage, String> stripe: barList){
		            g.drawImage(stripe.getKey(), x, 0, null);
		            x += barWidth;
		        }
		        g.dispose();
				
				savePath = savePathChooser.getSelectedFile().getPath();
				System.out.println(savePath);
				//save image
				try {
					File outputFile = new File(savePath);
					ImageIO.write(barcode, "png", outputFile);
				} catch (IOException ex) {
					//System.out.print("\nSaving failed");
					ex.printStackTrace(System.out);

				}
			}
		}
		
		//start button generates bar code
		if (e.getSource() == loadButton) {

			loadButton.setText("Loading...");
			loadButton.setEnabled(false);	
			//System.out.println(imgPath);
			
			loadImages();
			
		}
		
		if (e.getSource() == genButton) {
			
			//check validity of bar width text box
			try {
				int possBarWidth = Integer.parseInt(barWidthBox.getText());
				if (possBarWidth > 0 && possBarWidth <= 100) {
					barWidth = possBarWidth;
				} else {
					throw new NumberFormatException("Invalid bar width");
				}	
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Invalid bar width. Please enter a number between 1 and 100");
				return;
			}
			
			//check validity of image height text box
			try {
				int possImgHeight = Integer.parseInt(imgHeightBox.getText());
				if (possImgHeight > 0 && possImgHeight <= 1000) {
					imgHeight = possImgHeight;
				} else {
					throw new NumberFormatException("Invalid image height width");
				}	
			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Invalid image height. Please enter a number between 1 and 1000");
				return;
			}
			
			//make a deep copy to preserve the original arraylist
			ArrayList<Map.Entry<Color, String>> tempColorList = new ArrayList<Map.Entry<Color, String>>();
			for(Map.Entry<Color, String> pair : colorList) {
				Color c = (Color)pair.getKey(); //not sure why casting necessary
				
				//copy this element (awful syntax)
			    tempColorList.add(new AbstractMap.SimpleEntry<Color, String>(new Color(c.getRed(), c.getGreen(), c.getBlue()), (String)pair.getValue()));
			}
			
			//render barcode
			barList = BarcodeGenerator.generate(tempColorList, sortOption, enhanceOption, barWidth, imgHeight);
			
			imgPanel.removeAll(); //clear previous image
			
			for (int i = 0; i < barList.size(); i++) {
				JLabel picLabel = new JLabel(new ImageIcon(barList.get(i).getKey()));
				picLabel.setToolTipText(barList.get(i).getValue());
				ToolTipManager.sharedInstance().setDismissDelay(Integer.MAX_VALUE);
				ToolTipManager.sharedInstance().setInitialDelay(0);
				ToolTipManager.sharedInstance().setReshowDelay(0);
				//imgPanel.removeAll();
				imgPanel.updateUI();
				imgPanel.add(picLabel);
				//System.out.println("added a new image");
			}
			
			savePathChooseButton.setEnabled(true);
		
		}
	}
	
	/**
	 * This method runs the BarcodeWorker tasks in a new thread, and listens for updates.
	 */
	public void loadImages() {
		
		ImageLoadWorker bworker = new ImageLoadWorker(imgPath, jsonPath, hasJSON, progBar, progLabel);
		
		//determine when to reset button
		bworker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				String property = event.getPropertyName();
				
				if (property.equals("state")) {
					
					//if processing over, reset button
					if ((StateValue)event.getNewValue() == StateValue.DONE) {
						loadButton.setEnabled(true);
						loadButton.setText("Load Images");
						
						
						try {
							ArrayList<Map.Entry<Color, String>> tempColorList = bworker.get();
							
							if (tempColorList.size() > 0) { //if loading was successful
								colorList = tempColorList;
								
								//enable barcode settings
								sortOptionBox.setEnabled(true);
								enhanceOptionBox.setEnabled(true);
								barWidthBox.setEnabled(true);
								imgHeightBox.setEnabled(true);
								genButton.setEnabled(true);
								descriptionLabelSort.setForeground(Color.black);
								descriptionLabelEnhance.setForeground(Color.black);
								barWidthBoxLabel.setForeground(Color.black);
								imgHeightBoxLabel.setForeground(Color.black);
							
								
							} else {
								JOptionPane.showMessageDialog(null, "No images found. If you're using a JSON file, please make sure that it is called \"media.json\" and that you are in the top level of the folder downloaded from Instagram.");
								
							}
							
						} catch (ExecutionException e) {
							JOptionPane.showMessageDialog(null, "No images found. If you're using a JSON file, please make sure that it is called \"media.json\" and that you are in the top level of the folder downloaded from Instagram.");
							e.printStackTrace(System.out);
						} catch (InterruptedException e) {
							JOptionPane.showMessageDialog(null, "Image generation was interrupted.");
						}
					}
				}
				
			}
		});
				
		bworker.execute();		

	}
}
