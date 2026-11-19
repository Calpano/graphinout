# Security Policy

## Supported Versions

Security fixes go into the next release of the current `0.2.x` line.
We do not backport fixes to older releases, so please upgrade to the latest version.

| Version         | Supported          |
| --------------- | ------------------ |
| 0.2.x (latest)  | :white_check_mark: |
| < 0.2.x latest  | :x:                |

## Reporting a Vulnerability

**Please do not report security problems in public issues.**

Report them privately via GitHub:
[Security → Report a vulnerability](https://github.com/Calpano/graphinout/security/advisories/new).

Please include:

- the affected version or commit,
- the module and input format involved (e.g. `reader-graphml`),
- a minimal input file or code snippet that reproduces the problem,
- what happens, and what impact you expect.

## What to Expect

GraphInOut is an open source project maintained on a best-effort basis; there is no guaranteed response time.
We acknowledge a report within 21 days, and will keep you informed while we work on a fix.
Once a fix is released, we publish a GitHub security advisory and credit you, unless you prefer otherwise.

If you need a guaranteed response time or commercial support, [ITMV](https://maxvoelkel.de) can provide it.

## Scope

GraphInOut reads graph files in many formats, which may come from untrusted sources.
Relevant issues include, for example:

- XML external entity (XXE) or entity-expansion attacks in XML-based readers (GraphML, GEXF, GXL, …),
- inputs that cause excessive memory or CPU use far beyond what their size suggests,
- inputs that make a reader access files or the network.

Out of scope: bugs that only produce wrong output without a security impact — please file those as
[regular issues](https://github.com/Calpano/graphinout/issues).
