/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 * LGPLv2
 *
 **********************************************************************/

package org.kapott.hbci4java.msg;

import org.junit.Assert;
import org.junit.Test;
import org.kapott.hbci.exceptions.ParseErrorException;
import org.kapott.hbci.manager.HBCIKernelImpl;
import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.manager.MsgGen;
import org.kapott.hbci.protocol.MSG;
import org.kapott.hbci.protocol.factory.MSGFactory;
import org.kapott.hbci4java.AbstractTest;

import java.util.Hashtable;

/**
 * Tests fuer BUGZILLA 1129.
 */
public class TestBug1129 extends AbstractTest {
    /**
     * Testet das Parsen eines Responses mit ungueltigem DTAUS ohne Fehlertoleranz.
     * Code muss einen Fehler werfen.
     *
     * @throws Exception
     */
    @Test
    public void test001() throws Exception {
        try {
            parse();
            throw new Exception("Test-Code haette eine Exception werfen muessen");
        } catch (Exception e) {
            Assert.assertEquals(ParseErrorException.class, e.getClass());
            Assert.assertTrue(((ParseErrorException) e).isFatal());
        }
    }

    /**
     * Versucht, die Datei mit dem Response zu parsen.
     *
     * @return die geparsten Daten.
     * @throws Exception
     */
    private Hashtable<String, String> parse() throws Exception {
        String data = this.getFile("bugzilla-1129.txt");
        HBCIKernelImpl kernel = new HBCIKernelImpl(null, "plus");
        kernel.rawNewMsg("DauerList");

        MsgGen gen = kernel.getMsgGen();
        MSG msg = MSGFactory.getInstance().createMSG("CustomMsgRes", data, data.length(), gen);

        Hashtable<String, String> ht = new Hashtable<String, String>();
        msg.extractValues(ht);
        return ht;
    }

    /**
     * Testet das Parsen eines Responses mit ungueltigem DTAUS MIT Fehlertoleranz.
     *
     * @throws Exception
     */
    @Test
    public void test002() throws Exception {
        HBCIUtils.setParam("client.errors.ignoreWrongDataSyntaxErrors", "yes");
        parse();
    }

    /**
     * Testet das Decodieren der DIN-66003 Umlaute.
     *
     * @throws Exception
     */
    @Test
    public void test003() throws Exception {
        HBCIUtils.setParam("client.errors.ignoreWrongDataSyntaxErrors", "yes");
        Hashtable<String, String> ht = parse();
        Assert.assertEquals("EBÜHREN Z.T. IM VORAUS", ht.get("CustomMsgRes.GVRes_6.DauerListRes4.usage.usage_3"));
    }

}
