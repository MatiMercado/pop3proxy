import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by juanc on 19-May-16.
 */
public class backup {

    private BufferedReader reader;
    private StringBuilder response;
    private final Integer BUFFER_SIZE = 255;


    private void transformSubject(Encoding encoding) throws IOException {
        String currentLine;

        while ((currentLine = reader.readLine()) != null && !currentLine.substring(0, 7).toLowerCase().startsWith("subject:")) {
            response.append(currentLine);
        }

        if (currentLine == null)
            return;

        response.append("subject:");
        StringBuilder subject = new StringBuilder();

        subject.append(currentLine.substring(8)).append('\n');

        switch (encoding) {
            case NONE:
                while ((currentLine = reader.readLine()).startsWith(" ")) {
                    subject.append(currentLine).append('\n');
                }
                break;
            case QUOTEDPRINTABLE:
                break;
            default:
                break;
        }
        String singleLineSubject = replaceL33t(subject.toString(), encoding);

        if (encoding == Encoding.BASE64)
            response.append(currentLine);
    }

    private String replaceL33t(String subject, Encoding encoding) {
        char[] subjectArray;
        boolean quoted = false, newLine = false;
        char charsToIgnore = 0;
        if (encoding == Encoding.QUOTEDPRINTABLE)
            quoted = true;

        Map<Character, Character> charReplaceMap = new HashMap<>();
        charReplaceMap.put('a', '4');
        charReplaceMap.put('A', '4');
        charReplaceMap.put('e', '3');
        charReplaceMap.put('E', '3');
        charReplaceMap.put('o', '0');
        charReplaceMap.put('O', '0');
        charReplaceMap.put('c', '<');


        Character c, aux;
        if (encoding != Encoding.BASE64) {
            subjectArray = subject.toCharArray();
            Pattern LWSPattern = Pattern.compile("\\s");
            ParseState state = ParseState.TEXT;
            ParseState prevState = ParseState.TEXT;
            for (int i = 0; i < subjectArray.length; i++) {
                c = subjectArray[i];
                switch (state) {
                    case DECIDING:
                        if (c == '?') {
                            state = ParseState.RFC2047_CHARSET;
                        } else if (c != '=') {
                            if (quoted) {
                                state = ParseState.QUOTED;
                                prevState = ParseState.TEXT;
                            } else {
                                aux = charReplaceMap.get(c);
                                subjectArray[i] = (aux == null ? c : aux);
                                state = ParseState.TEXT;
                            }
                        }
                        break;
                    case QUOTED:
                        state = prevState;
                        break;
                    case TEXT:
                        if (c == '=') {
                            state = ParseState.DECIDING;
                        } else {
                            aux = charReplaceMap.get(c);
                            subjectArray[i] = (aux == null ? c : aux);
                        }
                        break;
                    case RFC2047_CHARSET:
                        if (c == '?')
                            state = ParseState.RFC2047_ENCODING;
                        break;
                    case RFC2047_ENCODING:
                        if (c == '?')
                            state = ParseState.RFC2047_ENCODED_TEXT;
                        break;
                    case RFC2047_ENCODED_TEXT:
                        if (c == '=') {
                            state = ParseState.RFC2047_DECIDING;
                        } else {
                            aux = charReplaceMap.get(c);
                            subjectArray[i] = (aux == null ? c : aux);
                        }
                        break;
                    case RFC2047_DECIDING:
                        if (c == '?') {
                            state = ParseState.LWS;
                        } else if (c != '=') {
                            if (quoted) {
                                state = ParseState.QUOTED;
                                prevState = ParseState.RFC2047_ENCODED_TEXT;
                            } else {
                                aux = charReplaceMap.get(c);
                                subjectArray[i] = (aux == null ? c : aux);
                                state = ParseState.TEXT;
                            }
                        }
                        break;
                    case LWS:
                        if (!LWSPattern.matcher(c.toString()).matches())
                            state = ParseState.RFC2047_ENCODED_TEXT;
                        break;
                }
            }


        } else {//BASE64
            subjectArray = new String(Base64.decode(subject)).toCharArray();
            for (int i = 0; i < subjectArray.length; i++) {
                c = subjectArray[i];
                aux = charReplaceMap.get(c);
                subjectArray[i] = (aux == null ? c : aux);
            }
            return new String(Base64.encode(new String(subjectArray).getBytes()));
        }
        return new String(subjectArray);
    }

      /*
        String currentLine, subject;
        while ((currentLine = reader.readLine()) != null && !currentLine.startsWith("content-transfer-encoding:")) {
            response.append(currentLine).append('\n');
        }
        if (currentLine.contains("BASE64")) {
            encoding = Encoding.BASE64;
        } else if (currentLine.contains("QUOTED-PRINTABLE")) {
            encoding = Encoding.QUOTEDPRINTABLE;
        }
        transformSubject(encoding);
        return response.toString();
    */
}
