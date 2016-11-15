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
                tryCode();
            } else if (_char.equals("[")) {
                tryAnchor();
            } else if (_char.equals("!")) {
                tryHeader("!");
            } else if (_char.equals("h")) {
                tryNumberedHeader();
            }
        }

        return _text;
    }

    private void tryWrap(String formattingString, String openingTag, String closingTag) {
        // Start of the formatting
        if (js_substr(_text, _pos, formattingString.length()).equals(formattingString)) {
            String nextChar = getCharAt(_text, _pos + formattingString.length());
            // The formatting string is immediately followed by the word
            if (!nextChar.equals(getCharAt(formattingString, 0)) && !isWhitespace(nextChar) && !isNewLine(nextChar)) {
                // There is a closing character
                int closingTagPos = _text.indexOf(formattingString, _pos + 1);
                if (closingTagPos > 0 && _text.indexOf("\n", _pos + 1) > closingTagPos) {
                    // The closing character is before "<" on the same line
                    if (_text.indexOf("<", _pos + 1) == -1 || _text.indexOf("<", _pos + 1) > closingTagPos) {
                        _text = js_substring(_text, 0, _pos) +
                                openingTag +
                                js_substring(_text, _pos + formattingString.length(), closingTagPos) +
                                closingTag +
                                js_substring(_text, closingTagPos + formattingString.length());
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
        if (js_substr(_text, _pos, openingEscapeTag.length()).equals(openingEscapeTag)) {
            // End of escaping formatting
            int closingEscapeTagPos = _text.indexOf(closingEscapeTag, _pos + 1);
            if (closingEscapeTagPos < 0) {
                closingEscapeTagPos = _text.length();
            }
            _text = js_substring(_text, 0, _pos) +
                    js_substring(_text, _pos + openingEscapeTag.length(), closingEscapeTagPos) +
                    js_substring(_text, closingEscapeTagPos + closingEscapeTag.length());
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
                    String href = js_substring(_text, _pos + 1, closingBracketPos);
                    String link = "<a href='" + href + "' target='_blank'>" + href + "</a>";
                    _text = js_substring(_text, 0, _pos) + link + js_substring(_text, closingBracketPos + 1);
                    _pos = closingBracketPos + link.length() - href.length() - 2; // 1 removed char, 1 char back from the closing bracket
                }
            }
        }
    }

    private void tryCode() {
        String codeTag = "{code}";

        // Start of the code block
        if (js_substr(_text, _pos, codeTag.length()).equals(codeTag)) {
            // End of the code block
            int closingTagPos = _text.indexOf(codeTag, _pos + 1);
            if (closingTagPos < 0) {
                closingTagPos = _text.length();
            }
            String codeBlock = js_substring(_text, _pos + codeTag.length(), closingTagPos);
            String codeBlockFormatted = "<pre class='codeblock'>" + codeBlock + "</pre>";
            _text = js_substring(_text, 0, _pos) + codeBlockFormatted + js_substring(_text, closingTagPos + codeTag.length());
            _pos = closingTagPos + codeBlockFormatted.length() - codeBlock.length() - codeTag.length() - 1; // 1 char back from the removed closing tag
        }
    }

    private void tryUl(String formattingString) {
        String liTag = formattingString + " ";

        // Start of the text or the line
        if (_pos == 0 || isNewLine(getCharAt(_text, _pos - 1))) {
            // Start of the list item
            if (js_substr(_text, _pos, liTag.length()).equals(liTag)) {
                // End of escaping formatting
                int eolPos = _text.indexOf("\n", _pos + 1);
                if (eolPos < 0) {
                    eolPos = _text.length();
                }

                String liText = js_substring(_text, _pos + 2, eolPos);
                String wrappedLiText = "<li>" + liText + "</li>";

                if (!_listOpened) {
                    wrappedLiText = "<ul>" + wrappedLiText;
                    _listOpened = true;
                }

                if (!js_substr(_text, eolPos + 1, liTag.length()).equals(liTag)) {
                    wrappedLiText = wrappedLiText + "</ul>";
                    _listOpened = false;
                }

                _text = js_substring(_text, 0, _pos) + wrappedLiText + js_substring(_text, eolPos);
            }
        }
    }

    private void tryHeader(String formattingString) {
        String hTag = " ";
        for (int i = 0; i < 6; i++) {
            hTag = formattingString + hTag;

            // Start of the text or the line
            if (_pos == 0 || isNewLine(getCharAt(_text, _pos - 1))) {
                // Start of the header
                if (js_substr(_text, _pos, hTag.length()).equals(hTag)) {
                    // End of escaping formatting
                    int eolPos = _text.indexOf("\n", _pos + 1);
                    if (eolPos < 0) {
                        eolPos = _text.length();
                    }

                    String hText = js_substring(_text, _pos + hTag.length(), eolPos);
                    Integer headerLevel = i + 1;
                    String wrappedHText = "<h" +  headerLevel.toString() + ">" + hText + "</h" + headerLevel.toString() + ">";

                    _text = js_substring(_text, 0, _pos) + wrappedHText + js_substring(_text, eolPos);
                }
            }
        }
    }

    private void tryNumberedHeader() {
        for (int i = 0; i < 6; i++) {
            Integer headerLevel = i + 1;
            String hTag = "h" + headerLevel.toString() + ". ";

            // Start of the text or the line
            if (_pos == 0 || isNewLine(getCharAt(_text, _pos - 1))) {
                // Start of the header
                if (js_substr(_text, _pos, hTag.length()).equals(hTag)) {
                    // End of escaping formatting
                    int eolPos = _text.indexOf("\n", _pos + 1);
                    if (eolPos < 0) {
                        eolPos = _text.length();
                    }

                    String hText = js_substring(_text, _pos + hTag.length(), eolPos);
                    String wrappedHText = "<h" + headerLevel.toString() + ">" + hText + "</h" + headerLevel.toString() + ">";

                    _text = js_substring(_text, 0, _pos) + wrappedHText + js_substring(_text, eolPos);
                }
            }
        }
    }

    private String js_substr(String text, int start, int length) {
        if (start < 0 || start >= text.length()) {
            return "";
        }
        return text.substring(start, Math.min(start + length, text.length()));
    }

    private String js_substring(String text, int start) {
        return js_substring(text, start, text.length());
    }

    private String js_substring(String text, int start, int end) {
        if (start < 0 || start >= text.length()) {
            return "";
        }
        return text.substring(start, Math.min(end, text.length()));
    }

    private String getCharAt(String text, int start) {
        return js_substr(text, start, 1);
    }

    private boolean isWhitespace(String text) {
        return text.matches("\\s");
    }

    private boolean isNewLine(String text) {
        return text.equals("\n");
    }
}
