import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.awt.*;
import javax.swing.*;
import javax.swing.SwingWorker.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
public class UserInterface extends JFrame implements ActionListener {
	
	
	String[] sortOptionsNoJSON = {"Hue", "Saturation", "Brightness", "None"};
	String[] sortOptionsJSON = {"Hue", "Saturation", "Brightness", "Chronological"};
	String[] enhanceOptions = {"Hue", "Saturation", "Brightness", "None"};
	
	String sortOption;
	String enhanceOption;
	int barWidth;
	int imgHeight;
	String imgPath;
	String jsonPath;
	boolean hasJSON;
	
	JLabel descriptionLabelSort, descriptionLabelEnhance;
	JFrame f;
	JComboBox sortOptionBox, enhanceOptionBox;
	JTextField barWidthBox, imgHeightBox, imgPathText, jsonPathText;
	JButton startButton, imgPathChooseButton, jsonPathChooseButton;
	JFileChooser imgPathChooser, jsonPathChooser;
	JPanel imgPanel;
	 
	
	UserInterface() {
		f = new JFrame();
		hasJSON = false;
		
		//drop-downs
		
		JPanel panel1 = new JPanel(new GridLayout(2, 2, 0, 0));
		descriptionLabelSort = new JLabel(" Select sorting method: ");
		descriptionLabelSort.setOpaque(false);
		panel1.add(descriptionLabelSort);
		
		descriptionLabelEnhance = new JLabel(" Select a value to enhance: ");	
		descriptionLabelEnhance.setOpaque(false);
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
		
		panel1.add(sortOptionBox);
		
		//enhance drop-down
		enhanceOptionBox = new JComboBox(enhanceOptions); //set default
		//enhanceOptionBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		enhanceOptionBox.addActionListener(this);
		enhanceOptionBox.setSelectedItem("None");
		enhanceOption = "None";
		
		panel1.add(enhanceOptionBox);
		
		//text boxes
		JPanel panel2 = new JPanel();
		JLabel barWidthBoxLabel = new JLabel(" Bar width: ");
		JLabel imgHeightBoxLabel = new JLabel(" Image Height: ");
		
		barWidthBox = new JTextField("1");
		barWidth = 1;
		imgHeightBox = new JTextField("100");
		imgHeight = 100;
		
		panel2.add(barWidthBoxLabel);
		panel2.add(imgHeightBoxLabel);
		panel2.add(barWidthBox);
		panel2.add(imgHeightBox);
		panel2.setLayout(new GridLayout(2, 2, 1, 1));
		
		
		JPanel panel3 = new JPanel();
		JLabel imgPathTextLabel = new JLabel(" Location of images: ");
		JLabel jsonPathTextLabel = new JLabel(" Location of the JSON file: ");
		
		File workingDirectory = new File(System.getProperty("user.dir"));
		
		//sub-panel with text box and file select button
		JPanel panel3a = new JPanel();
		imgPath = "images/";
		imgPathText = new JTextField("images/");
		imgPathText.setEditable(false);
		imgPathChooseButton = new JButton("Change");
		imgPathChooseButton.addActionListener(this);
		imgPathChooser = new JFileChooser(workingDirectory);
		imgPathChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		panel3a.add(imgPathText);
		panel3a.add(imgPathChooseButton);
		
		panel3a.setLayout(new GridLayout(1, 2, 1, 1));
		
		//sub-panel with text box and file select button
		JPanel panel3b = new JPanel();
		jsonPath = "";
		jsonPathText = new JTextField("");
		jsonPathText.setEditable(false);
		jsonPathChooseButton = new JButton("Change");
		jsonPathChooseButton.addActionListener(this);
		jsonPathChooser = new JFileChooser(workingDirectory);
		FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON FILES", "json");
		jsonPathChooser.setFileFilter(filter);
		jsonPathChooser.setAcceptAllFileFilterUsed(false);
		
		panel3b.add(jsonPathText);
		panel3b.add(jsonPathChooseButton);
		
		panel3b.setLayout(new GridLayout(1, 2, 1, 1));
		
		
		panel3.add(imgPathTextLabel);
		panel3.add(jsonPathTextLabel);
		panel3.add(panel3a);
		panel3.add(panel3b);
		
		panel3.setLayout(new GridLayout(2, 4, 1, 1));
		
		//start button
		startButton = new JButton("Create Barcode");
		//startButton.setBounds(130, 100, 100, 40);
		startButton.addActionListener(this);
		JPanel panel4 = new JPanel();
		panel4.add(startButton);
		
		imgPanel = new JPanel();
		
		f.add(panel1);
		f.add(panel2);
		f.add(panel3);
		f.add(panel4);
		f.add(imgPanel);
		
		f.setSize(400,  300);
		f.setLayout(new GridLayout(5, 1, 0, 0));
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
		
		if (e.getSource() == imgPathChooseButton) {
			int returnVal = imgPathChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				imgPath = imgPathChooser.getSelectedFile().getPath();
				imgPathText.setText(imgPath);
			}
		}
		
		if (e.getSource() == jsonPathChooseButton) {
			int returnVal = jsonPathChooser.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				hasJSON = true;
				sortOptionBox.addItem("Chronological");
				jsonPath = jsonPathChooser.getSelectedFile().getPath();
				jsonPathText.setText(jsonPath);
			}
		}
		
		//start button generates bar code
		if (e.getSource() == startButton) {
			
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
			
			startButton.setText("Loading...");
			startButton.setEnabled(false);	
			System.out.println(imgPath);
			
			createBarcode();
			
			
		}
	}
	
	public void createBarcode() {
		
		BarcodeWorker bworker = new BarcodeWorker(sortOption, enhanceOption, barWidth, imgHeight, imgPath, hasJSON, jsonPath);
		
		//determine when to reset button
		bworker.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(final PropertyChangeEvent event) {
				String property = event.getPropertyName();
				
				if (property.equals("state")) {
					
					//if processing over, reset button
					if ((StateValue)event.getNewValue() == StateValue.DONE) {
						startButton.setEnabled(true);
						startButton.setText("Create Barcode");
						
						
						try {
							BufferedImage output = bworker.get();
							JLabel picLabel = new JLabel(new ImageIcon(output));
							imgPanel.removeAll();
							imgPanel.updateUI();
							imgPanel.add(picLabel);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Invalid JSON file. Please make sure this JSON file corresponds to your selected images.");
							e.printStackTrace(System.out);						}
					}
				}
				
			}
		});
				
		bworker.execute();		

	}
}
