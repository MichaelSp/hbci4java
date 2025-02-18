/*  $Id: DDVCardService.java,v 1.1 2011/11/24 21:59:37 willuhn Exp $

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

package org.kapott.hbci.smartcardio;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


/**
 * Abstrakter DDV-Cardservice fuer den DDVPCSC-Passport, basierend auf dem OCF-Code
 * aus HBCI4Java 2.5.8.
 */
public abstract class DDVCardService extends HBCICardService {
    private String cid = null;

    /**
     * Liefert die Schluesseldaten.
     *
     * @return die Schluesseldaten.
     */
    public abstract DDVKeyData[] readKeyData();

    /**
     * @see org.kapott.hbci.smartcardio.HBCICardService#getCID()
     */
    @Override
    public String getCID() {
        if (this.cid == null) {
            byte[] bytes = readRecordBySFI(HBCI_DDV_EF_ID, 0);
            this.cid = new String(bytes, SmartCardService.CHARSET);
        }
        return this.cid;
    }

    /**
     * Liefert die Bank-Daten fuer den angegebenen Entry-Index.
     *
     * @param idx der Entry-Index.
     * @return die Bank-Daten.
     */
    public DDVBankData readBankData(int idx) {
        byte[] rawData = readRecordBySFI(HBCI_DDV_EF_BNK, idx);

        if (rawData == null)
            return null;

        DDVBankData ret = new DDVBankData();

        ret.recordnum = idx + 1;
        ret.shortname = new String(rawData, 0, 20, SmartCardService.CHARSET).trim();

        StringBuffer blz = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            // Linker Nibble ;)
            // 4 Byte nach rechts verschoben
            byte ch = rawData[20 + i];
            byte nibble = (byte) ((ch >> 4) & 0x0F);
            if (nibble > 0x09)
                nibble ^= 0x0F;

            blz.append((char) (nibble + 0x30)); // In ASCII-Bereich verschieben

            // Rechter Nibble
            nibble = (byte) (ch & 0x0F);
            if (nibble > 0x09)
                nibble ^= 0x0F;

            blz.append((char) (nibble + 0x30));
        }
        ret.blz = blz.toString();

        ret.commType = rawData[24];
        ret.commaddr = new String(rawData, 25, 28, SmartCardService.CHARSET).trim();
        ret.commaddr2 = new String(rawData, 53, 2, SmartCardService.CHARSET).trim();
        ret.country = new String(rawData, 55, 3, SmartCardService.CHARSET).trim();
        ret.userid = new String(rawData, 58, 30, SmartCardService.CHARSET).trim();

        return ret;
    }

    /**
     * Speichert die Bank-Daten auf die Karte.
     *
     * @param idx      Entry-Index.
     * @param bankData die Bank-Daten.
     */
    public void writeBankData(int idx, DDVBankData bankData) {
        byte[] rawData = new byte[88];

        System.arraycopy(expand(bankData.shortname, 20), 0, rawData, 0, 20);

        byte[] blzData = bankData.blz.getBytes(SmartCardService.CHARSET);
        for (int i = 0; i < 4; i++) {
            byte ch1 = (byte) (blzData[i << 1] - 0x30);
            byte ch2 = (byte) (blzData[(i << 1) + 1] - 0x30);

            if (ch1 == 2 && ch2 == 0) {
                ch1 ^= 0x0F;
            }

            rawData[20 + i] = (byte) ((ch1 << 4) | ch2);
        }

        rawData[24] = (byte) bankData.commType;
        System.arraycopy(expand(bankData.commaddr, 28), 0, rawData, 25, 28);
        System.arraycopy(expand(bankData.commaddr2, 2), 0, rawData, 53, 2);
        System.arraycopy(expand(bankData.country, 3), 0, rawData, 55, 3);
        System.arraycopy(expand(bankData.userid, 30), 0, rawData, 58, 30);

        updateRecordBySFI(HBCI_DDV_EF_BNK, idx, rawData);
    }

    /**
     * Liefert die Sig-ID.
     *
     * @return die Sig-ID.
     */
    public int readSigId() {
        int ret = -1;

        byte[] rawData = readRecordBySFI(HBCI_DDV_EF_SEQ, 0);
        if (rawData != null)
            ret = ((rawData[0] << 8) & 0xFF00) | (rawData[1] & 0xFF);

        return ret;
    }

    /**
     * Speichert die Sig-ID.
     *
     * @param sigId die Sig-ID.
     */
    public void writeSigId(int sigId) {
        byte[] rawData = new byte[2];
        rawData[0] = (byte) ((sigId >> 8) & 0xFF);
        rawData[1] = (byte) (sigId & 0xFF);
        updateRecordBySFI(HBCI_DDV_EF_SEQ, 0, rawData);
    }

    /**
     * Signiert die Daten.
     *
     * @param data die zu signierenden Daten.
     * @return die Signatur.
     */
    public byte[] sign(byte[] data) {
        byte[] data_l = new byte[8];
        byte[] data_r = new byte[12];

        System.arraycopy(data, 0, data_l, 0, 8);
        System.arraycopy(data, 8, data_r, 0, 12);

        updateRecordBySFI(HBCI_DDV_EF_MAC, 0, data_r);
        return calculateSignature(data_l);
    }

    /**
     * Erzeugt eine Signatur.
     *
     * @param data_l die zu signierenden Daten.
     * @return die Signature,
     */
    protected abstract byte[] calculateSignature(byte[] data_l);

    /**
     * Liefert die Encryption-Keys.
     *
     * @param keynum Schluessel-Nummer.
     * @return Encryption-Keys.
     */
    public byte[][] getEncryptionKeys(int keynum) {
        byte[][] keys = new byte[2][16];

        for (int i = 0; i < 2; i++) {
            byte[] challenge = getChallenge();
            System.arraycopy(challenge, 0, keys[0], i << 3, 8);
            byte[] enc = internalAuthenticate(keynum, challenge);
            System.arraycopy(enc, 0, keys[1], i << 3, 8);
        }

        return keys;
    }

    /**
     * Entschluesselt die Daten.
     *
     * @param keynum  die Schluessel-Nummer.
     * @param encdata die verschluesselten Daten.
     * @return die entschluesselten Daten.
     */
    public byte[] decrypt(int keynum, byte[] encdata) {
        byte[] plaindata = new byte[16];

        for (int i = 0; i < 2; i++) {
            byte[] enc = new byte[8];
            System.arraycopy(encdata, i << 3, enc, 0, 8);
            byte[] plain = internalAuthenticate(keynum, enc);
            System.arraycopy(plain, 0, plaindata, i << 3, 8);
        }

        return plaindata;
    }

    /**
     * @see org.kapott.hbci.smartcardio.HBCICardService#createPINVerificationDataStructure(int)
     */
    @Override
    protected byte[] createPINVerificationDataStructure(int pwdId) throws IOException {
        ByteArrayOutputStream verifyCommand = new ByteArrayOutputStream();
        verifyCommand.write(0x0f); // bTimeOut
        verifyCommand.write(0x05); // bTimeOut2
        verifyCommand.write(0x89); // bmFormatString
        verifyCommand.write(0x07); // bmPINBlockString
        verifyCommand.write(0x10); // bmPINLengthFormat
        verifyCommand.write(new byte[]{(byte) 8, (byte) 4}); // PIN size (max/min), volker: 12,4=>8,4
        verifyCommand.write(0x02); // bEntryValidationCondition
        verifyCommand.write(0x01); // bNumberMessage
        verifyCommand.write(new byte[]{0x04, 0x09}); // wLangId, volker: 13,8=>4,9
        verifyCommand.write(0x00); // bMsgIndex
        verifyCommand.write(new byte[]{0x00, 0x00, 0x00}); // bTeoPrologue
        byte[] verifyApdu = new byte[]{
                SECCOS_CLA_STD, // CLA
                SECCOS_INS_VERIFY, // INS
                0x00, // P1
                (byte) (SECCOS_PWD_TYPE_DF | pwdId), // P2 volker: 01=>81
                0x08, // Lc = 8 bytes in command data
                (byte) 0x25, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF,//volker:0x20=>0x25
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        verifyCommand.write(verifyApdu.length & 0xff); // ulDataLength[0]
        verifyCommand.write(0x00); // ulDataLength[1]
        verifyCommand.write(0x00); // ulDataLength[2]
        verifyCommand.write(0x00); // ulDataLength[3]
        verifyCommand.write(verifyApdu); // abData
        return verifyCommand.toByteArray();
    }
}
