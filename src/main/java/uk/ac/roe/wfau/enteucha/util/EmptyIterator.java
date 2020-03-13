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
 * Empty implementation of the {@link Iterator} interface. 
 *
 */
public class EmptyIterator<E>
    implements Iterator<E>
    {
    @Override
    public boolean hasNext()
        {
        return false;
        }

    @Override
    public E next()
        {
        throw new UnsupportedOperationException(
            "EmptyIterator has no elements"
            );
        }

    @Override
    public void remove()
        {
        throw new UnsupportedOperationException(
            "EmptyIterator has no elements"
            );
        }
    }
