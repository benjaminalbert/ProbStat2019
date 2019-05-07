# Probability and Statistics Spring 2019 Project

## Data

[Baltimore police call](https://data.baltimorecity.gov/Public-Safety/911-Police-Calls-for-Service/xviu-ezkt) and [weather](https://mesonet.agron.iastate.edu/request/download.phtml?network=MD_ASOS) data are automatically downloaded using [DataDownloader.java](https://github.com/benjaminalbert/ProbStat2019/blob/master/src/datacollection/DataDownloader.java). These data are updated more than hourly.

Downloaded data is read into [PoliceCall](https://github.com/benjaminalbert/ProbStat2019/blob/master/src/datacollection/PoliceCall.java) and [StationReport](https://github.com/benjaminalbert/ProbStat2019/blob/master/src/datacollection/WeatherReport.java) objects. While reading the data into objects, data is simultaneously filtered according to user parameters (e.g. datetime range, location range, crime severity, etc.)

StationReport objects are then compiled into WeatherReport objects: a WeatherReport selectively aggregates StationReports for a particular datetime range. Using this processes, one can choose to prefer data from one station (s1) over another (s2), but also decide to use s2 data if s1 data is missing. This helps to generate a clean dataset while taking the best available data.

PoliceCall and WeatherReport objects are then aggregated and binned geographically to produce a CSV file for further analysis.

## Deep Learning

Data is first read and formatted for a 2D convolutional long-short term memory network (ConvLSTM).

The input dimensionality is as follows:

|Dimension|Name       |Example|Description
|---------|-----------|-------|---------------------------------|
|0        |Batch      |128    |**Training instances per update**. Keras requires equivalent batch training and batch testing sizes. Increasing batch significantly increases memory dependency: a GTX 1050 with the example parameters could not exceed 256 batch.
|1        |Timesteps  |28     |**LSTM memory.** Each timestep of the data is an aggregation of police call and weather data for 6 hours in domain [0,6), [6,12), [12,18), [18,24). With 28 timesteps per instance, the network will analyze the previous week of data.
|2        |Channels   |16     |**Number of unique spatial features.** These include police calls, temperature (Fahrenheit), relative humidity (%), and precipitation (inches). To include weather forecast in this network, each forecasted variable is given a new channel. For instance, if temperature is to be forecasted 2 timesteps into the future, then instance t-1 that is predicting outputs t and t+1 will have 7 channels: police calls (t-1), humidity (t-1), precipitation (t-1), temperature (t-1), temperature (t), and temperature (t+1). This process is multiplied by the number of timesteps per instance, so variables significantly overlap across timesteps.
|3        |Rows       |10     |**Rows per grid of spatial data.** All variables must be 2D. If a variable is global (e.g. temperature of Baltimore), then the single temperature value of a timestep is transformed into a uniform grid of that temperature.
|4        |Columns    |10     |**Columns per grid of spatial data.** Rows and Columns need not be equal. However, it simplifies data visualizing and processing when a square is used.

The network outputs predicted police call data per timestep (3D if output timesteps > 1, 2D otherwise). The network can be adapted to predict multiple timesteps. For instance, if the network is trained to predict the next day of police calls but the timestep interval is 6 hours, then (assuming rows=columns=10) the network will have 400 output neurons where each set of 100 outputs a predicted timestep.

## Dependencies
* [Apache Commons IO](https://commons.apache.org/proper/commons-io/) (for automatic data downloading)
* [Tensorflow](https://www.tensorflow.org) (with Keras contrib) (developed with version 1.13.1)
* [CUDA 10.0](https://developer.nvidia.com/cuda-10.0-download-archive) (recommended, required for practical training times)
  * **Note: 10.0 is the required version as of this README (5/7/2019). 10.1 is incompatible**
* [TensorBoard](https://github.com/tensorflow/tensorboard) (optional)
