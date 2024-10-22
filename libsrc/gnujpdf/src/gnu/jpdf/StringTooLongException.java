/*
 * $Id: StringTooLongException.java,v 1.1 2007/08/22 01:02:32 gil1 Exp $ 
 *
 * $Date: 2007/08/22 01:02:32 $
 *
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version. 
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 * Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA 
 */
package gnu.jpdf;

/**
 * <p>
 * This exception is thrown from {@link gnu.jpdf.BoundingBox} if the string
 * won't fit into the box</p>
 *
 * @author Eric Z. Beard, ericzbeard@hotmail.com
 * @version $Revision: 1.1 $ $Date: 2007/08/22 01:02:32 $
 */
public class StringTooLongException extends Exception {

    private String msg;

    /**
     * <p>
     * Normally this exception is constructed with a message containing
     * information about the sizes of the parent and child box, and maybe the
     * string that caused the overflow.</p>
     *
     * @param msg a <code>String</code>, some informative message for the logs
     */
    public StringTooLongException(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return msg;
    }

    @Override
    public String getMessage() {
        return msg;
    }
} // end class StringTooLongException
