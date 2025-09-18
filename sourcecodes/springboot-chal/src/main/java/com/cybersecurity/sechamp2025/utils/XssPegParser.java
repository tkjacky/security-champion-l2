package com.cybersecurity.sechamp2025.utils;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class XssPegParser extends BaseParser<Object> {

    public Rule SafeInput() {
        return Sequence(
            ZeroOrMore(SafeChar()),
            EOI
        );
    }

    public Rule SafeChar() {
        return FirstOf(
            AlphaNumeric(),
            AnyOf(" ._-!?(),;\"'+<>/=%")
        );
    }

    public Rule AlphaNumeric() {
        return FirstOf(
            CharRange('a', 'z'),
            CharRange('A', 'Z'),
            CharRange('0', '9')
        );
    }

    public Rule DangerousChars() {
        return AnyOf("\"'<>;&{}[]\\");
    }
}
