/**
 * Created by juanc on 16-May-16.
 */
public enum ParseState {
    TEXT, QUOTED, DECIDING, RFC2047_CHARSET, RFC2047_ENCODING, RFC2047_ENCODED_TEXT , RFC2047_DECIDING, LWS,
    SUBJECT, NONE, IMAGE
}
