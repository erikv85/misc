#! /usr/bin/python

import re
from collections import defaultdict

def read_file_lines(input_file):
    with open(input_file, "r") as file:
        input = file.readlines()
    return input

def get_body_lines(input, start_pat, end_pat):
    mstart = re.compile(start_pat)
    mend = re.compile(end_pat)
    start = -1
    end = -1
    for i in range(len(input)):
        if mstart.match(input[i]):
            start = i
        elif mend.match(input[i]):
            end = i
            break
    return input[start : end]

def get_purchases(data):
    purchase_pattern = re.compile("^\s*([\.0-9]+)\s+([\.0-9]+)\s+([_0-9]+)\s+(\S+)\s+(\"[^\"]+\")(\s+#.*)?$")
    sec_map = defaultdict(list) # str -> list[tuple[float]]
    for line in data:
        match = purchase_pattern.match(line)
        if match:
            price = float(match.group(1))
            pieces = float(match.group(2))
            date = match.group(3)
            account = match.group(4)
            security = match.group(5)
            sec_map[security].append((price, pieces))
    return sec_map

############################################################
# TODO: Remaining code/behavior has not been documented
############################################################
# list[string] -> ( string -> tuple[string, float] )
def get_references(input):
    date_match = re.match("^\s*([_0-9]+)$", input[0])
    if date_match:
        date = date_match.group(1)
    else:
        date = "" # TODO: handle properly
    references = dict()
    for i in range(1, len(input)):
        ref_res = format_reference_line(input[i])
        references[ref_res[0]] = (date, ref_res[1])
    return references

def format_reference_line(line):
    match = re.match("^\s*(\"[^\"]+\")\s+([\.0-9]+)$", line)
    if match:
        return (match.group(1), float(match.group(2)))
    else:
        return ()

# sec_map    : "a" -> [(1.00, 10), (1.01, 10), (1.02, 10), (1.03, 10)]
# references : "a" -> (190331_0000, 1.04)

def security_principal(purchases):
    sec_principal = 0
    for purchase in purchases:
        sec_principal += purchase[0] * purchase[1]
    return sec_principal

def portfolio_principal(portfolio):
    pf_principal = 0
    for security in portfolio:
        pf_principal += security_principal(portfolio[security])
    return pf_principal

def portfolio_value(portfolio, prices):
    pf_val = 0
    for security in portfolio:
        pf_val += security_value(portfolio[security], prices[security][1])
    return pf_val

def security_value(purchases, price):
    sec_val = 0
    for purchase in purchases:
        sec_val += purchase[1]
    return sec_val * price

def make_security_report(sec_name, sec_map, references):
    sec_price = references[sec_name][1]
    sec_principal = security_principal(sec_map[sec_name])
    sec_val = security_value(sec_map[sec_name], sec_price)
    diff = sec_val - sec_principal
    percent_gain = 100 * diff / sec_principal
    return (sec_val, percent_gain, "%")

def make_portfolio_report(sec_map, references, fmt):
    pf_val = portfolio_value(sec_map, references)
    pf_principal = portfolio_principal(sec_map)
    pf_diff = pf_val - pf_principal
    pf_percent_gain = 100 * pf_diff / pf_principal
    return fmt % ("Portfolio", pf_val, pf_percent_gain, "%")

def make_full_report(sec_map, references, fmt):
    report = []
    diffs = []
    for key in sec_map:
        report.append(fmt % ((key,) + make_security_report(key, sec_map, references)))
        diff = security_value(sec_map[key], references[key][1]) -\
                security_principal(sec_map[key])
        diffs.append(diff)

    abs_diffs = map(lambda x: abs(x), diffs)
    abs_pf_diff = reduce(lambda x, y: x + y, abs_diffs)
    for i in range(len(abs_diffs)):
        frac = 100 * abs_diffs[i] / abs_pf_diff
        report[i] += " (%5.2f%s of total swing)" % (frac, "%")

    report.append('-' * 35)
    report.append(make_portfolio_report(sec_map, references, fmt))
    return report

def main():
    input = read_file_lines("dummy-data.txt")
    data = get_body_lines(input, "^tab_start:$", "^tab_end$")
    data = data[2 : ]
    sec_map = get_purchases(data)

    ref_data = get_body_lines(input, "^ref_start:$", "^ref_end$")
    references = get_references(ref_data[1 : ])

    fmt = "%15s %9.2f, %7.2f%s"
    final_report = '\n'.join(make_full_report(sec_map, references, fmt))
    print final_report
    print "OK" if test(final_report) else "error"

def test(report):
    key = """            "a"     41.60,    2.46% (20.00% of total swing)
            "b"     92.40,    1.65% (30.00% of total swing)
            "c"    204.00,    1.24% (50.00% of total swing)
-----------------------------------
      Portfolio    338.00,    1.50%"""
    return report == key

main()
