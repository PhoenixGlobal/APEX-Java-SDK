/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2017 - 2019
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package crypto;

import org.bouncycastle.asn1.sec.SECNamedCurves;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.security.interfaces.ECPrivateKey;

/**
 * This class provides methods to derive Mainnet Key values
 * from bouncycastle {@link ECPrivateKey} objects
 * @author Artem Eger
 * @since 20.08.2019
 */
public final class CPXMainnetKey {

    private static final byte [] CPX_PREFIX = Hex.decode("0548");
    private static final byte [] NEO_PREFIX = Hex.decode("17");
    private static final int WIF_PREFIX = 0x80;
    private static final int WIF_POSTFIX = 0x01;
    private static final String SCRIPT_PREFIX = "21";
    private static final String SCRIPT_POSTFIX = "ac";

    public static String getPrivKeyRaw(ECPrivateKey privateKey){
        String privKeyRaw = Hex.toHexString(privateKey.getS().toByteArray());
        if(privKeyRaw.startsWith("00")) privKeyRaw = privKeyRaw.substring(2);
        return privKeyRaw;
    }

    public static String getPrivKeyWIF(ECPrivateKey privateKey) throws Exception {
        final String privKeyRaw = CPXMainnetKey.getPrivKeyRaw(privateKey);
        byte [] wifBytes;
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            out.write(WIF_PREFIX);
            out.write(Hex.decode(privKeyRaw));
            out.write(WIF_POSTFIX);
            wifBytes = out.toByteArray();
        }
        return Base58.encodeChecked(wifBytes);
    }

    public static String getPubKeyCompressed(ECPrivateKey privateKey){
        final X9ECParameters params = SECNamedCurves.getByName(CryptoService.EC_CURVE);
        final ECPoint point = params.getG().multiply(privateKey.getS());
        return  Hex.toHexString(point.getEncoded(true));
    }

    public static String getPubKeyScript(ECPrivateKey privateKey){
        return SCRIPT_PREFIX + CPXMainnetKey.getPubKeyCompressed(privateKey) + SCRIPT_POSTFIX;
    }

    public static String getScriptHash(ECPrivateKey privateKey){
        return Hex.toHexString(CryptoService.getRIPEMD160(Hex.decode(CPXMainnetKey.getPubKeyScript(privateKey))));
    }

    public static String getPublicAddressCPX(ECPrivateKey privateKey) throws Exception {
        return CPXMainnetKey.getPubKey(CPX_PREFIX, privateKey);
    }

    public static String getPublicAddressNEO(ECPrivateKey privateKey) throws Exception {
        return CPXMainnetKey.getPubKey(NEO_PREFIX, privateKey);
    }

    private static String getPubKey(byte [] prefix, ECPrivateKey privateKey) throws Exception{
        final byte [] scriptHash = Hex.decode(CPXMainnetKey.getScriptHash(privateKey));
        byte [] pubKeyBytes;
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()){
            out.write(prefix);
            out.write(scriptHash);
            pubKeyBytes = out.toByteArray();
        }
        return Base58.encodeChecked(pubKeyBytes);
    }

}