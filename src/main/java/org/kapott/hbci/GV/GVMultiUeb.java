/*  $Id: GVMultiUeb.java,v 1.1 2011/05/04 22:37:53 willuhn Exp $

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
import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.LogFilter;

public class GVMultiUeb
        extends AbstractMultiGV {
    public GVMultiUeb(HBCIHandler handler) {
        super(handler, getLowlevelName(), new HBCIJobResultImpl());

        addConstraint("data", "data", null, LogFilter.FILTER_NONE);
        addConstraint("my.country", "KTV.KIK.country", "DE", LogFilter.FILTER_NONE);
        addConstraint("my.blz", "KTV.KIK.blz", null, LogFilter.FILTER_MOST);
        addConstraint("my.number", "KTV.number", null, LogFilter.FILTER_IDS);
        addConstraint("my.subnumber", "KTV.subnumber", "", LogFilter.FILTER_MOST);
    }

    public static String getLowlevelName() {
        return "SammelUeb";
    }

    public void setParam(String paramName, String value) {
        if (paramName.equals("data")) {
            value = "B" + value;
        }
        super.setParam(paramName, value);
    }

    public void verifyConstraints() {
        super.verifyConstraints();
        checkAccountCRC("my");
    }
}
