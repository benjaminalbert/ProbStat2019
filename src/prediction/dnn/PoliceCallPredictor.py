import tensorflow as tf
from Decorator import accepts


class PoliceCallPredictor(object):
    def __init__(self,
                 rows=30,
                 columns=30,
                 extra_vars=0,
                 classes=5,
                 timesteps=14,
                 model=None):
        self.rows = rows
        self.columns = columns
        self.extra_vars = extra_vars
        self.classes = classes
        self.timesteps = timesteps
        self.model = model

    def train(self,
              inputs,
              outputs,
              epochs=10,
              learning_rate=0.001,
              decay=1e-6,
              val_inputs=None,
              val_outputs=None,
              optimizer=tf.keras.optimizers.Adam,
              loss=tf.keras.metrics.sparse_categorical_crossentropy,
              metrics=tf.keras.metrics.sparse_categorical_accuracy,
              log_dir="./tensorboard",
              save=True,
              verbosity=1):
        """
        Build and train the model

        :param inputs: array of shape abiding by the model input shape
        :param outputs: array corresponding to the inputs variable and abiding by the model output shape
        :param epochs: number of epochs for which to train
        :param learning_rate: learning rate of the optimizer
        :param decay: optimizer learning rate decay
        :param val_inputs: validation inputs for testing after each epoch
        :param val_outputs: validation outputs corresponding to the val_inputs variable used after each epoch
        :param optimizer: optimizer method for minimizing loss function
        :param loss: loss function
        :param metrics: backend Keras methods for model evaluation during training. Can be tuple of methods
        :param log_dir: dir to save TensorBoard callback data
        :param save: bool to specify whether to save the model to the log_dir or not
        :param verbosity: 0 = silent, 1 = progress bar, 2 = one line per epoch (see Keras file training.py: Model.fit)
        :return: None
        """
        self.model.compile(
            loss=loss,
            optimizer=optimizer(lr=learning_rate, decay=decay),
            metrics=([*metrics] if isinstance(metrics, tuple) else [metrics]),
        )

        self.model.fit(
            inputs,
            outputs,
            epochs=epochs,
            validation_data=(val_inputs, val_outputs),
            callbacks=[tf.keras.callbacks.TensorBoard(log_dir=log_dir, write_graph=save)],
            verbose=verbosity
        )

    # TODO implement prediction method and calculate evaluation metrics (MCC, f1, etc.)
    # def predict(self, inputs):
    #     self.model.predict_proba(inputs)

    @property
    def model(self):
        return self._model

    @model.setter
    def model(self, model):
        """
        Sets the model of this predictor

        :param model: model to set instance variable. If None, ModelFactory.conv_convlstm_dense_2 is used by default
        :return: None
        """
        if not model or not isinstance(model, tf.keras.models.Sequential):
            from ModelFactory import ModelFactory
            input_shape = ((self.rows * self.columns * self.classes + self.extra_vars), self.timesteps)
            self._model = ModelFactory.conv_convlstm_dense_2(input_shape, self.classes)
        else:
            self._model = model

    @property
    def rows(self):
        return self._rows

    @rows.setter
    @accepts(object, int)
    def rows(self, rows):
        self._rows = rows

    @property
    def columns(self):
        return self._columns

    @columns.setter
    @accepts(object, int)
    def columns(self, columns):
        self._columns = columns

    @property
    def extra_vars(self):
        return self._extra_vars

    @extra_vars.setter
    @accepts(object, int)
    def extra_vars(self, extra_vars):
        self._extra_vars = extra_vars

    @property
    def classes(self):
        return self._classes

    @classes.setter
    @accepts(object, int)
    def classes(self, classes):
        self._classes = classes

    @property
    def timesteps(self):
        return self._timesteps

    @timesteps.setter
    @accepts(object, int)
    def timesteps(self, timesteps):
        self._timesteps = timesteps