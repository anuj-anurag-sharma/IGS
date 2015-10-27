package com.myt.binary.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.LogManager;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.util.StringUtils;

import sample.ABCD;


public class MainScreen {

	private JFrame frame;

	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainScreen window = new MainScreen();
					window.initialize();
					window.frame.setVisible(true);

					System.out.println("Application started....");

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		JTextArea jlogArea=new JTextArea();
		
		JScrollPane sp = new JScrollPane(jlogArea);
		
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.CYAN);
		Toolkit tk = Toolkit.getDefaultToolkit();
		int xSize = ((int) tk.getScreenSize().getWidth());
		int ySize = ((int) tk.getScreenSize().getHeight());
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new GridBagLayout());
		
		JPanel jp3 = new JPanel(new GridLayout(4, 2));
		JLabel cuurencyLbl = new JLabel("Currency");
		JLabel expiaryLbl = new JLabel("Expiary");
		JLabel stratgiesLbl = new JLabel("Chart");
		
		final JTextField currencyTxt = new JTextField("FM.D.EURUSD24.EURUSD24.IP");
		String[] expArr={"FIVE_MINUTES","ONE_MINUTE","SIXTY_MINUTES","TWENTY_MINUTES","TWO_MINUTES"};
		final JList<String> expLst=new JList<>(expArr);	
		expLst.setVisibleRowCount(1);
		expLst.setAutoscrolls(true);
		expLst.setBorder(new EmptyBorder(10,10, 10, 10));
		final JButton submitBtn = new JButton("Start");
		String[] strArr={ABCD.SEC_30,ABCD.MIN_1};
		final JList<String> startgiesLst=new JList<>(strArr);	
		startgiesLst.setBorder(new EmptyBorder(10,10, 10, 10));
		jp3.add(cuurencyLbl);jp3.add(currencyTxt);
		jp3.add(expiaryLbl);jp3.add(expLst);
		jp3.add(stratgiesLbl);jp3.add(startgiesLst);
		jp3.add(submitBtn);
		frame.add(jp3);
		submitBtn.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String currTxt=currencyTxt.getText();
				String expTxtValue=expLst.getSelectedValue();
				String chartValue=startgiesLst.getSelectedValue();
				if(StringUtils.hasText(currTxt) && StringUtils.hasText(expTxtValue) && expLst.getSelectedIndices().length==1 && StringUtils.hasText(chartValue) && startgiesLst.getSelectedIndices().length==1){
					System.out.println("Currency Text "+currTxt+" expiary "+expTxtValue+" chartValue "+chartValue);
					ABCD.EPIC=currTxt;
					ABCD.STR_ENABLE=chartValue;
				    ABCD.start();
				    submitBtn.setEnabled(false);
				}else{
					JOptionPane.showMessageDialog(frame, "Please enter all value Correctly", "Value not entered Properly", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
	}
	
	

}
