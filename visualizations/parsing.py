import json
import os
import pandas as pd
import glob





def parse_hv_files(run_dir, col_name):
    df = pd.DataFrame()
    wildcard_path = os.path.join(run_dir, '**', 'hypervolume.txt')
    for filename in glob.iglob(wildcard_path, recursive=True):
        csv_df = pd.read_csv(filename)
        # csv_df.drop(csv_df.index[csv_df['NFE'] < 50], inplace=True)
        csv_df.reset_index()
        df = df.append(csv_df, ignore_index=True)
    df.columns = ['NFE', col_name]
    return df

def parse_population_file(pop_file):
    with open(pop_file) as d_file:
        file_data = json.load(d_file)
        data = []
        for design in file_data:
            data.append([float(design['benefit']), float(design['cost'])])
        df = pd.DataFrame(data, columns=['benefit', 'cost'])
        return df


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
            df3['NAME'][index] = 'Overlap'
            df3['SYMBOL'][index] = 'cross'

    return df3





