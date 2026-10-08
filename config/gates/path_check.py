"""Gate: path check. Locked. Fails if any changed path matches config/gates/locked-paths.txt.

Usage: python3 config/gates/path_check.py config/gates/locked-paths.txt < changed-paths.txt
Patterns: `**` matches across folders, `*` within one folder name; anything else matches exactly.
Run by .github/workflows/path-check.yml with the PR's changed files, read from main.
"""

import re
import sys


def to_regex(pattern: str) -> re.Pattern[str]:
    out = ""
    i = 0
    while i < len(pattern):
        if pattern.startswith("**", i):
            out += ".*"
            i += 2
        elif pattern[i] == "*":
            out += "[^/]*"
            i += 1
        else:
            out += re.escape(pattern[i])
            i += 1
    return re.compile(out + r"\Z")


def main() -> int:
    with open(sys.argv[1], encoding="utf-8") as f:
        patterns = [line.strip() for line in f if line.strip() and not line.startswith("#")]
    regexes = [(p, to_regex(p)) for p in patterns]
    changed = [line.strip() for line in sys.stdin if line.strip()]
    hits = [(path, p) for path in changed for p, rx in regexes if rx.match(path)]
    for path, p in hits:
        print(f"LOCKED: {path} (matches {p})")
    if hits:
        print("This PR changes locked paths. Only an org admin can merge it (docs/platform-contract.md §7).")
        return 1
    print(f"path check: {len(changed)} changed files, none locked")
    return 0


if __name__ == "__main__":
    sys.exit(main())
