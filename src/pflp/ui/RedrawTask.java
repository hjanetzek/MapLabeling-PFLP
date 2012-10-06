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

package pflp.ui;

import java.util.TimerTask;

/**
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 *
 * forces the given visualization to reflect the current solution
 */
public class RedrawTask extends TimerTask
{
	private Visualization visualization = null;
	
	public RedrawTask(Visualization v)
	{
		visualization = v;
	}
	
	public void run()
	{
		if(visualization != null)
		{
			visualization.updateClone(false);
			visualization.repaint();
		}

	}
}
