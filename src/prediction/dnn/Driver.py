import tensorflow as tf
from ModelFactory import ModelFactory
from PoliceCallPredictor import PoliceCallPredictor


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

    model = ModelFactory.convlstm_2_dense_2((1, 28, 28, 1), 10)

    pcp = PoliceCallPredictor(model=model)

    pcp.train(x_train,
              y_train,
              loss=tf.keras.losses.sparse_categorical_crossentropy,
              val_inputs=x_test,
              val_outputs=y_test)


if __name__ == "__main__":
    train_mnist()
