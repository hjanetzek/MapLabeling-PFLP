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

import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.swing.JOptionPane;

import pflp.search.*;
import pflp.ui.*;

public class PFLPApp
{
	/**
	  * a global reference to our user interface
	  */
	public static GUI gui = null;

	/**
	 * a global reference to the current solution, if any
	 */
	public static Instance instance = null;

	/**
	 * a global reference to the current solution...
	 * this value remains null as long as no instance object
	 * has been created
	 */
	public static Solution solution = null;

	/**
	 * a array of all implemented search algorithms
	*/
	public static SearchThread[] algorithms = null;
	private static SearchThread running_algorithm = null;
	private static int selected_algorithm = 0;
	private static Thread search_thread = null;

	private static boolean point_selection = true;
	private static boolean debug_iterations = false;

	//random generator
	public static final Random random_generator = new Random();

	/**
	 * called by the {@link LblGUI user interface object} when the
	 * user selects the appropriate menu entry.
	 * @param file_name the name of the file in .lab .lbl or .xml format
	 */
	public static void onLoadInstance(String file_name)
	{
		if (gui != null)
			gui.setStatusText("loading instance...");

		Solution s = solution;
		if (s != null)
			s.acquireAccess();

		solution = null;
		instance = new Instance(file_name);

		if (instance.getNodes() == null || instance.size() <= 0)
			instance = null;

		if (s != null)
			s.releaseAccess();

		gui.redraw(true);
		gui.setStatusText("done");
	}

	/**
	 * called by the {@link LblGUI user interface object} when the
	 * user selects the appropriate menu entry.
	 */
	public static void onCreateRandomInstance()
	{
		gui.setStatusText("creating random instance...");

		Solution s = solution;
		if (s != null)
			s.acquireAccess();

		instance = new Instance();
		solution = null;

		if (s != null)
			s.releaseAccess();

		gui.redraw(true);
		gui.setStatusText("done");
	}

	/**
	 * called by the {@link GUI user interface object} when the
	 * user clicks the reset button
	 */
	public static void onResetSolution()
	{
		if (solution == null)
			return;

		Solution s = solution;
		s.acquireAccess();

		solution = null;

		s.releaseAccess();

		if (gui != null)
			gui.redraw(true);
	}

	/**
	 * loads an solution from the given file
	 * @param  file_name the name of the requested file
	 */
	public static void onLoadSolution(String file_name)
	{
		gui.setStatusText("loading solution...");

		Solution s = solution;

		if (s != null)
			s.acquireAccess();

		solution = null;
		instance = null;

		solution = new Solution(file_name);

		if (instance == null || solution == null || instance.getNodes() == null || instance.size() <= 0)
		{
			instance = null;
			solution = null;
			return;
		}

		if (s != null)
			s.releaseAccess();

		gui.redraw(true);
		gui.setStatusText("done");
	}

	public static void onAlgorithmChanged(int index)
	{
		if (isBusy())
		{
			System.err.println("can't change algorithm while another one is running...");
			return;
		}

		selected_algorithm = index;
	}

	/**
	 * starts the selected search algorithm thread
	 */
	public static void onStartAlgorithm()
	{
		if (instance == null)
		{
			if (gui != null)
				JOptionPane.showMessageDialog(null, "Load a instance first!");
			System.out.println("onStartAlgorithm called with no instance object!");
			return;
		}

		if (!isBusy())
		{
			System.out.println("starting search thread...");
			running_algorithm = algorithms[selected_algorithm];
			search_thread = new Thread(running_algorithm);
			search_thread.setDaemon(true);
			search_thread.start();
			System.out.println("done");
		}
		else
		{
			System.err.println("search thread is already running");
		}
	}

	/**
	 * stops the running algorithm
	 */
	public static void onStopAlgorithm()
	{
		if (search_thread != null && running_algorithm != null)
		{
			running_algorithm.softHalt();

			System.out.println("waiting for completion of the current iteration...");
			while (running_algorithm.isRunning())
			{
				try
				{
					Thread.sleep(100);
					
				} catch (InterruptedException e) {}
			}
			
			running_algorithm = null;
			System.out.println("done");
		}
	}

	/**
	 * returns a reference to the active search algorithm
	 */
	public static SearchThread getRunningAlgorithm()
	{
		return running_algorithm;
	}

	/**
	 * saves the current instance to the given filename
	 * @param  file_name the name of the requested file
	 */
	public static void onSaveSolution(String file_name)
	{
		Solution s = solution;
		if (s == null)
		{
			JOptionPane.showMessageDialog(null, "Nothing to save!");
			return;
		}

		s.acquireAccess();

		try
		{
			gui.setStatusText("dumping solution to " + file_name + "...");
			solution.dumpSolution(file_name);
		}
		catch (IOException e)
		{
			JOptionPane.showMessageDialog(null, "Error writing to " + file_name + ": " + e.getMessage());
		}

		if (s != null)
			s.releaseAccess();

		JOptionPane.showMessageDialog(null, "Solution successfully saved to \n" + file_name);
	}

	/**
	 * will be called to exit the program. additional cleanup
	 * code should reside here
	 */
	public static void onQuitApplication(int retcode)
	{
		System.exit(retcode);
	}

	/**
	 * enables or disables point selection mode...
	 *
	 */
	public static void setOptionPointSelection(boolean b)
	{
		point_selection = b;
	}

	/**
	 * if true, the visualization will be refreshed after every
	 * iteration of the search thread
	 */
	public static void setOptionDebugIterations(boolean b)
	{
		debug_iterations = b;
	}
	
	/**
	 * true <-> iteration debugging is on 
	 */
	public static boolean getOptionDebugIterations()
	{
		return debug_iterations;
	}

	/**
	 * true <-> point selection is enabled
	 */
	public static boolean getOptionPointSelection()
	{
		return point_selection;
	}

	/** 
	 * true if at least one search algorithm is running..
	 *
	 */
	public static boolean isBusy()
	{
		if (running_algorithm != null  && running_algorithm.isActive())
			return true;

		return false;
	}

	private static void registerAlgorithms()
	{
		algorithms = new SearchThread[5];
		algorithms[0] = new ForceDirectedLabeling();
		algorithms[1] = new SimulatedAnnealing();
		algorithms[2] = new HirschLabeling();
		algorithms[3] = new LeftmostHeuristic();
		algorithms[4] = new RandomPlacement();
	}

	private static void usage()
	{
		String usage = new String();

		usage += "PFLPApp \n";
		usage += "\t[ --batch <filename> \n\t [--retries <n>] \n\t [--disable-point-selection] \n\t [--solution <file_prfx>] \n\t [--algorithm {fdl|fdlcu|sa|hirsch|leftmost|random}]\n\t]\n";
		usage += "\t[--dump-min-dist <filename.sol> -o <outfile.dist>]\n";
		//usage += "\t[--beautify <filename.sol> -o <outfile.sol>]";
		System.out.println(usage);
		System.exit(1);
	}

	private static boolean dumpMinDistance(String in_file, String out_file)
	{
		System.out.println("Reading " + in_file + "...");

		solution = new Solution(in_file);
		if(instance == null || solution == null || instance.getNodes() == null || instance.getNodes().length <= 0 )
		{
		    System.out.println("can't import " + in_file);
		    return false;
		}

		try
		{
		    System.out.println("appending data to " + out_file + "...");
		    FileWriter f = new FileWriter(out_file, true); //append

		    Label[]labels = solution.getLabels();
		    
		    for(int i=0; i<labels.length; i++)
		    {
		    	String dmp = new String("");
		    	Label l = labels[i];

		    	double dist = 0.;
		    	
		    	if(!l.getUnplacable())
		    	{
		    		dist = Double.MAX_VALUE;
		    		for(int j=0; j<labels.length; j++)
		    		{
		    			if(j != i)
		    			{
		    				Label l2 = labels[j];
		    				double h = 0.;
		    			
		    				if(!l2.getUnplacable())
		    				{
		    					h = Math.max(0.,l.getDistance(l2));

		    					if(h < dist)
		    						dist = h;
		    				
		    					if(dist == 0.)
		    						break;
		    				}
		    			}
		    		}

		    		dmp = "" + dist;
					f.write(dmp + "\n");
		    	}
		    }

		    f.close();
		}
		catch (IOException e)
		{
		    System.out.println("error while writing to " + out_file + ": " + e.getMessage());
		    return false;
		}

		return true;
	}
	
	private static boolean executeBatch(
		String batch_file,
		int batch_retries,
		String batch_solutions,
		SearchThread batch_algorithm)
	{
		instance = new Instance(batch_file);
		if (instance.getNodes() == null || instance.getNodes().length <= 0)
			System.exit(1);

		System.out.println();
		System.out.println("processing " + batch_file + "...");
		System.out.println("-----------------------------------------------------------------------------");
		System.out.println("point selection: " + (getOptionPointSelection() ? "yes" : "no"));
		
		long best_labeled = 0;
		long best_time = 0;
		
		for (int i = 1; i <= batch_retries; i++)
		{
			solution = null;
			long start = (new GregorianCalendar()).getTimeInMillis();

			batch_algorithm.batchRun();

			long end = (new GregorianCalendar()).getTimeInMillis();

			Date t = new Date();

			long result = end - start;
			String mi = new String(), ss = new String(), mm = new String();

			mi = "" + (int) (result / 60000);
			result = result - ((long) (((int) (result / 60000)) * 60000));

			ss = "" + (int) (result / 1000);
			result = result - ((long) (((int) (result / 1000))) * 1000);

			mm = "" + (int) result;

			while (mi.length() < 2)
				mi = "0" + mi;
			while (ss.length() < 2)
				ss = "0" + ss;
			while (mm.length() < 3)
				mm = "0" + mm;

			long l = solution.countLabeledCities();

			System.out.println(
				"   run #"
					+ i
					+ ", wall time: "
					+ mi
					+ ":"
					+ ss
					+ ":"
					+ mm
					+ ", cities: "
					+ instance.getNodes().length
					+ ", labeled: "
					+ l
					+ ", unlabeled: "
					+ (solution.size() - l));

			if (best_labeled == 0 || l > best_labeled || (l == best_labeled && best_time > (end - start)))
			{
				best_time = end - start;
				best_labeled = l;
			}
		}
		System.out.println("-----------------------------------------------------------------------------");

		if (batch_solutions != null)
		{
			String filename_sol = batch_solutions + ".sol";
			String filename_dmp = batch_solutions + ".log";

			try
			{
				System.out.println("dumping solution to " + filename_sol + "...");

				solution.dumpSolution(filename_sol);
			}
			catch (IOException e)
			{
				System.out.println("error writing to " + filename_sol + ": " + e.getMessage());
			}

			try
			{
				System.out.println("writing " + filename_dmp + "...");

				FileWriter f = new FileWriter(filename_dmp);
				String dmp = new String("");
				Double time = new Double(((double) best_time) / 1000);

				dmp = batch_file + " " + "0 " + solution.size() + " ";
				dmp += best_labeled + " - ";
				dmp += (Math.round(best_time / 100)) / 10 + " -\n";

				f.write(dmp);
				f.close();
			}
			catch (IOException e)
			{
				System.out.println("error writing to " + filename_dmp + ": " + e.getMessage());
				return false;
			}

		}
		return true;
	}

	public static void main(String[] args)
	{
		//register search algorithms
		registerAlgorithms();

		boolean batch_run = false;
		SearchThread batch_algorithm = algorithms[0];
		String batch_solutions = null;
		String batch_file = null;
		int batch_retries = 1;

		if (args.length > 0)
		{
		    if(args.length == 4 && args[0].equals("--dump-min-dist") && args[2].equals("-o"))
		    {
		    	if(dumpMinDistance(args[1], args[3]))
		    	{
		    		System.out.println("done");
		    		System.exit(0);
		    	}
		    	System.exit(1);
		    }
			
			if (args[0].equals("--batch"))
			{
				batch_run = true;
				if (args.length < 2)
					usage();

				batch_file = args[1];
				for (int i = 2; i < args.length; i++)
				{
					if (args[i].equals("--retries"))
					{
						if (args.length <= i + 1)
							usage();

						i++;
						try
						{
							batch_retries = Integer.valueOf(args[i]).intValue();
						}
						catch (Exception e)
						{
							usage();
						}
					}
					else if (args[i].equals("--disable-point-selection"))
					{
						setOptionPointSelection(false);
					}
					else if (args[i].equals("--solutions"))
					{
						if (args.length <= i + 1)
							usage();

						i++;
						batch_solutions = args[i];
					}
					else if (args[i].equals("--algorithm"))
					{
						if (args.length <= i + 1)
							usage();

						i++;
						String alg = args[i];
						if (alg.equals("fdl"))
							batch_algorithm = algorithms[0];
						else if (alg.equals("sa"))
							batch_algorithm = algorithms[1];
						else if (alg.equals("hirsch"))
							batch_algorithm = algorithms[2];
						else if (alg.equals("leftmost"))
							batch_algorithm = algorithms[3];
						else if (alg.equals("random"))
							batch_algorithm = algorithms[4];
						else if (alg.equals("fdlcu"))
						{
							batch_algorithm = algorithms[0];
							((ForceDirectedLabeling)batch_algorithm).enableSimpleCleanup();
						}
						else
							usage();
					}
					else
						usage();
				}
			}

			if (batch_run)
			{
				int ec = 0;

				if (!executeBatch(batch_file, batch_retries, batch_solutions, batch_algorithm))
					ec = 1;

				System.exit(ec);
			}

			usage();
		}
		else
		{
			gui = new GUI();
		}
	}
}