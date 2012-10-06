/*
Copyright Dietmar Ebner, 2004, ebner@apm.tuwien.ac.at

This file is part of PFLP.

PFLP is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

PFLP is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PFLP; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/


/**
 * creats the user interface and the {@link Visualization visualization object}
 *
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */

package pflp.ui;

import java.util.*;
import javax.swing.*;

import pflp.PFLPApp;

import java.awt.*;
import java.awt.event.*;

public class GUI implements MouseListener
{
	private static final String APP_TITLE = "PFLP Search Engine";

	//UI items
	private JMenuBar menu_bar = new JMenuBar();
	private JMenu file_menu = new JMenu("File");
	private JFrame frame = new JFrame(APP_TITLE);
	private JMenuItem btn_load_instance = new JMenuItem("Load instance...");
	private JMenuItem btn_random_instance = new JMenuItem("Create random instance");
	private JMenuItem btn_load_solution = new JMenuItem("Load solution...");
	private JMenuItem btn_save_solution = new JMenuItem("Save solution...");
	private JMenuItem btn_export_eps = new JMenuItem("Export to EPS...");
	private JMenuItem btn_exit_program = new JMenuItem("Quit");
	private JComboBox algorithm_combo = null;
	private JComboBox poll_combo = null;
	private JLabel status_bar = new JLabel();
	private JToolBar toolbar = new JToolBar();
	private JToolBar status = new JToolBar();

	private JButton btn_zoom = null;
	private JButton btn_zoom_in = null;
	private JButton btn_zoom_out = null;
//	private JButton btn_settings = null;
	private JButton btn_reset_instance = null;

	private ButtonGroup radio_group = new ButtonGroup();
	private JRadioButton rdo_blank = new JRadioButton("blank labels");
	private JRadioButton rdo_text = new JRadioButton("text");
	private JRadioButton rdo_debuginfo = new JRadioButton("label info");

	private JCheckBox cb_point_selection = null;
	private JButton btn_run_pause = null;
	private ImageIcon img_run = new ImageIcon(PFLPApp.class.getResource("ui/images/run.gif" ));
	private ImageIcon img_pause = new ImageIcon(PFLPApp.class.getResource("ui/images/pause.gif"));

	private JScrollPane scroll_pane = null;
	private Visualization visualization = null;
	private final JFileChooser file_dialog = new JFileChooser("./problems");
	private final JFileChooser sol_file_dialog = new JFileChooser("./solutions");

	//private members
	private double zoom = 1;
	
	/**
	 * 
	 */
	public GUI()
	{
		super();
		setupUI();

		//1 sec
		poll_combo.setSelectedIndex(0);
		visualization.setRedrawIntervall(1);
	}

	private void setupUI()
	{
		rdo_blank.setActionCommand("blank");
		rdo_text.setActionCommand("text");
		rdo_debuginfo.setActionCommand("debuginfo");
		rdo_debuginfo.setSelected(true);
		
		radio_group.add(rdo_blank);
		radio_group.add(rdo_text);
		radio_group.add(rdo_debuginfo);

		Vector v = new Vector(10, 1);
		v.add(new Integer(1));
		v.add(new Integer(3));
		v.add(new Integer(10));
		v.add(new Integer(30));
		v.add(new Integer(60));
		v.add(new String("never"));
		v.add(new String("always"));
		poll_combo = new JComboBox(v);

		algorithm_combo = new JComboBox();
		for (int i = 0; PFLPApp.algorithms != null && i < PFLPApp.algorithms.length; i++)
			algorithm_combo.addItem(PFLPApp.algorithms[i].getAlgorithmName());

		btn_exit_program.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
		btn_random_instance.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
		btn_load_instance.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
		btn_load_solution.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
		btn_export_eps.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));

		file_menu.add(btn_random_instance);
		file_menu.add(btn_load_instance);
		file_menu.addSeparator();
		file_menu.add(btn_load_solution);
		file_menu.add(btn_save_solution);
		file_menu.addSeparator();
		file_menu.add(btn_export_eps);
		file_menu.addSeparator();
		file_menu.add(btn_exit_program);
		menu_bar.add(file_menu);

		btn_run_pause = new JButton(img_run);
		cb_point_selection = new JCheckBox("point selection", true);

		btn_zoom = new JButton(new ImageIcon(PFLPApp.class.getResource("ui/images/zoom.gif")));
		btn_zoom_in = new JButton(new ImageIcon(PFLPApp.class.getResource("ui/images/zoom_in.gif")));
		btn_zoom_out = new JButton(new ImageIcon(PFLPApp.class.getResource("ui/images/zoom_out.gif")));
//		btn_settings = new JButton(new ImageIcon(PFLPApp.class.getResource("ui/images/preferences.gif")));
		btn_reset_instance = new JButton(new ImageIcon(PFLPApp.class.getResource("ui/images/reset.gif")));

		btn_zoom.setBorder(null);
		btn_zoom_in.setBorder(null);
		btn_zoom_out.setBorder(null);
//		btn_settings.setBorder(null);
		btn_reset_instance.setBorder(null);

		toolbar.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolbar.add(menu_bar);
		//toolbar.addSeparator();
		toolbar.add(algorithm_combo);
//		toolbar.add(btn_settings);
		//toolbar.addSeparator();
		toolbar.add(btn_run_pause);
		toolbar.add(cb_point_selection);
		//toolbar.addSeparator();
		toolbar.add(btn_reset_instance);
		//toolbar.addSeparator();
		toolbar.add(btn_zoom);
		toolbar.add(btn_zoom_in);
		toolbar.add(btn_zoom_out);
		//toolbar.addSeparator();

		toolbar.add(rdo_blank);
		toolbar.add(rdo_text);
		toolbar.add(rdo_debuginfo);
		//toolbar.addSeparator();
		toolbar.add(new JLabel("poll frequency [sec]"));
		toolbar.add(poll_combo);

		status.add(status_bar);

		visualization = new Visualization();
		visualization.addMouseListener(this);

		scroll_pane = new JScrollPane(visualization);

		JPanel dummy = new JPanel();
		dummy.setLayout(new BorderLayout());
		dummy.add("North", toolbar);

		frame.getContentPane().add("North", dummy);
		frame.getContentPane().add("Center", scroll_pane);
		frame.getContentPane().add("South", status);

		frame.setSize(1200, 950);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = frame.getSize();
		frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

		setZoom(1.);

		//setup action listener
		RadioListener the_radio_listener = new RadioListener();
		rdo_blank.addActionListener(the_radio_listener);
		rdo_text.addActionListener(the_radio_listener);
		rdo_debuginfo.addActionListener(the_radio_listener);

		cb_point_selection.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PFLPApp.setOptionPointSelection(((JCheckBox) e.getSource()).isSelected());
			}
		});

		btn_run_pause.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (PFLPApp.isBusy())
				{
					PFLPApp.onStopAlgorithm();
					changeButtonState(true);
				}
				else
				{
					if (PFLPApp.gui != null && PFLPApp.instance == null)
					{
						JOptionPane.showMessageDialog(null, "Load a instance first!");
						return;
					}

					changeButtonState(false);
					PFLPApp.onStartAlgorithm();
				}

				//redraw(false);
			}

		});

		poll_combo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int seconds = -1;
				boolean debug_iterations = false;
				try
				{
					seconds = ((Integer) (poll_combo.getSelectedItem())).intValue();
				}
				catch (Exception ex)
				{
				}

				try
				{
					if(((String) poll_combo.getSelectedItem()).equals("always"))
						debug_iterations = true;
				}
				catch (Exception ex)
				{
				}
				
				if (visualization != null)
					visualization.setRedrawIntervall(seconds); //-1 <-> never
				
				PFLPApp.setOptionDebugIterations(debug_iterations);
			}
		});

		algorithm_combo.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PFLPApp.onAlgorithmChanged(algorithm_combo.getSelectedIndex());
			}
		});

		btn_load_instance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (file_dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					PFLPApp.onLoadInstance(file_dialog.getSelectedFile().getPath());
			}
		});

		btn_random_instance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PFLPApp.onCreateRandomInstance();
			}
		});

		btn_load_solution.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (sol_file_dialog.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION)
					PFLPApp.onLoadSolution(sol_file_dialog.getSelectedFile().getPath());
			}
		});

		btn_save_solution.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (file_dialog.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
					PFLPApp.onSaveSolution(file_dialog.getSelectedFile().getPath());
			}
		});

		btn_export_eps.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (visualization != null && file_dialog.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION)
				{
					visualization.exportEPS(file_dialog.getSelectedFile().getPath());
					JOptionPane.showMessageDialog(null, "Solution successfully exported to file\n" + file_dialog.getSelectedFile().getPath());
				}
			}
		});

		btn_exit_program.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PFLPApp.onQuitApplication(0);
			}
		});

		btn_reset_instance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				PFLPApp.onResetSolution();
			}
		});

		btn_zoom.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setZoom(1);
				redraw();
			}
		});

		btn_zoom_in.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setZoom(zoom * 1.2);
				redraw();
			}
		});

		btn_zoom_out.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setZoom(zoom * 0.8);
				redraw();
			}
		});

//		btn_settings.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				//				if (settings_bar.isShowing())
//				//					settings_bar.setVisible(false);
//				//				else
//				//					settings_bar.setVisible(true);
//			}
//		});

		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				PFLPApp.onQuitApplication(0);
			}
		});

		//show the whole frame
		frame.show();
		setStatusText("ready");

	}

	/**
	 * writes the given text in the status bar
	 * @param txt text to display
	 */
	public void setStatusText(String txt)
	{
		status_bar.setText(txt);
	}

	/**
	 * @return visualization object
	 */
	public Visualization getVisualization()
	{
		return visualization;
	}

	/**
	 * invoked by the appropriate action listener.
	 * See {@link LblVisualization LblVisualization} for possible values
	 * @param mode new selected mode
	 */
	public void changeLabelStyle(int mode)
	{
		if (mode >= 1 && mode <= 3)
		{
			visualization.setLabelStyle(mode);
		}
	}

	private void setZoom(double f)
	{
		zoom = Math.max(1, f);

		if (zoom > 1)
		{
			btn_zoom_out.setEnabled(true);
			btn_zoom.setEnabled(true);

			Dimension d = scroll_pane.getViewport().getExtentSize();
			visualization.setPreferredSize(new Dimension((int) (d.width * zoom), (int) (d.height * zoom)));
		}
		else
		{
			btn_zoom_out.setEnabled(false);
			btn_zoom.setEnabled(false);

			visualization.setPreferredSize(null);
		}

		visualization.revalidate();
	}

	/**
	 * tells the gui to refresh the current screen (no solution update)
	 */
	public void redraw()
	{
		redraw(false);
	}

	private void changeButtonState(boolean enabled)
	{
		if(enabled)
		{
			algorithm_combo.setEnabled(true);
			btn_reset_instance.setEnabled(true);
			if (btn_run_pause.getIcon() != img_run)
				btn_run_pause.setIcon(img_run);
			
			btn_load_instance.setEnabled(true);
			btn_random_instance.setEnabled(true);
			btn_load_solution.setEnabled(true);
			
			visualization.backgroundColor = Color.WHITE;

		}
		else
		{
			algorithm_combo.setEnabled(false);
			btn_reset_instance.setEnabled(false);
			if (btn_run_pause.getIcon() != img_pause)
				btn_run_pause.setIcon(img_pause);

			btn_load_instance.setEnabled(false);
			btn_random_instance.setEnabled(false);
			btn_load_solution.setEnabled(false);
			
			visualization.backgroundColor = Color.getHSBColor(50,50,50);
		}
	}
	
	/**
	 * tells the gui to refresh the current screen
	 */
	public void redraw(boolean update_solution)
	{
		String title = APP_TITLE;

		if(PFLPApp.instance != null)
			title += " - " + PFLPApp.instance.getName();
		frame.setTitle(title);
		
		if (PFLPApp.isBusy())
			changeButtonState(false);
		else
			changeButtonState(true);

		if (update_solution)
			visualization.updateClone();

		visualization.repaint();
	}

	public void mouseClicked(MouseEvent e)
	{
		if (visualization == null || e.getButton() != MouseEvent.BUTTON1)
			return;

		visualization.handleMouseClick(e.getPoint());
	}

	public void mouseReleased(MouseEvent e)
	{
	}
	public void mouseEntered(MouseEvent e)
	{
	}
	public void mousePressed(MouseEvent e)
	{
	}
	public void mouseExited(MouseEvent e)
	{
	}
}

class RadioListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		String value = e.getActionCommand();
		int option = 0;
		if (value == "blank")
			option = Visualization.OPTION_BLANK_LABELS;
		else if (value == "text")
			option = Visualization.OPTION_TEXT_LABELS;
		else if (value == "debuginfo")
			option = Visualization.OPTION_DEBUGINFO_LABELS;

		if (option != 0 && PFLPApp.gui != null)
			PFLPApp.gui.changeLabelStyle(option);
	}
}
