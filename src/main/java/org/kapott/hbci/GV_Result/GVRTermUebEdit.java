/*  $Id: GVRTermUebEdit.java,v 1.1 2011/05/04 22:37:48 willuhn Exp $

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

package org.kapott.hbci.GV_Result;

import org.kapott.hbci.manager.HBCIUtilsInternal;

/**
 * Ergebnis der Änderung einer terminierten Überweisung. Ein geänderter Auftrag kann
 * u.U. eine andere Auftrags-Identifikationsnummer erhalten als der ursprüngliche
 * Auftrag. Die neue und optional die alte Auftrags-ID können mit dieser Klasse
 * ermittelt werden.
 */
public class GVRTermUebEdit
        extends HBCIJobResultImpl {
    private String orderid;
    private String orderidold;

    /**
     * Gibt die Auftrags-Identifikationsnummer des ursprünglichen
     * Dauerauftrages zurück.
     *
     * @return alte Auftrags-ID oder <code>null</code>, wenn diese nicht
     * von der Bank bereitgestellt wird
     */
    public String getOrderIdOld() {
        return orderidold;
    }

    public void setOrderIdOld(String orderidold) {
        this.orderidold = orderidold;
    }

    public String toString() {
        return HBCIUtilsInternal.getLocMsg("ORDERID") + ": " + getOrderId();
    }

    /**
     * Gibt die Auftrags-Identifikationsnummer des geänderten Auftrages zurück.
     *
     * @return neue Auftrags-ID
     */
    public String getOrderId() {
        return orderid;
    }

    public void setOrderId(String orderid) {
        this.orderid = orderid;
    }
}
