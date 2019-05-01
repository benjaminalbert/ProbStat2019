import pandas as pd


def read_csv(csv_file):
    return pd.read_csv(csv_file)


def make_recurrent(dataframe, total_inputs=1, total_outputs=1):
    recurrent_data = list()
    column_headers = list()
    for input_timestep in range(total_inputs, 0, -1):
        recurrent_data.append(dataframe.shift(input_timestep))
        column_headers += ["{}_t-{}".format(label, input_timestep) for label in list(dataframe)]
    for output_timestep in range(total_outputs):
        recurrent_data.append(dataframe.shift(-output_timestep))
        column_headers += ["{}_t{}".format(label, "" if output_timestep == 0 else "+{}".format(output_timestep)) for label in list(dataframe)]
    recurrent_dataframe = pd.concat(recurrent_data, axis=1)
    recurrent_dataframe.columns = column_headers
    return recurrent_dataframe.drop(recurrent_dataframe.index[[x for x in range(total_inputs)] + [(dataframe.shape[0] - 1 - x) for x in range(total_outputs - 1)]])


if __name__ == "__main__":
    data = [x for x in range(10)]
    test = pd.DataFrame(data={"index": [x for x in data],
                              "int": [x for x in data],
                              "float": [float(x) for x in data],
                              "str": [str(x) for x in data]})
    test.set_index("index", inplace=True)
    print(test)
    data = make_recurrent(test, 3, 2)
    print(data.to_csv())
