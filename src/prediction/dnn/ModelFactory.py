import tensorflow as tf


def classifier(input_shape_rank):
    """
    Decorator for argument type and range validation

    :param input_shape_rank: an int to specify the dimensionality of the inputs to the model (e.g. input_shape_rank=2 specifies an MxN input shape)
    :return: an RNN (tf.keras.models.Sequential) object
    """
    if not input_shape_rank or type(input_shape_rank) != int or input_shape_rank < 1:
        raise ValueError("input_shape_rank must be a positive int")

    def wrapper(func):
        from Decorator import returns
        @returns(tf.keras.models.Sequential)
        def classifier_func(*args, **kwargs):
            from inspect import getfullargspec
            args_dict = dict(zip(getfullargspec(func)[0], args))
            if "input_shape" in args_dict.keys():
                input_shape = args_dict["input_shape"]
                if not input_shape or type(input_shape) != tuple or len(input_shape) != input_shape_rank:
                    raise ValueError("input_shape must be a non-empty tuple and len(input_shape) ({}) must equal input_shape_rank ({})".format(len(input_shape), input_shape_rank))
                for value in input_shape:
                    if type(value) is not int:
                        raise ValueError("all values in input_shape must be of type int")
            else:
                raise ValueError("input_shape parameter name required for classifier decorator")
            if "classes" in args_dict.keys():
                classes = args_dict["classes"]
                if not classes or type(classes) != int or classes <= 1:
                    raise ValueError("classes must be an int > 1")
            return func(*args, **kwargs)

        classifier_func.__name__ = func.__name__
        return classifier_func

    return wrapper


def round_even(x):
    """
    Convenience method for rounding a number to the nearest even int

    :param x: number (int or float) to be rounded to the nearest even int
    :return: nearest even int
    """
    return int(round(x / 2.0)) * 2


class ModelFactory(object):

    @staticmethod
    @classifier(input_shape_rank=2)
    def simple_lstm(input_shape, classes, leaky_relu_alpha=0.04):
        """
        Generate network of architecture:
            LSTM
                units = M * N / 10
                activation = leaky relu
            Dense
                units = classes
                activation = softmax


        :param input_shape: tuple of int with length input_shape_rank=2 specifying the model input shape (MxN)
        :param classes: number of output units
        :param leaky_relu_alpha: slope of the leaky relu activation function
        :return: network with an lstm layer and a dense layer (see network architecture)
        """
        from tensorflow.contrib.keras.api.keras.layers import LSTM, Dense, LeakyReLU
        model = tf.keras.models.Sequential()
        input_units = round_even(input_shape[0] * input_shape[1] / 10.0)
        model.add(LSTM(input_units, input_shape=input_shape))
        model.add(LeakyReLU(alpha=leaky_relu_alpha))
        model.add(Dense(classes, activation="softmax"))
        return model

    @staticmethod
    @classifier(input_shape_rank=2)
    def lstm_2_dense_2(input_shape,
                       classes,
                       input_unit_multiplier=0.10,
                       input_layer_dropout=0.16,
                       dropout_decay=0.5,
                       lstm_1_leaky_relu_alpha=0.04,
                       lstm_2_leaky_relu_alpha=0.02,
                       lstm_2_unit_input_multiplier=0.5,
                       dense_1_unit_input_multiplier=0.25,
                       presoftmax_dense_activation="tanh"):
        """
        Generate network of architecture:
            LSTM
                units = M * N * input_unit_multiplier
                activation = leaky relu
                dropout = input_layer_dropout
            LSTM
                units = M * N * input_unit_multiplier * lstm_2_unit_input_multiplier
                activation = leaky relu
                dropout = input_layer_dropout * dropout_decay
            Dense
                units = M * N * input_unit_multiplier * dense_1_unit_input_multiplier
                activation = presoftmax_dense_activation
                dropout = input_layer_dropout * (dropout_decay ** 2)
            Dense
                units = classes
                activation = softmax

        :param input_shape: tuple of int with length input_shape_rank=2 specifying the model input shape (MxN)
        :param classes: number of output units
        :param input_unit_multiplier: multiplier for generating number of input units (see model architecture)
        :param input_layer_dropout: dropout rate for the first LSTM. If 0, no dropout
        :param dropout_decay: dropout decay rate by which the initial dropout rate decays for lower layers. If 0, constant dropout rate
        :param lstm_1_leaky_relu_alpha: slope of the leaky relu activation function for the first lstm
        :param lstm_2_leaky_relu_alpha: slope of the leaky relu activation function for the second lstm
        :param lstm_2_unit_input_multiplier: multiplier for generating the number of units for the second lstm (see model architecture)
        :param dense_1_unit_input_multiplier: multiplier for generating the number of units for the first dense layer (see model architecture)
        :param presoftmax_dense_activation: activation function for the first dense layer
        :return: network with two lstm layers and two dense layers (see network architecture)
        """

        from tensorflow.contrib.keras.api.keras.layers import LSTM, Dense, LeakyReLU, Dropout
        model = tf.keras.models.Sequential()
        input_units = round_even(input_shape[0] * input_shape[1] * input_unit_multiplier)
        model.add(LSTM(input_units, input_shape=input_shape, return_sequences=True))
        model.add(LeakyReLU(alpha=lstm_1_leaky_relu_alpha))
        if input_layer_dropout > 0:
            model.add(Dropout(input_layer_dropout))
        model.add(LSTM(round_even(input_units * lstm_2_unit_input_multiplier)))
        model.add(LeakyReLU(alpha=lstm_2_leaky_relu_alpha))
        if input_layer_dropout > 0:
            model.add(Dropout(input_layer_dropout * dropout_decay))
        model.add(Dense(round_even(input_units * dense_1_unit_input_multiplier), activation=presoftmax_dense_activation))
        if input_layer_dropout > 0:
            model.add(Dropout(input_layer_dropout * (dropout_decay ** 2)))
        model.add(Dense(classes, activation="softmax"))

        return model

    @staticmethod
    @classifier(input_shape_rank=4)
    def convlstm_2_dense_2(input_shape,
                           output_units,
                           channels_first=True,
                           units=None,
                           filter_sizes=(5, 3),
                           dropouts=(0.08, 0.04, 0.02),
                           leaky_relu_alphas=(0.04, 0.04),
                           pool_size=2,
                           pool_method="avg",
                           dense_1_activation="tanh",
                           dense_2_activation="tanh",
                           print_model_architecture=True):

        from tensorflow.contrib.keras.api.keras.layers import ConvLSTM2D, LeakyReLU, Dense, AveragePooling2D, MaxPooling2D, Dropout, Flatten
        model = tf.keras.models.Sequential()

        data_format = "channels_first" if channels_first else "channels_last"

        if not units:
            units = []
            units.append(round_even(input_shape[2] * input_shape[3] / 2))
            units.append(units[0] * 2)
            units.append(round_even((units[1] + output_units) / 2))
        if print_model_architecture:
            print("building network with architecture:")
            print("\tCONV LSTM 2D")
            print("\t\tfilters: {}".format(units[0]))
            print("\t\tleaky relu alpha: {}".format(leaky_relu_alphas[0]))
            print("\t\tdata format: {}".format(data_format))
            if dropouts[0] > 0:
                print("\t\tdropout: {}".format(dropouts[0]))
            print("\tCONV LSTM 2D")
            print("\t\tfilters: {}".format(units[1]))
            print("\t\tleaky relu alpha: {}".format(leaky_relu_alphas[1]))
            print("\t\tdata format: {}".format(data_format))
            if dropouts[1] > 0:
                print("\t\tdropout: {}".format(dropouts[1]))
            if pool_size > 0:
                print("\tPOOL (N/A IF SIZE=0)")
                print("\t\tsize: {}".format(pool_size))
                print("\t\tmethod: {}".format(pool_method))
            print("\tFLATTEN")
            print("\t\tdata format: {}".format(data_format))
            print("\tDENSE")
            print("\t\tunits: {}".format(units[2]))
            print("\t\tactivation: {}".format(dense_1_activation))
            print("\tDENSE")
            print("\t\tunits: {}".format(output_units))
            print("\t\tactivation: {}".format(dense_2_activation))

        if len(units) != 3 or not all([x > 0 for x in units]):
            raise ValueError("inputs to each layer must be a positive int")

        model.add(ConvLSTM2D(units[0], (filter_sizes[0], filter_sizes[0]), input_shape=input_shape, data_format=data_format, return_sequences=True))
        model.add(LeakyReLU(alpha=leaky_relu_alphas[0]))
        if dropouts[0] > 0:
            model.add(Dropout(dropouts[0]))

        model.add(ConvLSTM2D(units[1], (filter_sizes[1], filter_sizes[1]), data_format=data_format))
        model.add(LeakyReLU(alpha=leaky_relu_alphas[1]))
        if dropouts[1] > 0:
            model.add(Dropout(dropouts[1]))

        if pool_size > 0:
            model.add(AveragePooling2D((pool_size, pool_size)) if pool_method == "avg" else MaxPooling2D(pool_size, pool_size))

        model.add(Flatten(data_format=data_format))
        model.add(Dense(units[2], activation=dense_1_activation))
        model.add(Dense(output_units, activation=dense_2_activation))

        return model
