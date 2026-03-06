package com.demo.java.xposed.rcs.pdu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GSM {

    private static final String ALPHABET =
            "@\u00A3$\u00A5\u00E8\u00E9\u00F9\u00EC\u00F2\u00C7\n\u00D8\u00F8\r\u00C5\u00E5\u0394_"
                    + "\u03A6\u0393\u039B\u03A9\u03A0\u03A8\u03A3\u0398\u039E\u001B\u00C6\u00E6\u00DF\u00C9"
                    + " !\"#\u00A4%&'()*+,-./0123456789:;<=>?"
                    + "\u00A1ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00C4\u00D6\u00D1\u00DC\u00A7\u00BF"
                    + "abcdefghijklmnopqrstuvwxyz\u00E4\u00F6\u00F1\u00FC\u00E0";

    private static final Map<Integer, Character> ALPHABET_EXT;
    private static final Map<Character, Integer> ALPHABET_EXT_INV;

    static {
        ALPHABET_EXT = new HashMap<>();
        ALPHABET_EXT.put(10, '\f');
        ALPHABET_EXT.put(20, '^');
        ALPHABET_EXT.put(40, '{');
        ALPHABET_EXT.put(41, '}');
        ALPHABET_EXT.put(47, '\\');
        ALPHABET_EXT.put(60, '[');
        ALPHABET_EXT.put(61, '~');
        ALPHABET_EXT.put(62, ']');
        ALPHABET_EXT.put(64, '|');
        ALPHABET_EXT.put(101, '€');

        ALPHABET_EXT_INV = new HashMap<>();
        for (Map.Entry<Integer, Character> entry : ALPHABET_EXT.entrySet()) {
            ALPHABET_EXT_INV.put(entry.getValue(), entry.getKey());
        }
    }

    private static final int CHAR_EXT = 0x1B;

    public static String decode(String data, boolean stripPadding) {
        String reversedHex = reversedOctets(data);
        StringBuilder reversedBits = new StringBuilder();
        for (int i = 0; i < reversedHex.length(); i += 2) {
            String hexByte = reversedHex.substring(i, i + 2);
            String binaryString = String.format("%8s", Integer.toBinaryString(Integer.parseInt(hexByte, 16))).replace(' ', '0');
            reversedBits.append(binaryString);
        }

        int length = reversedBits.length();
        int[] septets = new int[length / 7];
        for (int i = 0; i < septets.length; i++) {
            int startIndex = length - (i + 1) * 7;
            int endIndex = length - i * 7;
            septets[i] = Integer.parseInt(reversedBits.substring(startIndex, endIndex), 2);
        }

        StringBuilder res = new StringBuilder();
        boolean isExtended = false;
        for (int charIndex : septets) {
            if (charIndex == CHAR_EXT) {
                isExtended = true;
                continue;
            }
            if (isExtended) {
                isExtended = false;
                res.append(ALPHABET_EXT.getOrDefault(charIndex, ' '));
            } else {
                res.append(ALPHABET.charAt(charIndex));
            }
        }

        if (stripPadding && septets.length % 8 == 0 && res.charAt(res.length() - 1) == '\r') {
            return res.substring(0, res.length() - 1);
        }
        return res.toString();
    }

    public static String decode(String data) {
        return decode(data, false);
    }

    public static String encode(String data, boolean withPadding) {
        List<Integer> chars = new ArrayList<>();
        for (char ch : data.toCharArray()) {
            int charIndex;
            if (ALPHABET.indexOf(ch) != -1) {
                charIndex = ALPHABET.indexOf(ch);
            } else if (ALPHABET_EXT_INV.containsKey(ch)) {
                chars.add(CHAR_EXT); // add the escape character
                charIndex = ALPHABET_EXT_INV.get(ch);
            } else {
                throw new IllegalArgumentException("Char \"" + ch + "\" cannot be encoded with the GSM 7-bit codec");
            }
            chars.add(charIndex);
        }
        // Calculate the padding of zeros
//        StringBuilder res = new StringBuilder("0".repeat(chars.size() % 8));
        // Handle padding
        if (withPadding) {
            if (chars.size() % 8 == 0 && data.endsWith("\r")) {
                chars.add(ALPHABET.indexOf('\r'));
            }
            if (chars.size() % 8 == 7) {
                chars.add(ALPHABET.indexOf('\r'));
            }
        }

        // Calculate the padding of zeros
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < chars.size() % 8; i++) {
            res.append('0');
        }

        // Convert each character to a 7-bit binary string and reverse the list
        List<String> binaryStrings = new ArrayList<>();
        for (int charIndex : chars) {
            binaryStrings.add(String.format("%7s", Integer.toBinaryString(charIndex)).replace(' ', '0'));
        }
        Collections.reverse(binaryStrings);

        // Concatenate the padding and the reversed list of binary strings
        for (String binaryString : binaryStrings) {
            res.append(binaryString);
        }

        // Convert the binary string to a hexadecimal string
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < res.length(); i += 8) {
            String byteString = res.substring(i, Math.min(i + 8, res.length()));
            hexString.append(String.format("%02X", Integer.parseInt(byteString, 2)));
        }

        // Reverse the octets
        return reversedOctets(hexString.toString());

    }

    public static String encode(String data) {
        return encode(data, false);
    }

    private static String reversedOctets(String data) {
        StringBuilder res = new StringBuilder();
        for (int i = data.length(); i > 0; i -= 2) {
            res.append(data.substring(i - 2, i));
        }
        return res.toString();
    }

    public static void main(String[] args) {
//        System.out.println(GSM.decode("C8F71D14969741F977FD07"));
        System.out.println(GSM.encode("GoogleChat"));
    }
}
