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

import java.io.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class represents a instance for the map labeling problem.
 * It holds an collection of all points and the given map height and
 * width. 
 *
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class Instance
{
	private static final int RANDOM_MAP_WIDTH = 100;
	private static final int RANDOM_MAP_HEIGHT = 80;
	private static final int RANDOM_MAP_LABELS = 80;
	private static final int RANDOM_MAP_SCALE = 6;

	private static final double RANDOM_PCT_BIG_CITIES = 0.05;
	private static final double RANDOM_PCT_MEDIUM_CITIES = 0.15;

	private PointFeature[] nodes = null;

	private int map_width = 0;
	private int map_height = 0;

	private String name = "<not yet set>";

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

	private static int to_int(String s)
	{
		try
		{
			return Integer.valueOf(s).intValue();
		}
		catch (Exception e)
		{
			System.out.println("Failed to convert String \"" + s + "\" to int");
			return 0;
		}
	}
	
	/**
	 * creates an instance with the nodes given in Vector v
	 */
	public Instance(Vector v, String solution_name)
	{
		int i;
		nodes = new PointFeature[v.size()];
		for (i = 0; i < v.size(); i++)
			nodes[i] = (PointFeature)v.get(i);

		System.out.println(i + " labels processed...");
		name = solution_name;

		adjust_coordinates();
	}

	/**
	 * creates an instance with random distributed nodes. 
	 */
	public Instance()
	{
		int i = 0, j = 0;

		name = "random instance";

		map_width = RANDOM_MAP_WIDTH * RANDOM_MAP_SCALE;
		map_height = RANDOM_MAP_HEIGHT * RANDOM_MAP_SCALE;

		System.out.println("creating random instance with " + RANDOM_MAP_LABELS + " nodes...");

		//create random nodes
		boolean[][] taken = new boolean[RANDOM_MAP_WIDTH][RANDOM_MAP_HEIGHT];
		for (i = 0; i < RANDOM_MAP_WIDTH; i++)
			for (j = 0; j < RANDOM_MAP_HEIGHT; j++)
				taken[i][j] = false;

		nodes = new PointFeature[RANDOM_MAP_LABELS];
		for (i = 0; i < RANDOM_MAP_LABELS; i++)
		{
			int x, y;
			do
			{
				x = PFLPApp.random_generator.nextInt(RANDOM_MAP_WIDTH);
				y = PFLPApp.random_generator.nextInt(RANDOM_MAP_HEIGHT);

			}
			while (taken[x][y]);

			taken[x][y] = true;

			//generate random text
			int text_length = PFLPApp.random_generator.nextInt(8) + 4;
			byte s[] = new byte[text_length];
			for (; text_length-- > 0;)
				s[text_length] = (new Integer(65 + PFLPApp.random_generator.nextInt(91 - 65))).byteValue();
			String text = new String(s);

			//determine the type of the city (priority)
			int priority = 1;
			if (PFLPApp.random_generator.nextDouble() < RANDOM_PCT_BIG_CITIES)
				priority = 3;
			else if (PFLPApp.random_generator.nextDouble() < RANDOM_PCT_MEDIUM_CITIES)
				priority = 2;

			nodes[i] = new PointFeature(x * RANDOM_MAP_SCALE, y * RANDOM_MAP_SCALE, priority, text);
		}

		System.out.println("done");
		adjust_coordinates();
	}

	/**
	 * loads the instance from the specified file
	 */
	public Instance(String file)
	{
		BufferedReader r = null;
		String line = null;

		name = file;

		if (file.endsWith(".lab"))
		{
			System.out.println("using .lab file format...");
			try
			{
				r = new BufferedReader(new FileReader(file));
				Vector dummy = new Vector();
				int idx = -1;

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

					PointFeature new_lbl = new PointFeature(x, y, lbl_width, lbl_height, priority, text, font, fontsize);
					dummy.add(new_lbl);
				}

				int i;
				nodes = new PointFeature[dummy.size()];
				for (i = 0; i < dummy.size(); i++)
					nodes[i] = (PointFeature)dummy.get(i);

				System.out.println(i + " labels processed...");

				adjust_coordinates();

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
		else if (file.endsWith(".xml")) //XML - Format
		{
			System.out.println("using xml file format...");
			try
			{
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
				Document doc = docBuilder.parse(new File(file));

				// normalize text representation
				doc.getDocumentElement().normalize();

				String root_element = doc.getDocumentElement().getNodeName();
				if (root_element == "labelinstance")
				{
									
					double def_priority = 1;
					String def_font = new String(PointFeature.DEFAULT_FONT);
					int def_fontsize = PointFeature.DEFAULT_FONT_SIZE;

					//does the instance contains default-values?
					NodeList option_list = doc.getElementsByTagName("default");
					if (option_list.getLength() > 0) //yes
					{
						NodeList options = option_list.item(0).getChildNodes();
						for (int i = 0; i < options.getLength(); i++)
						{
							org.w3c.dom.Node current = options.item(i);
							String opt_name = current.getNodeName();

							if (opt_name.compareTo("priority") == 0)
								def_priority = to_double(current.getChildNodes().item(0).getNodeValue());
							else if (opt_name.compareTo("textsize") == 0)
								def_fontsize = to_int(current.getChildNodes().item(0).getNodeValue());
							else if (opt_name.compareTo("textfont") == 0)
								def_font = current.getChildNodes().item(0).getNodeValue().trim();
						}
					}

					//import labels
					NodeList lbl_list = doc.getElementsByTagName("label");
					int lbl_count = lbl_list.getLength();

					System.out.println("file contains " + lbl_count + " labels");
					nodes = new PointFeature[lbl_count];
					int count = 0;
					for (int s = 0; s < lbl_count; s++)
					{
						org.w3c.dom.Node label_node = lbl_list.item(s);
						if (label_node.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE)
						{
							NodeList nl = null;
							double priority = def_priority;
							String font = def_font;
							int fontsize = def_fontsize;
							Element current_lbl = (Element)label_node;

							Element coordinate = (Element) (current_lbl.getElementsByTagName("coordinate").item(0));
							String s_coordinate = ((org.w3c.dom.Node) (coordinate.getChildNodes().item(0))).getNodeValue().trim();

							Element text = (Element) (current_lbl.getElementsByTagName("text").item(0));
							String s_text = ((org.w3c.dom.Node) (text.getChildNodes().item(0))).getNodeValue().trim();

							if ((nl = current_lbl.getElementsByTagName("priority")).getLength() > 0)
								priority = to_double(nl.item(0).getChildNodes().item(0).getNodeValue().trim());
							if ((nl = current_lbl.getElementsByTagName("textsize")).getLength() > 0)
								fontsize = to_int(nl.item(0).getChildNodes().item(0).getNodeValue().trim());
							if ((nl = current_lbl.getElementsByTagName("textfont")).getLength() > 0)
								font = nl.item(0).getChildNodes().item(0).getNodeValue().trim();

							//x- and y-coordinate are separeted by space
							int idx = s_coordinate.indexOf(" ");
							if (idx != -1)
							{
								double d_x = to_double(s_coordinate.substring(0, idx));
								double d_y = to_double(s_coordinate.substring(idx + 1));

								nodes[count] = new PointFeature(d_x, d_y, priority, s_text, font, fontsize);
								count++;
							}
						}
					}
					System.out.println(count + " labels processed...");

					adjust_coordinates();
				}
			}
			catch (SAXParseException err)
			{
				System.out.println("** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
				System.out.println(" " + err.getMessage());

			}
			catch (SAXException e)
			{
				Exception x = e.getException();
				((x == null) ? e : x).printStackTrace();

			}
			catch (Throwable t)
			{
				t.printStackTrace();
			}
		}
		else if (file.endsWith(".lbl")) //.lbl - Format
		{
			System.out.println("using lbl file format...");
			try
			{
				r = new BufferedReader(new FileReader(file));

				map_width = to_int(r.readLine());
				map_height = to_int(r.readLine());

				int expected_size = to_int(r.readLine());
				String mode_line = r.readLine();

				boolean option_labelsize = (mode_line.indexOf("labelsize") != -1);
				boolean option_priority = (mode_line.indexOf("priority") != -1);
				boolean option_text = (mode_line.indexOf("text") != -1);
				boolean option_fontsize = (mode_line.indexOf("textsize") != -1);

				if (!option_text)
					option_labelsize = true;

				nodes = new PointFeature[expected_size];
				int count = 0;

				while (count < expected_size && (line = r.readLine()) != null)
				{
					double x = 0, y = 0;
					double lbl_width = 0, lbl_height = 0;
					double priority = 1;
					int fontsize = 0;
					String text = "";
					String font = new String(PointFeature.DEFAULT_FONT);

					//Labelobjekt erzeugen
					if (option_text)
					{
						int p = line.indexOf(">");
						if (p != -1)
							text = line.substring(p + 1);
						line = line.substring(0, p);
					}

					//Eingabestring zerlegen
					StringTokenizer tokenizer = new StringTokenizer(line);
					String[] array = new String[tokenizer.countTokens()];
					int tk_count = 0;

					while (tokenizer.hasMoreElements())
					{
						array[tk_count++] = (String)tokenizer.nextElement();
					}

					int j = 0;
					x = to_double(array[j++]);
					y = to_double(array[j++]);

					if (option_labelsize)
					{
						lbl_width = to_double(array[j++]);
						lbl_height = to_double(array[j++]);
					}

					if (option_priority)
						priority = to_double(array[j++]);
					else
						priority = 1;

					if (option_fontsize)
						fontsize = to_int(array[j++]);
					else
						fontsize = PointFeature.DEFAULT_FONT_SIZE;

					if (option_labelsize)
						nodes[count] = new PointFeature(x, y, lbl_width, lbl_height, priority, text, font, fontsize);
					else
						nodes[count] = new PointFeature(x, y, priority, text, font, fontsize);
					count++;
				}
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
		else //das seltsame neue Format ohne Endung
		{
			System.out.println("using benchmarkset file format (no extension)...");
			try
			{
				r = new BufferedReader(new FileReader(file));
				Vector dummy = new Vector();
				int idx = -1;

				double x = 0, y = 0;
				double lbl_width = 0, lbl_height = 0;
				double def_lbl_width = 100, def_lbl_height = 100;
				double priority = 1;
				int fontsize = 7;
				int cnt = 0;
				String text = "";
				String font = new String(PointFeature.DEFAULT_FONT);

				while ((line = r.readLine()) != null)
				{
					if (line.length() == 0)
						continue;

					if (line.charAt(0) == '%')
					{
						if (line.length() > 11 && line.substring(1, 10).compareTo("Labelsize") == 0)
						{
							String tmp = line.substring(12);
							tmp = tmp.trim();

							if ((idx = tmp.indexOf(" ")) != -1)
							{
								def_lbl_width = to_double(tmp.substring(0, idx));
								def_lbl_height = to_double(tmp.substring(idx + 1));

								System.out.println(
									"Setting default labelsize to (" + def_lbl_width + "x" + def_lbl_height + ")!");
							}
						}
					}
					else
					{

						String[] splitted = line.split(" ", -1);

						x = to_double(splitted[0]);
						y = to_double(splitted[1]);
						if (splitted.length > 2)
						{
							lbl_width = to_double(splitted[2]);
							lbl_height = to_double(splitted[3]);
						}
						else
						{
							lbl_width = def_lbl_width;
							lbl_height = def_lbl_height;
						}

						text = "L" + (cnt++);
						PointFeature new_lbl = new PointFeature(x, y, lbl_width, lbl_height, priority, text, font, fontsize);
						dummy.add(new_lbl);
					}
				}

				int i;
				nodes = new PointFeature[dummy.size()];
				for (i = 0; i < dummy.size(); i++)
					nodes[i] = (PointFeature)dummy.get(i);

				System.out.println(i + " labels processed...");

				adjust_coordinates();

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

	}

	private void adjust_coordinates()
	{
		double min_x = Double.MAX_VALUE;
		double min_y = Double.MAX_VALUE;

		System.out.println("adjusting coordinates...");
		for (int i = 0; i < nodes.length; i++)
		{
			PointFeature c = nodes[i];
			if (c.getX() - c.getWidth() < min_x)
				min_x = c.getX() - c.getWidth();

			if (c.getY() - c.getHeight() < min_y)
				min_y = c.getY() - c.getHeight();
		}

		double h_offset = -min_x;
		double v_offset = -min_y;

		double max_x = Double.MIN_VALUE;
		double max_y = Double.MIN_VALUE;

		//add offset to each label...
		for (int i = 0; i < nodes.length; i++)
		{
			PointFeature c = nodes[i];
			
			c.setX(c.getX() + h_offset);
			c.setY(c.getY() + v_offset);

			if (c.getX() + c.getWidth() > max_x)
				max_x = c.getX() + c.getWidth();

			if (c.getY() + c.getHeight() > max_y)
				max_y = c.getY() + c.getHeight();
		}

		map_width = (int)Math.ceil(max_x);
		map_height = (int)Math.ceil(max_y);

		System.out.println("done");
	}

	/**
	 * returns the width of the instance (map)
	 */
	public int getMapWidth()
	{
		return map_width;
	}

	/**
	 * returns the height of the instance (map)
	 */
	public int getMapHeight()
	{
		return map_height;
	}

	/**
	 * returns a reference to the array of nodes
	 */
	public PointFeature[] getNodes()
	{
		return (nodes);
	}

	/**
	 * returns the name of the file or the string "random instance"
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param string the new name of the instance
	 */
	public void setName(String string)
	{
		name = string;
	}

	/**
	 * @see hugs.Problem#size()
	 */
	public int size()
	{
			return nodes.length;
	}
}
