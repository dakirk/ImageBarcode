//import javax.swing.UIManager;
//import javax.swing.UnsupportedLookAndFeelException;

/**
 * @author David Kirk
 * This project is available on GitHub: https://github.com/dakirk/ImageBarcode
 */

public class Controller {

	public static void main(String[] args) {

		//use default look and feel for now
		
		/*
		//set the look and feel
		try {
		        // Set cross-platform Java L&F (also called "Metal")
		    UIManager.setLookAndFeel(
		        UIManager.getCrossPlatformLookAndFeelClassName());
		} 
		catch (UnsupportedLookAndFeelException e) {
		   System.out.println("Look and feel not found!");
		}
		catch (ClassNotFoundException e) {
		   // handle exception
		}
		catch (InstantiationException e) {
		   // handle exception
		}
		catch (IllegalAccessException e) {
		   // handle exception
		}
		*/
		new UserInterface();
		
		
	}

}
