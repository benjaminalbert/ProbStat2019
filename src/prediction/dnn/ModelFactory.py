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
                           classes,
                           channels_first=True,
                           input_filter_multiplier=0.10,
                           convlstm_1_filter_size=3,
                           convlstm_1_leaky_relu_alpha=0.04,
                           convlstm_1_dropout=0.08,
                           convlstm_2_filter_input_multiplier=2.0,
                           convlstm_2_filter_size=3,
                           convlstm_2_leaky_relu_alpha=0.04,
                           convlstm_2_dropout=0.01,
                           pool_size=4,
                           pool_method="avg",
                           dense_1_input_multiplier=0.50,
                           dense_1_activation="tanh",
                           dense_2_activation="tanh"):
        """
        Generate network of architecture:
            Conv LSTM 2D
                filters = M * N * L * input_filter_multiplier
                activation = leaky relu
                dropout = convlstm_1_dropout
            Conv LSTM 2D
                filters = M * N * L * input_filter_multiplier * convlstm_2_filter_input_multiplier
                activation = leaky relu
                dropout = convlstm_2_dropout
            Pool (see param specifications for contingencies)
                method = pool_method
            Flatten
                to one dimensional vector
            Dense
                units = M * N * L * dense_1_input_multiplier
            Dense
                units = classes
                activation = tanh


        :param input_shape: tuple of int with length input_shape_rank=3 specifying the model input shape (MxNxL).
        :param classes: number of output units
        :param input_filter_multiplier: multiplier for generating number of input filters (see model architecture)
        :param convlstm_1_filter_size: size of the first convlstm layer filters
        :param convlstm_1_leaky_relu_alpha: slope of the leaky relu activation function for the first convlstm layer
        :param convlstm_1_dropout: dropout rate for the first convlstm layer. If 0, no dropout
        :param convlstm_2_filter_input_multiplier: multiplier for generating number of filters for the second convlstm layer (see model architecture)
        :param convlstm_2_filter_size: size of the second convlstm layer filters
        :param convlstm_2_leaky_relu_alpha: slope of the leaky relu activation function for the second convlstm layer
        :param convlstm_2_dropout: dropout rate for the second convlstm layer. If 0, no dropout
        :param pool_size: size of the pooling square
        :param pool_method: type of pooling ("avg" or "max"). If None or not a str object, then pooling layer is not added
        :param dense_1_input_multiplier: multiplier for generating number of input units for the first dense layer (see model architecture)
        :param presoftmax_dense_activation: activation function for the first dense layer
        :return: network with two convlstm layers and two dense layers (see network architecture)
        """

        from tensorflow.contrib.keras.api.keras.layers import ConvLSTM2D, LeakyReLU, Dense, AveragePooling2D, MaxPooling2D, Dropout, Flatten
        model = tf.keras.models.Sequential()
        input_filters = round_even(input_shape[1] * input_shape[2] * input_filter_multiplier)

        data_format = "channels_first" if channels_first else "channels_last"

        model.add(ConvLSTM2D(input_filters, (convlstm_1_filter_size, convlstm_1_filter_size), input_shape=input_shape, data_format=data_format, return_sequences=True))
        model.add(LeakyReLU(alpha=convlstm_1_leaky_relu_alpha))
        if convlstm_1_dropout > 0:
            model.add(Dropout(convlstm_1_dropout))
        model.add(ConvLSTM2D(round_even(input_filters * convlstm_2_filter_input_multiplier), (convlstm_2_filter_size, convlstm_2_filter_size), data_format=data_format))
        model.add(LeakyReLU(alpha=convlstm_2_leaky_relu_alpha))
        if convlstm_2_dropout > 0:
            model.add(Dropout(convlstm_2_dropout))
        if pool_size and type(pool_size) == int and pool_size > 0 and pool_method and type(pool_method) == str:
            model.add(AveragePooling2D((pool_size, pool_size)) if pool_method == "avg" else MaxPooling2D(pool_size, pool_size))
        model.add(Flatten(data_format=data_format))
        model.add(Dense(round_even(input_shape[1] * input_shape[2] * dense_1_input_multiplier), activation=dense_1_activation))
        model.add(Dense(classes, activation=dense_2_activation))

        return model