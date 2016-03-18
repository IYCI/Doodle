import javax.swing.*;
import java.awt.*;

public class Main {

    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("Super Awesome Doodle");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create Model and view
        Model model = new Model();
        WholeView wholeView = new WholeView(model);
        model.addObserver(wholeView);
        model.addObserver(wholeView.mSlider);

        // let all the views know that they're connected to the model
        model.notifyObservers();

        // Create and set up the content pane.
        frame.getContentPane().add(wholeView);
        //frame.setJMenuBar(wholeView.createMenuBar());

        try {
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception e){
            e.printStackTrace();
        }

        // Display
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setMinimumSize(new Dimension(800, 600));
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
}
