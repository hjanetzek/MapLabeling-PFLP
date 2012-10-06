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

import java.util.LinkedList;
import java.util.ListIterator;

import pflp.*;
 

/**
 * dummy base clase for horizontal and vertical line segments
 */
abstract class LineSegment extends Object
{
	public SlidePoint slide_point = null;

	public double getLabelWidth()
	{
		return getPoint().getWidth();
	}
	
	public double getLabelHeight()
	{
		return getPoint().getHeight();
	}
	
	public PointFeature getPoint()
	{
		return slide_point.getPoint();
	}
	
	public Label getLabel()
	{
		return slide_point.getLabel();
	}
}

/**
 * Implements a horizontal segment of possible positions for
 * a label candidate. the reference point is the lower left
 * corner
 */
class HorizontalSegment extends LineSegment implements Comparable
{
	private double XLeft = 0., XRight=0., Y=0.;
	
	/**
	 * creates the initial horizontal segment
	 * @param s the referenced Slidepoint
	 * @param upperSegment true for the upper, false for the lower segment
	 */	
	public HorizontalSegment(SlidePoint s, boolean upperSegment)
	{
		slide_point = s;
		XLeft = getPoint().getX() - getLabelWidth();
		XRight = getPoint().getX();
		
		if(upperSegment)
			Y = getPoint().getY();
		else
			Y = getPoint().getY() + getLabelHeight();
	}
	
	public double getWidth()
	{
		return XRight - XLeft;
	}
	
	public double getXLeft() 
	{
		return XLeft;
	}
	
	public void setXLeft(double left) 
	{
		XLeft = left;
	}
	
	public double getXRight() 
	{
		return XRight;
	}
	
	public void setXRight(double right) 
	{
		XRight = right;
	}
	
	public double getY() 
	{
		return Y;
	}
	
	public void setY(double y) 
	{
		Y = y;
	}
	
	public int compareTo(Object o) 
	{
		try
		{
			if(o.getClass().getName().equals("pflp.search.HorizontalSegment"))
			{
				HorizontalSegment s =(HorizontalSegment)o;
				
				//this < s if it's right edge is left of s
				if(getXLeft() + getLabelWidth() < s.getXLeft() + s.getLabelWidth())
					return -1;
				else if(getXLeft() + getLabelWidth() > s.getXLeft() + s.getLabelWidth())
					return +1;
				else if(getY() < s.getY())
					return -1;
				else if(getY() > s.getY())
					return +1;
				else return 0;
			}
			else if(o.getClass().getName().equals("pflp.search.VerticalSegment"))
			{
				VerticalSegment s = (VerticalSegment)o;
				
				if(getXLeft() + getLabelWidth() < s.getX() + s.getLabelWidth())
					return -1;
				else if(getXLeft() + getLabelWidth() > s.getX() + s.getLabelWidth())
					return +1;
				else if(getY() < s.getYUp())
					return -1;
				else if(getY() > s.getYUp())
					return +1;
				else 
					return 0;
			}
			else
			{
				throw new Exception("unsupported class in HorizontalSegment::compareTo");
			}
			
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
};

/**
 * Implements a vertical segment of possible positions for
 * a label candidate. the reference point is the lower left
 * corner
 */
class VerticalSegment extends LineSegment implements Comparable
{
	private double YUp = 0., YDown=0., X=0.;
	
	/**
	 * creates the initial horizontal segment
	 * @param s the referenced Slidepoint
	 * @param upperSegment true for the upper, false for the lower segment
	 */	
	public VerticalSegment(SlidePoint s, boolean leftSegment)
	{
		slide_point = s;
		YUp = getPoint().getY();
		YDown = getPoint().getY() + getLabelHeight();
		
		if(leftSegment)
			X = getPoint().getX() - getLabelWidth();
		else
			X = getPoint().getX();
	}
	
	public double getHeight()
	{
		return YDown - YUp;
	}
			 
	public double getX() 
	{
		return X;
	}

	public void setX(double x) 
	{
		X = x;
	}

	public double getYDown() 
	{
		return YDown;
	}

	public void setYDown(double down) 
	{
		YDown = down;
	}

	public double getYUp() 
	{
		return YUp;
	}

	public void setYUp(double up) 
	{
		YUp = up;
	}

	public int compareTo(Object o)  
	{
		try
		{
			if(o.getClass().getName().equals("pflp.search.VerticalSegment"))
			{
				VerticalSegment s =(VerticalSegment)o;
			
				//this < s if it's right edge is left of s
				if(getX() + getLabelWidth() < s.getX() + s.getLabelWidth())
					return -1;
				else if(getX() + getLabelWidth() > s.getX() + s.getLabelWidth())
					return +1;
				else if(getYUp() < s.getYUp())
					return -1;
				else if(getYUp() > s.getYUp())
					return +1;
				else return 0;
			}
			else if(o.getClass().getName().equals("pflp.search.HorizontalSegment"))
			{
				HorizontalSegment s = (HorizontalSegment)o;
				
				if(getX() + getLabelWidth() < s.getXLeft() + s.getLabelWidth())
					return -1;
				else if(getX() + getLabelWidth() > s.getXLeft() + s.getLabelWidth())
					return +1;
				else if(getYUp() < s.getY())
					return -1;
				else if(getYUp() > s.getY())
					return +1;
				else return 0;
			}
			else
			{
				throw new Exception("unsupported class in VerticalSegment::compareTo");
			}
			
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return 0;
	}
	
};

class SlidePoint
{
	private HorizontalSegment horizontalSegmentUp, horizontalSegmentDown;
	private VerticalSegment verticalSegmentLeft, verticalSegmentRight;
	private PointFeature p;
	private Label l;
	
	public SlidePoint(PointFeature _p, Label _l)
	{
		p = _p;
		l = _l;
			
		//create horizontal and vertical segments
		horizontalSegmentUp = new HorizontalSegment(this, true);
		horizontalSegmentDown = new HorizontalSegment(this, false);
		verticalSegmentLeft = new VerticalSegment(this, true);
		verticalSegmentRight = new VerticalSegment(this, false);
	}
	
	public PointFeature getPoint()
	{
		return p;
	}
	
	public Label getLabel()
	{
		return l;
	}

	public HorizontalSegment getHorizontalSegmentDown() 
	{
		return horizontalSegmentDown;
	}

	public HorizontalSegment getHorizontalSegmentUp() 
	{
		return horizontalSegmentUp;
	}

	public VerticalSegment getVerticalSegmentLeft() 
	{
		return verticalSegmentLeft;
	}

	public VerticalSegment getVerticalSegmentRight() 
	{
		return verticalSegmentRight;
	}
}

/**
 * 1/2 approximation as proposed by kreveld/strijk/wolff
 * see "Point Labeling with Sliding Labels" (1999)
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class LeftmostHeuristic extends SearchThread
{
	private static long MAX_ITERATIONS = 50;
	
	private SlidePoint[] slide_points = null;
	private LinkedList horizontalSegments;
	private LinkedList verticalSegments;
	
	private Solution solution = null;

	private LineSegment nextSegment = null; //defined in precompute
	
	public LeftmostHeuristic()
	{
		super();
		name = new String("1/2 approximation");
		nextSegment = null;
		
		horizontalSegments = new LinkedList();
		verticalSegments = new LinkedList();
	}

	public boolean checkRequirements()
	{
//		PointFeature points[] = PFLPApp.instance.getNodes();
//		double height = points[0].getHeight();
//		for (int i = 1; i < points.length; i++)
//		{
//			if(points[i].getHeight() != height)
//			{
//				System.out.println(getAlgorithmName() + ": at least one pair of labels have different height(" + height + ", " + points[i].getHeight() + ")!");
//				return false;
//			}
//		}
		
		return true;
	}

	protected void precompute()
	{
		SlidePoint s = null;
		//initialize data structures...
		int n = PFLPApp.instance.size();
		solution = new Solution(PFLPApp.instance, false);
		
		slide_points = new SlidePoint[n];
		
		nextSegment = null;
		
		for(int i=0; i < n; i++)
		{
			//set label to "unlabeled"
			solution.getLabels()[i].setUnplacable(true);
			
			//create the 4 possible line segments and insert them in
			//the proper lists
			s = new SlidePoint(PFLPApp.instance.getNodes()[i], solution.getLabels()[i]);
			
			horizontalSegments.add(s.getHorizontalSegmentUp());
			horizontalSegments.add(s.getHorizontalSegmentDown());
			
			verticalSegments.add(s.getVerticalSegmentLeft());
			verticalSegments.add(s.getVerticalSegmentRight());
			
			if(nextSegment == null || s.getHorizontalSegmentUp().compareTo(nextSegment) < 0)
				nextSegment = s.getHorizontalSegmentUp();

			if(s.getHorizontalSegmentDown().compareTo(nextSegment) < 0)
				nextSegment = s.getHorizontalSegmentDown();

			if(s.getVerticalSegmentLeft().compareTo(nextSegment) < 0)
				nextSegment = s.getVerticalSegmentLeft();

			if(s.getVerticalSegmentRight().compareTo(nextSegment) < 0)
				nextSegment = s.getVerticalSegmentRight();
			
		}
		
		PFLPApp.solution = solution;
	}

	protected boolean iterate()
	{
		long counter = 0;
		while(nextSegment != null)
		{
			boolean isHSegment = false;

			try
			{
				isHSegment = nextSegment.getClass() == Class.forName("pflp.search.HorizontalSegment");
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
				return true;
			}
			
			Label next_label = nextSegment.getLabel();
			PointFeature next_point = nextSegment.getPoint();
			
			double h_offset = 0., v_offset = 0.;
			//place the label associated with nextSegment at the proper position
			if(isHSegment)
			{
				HorizontalSegment nextHSegment = (HorizontalSegment) nextSegment;
				
				h_offset = next_point.getX() - nextHSegment.getXLeft();
				
				if(nextHSegment.getY() == next_point.getY()) //upper segment
					v_offset = next_point.getHeight();
				else if(nextHSegment.getY() == next_point.getY() + next_point.getHeight()) //lower segment
					v_offset = 0.;
				else
					System.err.println("numerical problems in LeftMostHeuristic::iterate() [1]: should be never reached!");
			}
			else
			{
				VerticalSegment nextVSegment = (VerticalSegment) nextSegment;

				v_offset = next_point.getHeight() - (nextVSegment.getYUp() - next_point.getY());
				
				if(nextVSegment.getX() == next_point.getX()) //right segment
					h_offset = 0.;
				else if(nextVSegment.getX() == next_point.getX() - next_point.getWidth()) //left segment
					h_offset = next_point.getWidth();
				else
					System.err.println("numerical problems in LeftMostHeuristic::iterate() [2]: should be never reached!");
			}
			
			if(!next_label.getUnplacable())
				System.err.println("LeftMostHeuristic::iterate(): next_label already placed, what's wrong?");
			
			//place the label at the precomputed position
			next_label.moveTo(h_offset, v_offset);
			next_label.setUnplacable(false);
			
			//update horizontal + vertical segments and find next
			//minimal segment
			nextSegment = null;
			
			//compute new occupied frontier segment
			double ofX = next_label.getTopleft().x + next_label.getWidth();
			double ofYTop = next_label.getTopleft().y; 
			double ofYBottom = ofYTop + next_label.getHeight();
			
			//horizontal segments
			ListIterator li = null;
			li = horizontalSegments.listIterator();
			while(li.hasNext())
			{
				HorizontalSegment hs = (HorizontalSegment)li.next();
				double ofYLocalBottom = ofYBottom + hs.getLabelHeight();
				
				if(hs.getY()>= ofYTop && hs.getY() <= ofYLocalBottom)
				{
					if(hs.getXRight() <= ofX) //left of new frontier, no new labelings possible
					{
						li.remove();
						continue;
					}
					else if(hs.getXLeft() < ofX) //hs intersects the new frontier -> shrink it
					{
						hs.setXLeft(ofX);
						
						if(hs.getWidth() <= 0)
						{
							System.err.println("LeftMostHeuristic::iterate(): invalid length of horizontal segment: " + hs.getWidth());
						}
					}
				}
				
				//update nextSegment
				if(nextSegment == null || hs.compareTo(nextSegment) < 0)
					nextSegment = hs;
			}
			
			//vertical segments
			li = verticalSegments.listIterator();
			while(li.hasNext())
			{
				VerticalSegment vs = (VerticalSegment)li.next();
				double ofYLocalBottom = ofYBottom + vs.getLabelHeight();
				
				if(vs.getYDown() > ofYTop && vs.getYUp() < ofYLocalBottom && vs.getX() <= ofX)
				{
					//if the whole vertical segment is covered, remove it...
					if(vs.getYUp() >= ofYTop && vs.getYDown() <= ofYLocalBottom)
					{
						li.remove();
						continue;
					}
					else //shrink the current segment			
					{
						if(vs.getYDown() > ofYLocalBottom) //cut upper line segment
						{
							vs.setYUp(ofYLocalBottom);
						}
						else //cut lower line segment
						{
							vs.setYDown(ofYTop);
						}
						
						if(vs.getHeight() <= 0)
						{
							System.err.println("LeftMostHeuristic::iterate(): invalid height of vertical segment: " + vs.getHeight());
						}
					}
				}
				
				//update nextSegment
				if(nextSegment == null || vs.compareTo(nextSegment) < 0)
					nextSegment = vs;
			}

			counter ++;
			if(counter > MAX_ITERATIONS || PFLPApp.getOptionDebugIterations()) //give the visualization a chance to redraw
				return false;
		}

		return true;
	}

}
