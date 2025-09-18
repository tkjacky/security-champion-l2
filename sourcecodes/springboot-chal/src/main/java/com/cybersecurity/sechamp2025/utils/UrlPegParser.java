package com.cybersecurity.sechamp2025.utils;

import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;

@BuildParseTree
public class UrlPegParser extends BaseParser<Object> {

    public Rule Url() {
        return FirstOf(
            Sequence(
                Protocol(),
                "://",
                Domain(),
                Optional(Path())
            ),
            JavaScriptUrl()
        );
    }

    public Rule Protocol() {
        return FirstOf("https", "http");
    }

    public Rule JavaScriptUrl() {
        return Sequence(
            "javascript:",
            OneOrMore(ANY)
        );
    }

    public Rule Domain() {
        return Sequence(
            Optional("www."),
            DomainName(),
            ".",
            TopLevelDomain()
        );
    }

    public Rule DomainName() {
        return OneOrMore(
            FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'),
                CharRange('0', '9'),
                '-',
                '_'
            )
        );
    }

    public Rule TopLevelDomain() {
        return FirstOf("com", "org", "net", "edu", "gov");
    }

    public Rule Path() {
        return Sequence(
            "/",
            ZeroOrMore(
                FirstOf(
                    PathSegment(),
                    "/"
                )
            )
        );
    }

    public Rule PathSegment() {
        return OneOrMore(
            FirstOf(
                CharRange('a', 'z'),
                CharRange('A', 'Z'),
                CharRange('0', '9'),
                '-', '_', '.', '@', '%'
            )
        );
    }
}
