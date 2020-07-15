package lsy.project.socket.api.util;

import java.io.UnsupportedEncodingException;


/**
 * @author: lisongyue@edenep.net
 * @company: Eden Technology
 * @motto: 代码也可以是一种艺术
 * @version: 1.0
 * @date: 2020/7/15 10:56
 */
public class AESHelper {
    private final static int KEYLENGTH = 32;
    private final static int BLOCKSIZE = 16;
    private final static int ROUNDS = 14;
    private final static int BPOLY = 0x001b;

    public static int[] block1 = new int[256];
    public static int[] block2 = new int[256];
    public static int[] tempbuf = new int[256];

    public static int[] AES_Key_Table = new int[32];


    public static void CalcPowLog() {

        int i = 0, t = 1, k = 0;

        do {
            block1[i & 0x00ff] = (t & 0x00ff);
            tempbuf[t & 0x00ff] = (i & 0x00ff);
            i++;
            k = (t & 0x00ff) & 0x0080;
            if ((k & 0x00ff) != 0) {
                t ^= ((t << 1) ^ BPOLY) & 0x00ff;
            } else {
                t ^= ((t << 1) ^ 0) & 0x00ff;
            }
        }
        while ((t & 0x00ff) != 1);
        block1[255] = block1[0];
    }

    public static void CalcSBox() {
        int i, rot;
        int temp;
        int result;

        // Fill all entries of sBox[].
        i = 0;
        do {
            //Inverse in GF(2^8).
            if ((i & 0x00ff) > 0) {
                temp = block1[255 - tempbuf[i & 0x00ff]];
            } else {
                temp = 0;
            }

            // Affine transformation in GF(2).
            result = (temp & 0x00ff) ^ 0x0063; // Start with adding a vector in GF(2).
            for (rot = 0; rot < 4; rot++) {
                // Rotate left.
                temp = ((temp << 1) | (temp >> 7)) & 0x00ff;

                // Add rotated byte in GF(2).
                result ^= temp;
            }

            // Put result in table.
            block2[i & 0x00ff] = (result & 0x00ff);
        } while ((++i & 0x00ff) != 0);
    }

    public static int[] CycleLeft(int[] row) {
        // Cycle 4 bytes in an array left once.
        int temp = row[0];

        row[0] = row[1] & 0x00ff;
        row[1] = row[2] & 0x00ff;
        row[2] = row[3] & 0x00ff;
        row[3] = temp & 0x00ff;

        return row;
    }

    public static int[] SubBytes(int[] bytes, int count, boolean adFlag) {
        int Cnt = 0;
        do {

            if (adFlag) {
                bytes[Cnt & 0x00ff] = block2[bytes[Cnt & 0x00ff]];
            } else {
                bytes[Cnt & 0x00ff] = tempbuf[bytes[Cnt & 0x00ff]];
            }
            Cnt++;
            //*bytes = sBox[ *bytes ]; // Substitute every byte in state.
            //bytes++;
        } while ((--count) != 0);

        return bytes;
    }

    public static int[][] XORBytes(int[] bytes1, int[] bytes2, int count) {
        int Cnt = 0;
        do {
            bytes1[Cnt] ^= bytes2[Cnt];
            Cnt++;
            //*bytes1 ^= *bytes2; // Add in GF(2), ie. XOR.
            //bytes1++;
            //bytes2++;
        } while ((--count) != 0);

        int bytes[][] = new int[2][];
        bytes[0] = bytes1;
        bytes[1] = bytes2;
        return bytes;
    }


    public static void KeyExpansion(boolean adFlag) {
        int[] temp = new int[4];
        int i, Cnt = 0;
        int[] Rcon = {0x0001, 0x0000, 0x0000, 0x0000}; // Round constant.


        //byte * key = AES_Key_Table;

        // Copy key to start of expanded key.
        i = KEYLENGTH;
        Cnt = 0;
        do {
            block1[Cnt] = AES_Key_Table[Cnt];
            Cnt++;
        } while ((--i) != 0);

        // Prepare last 4 bytes of key in temp.

        Cnt -= 4;
        temp[0] = block1[(Cnt++) & 0x00ff] & 0x00ff;
        temp[1] = block1[(Cnt++) & 0x00ff] & 0x00ff;
        temp[2] = block1[(Cnt++) & 0x00ff] & 0x00ff;
        temp[3] = block1[(Cnt++) & 0x00ff] & 0x00ff;

        // Expand key.
        i = KEYLENGTH;
        while ((i & 0x00ff) < BLOCKSIZE * (ROUNDS + 1)) {
            // Are we at the start of a multiple of the key size?
            if (((i & 0x00ff) % KEYLENGTH) == 0) {
                temp = CycleLeft(temp); // Cycle left once.
                temp = SubBytes(temp, 4, adFlag); // Substitute each byte.
                int[][] mTmp = XORBytes(temp, Rcon, 4); // Add constant in GF(2).
                temp = mTmp[0];
                Rcon = mTmp[1];
                //if(Rcon )
                //*Rcon = (*Rcon << 1) ^ (*Rcon & 0x80 ? BPOLY : 0);
                if ((Rcon[0] & 0x0080) != 0) {
                    Rcon[0] = ((Rcon[0] << 1) ^ BPOLY) & 0x00ff;
                } else {
                    Rcon[0] = ((Rcon[0] << 1) ^ 0) & 0x00ff;
                }

                //Rcon[0] = (Rcon[0] << 1) ^ ((bool)(Rcon[0] & 0x80) ? BPOLY : 0);
            } else {
                if (KEYLENGTH > 24) {
                    if (((i & 0x00ff) % KEYLENGTH) == BLOCKSIZE) {
                        temp = SubBytes(temp, 4, adFlag); // Substitute each byte.
                    }
                }
            }

            // Keysize larger than 24 bytes, ie. larger that 192 bits?

            Cnt = Cnt - KEYLENGTH;
            int[] expandedKey = new int[KEYLENGTH];
            System.arraycopy(block1, Cnt, expandedKey, 0, KEYLENGTH);
            int[][] mTmp1 = XORBytes(temp, expandedKey, 4);
            temp = mTmp1[0];
            expandedKey = mTmp1[1];
            //System.arraycopy(expandedKey,0,block1,Cnt,KEYLENGTH);
            Cnt = Cnt + KEYLENGTH;
            // Copy result to current 4 bytes.
            block1[(Cnt++) & 0x00ff] = temp[0] & 0x00ff;
            block1[(Cnt++) & 0x00ff] = temp[1] & 0x00ff;
            block1[(Cnt++) & 0x00ff] = temp[2] & 0x00ff;
            block1[(Cnt++) & 0x00ff] = temp[3] & 0x00ff;

            i += 4; // Next 4 bytes.
        }
    }


    public static int[][] CopyBytes(int[] to, int[] from, int count) {
        int Cnt = 0;
        do {
            to[Cnt] = from[Cnt] & 0x00ff;
            Cnt++;
            //*to = *from;
            //to++;
            //from++;
        } while ((--count) != 0);

        int bytes[][] = new int[2][];
        bytes[0] = to;
        bytes[1] = from;
        return bytes;
    }

    public static int Multiply(int num, int factor) {
        int mask = 1;
        int result = 0;

        while (mask != 0) {
            // Check bit of factor given by mask.
            if ((mask & factor) != 0) {
                // Add current multiple of num in GF(2).
                result ^= num;
            }

            // Shift mask to indicate next bit.
            mask <<= 1;

            // Double num.
            if ((num & 0x0080) != 0) {
                num = ((num << 1) ^ BPOLY) & 0x00fff;
            } else {
                num = ((num << 1) ^ 0) & 0x00ff;
            }
            //num = (num << 1) ^ (num & 0x80 ? BPOLY : 0);
        }

        return result;
    }

    public static int[][] DotProduct(int[] vector1, int[] vector2) {
        int result = 0;


        result ^= Multiply(vector1[0], vector2[0]); //  *vector1++, *vector2++ );
        result ^= Multiply(vector1[1], vector2[1]);
        result ^= Multiply(vector1[2], vector2[2]);
        result ^= Multiply(vector1[3], vector2[3]);
        result = result & 0x00ff;
        int bytes[][] = new int[3][];
        int[] mResult = new int[1];
        mResult[0] = result;

        bytes[0] = mResult;
        bytes[1] = vector1;
        bytes[2] = vector2;

        return bytes;
    }

    public static int[] MixColumn(int[] column) {
        int[] row = {0x02, 0x03, 0x01, 0x01, 0x02, 0x03, 0x01, 0x01};
        // Prepare first row of matrix twice, to eliminate need for cycling.

        int[] result = new int[4];

        // Take dot products of each matrix row and the column vector.
        int[] kk = new int[8];
        System.arraycopy(row, 0, kk, 0, kk.length);
        int[][] mDotProduct0 = DotProduct(kk, column);
        result[0] = mDotProduct0[0][0];
        kk = mDotProduct0[1];
        column = mDotProduct0[2];
        System.arraycopy(kk, 0, row, 0, kk.length);

        kk = new int[5];
        System.arraycopy(row, 3, kk, 0, kk.length);
        int[][] mDotProduct1 = DotProduct(kk, column);
        result[1] = mDotProduct1[0][0];
        kk = mDotProduct1[1];
        column = mDotProduct1[2];
        System.arraycopy(kk, 0, row, 3, kk.length);

        kk = new int[6];
        System.arraycopy(row, 2, kk, 0, kk.length);
        int[][] mDotProduct2 = DotProduct(kk, column);
        result[2] = mDotProduct2[0][0];
        kk = mDotProduct2[1];
        column = mDotProduct2[2];
        System.arraycopy(kk, 0, row, 2, kk.length);

        kk = new int[7];
        System.arraycopy(row, 1, kk, 0, kk.length);
        int[][] mDotProduct3 = DotProduct(kk, column);
        result[3] = mDotProduct3[0][0];
        kk = mDotProduct3[1];
        column = mDotProduct3[2];
        System.arraycopy(kk, 0, row, 1, kk.length);

        // Copy temporary result to original column.
        column[0] = result[0] & 0x00ff;
        column[1] = result[1] & 0x00ff;
        column[2] = result[2] & 0x00ff;
        column[3] = result[3] & 0x00ff;

        return column;
    }

    public static int[] MixColumns(int[] state) {
        int[] kk = new int[state.length];
        System.arraycopy(state, 0, kk, 0, kk.length);
        kk = MixColumn(kk);
        System.arraycopy(kk, 0, state, 0, kk.length);

        kk = new int[state.length - 4];
        System.arraycopy(state, 4, kk, 0, kk.length);
        kk = MixColumn(kk);
        System.arraycopy(kk, 0, state, 4, kk.length);

        kk = new int[state.length - 8];
        System.arraycopy(state, 8, kk, 0, kk.length);
        kk = MixColumn(kk);
        System.arraycopy(kk, 0, state, 8, kk.length);

        kk = new int[state.length - 12];
        System.arraycopy(state, 12, kk, 0, kk.length);
        kk = MixColumn(kk);
        System.arraycopy(kk, 0, state, 12, kk.length);

        return state;
    }

    public static int[] ShiftRows(int[] state) {
        int temp;

        // Note: State is arranged column by column.

        // Cycle second row left one time.
        temp = state[1 + 0 * 4] & 0x00ff;
        state[1 + 0 * 4] = state[1 + 1 * 4] & 0x00ff;
        state[1 + 1 * 4] = state[1 + 2 * 4] & 0x00ff;
        state[1 + 2 * 4] = state[1 + 3 * 4] & 0x00ff;
        state[1 + 3 * 4] = temp & 0x00ff;

        // Cycle third row left two times.
        temp = state[2 + 0 * 4] & 0x00ff;
        state[2 + 0 * 4] = state[2 + 2 * 4] & 0x00ff;
        state[2 + 2 * 4] = temp & 0x00ff;
        temp = state[2 + 1 * 4] & 0x00ff;
        state[2 + 1 * 4] = state[2 + 3 * 4] & 0x00ff;
        state[2 + 3 * 4] = temp & 0x00ff;

        // Cycle fourth row left three times, ie. right once.
        temp = state[3 + 3 * 4] & 0x00ff;
        state[3 + 3 * 4] = state[3 + 2 * 4] & 0x00ff;
        state[3 + 2 * 4] = state[3 + 1 * 4] & 0x00ff;
        state[3 + 1 * 4] = state[3 + 0 * 4] & 0x00ff;
        state[3 + 0 * 4] = temp & 0x00ff;

        return state;
    }

    public static int[][] Cipher(int[] block, int[] expandedKey) {
        int[] buffer = new int[expandedKey.length];
        int round = (ROUNDS - 1);// 10
        int Cnt = 0;


        int[][] mTmp1 = XORBytes(block, expandedKey, 16);
        block = mTmp1[0];
        expandedKey = mTmp1[1];
        //expandedKey += BLOCKSIZE;
        Cnt += BLOCKSIZE;

        do {
            block = SubBytes(block, 16, true);
            block = ShiftRows(block);
            block = MixColumns(block);
            buffer = new int[BLOCKSIZE];
            System.arraycopy(expandedKey, Cnt, buffer, 0, buffer.length);
            int[][] mTmp2 = XORBytes(block, buffer, 16);
            block = mTmp2[0];
            buffer = mTmp2[1];
            //expandedKey += BLOCKSIZE;
            Cnt += BLOCKSIZE;
        } while ((--round) != 0);

        block = SubBytes(block, 16, true);
        block = ShiftRows(block);
        buffer = new int[BLOCKSIZE];
        System.arraycopy(expandedKey, Cnt, buffer, 0, buffer.length);
        int[][] mTmp3 = XORBytes(block, buffer, 16);
        block = mTmp3[0];
        buffer = mTmp3[1];

        int bytes[][] = new int[2][];
        bytes[0] = block;
        bytes[1] = expandedKey;
        return bytes;
    }

    public static void aesEncInit() {
        CalcPowLog();

        CalcSBox();

        KeyExpansion(true);
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static String aesEncrypt(String stringData, String key) {

        int[] chainBlock = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
        byte[] dataByte = stringData.getBytes();
        int[] mBuffer = new int[dataByte.length];
        for (int i = 0; i < (dataByte.length); i++) {
            int v = dataByte[i] & 0xFF;
            mBuffer[i] = v;
        }
        AES_Key_Table = hexString2Int(key);
        aesEncInit();
        int[][] mTmp = XORBytes(mBuffer, chainBlock, BLOCKSIZE);
        mBuffer = mTmp[0];
        chainBlock = mTmp[1];
        int[][] mTmp1 = Cipher(mBuffer, block1);
        mBuffer = mTmp1[0];
        block1 = mTmp1[1];

        int[][] mTmp2 = CopyBytes(chainBlock, mBuffer, BLOCKSIZE);
        chainBlock = mTmp2[0];
        mBuffer = mTmp2[1];

        byte bytes[] = new byte[mBuffer.length];
        int p = 0;
        for (int mbyte : mBuffer) {
            bytes[p++] = (byte) mbyte;
        }
        String strRet = bytesToHexString(bytes);
        if (strRet != null) {
            strRet.toUpperCase();
        }
        return strRet;
    }

    public static byte[] aesEncryptGetBytes(byte[] data, String key) {

        int[] chainBlock = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
        int[] mBuffer = new int[data.length];
        for (int i = 0; i < (data.length); i++) {
            int v = data[i] & 0xFF;
            mBuffer[i] = v;
        }
        AES_Key_Table = hexString2Int(key);
        aesEncInit();
        int[][] mTmp = XORBytes(mBuffer, chainBlock, BLOCKSIZE);
        mBuffer = mTmp[0];
        chainBlock = mTmp[1];
        int[][] mTmp1 = Cipher(mBuffer, block1);
        mBuffer = mTmp1[0];
        block1 = mTmp1[1];

        int[][] mTmp2 = CopyBytes(chainBlock, mBuffer, BLOCKSIZE);
        chainBlock = mTmp2[0];
        mBuffer = mTmp2[1];

        byte bytes[] = new byte[mBuffer.length];
        int p = 0;
        for (int mbyte : mBuffer) {
            bytes[p++] = (byte) mbyte;
        }

        return bytes;
    }

    //------------------------------------------------------------------------------------------

    public static void CalcPowLog1() {

        int i = 0, k = 0;
        int t = 1;

        do {
            block1[i & 0x00ff] = t & 0x00ff;
            block2[t & 0x00ff] = i & 0x00ff;
            i++;
            k = (t & 0x0080) & 0x00ff;
            if (k != 0) {
                t ^= ((t << 1) ^ BPOLY) & 0x00ff;
            } else {
                t ^= ((t << 1) ^ 0) & 0x00ff;
            }

        }
        while ((t & 0x00ff) != 1);
        block1[255] = block1[0];
    }

    public static void CalcSBox1() {
        int i, rot;
        int temp;
        int result;

        // Fill all entries of sBox[].
        i = 0;
        do {
            //Inverse in GF(2^8).
            if ((i & 0x00ff) > 0) {
                temp = block1[255 - block2[i & 0x00ff]] & 0x00ff;
            } else {
                temp = 0;
            }

            // Affine transformation in GF(2).
            result = (temp ^ 0x0063) & 0x00ff; // Start with adding a vector in GF(2).
            for (rot = 0; rot < 4; rot++) {
                // Rotate left.
                temp = ((temp << 1) | (temp >> 7)) & 0x00ff;

                // Add rotated byte in GF(2).
                result ^= temp;
            }

            // Put result in table.
            tempbuf[i & 0x00ff] = result & 0x00ff;
        } while ((++i & 0x00ff) != 0);
    }

    public static void CalcSBoxInv() {
        int i = 0;
        int j = 0;

        // Iterate through all elements in sBoxInv using  i.
        do {
            // Search through sBox using j.
            do {
                // Check if current j is the inverse of current i.
                if (tempbuf[j & 0x00ff] == (i & 0x00ff)) {
                    // If so, set sBoxInc and indicate search finished.
                    block2[i & 0x00ff] = j & 0x00ff;
                    j = 255;
                }
            } while ((++j & 0x00ff) != 0);
        } while ((++i & 0x00ff) != 0);
    }

    public static int[][] InvSubBytesAndXOR(int[] bytes, int[] key, int count) {
        int bCnt = 0;
        int kCnt = 0;
        do {
            // *bytes = sBoxInv[ *bytes ] ^ *key; // Inverse substitute every byte in state and add key.
            bytes[bCnt & 0x00ff] = ((block2[bytes[bCnt & 0x00ff]]) ^ (key[kCnt & 0x00ff])) & 0x00ff; // Use block2 directly. Increases speed.
            bCnt++;
            kCnt++;
        } while ((--count) != 0);

        int mBytes[][] = new int[2][];
        mBytes[0] = bytes;
        mBytes[1] = key;
        return mBytes;
    }

    public static int[] InvShiftRows(int[] state) {
        int temp;
        temp = state[1 + 3 * 4] & 0x00ff;
        state[1 + 3 * 4] = state[1 + 2 * 4] & 0x00ff;
        state[1 + 2 * 4] = state[1 + 1 * 4] & 0x00ff;
        state[1 + 1 * 4] = state[1 + 0 * 4] & 0x00ff;
        state[1 + 0 * 4] = temp & 0x00ff;

        // Cycle third row right two times.
        temp = state[2 + 0 * 4] & 0x00ff;
        state[2 + 0 * 4] = state[2 + 2 * 4] & 0x00ff;
        state[2 + 2 * 4] = temp & 0x00ff;
        temp = state[2 + 1 * 4] & 0x00ff;
        state[2 + 1 * 4] = state[2 + 3 * 4] & 0x00ff;
        state[2 + 3 * 4] = temp & 0x00ff;

        // Cycle fourth row right three times, ie. left once.
        temp = state[3 + 0 * 4] & 0x00ff;
        state[3 + 0 * 4] = state[3 + 1 * 4] & 0x00ff;
        state[3 + 1 * 4] = state[3 + 2 * 4] & 0x00ff;
        state[3 + 2 * 4] = state[3 + 3 * 4] & 0x00ff;
        state[3 + 3 * 4] = temp & 0x00ff;

        return state;
    }

    public static int[] InvMixColumn(int[] column) {
        int r0, r1, r2, r3, k;

        r0 = (column[1] ^ column[2] ^ column[3]) & 0x00ff;
        r1 = (column[0] ^ column[2] ^ column[3]) & 0x00ff;
        r2 = (column[0] ^ column[1] ^ column[3]) & 0x00ff;
        r3 = (column[0] ^ column[1] ^ column[2]) & 0x00ff;

        for (int i = 0; i < 4; i++) {
            k = (column[i] & 0x0080);
            if (k != 0) {
                column[i] = ((column[i] << 1) ^ BPOLY) & 0x00ff;
            } else {
                column[i] = ((column[i] << 1) ^ 0) & 0x00ff;
            }
        }

        r0 ^= (column[0] ^ column[1]);
        r1 ^= (column[1] ^ column[2]);
        r2 ^= (column[2] ^ column[3]);
        r3 ^= (column[0] ^ column[3]);

        for (int i = 0; i < 4; i++) {
            k = (column[i] & 0x0080) & 0x00ff;
            if (k != 0) {
                column[i] = ((column[i] << 1) ^ BPOLY) & 0x00ff;
            } else {
                column[i] = ((column[i] << 1) ^ 0) & 0x00ff;
            }
        }


        r0 ^= (column[0] ^ column[2]);
        r1 ^= (column[1] ^ column[3]);
        r2 ^= (column[0] ^ column[2]);
        r3 ^= (column[1] ^ column[3]);

        for (int i = 0; i < 4; i++) {
            k = (column[i] & 0x0080) & 0x00ff;
            if (k != 0) {
                column[i] = ((column[i] << 1) ^ BPOLY) & 0x00ff;
            } else {
                column[i] = ((column[i] << 1) ^ 0) & 0x00ff;
            }
        }
        //column[0] = (column[0] << 1) ^ (column[0] & 0x80 ? BPOLY : 0);
        //column[1] = (column[1] << 1) ^ (column[1] & 0x80 ? BPOLY : 0);
        //column[2] = (column[2] << 1) ^ (column[2] & 0x80 ? BPOLY : 0);
        //column[3] = (column[3] << 1) ^ (column[3] & 0x80 ? BPOLY : 0);

        column[0] ^= (column[1] ^ column[2] ^ column[3]);
        r0 ^= column[0];
        r1 ^= column[0];
        r2 ^= column[0];
        r3 ^= column[0];

        column[0] = r0 & 0x00ff;
        column[1] = r1 & 0x00ff;
        column[2] = r2 & 0x00ff;
        column[3] = r3 & 0x00ff;

        return column;
    }

    public static int[] InvMixColumns(int[] state) {
        int[] kk = new int[state.length];
        System.arraycopy(state, 0, kk, 0, kk.length);
        kk = InvMixColumn(kk);
        System.arraycopy(kk, 0, state, 0, kk.length);

        kk = new int[state.length - 4];
        System.arraycopy(state, 4, kk, 0, kk.length);
        kk = InvMixColumn(kk);
        System.arraycopy(kk, 0, state, 4, kk.length);

        kk = new int[state.length - 8];
        System.arraycopy(state, 8, kk, 0, kk.length);
        kk = InvMixColumn(kk);
        System.arraycopy(kk, 0, state, 8, kk.length);

        kk = new int[state.length - 12];
        System.arraycopy(state, 12, kk, 0, kk.length);
        kk = InvMixColumn(kk);
        System.arraycopy(kk, 0, state, 12, kk.length);

        //MixColumn( state + 0*4 );
        //MixColumn( state + 1*4 );
        //MixColumn( state + 2*4 );
        //MixColumn( state + 3*4 );

        return state;
    }


    public static int[][] InvCipher(int[] block, int[] expandedKey) {
        int[] buffer = new int[16];
        int round = (ROUNDS - 1);// 10
        int Cnt = 0;

        Cnt += BLOCKSIZE * ROUNDS;
        System.arraycopy(expandedKey, Cnt, buffer, 0, 16);
        int[][] mTmp = XORBytes(block, buffer, 16);
        block = mTmp[0];
        buffer = mTmp[1];
        Cnt -= BLOCKSIZE;

        do {
            block = InvShiftRows(block);
            System.arraycopy(expandedKey, Cnt, buffer, 0, 16);
            int[][] mTmp1 = InvSubBytesAndXOR(block, buffer, 16);
            block = mTmp1[0];
            buffer = mTmp1[1];
            Cnt -= BLOCKSIZE;
            //expandedKey -= BLOCKSIZE;
            block = InvMixColumns(block);
        } while ((--round) != 0);

        block = InvShiftRows(block);
        System.arraycopy(expandedKey, Cnt, buffer, 0, 16);
        int[][] mTmp2 = InvSubBytesAndXOR(block, buffer, 16);
        block = mTmp2[0];
        buffer = mTmp2[1];

        int bytes[][] = new int[2][];
        bytes[0] = block;
        bytes[1] = expandedKey;

        return bytes;
    }


    public static void aesDecInit() {
        CalcPowLog1();

        CalcSBox1();

        KeyExpansion(false);

        CalcSBoxInv();
    }

    /*public static byte[] strToHexByte(String hexString)
    {
        hexString = hexString.replace(" ", "");
        if ((hexString.length() % 2) != 0)
            hexString += " ";
        byte[] returnBytes = new byte[hexString.length() / 2];
        for (int i = 0; i < returnBytes.length; i++)
            returnBytes[i] = Convert.ToByte(hexString.substring(i * 2, 2), 16);hexString.t
        return returnBytes;
    }*/

    private static int[] hexString2Int(String hexString) {
        if ((null == hexString) || (hexString.isEmpty())) {
            return null;
        }
        int l = hexString.length() / 2;
        int[] ret = new int[l];
        for (int i = 0; i < l; i++) {
            ret[i] = Integer.valueOf(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        return ret;
    }

    public static String aesDecrypt(String hexStringData, String hexStringKey) {
        if ((null == hexStringData) || (hexStringData.isEmpty()) || (null == hexStringKey) || (hexStringKey.isEmpty())) {
            return null;
        }

        int i;
        int[] temp = new int[BLOCKSIZE];
        int[] temp1 = new int[BLOCKSIZE];

        int[] chainBlock = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
        int[] buffer = hexString2Int(hexStringData);
        AES_Key_Table = hexString2Int(hexStringKey);

        aesDecInit();

        int[][] chainBlockArry1 = CopyBytes(temp, buffer, BLOCKSIZE);
        temp = chainBlockArry1[0];
        buffer = chainBlockArry1[1];
        int[][] InvCipherArry = InvCipher(buffer, block1);
        buffer = InvCipherArry[0];
        block1 = InvCipherArry[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        int[][] XORBytesArry = XORBytes(buffer, chainBlock, BLOCKSIZE);
        buffer = XORBytesArry[0];
        chainBlock = XORBytesArry[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        int[][] chainBlockArry2 = CopyBytes(chainBlock, temp, BLOCKSIZE);
        chainBlock = chainBlockArry2[0];
        temp = chainBlockArry2[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        byte[] byteBuffer = new byte[buffer.length];
        for (int j = 0; j < buffer.length; j++) {
            byteBuffer[j] = (byte) buffer[j];
        }

        String strRet = null;
        try {
            strRet = new String(byteBuffer, "Gb2312");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strRet;
    }


    public static byte[] aesDecryptGetBytes(String hexStringData, String hexStringKey) {
        if ((null == hexStringData) || (hexStringData.isEmpty()) || (null == hexStringKey) || (hexStringKey.isEmpty())) {
            return null;
        }

        int i;
        int[] temp = new int[BLOCKSIZE];
        int[] temp1 = new int[BLOCKSIZE];

        int[] chainBlock = {0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000, 0x0000};
        int[] buffer = hexString2Int(hexStringData);
        AES_Key_Table = hexString2Int(hexStringKey);

        aesDecInit();

        int[][] chainBlockArry1 = CopyBytes(temp, buffer, BLOCKSIZE);
        temp = chainBlockArry1[0];
        buffer = chainBlockArry1[1];
        int[][] InvCipherArry = InvCipher(buffer, block1);
        buffer = InvCipherArry[0];
        block1 = InvCipherArry[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        int[][] XORBytesArry = XORBytes(buffer, chainBlock, BLOCKSIZE);
        buffer = XORBytesArry[0];
        chainBlock = XORBytesArry[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        int[][] chainBlockArry2 = CopyBytes(chainBlock, temp, BLOCKSIZE);
        chainBlock = chainBlockArry2[0];
        temp = chainBlockArry2[1];
        for (i = 0; i < BLOCKSIZE; i++) {
            temp1[i] = buffer[i];
        }

        byte[] byteBuffer = new byte[buffer.length];
        for (int j = 0; j < buffer.length; j++) {
            byteBuffer[j] = (byte) buffer[j];
        }

        return byteBuffer;
    }
}
