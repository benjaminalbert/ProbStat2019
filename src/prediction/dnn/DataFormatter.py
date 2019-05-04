from pandas import DataFrame
from Decorator import accepts, returns


def read_csv(csv_file):
    from pandas import read_csv
    return read_csv(csv_file)


@accepts(DataFrame, int, int)
# @returns(DataFrame)
def make_recurrent(dataframe, total_inputs=1, total_outputs=1):
    """
    Transform raw data into supervised recurrent regression data.

    :param dataframe: unmutated DataFrame containing raw dataset
    :param total_inputs: int >= 1 that specifies the number of input columns per row.
    :param total_outputs: int >= 1 that specifies the number of output columns per row
    :return: DataFrame where each column ends with one of three patterns: "_t-X", "_t", "_t+X" where X is an int > 0
                The columns ending with "_t" and "_t+X" are the target outputs for recurrent ML tasks.
                The corresponding input columns end with "_t-X" and are used to predict all target outputs.
                Therefore, each row is an independent training instance that contains overlapping information with adjacent rows
    """
    recurrent_data = list()
    column_headers = list()
    for input_timestep in range(total_inputs, 0, -1):
        recurrent_data.append(dataframe.shift(input_timestep))
        column_headers += ["{}_t-{}".format(label, input_timestep) for label in list(dataframe)]
    for output_timestep in range(total_outputs):
        recurrent_data.append(dataframe.shift(-output_timestep))
        column_headers += ["{}_t{}".format(label, "" if output_timestep == 0 else "+{}".format(output_timestep)) for label in list(dataframe)]
    from pandas import concat
    recurrent_dataframe = concat(recurrent_data, axis=1)
    recurrent_dataframe.columns = column_headers
    return recurrent_dataframe.drop(recurrent_dataframe.index[[x for x in range(total_inputs)] + [(dataframe.shape[0] - 1 - x) for x in range(total_outputs - 1)]])


@accepts(DataFrame, float)
# @returns((DataFrame, DataFrame))
def partition(dataframe, percentage):
    """
    Splits dataframe into two DataFrame objects according to percentage

    :param dataframe: unmutated DataFrame to be split
    :param percentage: float in range (0,1) specifying the percent split for the partition
    :return: tuple where the first DataFrame contains the first specified percentage of dataframe, and the second DataFrame contains the remaining rows
    """
    if not isinstance(percentage, float) or percentage >= 1 or percentage <= 0:
        raise ValueError("split must be a float in range (0,1) exclusive")
    row = len(dataframe) * percentage
    return dataframe[:row], dataframe[row:]


@accepts(DataFrame, list, list)
# @returns((DataFrame, DataFrame))
def split_recurrent_data(dataframe, input_columns=None, output_columns=None):
    """
    Split recurrent data into input DataFrame and output DataFrame

    :param dataframe: unmutated DataFrame with columns specifying timesteps, often generated using make_recurrent(...)
    :param input_columns: list of column names to treat as inputs. If None, all columns ending with "_t-X" are considered inputs where 'X' is an int
    :param output_columns: list of column names to treat as outputs. If None, all columns ending with "_t+X" or simply "_t" are considered outputs where 'X' is an int
    :return: tuple where the first DataFrame are recurrent inputs and the second DataFrame are recurrent outputs
    """
    if not input_columns:
        import re
        input_columns = [x for x in dataframe.keys() if (re.search("_t-[0-9]+$", x))]
    if not output_columns:
        import re
        output_columns = [x for x in dataframe.keys() if (re.search("_t\\+[0-9]+$", x) or x.endswith("_t"))]
    from pandas import concat
    return concat([dataframe[input_columns]], axis=1), concat([dataframe[output_columns]], axis=1)


@accepts(DataFrame, DataFrame, float)
def recurrent_regression_to_classification(input, output, minimum_delta_percentage=0.10):
    """
    Discretize recurrent regression data by converting outputs to represent increase (1), decrease (-1), or no change (0)

    :param input: unmutated regression input dataframe
    :param output: unmutated regression output dataframe
    :param minimum_delta_percentage: minimum percent change between timesteps to consider an increase, decrease, or constant
    :return: tuple where the first DataFrame is input[1:] and the second DataFrame is of size output[1:] but the elements
                represent the delta between timesteps:
                     0 = no change
                     1 = increase
                    -1 = decrease
                if the percent delta is greater than minimum_delta_percentage, then the corresponding output is set to 1
                similarly, if it is less than minimum_delta_percentage, it is set to -1
                the default is therefore 0

    """
    classification_data = output.diff().divide(abs(output))
    return input[1:], classification_data.applymap(lambda x: 0 if (-minimum_delta_percentage < x < minimum_delta_percentage) else x/abs(x))[1:]


if __name__ == "__main__":
    raw = DataFrame({"a": [1, 5, 5, 100, 1000, -10000, -100],
                     "b": [2, 5, 12, 16,   32,     70,  -40]})
    print("raw\n", raw, "\n")
    data = make_recurrent(raw, 2, 1)
    print("recurrent\n", data, "\n")
    input, output = recurrent_regression_to_classification(*split_recurrent_data(data))
    print("input\n", input, "\n")
    print("output\n", output, "\n")