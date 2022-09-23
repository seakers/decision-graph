import os
import pandas as pd
import glob

from parsing import parse_population_file
from scatterplots import scatterplot_2d, scatterplot_2d_overlay
from hvplots import get_hv_plot, get_hv_plot_multi


# results_dir = "/c/Users/apaza/repos/seakers/decision-graph/results/"
# results_dir = "C:\\Users\\apaza\\repos\\seakers\\decision-graph\\results\\"

root_dir = os.path.join('C:\\', 'Users', 'apaza', 'repos', 'seakers', 'decision-graph')
results_dir = os.path.join(root_dir, 'results')
problem_dir = os.path.join(results_dir, 'TDRS')



group_0 = os.path.join(problem_dir, 'group_0', 'runs') # ADG
group_1 = os.path.join(problem_dir, 'group_1', 'runs') # VANILLA
group_2 = os.path.join(problem_dir, 'group_2', 'runs') # VANILLA-RANDOM
group_3 = os.path.join(problem_dir, 'group_3', 'runs') # ADG-HIGH-MUTATION
group_4 = os.path.join(problem_dir, 'group_4', 'runs')


def run_single_analysis():

    # ------------------------
    # ----- SET RUN FILE -----
    # ------------------------
    run = group_0

    # --------------------
    # ----- ANALYSIS -----
    # --------------------
    pop_file = os.path.join(run, 'population.json')
    hv_file = os.path.join(run, 'hypervolume.txt')

    scatterplot_2d(pop_file)


    return 0

def run_dual_analysis():
    print(problem_dir)

    # ------------------------
    # ----- SET RUN FILE -----
    # ------------------------
    run_a = group_3
    run_b = group_4

    # --------------------
    # ----- ANALYSIS -----
    # --------------------
    get_hv_plot(run_a, run_b, tag_a='VANILLA-RANDOM', tag_b='ADG-RANDOM')


def run_multi_analysis():

    runs = [
        (group_0, 'ADG'),
        (group_1, 'VANILLA'),
        # (group_2, 'ADG-HIGH-MUTATION'),
        # (group_3, 'VANILLA 2')
    ]
    get_hv_plot_multi(runs)
















if __name__ == '__main__':
    run_multi_analysis()

