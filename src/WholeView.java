import javax.accessibility.AccessibleContext;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.beans.VetoableChangeListener;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by jason on 2/20/2016.
 */
public class WholeView extends JPanel implements Observer {
    private JPanel mWholeView = this;
    private Model mModel;
    private JMenuBar mMenuBar;
    public Slider mSlider;
    private drawingCanvas mDrawingCanvas;
    private drawingCanvas mDrawingCanvasInd;
    private JScrollPane mCanvasScrollPane;
    private int mFrames = 60;


    public WholeView(Model model) {
        super();
        mModel = model;

        this.setLayout(new BorderLayout());

        // North layout: menu
        this.add(createMenuBar(), BorderLayout.NORTH);

        // bottom layout
        JButton playButton = new JButton("Play");
        playButton.addActionListener(new mPlayActionListener());

        JButton playBackButton = new JButton("PlayBack");
        playBackButton.addActionListener(new mPlayBackActionListener());

        JButton startButton = new JButton("Start");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mModel.resetToStart();
                mSlider.setValue(0);
            }
        });
        JButton endButton = new JButton("End");
        endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mModel.resetToEnd();
                mSlider.setValue(mSlider.getMaximum());
            }
        });
        mSlider = new Slider(mModel);



        Box bottomBox = Box.createHorizontalBox();
        bottomBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 0, 0, 0),
                BorderFactory.createMatteBorder(1, 0, 0, 0, Color.BLACK)));
        bottomBox.setPreferredSize(new Dimension(this.getWidth(), 50));
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(playButton);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(playBackButton);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(mSlider);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(startButton);
        bottomBox.add(Box.createHorizontalStrut(10));
        bottomBox.add(endButton);
        bottomBox.add(Box.createHorizontalStrut(10));
        this.add(bottomBox, BorderLayout.SOUTH);


        // left bar layout
        JPanel mColorPanel = new JPanel(new GridLayout(6, 2, 10, 10));
        mColorPanel.add(new ColorItem(Color.black));
        mColorPanel.add(new ColorItem(Color.red));
        mColorPanel.add(new ColorItem(Color.yellow));
        mColorPanel.add(new ColorItem(Color.white));
        mColorPanel.add(new ColorItem(new Color(46,125,50)));
        mColorPanel.add(new ColorItem(Color.GRAY));
        mColorPanel.add(new ColorItem(Color.BLUE));
        mColorPanel.add(new ColorItem(Color.pink));
        mColorPanel.add(new ColorItem(Color.magenta));
        mColorPanel.add(new ColorItem(new Color(93,64,55)));
        mColorPanel.add(new ColorItem(new Color(205,220,57)));
        mColorPanel.add(new ColorItem(new Color(156,39,176)));

        JPanel mStrokePanel = new JPanel(new GridLayout(5, 1, 10, 10));
        ColorChooser colorChooser = new ColorChooser();
        colorChooser.setText("Color Chooser");

        mStrokePanel.add(colorChooser);
        mStrokePanel.add(new ThicknessItem(2));
        mStrokePanel.add(new ThicknessItem(4));
        mStrokePanel.add(new ThicknessItem(8));
        mStrokePanel.add(new ThicknessItem(16));

        Box leftBox = Box.createVerticalBox();
        leftBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        leftBox.add(mColorPanel);
        leftBox.add(Box.createVerticalStrut(10));
        leftBox.add(mStrokePanel);
        leftBox.add(Box.createVerticalGlue());
        this.add(leftBox, BorderLayout.WEST);


        // center canvas layout
        mModel.initCanvas();

        mDrawingCanvasInd = new drawingCanvas();
        mDrawingCanvasInd.setPreferredSize(mModel.getCanvasSize());

        mDrawingCanvas = new drawingCanvas();
        mDrawingCanvas.setPreferredSize(mModel.getCanvasSize());
        mCanvasScrollPane = new JScrollPane(mDrawingCanvas);

        this.add(mCanvasScrollPane, BorderLayout.CENTER);
    }


    public JMenuBar createMenuBar(){
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        JMenuItem newMenuItem =  new JMenuItem("New");
        newMenuItem.addActionListener(new newMenuActionListener());
        fileMenu.add(newMenuItem);


        JMenuItem loadMenuItem =  new JMenuItem("Load");
        loadMenuItem.addActionListener(new loadMenuActionListener());
        fileMenu.add(loadMenuItem);

        JMenuItem saveMenuItem =  new JMenuItem("Save");
        saveMenuItem.addActionListener(new saveMenuActionListener());
        fileMenu.add(saveMenuItem);


        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem fullSizeRadioButton = new JRadioButtonMenuItem("Full-size");
        fullSizeRadioButton.addActionListener(new fullSizeMenuActionListener());
        JRadioButtonMenuItem fitRadioButton = new JRadioButtonMenuItem("Fit");
        fitRadioButton.addActionListener(new fitMenuActionListener());
        fullSizeRadioButton.setSelected(true);
        fitRadioButton.setMnemonic(KeyEvent.VK_I);
        fullSizeRadioButton.setMnemonic(KeyEvent.VK_F);
        group.add(fitRadioButton);
        group.add(fullSizeRadioButton);

        viewMenu.add(fullSizeRadioButton);
        viewMenu.add(fitRadioButton);

        mMenuBar = new JMenuBar();
        mMenuBar.add(fileMenu);
        mMenuBar.add(viewMenu);

        return mMenuBar;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }

    public class fullSizeMenuActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            if (!mModel.isFullSize()){
                mModel.setFullSize(true);
                mWholeView.remove(mDrawingCanvasInd);
                mWholeView.add(mCanvasScrollPane, BorderLayout.CENTER);
                mWholeView.updateUI();
            }
        }
    }

    public class fitMenuActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            System.out.println("fit Yooooooooooooooooo");
            if (mModel.isFullSize()) {
                mModel.setFullSize(false);
                mWholeView.remove(mCanvasScrollPane);
                mWholeView.add(mDrawingCanvasInd, BorderLayout.CENTER);
                mWholeView.updateUI();
            }
        }
    }

    public class newMenuActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            Object[] options = {"Save", "Don't Save", "Cancel"};
            int choice = JOptionPane.showOptionDialog(getParent(), "Do you want to save changes into a file?", "Doodle",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
            if (choice == 0){
                new saveMenuActionListener().actionPerformed(null);
                mModel.resetCanvas();
                mModel.initCanvas();
            }
            else if (choice == 1){
                mModel.resetCanvas();
                mModel.initCanvas();
            }
        }
    }

    public class saveMenuActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Binary File", "bin"));
                fc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
                fc.setAcceptAllFileFilterUsed(false);
                fc.setApproveButtonText("Save");
                fc.setDialogTitle("Save");
                int returnVal = fc.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    mModel.saveIntoFile(fc.getSelectedFile(), fc.getFileFilter().getDescription());
                    System.out.println("Saved");
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    public class loadMenuActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                final JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("Binary File", "bin"));
                fc.setFileFilter(new FileNameExtensionFilter("Text File", "txt"));
                fc.setAcceptAllFileFilterUsed(false);
                int returnVal = fc.showOpenDialog(getParent());
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    mModel.loadFromFile(fc.getSelectedFile(), fc.getFileFilter().getDescription());
                    System.out.println("Loaded");
                }
            }
            catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }



    public class drawingCanvas extends JPanel implements MouseMotionListener, MouseListener, Scrollable{

        public drawingCanvas(){
            this.addMouseListener(this);
            this.addMouseMotionListener(this);

        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            int width, height;
            if (mModel.isFullSize()){
                width = mModel.getCanvasWidth();
                height = mModel.getCanvasHeight();
            }
            else{
                width = getWidth();
                height = getHeight();
            }
            g2.setColor(Color.WHITE);
            g2.fillRect(0, 0, width, height);

            // draw complete strokes
            for (int j = 0; j < mModel.getCurrentDrawingStroke(); j++){
                ArrayList<Point> mPoints = mModel.getmPointArrayStack().get(j);
                g2.setColor(mModel.getmPointColorStack().get(j));
                g2.setStroke(new BasicStroke(mModel.getmStrokeSizeStack().get(j)));
                if (mPoints.size() == 1){
                    g2.drawLine((int)mPoints.get(0).getX()*getWidth()/mModel.getCanvasWidth(), (int)mPoints.get(0).getY()*getHeight()/mModel.getCanvasHeight(),
                            (int)mPoints.get(0).getX()*getWidth()/mModel.getCanvasWidth(), (int)mPoints.get(0).getY()*getHeight()/mModel.getCanvasHeight());
                }
                else {
                    for (int i = 1; i < mPoints.size(); i++){
                        g2.drawLine((int)mPoints.get(i-1).getX()*getWidth()/mModel.getCanvasWidth(), (int)mPoints.get(i-1).getY()*getHeight()/mModel.getCanvasHeight(),
                                (int)mPoints.get(i).getX()*getWidth()/mModel.getCanvasWidth(), (int)mPoints.get(i).getY()*getHeight()/mModel.getCanvasHeight());
                    }
                }
            }

            //System.out.println("pop: " + (getmImageStack().size() - 1 - currentDrawingStroke));
            // draw incomplete points
            if (mModel.getCurrentDrawingPoint() > 0) {
                int pointsToDraw = mModel.getCurrentDrawingPoint();
                g2.setColor(mModel.getmPointColorStack().get(mModel.getCurrentDrawingStroke()));
                g2.setStroke(new BasicStroke(mModel.getmStrokeSizeStack().get(mModel.getCurrentDrawingStroke())));
                ArrayList<Point> mPoints = mModel.getmPointArrayStack().get(mModel.getCurrentDrawingStroke());
                if (pointsToDraw == 1) {
                    g2.drawLine((int) mPoints.get(0).getX() * getWidth() / mModel.getCanvasWidth(), (int) mPoints.get(0).getY() * getHeight() / mModel.getCanvasHeight(),
                            (int) mPoints.get(0).getX() * getWidth() / mModel.getCanvasWidth(), (int) mPoints.get(0).getY() * getHeight() / mModel.getCanvasHeight());
                } else {
                    for (int i = 1; i < pointsToDraw && i < mModel.getmPointArrayStack().get(mModel.getCurrentDrawingStroke()).size(); i++) {
                        g2.drawLine((int) mPoints.get(i - 1).getX() * getWidth() / mModel.getCanvasWidth(), (int) mPoints.get(i - 1).getY() * getHeight() / mModel.getCanvasHeight(),
                                (int) mPoints.get(i).getX() * getWidth() / mModel.getCanvasWidth(), (int) mPoints.get(i).getY() * getHeight() / mModel.getCanvasHeight());
                    }
                }
            }

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mModel.updateOnMouseDragged(e, getWidth(), getHeight());
            //System.out.println("mouse dragged!!!");
        }

        @Override
        public void mouseMoved(MouseEvent e) {

        }

        @Override
        public synchronized void addVetoableChangeListener(VetoableChangeListener listener) {
            super.addVetoableChangeListener(listener);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            mModel.updateOnMouseClick(e, getWidth(), getHeight());
            System.out.println("mouse clicked!!!");

            // add to slider
            mSlider.setValue(Math.max(0, (mModel.getmPointArrayStack().size() - 1)*100));
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            mModel.updateOnMousePressed();
            System.out.println("mousePressed!!!");
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            // update model
            mModel.updateOnMouseReleased(e);

            // add to slider
            mSlider.setValue(Math.max(0, (mModel.getmPointArrayStack().size() - 1)*100));
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            return super.getAccessibleContext();
        }

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            // when mouse wheel zoom
            return mModel.getBlockIncrementSize();
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return false;
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            // when directional button hit
            return mModel.getUnitIncrementSize();
        }

    }

    class ColorItem extends JButton implements ActionListener{
        Color mColor;
        public ColorItem(Color color) {
            super();
            mColor = color;
            setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
            setBackground(mColor);

            addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
                System.out.println("control pressed");
                mColor = JColorChooser.showDialog(null, "Choose a Color", new JLabel("Wow").getForeground());
                setBackground(mColor);
                mModel.setmCurrentColor(mColor);
            }
            mModel.setmCurrentColor(mColor);
        }
    }

    class ColorChooser extends JButton implements ActionListener{
        Color mColor;
        public ColorChooser() {
            super();
            setBackground(mColor);
            addActionListener(this);
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            mColor = JColorChooser.showDialog(null, "Choose a Color", new JLabel("Wow").getForeground());
            setBackground(mColor);
            mModel.setmCurrentColor(mColor);
        }
    }

    class ThicknessItem extends JButton implements ActionListener{
        int mStroke;
        public ThicknessItem(int thickness) {
            super();
            mStroke = thickness;
            setBackground(Color.white);
            addActionListener(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            Stroke preStroke = g2.getStroke();
            g2.setColor(Color.black);
            g2.setStroke(new BasicStroke(mStroke));
            g2.drawLine(10 + mStroke/2, getHeight()/2, getWidth() - 10 - mStroke/2, getHeight()/2);
            g2.setStroke(preStroke);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            mModel.setStrokeSize(mStroke);
        }
    }

    class mPlayActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            mModel.startPlay(mSlider);
        }
    }

    class mPlayBackActionListener implements ActionListener{
        @Override
        public void actionPerformed(ActionEvent e) {
            mModel.startPlayBack(mSlider);
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint();
    }
}
