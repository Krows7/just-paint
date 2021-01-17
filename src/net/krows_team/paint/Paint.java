package net.krows_team.paint;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 
 * Just a paint.
 * 
 * @author Krows
 * 
 */
public class Paint extends JFrame {
	
/**
 * 
 * Serial Version UID.
 * 
 */
	private static final long serialVersionUID = -5002214818262647854L;
	
	private JMenu fileMenu;
	
	private File currentFile;
	
	private BufferedImage image;
	
	private Graphics2D gr;
	
	private JPanel canvas;
	
	private JScrollPane scrollPane;
	
	private boolean saved = true;
	
	public Paint() {
		
		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e1) {
			
			e1.printStackTrace();
		}
		
		canvas = new JPanel(true) {
			
/**
 * 
 * Serial Version UID.
 * 
 */
			private static final long serialVersionUID = -7963532029109417767L;

			@Override
			protected void paintComponent(Graphics g) {
				
				super.paintComponent(g);
				
				g.drawImage(image, (getWidth() - image.getWidth()) / 2, (getHeight() - image.getHeight()) / 2, null);
			}
		};
		
		JColorChooser chooser = new JColorChooser();
		
		JSlider slider = new JSlider(1, 100);
		
		scrollPane = new JScrollPane(canvas);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		scrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		JPanel tools = new JPanel();
		tools.setLayout(new BoxLayout(tools, BoxLayout.Y_AXIS));
		tools.add(chooser);
		tools.add(slider);
		
		setTitle("My Paint :3");
		setSize(1280, 720);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		getContentPane().add(tools, BorderLayout.EAST);
		addWindowListener(new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) {
				
				if(!saved) {
					
					int o = JOptionPane.showConfirmDialog(Paint.this, "File is not saved. Do you want save?", "Close Paint", JOptionPane.YES_NO_CANCEL_OPTION);
					
					if(o == JOptionPane.NO_OPTION) dispose();
					else if(o == JOptionPane.YES_OPTION) {
						
						if(currentFile == null) saveAs();
						else save();
						
						dispose();
					}
				}
			}
		});
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		fileMenu = new JMenu("File");
		fileMenu.setActionCommand("File");
		menuBar.add(fileMenu);
		
		JMenuItem openFile = new JMenuItem("Open File");
		openFile.addActionListener(e -> {
			
			JFileChooser c = new JFileChooser(new File(System.getProperty("user.dir")));
			c.setFileFilter(new FileNameExtensionFilter("Image Files (jpg, jpeg, png)", "jpg", "jpeg", "png"));
			
			if(c.showSaveDialog(Paint.this) == JFileChooser.APPROVE_OPTION) {
				
				currentFile = c.getSelectedFile();
				
				try {
					
					resetImage(ImageIO.read(currentFile));
					setTitle(currentFile.toString());
					revalidate();
					repaint();
					
					saved = true;
				} catch (IOException e1) {
					
					e1.printStackTrace();
				}
			}
		});
		
		JMenuItem saveAs = new JMenuItem("Save As");
		saveAs.addActionListener(e -> saveAs());
		
		JMenuItem save = new JMenuItem("Save");
		save.addActionListener(e -> {
			
			if(currentFile == null) saveAs();
			else save();
		});
		
		fileMenu.add(openFile);
		fileMenu.add(save);
		fileMenu.add(saveAs);
		
		setVisible(true);
		
		resetImage(new BufferedImage(500, 500, BufferedImage.TYPE_INT_RGB), true);
		
		MouseAdapter listener = new MouseAdapter() {
			
			Point prev;
			
			boolean pressed;
			
			int pressedButton;
			
			@Override
			public void mouseDragged(MouseEvent e) {
				
				if(pressedButton == MouseEvent.BUTTON2) {
					
					if(prev != null) gr.translate(e.getX() - prev.x, e.getY() - prev.y);
				} else {
					
					e.translatePoint(- (canvas.getWidth() - image.getWidth()) / 2, - (canvas.getHeight() - image.getHeight()) / 2);
					
					if(prev != null) {
						
						saved = false;
						
						gr.drawLine(prev.x, prev.y, e.getX(), e.getY());
						
						canvas.repaint();
					}
				}
				
				prev = e.getPoint();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				
				if(e.getButton() == MouseEvent.BUTTON1) {
					
					e.translatePoint(- (canvas.getWidth() - image.getWidth()) / 2, - (canvas.getHeight() - image.getHeight()) / 2);
					
					if(!pressed) {
						
						saved = false;
						
						gr.drawLine(e.getX(), e.getY(), e.getX(), e.getY());
						
						canvas.repaint();
					}
				}
				
				pressedButton = e.getButton();
				
				prev = e.getPoint();
				
				pressed = true;
			}
			
			@Override
			public void mouseReleased(MouseEvent e) {
				
				pressed = false;
				
				pressedButton = - 1;
			}
		};
		
		chooser.getSelectionModel().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				
				gr.setColor(chooser.getColor());
			}
		});
		
		slider.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				
				gr.setStroke(new BasicStroke(slider.getValue(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			}
		});
		
		canvas.addMouseMotionListener(listener);
		canvas.addMouseListener(listener);
	}
	
	private void save() {
		
		File f = currentFile;
		
		String fs = f.getName();
		
		try {
			
			if(f.exists() || f.createNewFile()) ImageIO.write(image, fs.substring(fs.lastIndexOf('.') + 1), f);
			
			saved = true;
		} catch (IOException e1) {
			
			e1.printStackTrace();
		}
	}
	
	private void saveAs() {
		
		JFileChooser c = new JFileChooser(new File(System.getProperty("user.dir")));
		c.setFileFilter(new FileNameExtensionFilter("Image Files (jpg, jpeg, png)", "jpg", "jpeg", "png"));
		
		if(c.showSaveDialog(Paint.this) == JFileChooser.APPROVE_OPTION) {
			
			File f = c.getSelectedFile();
			
			String fs = f.getName();
			
			try {
				
				if(f.exists() || f.createNewFile()) {
					
					ImageIO.write(image, fs.substring(fs.lastIndexOf('.') + 1), f);
					
					currentFile = f;
					
					setTitle(currentFile.toString());
					
					saved = true;
				}
				
			} catch (IOException e1) {
				
				e1.printStackTrace();
			}
		}
	}
	
	private void resetImage(BufferedImage image) {
		
		resetImage(image, false);
	}
	
	private void resetImage(BufferedImage image, boolean f) {
		
		canvas.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
		
		scrollPane.getViewport().revalidate();
		
		this.image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		
		gr = this.image.createGraphics();
		gr.drawImage(image, 0, 0, null);
		gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		gr.setStroke(new BasicStroke(10.0F, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		
		if(f) {
			
			gr.setColor(Color.WHITE);
			gr.fillRect(0, 0, image.getWidth(), image.getHeight());
		}
		
		gr.setColor(Color.BLACK);
	}
	
	/**
	 * 
	 * Main method.
	 * 
	 * @param args Arguments for method.
	 * 
	 */
	public static void main(String[] args) {
		
		new Paint();
	}
}