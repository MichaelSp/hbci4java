/*  $Id: GVUmb.java,v 1.1 2011/05/04 22:37:53 willuhn Exp $

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

import org.kapott.hbci.manager.HBCIHandler;
import org.kapott.hbci.manager.HBCIUtilsInternal;
import org.kapott.hbci.manager.LogFilter;

import java.util.Properties;

public class GVUmb
        extends GVUeb {
    public GVUmb(HBCIHandler handler) {
        super(handler, getLowlevelName());

        addConstraint("src.country", "My.KIK.country", "DE", LogFilter.FILTER_NONE);
        addConstraint("src.blz", "My.KIK.blz", null, LogFilter.FILTER_MOST);
        addConstraint("src.number", "My.number", null, LogFilter.FILTER_IDS);
        addConstraint("src.subnumber", "My.subnumber", "", LogFilter.FILTER_MOST);
        addConstraint("dst.country", "Other.KIK.country", "DE", LogFilter.FILTER_NONE);
        addConstraint("dst.blz", "Other.KIK.blz", null, LogFilter.FILTER_MOST);
        addConstraint("dst.number", "Other.number", null, LogFilter.FILTER_IDS);
        addConstraint("dst.subnumber", "Other.subnumber", "", LogFilter.FILTER_MOST);
        addConstraint("btg.value", "BTG.value", null, LogFilter.FILTER_MOST);
        addConstraint("btg.curr", "BTG.curr", null, LogFilter.FILTER_NONE);
        addConstraint("name", "name", null, LogFilter.FILTER_IDS);

        addConstraint("name2", "name2", "", LogFilter.FILTER_IDS);
        addConstraint("key", "key", "51", LogFilter.FILTER_NONE);

        Properties parameters = getJobRestrictions();
        int maxusage = Integer.parseInt(parameters.getProperty("maxusage"));

        for (int i = 0; i < maxusage; i++) {
            String name = HBCIUtilsInternal.withCounter("usage", i);
            addConstraint(name, "usage." + name, "", LogFilter.FILTER_MOST);
        }
    }

    public static String getLowlevelName() {
        return "Umb";
    }
}
