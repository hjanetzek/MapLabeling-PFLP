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

import java.util.Iterator;

import javax.swing.JOptionPane;

import pflp.*;

/**
 * The thread implementing the algorithm.
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */

public abstract class SearchThread implements Runnable
{
	protected String name = null;

	private boolean halt = false;
	private boolean running = false;

	protected long iterations = 0;

	public SearchThread()
	{
		super();
	}

	/**
	 * @return name of the algorithm
	 */
	public final String getAlgorithmName()
	{
		if (name == null)
			return new String("<not set>");
		else
			return name;
	}

	public String toString()
	{
		return new String("pflp search algorithm: " + getAlgorithmName());
	}

	/**
	 * Deactivates the search thread.
	 */
	public void softHalt()
	{
		halt = true;
	}

	/**
	 * Returns true, if the current search thread is active.
	 */
	public boolean isActive()
	{
		return (!halt);
	}

	/**
	 * Returns true, if the current search thread is working. Note that
	 * isRunning() implies isActive.
	 */
	public boolean isRunning()
	{
		return running;
	}

	public void batchRun()
	{
		if (PFLPApp.instance == null)
			return;

		Solution s = PFLPApp.solution;
		if (s != null)
			s.acquireAccess();

		if(!checkRequirements())
		{
			System.out.println("algorithm not applicable!");
			if (s != null)
				s.releaseAccess();
			return;
		}

		//PFLPApp.random_generator.setSeed(181278);
		precompute();

		while (!iterate());

		if (s != null)
			s.releaseAccess();
	}

	public void run()
	{
		halt = false;
		
		//execute precompute
		Solution s = PFLPApp.solution;
		if (s != null)
			s.acquireAccess();

		if(!checkRequirements())
		{
			if (PFLPApp.gui != null)
				JOptionPane.showMessageDialog(null, "Algorithm not applicable!");
			else
				System.out.println("algorithm not applicable!");

			if (s != null)
				s.releaseAccess();
			return;
		}


		iterations = 0;
		precompute();

		if (s != null)
			s.releaseAccess();
			
		//start doing the "real" work...
		while (true)
		{
			if (!halt)
			{
				iterations++;

				//gain access to current solution
				s = PFLPApp.solution;

				if (s != null)
					s.acquireAccess();

				running = true;
				halt = iterate();
				
				if (s != null)
					s.releaseAccess();

				running = false;
				
				if(PFLPApp.getOptionDebugIterations() && PFLPApp.gui != null)
					PFLPApp.gui.redraw(true);
			}
			else
			{
				if (PFLPApp.gui != null)
					PFLPApp.gui.redraw(true);

				return;
			}
		}
	}

	/**
	 * whenever the visualization class creates a copy of the current solution it will
	 * call this function to retrieve some debug information about the current algorithm
	 * that can be displayed instead of label names.
	 */
	public String getLabelInfo(int i)
	{
		return null;
	}
	
	/**
	 * whenever the visualization class creates a copy of the current solution it will
	 * call this function to retrieve a status string that will be shown in the status
	 * bar
	 */
	public String getStatusString()
	{
		return null;
	}

	/**
	 *  checks, if the algorithm is applicable to the current instance
	 */
	public boolean checkRequirements()
	{
		return true;
	}


	/**
	 * does the real work
	 * @return true <-> algorithm is ready
	 */
	protected abstract boolean iterate();

	/**
	 * computations that should happen only once
	 */
	protected abstract void precompute();
	
	/**
	 * subsequently removes the label with the maximum
	 * number of intersections, until no more intersections
	 * are detected
	 */
	public void cleanupSolution(Solution solution)
	{
		Label[] labels = solution.getLabels();
		int n = labels.length;
		int numoverlaps[] = new int[n];

		int next_idx = -1;
		
		for(int i=0; i < n; i++)
		{
			Label l = labels[i];
			numoverlaps[i] = 0;
			
			if(!l.getUnplacable())
			{
				Iterator it = l.getNeighbours().iterator();
				while (it.hasNext())
				{
					Label l2 = (Label) it.next();
					if (!l2.getUnplacable() && labels[i].doesIntersect(l2))
						numoverlaps[i]++;
				}
			}
			
			if(numoverlaps[i] > 0 && (next_idx == -1 || numoverlaps[i] > numoverlaps[next_idx]))
				next_idx = i;
		}

		while(next_idx > -1)
		{
			//remove label next_idx
			Iterator it = labels[next_idx].getNeighbours().iterator();
			while (it.hasNext())
			{
				Label l2 = (Label) it.next();
				if (!l2.getUnplacable() && labels[next_idx].doesIntersect(l2))
					numoverlaps[l2.getIndex()]--;
			}
		
			labels[next_idx].setUnplacable(true);

			//find next victim 
			next_idx = -1;
			for(int i=0; i < n; i++)
			{
				if(!labels[i].getUnplacable() && numoverlaps[i] > 0 && (next_idx == -1 || numoverlaps[i] > numoverlaps[next_idx]))
					next_idx = i;
			}		
		}
	}
	
	public void cleanupSolutionSimple(Solution solution)
	{
		//simple algorithm...
		Label[] labels = solution.getLabels();
		int size = labels.length;
		Label found = null;
		int max_ovl = -1;
		
		do
		{
			found = null;
			max_ovl = -1;
			for (int i = 0; i < size; i++)
			{
				if (labels[i].getUnplacable())
					continue;
				
				int ovl_count = 0;
				Iterator it = labels[i].getNeighbours().iterator();
				while (it.hasNext())
				{
					Label l2 = (Label) it.next();
					if (!l2.getUnplacable() && labels[i].doesIntersect(l2))
						ovl_count++;
				}
				
				if (ovl_count > 0 && (found == null || ovl_count > max_ovl))
				{
					max_ovl = ovl_count;
					found = labels[i];
				}
			}
			
			if (found != null)
				found.setUnplacable(true);
			
		}
		while (found != null);
	}
}
