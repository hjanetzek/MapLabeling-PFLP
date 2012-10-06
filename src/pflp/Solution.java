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

package pflp;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;

import pflp.util.Semaphore;

/**
 * Represents a solution of the map labeling problems
 * (a mapping of the label to (x, y) - coordinates).
 * <br>
 * Note that different threads may access the same solution
 * object (e.g. the search thread & the paint() method).
 * This can be synchronized by using the methods acquireAccess() 
 * and releaseAccess().
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */

public class Solution
{
	//the instance for this solution
	private Instance instance = null;

	//where to place the labels
	private Label[] labels = null;

	//A Semaphore to syncronice access to the labels
	//between the visualization and the SearchThread(s)
	private Semaphore sem = new Semaphore(1);

	//static helper functions
	private static double to_double(String s)
	{
		try
		{
			return Double.valueOf(s).doubleValue();
		}
		catch (Exception e)
		{
			System.out.println("Failed to convert String \"" + s + "\" to double!");
			return 0.0;
		}
	}

	private static double r100(double d)
	{
		return (Math.round(d * 100) / 100);
	}

	public Solution(Instance inst)
	{
		this(inst, true);
	}

	/**
	 * constructs a new initial Solution to the given instance.
	 * For every node a list of neighbours will be generated.
	 * The initial position is one of the four possible corner positions 
	 * @param inst a reference to the {@link Instance instance object} 
	 */
	public Solution(Instance inst, boolean init_solution)
	{
		instance = inst;

		if (instance != null)
		{
			labels = new Label[instance.getNodes().length];

			for (int i = 0; i < instance.getNodes().length; i++)
				labels[i] = new Label((PointFeature) instance.getNodes()[i], i);
		}

		setNeighbours();

		if(init_solution)
			findInitialPlacement();
	}

	/**
	 * clones the given solution  
	 * nodes store only static information, that's why they will not be cloned 
	 */
	public Solution(Solution s)
	{
		instance = s.instance;
		labels = new Label[s.getLabels().length];

		for (int i = 0; i < s.getLabels().length; i++)
			labels[i] = new Label((Label) s.getLabels()[i]);

		setNeighbours();
	}

	/**
	 *  reads the solution from the given filename  
	 *  @param file filename 
	 */
	public Solution(String file)
	{
		BufferedReader r = null;
		String line = null;

		try
		{
			r = new BufferedReader(new FileReader(file));
			Vector dummy = new Vector();
			Vector dummy_labels = new Vector();
			int idx = -1;
			int i_label = 0;

			double x = 0, y = 0;
			double lbl_width = 0, lbl_height = 0;
			double priority = 1;
			int fontsize = 7;
			String text = "";
			String font = new String(PointFeature.DEFAULT_FONT);

			while ((line = r.readLine()) != null)
			{
				String[] splitted = line.split("[ (),]", -1);

				x = to_double(splitted[1]);
				y = to_double(splitted[2]);

				lbl_width = to_double(splitted[4]);
				lbl_height = to_double(splitted[5]);

				int i1 = line.indexOf("\"");
				int i2 = line.lastIndexOf("\"");
				text = line.substring(i1 + 1, i2);

				double tl_x = 0, tl_y = 0;
				boolean is_unset = false;

				//Beistriche im Namen machen Probleme - muss schnell gehen - irgendwann besser machen
				tl_x = to_double(splitted[splitted.length - 4]);
				tl_y = to_double(splitted[splitted.length - 3]);

				is_unset = splitted[splitted.length - 1].equals("0");

				PointFeature new_node = new PointFeature(x, y, lbl_width, lbl_height, priority, text, font, fontsize);
				Label dummy_label = new Label(new_node, i_label);
				i_label++;

				dummy_label.moveTo(x - tl_x, y - tl_y);
				dummy_label.setUnplacable(is_unset);

				dummy.add(new_node);
				dummy_labels.add(dummy_label);
			}

			Instance inst = new Instance(dummy, file);
			PFLPApp.instance = inst;
			instance = inst;

			labels = new Label[dummy_labels.size()];
			for (int i = 0; i < dummy_labels.size(); i++)
				labels[i] = (Label) dummy_labels.get(i);

			setNeighbours();
		}
		catch (FileNotFoundException e)
		{
			System.out.println("can't open file " + file);
			return;
		}
		catch (NumberFormatException e)
		{
			System.out.println("invalid file format!");
		}
		catch (IOException e)
		{
			System.out.println("error reading from " + file);
		}
	}

	private void setNeighbours()
	{
		for (int i = 0; i < labels.length; i++)
		{
			Label current = labels[i];

			//create a list of neighbours
			for (int j = 0; j < labels.length; j++)
			{
				if (i != j)
				{
					if (current.getNode().canIntersect(labels[j].getNode()))
						current.addNeighbour(labels[j]);
				}
			}
		}
	}

	private void findInitialPlacement()
	{
		for (int i = 0; i < labels.length; i++)
		{
			if (labels[i].hasNeighbours())
				labels[i].findInitialPlacement();
			else
				labels[i].moveTo(0., 0.);
		}
	}

	/**
	 * Dumps the current solution in a predefined format to the given file.
	 */
	public void dumpSolution(String filename) throws IOException
	{
		try
		{
			FileWriter f = new FileWriter(filename);
			String out = new String("");
			for (int i = 0; i < labels.length; i++)
			{
				Label l = labels[i];
				PointFeature n = l.getNode();
				out = "(" + r100(n.getX()) + "," + r100(n.getY()) + ") ";
				out += r100(l.getWidth()) + " " + r100(l.getHeight()) + " ";
				out += "\"" + n.getText() + "\" ";
				out += "(" + r100(l.getTopleft().x) + "," + r100(l.getTopleft().y) + ") ";
				out += (l.getUnplacable() || l.isOverlapping() ? "0" : "1");
				out += "\n";
				f.write(out);
			}

			f.close();
		}
		catch (IOException e)
		{
			throw (e);
		}
	}

	/**
	 * finds the label which covers the given point. The values for
	 * the x and y component of the point are expected to be in 
	 * world coordinates. 
	 * @param p the point where
	 * @return a reference to the covering label or null, if no label could be found
	 */
	public Label findLabelByCoordinates(Point p)
	{
		for (int i = labels.length - 1; i >= 0; i--)
		{
			Rectangle2D.Double r = labels[i].getRectangle();

			if (r.contains(p) && !labels[i].getUnplacable())
				return labels[i];

			//check if the point surrounds the given Node...
			PointFeature node = labels[i].getNode();
			int size = 2;
			Rectangle2D.Double r_node =
				new Rectangle2D.Double(node.getX() - size, node.getY() - size, 2 * size, 2 * size);
			if (r_node.contains(p))
				return labels[i];
		}

		return null;
	}

	/**
	 * searches the label with the most intersecting area with all
	 * of it's neighbours. If more than two labels cover a single point, 
	 * the corresponding area summed up multiple times.
	 * @return a reference to the found label or null, if no label intersects another one
	 */
	public int getMostIntersectingLabelIndex()
	{
		double max_area = -1;
		int max_lbl = -1;
		Rectangle2D r = new Rectangle2D.Double();

		for (int i = 0; i < labels.length; i++)
		{
			Label l = labels[i];

			if (l.getUnplacable())
				continue;

			Vector n = l.getNeighbours();
			double x = 0.0;
			Iterator it = n.iterator();

			while (it.hasNext())
			{
				Label l2 = (Label) it.next();

				if (l2.getUnplacable() || !l.doesIntersect(l2))
					continue;

				Rectangle2D.intersect(l.getRectangle(), l2.getRectangle(), r);
				x += r.getWidth() * r.getHeight();
			}

			if (x > 0.0 && x > max_area)
			{
				max_area = x;
				max_lbl = i;
			}
		}

		return max_lbl;
	}

	/**
	 * returns the array of all {@link LblLabel labels} corresponding to the given {@link Instance instance}
	 */
	public Label[] getLabels()
	{
		return labels;
	}

	/**
	 * counts the number of valid labeled cities (no intersections).
	 */
	public int countLabeledCities()
	{
		int set = 0;
		for (int i = 0; i < labels.length; i++)
		{
			if (!labels[i].getUnplacable() && !labels[i].isOverlapping())
				set++;
		}

		return set;
	}

	/**
	 * counts the number of intersecting or unused labels.
	 */
	public int countUnlabeledCities()
	{
		return (labels.length - countLabeledCities());
	}

	/**
	 * 
	 * @return true <-> at least one label is obstructed by another one
	 */
	public boolean existsOverlapping()
	{
		for(int k=0; k<labels.length; k++)
		{
			if(labels[k].isOverlapping())
				return true;
		}
		
		return false;
	}

	/** returns a clone object of the current solution
	 * @see hugs.Solution#copy()
	 */
	public Solution copy()
	{
		return new Solution(this);
	}

	/** returns the size of the solution
	 * @see hugs.Solution#size()
	 */
	public int size()
	{
		if (instance == null)
			return 0;
		else
			return instance.size();
	}

	/**
	 * @return reference to the associated instance
	 */
	public Instance getInstance()
	{
		return instance;
	}

	/**
	 * corresponds to a wait() operation of a Semphore.
	 * The call is blocking until access to the current object
	 * is granted.
	 */
	public void acquireAccess()
	{
		try
		{
			sem.acquire();
		}
		catch (InterruptedException e)
		{
			System.out.println("acquire_access() interrupted!");
		}
	}

	/**
	 * corresponds to a free() operation of a Semphore.
	 * After release_access() no changes to any {@link LblLabel labels} or 
	 * properties of the current solution should be done.
	 */
	public void releaseAccess()
	{
		sem.release();
	}

}
