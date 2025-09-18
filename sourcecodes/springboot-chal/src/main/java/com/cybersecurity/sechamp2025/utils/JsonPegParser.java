package com.cybersecurity.sechamp2025.utils;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

/**
 * PEG Parser for JSON validation with intentional vulnerabilities for educational purposes
 */
@BuildParseTree
public class JsonPegParser extends BaseParser<Object> {

    public Rule JsonValue() {
        return FirstOf(
            JsonObject(),
            JsonArray(),
            JsonString(),
            JsonNumber(),
            JsonBoolean(),
            JsonNull()
        );
    }

    public Rule JsonObject() {
        return Sequence(
            '{',
            Optional(Spacing()),
            Optional(
                Sequence(
                    JsonMember(),
                    ZeroOrMore(
                        Sequence(
                            ',',
                            Optional(Spacing()),
                            JsonMember()
                        )
                    )
                )
            ),
            Optional(Spacing()),
            '}'
        );
    }

    public Rule JsonMember() {
        return Sequence(
            Optional(Spacing()),
            JsonString(),
            Optional(Spacing()),
            ':',
            Optional(Spacing()),
            JsonValue(),
            Optional(Spacing())
        );
    }

    public Rule JsonArray() {
        return Sequence(
            '[',
            Optional(Spacing()),
            Optional(
                Sequence(
                    JsonValue(),
                    ZeroOrMore(
                        Sequence(
                            ',',
                            Optional(Spacing()),
                            JsonValue()
                        )
                    )
                )
            ),
            Optional(Spacing()),
            ']'
        );
    }

    public Rule JsonString() {
        return Sequence(
            '"',
            ZeroOrMore(
                FirstOf(
                    // Vulnerable: allows dangerous property names to pass
                    Sequence('\\', AnyOf("\"\\/bfnrt")),
                    Sequence('\\', 'u', HexDigit(), HexDigit(), HexDigit(), HexDigit()),
                    Sequence(TestNot(AnyOf("\"\\")), ANY)
                )
            ),
            '"'
        );
    }

    public Rule JsonNumber() {
        return Sequence(
            Optional('-'),
            FirstOf(
                '0',
                Sequence(CharRange('1', '9'), ZeroOrMore(Digit()))
            ),
            Optional(
                Sequence(
                    '.',
                    OneOrMore(Digit())
                )
            ),
            Optional(
                Sequence(
                    AnyOf("eE"),
                    Optional(AnyOf("+-")),
                    OneOrMore(Digit())
                )
            )
        );
    }

    public Rule JsonBoolean() {
        return FirstOf("true", "false");
    }

    public Rule JsonNull() {
        return String("null");
    }

    public Rule Digit() {
        return CharRange('0', '9');
    }

    public Rule HexDigit() {
        return FirstOf(
            CharRange('0', '9'),
            CharRange('a', 'f'),
            CharRange('A', 'F')
        );
    }

    public Rule Spacing() {
        return OneOrMore(AnyOf(" \t\n\r"));
    }
}
