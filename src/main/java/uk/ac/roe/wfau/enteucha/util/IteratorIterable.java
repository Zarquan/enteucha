/*
 *  Copyright (C) 2020 Royal Observatory, University of Edinburgh, UK
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package uk.ac.roe.wfau.enteucha.util;

import java.util.Iterator;

/**
 *
 */
public class IteratorIterable<T>
implements Iterable<T>
    {
    public IteratorIterable(final Iterator<T> iter)
        {
        this.iter = iter;
        }
    
    private Iterator<T> iter ;
    
    @Override
    public synchronized Iterator<T> iterator()
        {
        if (this.iter != null)
            {
            Iterator<T> temp = this.iter;
            this.iter = null ;
            return temp;
            }
        else {
            throw new UnsupportedOperationException(
                "Iterator<T> already used"
                );
            }
        }
    }
