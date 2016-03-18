import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by jason on 2/23/2016.
 */
public class Slider extends JSlider implements ChangeListener, Observer {
    private Model mModel;

    public Slider(Model model) {
        super();
        mModel = model;

        this.setMaximum(0);
        this.setMinimum(0);
        this.setValue(0);
        this.setMajorTickSpacing(100);
        this.setPaintTicks(true);
        //this.setPaintLabels(true);
        this.setPaintTrack(true);
        //this.setSnapToTicks(true);


        this.addChangeListener(this);
    }


    @Override
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        int value = Math.max(0, source.getValue());
        if (!mModel.getPlaying() && !mModel.getPlayingBack() && !mModel.getDrawing()) {
            mModel.setCurrentDrawingStroke(value / 100);
            mModel.setCurrentDrawingPoint(value % 100 * mModel.getmPointArrayStack().get(mModel.getCurrentDrawingStroke()).size() / 100);
            mModel.updateView();
        }
        System.out.println("source.getValue() is " + value);
    }

    @Override
    public void update(Observable o, Object arg) {
        setMaximum((mModel.getmPointArrayStack().size() - 1)*100);
        //setValue(mModel.getCurrentSliderValue());
    }
}
