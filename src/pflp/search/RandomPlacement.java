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

import pflp.PFLPApp;
import pflp.Solution;

/**
 * random labeling (4pos)
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */

public class RandomPlacement extends SearchThread
{
	public RandomPlacement()
	{
		super();
		name = new String("random (4pos)");
	}

	protected void precompute()
	{
		PFLPApp.solution = new Solution(PFLPApp.instance);

		if (PFLPApp.getOptionPointSelection())
			super.cleanupSolution(PFLPApp.solution);
	}

	protected boolean iterate()
	{
		return true; //everything is done in precompute...
	}

}
