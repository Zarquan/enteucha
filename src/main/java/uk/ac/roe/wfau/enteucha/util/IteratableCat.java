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
 * An {@link Iterable} that concatenates the elements from a list of {@link Iterator}s.
 *
 */
public class IteratableCat<T> implements Iterable<T>
    {
    /**
     * Public constructor.
     * @param iters The {@link Iterable} list of {@link Iterator}s to concatenate. 
     * 
     */
    public IteratableCat(Iterable<Iterator<T>> iters)
        {
        this.iters = iters;
        }
    
    private Iterable<Iterator<T>> iters ;
    
    @Override
    public Iterator<T> iterator()
        {
        return new Iterator<T>()
            {
            private Iterator<Iterator<T>> iter = iters.iterator();
            private Iterator<T> next = null ;

            @Override
            public boolean hasNext()
                {
                if (next == null)
                    {
                    if (iter.hasNext())
                        {
                        next = iter.next();
                        }
                    }
                if (next != null)
                    {
                    if (next.hasNext())
                        {
                        return true ;
                        }
                    else {
                        if (iter.hasNext())
                            {
                            next = iter.next();
                            return this.hasNext();
                            }
                        }
                    }
                return false;
                }
        
            @Override
            public T next()
                {
                if (this.hasNext())
                    {
                    return next.next();
                    }
                else {
                    throw new UnsupportedOperationException(
                        "Call to next() on empty Iterator"
                        ); 
                    }
                }
            };
        }
    }
