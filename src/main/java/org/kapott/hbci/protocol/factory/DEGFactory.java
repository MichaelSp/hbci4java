/*  $Id: DEGFactory.java,v 1.1 2011/05/04 22:37:49 willuhn Exp $

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

package org.kapott.hbci.protocol.factory;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.protocol.DEG;
import org.kapott.hbci.tools.ObjectFactory;
import org.w3c.dom.Document;

import java.util.Hashtable;

public class DEGFactory
        extends ObjectFactory {
    private static DEGFactory instance;

    private DEGFactory() {
        super(Integer.parseInt(HBCIUtils.getParam("kernel.objpool.DEG", "512")));
    }

    public static synchronized DEGFactory getInstance() {
        if (instance == null) {
            instance = new DEGFactory();
        }
        return instance;
    }

    public DEG createDEG(String type, String name, String path, char predelim, int idx, StringBuffer res, int fullResLen, Document syntax, Hashtable<String, String> predefs, Hashtable<String, String> valids) {
        DEG ret = (DEG) getFreeObject();

        if (ret == null) {
            // HBCIUtils.log("creating new DEG object",HBCIUtils.LOG_DEBUG);
            ret = new DEG(type, name, path, predelim, idx, res, fullResLen, syntax, predefs, valids);
            addToUsedPool(ret);
        } else {
            // HBCIUtils.log("reusing DEG object",HBCIUtils.LOG_DEBUG);
            try {
                ret.init(type, name, path, predelim, idx, res, fullResLen, syntax, predefs, valids);
                addToUsedPool(ret);
            } catch (RuntimeException e) {
                addToFreePool(ret);
                throw e;
            }
        }

        return ret;
    }

    public DEG createDEG(String type, String name, String path, int idx, Document syntax) {
        DEG ret = (DEG) getFreeObject();

        if (ret == null) {
            // HBCIUtils.log("creating new DEG object",HBCIUtils.LOG_DEBUG);
            ret = new DEG(type, name, path, idx, syntax);
            addToUsedPool(ret);
        } else {
            // HBCIUtils.log("reusing DEG object",HBCIUtils.LOG_DEBUG);
            try {
                ret.init(type, name, path, idx, syntax);
                addToUsedPool(ret);
            } catch (RuntimeException e) {
                addToFreePool(ret);
                throw e;
            }
        }

        return ret;
    }

    public void unuseObject(Object o) {
        if (o != null) {
            ((DEG) o).destroy();
            super.unuseObject(o);
        }
    }
}
