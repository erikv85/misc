#! /usr/bin/python

import re
from collections import defaultdict

input_file = "dummy-data.txt"
with open(input_file, "r") as file:
    input = file.readlines()

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
        pf_val += security_value(purchases, price)
    return pf_val

def security_value(purchases, price):
    sec_val = 0
    for purchase in purchases:
        sec_val += purchase[1] * price
    return sec_val

report = []
fmt = "%15s %9.2f, %7.2f%s"
pf_principal = 0
pf_curr_value = 0
diffs = []
for key in sec_map:
    curr_price = references[key][1]
    principal = 0
    total_pieces = 0
    for x in sec_map[key]:
        principal += x[0] * x[1]
        total_pieces += x[1]
    curr_tot_value = curr_price * total_pieces
    diff = curr_tot_value - principal
    diffs.append(diff)
    percent_gain = diff / principal
    report.append(fmt % (key, \
                         curr_tot_value, \
                         percent_gain, \
                         "%"))
    pf_principal += principal
    pf_curr_value += curr_tot_value

abs_diffs = map(lambda x: abs(x), diffs)
abs_pf_diff = reduce(lambda x, y: x + y, abs_diffs)
for i in range(0, len(abs_diffs)):
    frac = 100 * abs_diffs[i] / abs_pf_diff
    report[i] += " (%5.2f%s of total swing)" % (frac, "%")

pf_diff = pf_curr_value - pf_principal
pf_percent_gain = pf_diff / pf_principal
report.append('-----------------------------------')
report.append(fmt % ("Portfolio", \
                     pf_curr_value, \
                     pf_percent_gain, \
                     "%"))

print '\n'.join(report)
