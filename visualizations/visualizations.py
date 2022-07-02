import plotly.express as px
from plotly.subplots import make_subplots
import plotly.graph_objects as go
import numpy as np
import pandas as pd
from parsing import parse_design_file


from trace import get_3d_trace, get_3d_merge_trace, get_3d_trace_basic, get_3d_merge_name_trace

cost_bound = [0, 5000]
science_bound = [0, 0.5]
data_continuity_bound = [0, 1000]




scene = {
    'xaxis': {'range': [0, 1000], 'autorange': 'reversed', 'title': 'Data Continuity'},
    'yaxis': {'range': [0, 0.5], 'title': 'Science'},
    'zaxis': {'range': [0, 2000], 'title': 'Cost'},
}


def plot_pareto_front():


    return






def plot_3_nfe_overlay(f1_name, f2_name, f3_name):
    fig = go.Figure()

    df1 = pd.read_csv(f1_name, sep=',')  # blue
    df2 = pd.read_csv(f2_name, sep=',')  # green
    df3 = pd.read_csv(f3_name, sep=',')  # orange

    df4 = merge_three_dataframes(df1, df2, df3)
    print(df4)

    trace1 = get_3d_merge_trace(df4)

    fig.add_trace(trace=trace1)
    fig.update_layout(scene=scene)
    fig.show()


def merge_three_dataframes(df1, df2, df3):
    df4 = df1.append(df2, ignore_index=True)
    df5 = df4.append(df3, ignore_index=True)
    df_dupe = df5.duplicated(keep=False)

    df1['COLOR'] = 'blue'
    df2['COLOR'] = 'green'
    df3['COLOR'] = 'orange'

    dfa = df1.append(df2, ignore_index=True)
    dfb = dfa.append(df3, ignore_index=True)
    print(dfb)
    for index, row in df_dupe.iteritems():
        if(row is True):
            dfb['COLOR'][index] = 'red'

    return dfb






def plot_3_separate_nfe_overlay(f_compare, f1_name, f2_name, f3_name):
    fig = make_subplots(
        rows=1, cols=3,
        specs=[[{"type": "scene"}, {"type": "scene"}, {"type": "scene"}]],
        subplot_titles=("SEQUENTIAL (SELECTING) - COMBINED (ADD)", "SEQUENTIAL (SELECTING) - COMBINED (ADD)", "SEQUENTIAL (SELECTING) - COMBINED (ADD)")
    )

    df1 = pd.read_csv(f1_name, sep=',')  #
    df2 = pd.read_csv(f2_name, sep=',')  #
    df3 = pd.read_csv(f3_name, sep=',')  #

    df_compare = pd.read_csv(f_compare, sep=',')  #

    df1m = merge_dataframes(df_compare, df1, '500 NFE - SELECTING', '500 NFE - ADD')
    df2m = merge_dataframes(df_compare, df2, '500 NFE - SELECTING', '1000 NFE - ADD')
    df3m = merge_dataframes(df_compare, df3, '500 NFE - SELECTING', '2000 NFE - ADD')

    trace1 = get_3d_merge_trace(df1m)
    trace2 = get_3d_merge_trace(df2m)
    trace3 = get_3d_merge_trace(df3m)

    fig.add_trace(trace1, row=1, col=1)
    fig.add_trace(trace2, row=1, col=2)
    fig.add_trace(trace3, row=1, col=3)

    scene = {
        'xaxis': {'range': [0, 1000], 'autorange': 'reversed', 'title': 'Data Continuity'},
        'yaxis': {'range': [0, 0.8], 'title': 'Science', 'tickmode': 'auto', 'nticks': 9},
        'zaxis': {'range': [0, 5000], 'title': 'Cost'},
        'dragmode': 'turntable'
    }

    fig.update_layout(scene=scene, scene2=scene, scene3=scene)

    fig.show()



def plot_nfe_overlay_json(f1_name, f2_name, t1_name='test1', t2_name='test2', plot_title='PLOT TITLE'):
    fig = go.Figure()

    df1 = parse_design_file(f1_name)  # blue
    df2 = parse_design_file(f2_name)  # green

    df3 = merge_dataframes(df1, df2, t1_name, t2_name)

    groups = disaggregate_dataframe(df3)

    traces = []
    for group in groups:
        fig.add_trace(trace=get_3d_trace(group))
        # traces.add(get_3d_trace(group))

    # trace1 = get_3d_merge_name_trace(df3)

    # fig.add_trace(trace=trace1)
    fig.update_layout({'title': plot_title})
    fig.update_layout(scene=scene)
    # fig.write_html('/home/gabe/Dropbox/Research/ADD/paper_figures/sequential_vs_combined_1.html')
    fig.show()


def plot_nfe_overlay(f1_name, f2_name, t1_name='test1', t2_name='test2'):
    fig = go.Figure()

    df1 = pd.read_csv(f1_name, sep=',') # blue
    df2 = pd.read_csv(f2_name, sep=',') # green

    df3 = merge_dataframes(df1, df2, t1_name, t2_name)

    trace1 = get_3d_merge_name_trace(df3)


    fig.add_trace(trace=trace1)
    fig.update_layout(scene=scene)
    fig.show()


def disaggregate_dataframe(df):
    grouped = [x for _, x in df.groupby('COLOR')]
    return grouped





def plot_three_nfe(f1_name, f2_name, f3_name, save_file='/home/gabe/Dropbox/Research/ADD/add_ga_decadal.html'):
    fig = make_subplots(
        rows=1, cols=3,
        specs=[[{"type": "scene"}, {"type": "scene"}, {"type": "scene"}]],
        subplot_titles=("Combined method - 500 NFE (Decadal Survey)", "Combined method - 1000 NFE (Decadal Survey)", "Combined method - 2000 NFE (Decadal Survey)")
    )

    df1 = pd.read_csv(f1_name, sep=',')
    df2 = pd.read_csv(f2_name, sep=',')
    df3 = pd.read_csv(f3_name, sep=',')

    trace1 = get_3d_trace_basic(df1)
    trace2 = get_3d_trace_basic(df2)
    trace3 = get_3d_trace_basic(df3)

    fig.add_trace(trace1, row=1, col=1)
    fig.add_trace(trace2, row=1, col=2)
    fig.add_trace(trace3, row=1, col=3)


    fig.update_layout(scene=scene, scene2=scene, scene3=scene)

    fig.write_html(save_file)


    fig.show()






def scatter_3d_figure(f_name):
    fig = make_subplots(rows=1, cols=2, specs=[[{"type": "scene"}, {"type": "scene"}]])

    df = pd.read_csv(f_name, sep=',')


    trace = go.Scatter3d(name='test',
                         x=df['data_continuity'],
                         y=df['science'],
                         z=df['cost'],
                         mode='markers',
                         marker={
                             'size': 5,
                         },



                         )

    trace2 = go.Scatter3d(name='test',
                          x=df['data_continuity'],
                          y=df['science'],
                          z=df['cost'],
                          mode='markers',
                          marker={
                              'size': 5,
                          },

                          )

    fig.add_trace(trace, row=1, col=1)
    fig.add_trace(trace2, row=1, col=2)
    fig.show()









def merge_dataframes(df1, df2, df1_name='df1', df2_name='df2'):
    df4 = df1.append(df2, ignore_index=True)
    df_dupe = df4.duplicated(keep=False)

    df1['NAME'] = df1_name
    df2['NAME'] = df2_name

    df1['SYMBOL'] = 'circle'
    df2['SYMBOL'] = 'square'


    df1['COLOR'] = 'blue'
    df2['COLOR'] = 'green'

    df3 = df1.append(df2, ignore_index=True)
    for index, row in df_dupe.iteritems():
        if(row is True):
            df3['COLOR'][index] = 'red'
            df3['NAME'][index] = 'OVERLAP'
            df3['SYMBOL'][index] = 'cross'

    return df3



def plot_pareto_overlap(non_dominated_file, pop_file, title, save_file):
    df = pd.read_csv(non_dominated_file, sep=',')

    df2 = pd.read_csv(pop_file, sep=',')

    df4 = df.append(df2, ignore_index=True)
    df_dupe = df4.duplicated(keep=False)



    df['Population'] = 'ADD GA'
    df2['Population'] = 'SELECTING GA'
    df3 = df.append(df2, ignore_index=True)

    for index, row in df_dupe.iteritems():
        if(row is True):
            df3['Population'][index] = 'OVERLAP'

    print(df3)
    fig = px.scatter_3d(df3, x='data_continuity', y='science', z='cost', color='Population', color_discrete_sequence=['red','green','blue'], title=title)
    #fig.update_zaxes(autorange="reversed")
    #fig.update_yaxes(autorange="reversed")
    fig.update_traces(marker=dict(size=4))
    fig.update_layout(
        scene = dict(
            xaxis = dict(range=[0,1000],autorange="reversed"),
            yaxis = dict(range=[0,0.8]),
            zaxis = dict(range=[0,5000])
        )
    )
    print(df3)


    fig.write_html(save_file)
    fig.show()




    return





