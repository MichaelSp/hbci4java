/*  $Id: HBCIPassportPinTan.java,v 1.6 2012/03/13 22:07:43 willuhn Exp $

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

package org.kapott.hbci.passport;

import org.kapott.hbci.callback.HBCICallback;
import org.kapott.hbci.exceptions.HBCI_Exception;
import org.kapott.hbci.manager.*;
import org.kapott.hbci.manager.HHDVersion.Type;
import org.kapott.hbci.passport.storage.PassportData;
import org.kapott.hbci.passport.storage.PassportStorage;
import org.kapott.hbci.security.Sig;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * <p>Passport-Klasse für HBCI mit PIN/TAN. Dieses Sicherheitsverfahren wird erst
 * in FinTS 3.0 spezifiziert, von einigen Banken aber schon mit früheren HBCI-Versionen
 * angeboten.</p><p>
 * Bei diesem Verfahren werden die Nachrichten auf HBCI-Ebene nicht mit kryptografischen
 * Verfahren signiert oder verschlüsselt. Als "Signatur" werden statt dessen TANs
 * zusammen mit einer PIN verwendet. Die PIN wird dabei in <em>jeder</em> HBCI-Nachricht als
 * Teil der "Signatur" eingefügt, doch nicht alle Nachrichten benötigen eine TAN.
 * Eine TAN wird nur bei der Übermittlung bestimmter Geschäftsvorfälle benötigt. Welche
 * GV das konkret sind, ermittelt <em>HBCI4Java</em> automatisch aus den BPD. Für jeden GV, der
 * eine TAN benötigt, wird diese via Callback abgefragt und in die Nachricht eingefügt.</p><p>
 * Die Verschlüsselung der Nachrichten bei der Übertragung erfolgt auf einer höheren
 * Transportschicht. Die Nachrichten werden nämlich nicht direkt via TCP/IP übertragen,
 * sondern in das HTTP-Protokoll eingebettet. Die Verschlüsselung der übertragenen Daten
 * erfolgt dabei auf HTTP-Ebene (via SSL = HTTPS).</p><p>
 * Wie auch bei {@link org.kapott.hbci.passport.HBCIPassportRDHNew} wird eine "Schlüsseldatei"
 * verwendet. In dieser werden allerdings keine kryptografischen Schlüssel abgelegt, sondern
 * lediglich die Zugangsdaten für den HBCI-Server (Hostadresse, Nutzerkennung, usw.) sowie
 * einige zusätzliche Daten (BPD, UPD, zuletzt benutzte HBCI-Version). Diese Datei wird
 * vor dem Abspeichern verschlüsselt. Vor dem Erzeugen bzw. erstmaligen Einlesen wird via
 * Callback nach einem Passwort gefragt, aus welchem der Schlüssel für die Verschlüsselung
 * der Datei berechnet wird</p>
 */
public class HBCIPassportPinTan extends AbstractPinTanPassport {
    private String filename;

    /**
     * ct.
     *
     * @param init
     * @param dummy
     */
    public HBCIPassportPinTan(Object init, int dummy) {
        super(init);
    }

    /**
     * ct.
     *
     * @param initObject
     */
    public HBCIPassportPinTan(Object initObject) {
        this(initObject, 0);

        String header = "client.passport.PinTan.";
        String fname = HBCIUtils.getParam(header + "filename");
        boolean init = HBCIUtils.getParam(header + "init", "1").equals("1");

        setFileName(fname);
        setCertFile(HBCIUtils.getParam(header + "certfile"));
        setCheckCert(HBCIUtils.getParam(header + "checkcert", "1").equals("1"));

        setProxy(HBCIUtils.getParam(header + "proxy", ""));
        setProxyUser(HBCIUtils.getParam(header + "proxyuser", ""));
        setProxyPass(HBCIUtils.getParam(header + "proxypass", ""));

        if (init) {
            this.read();


            if (askForMissingData(true, true, true, true, true, true, true))
                saveChanges();
        }
    }

    /**
     * Gibt den Dateinamen der Schlüsseldatei zurück.
     *
     * @return Dateiname der Schlüsseldatei
     */
    public String getFileName() {
        return filename;
    }

    /**
     * Speichert den Dateinamen der Passport-Datei.
     *
     * @param filename
     */
    public void setFileName(String filename) {
        this.filename = filename;
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#resetPassphrase()
     */
    public void resetPassphrase() {
    }

    /**
     * Erzeugt die Passport-Datei wenn noetig.
     * In eine extra Funktion ausgelagert, damit es von abgeleiteten Klassen ueberschrieben werden kann.
     */
    protected void create() {
        String fname = this.getFileName();
        if (fname == null) {
            throw new NullPointerException("client.passport.PinTan.filename must not be null");
        }

        File file = new File(fname);
        if (file.exists() && file.isFile() && file.canRead())
            return;

        HBCIUtils.log("have to create new passport file", HBCIUtils.LOG_WARN);
        askForMissingData(true, true, true, true, true, true, true);
        saveChanges();
    }

    /**
     * Liest die Daten aus der Passport-Datei ein.
     * In eine extra Funktion ausgelagert, damit es von abgeleiteten Klassen ueberschrieben werden kann.
     * Zum Beispiel, um eine andere Art der Persistierung zu implementieren.
     */
    protected void read() {
        create();

        String fname = this.getFileName();
        if (fname == null)
            throw new NullPointerException("client.passport.PinTan.filename must not be null");

        PassportData data = PassportStorage.load(this, new File(fname));
        this.setCountry(data.country);
        this.setBLZ(data.blz);
        this.setHost(data.host);
        this.setPort(data.port);
        this.setUserId(data.userId);
        this.setSysId(data.sysId);
        this.setBPD(data.bpd);
        this.setUPD(data.upd);
        this.setHBCIVersion(data.hbciVersion);
        this.setCustomerId(data.customerId);
        this.setFilterType(data.filter);
        this.setAllowedTwostepMechanisms(data.twostepMechs);
        this.setCurrentTANMethod(data.tanMethod);
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassport#saveChanges()
     */
    @Override
    public void saveChanges() {
        try {

            final PassportData data = new PassportData();

            data.country = this.getCountry();
            data.blz = this.getBLZ();
            data.host = this.getHost();
            data.port = this.getPort();
            data.userId = this.getUserId();
            data.sysId = this.getSysId();
            data.bpd = this.getBPD();
            data.upd = this.getUPD();

            data.hbciVersion = this.getHBCIVersion();
            data.customerId = this.getCustomerId();
            data.filter = this.getFilterType();

            final List<String> l = getAllowedTwostepMechanisms();
            HBCIUtils.log("saving two step mechs: " + l, HBCIUtils.LOG_DEBUG);
            data.twostepMechs = l;

            final String s = this.getCurrentTANMethod(false);
            HBCIUtils.log("saving current tan method: " + s, HBCIUtils.LOG_DEBUG);
            data.tanMethod = s;

            PassportStorage.save(this, data, new File(this.getFileName()));
        } catch (HBCI_Exception he) {
            throw he;
        } catch (Exception e) {
            throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_PASSPORT_WRITEERR"), e);
        }
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#hash(byte[])
     */
    @Override
    public byte[] hash(byte[] data) {
        /* there is no hashing before signing, so we return the original message,
         * which will later be "signed" by sign() */
        return data;
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#sign(byte[])
     */
    @Override
    public byte[] sign(byte[] data) {
        try {
            // TODO: wenn die eingegebene PIN falsch war, muss die irgendwie
            // resettet werden, damit wieder danach gefragt wird
            if (getPIN() == null) {
                StringBuffer s = new StringBuffer();

                HBCIUtilsInternal.getCallback().callback(this,
                        HBCICallback.NEED_PT_PIN,
                        HBCIUtilsInternal.getLocMsg("CALLB_NEED_PTPIN"),
                        HBCICallback.TYPE_SECRET,
                        s);
                if (s.length() == 0) {
                    throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_PINZERO"));
                }
                setPIN(s.toString());
                LogFilter.getInstance().addSecretData(getPIN(), "X", LogFilter.FILTER_SECRETS);
            }

            String tan = "";

            // tan darf nur beim einschrittverfahren oder bei
            // PV=1 und passport.contains(challenge)           und tan-pflichtiger auftrag oder bei
            // PV=2 und passport.contains(challenge+reference) und HKTAN
            // ermittelt werden

            String pintanMethod = getCurrentTANMethod(false);

            if (pintanMethod.equals(Sig.SECFUNC_SIG_PT_1STEP)) {
                // nur beim normalen einschritt-verfahren muss anhand der segment-
                // codes ermittelt werden, ob eine tan benötigt wird
                HBCIUtils.log("onestep method - checking GVs to decide whether or not we need a TAN", HBCIUtils.LOG_DEBUG);

                // segment-codes durchlaufen
                String codes = collectSegCodes(new String(data, "ISO-8859-1"));
                StringTokenizer tok = new StringTokenizer(codes, "|");

                while (tok.hasMoreTokens()) {
                    String code = tok.nextToken();
                    String info = getPinTanInfo(code);

                    if (info.equals("J")) {
                        // für dieses segment wird eine tan benötigt
                        HBCIUtils.log("the job with the code " + code + " needs a TAN", HBCIUtils.LOG_DEBUG);

                        if (tan.length() == 0) {
                            // noch keine tan bekannt --> callback

                            StringBuffer s = new StringBuffer();
                            try {
                                HBCIUtilsInternal.getCallback().callback(this,
                                        HBCICallback.NEED_PT_TAN,
                                        HBCIUtilsInternal.getLocMsg("CALLB_NEED_PTTAN"),
                                        HBCICallback.TYPE_TEXT,
                                        s);
                            } catch (HBCI_Exception e) {
                                throw e;
                            } catch (Exception e) {
                                throw new HBCI_Exception(e);
                            }
                            if (s.length() == 0) {
                                throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_TANZERO"));
                            }
                            tan = s.toString();
                        } else {
                            HBCIUtils.log("there should be only one job that needs a TAN!", HBCIUtils.LOG_WARN);
                        }

                    } else if (info.equals("N")) {
                        HBCIUtils.log("the job with the code " + code + " does not need a TAN", HBCIUtils.LOG_DEBUG);

                    } else if (info.length() == 0) {
                        // TODO: ist das hier dann nicht ein A-Segment? In dem Fall
                        // wäre diese Warnung überflüssig
                        HBCIUtils.log("the job with the code " + code + " seems not to be allowed with PIN/TAN", HBCIUtils.LOG_WARN);
                    }
                }
            } else {
                HBCIUtils.log("twostep method - checking passport(challenge) to decide whether or not we need a TAN", HBCIUtils.LOG_DEBUG);
                Properties secmechInfo = getCurrentSecMechInfo();

                // gespeicherte challenge aus passport holen
                String challenge = (String) getPersistentData("pintan_challenge");
                setPersistentData("pintan_challenge", null);

                if (challenge == null) {
                    // es gibt noch keine challenge
                    HBCIUtils.log("will not sign with a TAN, because there is no challenge", HBCIUtils.LOG_DEBUG);
                } else {
                    HBCIUtils.log("found challenge in passport, so we ask for a TAN", HBCIUtils.LOG_DEBUG);

                    // willuhn 2011-05-27 Wir versuchen, den Flickercode zu ermitteln und zu parsen
                    String hhduc = (String) getPersistentData("pintan_challenge_hhd_uc");
                    setPersistentData("pintan_challenge_hhd_uc", null); // gleich wieder aus dem Passport loeschen

                    HHDVersion hhd = HHDVersion.find(secmechInfo);
                    HBCIUtils.log("detected HHD version: " + hhd, HBCIUtils.LOG_DEBUG);

                    final StringBuffer payload = new StringBuffer();
                    final String msg = secmechInfo.getProperty("name") + "\n" + secmechInfo.getProperty("inputinfo") + "\n\n" + challenge;

                    int callback = HBCICallback.NEED_PT_TAN;

                    // Um sicherzustellen, dass wird keinen falschen Callback ausloesen, weil wir die HHD-Version
                    // eventuell falsch erkannt haben, versuchen wir bei PhotoTAN und QR-Code zusaetzlich, die Daten
                    // zu parsen. Nur wenn sie korrekt geparst werden koennen, verwenden wir auch den spezifischen Callback
                    if (hhd.getType() == Type.PHOTOTAN && (MatrixCode.tryParse(hhduc) != null)) {
                        // Bei PhotoTAN haengen wir ungeparst das HHDuc an. Das kann dann auf
                        // Anwendungsseite per MatrixCode geparst werden
                        payload.append(hhduc);
                        callback = HBCICallback.NEED_PT_PHOTOTAN;
                    } else if (hhd.getType() == Type.QRCODE && (QRCode.tryParse(hhduc, msg) != null)) {
                        // Bei QR-Code haengen wir ungeparst das HHDuc an. Das kann dann auf
                        // Anwendungsseite per QRCode geparst werden
                        payload.append(hhduc);
                        callback = HBCICallback.NEED_PT_QRTAN;
                    } else {
                        FlickerCode flicker = FlickerCode.tryParse(challenge, hhduc);
                        if (flicker != null) {
                            // Bei chipTAN liefern wir den bereits geparsten und gerenderten Flickercode
                            payload.append(flicker.render());
                        }
                    }

                    // Callback durchfuehren
                    HBCIUtilsInternal.getCallback().callback(this, callback, msg, HBCICallback.TYPE_TEXT, payload);

                    setPersistentData("externalid", null); // External-ID aus Passport entfernen
                    if (payload == null || payload.length() == 0) {
                        throw new HBCI_Exception(HBCIUtilsInternal.getLocMsg("EXCMSG_TANZERO"));
                    }
                    tan = payload.toString();
                }
            }
            if (tan.length() != 0) {
                LogFilter.getInstance().addSecretData(tan, "X", LogFilter.FILTER_SECRETS);
            }

            return (getPIN() + "|" + tan).getBytes("ISO-8859-1");
        } catch (Exception ex) {
            throw new HBCI_Exception("*** signing failed", ex);
        }
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#verify(byte[], byte[])
     */
    public boolean verify(byte[] data, byte[] sig) {
        // TODO: fuer bankensignaturen fuer HITAN muss dass hier geändert werden
        return true;
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#encrypt(byte[])
     */
    public byte[][] encrypt(byte[] plainMsg) {
        try {
            int padLength = plainMsg[plainMsg.length - 1];
            byte[] encrypted = new String(plainMsg, 0, plainMsg.length - padLength, "ISO-8859-1").getBytes("ISO-8859-1");
            return new byte[][]{new byte[8], encrypted};
        } catch (Exception ex) {
            throw new HBCI_Exception("*** encrypting message failed", ex);
        }
    }

    /**
     * @see org.kapott.hbci.passport.HBCIPassportInternal#decrypt(byte[], byte[])
     */
    public byte[] decrypt(byte[] cryptedKey, byte[] cryptedMsg) {
        try {
            return new String(new String(cryptedMsg, "ISO-8859-1") + '\001').getBytes("ISO-8859-1");
        } catch (Exception ex) {
            throw new HBCI_Exception("*** decrypting of message failed", ex);
        }
    }

    /**
     * @see org.kapott.hbci.passport.AbstractHBCIPassport#close()
     */
    public void close() {
        super.close();
    }

}
