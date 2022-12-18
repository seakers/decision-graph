import plotly.express as px
from plotly.subplots import make_subplots
import plotly.graph_objects as go
import numpy as np
import pandas as pd

from parsing import parse_population_file, merge_dataframes


# figures_dir = "/c/Users/apaza/repos/seakers/decision-graph/visualizations/figures/"
figures_dir = "C:\\Users\\apaza\\repos\\seakers\\decision-graph\\visualizations\\figures\\"

# fig = px.scatter(df, x='benefit', y='cost')

def scatterplot_2d(pop_file):
    file_name = figures_dir + 'scatterplot_2d.html'

    df = parse_population_file(pop_file)

    fig = px.scatter(df, x='benefit', y='cost')
    # fig.write_html(file_name)
    fig.show()
    return

def scatterplot_2d_overlay(pop_file1, pop_file2):
    file_name = figures_dir + 'scatterplot_2d_overlap.html'

    df1 = parse_population_file(pop_file1)
    df2 = parse_population_file(pop_file2)
    df3 = merge_dataframes(df1, df2, 'Run A', 'Run B')

    fig = px.scatter(df3, x='benefit', y='cost', color='NAME')
    fig.update_layout(
        scene = dict(
            xaxis = dict(range=[0,1]),
        )
    )
    # fig.write_html(file_name)
    fig.show()
    return




