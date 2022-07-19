import pandas as pd
import numpy as np
import os
import seaborn as sns
import matplotlib.pyplot as plt

from parsing import parse_hv_files



def get_hv_plot(runs_dir_a, runs_dir_b, tag_a='EXPERT', tag_b='ADG'):
    df1 = parse_hv_files(runs_dir_a, tag_a)
    df2 = parse_hv_files(runs_dir_b, tag_b)
    df1[tag_b] = df2[tag_b]
    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df1, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()

def get_hv_plot_multi(run_groups):
    num_groups = len(run_groups)
    df = parse_hv_files(run_groups[0][0], run_groups[0][1])

    count = 1
    while count < num_groups:
        df_tmp = parse_hv_files(run_groups[count][0], run_groups[count][1])
        df[run_groups[count][1]] = df_tmp[run_groups[count][1]]
        count += 1

    ax = sns.lineplot(x='NFE', y='value', hue='variable', data=pd.melt(df, ['NFE']))
    ax.plot()
    plt.xlabel('NFE')
    plt.ylabel('HV')
    plt.show()


