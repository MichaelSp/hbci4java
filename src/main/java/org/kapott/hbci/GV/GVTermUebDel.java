/*  $Id: GVTermUebDel.java,v 1.1 2011/05/04 22:37:54 willuhn Exp $

    This file is part of HBCI4Java
    Copyright (C) 2001-2008  Stefan Palme

    HBCI4Java is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    HBCI4Java is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package org.kapott.hbci.GV;

import org.kapott.hbci.GV_Result.HBCIJobResultImpl;
import org.kapott.hbci.exceptions.InvalidUserDataException;
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.manager.LogFilter;

import java.util.Enumeration;
import java.util.Properties;

public final class GVTermUebDel
        extends HBCIJobImpl {
    public GVTermUebDel(HBCIHandler handler) {
        super(handler, getLowlevelName(), new HBCIJobResultImpl());

        addConstraint("orderid", "id", null, LogFilter.FILTER_NONE);
    }

    public static String getLowlevelName() {
        return "TermUebDel";
    }

    public void setParam(String paramName, String value) {
        super.setParam(paramName, value);

        if (paramName.equals("orderid")) {
            Properties p = (Properties) getMainPassport().getPersistentData("termueb_" + value);
            if (p == null) {
                String msg = HBCIUtilsInternal.getLocMsg("EXCMSG_NOSUCHSCHEDTRANS", value);
                if (!HBCIUtilsInternal.ignoreError(getMainPassport(), "client.errors.ignoreWrongJobDataErrors", msg))
                    throw new InvalidUserDataException(msg);
                p = new Properties();
            }

            for (Enumeration e = p.propertyNames(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();

                setLowlevelParam(getName() + "." + key,
                        p.getProperty(key));
            }
        }
    }
}
