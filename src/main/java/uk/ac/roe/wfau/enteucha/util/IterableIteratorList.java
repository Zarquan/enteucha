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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A {@link List} based {@link IteratableIterator}.
 *
 */
public class IterableIteratorList<T>
extends IteratableIterator<T>
implements Iterable<T>
    {
    public IterableIteratorList(int size)
        {
        this(
            new ArrayList<Iterator<T>>(
                size
                )
            );
        }
    
    public IterableIteratorList()
        {
        this(
            new ArrayList<Iterator<T>>()
            );
        }

    public IterableIteratorList(List<Iterator<T>> list)
        {
        super(
            list
            );
        this.list = list ;
        }
    
    private List<Iterator<T>> list ;
    
    public void add(Iterator<T> iter)
        {
        list.add(
            iter
            );
        }
    }
