import tensorflow as tf


def classifier(input_shape_rank):
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
    return int(round(x / 2.0)) * 2


class ModelFactory(object):

    @staticmethod
    @classifier(input_shape_rank=2)
    def simple_lstm(input_shape, classes, leaky_relu_alpha=0.04):
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
    @classifier(input_shape_rank=3)
    def conv_convlstm_dense_2(input_shape,
                              classes,
                              input_filter_multiplier=0.02,
                              conv_filter_size=5,
                              conv_leaky_relu_alpha=0.04,
                              conv_dropout=0.08,
                              convlstm_filter_input_multiplier=2.0,
                              convlstm_filter_size=3,
                              convlstm_leaky_relu_alpha=0.04,
                              convlstm_dropout=0.16,
                              pool_size=4,
                              dense_1_input_multiplier=0.025,
                              presoftmax_dense_activation="tanh"):

        from tensorflow.contrib.keras.api.keras.layers import Conv2D, ConvLSTM2D, LeakyReLU, Dense, AveragePooling2D, Dropout, Flatten
        model = tf.keras.models.Sequential()
        input_filters = round_even(input_shape[0] * input_shape[1] * input_filter_multiplier)

        model.add(Conv2D(input_filters, (conv_filter_size, conv_filter_size), input_shape=input_shape))
        model.add(LeakyReLU(alpha=conv_leaky_relu_alpha))
        model.add(Dropout(conv_dropout))
        model.add(Conv2D(round_even(input_filters * convlstm_filter_input_multiplier), (convlstm_filter_size, convlstm_filter_size)))
        model.add(LeakyReLU(alpha=convlstm_leaky_relu_alpha))
        model.add(Dropout(convlstm_dropout))
        model.add(AveragePooling2D((pool_size, pool_size)))
        model.add(Flatten())
        model.add(Dense(round_even(input_shape[0] * input_shape[1] * dense_1_input_multiplier), activation=presoftmax_dense_activation))
        model.add(Dense(classes, activation="softmax"))

        return model

