import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.util.*;

/**
 * Created by jason on 2/20/2016.
 */
public class Model extends Observable {
    private boolean mFullSize = true;
    private int canvasWidth = 800;
    private int canvasHeight = 600;
    private int mStrokeSize = 2;
    private Color mCurrentColor = Color.red;
    private int mUnitIncrementSize = 10;
    private int mBlockIncrementSize = 20;
    private Dimension CanvasSize = new Dimension(canvasWidth, canvasHeight);

    /** stacks **/
    private Stack<ArrayList<Point>> mPointArrayStack = new Stack<>();
    private Stack<Color> mPointColorStack = new Stack<>();
    private Stack<Integer> mStrokeSizeStack = new Stack<>();
    /*****  *****/

    private int currentDrawingStroke;
    private int currentDrawingPoint;
    private Timer mPlayTimer;
    private Timer mPlayBackTimer;
    private Boolean isPlaying = false;
    private Boolean isPlayingBack = false;
    private Boolean isDrawing = false;

    private int preX = -1;
    private int preY = -1;

    public Model() {
        setChanged();
    }

    public void initCanvas(){
        pushCurrentPointArray();
        updateView();
    }

    public void resetCanvas(){
        mPointArrayStack = new Stack<>();
        mPointColorStack = new Stack<>();
        mStrokeSizeStack = new Stack<>();

        currentDrawingStroke = 0;
        currentDrawingPoint = 0;
    }

    public void updateOnMouseDragged(MouseEvent e, int width, int height){
        isDrawing = true;
        preX = e.getX() * getCanvasWidth() / width;
        preY = e.getY() * getCanvasHeight() / height;

        // insert points
        mPointArrayStack.get(currentDrawingStroke).add(new Point(preX, preY));
        mPointColorStack.set(currentDrawingStroke, mCurrentColor);
        mStrokeSizeStack.set(currentDrawingStroke, mStrokeSize);

        currentDrawingPoint = getmPointArrayStack().get(currentDrawingStroke).size();
        updateView();
    }

    public void updateOnMouseClick(MouseEvent e, int width, int height){
        // update stack
        setCurrentDrawingStroke(getmPointArrayStack().size() - 1);

        mPointArrayStack.get(currentDrawingStroke).add(new Point(e.getX() * getCanvasWidth() / width, e.getY() * getCanvasHeight() / height));
        mPointColorStack.set(currentDrawingStroke, mCurrentColor);
        mStrokeSizeStack.set(currentDrawingStroke, mStrokeSize);
        pushCurrentPointArray();
        updateView();
    }

    public void updateOnMouseReleased(MouseEvent e){
        isDrawing = false;
        if (preX != -1 && preY != -1){
            setCurrentDrawingStroke(getmPointArrayStack().size());
            pushCurrentPointArray();
            updateView();
        }
        System.out.println("mouse released!!!");

        preX = -1;
        preY = -1;
    }

    public void updateOnMousePressed(){
        if (getCurrentDrawingStroke() != mPointArrayStack.size() - 1) {
            System.out.println("pressed on previous time point");
            // pop useless
            int strokesToPop = mPointArrayStack.size() - 1 - currentDrawingStroke;
            System.out.println("pop: " + strokesToPop);
            for (int i = 0; i < strokesToPop; i++) {
                mPointArrayStack.pop();
                mPointColorStack.pop();
                mStrokeSizeStack.pop();
            }
            // remove extra points
            for (int i = mPointArrayStack.get(currentDrawingStroke).size() - 1; i >= currentDrawingPoint; i--){
                if (i >= 0) {
                    mPointArrayStack.get(currentDrawingStroke).remove(i);
                }
            }
            currentDrawingStroke++;
            pushCurrentPointArray();
        }
    }

    public void startPlay(Slider mSlider){
        int maxStrokeSize = getmPointArrayStack().size() - 1;
        setCurrentDrawingStroke(0);
        setCurrentDrawingPoint(0);
        updateView();

        mPlayTimer = new Timer(1000 / 45, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCurrentDrawingPoint() >= getmPointArrayStack().get(currentDrawingStroke).size()){
                    setCurrentDrawingStroke(getCurrentDrawingStroke() + 1);
                    setCurrentDrawingPoint(0);
                }
                else {
                    setCurrentDrawingPoint(getCurrentDrawingPoint() + 1);
                }

                if (getCurrentDrawingStroke() >= maxStrokeSize){
                    mPlayTimer.stop();
                    isPlaying = false;
                    System.out.println("stopped");
                    return;
                }

                // add to slider
                mSlider.setValue(Math.max(0, currentDrawingStroke*100 + currentDrawingPoint*100/getmPointArrayStack().get(getCurrentDrawingStroke()).size()));

                updateView();
            }
        });
        mPlayTimer.start();
        isPlaying = true;
    }

    public void startPlayBack(Slider mSlider){
        setCurrentDrawingStroke(Math.max(0, getmPointArrayStack().size() - 1));
        setCurrentDrawingPoint(getmPointArrayStack().get(currentDrawingStroke).size());
        updateView();

        mPlayBackTimer = new Timer(1000 / 45, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (getCurrentDrawingPoint() - 1 < 0 && currentDrawingStroke - 1 >= 0){
                    setCurrentDrawingStroke(getCurrentDrawingStroke() - 1);
                    setCurrentDrawingPoint(getmPointArrayStack().get(currentDrawingStroke).size());
                }
                else {
                    setCurrentDrawingPoint(getCurrentDrawingPoint() - 1);
                }
                // add to slider
                if (getmPointArrayStack().get(getCurrentDrawingStroke()).size() > 0) {
                    mSlider.setValue(Math.max(0, currentDrawingStroke * 100 + currentDrawingPoint * 100 / getmPointArrayStack().get(getCurrentDrawingStroke()).size()));
                }

                updateView();
                if (getCurrentDrawingStroke() < 0 || (currentDrawingStroke == 0 && currentDrawingPoint == 0)){
                    mPlayBackTimer.stop();
                    isPlayingBack = false;
                }
            }
        });
        mPlayBackTimer.start();
        isPlayingBack = true;
    }

    public void saveIntoFile(File file, String fileType){
        try {
            if (fileType.equals("Binary File")) {
                file.createNewFile();
                FileOutputStream fos;
                if (file.getName().length() <= 4 || !file.getName().substring(file.getName().length() - 4, file.getName().length()).equals(".bin")) {
                    fos = new FileOutputStream(file + ".bin");
                }
                else {
                    fos = new FileOutputStream(file);
                }
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(mPointArrayStack);
                oos.writeObject(mStrokeSizeStack);
                oos.writeObject(mPointColorStack);
                oos.close();
            }
            else if (fileType.equals("Text File")){
                file.createNewFile();
                FileWriter fw;
                if (file.getName().length() <= 4 || !file.getName().substring(file.getName().length() - 4, file.getName().length()).equals(".txt")) {
                    fw = new FileWriter(file + ".txt");
                }
                else {
                    fw = new FileWriter(file);
                }

                BufferedWriter bw = new BufferedWriter(fw);

                // write mPointArrayStack
                bw.write(mPointArrayStack.size() + "\n");
                for (ArrayList<Point> mArray : mPointArrayStack){
                    bw.write(mArray.size() + "\n");
                    for (Point mPoint : mArray){
                        bw.write((int)mPoint.getX() + "\n");
                        bw.write((int)mPoint.getY() + "\n");
                    }
                }
                // write mStrokeSizeStack
                bw.write(mStrokeSizeStack.size() + "\n");
                for (Integer stroke : mStrokeSizeStack){
                    bw.write(stroke + "\n");
                }
                // write mPointColorStack
                bw.write(mPointColorStack.size() + "\n");
                for (Color color : mPointColorStack){
                    bw.write(color.getRGB() + "\n");
                }

                bw.close();
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public void loadFromFile(File file, String fileType){
        try {
            if (fileType.equals("Binary File")) {
                FileInputStream fis = new FileInputStream(file);
                ObjectInputStream ois = new ObjectInputStream(fis);
                mPointArrayStack = (Stack<ArrayList<Point>>) ois.readObject();
                mStrokeSizeStack = (Stack<Integer>) ois.readObject();
                mPointColorStack = (Stack<Color>) ois.readObject();
                ois.close();
            }
            else if (fileType.equals("Text File")){
                FileReader fr = new FileReader(file.getAbsoluteFile());
                BufferedReader br = new BufferedReader(fr);
                // write mPointArrayStack
                int mPointArrayStackSize = Integer.parseInt(br.readLine());
                for (int i = 0; i < mPointArrayStackSize; i++){
                    int arraySize = Integer.parseInt(br.readLine());
                    ArrayList<Point> array = new ArrayList<>();
                    for (int j = 0; j < arraySize; j++){
                        array.add(new Point(Integer.parseInt(br.readLine()), Integer.parseInt(br.readLine())));
                    }
                    mPointArrayStack.add(array);
                }
                // write mStrokeSizeStack
                int mStrokeSizeStackSize = Integer.parseInt(br.readLine());
                for (int i = 0; i < mStrokeSizeStackSize; i++){
                    mStrokeSizeStack.add(Integer.parseInt(br.readLine()));
                }
                // write mPointColorStack
                int mPointColorStackSize = Integer.parseInt(br.readLine());
                for (int i = 0; i < mPointColorStackSize; i++){
                    mPointColorStack.add(new Color(Integer.parseInt(br.readLine())));
                }
                br.close();
            }

            updateView();
            resetToEnd();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }


    public void updateView(){
        setChanged();
        notifyObservers();
    }

    public boolean isFullSize(){
        return mFullSize;
    }

    public Boolean getPlaying() {
        return isPlaying;
    }

    public Boolean getPlayingBack() {
        return isPlayingBack;
    }

    public void setFullSize(boolean fullSize){
        mFullSize = fullSize;
    }

    public Dimension getCanvasSize(){
        return CanvasSize;
    }

    public int getCanvasWidth(){
        return canvasWidth;
    }

    public int getCanvasHeight(){
        return canvasHeight;
    }

    public int getStrokeSize(){
        return mStrokeSize;
    }

    public void setStrokeSize(int strokeSize){
        mStrokeSize = strokeSize;
    }

    public int getUnitIncrementSize() {
        return mUnitIncrementSize;
    }

    public int getBlockIncrementSize() {
        return mBlockIncrementSize;
    }

    public int getCurrentDrawingStroke() {
        return currentDrawingStroke;
    }

    public void setCurrentDrawingStroke(int currentDrawingStroke) {
        this.currentDrawingStroke = currentDrawingStroke;
    }

    public Stack<ArrayList<Point>> getmPointArrayStack() {
        return mPointArrayStack;
    }

    public void pushCurrentPointArray(){
        mPointArrayStack.push(new ArrayList<>());
        mPointColorStack.push(mCurrentColor);
        mStrokeSizeStack.push(mStrokeSize);
    }

    public Stack<Color> getmPointColorStack() {
        return mPointColorStack;
    }

    public void setmCurrentColor(Color mCurrentColor) {
        this.mCurrentColor = mCurrentColor;
    }

    public Color getmCurrentColor() {
        return mCurrentColor;
    }

    public int getCurrentDrawingPoint() {
        return currentDrawingPoint;
    }

    public void setCurrentDrawingPoint(int currentDrawingPoint) {
        this.currentDrawingPoint = currentDrawingPoint;
    }

    public void resetToStart(){
        currentDrawingPoint = 0;
        currentDrawingStroke = 0;
        updateView();
    }

    public void resetToEnd(){
        currentDrawingPoint = 0;
        currentDrawingStroke = getmPointArrayStack().size();
        updateView();
    }

    public Stack<Integer> getmStrokeSizeStack() {
        return mStrokeSizeStack;
    }

    public Boolean getDrawing() {
        return isDrawing;
    }
}
