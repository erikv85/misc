#! /usr/bin/python

import re
from collections import defaultdict

input_file = "dummy-data.txt"
with open(input_file, "r") as file:
    input = file.readlines()
"""
OOOOOR:
    input = file.read()
and then
    [s.start() for s in re.finditer('\n', input)]
to get indices of newlines
"""

tab_start = -1
fields_start = -1
data_start = -1
tab_end = -2
for i in range(0, len(input)):
    if re.match("^tab_start:$", input[i]):
        tab_start = i
    elif re.match("^tab_end$", input[i]):
        tab_end = i
        break

if tab_start < 0 or tab_end <= tab_start + 2:
    print "no data found"
    exit(1)

fields_start = tab_start + 1
data_start = tab_start + 2

fields = re.split("\s+", input[fields_start])
fields = filter(lambda x: len(x) > 0, fields)

data = input[data_start : tab_end]
sec_map = defaultdict(list) # str -> list[tuple[float]]
for line in data:
    divider = line.index('"')
    numeric = line[ : divider].strip()
    cols = re.split("\s+", numeric)
    price = float(cols[0])
    pieces = float(cols[1])
    date = cols[2]
    account = cols[3]
    security = line[divider : ].replace('\n', '')
    sec_map[security].append((price, pieces))

############################################################
# TODO: Remaining code/behavior has not been documented
############################################################
# list[string] -> ( string -> tuple[string, float] )
def read_and_get_references(input):
    ref_start = -1
    ref_end = -1
    references = dict()
    for i in range(0, len(input)):
        if re.match("^ref_start:$", input[i]):
            ref_start = i
        elif ref_start >= 0 and not re.match("^ref_end$", input[i]):
            ref_res = format_reference_line(input[i])
            references[ref_res[0]] = (ref_res[1], ref_res[2])
        elif re.match("^ref_end$", input[i]):
            break
    return references


# string -> tuple[string, string, float]
def format_reference_line(line):
    ref_name_start = line.index('"')
    ref_name_end = line[ref_name_start + 1 : ].index('"')
    ref_name = line[ref_name_start : ref_name_end + 2]
    numeric = line[ref_name_end + 2 : ].strip()
    cols = re.split("\s+", numeric)
    return (ref_name, cols[0], float(cols[1]))

references = read_and_get_references(input)

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

def make_security_report(sec_name):
    sec_price = references[sec_name][1]
    sec_principal = security_principal(sec_map[sec_name])
    sec_val = security_value(sec_map[sec_name], sec_price)
    diff = sec_val - sec_principal
    percent_gain = 100 * diff / sec_principal
    return fmt % (sec_name, sec_val, percent_gain, "%")

def make_portfolio_report():
    pf_val = portfolio_value(sec_map, references)
    pf_principal = portfolio_principal(sec_map)
    pf_diff = pf_val - pf_principal
    pf_percent_gain = 100 * pf_diff / pf_principal
    return fmt % ("Portfolio", pf_val, pf_percent_gain, "%")

fmt = "%15s %9.2f, %7.2f%s"
def make_full_report():
    report = []
    diffs = []
    for key in sec_map:
        report.append(make_security_report(key))
        diff = security_value(sec_map[key], references[key][1]) -\
                security_principal(sec_map[key])
        diffs.append(diff)

    abs_diffs = map(lambda x: abs(x), diffs)
    abs_pf_diff = reduce(lambda x, y: x + y, abs_diffs)
    for i in range(0, len(abs_diffs)):
        frac = 100 * abs_diffs[i] / abs_pf_diff
        report[i] += " (%5.2f%s of total swing)" % (frac, "%")

    report.append('-----------------------------------')
    report.append(make_portfolio_report())
    return report

final_report = '\n'.join(make_full_report())

key = """            "a"     41.60,    2.46% (20.00% of total swing)
            "b"     92.40,    1.65% (30.00% of total swing)
            "c"    204.00,    1.24% (50.00% of total swing)
-----------------------------------
      Portfolio    338.00,    1.50%"""

print final_report
print "OK" if final_report == key else "error"
