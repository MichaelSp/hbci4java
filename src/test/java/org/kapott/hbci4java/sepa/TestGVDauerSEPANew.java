package org.kapott.hbci4java.sepa;

import org.junit.Test;
import org.kapott.hbci.GV.HBCIJob;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.structures.Konto;
import org.kapott.hbci.structures.Value;
import org.kapott.hbci4java.AbstractTestGV;

import java.util.Properties;


/**
 * Testet das Einreichen eines neuen SEPA-Dauerauftrages.
 */
public class TestGVDauerSEPANew extends AbstractTestGV {
    /**
     * Testet das Einreichen eines neuen SEPA-Dauerauftrages.
     */
    @Test
    public void test() {
        this.execute(new Execution() {

            @Override
            public String getJobname() {
                return "DauerSEPANew";
            }

            /**
             * @see org.kapott.hbci4java.AbstractTestGV.Execution#configure(org.kapott.hbci.GV.HBCIJob, org.kapott.hbci.passport.HBCIPassport, java.util.Properties)
             */
            @Override
            public void configure(HBCIJob job, HBCIPassport passport, Properties params) {
                Konto acc = new Konto();
                acc.blz = params.getProperty("blz");
                acc.number = params.getProperty("konto");
                acc.name = params.getProperty("name");
                acc.bic = params.getProperty("bic");
                acc.iban = params.getProperty("iban");
                job.setParam("dst", acc);

                int idx = Integer.parseInt(params.getProperty("passport_index", "0"));
                job.setParam("src", passport.getAccounts()[idx]);

                String value = params.getProperty("value", "1");
                job.setParam("btg", new Value(Integer.parseInt(value), "EUR"));
                job.setParam("usage", params.getProperty("usage"));

                job.setParam("firstdate", params.getProperty("firstdate"));
                job.setParam("timeunit", params.getProperty("timeunit", "M"));
                job.setParam("turnus", params.getProperty("turnus", "1"));
                job.setParam("execday", params.getProperty("execday", "1"));
            }

        });
    }
}
