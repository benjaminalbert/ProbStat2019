import tensorflow as tf
from Decorator import accepts, returns


class PoliceCallPredictor(object):
    def __init__(self,
                 rows=10,
                 columns=10,
                 extra_vars=0,
                 classes=5,
                 timesteps=14,
                 model=None):
        self._rows = rows
        self._columns = columns
        self._extra_vars = extra_vars
        self._classes = classes
        self._timesteps = timesteps
        self._model = model

    def train(self,
              inputs,
              outputs,
              epochs=10,
              learning_rate=0.001,
              decay=1e-6,
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
            verbose=verbosity
        )

    def predict(self, inputs):
        self.model.predict_proba(inputs, )


    @staticmethod
    @accepts(tuple, int)
    @returns(tf.keras.models.Sequential)
    def default_model(input_shape, classes):

        from tensorflow.contrib.keras.api.keras.layers import LSTM, Dense, LeakyReLU, Dropout

        model = tf.keras.models.Sequential()

        model.add(LSTM(256, input_shape=input_shape, return_sequences=True))
        model.add(LeakyReLU(alpha=0.05))
        model.add(Dropout(0.2))

        model.add(LSTM(128))
        model.add(LeakyReLU(alpha=0.02))
        model.add(Dropout(0.1))

        model.add(Dense(32, activation="tanh"))
        model.add(Dropout(0.1))

        model.add(Dense(classes, activation="softmax"))

        return model

    @property
    def model(self):
        return self._model

    @model.setter
    def model(self, model):
        if not model or not isinstance(model, tf.keras.models.Sequential):
            input_shape = (self.rows * self.columns * self.classes + self.extra_vars, self.timesteps)
            self._model = PoliceCallPredictor.default_model(input_shape, self.classes)
        else:
            self._model = model

    @property
    def rows(self):
        return self._rows

    @rows.setter
    @accepts(int)
    def rows(self, rows):
        self._rows = rows

    @property
    def columns(self):
        return self._columns

    @columns.setter
    @accepts(int)
    def columns(self, columns):
        self._columns = columns

    @property
    def extra_vars(self):
        return self._extra_vars

    @extra_vars.setter
    @accepts(int)
    def extra_vars(self, extra_vars):
        self._extra_vars = extra_vars

    @property
    def classes(self):
        return self._classes

    @classes.setter
    @accepts(int)
    def classes(self, classes):
        self._classes = classes

    @property
    def timesteps(self):
        return self._timesteps

    @timesteps.setter
    @accepts(int)
    def timesteps(self, timesteps):
        self._timesteps = timesteps


batch_size = 128
classes = 10

mnist = tf.keras.datasets.mnist
(x_train, y_train), (x_test, y_test) = mnist.load_data()

x_train = x_train[:1000]
y_train = y_train[:1000]

x_train = x_train / 255.0
x_test = x_test / 255.0

print(x_train.shape)
print(x_train[0].shape)

model = tf.keras.models.Sequential().fit()

print(model.predict_proba(x_test,100).shape)
