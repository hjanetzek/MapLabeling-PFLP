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


package pflp.search;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.util.Iterator;

import pflp.Label;
import pflp.PFLPApp;
import pflp.Solution;

/**
 * hirsch's method
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class HirschLabeling extends SearchThread
{
	public static final int X = 0;
	public static final int Y = 1;

	private static final int MAX_ITERATIONS = 1000;

	private static final int SLIDE_HORIZONTAL = 1;
	private static final int SLIDE_VERTICAL = 2;

	private static final int METHOD1 = 1; //incremental movement
	private static final int METHOD2 = 2; //absolut movement

	private int size = 0;
	private Label[] labels = null;
	private Solution solution = null;
	private double[][] overlap_vectors = null;
	private int nInterations = 0;

	public HirschLabeling()
	{
		super();
		name = new String("hirsch");
	}

	public String getLabelInfo(int i)
	{
		if (i >= 0 && i < size)
		{
			return new String(
				"("
					+ ((double) Math.round(overlap_vectors[i][X] * 10)) / 10
					+ ", "
					+ ((double) Math.round(overlap_vectors[i][Y] * 10)) / 10
					+ ")");
		}
		else
		{
			return null;
		}
	}

	protected void precompute()
	{
		int i = 0;
		PFLPApp.solution = new Solution(PFLPApp.instance, false);

		labels = PFLPApp.solution.getLabels();
		size = PFLPApp.solution.size();
		solution = PFLPApp.solution;

		overlap_vectors = new double[size][2];
		//init: special zone right from the point...
		for (i = 0; i < size; i++)
		{
			overlap_vectors[i][X] = 0.0;
			overlap_vectors[i][Y] = 0.0;

			labels[i].moveTo(0.0, labels[i].getHeight());
		}

		nInterations = 0;

		//initialize overlap vectors...
		computeOverlapVectors();
	}

	protected boolean iterate()
	{
		if(nInterations > MAX_ITERATIONS)
		{
			if(PFLPApp.getOptionPointSelection())
				super.cleanupSolution(solution);
			return true;
		}
		
		if (!solution.existsOverlapping())
			return true;

		nInterations++;
		computeOverlapVectors();

		//apply method 2 every sixth iteration 
		if (nInterations % 6 != 0)
			mapSweep(METHOD1);
		else
			mapSweep(METHOD2);

		return false;
	}

	private void mapSweep(int method)
	{
		for (int k = 0; k < size; k++)
		{
			Label current = labels[k];
			if (method == METHOD1)
			{
				//like described in the paper by christensen/marks/shieber 
				//the radius of the surrounding circle ist reduced to zero to make
				//the method comparable to other algorithms.
				
				//rule1 + rule2 is obsolete, since we reduced the size of the circle 
				//to zero. 
//				double angle = Math.atan(overlap_vectors[k][Y] / overlap_vectors[k][X]);
//				angle = angle * 180 / Math.PI;
//				if(overlap_vectors[k][Y] < 0)
//					angle += 180;
				
				boolean slideable_v = canSlideVertical(current);
				boolean slideable_h = canSlideHorizontal(current);

				if (!slideable_h && !slideable_v)
					continue;

				if ((slideable_h || slideable_v))
				{
					boolean slide_h = false;
					if (slideable_h && slideable_v)
					{
						if (Math.abs(overlap_vectors[k][X]) > Math.abs(overlap_vectors[k][Y]))
							slide_h = true;
						else
							slide_h = false;
					}
					else if (slideable_h)
					{
						slide_h = true;
					}
					else if (slideable_v)
					{
						slide_h = false;
					}

					slideBy(
						current,
						slide_h ? SLIDE_HORIZONTAL : SLIDE_VERTICAL,
						slide_h ? overlap_vectors[k][X] : overlap_vectors[k][Y]);
				}
			}
			else //METHOD 2 (absolut movement)
				{
				//implemented without using special zones. the method just moves
				//the label to the quadrant the vector indicates (4pos model)
				double h_offset = current.getOffsetHorizontal();
				double v_offset = current.getOffsetVertical();
				
				if(overlap_vectors[k][X] != 0.)
					h_offset = overlap_vectors[k][X] >= 0 ? 0.0 : current.getWidth();
				
				if(overlap_vectors[k][Y] != 0.)
					v_offset = overlap_vectors[k][Y] >= 0 ? 0.0 : current.getHeight();
					
				current.moveTo(h_offset, v_offset);
			}
		}
	}

	private void computeOverlapVectors()
	{
		for (int i = 0; i < size; i++)
			computeOverlapVector(i);
	}

	private void computeOverlapVector(int i)
	{
		Label l1 = labels[i];

		overlap_vectors[i][X] = overlap_vectors[i][Y] = 0.0;

		Iterator it = labels[i].getNeighbours().iterator();
		while (it.hasNext())
		{
			Label l2 = (Label) it.next();

			if (l1.doesIntersect(l2))
			{
				Rectangle2D.Double r = (Double) l1.getRectangle().createIntersection(l2.getRectangle());
				double dx = r.getWidth() / 2;
				double dy = r.getHeight() / 2;

				//				if(l1.getTopleft().getY() < l2.getTopleft().getY()) //l1 is on top of l2
				//					dy = -dy;

				//				if(l1.getTopleft().getX() < l2.getTopleft().getX()) //l1 is on top of l2
				//					dx = -dx;

				if (l1.getNode().getY() < l2.getNode().getY()) //l1 is on top of l2
					dy = -dy;

				if (l1.getNode().getX() < l2.getNode().getX()) //l1 is on top of l2
					dx = -dx;

				overlap_vectors[i][X] += dx;
				overlap_vectors[i][Y] += dy;
			}
		}
	}

	private boolean canSlideHorizontal(Label l)
	{
		return (
			(l.getOffsetVertical() == 0.0 || l.getOffsetVertical() == l.getHeight())
				&& Math.abs(overlap_vectors[l.getIndex()][X]) > 0
				&& ((overlap_vectors[l.getIndex()][X] > 0.0 && l.getOffsetHorizontal() > 0)
					|| (overlap_vectors[l.getIndex()][X] < 0.0 && l.getOffsetHorizontal() < l.getWidth())));

	}

	private boolean canSlideVertical(Label l)
	{
		return (
			(l.getOffsetHorizontal() == 0.0 || l.getOffsetHorizontal() == l.getWidth())
				&& Math.abs(overlap_vectors[l.getIndex()][Y]) > 0.0
				&& ((overlap_vectors[l.getIndex()][Y] > 0.0 && l.getOffsetVertical() > 0.0)
					|| (overlap_vectors[l.getIndex()][Y] < 0.0 && l.getOffsetVertical() < l.getHeight())));

	}

	/**
	 * moves the label value units in the direction determined by 
	 * the first parameter
	 * @param l the label to be moved	 
	 * @param direction horizontal or vertical moves?
	 * @param value determines amount of the change
	 */
	public void slideBy(Label l, int direction, double value)
	{
		double h_offset = l.getOffsetHorizontal();
		double v_offset = l.getOffsetVertical();
		int i_label = l.getIndex();

		value = Math.abs(value);

		if (direction == SLIDE_HORIZONTAL)
		{
			if (overlap_vectors[i_label][X] > 0)
				h_offset = Math.max(0, l.getOffsetHorizontal() - value);
			else
				h_offset = Math.min(l.getWidth(), l.getOffsetHorizontal() + value);
		}
		else
		{
			if (overlap_vectors[i_label][Y] > 0)
				v_offset = Math.max(0, l.getOffsetVertical() - value);
			else
				v_offset = Math.min(l.getHeight(), l.getOffsetVertical() + value);
		}

		l.moveTo(h_offset, v_offset);
	}
}
