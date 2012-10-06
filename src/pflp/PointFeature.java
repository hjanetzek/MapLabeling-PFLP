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

import java.awt.*;
import java.awt.font.*;

/**
 * This class represents a city with it's location and the 
 * required space for it's corresponding label.
 * The corresponding coordinates are normalized to be 
 * greater than 0 in each component
 *
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class PointFeature extends Object
{
	static public final String DEFAULT_FONT = "Arial";
	static public final int DEFAULT_FONT_SIZE = 16;

	/**
	 * the x coordinate of the city
	 */
	private double x = 0;
	/**
	 * the x coordinate of the city
	 */
	private double y = 0;
	/**
	 * the width of the corresponding label
	 */
	private double width = 0;
	/**
	 * the height of the corresponding label
	 */
	private double height = 0;
	/**
	 * the priority of this city (not yet used)
	 */
	private double priority = 0;
	/**
	 * the name of this city
	 */
	private String text = "";
	/**
	 * the font used to display the corresponding label
	 */
	private String font = DEFAULT_FONT;
	/**
	 * the font size used to display the corresponding label
	 */
	private int fontsize = DEFAULT_FONT_SIZE;

	/**
	 * constructs a new node with the given size and width
	 */
	public PointFeature(
		double _x,
		double _y,
		double lbl_size_x,
		double lbl_size_y,
		double p,
		String _text,
		String _font,
		int _fontsize)
	{
		x = _x;
		y = _y;
		width = lbl_size_x;
		height = lbl_size_y;
		priority = p;
		text = _text;
		font = _font;
		fontsize = _fontsize;
	}

	/**
	 * constructs a new node. width and height will be determined by the needs of the text (default value)
	 */
	public PointFeature(double _x, double _y, double p, String _text)
	{
		x = _x;
		y = _y;
		priority = p;
		text = _text;
		font = DEFAULT_FONT;
		fontsize = DEFAULT_FONT_SIZE;

		calcLabelSize();
	}

	/**
	 * constructs a new node. width and height will be determined by the needs of the given text
	 */
	public PointFeature(double _x, double _y, double p, String _text, String _font, int _fontsize)
	{
		x = _x;
		y = _y;
		priority = p;
		text = _text;
		font = _font;
		fontsize = _fontsize;

		calcLabelSize();
	}

	private void calcLabelSize()
	{
		FontRenderContext frc = new FontRenderContext(null, false, false);
		Font fnt = new Font(font, Font.PLAIN, fontsize);
		Rectangle r = (fnt.createGlyphVector(frc, text)).getOutline(0, 0).getBounds();
		width = r.getWidth();
		height = r.getHeight();
	}

	/**
	 * returns true if and only if the label can intersect in any location with the
	 * label of the given node "node"
	 * @param node the node to compare
	 */
	public boolean canIntersect(PointFeature node)
	{
		return (
			(getWidth() + node.getWidth() >= Math.abs(getX() - node.getX()))
				&& (getHeight() + node.getHeight() >= Math.abs(getY() - node.getY())));
	}

	/**
	 * @return name of the font for the associated label
	 */
	public String getFont()
	{
		return font;
	}

	/**
	 * @return size of the font for the associated label
	 */
	public int getFontsize()
	{
		return fontsize;
	}

	/**
	 * @return Height of the label
	 */
	public double getHeight()
	{
		return height;
	}

	/**
	 * @return Width of the label
	 */
	public double getWidth()
	{
		return width;
	}

	/**
	 * @return Priority of the label
	 */
	public double getPriority()
	{
		return priority;
	}

	/**
	 * @return Text for the label
	 */
	public String getText()
	{
		return text;
	}

	/**
	 * @return x-coodinate of the label
	 */
	public double getX()
	{
		return x;
	}

	/**
	 * @return y-coodinate of the label
	 */
	public double getY()
	{
		return y;
	}

	/**
	 * @param d new x-coordinate
	 */
	public void setX(double d)
	{
		x = d;
	}

	/**
	 * @param d new y-coordinate
	 */
	public void setY(double d)
	{
		y = d;
	}
}
