import os
import pandas as pd
import glob

from parsing import parse_population_file
from scatterplots import scatterplot_2d, scatterplot_2d_overlay
from hvplots import get_hv_plot, get_hv_plot_multi
from pareto_funcs import find_combined_pareto
from scatterplots import combined_pareto_overlay, combined_pareto_single
results_dir = "/c/Users/apaza/repos/seakers/decision-graph/results/"
# results_dir = "C:\\Users\\apaza\\repos\\seakers\\decision-graph\\results\\"

# root_dir = os.path.join('C:\\', 'Users', 'apaza', 'repos', 'seakers', 'decision-graph')
# results_dir = os.path.join(root_dir, 'results')
problem_dir = os.path.join(results_dir, 'TDRS')



group_0 = os.path.join(problem_dir, 'group_0', 'runs') # VANILLA
group_1 = os.path.join(problem_dir, 'group_1', 'runs') # ADG
group_2 = os.path.join(problem_dir, 'group_2', 'runs') # EXPERT
group_3 = os.path.join(problem_dir, 'group_3', 'runs') # ADG-HIGH-MUTATION


group_4 = os.path.join(problem_dir, 'group_4', 'runs') # ADG
group_5 = os.path.join(problem_dir, 'group_5', 'runs') # EXPERT



def combine_pareto():
    group_a = group_4
    name_a = os.path.split(os.path.split(group_a)[0])[1]
    name_a = 'ADD_FORMULATION'

    group_b = group_5
    name_b = os.path.split(os.path.split(group_b)[0])[1]
    name_b = 'EXPERT_FORMULATION'


    combined_pareto_single(group_a, name_a)
    combined_pareto_single(group_b, name_b)
    combined_pareto_overlay(group_a, name_a, group_b, name_b)




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
    run_a = group_0
    run_b = group_1

    # --------------------
    # ----- ANALYSIS -----
    # --------------------
    get_hv_plot(run_a, run_b, tag_a='VANILLA', tag_b='ADG')


def run_multi_analysis():

    runs = [
        (group_5, 'EXPERT_FORMULATION'),
        (group_4, 'ADD_FORMULATION')
    ]
    get_hv_plot_multi(runs)
















if __name__ == '__main__':
    run_multi_analysis()
    combine_pareto()

