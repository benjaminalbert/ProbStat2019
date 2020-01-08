from pandas import DataFrame
from Decorator import accepts, returns


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
    row = int(len(dataframe) * percentage)
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


@accepts(DataFrame, DataFrame, float, float, bool)
def recurrent_regression_to_classification(input, output, minimum_delta=1, minimum_delta_percentage=0.10, enforce_both_minimums=True):
    """
    NOTE: Classification using non-diffed inputs (i.e. raw continuous data) does not perform well.
        Either diff the inputs for classification or do not use this method and simply do regression instead.

    Discretize recurrent regression data by converting outputs to represent increase (1), decrease (-1), or no change (0)

    :param input: unmutated regression input dataframe
    :param output: unmutated regression output dataframe
    :param minimum_delta: minimum absolute value between timesteps to consider increase, decrease, or remaining constant
    :param minimum_delta_percentage: minimum percent change between timesteps to consider increase, decrease, or remaining constant
    :param enforce_both_minimums: if True, then deltas must pass both the minimum_delta and minimum_delta_percentage thresholds,
                                    else only one of the threshold values is sufficient to consider increase or decrease
    :return: tuple where the first DataFrame is input[1:] and the second DataFrame is of size output[1:] but the elements
                represent the delta between timesteps:
                     0 = no change
                     1 = increase
                    -1 = decrease
                if the percent delta is greater than minimum_delta_percentage, then the corresponding output is set to 1
                similarly, if it is less than minimum_delta_percentage, it is set to -1
                the default is therefore 0

    """
    import numpy as np

    def _print_total_nan(name, x):
        print("total nan in {}:".format(name), np.count_nonzero(np.isnan(x)))

    def _fill_divisors(x):
        y = np.copy(x)
        y[y == 0] = 1
        y[np.isnan(y)] = 1
        return y

    diff = output.fillna(value=0).diff()[1:].values
    _print_total_nan("diff", diff)
    mask = lambda x, y: np.sign((abs(x) >= y) * diff)
    delta_mask = mask(diff, minimum_delta)
    _print_total_nan("delta_mask", delta_mask)
    _print_total_nan("divisor", _fill_divisors(output.values[1:]))
    delta_percent_mask = mask((diff / _fill_divisors(output.values[1:])), minimum_delta_percentage)
    _print_total_nan("delta_percent_mask", delta_percent_mask)
    ndarray_to_dataframe = lambda x: DataFrame(dict(zip(list(output), x.T)))
    # when applying logical operators, we can use a property of the masks to our advantage:
    #   neither mask will have corresponding elements with opposing signs.
    #   the only possible values combinations are: (0,0), (0,1), (0,-1), (1,1), (-1,-1)
    if enforce_both_minimums:
        # logical and operation that preserves sign: sqrt(x)*sqrt(y)
        return input[1:], ndarray_to_dataframe(np.sqrt(delta_mask.astype(complex)) * np.sqrt(delta_percent_mask.astype(complex))).applymap(lambda x: x.real)
    else:
        # logical or operation that preserves sign: (x+y)/|x+y|
        # to avoid division by zero errors, the sum mask zeros are replaced with ones
        sum_mask = delta_mask + delta_percent_mask
        return input[1:], ndarray_to_dataframe(sum_mask / abs(_fill_divisors(sum_mask)))


@accepts(DataFrame, int, int, list, dict, float)
def reshape_recurrent_input(input, rows, columns, global_columns=None, global_column_forecast_timesteps=None, abs_max=0):
    """
    :param input: dataframe to copy and reshape (unmutated)
    :param rows: number of rows of the map grid
    :param columns: number of columns of the map grid
    :param global_columns: base column names of column data that should be transformed into 2D by copying a single value into a (row, column) array
    :param global_column_forecast_timesteps: how many timesteps forward to forecast global variables. (e.g. weather forecasting temperature/relative humidity/precipitation)
    :param abs_max: normalization divisor. if 0, maximum is calculated and return for use with testing data to keep a constant normalization factor and avoid look-ahead bias
    :return: reshaped input for recurrent deep learning tasks, normalization divisor
    """
    import re
    import numpy as np
    from pandas import concat
    timesteps = len({x[re.search("t-[0-9]+$", x).start():] for x in input.keys()})
    spatial_inputs = []
    global_inputs = []
    total_nan = sum(input.isna().sum())
    if total_nan > 0:
        print("replacing {} input nan values with 0".format(total_nan))
    input.fillna(value=0, inplace=True)
    if not abs_max:
        abs_max = abs(input.values).max()
        print("normalizing with abs_max:", abs_max)
    for x, key in enumerate(input.keys()):
        base_column_name = key[:key.rfind("_")]
        if not global_columns or base_column_name not in global_columns:
            spatial_inputs = [*spatial_inputs, input[key]]
        else:
            global_inputs = [*global_inputs, input[key] / abs_max]
            if global_column_forecast_timesteps and base_column_name in global_column_forecast_timesteps:
                for t in range(global_column_forecast_timesteps[base_column_name]):
                    shifted = input[key].shift(-(t + 1))
                    shifted.rename("{}_t{}".format(base_column_name, "" if not t else "+{}".format(t)), inplace=True)
                    global_inputs = [*global_inputs, shifted / abs_max]


    spatial_inputs = concat(spatial_inputs, axis=1).values.reshape([spatial_inputs[0].shape[0], timesteps, 1, rows, columns])
    spatial_inputs /= abs_max
    if not global_inputs:
        return spatial_inputs, abs_max
    global_inputs = concat(global_inputs, axis=1).values
    total_global_inputs = int(global_inputs.shape[1] / timesteps)
    global_inputs = global_inputs.reshape(spatial_inputs.shape[0], timesteps, total_global_inputs)

    global_inputs = np.array([np.full([rows, columns], global_inputs[x, y, z])
                              for x in range(global_inputs.shape[0])
                              for y in range(timesteps)
                              for z in range(total_global_inputs)]
                             ).reshape([global_inputs.shape[0], timesteps, total_global_inputs, rows, columns])
    merged = np.concatenate((spatial_inputs, global_inputs), axis=2)
    return merged[:(-max(global_column_forecast_timesteps.values()) if total_global_inputs != len(global_columns) else merged.shape[0])], abs_max


@accepts(DataFrame, list)
def remove_global_output_columns(dataframe, global_columns):
    """
    :param dataframe: data to be copied for manipulation (not mutated)
    :param global_columns: list of base column names to remove (does not require timestep chars)
    :return: copy of dataframe with all global_columns removed
    """
    copy = dataframe.copy()
    for x in dataframe.keys():
        if x[:x.rfind("_")] in global_columns:
            copy.drop(x, axis=1, inplace=True)
    return copy


if __name__ == "__main__":
    import numpy as np
    data = dict(zip(list("abcdefghij"), np.array([x for x in range(60)]).reshape([10, 6])))
    raw = DataFrame(data)
    # print("raw\n", raw, "\n")
    data = make_recurrent(raw, 1, 1)
    # print("recurrent\n", data, "\n")
    input, output = split_recurrent_data(data)
    # input, output = recurrent_regression_to_classification(*split_recurrent_data(data), enforce_both_minimums=True)
    global_column_forecast_timesteps = {"i": 2, "j": 2}
    # global_column_forecast_timesteps = None
    reshape_recurrent_input(input, rows=2, columns=4, global_columns=["i", "j"], global_column_forecast_timesteps=global_column_forecast_timesteps)
    print("input\n", input, "\n")
    print("output\n", output, "\n")
