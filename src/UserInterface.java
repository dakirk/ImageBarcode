import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.*;
import javax.swing.*;
import javax.swing.SwingWorker.*;
import javax.swing.SwingWorker.StateValue;
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
	static JTextField barWidth, imgHeight;
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
		
		//text boxes
		JPanel panel3 = new JPanel();
		
		barWidth = new JTextField();
		barWidth.setBounds(50,150,150,20);
		
		imgHeight = new JTextField();
		imgHeight.setBounds(50,200,150,20); 
		
		panel3.add(barWidth);
		panel3.add(imgHeight);
		
		
		//start button
		startButton = new JButton("Create Barcode");
		startButton.setBounds(130, 100, 100, 40);
		startButton.addActionListener(this);
		JPanel panel4 = new JPanel();
		panel4.add(startButton);
		
		
		
		f.add(panel1);
		f.add(panel2);
		f.add(panel3);
		f.add(panel4);
		
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
			
			
		}
	}
	
	public void createBarcode() {
		
		BarcodeWorker bworker = new BarcodeWorker(sortOption, enhanceOption, hasJSON);
		
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
					}
				}
				
			}
		});
				
		bworker.execute();
		

	}
}
