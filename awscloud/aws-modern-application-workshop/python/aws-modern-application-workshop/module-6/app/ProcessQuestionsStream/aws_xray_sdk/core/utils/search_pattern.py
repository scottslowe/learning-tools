def wildcard_match(pattern, text, case_insensitive=True):
    """
    Performs a case-insensitive wildcard match against two strings.
    This method works with pseduo-regex chars; specifically ? and * are supported.
    An asterisk (*) represents any combination of characters.
    A question mark (?) represents any single character.
    :param str pattern: the regex-like pattern to be compared against
    :param str text: the string to compare against the pattern
    :param boolean case_insensitive: dafault is True
    return whether the text matches the pattern
    """
    if pattern is None or text is None:
        return False

    pattern_len = len(pattern)
    text_len = len(text)
    if pattern_len == 0:
        return text_len == 0

    # Check the special case of a single * pattern, as it's common
    if pattern == '*':
        return True

    if case_insensitive:
        pattern = pattern.lower()
        text = text.lower()

    # Infix globs are relatively rare, and the below search is expensive.
    # Check for infix globs and, in their absence, do the simple thing.
    if '*' not in pattern or pattern.index('*') == len(pattern) - 1:
        return _simple_wildcard_match(pattern, text)

    # The res[i] is used to record if there is a match between
    # the first i chars in text and the first j chars in pattern.
    # So will return res[textLength+1] in the end
    # Loop from the beginning of the pattern
    # case not '*': if text[i]==pattern[j] or pattern[j] is '?',
    # and res[i] is true, set res[i+1] to true, otherwise false.
    # case '*': since '*' can match any globing, as long as there is a true
    # in res before i, all the res[i+1], res[i+2],...,res[textLength]
    # could be true
    res = [None] * (text_len + 1)
    res[0] = True
    for j in range(0, pattern_len):
        p = pattern[j]
        if p != '*':
            for i in range(text_len - 1, -1, -1):
                res[i + 1] = res[i] and (p == '?' or (p == text[i]))
        else:
            i = 0
            while i <= text_len and not res[i]:
                i += 1
            for m in range(i, text_len + 1):
                res[m] = True

        res[0] = res[0] and (p == '*')

    return res[text_len]


def _simple_wildcard_match(pattern, text):
    j = 0
    pattern_len = len(pattern)
    text_len = len(text)
    for i in range(0, pattern_len):
        p = pattern[i]
        if p == '*':
            # Presumption for this method is that globs only occur at end
            return True
        elif p == '?':
            if j == text_len:
                # No character to match
                return False
            j += 1
        else:
            if j >= text_len:
                return False

            if(p != text[j]):
                return False
            j += 1

    # Ate up all the pattern and didn't end at a glob, so a match
    # will have consumed all the text
    return j == text_len
