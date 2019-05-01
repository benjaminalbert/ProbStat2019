import tensorflow as tf
from ModelFactory import ModelFactory
from PoliceCallPredictor import PoliceCallPredictor


def train_mnist():
    (x_train, y_train), (x_test, y_test) = tf.keras.datasets.mnist.load_data()

    x_train = x_train.reshape(x_train.shape[0], 28, 28, 1)
    x_test = x_test.reshape(x_test.shape[0], 28, 28, 1)

    x_train = x_train[:1000]
    y_train = y_train[:1000]

    x_train = x_train / 255.0
    x_test = x_test / 255.0

    model = ModelFactory.conv_convlstm_dense_2((28, 28, 1), 10)

    pcp = PoliceCallPredictor(model=model)

    pcp.train(x_train,
              y_train,
              loss=tf.keras.losses.sparse_categorical_crossentropy,
              val_inputs=x_test,
              val_outputs=y_test)


if __name__ == "__main__":
    train_mnist()
