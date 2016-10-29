import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by juanc on 15-May-16.
 */
public class SubjectToL33t {

    private StringBuilder response;
    private StringBuilder input;
    private ParseState state;
    private boolean RFC2047;

    private Map<Character, Character> charReplaceMap = new HashMap<>();

    public SubjectToL33t() {
        response = new StringBuilder();
        input = new StringBuilder();
        state = ParseState.NONE;
        RFC2047 = false;
        InputStream stream = null;
/*        try {
            final String FORMAT = "PNG";
            String imageString = " ";
            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageString.getBytes());
            BufferedImage bufferedImage = ImageIO.read(inputStream);

            AffineTransform transform = new AffineTransform();
            transform.rotate(Math.PI / 4, bufferedImage.getWidth()/2, bufferedImage.getHeight()/2);
            AffineTransformOp op = new AffineTransformOp(transform, AffineTransformOp.TYPE_BILINEAR);
            bufferedImage = op.filter(bufferedImage, null);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, FORMAT, outputStream);
            String rotatedImageString = String.valueOf(outputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }
*/


        charReplaceMap.put('a', '4');
        charReplaceMap.put('A', '4');
        charReplaceMap.put('e', '3');
        charReplaceMap.put('E', '3');
        charReplaceMap.put('o', '0');
        charReplaceMap.put('O', '0');
        charReplaceMap.put('c', '<');

    }


    public void addBytes(Connection con, Byte[] bytes) {

        String newInput = String.valueOf(bytes);
        input.append(newInput);

        String[] lines = input.toString().split("\r\n");
        for (int index = 1; index < lines.length; index++) {
            try {
                parse(lines[index]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void parse(String currentLine) throws IOException {
        Encoding encoding = Encoding.NONE;

        switch (state) {
            case NONE:
                if (!currentLine.substring(0, 7).toLowerCase().startsWith("subject:")) {
                    state = ParseState.SUBJECT;
                    response.append(currentLine);//TODO commit line
                    int index = 9;
                    if (currentLine.charAt(9) == '=') {
                        RFC2047 = true;
                    }

                    char[] currentLineArray;
                    currentLineArray = currentLine.toCharArray();

                    if (RFC2047) {
                        int startOfCharset, endOfCharset;
                        while (currentLineArray[index++] != '?') ;
                        startOfCharset = index;
                        while (currentLineArray[index++] != '?') ;
                        endOfCharset = index - 2;
                        if (Character.toUpperCase(currentLineArray[index]) == 'B') {
                            encoding = Encoding.BASE64;
                        } else {
                            encoding = Encoding.QUOTEDPRINTABLE;
                        }
                        index += 2;
                        char c;
                        Character r;

                        if (encoding == Encoding.BASE64) {
                            String charset = String.valueOf(Arrays.copyOfRange(currentLineArray, startOfCharset, endOfCharset));
                            String body = currentLine.substring(index + 1, currentLine.length() - 2);
                            String prefix = currentLine.substring(0, index);
                            char[] auxArray = String.valueOf(Base64.decode(body)).toCharArray();
                            while ((c = auxArray[index]) != '?') {
                                r = charReplaceMap.get(c);
                                if (r != null)
                                    auxArray[index] = r;
                            }
                            String toCommit = String.format("%s%s?=", prefix, String.valueOf(Base64.encode(String.valueOf(auxArray).getBytes(charset))));
                            //TODO commit
                        } else {//Quoted printable
                            int charsToIgnore = 0;
                            while ((c = currentLineArray[index]) != '?') {
                                if (c == '=')
                                    charsToIgnore = 2;
                                else {
                                    if (charsToIgnore == 0) {
                                        r = charReplaceMap.get(c);
                                        if (r != null)
                                            currentLineArray[index] = r;
                                    } else
                                        charsToIgnore--;
                                }
                                index++;
                            }
                            //TODO commit currentLineArray
                        }
                    }

                }

        }


    }


    public void parseConnection(SelectionKey key, byte[] bytes) throws ClosedChannelException {
        Connection connection = (Connection) key.attachment();
        //key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ); //TODO: Esta linea es la que hace que haya writes de server de 0, tendria que ver el remaining
        //Como vine del read, la key en este momento deberia estar en 0, la voy a dejar asi.

        connection.getOtherSide().getOutputByteBuffer().put(bytes);
        connection.getOtherSide().getChannel().register(key.selector(), SelectionKey.OP_READ |
                SelectionKey.OP_WRITE, connection.getOtherSide());

    }
}
