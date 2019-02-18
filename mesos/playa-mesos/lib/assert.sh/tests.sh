#!/bin/bash

. assert.sh

assert "echo"                           # no output expected
assert "echo foo" "foo"                 # output expected
assert "cat" "bar" "bar"                # output expected if input's given
assert_raises "true" 0 ""               # status code expected
assert_raises "exit 127" 127 ""         # status code expected
assert "head -1 < $0" "#!/bin/bash"     # redirections
assert "seq 2" "1\n2"                   # multi-line output expected
assert_raises 'read a; exit $a' 42 "42" # variables still work
assert "echo 1;
echo 2      # ^" "1\n2"                 # semicolon required!
assert_end demo

_clean() {
    _assert_reset # reset state
    DEBUG= STOP= INVARIANT=1 DISCOVERONLY= # reset flags
    eval $* # read new flags
}

# clean output
assert "_clean; assert true; assert_end" \
"all 1 tests passed."
# error reports on failure
assert "_clean; assert 'seq 1'; assert_end" \
'test #1 "seq 1" failed:\n\texpected nothing\n\tgot "1"\n1 of 1 tests failed.'
assert "_clean; assert true '1'; assert_end" \
'test #1 "true" failed:\n\texpected "1"\n\tgot nothing\n1 of 1 tests failed.'
# debug output (-v)
assert "_clean DEBUG=1; assert true; assert_end" \
".\nall 1 tests passed."
assert "_clean DEBUG=1; assert_raises false; assert_end" \
'X\ntest #1 "false" failed:\n\tprogram terminated with code 1 instead of 0
1 of 1 tests failed.'
# collect tests only (-d)
assert "_clean DISCOVERONLY=1; assert true; assert false; assert_end" \
"collected 2 tests."
# stop immediately on failure (-x)
assert "_clean STOP=1 assert_raises false; assert_end" \
'test #1 "false" failed:\n\tprogram terminated with code 1 instead of 0'
# runtime statistics (omission of -i)
assert_raises "_clean INVARIANT=;
assert_end | egrep 'all 0 tests passed in ([0-9]|[0-9].[0-9]{3})s'"
# assert_end exit code is the number of failures
assert_raises "_clean; assert_raises false; assert_raises false; assert_end" 2
# stderr should NOT leak if ignored
assert "_clean; assert less" ""
# stderr should be redirectable though
assert '_clean; assert "less 2>&1" "Missing filename (\"less --help\" for help)"'
# bash failures behave just like stderr
assert "_clean; assert ___invalid" ""
# test suites can be nested and settings are inherited
# (ie. we don't need to invoke the inner suite with the very same options,
# namely --invariant)
assert "_clean; bash -c '
. assert.sh;
assert_raises true; assert_end outer;
bash -c \". assert.sh; assert_raises true; assert_end inner\"
' '<exec>' --invariant" "all 1 outer tests passed.
all 1 inner tests passed."  # <exec> is $0
assert_end output

# commit: fixed output to report all errors, not just the first
assert "_clean;
assert_raises false; assert_raises false;
assert_end" 'test #1 "false" failed:
\tprogram terminated with code 1 instead of 0
test #2 "false" failed:
\tprogram terminated with code 1 instead of 0
2 of 2 tests failed.'
# commit: added default value for assert_raises
assert_raises "_clean; assert_raises true; assert_end" 0
# commit: fixed verbose failure reports in assert_raises
assert "_clean DEBUG=1; assert_raises false; assert_end" 'X
test #1 "false" failed:
\tprogram terminated with code 1 instead of 0
1 of 1 tests failed.'
# commit: redirected assert_raises output
assert "_clean; assert_raises 'echo 1'; assert_end" "all 1 tests passed."
# commit: fixed --discover to reset properly
assert "_clean DISCOVERONLY=1;
assert 1; assert 1; assert_end;
assert 1; assert_end;" "collected 2 tests.\ncollected 1 tests."
# commit: stopped errors from leaking into other test suites
assert "_clean;
assert_raises false; assert_raises false; assert_end;
assert_raises false; assert_end" 'test #1 "false" failed:
\tprogram terminated with code 1 instead of 0
test #2 "false" failed:
\tprogram terminated with code 1 instead of 0
2 of 2 tests failed.
test #1 "false" failed:
\tprogram terminated with code 1 instead of 0
1 of 1 tests failed.'
# issue 1: assert.sh: line 87: DISCOVERONLY: unbound variable
assert "_clean; set -u; assert_raises true; assert true; assert_end" \
"all 2 tests passed."
assert_end regression
