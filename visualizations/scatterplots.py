import plotly.express as px
from plotly.subplots import make_subplots
import plotly.graph_objects as go
import numpy as np
import pandas as pd
from pareto_funcs import find_combined_pareto
import os
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





def combined_pareto_single(group_a, name_a):
    front_a = find_combined_pareto(group_a)
    df_a = pd.DataFrame(front_a, columns =['Benefit', 'Cost'])
    fig = px.scatter(df_a, x='Benefit', y='Cost')
    fig.update_layout(title_text=name_a)
    fig.update_xaxes(range=[-0.02, 1])
    fig.update_yaxes(range=[0, 22000])
    fig.show()



def combined_pareto_overlay(group_a, name_a, group_b, name_b):


    front_a = find_combined_pareto(group_a)
    front_b = find_combined_pareto(group_b)

    df_a = pd.DataFrame(front_a, columns =['Benefit', 'Cost'])
    df_b = pd.DataFrame(front_b, columns =['Benefit', 'Cost'])
    df3 = merge_dataframes(df_a, df_b, name_a, name_b)

    print(df3)
    fig = px.scatter(df3, x='Benefit', y='Cost', color='NAME')
    fig.update_layout(title_text='SCAN: Combined Pareto Front Comparison', legend_title_text='Formulation')
    fig.update_xaxes(range=[-0.02, 1])
    # fig.update_yaxes(range=[0, 22000])


    fig.update_layout(title_font=dict(size=26))
    fig.update_xaxes(title="Benefit (adimensional)", title_font=dict(size=22), tickfont=dict(size=22))
    fig.update_yaxes(title="Cost (millions)", title_font=dict(size=22), tickfont=dict(size=22))
    fig.update_layout(legend_title=dict(font=dict(size=22)), legend=dict(font=dict(size=22)))

    fig.show()


    return 0




