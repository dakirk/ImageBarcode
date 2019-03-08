//import javax.swing.UIManager;
//import javax.swing.UnsupportedLookAndFeelException;

/**
 * This class contains the main method and runs UserInterface. In the future it may also set the look and feel,
 * if I find one I like.
 * 
 * @author David Kirk
 * @version 1.2
 * @since 1.0
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
