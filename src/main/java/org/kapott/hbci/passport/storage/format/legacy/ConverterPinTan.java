/**********************************************************************
 *
 * Copyright (c) by Olaf Willuhn
 * All rights reserved
 *
 **********************************************************************/

package org.kapott.hbci.passport.storage.format.legacy;

import org.kapott.hbci.manager.HBCIUtils;
import org.kapott.hbci.passport.HBCIPassport;
import org.kapott.hbci.passport.HBCIPassportPinTan;
import org.kapott.hbci.passport.storage.PassportData;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Properties;

/**
 * Implementierung des Converter fuer PIN/TAN.
 */
public class ConverterPinTan extends AbstractConverter {
    /**
     * @see org.kapott.hbci.passport.storage.format.legacy.Converter#load(java.io.InputStream)
     */
    @Override
    public PassportData load(InputStream is) throws Exception {
        final PassportData data = new PassportData();

        final ObjectInputStream o = new ObjectInputStream(is);

        data.country = (String) o.readObject();
        data.blz = (String) o.readObject();
        data.host = (String) o.readObject();
        data.port = (Integer) o.readObject();
        data.userId = (String) o.readObject();
        data.sysId = (String) o.readObject();
        data.bpd = (Properties) o.readObject();
        data.upd = (Properties) o.readObject();
        data.hbciVersion = (String) o.readObject();
        data.customerId = (String) o.readObject();
        data.filter = (String) o.readObject();

        // Wir tolerieren, wenn das fehlschlaegt
        try {
            data.twostepMechs = (List<String>) o.readObject();
        } catch (Exception e) {
            HBCIUtils.log("no list of allowed secmechs found in passport file", HBCIUtils.LOG_WARN);
        }
        try {
            data.tanMethod = (String) o.readObject();
        } catch (Exception e) {
            HBCIUtils.log("no current secmech found in passport file", HBCIUtils.LOG_WARN);
        }

        return data;
    }

    /**
     * @see org.kapott.hbci.passport.storage.format.legacy.Converter#save(org.kapott.hbci.passport.storage.PassportData, java.io.OutputStream)
     */
    @Override
    public void save(PassportData data, OutputStream os) throws Exception {
        final ObjectOutputStream o = new ObjectOutputStream(os);
        o.writeObject(data.country);
        o.writeObject(data.blz);
        o.writeObject(data.host);
        o.writeObject(data.port);
        o.writeObject(data.userId);
        o.writeObject(data.sysId);
        o.writeObject(data.bpd);
        o.writeObject(data.upd);
        o.writeObject(data.hbciVersion);
        o.writeObject(data.customerId);
        o.writeObject(data.filter);
        o.writeObject(data.twostepMechs);
        o.writeObject(data.tanMethod);
        os.flush();
    }

    /**
     * @see org.kapott.hbci.passport.storage.format.legacy.Converter#supports(org.kapott.hbci.passport.HBCIPassport)
     */
    @Override
    public boolean supports(HBCIPassport passport) {
        return passport != null && (passport instanceof HBCIPassportPinTan);
    }

}
