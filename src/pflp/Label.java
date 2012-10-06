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

import java.awt.geom.*;
import java.util.*;

/**
 * Represents a mapping of a label (specified by the corresponding node)
 * to (x,y) corrdinates.
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class Label extends Object
{
	public static final int TOPLEFT = 1;
	public static final int TOPRIGHT = 2;
	public static final int BOTTOMLEFT = 3;
	public static final int BOTTOMRIGHT = 4;	
	
	//a reference to the node itself
	private PointFeature node = null;

	//a reference to all neighbours
	private Vector neighbours = new Vector();

	//the position of the label
	private double lbl_h_offset = 0.0; // 0 <= lbl_h_offset <= node.lbl_width
	private double lbl_v_offset = 0.0; // 0 <= lbl_v_offset <= node.lbl_height

	//true <-> label is not included in the current solution
	private boolean unplacable = false;
	
	//index of the label in the solution object...
	int index = -1;
		
	/**
	 * constructs a new label centerd at the specified city.
	 * @param n the corresponding {@link LblNode node}
	 */
	public Label(PointFeature n, int idx)
	{
		index = idx;
		node = n;
		moveTo(getWidth() / 2, getHeight() / 2);
	}

	/**
	* cretes an exact copy of the given label
	* don't forget to add all neighbours to the new object
	* @param label
	*/
	public Label(Label label)
	{
		node = label.getNode();
		lbl_h_offset = label.getOffsetHorizontal();
		lbl_v_offset = label.getOffsetVertical();
		unplacable = label.getUnplacable();
		index = label.getIndex();
	}

	/**
	 * Allows to mark the current label as "unset"
	 * @param b true, if the label shouldn't be included in the current solution
	 */
	public void setUnplacable(boolean b)
	{
		unplacable = b;
	}

	/**
	 * Returns true, if the label is not included in the current solution (not labeled)
	 */
	public boolean getUnplacable()
	{
		return unplacable;
	}

	/**
	 * Returns true, if the label intersects with one of it's neighbours.
	 */
	public boolean isOverlapping()
	{
		if (getUnplacable())
			return false;

		Iterator it = neighbours.iterator();
		while (it.hasNext())
		{
			Label next = (Label)it.next();

			if (next.getUnplacable())
				continue;

			if (doesIntersect(next))
			{
				return true;
			}
		}
		
		return false;
	}

	/**
	 * Returns true, if the current label intersects with the given
	 * label l2. This means, there is at least on point covered by
	 * both labels.
	 * @param l2 a reference to the label to compare
	 */
	public boolean doesIntersect(Label l2)
	{
		Point2D.Double tl1 = getTopleft();
		Point2D.Double tl2 = l2.getTopleft();

		return (
			(tl2.x + l2.getWidth() > tl1.x && tl2.x < tl1.x + getWidth())
				&& (tl2.y + l2.getHeight() > tl1.y && tl2.y < tl1.y + getHeight()));
	}

	/**
	 * Returns true, if the current label intersects the given label in horizontal direction.
	 * @param l2 a reference to the label to compare
	 */
	public boolean doesIntersectHorizontal(Label l2)
	{
		Point2D.Double tl1 = getTopleft();
		Point2D.Double tl2 = l2.getTopleft();

		return ((tl2.x + l2.getWidth() > tl1.x && tl2.x < tl1.x + getWidth()));
	}

	/**
	 * Returns true, if the current label intersects the given label in vertical direction.
	 * @param l2 a reference to the label to compare
	 */
	public boolean doesIntersectVertical(Label l2)
	{
		Point2D.Double tl1 = getTopleft();
		Point2D.Double tl2 = l2.getTopleft();

		return ((tl2.y + l2.getHeight() > tl1.y && tl2.y < tl1.y + getHeight()));
	}

	/**
	 * Searches a starting position for the given label.
	 */
	public void findInitialPlacement()
	{
		//use randomly one of the four corners....
		moveTo(getWidth() * PFLPApp.random_generator.nextInt(2), getHeight() * PFLPApp.random_generator.nextInt(2));
	}

	public void moveTo(int pos)
	{
		if(pos != TOPLEFT && pos != TOPRIGHT && pos != BOTTOMLEFT && pos != BOTTOMRIGHT)
			return;
		
		double f1 = (pos == TOPLEFT || pos == BOTTOMLEFT) ? 1 : 0;
		double f2 = (pos == TOPLEFT || pos == TOPRIGHT) ? 1: 0;
		
		moveTo(f1 * getWidth(), f2*getHeight());
	}

	/**
	 * Moves the label to the position. Note, that the given values are not
	 * checked and should be calculated carefully.
	 * A offset of (0, 0) means the lower right corner.
	 * @param h_offset the horizontal offset
	 * @param v_offset the vertical offset
	 */
	public void moveTo(double h_offset, double v_offset)
	{
		lbl_h_offset = h_offset;
		lbl_v_offset = v_offset;
	}

	/**
	 * Adds the given label to the Vector of neighbours.
	 * @param l a reference to a label, that can intersect with the current label in at least one possible position
	 */
	public void addNeighbour(Label l)
	{
		neighbours.add(l);
	}

	/**
	 * Returns true, if there is at least one label that could intersect in
	 * any position with the current label. If has_neighbours() returns false,
	 * any given position is a valid labeling.
	 */
	public boolean hasNeighbours()
	{
		return (!neighbours.isEmpty());
	}

	/**
	 * Returns a Vector of the labels neighbours.
	 * @return a reference to a Vector of neighbours
	 */
	public Vector getNeighbours()
	{
		return neighbours;
	}

	/**
	 * Returns a reference to the corresponding node.
	 */
	public PointFeature getNode()
	{
		return node;
	}

	/**
	 * Returns the center point of the current label.
	 */
	public Point2D.Double getCenter()
	{
		return new Point2D.Double(
			node.getX() - lbl_h_offset + (getWidth() / 2),
			node.getY() - lbl_v_offset + (getHeight() / 2));
	}

	/**
	 * Returns the top left point of the current label.	
	 */
	public Point2D.Double getTopleft()
	{
		return new Point2D.Double(node.getX() - lbl_h_offset, node.getY() - lbl_v_offset);
	}

	/**
	 * Returns the width of the current label.
	 */
	public double getWidth()
	{
		return (node.getWidth());
	}

	/**
	 * Returns the height of the current label.
	 */
	public double getHeight()
	{
		return (node.getHeight());
	}

	/**
	 * Returns the horizontal offset. Zero means the label is 
	 * positioned with it's left border at the city.
	 */
	public double getOffsetHorizontal()
	{
		return (lbl_h_offset);
	}

	/**
	 * Returns the vertical offset. Zero means the label is 
	 * positioned with it's top border at the city.
	 */
	public double getOffsetVertical()
	{
		return (lbl_v_offset);
	}

	/**
	 * @return true <-> label is placed at the upper left corner  
	 */
	public boolean isTopLeft()
	{
		return getOffsetHorizontal() == getWidth() && getOffsetVertical() == getHeight();
	}

	/**
	 * @return true <-> label is placed at the upper right corner  
	 */
	public boolean isTopRight()
	{
		return getOffsetHorizontal() == 0 && getOffsetVertical() == getHeight();
	}

	/**
	 * @return true <-> label is placed at the lower left corner  
	 */
	public boolean isBottomLeft()
	{
		return getOffsetHorizontal() == getWidth() && getOffsetVertical() == 0;
	}

	/**
	 * @return true <-> label is placed at the lower right corner  
	 */
	public boolean isBottomRight()
	{
		return getOffsetHorizontal() == 0 && getOffsetVertical() == 0;
	}

	/**
	 * Returns a rectangle representing the current placement.
	 */
	public Rectangle2D.Double getRectangle()
	{
		Point2D.Double tl = getTopleft();
		return new Rectangle2D.Double(tl.getX(), tl.getY(), getWidth(), getHeight());
	}

	/**
	 * returns the smallest distance between two rectangles, -1 if they overlap...
	 */
	public double getDistance(Label l2)
	{
		Point2D.Double tl1 = getTopleft();
		Point2D.Double tl2 = l2.getTopleft();

		double d = 0.0;

		boolean h_intersect = doesIntersectHorizontal(l2);
		boolean v_intersect = doesIntersectVertical(l2);

		if (v_intersect && h_intersect)
		{
			d = -1.0;
		}
		else if (h_intersect)
		{
			d = Math.abs(tl1.y - tl2.y);
			if (tl1.y < tl2.y)
				d -= getHeight();
			else
				d -= l2.getHeight();
		}
		else if (v_intersect)
		{
			d = Math.abs(tl1.x - tl2.x);
			if (tl1.x < tl2.x)
				d -= getWidth();
			else
				d -= l2.getWidth();
		}
		else
		{
			double x1 = tl1.x;
			double y1 = tl1.y;
			double x2 = tl2.x;
			double y2 = tl2.y;

			if (tl1.x > tl2.x)
			{
				if (tl1.y > tl2.y)
				{
					y1 += getHeight();
					x2 += l2.getWidth();
				}
				else
				{
					x2 += l2.getWidth();
					y2 += l2.getHeight();
				}
			}
			else
			{
				if (tl1.y > tl2.y)
				{
					x1 += getWidth();
					y1 += getHeight();
				}
				else
				{
					y2 += l2.getHeight();
					x1 += getWidth();
				}
			}

			double dx = x1 - x2;
			double dy = y1 - y2;

			d = Math.sqrt(dx * dx + dy * dy);
		}

		return d;
	}
	
	/**
	 * @return index of the label in  the current instance
	 */
	public int getIndex()
	{
		return index;
	}

}
