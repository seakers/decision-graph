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