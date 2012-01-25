/*
  (c) Copyright 2007 Hewlett-Packard Development Company, LP
  [See end of file]
  $Id: Contract.java 1121 2007-04-11 15:02:58Z jeremy_carroll $
*/
package com.hp.hpl.jena.grddl.license;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import edu.stanford.ejalbert.BrowserLauncher;
import edu.stanford.ejalbert.exception.BrowserLaunchingInitializingException;
import edu.stanford.ejalbert.exception.UnsupportedOperatingSystemException;

/**
 * Contract
 * @author Jeremy J. Carroll
 */
@SuppressWarnings("serial")
public class Contract extends JPanel
                      implements ActionListener, HyperlinkListener, WindowListener {
    JEditorPane htmlTextArea;
    JButton     leftButton;
    JButton     midButton;
    JButton     rightButton;
    
    int lastPage = 3;
    int currentPage;
    
    ClassLoader cl;
    JScrollPane scrollPane;
    final Thread parent;
    final boolean result[];
    final Window frame;
    int updated = 0;
	protected boolean scrolling;
    public Contract(Thread t, boolean rslt[], JFrame w) {
    	parent = t;
    	result = rslt;
    	frame = w;
    	frame.addWindowListener(this);
//    	w.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

       
        cl = Contract.class.getClassLoader();
        

        leftButton = button("Back");
        midButton  = button("Next");
        rightButton = button("I Agree");
        
        htmlTextArea = new JEditorPane("text/html","hh ww");
//        htmlTextArea.setText("<html><p>a b c d e f g h i j k l m");
        htmlTextArea.setEditable(false);
        htmlTextArea.addHyperlinkListener(this);
        scrollPane = new JScrollPane(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
        		ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        
        scrollPane.setViewportView(htmlTextArea);
        JScrollBar sb=
        scrollPane.getVerticalScrollBar();
//        sb.addAdjustmentListener(new AdjustmentListener(){
//
//			public void adjustmentValueChanged(AdjustmentEvent arg0) {
//				System.err.println("Scrolling");
//			}
//        	
//        });
        sb.addMouseListener(new MouseListener(){

			public void mouseClicked(MouseEvent arg0) {}

			public void mouseEntered(MouseEvent arg0) {
				scrolling = true;
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
        	
        });
        sb.addFocusListener(new FocusListener(){

			public void focusGained(FocusEvent arg0) {
				scrolling = true;
			}

			public void focusLost(FocusEvent arg0) {
			}
        	
        });
        scrollPane.getViewport().addChangeListener(new ChangeListener(){

			public void stateChanged(ChangeEvent arg0) {
				if (!scrolling) {
					// When displaying new page, scroll to top.
					// Keep pumpng this event through
					// this must be wrong, but I can't work out the
					// right way to do this.
					 scrollPane.getViewport().scrollRectToVisible(new Rectangle(-1000,-1000,1,1));
				}
				if (scrolling && currentPage==lastPage) {
					JScrollBar sb = scrollPane.getVerticalScrollBar();
//					System.err.println(sb.getValue() + " "+ sb.getMaximum() + " " + sb.getHeight());
					if (sb.getValue() + sb.getHeight() == sb.getMaximum())
						rightButton.setEnabled(true);
				}
//				System.err.println(updated+" Ch: "+arg0.toString());
//			    if (updated>0 && !scrollPane.getViewport().isValid()) {
//			    	scrollPane.getViewport().setViewPosition(new Point());
//					updated --;
//			    }
			}
        	
        });
        
        loadPage(0);

       
      
        JPanel buttonPanel = new JPanel();
//        setLayout(new GridBagLayout());
//        GridBagConstraints c = new GridBagConstraints();
//        c.weightx = 0.5;
//        c.weighty = 0.5;
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    "GRDDL 0.2 License"),
                BorderFactory.createEmptyBorder(10,10,10,10)));
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridheight = 40;
//        c.gridwidth = 3;
//        c.fill = GridBagConstraints.BOTH;
        add(scrollPane);
//        c.fill = GridBagConstraints.NONE;
//        c.gridheight = 1;
//        c.gridy = 40;
        add(Box.createRigidArea(new Dimension(0,10)));
//        c.gridy = 41;
//        c.gridheight = 3;
//        c.gridwidth = 1;
        buttonPanel.setLayout(new GridLayout(1,3,20,20));
        buttonPanel.add(leftButton);
//        c.gridx = 1;
        buttonPanel.add(midButton);
//        c.gridx = 2;
        buttonPanel.add(rightButton);
        Dimension size = buttonPanel.getPreferredSize();
//    	System.err.println(" pref: "+size);
//        size.width = 0;
////		buttonPanel.setPreferredSize(size);
		size.width = Integer.MAX_VALUE;
        buttonPanel.setMaximumSize(size);
        add(buttonPanel);

    
//        setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
//        add(leftPanel);
    }

    private void loadPage(int i) {
		InputStream in =
		cl.getResourceAsStream(
		 "com/hp/hpl/jena/grddl/license/data/page"+i+".html"
		);
		
		StringBuffer buf = new StringBuffer();
		
		while (true) {
			int ch;
			try {
				ch = in.read();
			} catch (IOException e) {
				break;
			}
			if (ch==-1)
				break;
//			System.out.append((char)ch);
			buf.append((char)ch);
		}
		
//		System.err.println("Page: "+i);
//		System.err.println(buf.toString());

//        htmlTextArea.setEditable(true);
//		if (i==1)
		scrolling = false;
		htmlTextArea.setText(buf.toString());
//		htmlTextArea.validate();
//		scrollPane.getViewport().validate();
//        htmlTextArea.setEditable(false);
		leftButton.setEnabled(i!=1);
		midButton.setText(i==lastPage?"I Decline":"Next");
		rightButton.setEnabled(false);
		currentPage = i;
//		scrollPane.getViewport().scrollRectToVisible(new Rectangle(-1000,-1000,1,1));
//		JScrollBar sb =
//		scrollPane.getVerticalScrollBar();
//		System.err.println(sb.getMinimum() + " => "+ sb.getMaximum());
//		System.err.println(sb.getValue());
////		frame.pack();
////		if (i!=0) frame.validate();
//		System.err.println(sb.getMinimum() + " => "+ sb.getMaximum());
//		System.err.println(sb.getValue());
//		
//		sb.setValue(sb.getMinimum());
		
	}

	JButton button(String text) {
    	JButton r = new JButton(text);
    	r.setAlignmentX(Component.CENTER_ALIGNMENT);
    	r.addActionListener(this);
//    	System.err.print(text);
    	Dimension size = r.getMinimumSize();
//    	System.err.println(" min: "+size);
		r.setPreferredSize(size);
		size.width = Integer.MAX_VALUE;
		r.setMaximumSize(size);
    	return r;
    }
    //React to the user pushing the Change button.
    public void actionPerformed(ActionEvent e) {
    	if (e.getSource() == midButton) {
    		if (currentPage==lastPage)
    		   System.exit(-1);
    		else
    			loadPage(currentPage+1);
    	}
    	if (e.getSource() == leftButton) 
    		loadPage(currentPage-1);
    	
    	if (e.getSource() == rightButton)  {
    		result[0] = true;
    		parent.interrupt();
    		frame.dispose();
    	}
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     * @param rslt 
     * @param t 
     */
    private static void createAndShowGUI(Thread t, boolean[] rslt) {
        //Create and set up the window.
        JFrame frame = new JFrame("Jena GRDDL Reader 0.2 License");
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        Contract newContentPane = new Contract(t,rslt,frame);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        

        //Display the window.
        frame.pack();
        frame.setLocationRelativeTo(null);
//        Rectangle r = frame.getBounds();
//        frame.setLocation(-r.width/2, -r.height/2);
        frame.setVisible(true);
        newContentPane.loadPage(1);
    }
 
    public static void main(String[] args) {
    	System.err.println();
    	final boolean rslt = askUser();
        System.err.println("Result: "+rslt);
    }

	static boolean askUser() {
		final Thread t = Thread.currentThread();
    	final boolean rslt[] = new boolean[]{false};
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(t,rslt);
            }
        });
        
        while (!Thread.interrupted()) {
        	try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				break;
			}
        }
		return rslt[0];
	}

	public void hyperlinkUpdate(HyperlinkEvent link) {
		if (link.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
		try {
			System.err.println("Try");
			new BrowserLauncher().openURLinBrowser( link.getURL().toString() );
		} catch (BrowserLaunchingInitializingException e) {
			// TODO failure
			System.err.println("Failure");
		} catch (UnsupportedOperatingSystemException e) {
			// TODO failure
			System.err.println("Failure");
		}
		
	}

	public void windowStateChanged(WindowEvent event) {
		if (event.getNewState() == WindowEvent.WINDOW_CLOSING) {
			System.err.println("closing");
			System.exit(-1);
		}
	}

	public void windowActivated(WindowEvent arg0) {}

	public void windowClosed(WindowEvent arg0) {}

	public void windowClosing(WindowEvent arg0) {
		System.err.println("closing");
		System.exit(-1);
	}

	public void windowDeactivated(WindowEvent arg0) {}

	public void windowDeiconified(WindowEvent arg0) {}

	public void windowIconified(WindowEvent arg0) {}

	public void windowOpened(WindowEvent arg0) {}
}


/*
    (c) Copyright 2007 Hewlett-Packard Development Company, LP
    All rights reserved.

    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions
    are met:

    1. Redistributions of source code must retain the above copyright
       notice, this list of conditions and the following disclaimer.

    2. Redistributions in binary form must reproduce the above copyright
       notice, this list of conditions and the following disclaimer in the
       documentation and/or other materials provided with the distribution.

    3. The name of the author may not be used to endorse or promote products
       derived from this software without specific prior written permission.

    THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
    IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
    OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
    IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
    INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
    NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
    DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
    THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
    THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/