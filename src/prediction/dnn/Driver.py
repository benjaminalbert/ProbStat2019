import tensorflow as tf
from ModelFactory import ModelFactory
from PoliceCallPredictor import PoliceCallPredictor
from Decorator import accepts


def train_mnist():
    """
    Test function used to verify that the machine learning methods are functioning on the MNIST dataset
    The columns of the 28x28 MNIST digit images are treated as time-variant inputs; this conceptualization allows
        recurrent methods to perform image classification

    This method automatically downloads the MNIST data, builds a hard-coded RNN, and trains it by using a
        PoliceCallPredictor object to manage the training process

    TODO add testing/evaluation on the train/test split

    :return: None
    """
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()

    x_train = x_train.reshape(x_train.shape[0], 1, 28, 28, 1)
    x_test = x_test.reshape(x_test.shape[0], 1, 28, 28, 1)

    x_train = x_train[:10000]
    y_train = y_train[:10000]

    x_train = x_train / 255.0
    x_test = x_test / 255.0

    model = ModelFactory.convlstm_2_dense_2((1, 28, 28, 1), 10, channels_first=False, dense_2_activation="softmax")

    pcp = PoliceCallPredictor(model=model)

    pcp.train(x_train,
              y_train,
              loss=tf.keras.losses.sparse_categorical_crossentropy,
              metrics=tf.keras.metrics.sparse_categorical_accuracy,
              val_inputs=x_test,
              val_outputs=y_test)


@accepts(str, int, int, float, int, int, int, bool)
def read_police_call_weather_data(csv_file, rows, columns, train_test_split=0.80, backward_timesteps=28, forward_timesteps=4, forecast_timesteps=4, regression=True):
    from pandas import read_csv
    import DataFormatter
    raw = read_csv(csv_file)
    global_columns = ["Fahrenheit", "Precipitation", "Relative Humidity"]
    raw.drop(["Start Date", "End Date", "Num of Calls"], axis=1, inplace=True)
    global_column_forecast_timesteps = dict(zip(global_columns, [forecast_timesteps] * len(global_columns)))
    # raw.drop(global_columns, axis=1, inplace=True)
    recurrent = DataFormatter.make_recurrent(raw, backward_timesteps, forward_timesteps)
    training_set, testing_set = DataFormatter.partition(recurrent, train_test_split)
    if regression:
        train_input, train_output = DataFormatter.split_recurrent_data(training_set)
        test_input, test_output = DataFormatter.split_recurrent_data(testing_set)
    else:
        regression_to_classification_formatter = lambda x: DataFormatter.recurrent_regression_to_classification(*DataFormatter.split_recurrent_data(x),
                                                                                                                minimum_delta=2,
                                                                                                                minimum_delta_percentage=0.10,
                                                                                                                enforce_both_minimums=True)
        train_input, train_output = regression_to_classification_formatter(training_set)
        test_input, test_output = regression_to_classification_formatter(testing_set)
    input_reshaper = lambda x: DataFormatter.reshape_recurrent_input(x,
                                                                     rows=rows,
                                                                     columns=columns,
                                                                     global_columns=global_columns,
                                                                     global_column_forecast_timesteps=global_column_forecast_timesteps)
    train_input = input_reshaper(train_input)
    test_input = input_reshaper(test_input)
    global_output_column_remover = lambda x: DataFormatter.remove_global_output_columns(x, global_columns)
    train_output = global_output_column_remover(train_output).values.astype(float)[:train_input.shape[0]]
    test_output = global_output_column_remover(test_output).values.astype(float)[:test_input.shape[0]]
    train_output /= max(train_output.max(), abs(train_output.min()))
    test_output /= max(test_output.max(), abs(test_output.min()))

    return train_input.shape, (train_input, train_output), (test_input, test_output)


if __name__ == "__main__":

    # train_mnist()

    from numpy import set_printoptions
    import sys
    import re
    set_printoptions(precision=3, suppress=True, threshold=sys.maxsize)

    csv_file = "../../../data/Formatted_10x10_Training_Data.csv"
    rows = int(re.search("_([0-9]+)x", csv_file).group(1))
    columns = int(re.search("x([0-9]+)_", csv_file).group(1))

    forward_timesteps = 4
    forecast_timesteps = 4

    input_shape, training_set, testing_set = read_police_call_weather_data(csv_file, rows, columns, forward_timesteps=forward_timesteps, forecast_timesteps=forecast_timesteps)

    print("training_input {}\n".format(training_set[0].shape))
    # for t in range(training_set[0].shape[0]):
    #     for channel in range(input_shape[2]):
    #         print(training_set[0][t, 0, channel].reshape([1, rows * columns])[0])
    #     print()
    # print("\ntrain_output\n\n", training_set[1], "\n")
    #
    model = ModelFactory.convlstm_2_dense_2(input_shape[1:],
                                            rows * columns * forward_timesteps,
                                            filter_sizes=(5, 3))
    pcp = PoliceCallPredictor(model=model)
    pcp.train(*training_set,
              epochs=500,
              batch_size=32,
              learning_rate=1e-3,
              decay=1e-5,
              val_inputs=testing_set[0],
              val_outputs=testing_set[1],
              loss=tf.keras.losses.mean_absolute_error,
              metrics=[tf.keras.metrics.mean_absolute_error,
                       tf.keras.metrics.mean_absolute_percentage_error,
                       tf.keras.metrics.mean_squared_error,
                       tf.keras.metrics.mean_squared_logarithmic_error],
              model_name="{}x{}x{}_911_call_predictor".format(forward_timesteps, rows, columns),
              write_tensorboard_graph=True)
