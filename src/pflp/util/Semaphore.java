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


package pflp.util;
/**
 * Implements a little Semaphore class by using
 * wait() and notify().
 * The idea of this class has been seen in an java tutorial somewhere
 * in the net.
 * @author Ebner Dietmar, ebner@apm.tuwien.ac.at
 */
public class Semaphore 
{
	private int counter;

	public Semaphore() 
	{
		this(0);
	}

	public Semaphore(int i) 
	{
		if (i < 0) 
		throw new IllegalArgumentException(i + " < 0");
		counter = i;
	}
    
	/**
	 * frees the access to the corresponding resource
	 */
	public synchronized void release() 
	{
		if (counter == 0) 
			this.notify();

		counter++;
	}

	/**
	 * blocks until the resource is available
	 */
	public synchronized void acquire() throws InterruptedException 
	{
		while (counter == 0) 
			this.wait();
		counter--;
	}
}