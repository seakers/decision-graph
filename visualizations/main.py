import os
from parsing import parse_population_file
from scatterplots import scatterplot_2d, scatterplot_2d_overlay
from hvplots import get_hv_plot


# results_dir = "/c/Users/apaza/repos/seakers/decision-graph/results/"
# results_dir = "C:\\Users\\apaza\\repos\\seakers\\decision-graph\\results\\"

root_dir = os.path.join('C:\\', 'Users', 'apaza', 'repos', 'seakers', 'decision-graph')
results_dir = os.path.join(root_dir, 'results')
problem_dir = os.path.join(results_dir, 'TDRS')



run_0 = os.path.join(problem_dir, 'run_0')
run_1 = os.path.join(problem_dir, 'run_1')


def run_single_analysis():

    # ------------------------
    # ----- SET RUN FILE -----
    # ------------------------
    run = run_0

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
    run_a = problem_dir
    run_b = problem_dir

    # --------------------
    # ----- ANALYSIS -----
    # --------------------
    get_hv_plot(run_a, run_b)























if __name__ == '__main__':
    run_dual_analysis()

