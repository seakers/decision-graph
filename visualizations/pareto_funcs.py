from parsing import parse_population_files
import pareto




def find_combined_pareto(run_path):
    combined_df = parse_population_files(run_path)
    of_cols = [0, 1]
    inputs = [list(combined_df.itertuples(False))]
    nondominated = pareto.eps_sort(inputs, of_cols)
    for arch in nondominated:
        arch[0] *= -1
    return nondominated






def dominates(a1, a2):
    # --> Does a1 dominate a2?
    x1 = float(a1['benefit']) - float(a2['benefit'])
    x2 = float(a1['cost']) - float(a2['cost'])

    if x1 == x2 and x2 == 0:
        return 0

    benefit_dom = (x1 >= 0)
    cost_dom = (x1 <= 0)

    if benefit_dom is True and cost_dom is True:
        return 1  # A dominates B
    if benefit_dom is False and cost_dom is False:
        return -1  # B dominates A
    else:
        return 0