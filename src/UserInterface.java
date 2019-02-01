import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.*;
import javax.swing.*;
import javax.swing.SwingWorker.*;
import javax.swing.SwingWorker.StateValue;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
public class UserInterface extends JFrame implements ActionListener {
	
	
	String[] sortOptionsNoJSON = {"Hue", "Saturation", "Brightness", "None"};
	String[] sortOptionsJSON = {"Hue", "Saturation", "Brightness", "Chronological"};
	String[] enhanceOptions = {"Hue", "Saturation", "Brightness", "None"};
	
	static String sortOption;
	static String enhanceOption;
	static int barWidth;
	static int imgHeight;
	
	static boolean hasJSON;
	
	static JLabel descriptionLabelSort, descriptionLabelEnhance;
	static JFrame f;
	static JComboBox sortOptionBox, enhanceOptionBox;
	static JTextField barWidthBox, imgHeightBox;
	static JButton startButton;
	static JPanel imgPanel;
	 
	
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
		JPanel panel3 = new JPanel();
		
		JLabel barWidthBoxLabel = new JLabel(" Bar width: ");
		JLabel imgHeightBoxLabel = new JLabel(" Image Height: ");
		
		barWidthBox = new JTextField("1");
		barWidth = 1;
		
		imgHeightBox = new JTextField("100");
		imgHeight = 100;
		
		panel3.add(barWidthBoxLabel);
		panel3.add(imgHeightBoxLabel);
		panel3.add(barWidthBox);
		panel3.add(imgHeightBox);
		panel3.setLayout(new GridLayout(2, 2, 1, 1));
		
		
		//start button
		startButton = new JButton("Create Barcode");
		//startButton.setBounds(130, 100, 100, 40);
		startButton.addActionListener(this);
		JPanel panel4 = new JPanel();
		panel4.add(startButton);
		
		imgPanel = new JPanel();
		
		f.add(panel1);
		f.add(panel3);
		f.add(panel4);
		f.add(imgPanel);
		
		f.setSize(400,  200);
		f.setLayout(new GridLayout(4, 1, 0, 0));
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
			
			
		}
	}
	
	public BufferedImage createBarcode() {
		
		BarcodeWorker bworker = new BarcodeWorker(sortOption, enhanceOption, barWidth, hasJSON);
		
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
							System.out.println("Process interrupted");
						}
					}
				}
				
			}
		});
				
		bworker.execute();
		
		return null;
		

	}
}
