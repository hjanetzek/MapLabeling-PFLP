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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import pflp.Label;
import pflp.PFLPApp;
import pflp.Solution;

/**
 * simulated annealing like proposed by Christensen/Marks/Shieber
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */

public class SimulatedAnnealing extends SearchThread
{
	private static int MOVES_PER_ITERATION = 500;
	
	private static long REMOVE_PENALTY = 1;
	
	private double temperature = 0;
	private int size = 0;

	private int nRejected = 0;
	private int nTaken = 0;
	private int nStages = 0;

	private Label[] labels = null;
	private Solution solution = null;

	private HashSet obstructedLabels = null;
	private long objective = 0;
	
	public SimulatedAnnealing()
	{
		super();
		name = new String("simulated annealing (4pos)");
	}

	protected void precompute()
	{
		//create new HashSet containing all obstructed labels
		obstructedLabels = new HashSet();
		
		//create initial solution
		if (PFLPApp.solution == null)
		{
			PFLPApp.solution = new Solution(PFLPApp.instance);
			size = PFLPApp.solution.size();
			labels = PFLPApp.solution.getLabels();
		}
		else
		{
			size = PFLPApp.solution.size();
			labels = PFLPApp.solution.getLabels();

			//check each label if it is a valid 4pos placement
			for (int i = 0; i < size; i++)
			{
				Label l = labels[i];
				if (!l.isBottomLeft() && !l.isBottomRight() && !l.isTopLeft() && !l.isTopRight())
					l.moveTo(Label.TOPLEFT);
			}
		}

		solution = PFLPApp.solution;

		//p should be 2/3 when dE = 1
		temperature = -1.0 / Math.log(1. / 3.);
		System.out.println("simulated annealing starting with temperature " + temperature);
		nTaken = nStages = nRejected = 0;

		//initialize the HashSet with all obstructed labels
		for (int i = 0; i < size; i++)
		{
			if(labels[i].isOverlapping() || labels[i].getUnplacable())
				obstructedLabels.add(labels[i]);
		}
		
		objective = calcObjectiveFunction();
	}

	protected boolean iterate()
	{
		List  newOverlappingLabels = new LinkedList();
		for (int k = 0; k < MOVES_PER_ITERATION; k++)
		{			
			//choose random overlapping label
			Label l = null;
			boolean found = false;
			do
			{
				if(obstructedLabels.size() == 0) //optimal solution found
				{
					System.out.println("stopping (optimum found)...");
					return true;
				}

				int i = PFLPApp.random_generator.nextInt(obstructedLabels.size());

				//is there a better way to get the n_th Element out of a set?
				Iterator it = obstructedLabels.iterator();
				for(int j=0; j <= i && it.hasNext(); j++)
					l = (Label) it.next();
				
				//we don't care about removing non-obstructed labels from the set, 
				//so this must be checked here...
				if(l.getUnplacable() || l.isOverlapping())
					found = true;
				else
					obstructedLabels.remove(l);

			} while(!found);
				
			//0 -> UNPLACED
			//1 -> TOPLEFT
			//2 -> TOPRIGHT
			//3 -> BOTTOMLEFT
			//4 -> BOTTOMRIGHT
			int old_pos = 0;
			int next_pos = -1;
			if(!l.getUnplacable())
			{
				if (l.isTopLeft())
					old_pos = 1;
				else if (l.isTopRight())
					old_pos = 2;
				else if (l.isBottomLeft())
					old_pos = 3;
				else if (l.isBottomRight())
					old_pos = 4;
				else 
					System.err.println("should be never reached [0]!");
			}
			
			boolean moved = false;
			if(PFLPApp.getOptionPointSelection())
			{
				//overlapping labels are removed with p = 1/4
				if(old_pos != 0 && l.isOverlapping() && PFLPApp.random_generator.nextDouble() <= 1./4.)
				{
					next_pos = 0;
					moved = true;
				}
			}
			
			if(!moved)
			{
				//reinsert or move the label to another randomly chosen position
				if(old_pos == 0)
					next_pos = PFLPApp.random_generator.nextInt(4) + 1;
				else
					next_pos = (old_pos + PFLPApp.random_generator.nextInt(3)) % 4 + 1;
			}
			
			//calculate the change of the objective function (< 0 means better)...
			long dE = 0;
			Label clone = null;
			newOverlappingLabels.clear();
			
			if (next_pos == 0) //we remove the label
			{
				dE += REMOVE_PENALTY; //deleted label
				
				Iterator orig_neighbours = l.getNeighbours().iterator();
				while(orig_neighbours.hasNext())
				{
					Label n = (Label) orig_neighbours.next();
					if(!n.getUnplacable() && n.doesIntersect(l))
						dE --;
				}
			}
			else
			{
				clone = new Label(l);
				clone.setUnplacable(false);
				switch(next_pos)
				{
					case 1:
						clone.moveTo(Label.TOPLEFT);
						break;
					case 2:
						clone.moveTo(Label.TOPRIGHT);
						break;
					case 3:
						clone.moveTo(Label.BOTTOMLEFT);
						break;
					case 4:
						clone.moveTo(Label.BOTTOMRIGHT);
						break;
				}
				
				if(old_pos == 0) //original label was unplaced
					dE -= REMOVE_PENALTY;

				boolean add_once = false;
				
				Iterator orig_neighbours = l.getNeighbours().iterator();
				while(orig_neighbours.hasNext())
				{
					Label neigh = (Label) orig_neighbours.next();
					
					if(neigh.getUnplacable())
						continue;
					
					boolean old_overplots = false;
					if(!l.getUnplacable())
						old_overplots = neigh.doesIntersect(l);
					
					boolean new_overplots = neigh.doesIntersect(clone);
					
					if(new_overplots)
					{
						if(!add_once)
						{
							add_once = true;
							newOverlappingLabels.add(l);
						}
						
						newOverlappingLabels.add(neigh);
					}
					
					if(old_overplots && !new_overplots)
						dE --;
					else if(!old_overplots && new_overplots)
						dE ++;
				}
			}

			double p = PFLPApp.random_generator.nextDouble();

			if (dE == 0 || dE > 0 && p >= Math.exp(-((double)dE) / temperature))
			{
				nRejected++;
			}
			else
			{
				nTaken++;
				
				//apply the move
				if(next_pos == 0)
				{
					l.setUnplacable(true);
					obstructedLabels.add(l);
				}
				else
				{
					l.moveTo(clone.getOffsetHorizontal(), clone.getOffsetVertical());
					l.setUnplacable(false);

					//add new produced intersections to our set of obstructed labels...
					Iterator itInt=newOverlappingLabels.iterator();
					while(itInt.hasNext())
					{
						Label o = (Label) itInt.next();
						obstructedLabels.add(o);
					}
				}
				
				//save new objective function value
				objective += dE;
				
//				if(objective != calcObjectiveFunction())
//				{
//					System.err.println("da is was faul im staate dänemark!" + objective + " vs. " + calcObjectiveFunction());
//				}
			}

			//cool?
			if (nTaken + nRejected >= 20 * size || nTaken > 5 * size)
			{
				if (nTaken == 0) //stop
				{
					System.out.println("stopping (nTaken == 0)...");
					cleanupSolution();
					return true;
				}

				//decrease temperature by 10%
				temperature = temperature * 0.9;
				
				System.out.println("decreasing temp. to " +  temperature + ", nTaken = " + nTaken + ", nRejected = " + nRejected + ", size = " + size);
				nStages++;
				nRejected = 0;
				nTaken = 0;
			}

			if (nStages > 50) //stop 
			{
				System.out.println("stopping (max stages reached)...");
				cleanupSolution();
				return true;
			}
		}
		return false;
	}

	private long calcObjectiveFunction()
	{
		long overplots = 0;
		long removed = 0;
		//count the number of pairwise overplots + #delted labels...
		for(int i=0; i<size; i++)
		{
			Label l1 = labels[i];
			if(l1.getUnplacable())
			{
				removed ++;
			}
			else
			{
				Iterator it = l1.getNeighbours().iterator();
				while(it.hasNext())
				{
					Label o = (Label) it.next();
					if(!o.getUnplacable() && o.doesIntersect(l1))
						overplots ++;
				}
			}
		}

		return (overplots / 2) + (removed * REMOVE_PENALTY);
	}

	private void cleanupSolution()
	{
		if(PFLPApp.getOptionPointSelection())
			super.cleanupSolution(solution);
	}
}
