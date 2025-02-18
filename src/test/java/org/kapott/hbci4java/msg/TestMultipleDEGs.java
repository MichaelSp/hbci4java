package org.kapott.hbci4java.msg;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kapott.hbci.manager.HBCIKernelImpl;
import org.kapott.hbci.manager.MsgGen;
import org.kapott.hbci.protocol.MSG;
import org.kapott.hbci.protocol.MultipleSyntaxElements;
import org.kapott.hbci.protocol.factory.MSGFactory;
import org.kapott.hbci4java.AbstractTest;

import java.util.Hashtable;

/**
 * Testet den Workaround zum Abkuerzen multipler optionaler DEGs. Siehe
 * {@link MultipleSyntaxElements#initData}
 */
public class TestMultipleDEGs extends AbstractTest {
    /**
     * Deaktiviert den Test, wenn das System-Property nicht auf "true" steht.
     *
     * @throws Exception
     */
    @BeforeClass
    public static void beforeClass() throws Exception {
        Assume.assumeTrue(Boolean.valueOf(System.getProperty(AbstractTest.SYSPROP_PERFORMANCE, Boolean.TRUE.toString())));
    }


    /**
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

        String data = getFile("TestMultipleDEGs-01.txt");
        HBCIKernelImpl kernel = new HBCIKernelImpl(null, "300");

        kernel.rawNewMsg("DialogInit");

        long start = System.currentTimeMillis();
        MsgGen gen = kernel.getMsgGen();
        MSG msg = MSGFactory.getInstance().createMSG("DialogInitRes", data, data.length(), gen);
        Hashtable<String, String> ht = new Hashtable<String, String>();
        msg.extractValues(ht);
        long end = System.currentTimeMillis();

        // List<String> keys = new ArrayList<String>(ht.keySet());
        // Collections.sort(keys);
        // for (String key:keys)
        // {
        // System.out.println(key + ": " + ht.get(key));
        // }
        //
        // Das sollte unter 1 Sekunde dauern
        long used = end - start;
        System.out.println("used time: " + used + " millis");
        Assert.assertTrue("Sollte weniger als 1 Sekunde dauern", used < 1000);
    }

}
