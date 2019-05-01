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
              verbosity=1):

        self.model.compile(
            loss=loss,
            optimizer=optimizer(lr=learning_rate, decay=decay),
            metrics=[metrics]
        )

        self.model.fit(
            inputs,
            outputs,
            epochs=epochs,
            validation_data=(val_inputs, val_outputs),
            verbose=verbosity
        )

    def predict(self, inputs):
        self.model.predict_proba(inputs)

    @property
    def model(self):
        return self._model

    @model.setter
    def model(self, model):
        if not model or not isinstance(model, tf.keras.models.Sequential):
            input_shape = ((self.rows * self.columns * self.classes + self.extra_vars), self.timesteps)
            self._model = PoliceCallPredictor.default_model(input_shape, self.classes)
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