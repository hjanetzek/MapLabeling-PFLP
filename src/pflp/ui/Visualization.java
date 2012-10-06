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

package pflp.ui;

import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import javax.swing.*;
import java.util.*;
import java.util.Timer;

import pflp.*;
import pflp.Label;
import pflp.util.epsgraphics.*;

public class Visualization extends JPanel
{
	/**
	 * valid constant for the label style (blank)
	 */
	public static final int OPTION_BLANK_LABELS = 1;
	/**
	 * valid constant for the label style (text)
	 */
	public static final int OPTION_TEXT_LABELS = 2;
	/**
	 * valid constant for the label style (debuginfo)
	 */
	public static final int OPTION_DEBUGINFO_LABELS = 3;

	private static final int BORDER_SPACING = 30;
	private static final Color DEFAULT_NODE_COLOR = Color.green;
	private static final Color DEFAULT_UNPLACED_NODE_COLOR = Color.red;
	private static final Color DEFAULT_SHADED_LABEL_COLOR = Color.lightGray;
	private static final Color DEFAULT_OVERLAPPING_LABEL_COLOR = new Color(160, 50, 50);
	private static final Color DEFAULT_VALID_LABEL_COLOR = new Color(220, 220, 220);
	private static final Color ACTIVE_LABEL_COLOR = new Color(150, 150, 150);

	private static final Color DEFAULT_NODE_COLOR_EPS = Color.darkGray;
	private static final Color DEFAULT_UNPLACED_NODE_COLOR_EPS = Color.red;
	private static final Color DEFAULT_OVERLAPPING_LABEL_COLOR_EPS = Color.red;
	private static final Color DEFAULT_VALID_LABEL_COLOR_EPS = new Color(235,235,235);
	
	//values set by update_scale_factor();
	private double scale_factor = 1;
	private int h_offset = 0;
	private int v_offset = 0;

	//current instance / solution
	private Solution sol_clone = null;
	private String[] sol_debuginfos = null;
	private int nMarked = 0;
	private String sol_debuginfo_status = null;

	//redraw timer
	private Timer redraw_timer = null;

	//display option
	private int label_style = OPTION_DEBUGINFO_LABELS;
	public Color backgroundColor = Color.WHITE;

	/**
	 * a reference to the current selcted {@link Label label}, if any
	 */
	private Label selected_label = null;
	private boolean startsWith;

	public Visualization()
	{
		super();
		setBackground(Color.white);
	}

	/**
	  * used to set the preferred label style
	  */
	public void setLabelStyle(int style)
	{
		label_style = style;
		repaint();
	}

	/**
	 * updates the Solution that is displayed
	 */
	public void updateClone()
	{
		updateClone(true);
	}

	/**
	 * updates the Solution that is displayed
	 */
	public void updateClone(boolean force_action)
	{
		if (PFLPApp.solution == null)
		{
			sol_clone = null;
			sol_debuginfos = null;
			return;
		}

		if (!force_action && !PFLPApp.isBusy())
			return;

		PFLPApp.solution.acquireAccess();
		
		sol_clone = PFLPApp.solution.copy();
		sol_debuginfos = null;
		sol_debuginfo_status = null;
		nMarked = 0;
//		System.gc();

		if (PFLPApp.getRunningAlgorithm() != null)
		{
			sol_debuginfos = new String[sol_clone.size()];
			for (int i = 0; i < sol_clone.size(); i++)
			{
				sol_debuginfos[i] = PFLPApp.getRunningAlgorithm().getLabelInfo(i);
				if(sol_debuginfos[i] != null && sol_debuginfos[i].startsWith("[*]"))
					nMarked ++;
			}
			sol_debuginfo_status = PFLPApp.getRunningAlgorithm().getStatusString();
		}

		PFLPApp.solution.releaseAccess();
		selected_label = null;
	}

	/**
	 * called by the framework to redraw the frame
	 */
	public void paint(Graphics g)
	{
		setBackground(backgroundColor);
		super.paint(g); //paint background

		if (PFLPApp.instance != null)
		{
			updateScaleFactor(PFLPApp.instance.getMapWidth(), PFLPApp.instance.getMapHeight());

			drawLabels(g, true);
			drawLabels(g);
			drawNodes(g);
			drawSelected(g);

			if (sol_clone != null && PFLPApp.gui != null)
			{
				String stext = new String();
				int set = sol_clone.countLabeledCities();

				stext = "cities: " + sol_clone.size();
				stext += ", labeled: " + set;
				stext += ", unlabeled: " + (sol_clone.size() - set);
				stext += ", marked: " + nMarked;

				if(sol_debuginfo_status != null)
					stext +=", algorithm status: [" + sol_debuginfo_status + "]";
				
				PFLPApp.gui.setStatusText(stext);
			}
		}
	}

	/** 
	 * exports the current instance to the given filename using the current
	 * display settings. 
	 * @param file_name target file
	 */
	public void exportEPS(String file_name)
	{
		FileOutputStream outputStream;
		try
		{
			System.out.println("starting eps export...\n");

			outputStream = new FileOutputStream(file_name);

			// Draw stuff
			if (PFLPApp.instance != null)
			{
				//Calculate current map boundaries...
				int max_x = 0;
				int max_y = 0;

				for (int i = 0; i < PFLPApp.solution.size(); i++)
				{
					Label l = PFLPApp.solution.getLabels()[i];

					Rectangle2D.Double r = (Rectangle2D.Double) l.getRectangle();
					max_x = (int) Math.max(max_x, Math.ceil(r.getMaxX()));
					max_y = (int) Math.max(max_y, Math.ceil(r.getMaxY()));
				}

				max_x += 2 * BORDER_SPACING;
				max_y += 2 * BORDER_SPACING;

				// Create a new document
				EpsGraphics2D g = new EpsGraphics2D("Sample Labeling", outputStream, 0, 0, max_x, max_y);

				// Turn off accurate text mode
				g.setAccurateTextMode(false);

				// export using world coordinates
				scale_factor = 1; 
				h_offset = v_offset = BORDER_SPACING;

//				drawLabels(g, true);
				drawLabels(g, false, true);
				drawNodes(g, true);

				// Flush and close the document (don't forget to do this!)
				g.flush();
				g.close();
			}

			System.out.println("done\n");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * handles a left click at the given Point
	 * @param p the coordinate of the mouse click
	 */
	public void handleMouseClick(Point p)
	{
		if (sol_clone == null)
			return;

		Point p_world = getWorldCoordinates(p.x, p.y);
		Label l = sol_clone.findLabelByCoordinates(p_world);

		if (l != null)
		{

			if (selected_label != l)
			{
				selected_label = l;

				//show debug output...
				System.out.println("-------------------------------------------------------------");
				System.out.println(l.getNode().getText());
				System.out.println(
					"    top_left:       " + "(" + l.getTopleft().getX() + ", " + l.getTopleft().getY() + ")");
				System.out.println("    height:         " + l.getHeight());
				System.out.println("    width:          " + l.getWidth());
				System.out.println("    lbl_v_offset:   " + l.getOffsetVertical());
				System.out.println("    lbl_h_offset:   " + l.getOffsetHorizontal());
				System.out.println("    is_overlapping: " + l.isOverlapping());
				System.out.println("    is_unset:       " + l.getUnplacable());

				if (l.isOverlapping())
				{
					System.out.println("    intersecting labels:");
					Vector n = l.getNeighbours();

					Iterator it = n.iterator();
					while (it.hasNext())
					{
						Label x = (Label) it.next();

						if (l.doesIntersect(x) && !x.getUnplacable())
							System.out.println("         *)" + x.getNode().getText());
					}
				}

				System.out.println("-------------------------------------------------------------");
			}
			else
			{
				selected_label = null;
			}

			repaint();
		}
	}

	private void drawNodes(Graphics g)
	{
		drawNodes(g, false);
	}	
	
	private void drawNodes(Graphics g, boolean eps_export)
	{
		if (PFLPApp.instance != null)
		{
			for (int i = 0; i < PFLPApp.instance.size(); i++)
			{
				Color c = eps_export ? DEFAULT_NODE_COLOR_EPS : DEFAULT_NODE_COLOR;

				if (sol_clone != null && sol_clone.getLabels()[i].getUnplacable())
					c = eps_export ? DEFAULT_UNPLACED_NODE_COLOR_EPS : DEFAULT_UNPLACED_NODE_COLOR;

				int size = 2 + (int) PFLPApp.instance.getNodes()[i].getPriority();
				if(eps_export )
				{
					if(sol_clone.getLabels()[i].getUnplacable())
						size += 1;
					else
						size -= 1;
				}
				
				drawNode(g, PFLPApp.instance.getNodes()[i], c, size);
			}
		}
	}

	private void drawNode(Graphics g, PointFeature node, Color c, int argsize)
	{
		Point p = getScreenCoordinates(node.getX(), node.getY());

		int size = Math.max(0, Math.min(9, argsize ));
		int[] ax = new int[4], ay = new int[4];
		ax[0] = p.x - size;
		ax[1] = p.x;
		ax[2] = p.x + size;
		ax[3] = p.x;
		ay[0] = p.y;
		ay[1] = p.y - size;
		ay[2] = p.y;
		ay[3] = p.y + size;
		Polygon pol = new Polygon(ax, ay, 4);
		g.setColor(c);
		g.fillPolygon(pol);
		g.setColor(Color.black);
		g.drawPolygon(pol);
	}

	private void drawLabels(Graphics g)
	{
		drawLabels(g, false);
	}

	private void drawLabels(Graphics g, boolean unset_only)
	{
		drawLabels(g, unset_only, false);
	}
	
	private void drawLabels(Graphics g, boolean unset_only, boolean eps_export)
	{
		if (PFLPApp.instance != null)
		{
			if (sol_clone == null)
			{
				for (int i = 0; i < PFLPApp.instance.size(); i++)
					drawLabel(g, PFLPApp.instance.getNodes()[i], eps_export);
			}
			else
			{
				if (sol_clone == null)
					updateClone();

				for (int i = 0; i < sol_clone.size(); i++)
				{
					if (unset_only && sol_clone.getLabels()[i].getUnplacable())
						drawLabel(g, PFLPApp.instance.getNodes()[i], eps_export);
					else if (!unset_only && !sol_clone.getLabels()[i].getUnplacable())
						drawLabel(g, sol_clone.getLabels()[i], eps_export);
				}
			}
		}
	}

	private void drawLabel(Graphics g, PointFeature node, boolean eps_export)
	{
		//if we have no initial solution, draw labels shaded and centerd behind the 
		//node to indicate it's size

		int lbl_width = (int) ((double) node.getWidth() * scale_factor);
		int lbl_height = (int) ((double) node.getHeight() * scale_factor);
		Point top_left = getScreenCoordinates(node.getX(), node.getY());

		g.setColor(DEFAULT_SHADED_LABEL_COLOR);
		g.drawRect(top_left.x - lbl_width / 2, top_left.y - lbl_height / 2, lbl_width, lbl_height);
	}

	private void drawLabel(Graphics g, Label placement, boolean eps_export)
	{
		if (placement.isOverlapping())
			drawLabel(g, placement, eps_export ? DEFAULT_OVERLAPPING_LABEL_COLOR_EPS : DEFAULT_OVERLAPPING_LABEL_COLOR);
		else
			drawLabel(g, placement, eps_export ? DEFAULT_VALID_LABEL_COLOR_EPS : DEFAULT_VALID_LABEL_COLOR);
	}

	private void drawLabel(Graphics g, Label label, Color c)
	{
		int lbl_width = (int) ((double) label.getWidth() * scale_factor);
		int lbl_height = (int) ((double) label.getHeight() * scale_factor);

		Point2D.Double top_left_world = label.getTopleft();
		Point top_left = getScreenCoordinates(top_left_world.x, top_left_world.y);

		//draw the label

		if (label_style == OPTION_DEBUGINFO_LABELS && sol_debuginfos != null && sol_debuginfos[label.getIndex()] != null && sol_debuginfos[label.getIndex()].startsWith("[*]"))
			g.setColor(c.darker());
		else
			g.setColor(c);
		g.fillRect(top_left.x, top_left.y, lbl_width, lbl_height);
		g.setColor(Color.black);
		g.drawRect(top_left.x, top_left.y, lbl_width, lbl_height);
		

		if (label_style == OPTION_TEXT_LABELS || label_style == OPTION_DEBUGINFO_LABELS)
		{
			String text = new String();
			if (label_style == OPTION_TEXT_LABELS)
				text = label.getNode().getText();
			else if (sol_debuginfos != null && sol_debuginfos[label.getIndex()] != null)
			{
				text = sol_debuginfos[label.getIndex()];
				if(text.startsWith("[*]"))
					text = text.substring(3);
			}

			int font_h_offset = 0;
			int font_v_offset = 0;

			int fontsize = (int) Math.floor(label.getNode().getFontsize() * scale_factor);
			if (label_style == OPTION_DEBUGINFO_LABELS)
				fontsize = Math.min(10, fontsize *= 0.6);

			Font f = new Font(label.getNode().getFont(), Font.PLAIN, fontsize);
			g.setFont(f);

			Rectangle2D rect = f.getStringBounds(text, ((Graphics2D) g).getFontRenderContext());

			font_h_offset = (int) (Math.max(0, (lbl_width - rect.getWidth()) / 2));
			font_v_offset = (int) (Math.max(0, (lbl_height - rect.getHeight()) / 2));

			g.drawString(text, top_left.x + font_h_offset, top_left.y + lbl_height - font_v_offset);
		}
	}

	private void drawSelected(Graphics g)
	{
		if (selected_label != null)
		{
			drawLabel(g, selected_label, ACTIVE_LABEL_COLOR);

			Point from = getScreenCoordinates(selected_label.getCenter().getX(), selected_label.getCenter().getY());

			Iterator it = selected_label.getNeighbours().iterator();

			while (it.hasNext())
			{
				Label next = (Label) it.next();
				if (next.getUnplacable())
					continue;

				Point2D.Double p2 = next.getCenter();
				Point to = getScreenCoordinates(p2.x, p2.y);

				g.setColor(Color.green);
				g.drawLine(from.x, from.y, to.x, to.y);
				g.setColor(Color.blue);

				g.drawOval(to.x - 1, to.y - 1, 3, 3);
				g.fillOval(to.x - 1, to.y - 1, 3, 3);
			}

			g.drawOval(from.x - 1, from.y - 1, 3, 3);
			g.fillOval(from.x - 1, from.y - 1, 3, 3);
		}
	}

	private Point getScreenCoordinates(double x, double y)
	{
		return new Point((int) (x * scale_factor + h_offset), (int) (y * scale_factor + v_offset));
	}

	private Point getWorldCoordinates(int x, int y)
	{
		return new Point((int) ((x - h_offset) / scale_factor), (int) ((y - v_offset) / scale_factor));
	}

	private void updateScaleFactor(int world_width, int world_height)
	{
		Rectangle bounds = getBounds();
		int screen_width = bounds.width - 2 * BORDER_SPACING;
		int screen_height = bounds.height - 2 * BORDER_SPACING;

		double scale_x = (double) screen_width / (double) world_width;
		double scale_y = (double) screen_height / (double) world_height;

		scale_factor = Math.max(0, Math.min(scale_x, scale_y));
		h_offset = BORDER_SPACING;
		v_offset = BORDER_SPACING;

		if (scale_x > scale_y)
			h_offset += (int) ((scale_x - scale_y) * world_width / 2);
		else
			v_offset += (int) ((scale_y - scale_x) * world_height / 2);
	}

	/**
	 * sets the intervall [seconds] to redraw the screen..
	 *
	 */

	public void setRedrawIntervall(int seconds)
	{
		if (redraw_timer != null)
		{
			redraw_timer.cancel();
			redraw_timer = null;
		}

		if (seconds != -1)
		{
			redraw_timer = new Timer();
			redraw_timer.schedule(new RedrawTask(this), 0, seconds * 1000);
		}
	}
}
