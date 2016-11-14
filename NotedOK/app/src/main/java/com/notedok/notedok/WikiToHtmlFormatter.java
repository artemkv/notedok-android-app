package com.notedok.notedok;

import android.text.TextUtils;

public class WikiToHtmlFormatter {
    public static final String QUOTE = "&quot;";

    private String _char = "";
    private String _text = "";
    private int _pos = -1;
    private boolean _listOpened = false;

    public String format(String wiki) {
        // Normalize line ends
        String[] strings = wiki.split("\\r\\n|\\n|\\r", -1);
        StringBuilder sb = new StringBuilder(wiki.length());
        if (strings.length > 0) {
            for (int i = 0; i < strings.length; i++) {
                sb.append(strings[i]);
                sb.append("\n");
            }
            _text = sb.toString();
        } else {
            _text = "\n";
        }

        // Length has to be re-calculated every time, because it can change
        for (_pos = 0; _pos < _text.length(); _pos++) {
            _char = getCharAt(_text, _pos);

            if (_char.equals("*")) {
                tryWrap("*", "<b>", "</b>");
                tryUl("*");
            } else if (_char.equals("_")) {
                tryWrap("_", "<i>", "</i>");
            } else if (_char.equals("-")) {
                tryWrap("--", "<del>", "</del>");
                tryUl("-");
            } else if (_char.equals("+")) {
                tryWrap("++", "<u>", "</u>");
            } else if (_char.equals("^")) {
                tryWrap("^", "<sup>", "</sup>");
            } else if (_char.equals("~")) {
                tryWrap("~", "<sub>", "</sub>");
            } else if (_char.equals("{")) {
                tryEscaped();
                // TODO: tryCode();
            } else if (_char.equals("[")) {
                tryAnchor();
            } else if (_char.equals("!")) {
                // TODO: tryHeader("!");
            } else if (_char.equals("h")) {
                // TODO: tryNumberedHeader();
            }
        }

        return _text;
    }

    private void tryWrap(String formattingString, String openingTag, String closingTag) {
        // Start of the formatting
        if (substr(_text, _pos, formattingString.length()).equals(formattingString)) {
            String nextChar = getCharAt(_text, _pos + formattingString.length());
            // The formatting string is immediately followed by the word
            if (!nextChar.equals(getCharAt(formattingString, 0)) && !isWhitespace(nextChar) && !isNewLine(nextChar)) {
                // There is a closing character
                int closingTagPos = _text.indexOf(formattingString, _pos + 1);
                if (closingTagPos > 0 && _text.indexOf("\n", _pos + 1) > closingTagPos) {
                    // The closing character is before "<" on the same line
                    if (_text.indexOf("<", _pos + 1) == -1 || _text.indexOf("<", _pos + 1) > closingTagPos) {
                        _text = _text.substring(0, _pos) +
                                openingTag +
                                _text.substring(_pos + formattingString.length(), closingTagPos) +
                                closingTag +
                                _text.substring(closingTagPos + formattingString.length());
                        _pos = _pos + openingTag.length() - formattingString.length();
                    }
                }
            }
        }
    }

    private void tryEscaped() {
        String openingEscapeTag = "{" + QUOTE;
        String closingEscapeTag = QUOTE + "}";

        // Start of the escaped formatting
        if (substr(_text, _pos, openingEscapeTag.length()).equals(openingEscapeTag)) {
            // End of escaping formatting
            int closingEscapeTagPos = _text.indexOf(closingEscapeTag, _pos + 1);
            if (closingEscapeTagPos < 0) {
                closingEscapeTagPos = _text.length();
            }
            _text = _text.substring(0, _pos) +
                    _text.substring(_pos + openingEscapeTag.length(), closingEscapeTagPos) +
                    _text.substring(closingEscapeTagPos + closingEscapeTag.length());
            _pos = closingEscapeTagPos - openingEscapeTag.length() - 1; //-1 char back from the removed closing tag
        }
    }

    private void tryAnchor() {
        String nextChar = getCharAt(_text, _pos + 1);

        // The link opening bracket is immediately followed by the link
        if (!nextChar.equals("[") && !isWhitespace(nextChar) && !isNewLine(nextChar)) {
            // There is a closing bracket
            int closingBracketPos = _text.indexOf("]", _pos + 1);
            if (closingBracketPos > 0 && _text.indexOf("\n", _pos + 1) > closingBracketPos) {
                // The closing character is before "<" on the same line
                if (_text.indexOf("<", _pos + 1) == -1 || _text.indexOf("<", _pos + 1) > closingBracketPos) {
                    String href = _text.substring(_pos + 1, closingBracketPos);
                    String link = "<a href='" + href + "' target='_blank'>" + href + "</a>";
                    _text = _text.substring(0, _pos) + link + _text.substring(closingBracketPos + 1);
                    _pos = closingBracketPos + link.length() - href.length() - 2; // 1 removed char, 1 char back from the closing bracket
                }
            }
        }
    }

    private void tryUl(String formattingString) {
        String liTag = formattingString + " ";

        // Start of the text or the line
        if (_pos == 0 || isNewLine(getCharAt(_text, _pos - 1))) {
            // Start of the list item
            if (substr(_text, _pos, liTag.length()).equals(liTag)) {
                // End of escaping formatting
                int eolPos = _text.indexOf("\n", _pos + 1);
                if (eolPos < 0) {
                    eolPos = _text.length();
                }

                String liText = _text.substring(_pos + 2, eolPos);
                String wrappedLiText = "<li>" + liText + "</li>";

                if (!_listOpened) {
                    wrappedLiText = "<ul>" + wrappedLiText;
                    _listOpened = true;
                }

                if (!substr(_text, eolPos + 1, liTag.length()).equals(liTag)) {
                    wrappedLiText = wrappedLiText + "</ul>";
                    _listOpened = false;
                }

                _text = _text.substring(0, _pos) + wrappedLiText + _text.substring(eolPos);
            }
        }
    }

    private String substr(String text, int start, int length) {
        return text.substring(start, Math.min(start + length, text.length()));
    }

    private String getCharAt(String text, int start) {
        return substr(text, start, 1);
    }

    private boolean isWhitespace(String text) {
        return text.matches("\\s");
    }

    private boolean isNewLine(String text) {
        return text.equals("\n");
    }
}
