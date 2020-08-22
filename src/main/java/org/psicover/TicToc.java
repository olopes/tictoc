package org.psicover;

import java.awt.AWTException;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class TicToc implements Runnable {

	private static Color DARK_RED = new Color(0x8b0000);
	private static Color DARK_YELLOW = new Color(0x9b870c);
	private static Color DARK_GREEN = new Color(0x008b00);
	private static Image[] trafficLights = renderTrafficLights();
	private static final boolean windows = System.getProperty("os.name", "").toLowerCase().startsWith("windows");

	private JFrame frame = new JFrame("TicToc");
	private TrayIcon trayIcon;
	
	public TicToc() {
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	private static Image[] renderTrafficLights() {
		BufferedImage offImage = drawTrafficLights(0);
		BufferedImage redImage = drawTrafficLights(1);
		BufferedImage yellowImage = drawTrafficLights(2);
		BufferedImage greenImage = drawTrafficLights(4);
		
		return new Image[] { offImage, redImage, yellowImage, greenImage };
	}

	private static BufferedImage drawTrafficLights(int light) {
		BufferedImage image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = image.createGraphics();
	    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    
	    // :-(
	    if (windows) {
		    g2d.setComposite(AlphaComposite.Clear);
		    g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
		    g2d.setComposite(AlphaComposite.Src);
	    } else {
	    	// no transparency under linux
			g2d.setColor(Color.DARK_GRAY);
		    g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
	    }
	    
	    
		g2d.setColor(Color.DARK_GRAY);
		g2d.setStroke(new BasicStroke(5.0f));
		g2d.fillRoundRect(29, 2, 72, 124, 8, 8);
		g2d.setColor(Color.WHITE);
		g2d.drawRoundRect(29, 2, 72, 124, 8, 8);
		
		g2d.setStroke(new BasicStroke(2.0f));
		
		g2d.setColor((light & 0x1) == 0 ? DARK_RED : Color.PINK);
		g2d.fillOval(49, 8, 32, 32);
		g2d.setColor((light & 0x1) == 0 ? Color.DARK_GRAY : DARK_RED);
		g2d.drawOval(49, 8, 32, 32);
		
		g2d.setColor((light & 0x2) == 0 ? DARK_YELLOW : Color.YELLOW);
		g2d.fillOval(49, 8+7+32, 32, 32);
		g2d.setColor((light & 0x2) == 0 ? Color.DARK_GRAY : DARK_YELLOW);
		g2d.drawOval(49, 8+7+32, 32, 32);
		
		g2d.setColor((light & 0x4) == 0 ? DARK_GREEN : Color.GREEN);
		g2d.fillOval(49, 8+7+7+32+32, 32, 32);
		g2d.setColor((light & 0x4) == 0 ? Color.DARK_GRAY : DARK_GREEN);
		g2d.drawOval(49, 8+7+7+32+32, 32, 32);
		g2d.dispose();
		return image;
	}

	@Override
	public void run() {
		if (!SystemTray.isSupported()) {
			JOptionPane.showMessageDialog(null, "No system tray... bye!");
			return;
		}
		this.trayIcon = createTrayIcon();
		for(Image image : trafficLights) {
			this.showTrayNotification(image);
			JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(image)));
		}
		// SwingUtilities.invokeLater(this::showTrayNotification);
		JOptionPane.showMessageDialog(null, "Finished!");
		SystemTray.getSystemTray().remove(trayIcon);
		frame.setVisible(false);
		frame.dispose();
	}
	
	private TrayIcon createTrayIcon() {
		SystemTray tray = SystemTray.getSystemTray();
		
		JPopupMenu popup = new JPopupMenu("Preeeet");
		popup.add(new JMenuItem("Restart"));
		popup.add(new JMenuItem("Set time"));
		popup.addSeparator();
		popup.add(new JMenuItem("Quit"));
		TrayIcon trayIcon = new TrayIcon(trafficLights[0], "TicToc: Let's do work, I'll tell you when to stop!", null);
		trayIcon.setImageAutoSize(true);

//		trayIcon.addMouseListener(new java.awt.event.MouseAdapter() {
//			@Override
//			public void mouseClicked(java.awt.event.MouseEvent e) {
//				if(e.isPopupTrigger()) {
//					System.out.println("popup");
//					popup.show(frame, e.getX(), e.getY());
//				} else /*if (e.getClickCount() >= 2)*/ {
//					frame.setVisible(!frame.isVisible());
//				}
//				System.out.println(e);
//			}
//		});
		
		trayIcon.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				System.out.println(e);
				frame.setVisible(!frame.isVisible());
	        	// SwingUtilities.invokeLater(()->JOptionPane.showMessageDialog(null, "ouch! don't touch me!"));
			}
		});
		
		try {
			tray.add(trayIcon);
			return trayIcon;
		} catch (AWTException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void showTrayNotification(Image image) {
		trayIcon.setImage(image);
		if(windows) {
			trayIcon.displayMessage("TicToc", "Icon changed!!", MessageType.ERROR);
		} else {
			try {
				new ProcessBuilder("notify-send", "-a", "TicToc", "Icon changed").inheritIO().start();
			} catch (IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		//this SHOULD enable global anti-aliasing
		System.setProperty("awt.useSystemAAFontSettings","on");
		System.setProperty("swing.aatext", "true");
		SwingUtilities.invokeLater(new TicToc());

	}

}
